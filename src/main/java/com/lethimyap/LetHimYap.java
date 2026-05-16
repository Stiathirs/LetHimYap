package com.lethimyap;

import com.lethimyap.api.YapPoolRegistry;
import com.lethimyap.network.ModNetwork;
import com.lethimyap.registry.ModEffects;
import com.lethimyap.registry.ModPotions;
import com.lethimyap.registry.ModSounds;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(LetHimYap.MODID)
public class LetHimYap {

    public static final String MODID = "lethimyap";

    public LetHimYap() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModSounds.register(bus);
        YapPoolRegistry.registerVanillaGlobalDamageBlacklistDefaults();
        ModEffects.register(bus);
        ModPotions.register(bus);
        ModNetwork.register();
    }
}