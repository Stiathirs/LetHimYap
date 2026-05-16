package com.lethimyap.registry;

import com.lethimyap.LetHimYap;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModPotions {

    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(ForgeRegistries.POTIONS, LetHimYap.MODID);

    public static final RegistryObject<Potion> SILENCE =
            POTIONS.register("silence", () ->
                    new Potion(
                            new MobEffectInstance(
                                    ModEffects.SILENCED.get(),
                                    20 * 60
                            )
                    )
            );

    public static final RegistryObject<Potion> LONG_SILENCE =
            POTIONS.register("long_silence", () ->
                    new Potion(
                            "silence",
                            new MobEffectInstance(
                                    ModEffects.SILENCED.get(),
                                    20 * 60 * 3
                            )
                    )
            );

    public static void register(IEventBus bus) {
        POTIONS.register(bus);
    }
}