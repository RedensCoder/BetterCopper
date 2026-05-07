package org.pherment.bettercopper.Data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CopperLocksSavedData extends SavedData {
    private static final String DATA_NAME = "copper_locks";

    private final Map<BlockPos, CopperLocksData> locks = new HashMap<>();

    public static CopperLocksSavedData get(net.minecraft.server.level.ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        CopperLocksSavedData::new,
                        CopperLocksSavedData::load,
                        null
                ),
                DATA_NAME
        );
    }

    public boolean hasLock(BlockPos pos) {
        return locks.containsKey(pos);
    }

    public void addLock(BlockPos pos, UUID ownerUuid, UUID lockUuid) {
        locks.put(pos.immutable(), new CopperLocksData(ownerUuid, lockUuid, false));
        setDirty();
    }

    public void removeLock(BlockPos pos) {
        locks.remove(pos);
        setDirty();
    }

    public void setLocked(BlockPos pos, boolean locked) {
        CopperLocksData old = locks.get(pos);

        if (old == null) {
            return;
        }

        locks.put(pos.immutable(), new CopperLocksData(
                old.ownerUuid(),
                old.lockUuid(),
                locked
        ));

        setDirty();
    }

    public CopperLocksData getLock(BlockPos pos) {
        return locks.get(pos);
    }

    public Map<BlockPos, CopperLocksData> getLocks() {
        return locks;
    }

    public static CopperLocksSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        CopperLocksSavedData data = new CopperLocksSavedData();

        ListTag list = tag.getList("Locks", Tag.TAG_COMPOUND);

        for (Tag raw : list) {
            CompoundTag lockTag = (CompoundTag) raw;

            BlockPos pos = BlockPos.of(lockTag.getLong("Pos"));
            UUID ownerUuid = lockTag.getUUID("OwnerUUID");
            UUID lockUuid = lockTag.getUUID("LockUUID");

            boolean locked = lockTag.getBoolean("Locked");
            data.locks.put(pos, new CopperLocksData(ownerUuid, lockUuid, locked));
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();

        for (Map.Entry<BlockPos, CopperLocksData> entry : locks.entrySet()) {
            CompoundTag lockTag = new CompoundTag();

            lockTag.putLong("Pos", entry.getKey().asLong());
            lockTag.putUUID("OwnerUUID", entry.getValue().ownerUuid());
            lockTag.putUUID("LockUUID", entry.getValue().lockUuid());
            lockTag.putBoolean("Locked", entry.getValue().locked());

            list.add(lockTag);
        }

        tag.put("Locks", list);
        return tag;
    }
}
