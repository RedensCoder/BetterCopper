package org.pherment.bettercopper.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pherment.bettercopper.Bettercopper;
import org.pherment.bettercopper.ChargedCopperSavedData;
import org.spongepowered.asm.mixin.injection.struct.InjectorGroupInfo;

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
