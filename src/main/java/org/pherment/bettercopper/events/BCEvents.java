package org.pherment.bettercopper.events;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pherment.bettercopper.Bettercopper;
import org.pherment.bettercopper.Data.ChargedCopperSavedData;
import org.pherment.bettercopper.Helpers.MagneticCoilCharge;
import org.pherment.bettercopper.Helpers.MagneticItems;
import org.pherment.bettercopper.items.BCItems;
import org.spongepowered.asm.mixin.injection.struct.InjectorGroupInfo;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Bettercopper.MODID)
public class BCEvents {
    private static final float LIGHTNING_DAMAGE = 5.0F;

    @SubscribeEvent
    public static void onLightingSpawn(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof LightningBolt lightningBolt)) return;

        Level level = event.getLevel();

        if (level.isClientSide) return;

        BlockPos pos = lightningBolt.blockPosition();

        for (BlockPos checkPos : BlockPos.betweenClosed(
                pos.offset(-1, -2, -1),
                pos.offset(1, 1, 1)
        )) {
            BlockState state = level.getBlockState(checkPos);

            if (state.is(Blocks.COPPER_BLOCK)) {
                ServerLevel serverLevel = (ServerLevel) level;

                long chargeDuration = 20L * 60L * 2L;
                long expireTime = serverLevel.getGameTime() + chargeDuration;

                ChargedCopperSavedData data = ChargedCopperSavedData.get(serverLevel);
                data.charge(checkPos, expireTime);
                break;
            }
        }
    }

    @SubscribeEvent
    public static void electricityCopperBlock(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!(event.level instanceof ServerLevel serverLevel)) {
            return;
        }

        ChargedCopperSavedData data = ChargedCopperSavedData.get(serverLevel);

        long gameTime = serverLevel.getGameTime();

        data.removeExpired(gameTime);

        if (gameTime % 5 != 0) {
            return;
        }

        for (InjectorGroupInfo.Map.Entry<BlockPos, Long> entry : data.getChargedBlocks().entrySet()) {
            BlockPos pos = entry.getKey();

            double x = pos.getX() + 0.5;
            double y = pos.getY() + 1.05;
            double z = pos.getZ() + 0.5;

            serverLevel.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    x, y, z,
                    4,
                    0.35, 0.25, 0.35,
                    0.02
            );
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos pos = event.getPos();
        ChargedCopperSavedData data = ChargedCopperSavedData.get(serverLevel);

        if (data.isCharged(pos)) {
            data.remove(pos);
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        BlockPos pos = event.getPos();
        dischargeIntoPlayer(serverLevel, player, pos);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }

        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        BlockPos playerPos = player.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
                playerPos.offset(-1, -1, -1),
                playerPos.offset(1, 1, 1)
        )) {
            dischargeIntoPlayer(serverLevel, player, pos);
        }
    }

    @SubscribeEvent
    public static void onItemEntityTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!(event.level instanceof ServerLevel serverLevel)) {
            return;
        }

        long gameTime = serverLevel.getGameTime();

        if (gameTime % 5 != 0) {
            return;
        }

        ChargedCopperSavedData data = ChargedCopperSavedData.get(serverLevel);

        for (Entity entity : serverLevel.getAllEntities()) {
            if (!(entity instanceof ItemEntity itemEntity)) {
                continue;
            }

            ItemStack stack = itemEntity.getItem();

            if (!stack.is(BCItems.COIL.get())) {
                continue;
            }

            BlockPos itemPos = itemEntity.blockPosition();

            for (BlockPos checkPos : BlockPos.betweenClosed(
                    itemPos.offset(-1, -1, -1),
                    itemPos.offset(1, 1, 1)
            )) {
                if (!data.isCharged(checkPos)) {
                    continue;
                }

                transformItem(serverLevel, itemEntity, checkPos);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }

        for (Slot slot : event.getContainer().slots) {
            ItemStack stack = slot.getItem();

            if (!stack.is(BCItems.MAGNETIC_COIL.get())) {
                continue;
            }

            if (MagneticCoilCharge.isExpired(stack, serverLevel)) {
                slot.set(MagneticCoilCharge.discharge(stack));
            }
        }
    }

    @SubscribeEvent
    public static void onMagnetPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }

        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!hasChargedCoil(player)) {
            return;
        }

        if (serverLevel.getGameTime() % 2 != 0) {
            return;
        }

        double radius = 5.0;

        AABB area = player.getBoundingBox().inflate(radius);

        List<ItemEntity> items = serverLevel.getEntitiesOfClass(
                ItemEntity.class,
                area,
                itemEntity -> MagneticItems.isMagnetic(itemEntity.getItem().getItem())
        );

        for (ItemEntity itemEntity : items) {
            pullItemToPlayer(player, itemEntity);
        }
    }

    @SubscribeEvent
    public static void onGroundMagnetTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!(event.level instanceof ServerLevel serverLevel)) {
            return;
        }

        // не каждый тик
        if (serverLevel.getGameTime() % 2 != 0) {
            return;
        }

        List<ItemEntity> magneticCoils = serverLevel.getEntitiesOfClass(
                ItemEntity.class,
                new AABB(
                        -30000000, -30000000, -30000000,
                        30000000,  30000000,  30000000
                ),
                itemEntity -> itemEntity.getItem().is(BCItems.MAGNETIC_COIL.get())
        );

        for (ItemEntity coilEntity : magneticCoils) {
            pullItemsToGroundMagnet(serverLevel, coilEntity);
        }
    }

    private static void pullItemsToGroundMagnet(ServerLevel serverLevel, ItemEntity coilEntity) {
        double radius = 3.0;

        AABB area = coilEntity.getBoundingBox().inflate(radius);

        List<ItemEntity> nearbyItems = serverLevel.getEntitiesOfClass(
                ItemEntity.class,
                area,
                itemEntity -> {
                    if (itemEntity == coilEntity) {
                        return false;
                    }

                    ItemStack stack = itemEntity.getItem();

                    // не тянем другие магнитные катушки
                    if (stack.is(BCItems.MAGNETIC_COIL.get())) {
                        return false;
                    }

                    return MagneticItems.isMagnetic(stack.getItem());
                }
        );

        for (ItemEntity targetItem : nearbyItems) {
            pullItemToGroundMagnet(coilEntity, targetItem);
        }
    }

    private static void pullItemToGroundMagnet(ItemEntity magnet, ItemEntity target) {
        ItemStack stack = target.getItem();

        double basePower = MagneticItems.getPower(stack.getItem());

        if (basePower <= 0.0) {
            return;
        }

        double powerMultiplier = 0.45;

        double stackWeight = Math.sqrt(stack.getCount());

        double power = (basePower * powerMultiplier) / stackWeight;

        Vec3 targetPos = magnet.position().add(0.0, 0.1, 0.0);
        Vec3 itemPos = target.position();

        Vec3 direction = targetPos.subtract(itemPos);

        double distance = direction.length();

        if (distance < 0.15) {
            return;
        }

        Vec3 motion = direction.normalize().scale(power);

        target.setDeltaMovement(
                target.getDeltaMovement().add(motion)
        );

        target.hasImpulse = true;
    }

    private static void pullItemToPlayer(ServerPlayer player, ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();

        double power = MagneticItems.getPower(stack.getItem());

        if (power <= 0.0) {
            return;
        }

        Vec3 itemPos = itemEntity.position();
        Vec3 targetPos = player.position().add(0.0, 1.0, 0.0);

        Vec3 direction = targetPos.subtract(itemPos);

        double distance = direction.length();

        if (distance < 0.3) {
            return;
        }

        Vec3 pull = direction.normalize().scale(power);

        itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().add(pull));
        itemEntity.hasImpulse = true;
    }

    private static boolean hasChargedCoil(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(BCItems.MAGNETIC_COIL.get())) {
                return true;
            }
        }

        return false;
    }

    private static void transformItem(ServerLevel serverLevel, ItemEntity itemEntity, BlockPos chargedBlockPos) {
        ChargedCopperSavedData data = ChargedCopperSavedData.get(serverLevel);

        ItemStack oldStack = itemEntity.getItem();

        ItemStack chargedCoil = new ItemStack(BCItems.MAGNETIC_COIL.get(), 1);
        MagneticCoilCharge.setExpireTime(chargedCoil, serverLevel);

        oldStack.shrink(1);

        if (oldStack.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(oldStack);
        }

        ItemEntity chargedItemEntity = new ItemEntity(
                serverLevel,
                itemEntity.getX(),
                itemEntity.getY(),
                itemEntity.getZ(),
                chargedCoil
        );

        chargedItemEntity.setDeltaMovement(
                itemEntity.getDeltaMovement().x,
                0.15,
                itemEntity.getDeltaMovement().z
        );

        serverLevel.addFreshEntity(chargedItemEntity);

        data.remove(chargedBlockPos);

        serverLevel.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                itemEntity.getX(),
                itemEntity.getY() + 0.2,
                itemEntity.getZ(),
                35,
                0.4, 0.4, 0.4,
                0.15
        );

        serverLevel.playSound(
                null,
                itemEntity.getX(),
                itemEntity.getY(),
                itemEntity.getZ(),
                SoundEvents.COPPER_BULB_TURN_ON,
                SoundSource.BLOCKS,
                1.0F,
                1.8F
        );

        serverLevel.playSound(
                null,
                itemEntity.getX(),
                itemEntity.getY(),
                itemEntity.getZ(),
                SoundEvents.BEE_STING,
                SoundSource.BLOCKS,
                0.8F,
                2.0F
        );

        giveAdvancementToOwner(serverLevel, itemEntity);
    }

    private static void giveAdvancementToOwner(ServerLevel serverLevel, ItemEntity itemEntity) {
        UUID ownerUuid = Objects.requireNonNull(itemEntity.getOwner()).getUUID();

        ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(ownerUuid);

        if (player == null) {
            return;
        }

        AdvancementHolder advancement = serverLevel.getServer()
                .getAdvancements()
                .get(ResourceLocation.fromNamespaceAndPath(Bettercopper.MODID, "magnetic_coil"));

        if (advancement == null) {
            return;
        }

        player.getAdvancements().award(advancement, "transform");
    }

    private static void dischargeIntoPlayer(ServerLevel serverLevel, ServerPlayer player, BlockPos pos) {
        ChargedCopperSavedData data = ChargedCopperSavedData.get(serverLevel);

        if (!data.isCharged(pos)) {
            return;
        }

        if (!serverLevel.getBlockState(pos).is(Blocks.COPPER_BLOCK)) {
            data.remove(pos);
            return;
        }

        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.COPPER_BULB_TURN_ON, SoundSource.PLAYERS, 1.0F, 1.8F);
        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEE_STING, SoundSource.PLAYERS, 0.8F, 2.0F);
        player.hurt(serverLevel.damageSources().lightningBolt(), LIGHTNING_DAMAGE);

        serverLevel.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                player.getX(),
                player.getY() + 1.0,
                player.getZ(),
                20,
                0.4, 0.8, 0.4,
                0.15
        );

        data.remove(pos);
    }
}
