package com.lethimyap.client.config;

import com.lethimyap.api.YapVoice;
import com.lethimyap.api.YapVoiceRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public final class VoicePreviewPlayer {

    private static final RandomSource RANDOM = RandomSource.create();

    private VoicePreviewPlayer() {
    }

    public static void play(String voiceId) {
        YapVoice voice = YapVoiceRegistry.get(voiceId);

        if (voice == null) voice = YapVoiceRegistry.getDefault();
        if (voice == null) return;

        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(voice.soundId());
        if (sound == null) return;

        float pitch = voice.basePitch() * randomPitch(0.9F, 1.15F);

        Minecraft.getInstance().getSoundManager().play(
                new SimpleSoundInstance(
                        sound.getLocation(),
                        SoundSource.PLAYERS,
                        1.5F,
                        pitch,
                        RANDOM,
                        false,
                        0,
                        SimpleSoundInstance.Attenuation.NONE,
                        0.0D,
                        0.0D,
                        0.0D,
                        true
                )
        );
    }

    private static float randomPitch(float min, float max) {
        return min + RANDOM.nextFloat() * (max - min);
    }
}