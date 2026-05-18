package com.lethimyap.messages;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;

public class PoolCondition {

    public String type = "always";

    public String source = "";
    public String nbtPath = "";

    public String compare = "below_or_equal";
    public double value = 0.0;

    public static PoolCondition always() {
        PoolCondition condition = new PoolCondition();
        condition.type = "always";
        return condition;
    }

    public static PoolCondition healthPercent(
            String compare,
            double value
    ) {
        PoolCondition condition = new PoolCondition();
        condition.type = "health_percent";
        condition.compare = compare;
        condition.value = value;
        return condition;
    }

    public static PoolCondition health(
            String compare,
            double value
    ) {
        PoolCondition condition = new PoolCondition();
        condition.type = "health";
        condition.compare = compare;
        condition.value = value;
        return condition;
    }

    public static PoolCondition hunger(
            String compare,
            double value
    ) {
        PoolCondition condition = new PoolCondition();
        condition.type = "hunger";
        condition.compare = compare;
        condition.value = value;
        return condition;
    }

    public static PoolCondition air(
            String compare,
            double value
    ) {
        PoolCondition condition = new PoolCondition();
        condition.type = "air";
        condition.compare = compare;
        condition.value = value;
        return condition;
    }

    public static PoolCondition nbtNumber(
            String nbtPath,
            String compare,
            double value
    ) {
        PoolCondition condition = new PoolCondition();
        condition.type = "nbt_number";
        condition.nbtPath = nbtPath;
        condition.compare = compare;
        condition.value = value;
        return condition;
    }

    public static PoolCondition sourceNumber(
            String source,
            String compare,
            double value
    ) {
        PoolCondition condition = new PoolCondition();
        condition.type = "source_number";
        condition.source = source;
        condition.compare = compare;
        condition.value = value;
        return condition;
    }

    public boolean matches(ServerPlayer player) {
        if (player == null) return false;

        if ("always".equals(type)) {
            return true;
        }

        double found = getValue(player);

        if (Double.isNaN(found)) {
            return false;
        }

        PoolCompareMode mode =
                PoolCompareMode.fromString(compare);

        return mode.compare(found, value);
    }

    private double getValue(ServerPlayer player) {
        return switch (type) {
            case "health_percent" ->
                    (player.getHealth() / player.getMaxHealth()) * 100.0;

            case "health" ->
                    player.getHealth();

            case "hunger" -> {
                FoodData food = player.getFoodData();
                yield food.getFoodLevel();
            }

            case "air" ->
                    player.getAirSupply();

            case "nbt_number" ->
                    NbtPathReader.getNumber(player, nbtPath, Double.NaN);

            case "source_number" ->
                    PoolConditionSourceRegistry.getNumber(player, source, Double.NaN);

            default ->
                    Double.NaN;
        };
    }
}