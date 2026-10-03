package com.lethimyap.client.config;

import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

public final class ConfigSearch {

    private ConfigSearch() {
    }

    public record Query(String modFilter, String deepText, String text) {

        public boolean hasModFilter() {
            return modFilter != null && !modFilter.isBlank();
        }

        public boolean hasDeepText() {
            return deepText != null && !deepText.isBlank();
        }

        public boolean hasText() {
            return text != null && !text.isBlank();
        }
    }

    public static Query parse(String input) {
        if (input == null || input.isBlank()) return new Query(null, null, "");

        String remaining = input.trim();
        String modFilter = null;
        String deepText = null;

        while (!remaining.isEmpty()) {
            if (remaining.startsWith("@\"")) {
                int closingQuote = remaining.indexOf('"', 2);

                if (closingQuote < 0)
                    return new Query(remaining.substring(2), deepText, "");

                modFilter = remaining.substring(2, closingQuote).trim();
                remaining = remaining.substring(closingQuote + 1).trim();
                continue;
            }

            if (remaining.startsWith("@")) {
                int space = remaining.indexOf(' ');

                if (space < 0)
                    return new Query(remaining.substring(1), deepText, "");

                modFilter = remaining.substring(1, space).trim();
                remaining = remaining.substring(space + 1).trim();
                continue;
            }

            if (remaining.startsWith("#\"")) {
                int closingQuote = remaining.indexOf('"', 2);

                if (closingQuote < 0)
                    return new Query(modFilter, remaining.substring(2), "");

                deepText = remaining.substring(2, closingQuote).trim();
                remaining = remaining.substring(closingQuote + 1).trim();
                continue;
            }

            break;
        }

        return new Query(modFilter, deepText, remaining);
    }

    public static boolean matchesModId(String input, String modId) {
        Query query = parse(input);
        return query.hasModFilter() && normalize(modId).equals(normalize(query.modFilter()));
    }

    public static boolean matches(String input, String searchableText, String modId, String modName) {
        Query query = parse(input);

        if (query.hasModFilter()) {
            String wantedMod = normalize(query.modFilter());

            boolean matchesMod = normalize(modId).contains(wantedMod)
                    || normalize(modName).contains(wantedMod)
                    || normalizeWithoutSpaces(modName).contains(normalizeWithoutSpaces(query.modFilter()));

            if (!matchesMod) return false;
        }

        if (query.hasText()) return normalize(searchableText).contains(normalize(query.text()));

        return true;
    }

    public static Component formatVoice(String input) {
        if (input == null || input.isEmpty()) return Component.empty();

        MutableComponent result = Component.empty();

        if (!input.startsWith("@")) return result.append(Component.literal(input));

        // @"Let Him Yap" click
        if (input.startsWith("@\"")) {
            result.append(Component.literal("@").withStyle(ChatFormatting.AQUA));

            int closingQuote = input.indexOf('"', 2);

            if (closingQuote < 0) {
                result.append(Component.literal(input.substring(1)).withStyle(ChatFormatting.GOLD));
                return result;
            }

            result.append(Component.literal(input.substring(1, closingQuote + 1)).withStyle(ChatFormatting.GOLD));

            if (closingQuote + 1 < input.length())
                result.append(Component.literal(input.substring(closingQuote + 1)));

            return result;
        }

        // @lethimyap click
        int space = input.indexOf(' ');

        if (space < 0) return result.append(Component.literal(input).withStyle(ChatFormatting.AQUA));

        result.append(Component.literal(input.substring(0, space)).withStyle(ChatFormatting.AQUA));
        result.append(Component.literal(input.substring(space)));

        return result;
    }

    public static Component formatDialogue(String input) {
        if (input == null || input.isEmpty()) return Component.empty();

        MutableComponent result = Component.empty();
        int index = 0;

        while (index < input.length()) {
            if (input.startsWith("@\"", index)) {
                result.append(Component.literal("@").withStyle(ChatFormatting.AQUA));

                int closingQuote = input.indexOf('"', index + 2);

                if (closingQuote < 0) {
                    result.append(Component.literal(input.substring(index + 1)).withStyle(ChatFormatting.GOLD));
                    break;
                }

                result.append(Component.literal(input.substring(index + 1, closingQuote + 1)).withStyle(ChatFormatting.GOLD));
                index = closingQuote + 1;
                continue;
            }

            if (input.charAt(index) == '@') {
                int end = findTokenEnd(input, index);

                result.append(Component.literal(input.substring(index, end)).withStyle(ChatFormatting.AQUA));
                index = end;
                continue;
            }

            if (input.startsWith("#\"", index)) {
                result.append(Component.literal("#").withStyle(ChatFormatting.LIGHT_PURPLE));

                int closingQuote = input.indexOf('"', index + 2);

                if (closingQuote < 0) {
                    result.append(Component.literal(input.substring(index + 1)).withStyle(ChatFormatting.GOLD));
                    break;
                }

                result.append(Component.literal(input.substring(index + 1, closingQuote + 1)).withStyle(ChatFormatting.GOLD));
                index = closingQuote + 1;
                continue;
            }

            int nextOperator = findNextDialogueOperator(input, index);

            result.append(Component.literal(input.substring(index, nextOperator)));
            index = nextOperator;
        }

        return result;
    }

    private static int findTokenEnd(String input, int start) {
        int space = input.indexOf(' ', start);
        return space < 0 ? input.length() : space;
    }

    private static int findNextDialogueOperator(String input, int start) {
        int nextAt = input.indexOf('@', start);
        int nextDeep = input.indexOf("#\"", start);

        if (nextAt < 0) return nextDeep < 0 ? input.length() : nextDeep;
        if (nextDeep < 0) return nextAt;

        return Math.min(nextAt, nextDeep);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private static String normalizeWithoutSpaces(String value) {
        return normalize(value).replace(" ", "");
    }

    public static Component voiceHint() {
        return Component.empty()
                .append(Component.literal("Search... (").withStyle(ChatFormatting.GRAY))
                .append(Component.literal("@mod").withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" or ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal("@").withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\"Mod Name\"").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" supported)").withStyle(ChatFormatting.GRAY));
    }

    public static Component dialogueHint() {
    return Component.empty()
            .append(Component.literal("Search... (").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("@mod").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(", ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("@").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("\"Mod Name\"").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(", or ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("#").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("\"dialogue\"").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" supported)").withStyle(ChatFormatting.GRAY));
}

    public static FormattedCharSequence formatVoiceEditBox(String input, int offset) {
        return formatVoice(input).getVisualOrderText();
    }
    
    public static FormattedCharSequence formatDialogueEditBox(String input, int offset) {
        return formatDialogue(input).getVisualOrderText();
    }
}