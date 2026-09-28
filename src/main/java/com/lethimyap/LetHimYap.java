package com.lethimyap;

import com.lethimyap.api.YapPoolRegistry;
import com.lethimyap.api.LetHimYapApi;
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

    public LetHimYap(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();

        ModSounds.register(bus);

        LetHimYapApi.registerVoice(
                "lethimyap:click",
                "Click",
                "A soft clicking sound. Classic.",
                "lethimyap:click",
                1.0F
        );

        LetHimYapApi.registerVoice(
                "lethimyap:deep_click",
                "Deep Click",
                "Same as the basic Click, just pitched down.",
                "lethimyap:click",
                0.65F
        );

        LetHimYapApi.registerVoice(
                "lethimyap:high_click",
                "High Click",
                "Same as the basic Click, just pitched up.",
                "lethimyap:click",
                1.2F
        );

        LetHimYapApi.registerVoice(
                "lethimyap:clack",
                "Clack",
                "A clacking sound, like a key being pressed.",
                "lethimyap:clack",
                1.0F
        );

        LetHimYapApi.registerVoice(
                "lethimyap:keyboard",
                "Keyboard",
                "The sound of a spacebar being released.",
                "lethimyap:keyboard",
                1.0F
        );

        LetHimYapApi.registerVoice(
                "lethimyap:beep",
                "Beep",
                "A robotic beep sound.",
                "lethimyap:beep",
                0.5F
        );

        LetHimYapApi.registerVoice(
                "lethimyap:buzz",
                "Buzz",
                "A digital buzzing sound.",
                "lethimyap:buzz",
                1.0F
        );

        YapPoolRegistry.registerVanillaGlobalDamageBlacklistDefaults();
        ModEffects.register(bus);
        ModPotions.register(bus);
        ModNetwork.register();
    }
}