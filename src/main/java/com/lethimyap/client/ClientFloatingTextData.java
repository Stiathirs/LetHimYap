package com.lethimyap.client;

import com.lethimyap.messages.DialogueColor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ClientFloatingTextData {

    private static final Map<UUID, FloatingText> TEXTS = new HashMap<>();

    public static void set(UUID playerId, String text, String color, int ticks) {
        TEXTS.put(playerId, new FloatingText(
                text,
                DialogueColor.fromString(color),
                ticks
        ));
    }

    public static void clear(UUID playerId) {
        TEXTS.remove(playerId);
    }

    public static FloatingText get(UUID playerId) {
        return TEXTS.get(playerId);
    }

    public static void tick() {
        Iterator<Map.Entry<UUID, FloatingText>> iterator = TEXTS.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, FloatingText> entry = iterator.next();

            entry.getValue().ticks--;

            if (entry.getValue().ticks <= 0) {
                iterator.remove();
            }
        }
    }

    public static class FloatingText {
        public final String text;
        public final DialogueColor color;
        public int ticks;
        public final int maxTicks;

        public FloatingText(String text, DialogueColor color, int ticks) {
            this.text = text;
            this.color = color;
            this.ticks = ticks;
            this.maxTicks = ticks;
        }

        public float alpha(int fadeTicks) {
            if (fadeTicks <= 0) return 1.0f;
            if (ticks >= fadeTicks) return 1.0f;

            return Math.max(0.0f, ticks / (float) fadeTicks);
        }
    }
}