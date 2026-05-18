package com.lethimyap.messages;

import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class PoolConditionSourceRegistry {

    private static final Map<String, Function<ServerPlayer, Double>> NUMBER_SOURCES =
            new LinkedHashMap<>();

    public static void registerNumberSource(
            String id,
            Function<ServerPlayer, Double> getter
    ) {
        if (id == null || id.isBlank()) return;
        if (getter == null) return;

        NUMBER_SOURCES.put(id, getter);
    }

    public static double getNumber(
            ServerPlayer player,
            String id,
            double fallback
    ) {
        if (player == null) return fallback;
        if (id == null || id.isBlank()) return fallback;

        Function<ServerPlayer, Double> getter = NUMBER_SOURCES.get(id);

        if (getter == null) {
            return fallback;
        }

        try {
            Double value = getter.apply(player);

            if (value == null || Double.isNaN(value)) {
                return fallback;
            }

            return value;

        } catch (Exception e) {
            e.printStackTrace();
            return fallback;
        }
    }

    public static boolean hasNumberSource(String id) {
        return id != null && NUMBER_SOURCES.containsKey(id);
    }
}