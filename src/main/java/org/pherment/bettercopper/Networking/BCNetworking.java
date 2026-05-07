package org.pherment.bettercopper.Networking;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import org.pherment.bettercopper.Bettercopper;

import java.util.Set;

public class BCNetworking {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(Bettercopper.MODID, "main"))
            .networkProtocolVersion(1)
            .clientAcceptedVersions((status, version) -> true)
            .serverAcceptedVersions((status, version) -> true)
            .simpleChannel();

    private static int packetId = 0;

    public static void register() {
        CHANNEL.messageBuilder(SyncCopperLocksPacket.class, packetId)
                .encoder(SyncCopperLocksPacket::encode)
                .decoder(SyncCopperLocksPacket::decode)
                .consumerMainThread(SyncCopperLocksPacket::handle)
                .add();

        packetId++;
    }

    public static void sendAllLocksToPlayer(ServerPlayer player, Set<BlockPos> locks) {
        CHANNEL.send(
                new SyncCopperLocksPacket(locks),
                PacketDistributor.PLAYER.with(player)
        );
    }

    public static void sendAllLocksToDimension(ServerLevel level, Set<BlockPos> locks) {
        CHANNEL.send(
                new SyncCopperLocksPacket(locks),
                PacketDistributor.DIMENSION.with(level.dimension())
        );
    }
}
