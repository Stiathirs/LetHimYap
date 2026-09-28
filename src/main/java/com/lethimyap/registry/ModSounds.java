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

    public static final RegistryObject<SoundEvent> CLICK =
            SOUND_EVENTS.register("click", () ->
                    SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(LetHimYap.MODID, "click")
                    )
            );
    public static final RegistryObject<SoundEvent> BEEP =
            SOUND_EVENTS.register("beep", () ->
                    SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(LetHimYap.MODID, "beep")
                    )
            );
    public static final RegistryObject<SoundEvent> BUZZ =
            SOUND_EVENTS.register("buzz", () ->
                    SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(LetHimYap.MODID, "buzz")
                    )
            );
    public static final RegistryObject<SoundEvent> CLACK =
            SOUND_EVENTS.register("clack", () ->
                    SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(LetHimYap.MODID, "clack")
                    )
            );
    public static final RegistryObject<SoundEvent> KEYBOARD =
            SOUND_EVENTS.register("keyboard", () ->
                    SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(LetHimYap.MODID, "keyboard")
                    )
            );
            
            

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}