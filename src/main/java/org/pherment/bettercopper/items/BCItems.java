package org.pherment.bettercopper.items;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.pherment.bettercopper.Bettercopper;

public class BCItems {
    public static final DeferredRegister<Item> ITEM = DeferredRegister.create(ForgeRegistries.ITEMS, Bettercopper.MODID);

    // Items
    public static final RegistryObject<Item> WIRE = ITEM.register("wire",
            () -> new Item(new Item.Properties()));

    public static void register(IEventBus bus) {
        ITEM.register(bus);
    }
}
