package org.pherment.bettercopper.Helpers;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.pherment.bettercopper.items.BCItems;

public class MagneticCoilCharge {
    private static final String CHARGED_UNTIL = "ChargedUntil";

    public static final long CHARGE_DURATION = 20L * 60L;
    // public static final long CHARGE_DURATION = 24000L;

    public static void setExpireTime(ItemStack stack, ServerLevel level) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putLong(CHARGED_UNTIL, level.getGameTime() + CHARGE_DURATION);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static boolean isExpired(ItemStack stack, ServerLevel level) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        if (!tag.contains(CHARGED_UNTIL)) {
            return false;
        }

        return level.getGameTime() >= tag.getLong(CHARGED_UNTIL);
    }

    public static ItemStack discharge(ItemStack chargedStack) {
        return new ItemStack(BCItems.COIL.get(), chargedStack.getCount());
    }
}
