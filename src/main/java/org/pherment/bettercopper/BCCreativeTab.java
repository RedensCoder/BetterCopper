package org.pherment.bettercopper;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.pherment.bettercopper.items.BCItems;

public class BCCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CreativeTab = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Bettercopper.MODID);

    public static final RegistryObject<CreativeModeTab> BCTab = CreativeTab.register("bc_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(BCItems.MAGNETIC_COIL.get()))
                    .title(Component.translatable("itemGroup.bc_tab"))
                    .displayItems(((parametrs, output) -> {
                        output.accept(BCItems.WIRE.get());
                        output.accept(BCItems.COIL.get());
                        output.accept(BCItems.MAGNETIC_COIL.get());
                    }))
                    .build());

    public static void register(IEventBus eventBus) {
        CreativeTab.register(eventBus);
    }
}
