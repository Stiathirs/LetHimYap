package com.lethimyap.api;

import net.minecraft.resources.ResourceLocation;

public record YapVoice(
        ResourceLocation id,
        String displayName,
        String description,
        ResourceLocation soundId,
        float basePitch
) {

    public YapVoice {
        if (id == null) {
            throw new IllegalArgumentException("Voice ID cannot be null.");
        }

        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Voice display name cannot be blank.");
        }

        if (description == null) {
            description = "";
        }

        if (soundId == null) {
            throw new IllegalArgumentException("Voice sound ID cannot be null.");
        }

        if (!Float.isFinite(basePitch) || basePitch <= 0.0F) {
            throw new IllegalArgumentException(
                    "Voice base pitch must be finite and greater than 0."
            );
        }
    }
}