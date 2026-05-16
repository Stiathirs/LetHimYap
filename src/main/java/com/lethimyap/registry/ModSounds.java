package com.lethimyap.registry;

import com.lethimyap.LetHimYap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, LetHimYap.MODID);

    public static final RegistryObject<SoundEvent> YAP =
            SOUND_EVENTS.register("yap", () ->
                    SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(LetHimYap.MODID, "yap")
                    )
            );

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}