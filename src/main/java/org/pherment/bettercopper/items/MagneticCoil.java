package org.pherment.bettercopper.items;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.pherment.bettercopper.Helpers.MagneticCoilCharge;

public class MagneticCoil extends Item {
    public MagneticCoil(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!(entity instanceof Player player)) {
            return;
        }

        if (!MagneticCoilCharge.isExpired(stack, serverLevel)) {
            return;
        }

        player.getInventory().setItem(slotId, MagneticCoilCharge.discharge(stack));
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            if (MagneticCoilCharge.isExpired(stack, serverLevel)) {
                entity.setItem(MagneticCoilCharge.discharge(stack));
            }
        }

        return false;
    }
}
