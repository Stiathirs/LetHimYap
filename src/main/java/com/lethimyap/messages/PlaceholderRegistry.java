package com.lethimyap.messages;

import com.lethimyap.api.YapPlaceholderResolver;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderRegistry {
    private static final Pattern PLACEHOLDER_PATTERN =
            Pattern.compile("\\{([a-z0-9_.-]+:[a-z0-9_./-]+)}");

    private static final Map<String, YapPlaceholderResolver> RESOLVERS =
            new LinkedHashMap<>();

    public static void register(String id, YapPlaceholderResolver resolver) {
        if (id == null || id.isBlank()) return;
        if (resolver == null) return;

        RESOLVERS.put(id, resolver);
    }

    public static String resolve(ServerPlayer player, String message) {
        if (player == null) return message;
        if (message == null || message.isBlank()) return message;

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(message);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String id = matcher.group(1);
            YapPlaceholderResolver resolver = RESOLVERS.get(id);

            if (resolver == null) {
                matcher.appendReplacement(
                        result,
                        Matcher.quoteReplacement(matcher.group(0))
                );
                continue;
            }

            Object value;

            try {
                value = resolver.resolve(player);
            } catch (Exception e) {
                e.printStackTrace();
                matcher.appendReplacement(
                        result,
                        Matcher.quoteReplacement(matcher.group(0))
                );
                continue;
            }

            if (value == null) {
                matcher.appendReplacement(
                        result,
                        Matcher.quoteReplacement(matcher.group(0))
                );
                continue;
            }

            matcher.appendReplacement(
                    result,
                    Matcher.quoteReplacement(String.valueOf(value))
            );
        }

        matcher.appendTail(result);

        return result.toString();
    }
}