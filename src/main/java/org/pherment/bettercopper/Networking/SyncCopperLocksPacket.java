package org.pherment.bettercopper.Networking;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import org.pherment.bettercopper.Helpers.ClientCopperLocks;

import java.util.HashSet;
import java.util.Set;

public class SyncCopperLocksPacket {
    private final Set<BlockPos> locks;

    public SyncCopperLocksPacket(Set<BlockPos> locks) {
        this.locks = new HashSet<>(locks);
    }

    public static void encode(SyncCopperLocksPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.locks.size());

        for (BlockPos pos : packet.locks) {
            buf.writeBlockPos(pos);
        }
    }

    public static SyncCopperLocksPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();

        Set<BlockPos> locks = new HashSet<>();

        for (int i = 0; i < size; i++) {
            locks.add(buf.readBlockPos());
        }

        return new SyncCopperLocksPacket(locks);
    }

    public static void handle(SyncCopperLocksPacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ClientCopperLocks.setLocks(packet.locks);
        });

        context.setPacketHandled(true);
    }
}
