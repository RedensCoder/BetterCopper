package org.pherment.bettercopper.Helpers;

import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.Set;

public class ClientCopperLocks {
    public static final Set<BlockPos> LOCKS = new HashSet<>();

    public static void setLocks(Set<BlockPos> locks) {
        LOCKS.clear();
        LOCKS.addAll(locks);
    }

    public static void add(BlockPos pos) {
        LOCKS.add(pos.immutable());
    }

    public static void remove(BlockPos pos) {
        LOCKS.remove(pos);
    }
}
