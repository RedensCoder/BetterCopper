package org.pherment.bettercopper.events;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pherment.bettercopper.Bettercopper;
import org.pherment.bettercopper.Data.CopperLocksData;
import org.pherment.bettercopper.Data.CopperLocksSavedData;
import org.pherment.bettercopper.Helpers.ClientCopperLocks;
import org.pherment.bettercopper.Helpers.CopperKeyData;
import org.pherment.bettercopper.Networking.BCNetworking;
import org.pherment.bettercopper.items.BCItems;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Bettercopper.MODID)
public class CopperLocksEvents {
    @SubscribeEvent
    public static void onRightClickStorage(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack stack = event.getItemStack();

        BlockPos clickedPos = event.getPos();
        BlockState state = serverLevel.getBlockState(clickedPos);

        if (!isStorageBlock(state)) {
            return;
        }

        BlockPos lockPos = getLockStoragePos(state, clickedPos);

        CopperLocksSavedData data = CopperLocksSavedData.get(serverLevel);
        CopperLocksData lockData = data.getLock(lockPos);

        // =========================================================
        // ПРИВЯЗКА КЛЮЧА
        // =========================================================

        if (stack.is(BCItems.COPPER_KEY.get())) {

            if (lockData == null) {
                return;
            }

            // Если ключ уже подходит к этому замку — разрешаем открыть сундук
            if (CopperKeyData.matches(stack, lockData.lockUuid())) {
                return;
            }

            // Если ключ НЕ подходит, но игрок НЕ владелец замка — запрещаем
            if (!lockData.ownerUuid().equals(player.getUUID())) {
                serverLevel.playSound(
                        null,
                        clickedPos,
                        SoundEvents.CHAIN_HIT,
                        SoundSource.BLOCKS,
                        1.0F,
                        0.8F
                );

                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
                return;
            }

            // Игрок владелец — можно привязать или перепривязать ключ
            CopperKeyData.bind(stack, lockData.lockUuid());

            data.setLocked(lockPos, true);

            serverLevel.playSound(
                    null,
                    clickedPos,
                    SoundEvents.CHAIN_PLACE,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.5F
            );

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        // =========================================================
        // УСТАНОВКА ЗАМКА
        // =========================================================

        if (stack.is(BCItems.COPPER_LOCK.get())) {

            // Уже есть замок
            if (lockData != null) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
                return;
            }

            UUID ownerUuid = player.getUUID();
            UUID lockUuid = UUID.randomUUID();

            data.addLock(lockPos, ownerUuid, lockUuid);

            BCNetworking.sendAllLocksToDimension(
                    serverLevel,
                    data.getLocks().keySet()
            );

            if (!player.isCreative()) {
                stack.shrink(1);
            }

            serverLevel.playSound(
                    null,
                    lockPos,
                    SoundEvents.CHAIN_PLACE,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.2F
            );

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        // =========================================================
        // ПРОВЕРКА ДОСТУПА
        // =========================================================

        if (lockData != null && lockData.locked()) {

            if (!hasMatchingKey(player, lockData.lockUuid())) {

                serverLevel.playSound(
                        null,
                        clickedPos,
                        SoundEvents.CHAIN_HIT,
                        SoundSource.BLOCKS,
                        1.0F,
                        0.8F
                );

                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null) {
            return;
        }

        PoseStack poseStack = new PoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        ItemRenderer itemRenderer = mc.getItemRenderer();

        Vec3 camera = event.getCamera().getPosition();

        for (BlockPos pos : ClientCopperLocks.LOCKS) {
            BlockState state = mc.level.getBlockState(pos);

            Direction facing = getFacing(state);

            poseStack.pushPose();

            Vec3 renderPos = getLockRenderPos(mc.level, pos, state, facing);

            double x = renderPos.x - camera.x;
            double y = renderPos.y - camera.y;
            double z = renderPos.z - camera.z;

            poseStack.translate(x, y, z);

            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));

            poseStack.scale(0.45F, 0.45F, 0.45F);

            ItemStack lockStack = new ItemStack(BCItems.COPPER_LOCK.get());

            itemRenderer.renderStatic(
                    lockStack,
                    ItemDisplayContext.FIXED,
                    15728880,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    mc.level,
                    0
            );

            poseStack.popPose();
        }

        buffer.endBatch();
    }

    @SubscribeEvent
    public static void onStorageBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos brokenPos = event.getPos();
        BlockState state = serverLevel.getBlockState(brokenPos);

        CopperLocksSavedData data = CopperLocksSavedData.get(serverLevel);

        BlockPos lockPos = getLockStoragePos(state, brokenPos);

        if (!data.hasLock(lockPos)) {
            return;
        }

        data.removeLock(lockPos);

        serverLevel.playSound(
                null,
                brokenPos,
                SoundEvents.CHAIN_BREAK,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );

        serverLevel.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                brokenPos.getX() + 0.5,
                brokenPos.getY() + 0.5,
                brokenPos.getZ() + 0.5,
                16,
                0.3, 0.3, 0.3,
                0.03
        );

        BCNetworking.sendAllLocksToDimension(
                serverLevel,
                data.getLocks().keySet()
        );
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ServerLevel serverLevel = player.serverLevel();

        CopperLocksSavedData data = CopperLocksSavedData.get(serverLevel);

        BCNetworking.sendAllLocksToPlayer(player, data.getLocks().keySet());
    }

    private static Direction getFacing(BlockState state) {
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            return state.getValue(HorizontalDirectionalBlock.FACING);
        }

        if (state.hasProperty(DirectionalBlock.FACING)) {
            return state.getValue(DirectionalBlock.FACING);
        }

        return Direction.NORTH;
    }

    private static boolean isStorageBlock(BlockState state) {
        Block block = state.getBlock();

        return block instanceof ChestBlock
                || block instanceof BarrelBlock
                || block instanceof ShulkerBoxBlock
                || block instanceof EnderChestBlock;
    }

    private static BlockPos getLockStoragePos(BlockState state, BlockPos pos) {
        if (!(state.getBlock() instanceof ChestBlock)) {
            return pos;
        }

        if (!state.hasProperty(ChestBlock.TYPE) || !state.hasProperty(ChestBlock.FACING)) {
            return pos;
        }

        ChestType type = state.getValue(ChestBlock.TYPE);
        Direction facing = state.getValue(ChestBlock.FACING);

        if (type == ChestType.SINGLE) {
            return pos;
        }

        Direction connectedDirection = ChestBlock.getConnectedDirection(state);
        BlockPos otherPos = pos.relative(connectedDirection);

        return pos.asLong() < otherPos.asLong() ? pos : otherPos;
    }

    private static Vec3 getLockRenderPos(Level level, BlockPos pos, BlockState state, Direction facing) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.45;
        double z = pos.getZ() + 0.5;

        if (state.getBlock() instanceof ChestBlock && state.hasProperty(ChestBlock.TYPE)) {
            ChestType type = state.getValue(ChestBlock.TYPE);

            if (type != ChestType.SINGLE) {
                Direction connectedDirection = ChestBlock.getConnectedDirection(state);
                BlockPos otherPos = pos.relative(connectedDirection);

                x = (pos.getX() + otherPos.getX()) / 2.0 + 0.5;
                z = (pos.getZ() + otherPos.getZ()) / 2.0 + 0.5;
            }
        }

        x += facing.getStepX() * 0.53;
        z += facing.getStepZ() * 0.53;

        return new Vec3(x, y, z);
    }

    private static boolean hasMatchingKey(ServerPlayer player, UUID lockUuid) {
        for (ItemStack stack : player.getInventory().items) {
            if (CopperKeyData.matches(stack, lockUuid)) {
                return true;
            }
        }

        for (ItemStack stack : player.getInventory().offhand) {
            if (CopperKeyData.matches(stack, lockUuid)) {
                return true;
            }
        }

        return false;
    }
}
