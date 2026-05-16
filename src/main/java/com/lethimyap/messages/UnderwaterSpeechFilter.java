package com.lethimyap.messages;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Random;

public class UnderwaterSpeechFilter {

    private static final Random RANDOM = new Random();

    private static final String[] SYLLABLES = {
            "blg",
            "blub",
            "glub",
            "ub",
            "bul",
            "blubul",
            "glg"
    };

    public static String apply(ServerPlayer player, String original) {
        if (!shouldMuffle(player)) {
            return original;
        }

        return garble(original);
    }

    private static boolean shouldMuffle(ServerPlayer player) {
        if (!player.isUnderWater()) return false;
        if (player.hasEffect(MobEffects.WATER_BREATHING)) return false;
        if (hasAquaAffinityHelmet(player)) return false;

        return true;
    }

    private static boolean hasAquaAffinityHelmet(ServerPlayer player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);

        if (helmet.isEmpty()) return false;

        return helmet.getEnchantmentLevel(Enchantments.AQUA_AFFINITY) > 0;
    }

    private static boolean isPreservedPunctuation(char c) {
        return !Character.isLetterOrDigit(c)
                && !Character.isWhitespace(c)
                && c != '\'';
    }

    private static char randomLetter() {
        String letters = "blug";
        return letters.charAt(RANDOM.nextInt(letters.length()));
    }

    private static char randomReplacementChar() {
        String syllable = SYLLABLES[RANDOM.nextInt(SYLLABLES.length)];
        return syllable.charAt(RANDOM.nextInt(syllable.length()));
    }

    public static boolean shouldMuffleSpeech(ServerPlayer player) {
        return shouldMuffle(player);
    }

    public static String garble(String original) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < original.length(); i++) {
            char c = original.charAt(i);

            if (Character.isWhitespace(c)) {
                result.append(c);
            } else if (isPreservedPunctuation(c)) {
                result.append(c);
            } else if (c == '\'') {
                result.append(randomLetter());
            } else if (Character.isLetterOrDigit(c)) {
                result.append(randomReplacementChar());
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
}