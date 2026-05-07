package org.pherment.bettercopper.Helpers;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.pherment.bettercopper.items.BCItems;

import java.util.UUID;

public class CopperKeyData {
    private static final String LOCK_UUID = "LockUUID";

    public static boolean isBound(ItemStack stack) {
        if (!stack.is(BCItems.COPPER_KEY.get())) {
            return false;
        }

        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.hasUUID(LOCK_UUID);
    }

    public static void bind(ItemStack stack, UUID lockUuid) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putUUID(LOCK_UUID, lockUuid);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static boolean matches(ItemStack stack, UUID lockUuid) {
        if (!stack.is(BCItems.COPPER_KEY.get())) {
            return false;
        }

        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        if (!tag.hasUUID(LOCK_UUID)) {
            return false;
        }

        return tag.getUUID(LOCK_UUID).equals(lockUuid);
    }
}
