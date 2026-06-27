package com.lethimyap.messages;

import com.lethimyap.api.YapTextMutationCondition;
import com.lethimyap.api.YapTextMutator;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TextMutationRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Map<String, Entry> MUTATIONS = new LinkedHashMap<>();

    public static void register(
            String id,
            int priority,
            YapTextMutationCondition condition,
            YapTextMutator mutator
    ) {
        if (id == null || id.isBlank()) {
            LOGGER.warn("[Let Him Yap API] Tried to register a text mutation without a valid ID. It was ignored.");
            return;
        }

        if (condition == null) {
            LOGGER.warn("[Let Him Yap API] Text mutation '{}' has no condition callback. It was ignored.", id);
            return;
        }

        if (mutator == null) {
            LOGGER.warn("[Let Him Yap API] Text mutation '{}' has no mutator callback. It was ignored.", id);
            return;
        }

        MUTATIONS.put(id, new Entry(id, priority, condition, mutator));
    }

    public static String apply(ServerPlayer player, String message) {
        if (player == null) return message;
        if (message == null || message.isBlank()) return message;
        if (MUTATIONS.isEmpty()) return message;

        String result = message;

        List<Entry> entries = new ArrayList<>(MUTATIONS.values());
        entries.sort(Comparator
                .comparingInt((Entry entry) -> entry.priority)
                .thenComparing(entry -> entry.id)
        );

        for (Entry entry : entries) {
            boolean shouldApply;

            try {
                shouldApply = entry.condition.shouldApply(player);
            } catch (Exception e) {
                LOGGER.warn(
                        "[Let Him Yap API] Text mutation '{}' threw an exception while checking its condition.",
                        entry.id,
                        e
                );
                continue;
            }

            if (!shouldApply) continue;

            String mutated;

            try {
                mutated = entry.mutator.mutate(player, result);
            } catch (Exception e) {
                LOGGER.warn(
                        "[Let Him Yap API] Text mutation '{}' threw an exception while mutating dialogue.",
                        entry.id,
                        e
                );
                continue;
            }

            if (mutated == null) {
                LOGGER.warn(
                        "[Let Him Yap API] Text mutation '{}' returned null. The previous message was kept.",
                        entry.id
                );
                continue;
            }

            result = mutated;
        }

        return result;
    }

    private record Entry(
            String id,
            int priority,
            YapTextMutationCondition condition,
            YapTextMutator mutator
    ) {
    }

    public static List<String> getRegisteredMutationDescriptions() {
        List<Entry> entries = new ArrayList<>(MUTATIONS.values());

        entries.sort(Comparator
                .comparingInt((Entry entry) -> entry.priority)
                .thenComparing(entry -> entry.id)
        );

        ArrayList<String> result = new ArrayList<>();

        for (Entry entry : entries) {
            result.add(entry.id + " | priority=" + entry.priority);
        }

        return result;
    }
}