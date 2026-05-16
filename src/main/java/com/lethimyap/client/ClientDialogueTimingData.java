package com.lethimyap.client;

import java.util.Random;

public class ClientDialogueTimingData {

    private static final Random RANDOM = new Random();

    public static boolean enabled = true;

    public static int minTicksBetweenMessages = 400;
    public static int maxTicksBetweenMessages = 1200;

    private static int timer = -1;

    public static void update(boolean enabled, int minTicks, int maxTicks) {
        ClientDialogueTimingData.enabled = enabled;
        ClientDialogueTimingData.minTicksBetweenMessages = Math.max(1, minTicks);
        ClientDialogueTimingData.maxTicksBetweenMessages =
                Math.max(ClientDialogueTimingData.minTicksBetweenMessages, maxTicks);

        if (timer < 0) {
            resetTimer();
        }
    }

    public static boolean tickAndShouldRequest() {
        if (!enabled) return false;

        if (timer < 0) {
            resetTimer();
            return false;
        }

        timer--;

        if (timer <= 0) {
            resetTimer();
            return true;
        }

        return false;
    }

    public static void resetTimer() {
        int min = Math.max(1, minTicksBetweenMessages);
        int max = Math.max(min, maxTicksBetweenMessages);

        timer = min + RANDOM.nextInt(max - min + 1);
    }
}