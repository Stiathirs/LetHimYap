package com.lethimyap.client;

import com.lethimyap.messages.DialogueColor;
import com.lethimyap.messages.ServerMessageConfig;

public class ClientSpeechHudData {

    public static String text = "";
    public static DialogueColor color = DialogueColor.WHITE;
    public static int ticks = 0;
    public static int maxTicks = 0;

    public static void set(String message, String colorId, int duration) {
        text = message == null ? "" : message;
        color = DialogueColor.fromString(colorId);
        ticks = Math.max(0, duration);
        maxTicks = ticks;
    }

    public static void tick() {
        if (ticks > 0) {
            ticks--;
        }

        if (ticks <= 0) {
            text = "";
        }
    }

    public static float alpha() {
        int fadeTicks = Math.max(1, ServerMessageConfig.get().floatingMessageFadeTicks);

        if (ticks <= 0) return 0.0f;
        if (ticks >= fadeTicks) return 1.0f;

        return ticks / (float) fadeTicks;
    }
}