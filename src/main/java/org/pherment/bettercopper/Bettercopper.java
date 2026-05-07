package org.pherment.bettercopper;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.pherment.bettercopper.Networking.BCNetworking;
import org.pherment.bettercopper.items.BCItems;import org.slf4j.Logger;

@Mod(Bettercopper.MODID)
public class Bettercopper {
    public static final String MODID = "bettercopper";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Bettercopper(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);

        // Register
        BCCreativeTab.register(modEventBus);
        BCNetworking.register();

        BCItems.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }
}
