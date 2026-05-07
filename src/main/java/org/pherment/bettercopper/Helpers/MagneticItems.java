package org.pherment.bettercopper.Helpers;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.pherment.bettercopper.items.BCItems;

import java.util.HashMap;
import java.util.Map;

public class MagneticItems {
    private static final Map<Item, Double> MAGNETIC_POWER = new HashMap<>();

    static {
        MAGNETIC_POWER.put(BCItems.WIRE.get(), 0.15);
        MAGNETIC_POWER.put(BCItems.COIL.get(), 0.11);
        MAGNETIC_POWER.put(BCItems.COPPER_LOCK.get(), 0.11);
        MAGNETIC_POWER.put(BCItems.COPPER_KEY.get(), 0.13);
        MAGNETIC_POWER.put(BCItems.MAGNETIC_COIL.get(), 0.20);

        MAGNETIC_POWER.put(Items.IRON_NUGGET, 0.13);
        MAGNETIC_POWER.put(Items.IRON_INGOT, 0.09);
        MAGNETIC_POWER.put(Items.RAW_IRON, 0.085);
        MAGNETIC_POWER.put(Items.IRON_BLOCK, 0.035);

        MAGNETIC_POWER.put(Items.IRON_BARS, 0.075);
        MAGNETIC_POWER.put(Items.CHAIN, 0.10);
        MAGNETIC_POWER.put(Items.HEAVY_WEIGHTED_PRESSURE_PLATE, 0.055);

        MAGNETIC_POWER.put(Items.IRON_SWORD, 0.065);
        MAGNETIC_POWER.put(Items.IRON_PICKAXE, 0.06);
        MAGNETIC_POWER.put(Items.IRON_AXE, 0.06);
        MAGNETIC_POWER.put(Items.IRON_SHOVEL, 0.065);
        MAGNETIC_POWER.put(Items.IRON_HOE, 0.065);

        MAGNETIC_POWER.put(Items.IRON_HELMET, 0.055);
        MAGNETIC_POWER.put(Items.IRON_CHESTPLATE, 0.04);
        MAGNETIC_POWER.put(Items.IRON_LEGGINGS, 0.045);
        MAGNETIC_POWER.put(Items.IRON_BOOTS, 0.055);

        MAGNETIC_POWER.put(Items.SHIELD, 0.04);
        MAGNETIC_POWER.put(Items.BUCKET, 0.08);
        MAGNETIC_POWER.put(Items.WATER_BUCKET, 0.045);
        MAGNETIC_POWER.put(Items.LAVA_BUCKET, 0.04);
        MAGNETIC_POWER.put(Items.MILK_BUCKET, 0.045);

        MAGNETIC_POWER.put(Items.MINECART, 0.04);
        MAGNETIC_POWER.put(Items.HOPPER_MINECART, 0.035);
        MAGNETIC_POWER.put(Items.CHEST_MINECART, 0.035);
        MAGNETIC_POWER.put(Items.FURNACE_MINECART, 0.035);
        MAGNETIC_POWER.put(Items.TNT_MINECART, 0.035);

        MAGNETIC_POWER.put(Items.HOPPER, 0.04);
        MAGNETIC_POWER.put(Items.ANVIL, 0.025);
        MAGNETIC_POWER.put(Items.CHIPPED_ANVIL, 0.025);
        MAGNETIC_POWER.put(Items.DAMAGED_ANVIL, 0.025);
        MAGNETIC_POWER.put(Items.CAULDRON, 0.04);

        MAGNETIC_POWER.put(Items.COPPER_INGOT, 0.075);
        MAGNETIC_POWER.put(Items.RAW_COPPER, 0.07);
        MAGNETIC_POWER.put(Items.COPPER_BLOCK, 0.03);
        MAGNETIC_POWER.put(Items.RAW_COPPER_BLOCK, 0.028);

        MAGNETIC_POWER.put(Items.COPPER_BULB, 0.045);
        MAGNETIC_POWER.put(Items.COPPER_GRATE, 0.05);
        MAGNETIC_POWER.put(Items.COPPER_DOOR, 0.04);
        MAGNETIC_POWER.put(Items.COPPER_TRAPDOOR, 0.05);
        MAGNETIC_POWER.put(Items.LIGHTNING_ROD, 0.08);

        MAGNETIC_POWER.put(Items.CUT_COPPER, 0.035);
        MAGNETIC_POWER.put(Items.CUT_COPPER_STAIRS, 0.035);
        MAGNETIC_POWER.put(Items.CUT_COPPER_SLAB, 0.045);

        MAGNETIC_POWER.put(Items.EXPOSED_COPPER, 0.03);
        MAGNETIC_POWER.put(Items.WEATHERED_COPPER, 0.03);
        MAGNETIC_POWER.put(Items.OXIDIZED_COPPER, 0.03);

        MAGNETIC_POWER.put(Items.GOLD_NUGGET, 0.08);
        MAGNETIC_POWER.put(Items.GOLD_INGOT, 0.045);
        MAGNETIC_POWER.put(Items.RAW_GOLD, 0.04);
        MAGNETIC_POWER.put(Items.GOLD_BLOCK, 0.018);
        MAGNETIC_POWER.put(Items.RAW_GOLD_BLOCK, 0.018);

        MAGNETIC_POWER.put(Items.GOLDEN_SWORD, 0.04);
        MAGNETIC_POWER.put(Items.GOLDEN_PICKAXE, 0.04);
        MAGNETIC_POWER.put(Items.GOLDEN_AXE, 0.04);
        MAGNETIC_POWER.put(Items.GOLDEN_SHOVEL, 0.045);
        MAGNETIC_POWER.put(Items.GOLDEN_HOE, 0.045);

        MAGNETIC_POWER.put(Items.GOLDEN_HELMET, 0.035);
        MAGNETIC_POWER.put(Items.GOLDEN_CHESTPLATE, 0.025);
        MAGNETIC_POWER.put(Items.GOLDEN_LEGGINGS, 0.03);
        MAGNETIC_POWER.put(Items.GOLDEN_BOOTS, 0.035);

        MAGNETIC_POWER.put(Items.LIGHT_WEIGHTED_PRESSURE_PLATE, 0.045);

        MAGNETIC_POWER.put(Items.NETHERITE_INGOT, 0.03);
        MAGNETIC_POWER.put(Items.NETHERITE_SCRAP, 0.035);
        MAGNETIC_POWER.put(Items.NETHERITE_BLOCK, 0.012);

        MAGNETIC_POWER.put(Items.NETHERITE_SWORD, 0.025);
        MAGNETIC_POWER.put(Items.NETHERITE_PICKAXE, 0.025);
        MAGNETIC_POWER.put(Items.NETHERITE_AXE, 0.025);
        MAGNETIC_POWER.put(Items.NETHERITE_SHOVEL, 0.03);
        MAGNETIC_POWER.put(Items.NETHERITE_HOE, 0.03);

        MAGNETIC_POWER.put(Items.NETHERITE_HELMET, 0.022);
        MAGNETIC_POWER.put(Items.NETHERITE_CHESTPLATE, 0.016);
        MAGNETIC_POWER.put(Items.NETHERITE_LEGGINGS, 0.018);
        MAGNETIC_POWER.put(Items.NETHERITE_BOOTS, 0.022);

        MAGNETIC_POWER.put(Items.COMPASS, 0.09);
        MAGNETIC_POWER.put(Items.CLOCK, 0.05);
        MAGNETIC_POWER.put(Items.SHEARS, 0.07);
        MAGNETIC_POWER.put(Items.FLINT_AND_STEEL, 0.07);

        MAGNETIC_POWER.put(Items.REDSTONE, 0.035);
    }

    public static boolean isMagnetic(Item item) {
        return MAGNETIC_POWER.containsKey(item);
    }

    public static double getPower(Item item) {
        return MAGNETIC_POWER.getOrDefault(item, 0.0);
    }
}
