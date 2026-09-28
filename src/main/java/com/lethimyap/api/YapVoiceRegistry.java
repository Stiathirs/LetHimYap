package com.lethimyap.api;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class YapVoiceRegistry {

    public static final ResourceLocation DEFAULT_VOICE_ID =
            ResourceLocation.fromNamespaceAndPath(
                    "lethimyap",
                    "click"
            );

    private static final Map<ResourceLocation, YapVoice> VOICES =
            new LinkedHashMap<>();

    private YapVoiceRegistry() {
    }

    public static void register(YapVoice voice) {
        if (voice == null) {
            throw new IllegalArgumentException("Voice cannot be null.");
        }

        if (VOICES.containsKey(voice.id())) {
            throw new IllegalArgumentException(
                    "Voice already registered: " + voice.id()
            );
        }

        VOICES.put(
                voice.id(),
                voice
        );
    }

    public static YapVoice get(ResourceLocation id) {
        return VOICES.get(id);
    }

    public static YapVoice get(String id) {
        ResourceLocation parsed =
                ResourceLocation.tryParse(id);

        return parsed != null
                ? get(parsed)
                : null;
    }

    public static YapVoice getDefault() {
        return get(DEFAULT_VOICE_ID);
    }

    public static List<YapVoice> getVoices() {
        return Collections.unmodifiableList(
                new ArrayList<>(VOICES.values())
        );
    }

    public static boolean contains(ResourceLocation id) {
        return VOICES.containsKey(id);
    }
}