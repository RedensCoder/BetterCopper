package org.pherment.bettercopper.Data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ChargedCopperSavedData extends SavedData {
    private static final String DATA_NAME = "charged_copper";

    // BlockPos -> gameTime когда заряд спадёт
    private final Map<BlockPos, Long> chargedBlocks = new HashMap<>();

    public static ChargedCopperSavedData get(net.minecraft.server.level.ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        ChargedCopperSavedData::new,
                        ChargedCopperSavedData::load,
                        null
                ),
                DATA_NAME
        );
    }

    public void charge(BlockPos pos, long expireTime) {
        chargedBlocks.put(pos.immutable(), expireTime);
        setDirty();
    }

    public boolean isCharged(BlockPos pos) {
        return chargedBlocks.containsKey(pos);
    }

    public Map<BlockPos, Long> getChargedBlocks() {
        return chargedBlocks;
    }

    public void remove(BlockPos pos) {
        chargedBlocks.remove(pos);
        setDirty();
    }

    public void removeExpired(long gameTime) {
        Iterator<Map.Entry<BlockPos, Long>> iterator = chargedBlocks.entrySet().iterator();

        boolean changed = false;

        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Long> entry = iterator.next();

            if (entry.getValue() <= gameTime) {
                iterator.remove();
                changed = true;
            }
        }

        if (changed) {
            setDirty();
        }
    }

    public static ChargedCopperSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        ChargedCopperSavedData data = new ChargedCopperSavedData();

        ListTag list = tag.getList("ChargedBlocks", Tag.TAG_COMPOUND);

        for (Tag rawTag : list) {
            CompoundTag blockTag = (CompoundTag) rawTag;

            BlockPos pos = BlockPos.of(blockTag.getLong("Pos"));
            long expireTime = blockTag.getLong("ExpireTime");

            data.chargedBlocks.put(pos, expireTime);
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();

        for (Map.Entry<BlockPos, Long> entry : chargedBlocks.entrySet()) {
            CompoundTag blockTag = new CompoundTag();

            blockTag.putLong("Pos", entry.getKey().asLong());
            blockTag.putLong("ExpireTime", entry.getValue());

            list.add(blockTag);
        }

        tag.put("ChargedBlocks", list);

        return tag;
    }
}
