package com.lethimyap.registry;

import com.lethimyap.LetHimYap;
import com.lethimyap.effect.SilencedEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, LetHimYap.MODID);

    public static final RegistryObject<MobEffect> SILENCED =
            EFFECTS.register("silenced", SilencedEffect::new);

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}