package com.lethimyap.api;

import com.lethimyap.messages.PoolCondition;
import com.lethimyap.messages.PoolConditionMode;
import com.lethimyap.messages.ServerMessageConfig;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class YapPoolBuilder {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final String id;

    private String group;
    private int tier = 0;
    private int weight = 10;
    private boolean important = true;
    private boolean forcedOnly = false;
    private PoolConditionMode conditionMode = PoolConditionMode.AND;

    private int cooldownMinTicks = 3600;
    private int cooldownMaxTicks = 3600;
    private int forcedCooldownMinTicks = 0;
    private int forcedCooldownMaxTicks = 0;

    private boolean groupSet = false;
    private boolean tierSet = false;
    private boolean weightSet = false;
    private boolean conditionSet = false;
    private boolean defaultLinesSet = false;
    private boolean cooldownsSet = false;
    private boolean forcedCooldownsSet = false;

    private final List<PoolCondition> conditions = new ArrayList<>();
    private final List<String> defaultLines = new ArrayList<>();

    public YapPoolBuilder(String id) {
        this.id = id;
        this.group = id;
    }

    public YapPoolBuilder group(String group) {
        this.group = group;
        this.groupSet = true;
        return this;
    }

    public YapPoolBuilder tier(int tier) {
        this.tierSet = true;

        if (tier < 0) {
            warn(
                    "Pool '{}' was given a negative tier ({}). It has been set to 0.",
                    id,
                    tier
            );

            this.tier = 0;
        } else {
            this.tier = tier;
        }

        return this;
    }

    public YapPoolBuilder weight(int weight) {
        this.weightSet = true;

        if (weight < 0) {
            warn(
                    "Pool '{}' was given a negative weight ({}). It has been set to 0. " +
                            "A weight of 0 means this pool cannot be selected through normal random dialogue. " +
                            "The pool may still be triggered through forced dialogue, direct API calls, or forced-pool systems.",
                    id,
                    weight
            );

            this.weight = 0;
        } else {
            this.weight = weight;
        }

        return this;
    }

    public YapPoolBuilder important(boolean important) {
        this.important = important;
        return this;
    }

    public YapPoolBuilder forcedOnly(boolean forcedOnly) {
        this.forcedOnly = forcedOnly;
        return this;
    }

    public YapPoolBuilder conditionMode(PoolConditionMode conditionMode) {
        this.conditionMode = conditionMode == null
                ? PoolConditionMode.AND
                : conditionMode;

        return this;
    }

    public YapPoolBuilder condition(PoolCondition condition) {
        this.conditionSet = true;

        if (condition != null) {
            this.conditions.add(condition);
        }

        return this;
    }

    public YapPoolBuilder conditions(PoolCondition... conditions) {
        this.conditionSet = true;

        if (conditions == null) return this;

        for (PoolCondition condition : conditions) {
            if (condition != null) {
                this.conditions.add(condition);
            }
        }

        return this;
    }

    public YapPoolBuilder cooldowns(int minTicks, int maxTicks) {
        this.cooldownsSet = true;

        int originalMin = minTicks;
        int originalMax = maxTicks;

        if (minTicks < 0) {
            warn(
                    "Pool '{}' was given a negative normal cooldown minimum ({}). It has been set to 0.",
                    id,
                    originalMin
            );

            minTicks = 0;
        }

        if (maxTicks < 0) {
            warn(
                    "Pool '{}' was given a negative normal cooldown maximum ({}). It has been set to 0.",
                    id,
                    originalMax
            );

            maxTicks = 0;
        }

        if (maxTicks < minTicks) {
            warn(
                    "Pool '{}' was given a normal cooldown maximum ({}) lower than its minimum ({}). " +
                            "The maximum has been set to the minimum. You may have meant to swap these values.",
                    id,
                    originalMax,
                    minTicks
            );

            maxTicks = minTicks;
        }

        this.cooldownMinTicks = minTicks;
        this.cooldownMaxTicks = maxTicks;

        return this;
    }

    public YapPoolBuilder forcedCooldowns(int minTicks, int maxTicks) {
        this.forcedCooldownsSet = true;

        int originalMin = minTicks;
        int originalMax = maxTicks;

        if (minTicks < 0) {
            warn(
                    "Pool '{}' was given a negative forced cooldown minimum ({}). It has been set to 0.",
                    id,
                    originalMin
            );

            minTicks = 0;
        }

        if (maxTicks < 0) {
            warn(
                    "Pool '{}' was given a negative forced cooldown maximum ({}). It has been set to 0.",
                    id,
                    originalMax
            );

            maxTicks = 0;
        }

        if (maxTicks < minTicks) {
            warn(
                    "Pool '{}' was given a forced cooldown maximum ({}) lower than its minimum ({}). " +
                            "The maximum has been set to the minimum. You may have meant to swap these values.",
                    id,
                    originalMax,
                    minTicks
            );

            maxTicks = minTicks;
        }

        this.forcedCooldownMinTicks = minTicks;
        this.forcedCooldownMaxTicks = maxTicks;

        return this;
    }

    public YapPoolBuilder defaultLines(String... lines) {
        this.defaultLinesSet = true;

        if (lines == null) return this;

        for (String line : lines) {
            if (line == null || line.isBlank()) continue;

            this.defaultLines.add(line);
        }

        return this;
    }

    public ServerMessageConfig.Pool build() {
        if (!validateAndWarn()) {
            return null;
        }

        ServerMessageConfig.Pool pool =
                ServerMessageConfig.Pool.custom(
                        id,
                        group,
                        tier,
                        weight,
                        important,
                        forcedOnly,
                        conditionMode,
                        conditions.toArray(new PoolCondition[0])
                );

        pool.cooldownMinTicks = cooldownMinTicks;
        pool.cooldownMaxTicks = cooldownMaxTicks;
        pool.forcedCooldownMinTicks = forcedCooldownMinTicks;
        pool.forcedCooldownMaxTicks = forcedCooldownMaxTicks;

        return pool;
    }

    public ServerMessageConfig.Pool register() {
        ServerMessageConfig.Pool pool = build();

        if (pool == null) {
            return null;
        }

        YapPoolRegistry.registerServerPool(pool);

        if (!defaultLines.isEmpty()) {
            YapPoolRegistry.registerClientDefaultLines(
                    id,
                    defaultLines.toArray(new String[0])
            );
        }

        return pool;
    }

    private boolean validateAndWarn() {
        if (id == null || id.trim().isEmpty()) {
            warn(
                    "A pool builder was registered without a valid pool ID. " +
                            "The pool will be ignored. Use LetHimYapApi.pool(\"mymod.pool_id\") with a valid ID."
            );

            return false;
        }

        if (!groupSet) {
            warn(
                    "Pool '{}' was registered without an explicit group. " +
                            "It will use its pool ID as its group. This may prevent tier-based selection " +
                            "and group cooldown behavior from working with related pools. " +
                            "Call .group(\"mymod.group\") if this pool should share tiers/cooldowns with other pools.",
                    id
            );
        } else if (group == null || group.isBlank()) {
            warn(
                    "Pool '{}' was given a blank group. " +
                            "It will use its pool ID as its group. This may prevent tier-based selection " +
                            "and group cooldown behavior from working with related pools.",
                    id
            );

            group = id;
        }

        if (!forcedOnly) {
            if (!tierSet) {
                warn(
                        "Pool '{}' was registered without an explicit tier. " +
                                "It will use tier 0. If this pool is part of a tiered group, it may not override " +
                                "weaker pools as expected. Call .tier(...) to define its severity.",
                        id
                );
            }

            if (!weightSet) {
                warn(
                        "Pool '{}' was registered without an explicit weight. " +
                                "It will use the default weight of {}. This may make the pool appear more or less often " +
                                "than intended. Call .weight(...) to tune selection chance.",
                        id,
                        weight
                );
            }

            boolean hasNoConditions =
                    !conditionSet || conditions.isEmpty();

            boolean hasZeroWeight =
                    weightSet && weight == 0;

            if (hasNoConditions && hasZeroWeight) {
                warn(
                        "Pool '{}' has no conditions and a weight of 0. " +
                                "It will always pass condition checks, but it cannot be selected through normal random dialogue. " +
                                "If this pool is meant to be triggered manually, consider setting .forcedOnly(true). " +
                                "If it is meant to be ambient dialogue, add .condition(LetHimYapApi.alwaysCondition()) " +
                                "and set .weight(...) above 0.",
                        id
                );
            } else if (hasNoConditions) {
                warn(
                        "Pool '{}' was registered without any conditions. " +
                                "It will always pass condition checks. If this is intentional, " +
                                "add .condition(LetHimYapApi.alwaysCondition()) to make the behavior explicit.",
                        id
                );
            } else if (hasZeroWeight) {
                warn(
                        "Pool '{}' has a weight of 0. " +
                                "It cannot be selected through normal random dialogue. " +
                                "The pool may still be triggered through forced dialogue, direct API calls, or forced-pool systems.",
                        id
                );
            }
        } else {
            if (weightSet && weight != 0) {
                warn(
                        "Pool '{}' is forced-only. Its normal selection weight ({}) will not matter because forced-only pools are not randomly rolled.",
                        id,
                        weight
                );
            }

            if (cooldownsSet && (cooldownMinTicks > 0 || cooldownMaxTicks > 0)) {
                warn(
                        "Pool '{}' is forced-only. Its normal cooldown range ({}-{} ticks) will not matter because forced dialogue uses forced cooldowns instead.",
                        id,
                        cooldownMinTicks,
                        cooldownMaxTicks
                );
            }

            if (!conditionSet || conditions.isEmpty()) {
                warn(
                        "Pool '{}' was registered without any conditions. " +
                                "It will always pass condition checks. If this is intentional, " +
                                "add .condition(LetHimYapApi.alwaysCondition()) to make the behavior explicit.",
                        id
                );
            }
        }

        if (!defaultLinesSet || defaultLines.isEmpty()) {
            warn(
                    "Pool '{}' was registered without default client lines. " +
                            "The pool can still be selected, but players will not see dialogue unless they add lines manually " +
                            "or another addon registers default lines for the same pool ID.",
                    id
            );
        }

        return true;
    }

    private static void warn(String message, Object... args) {
        LOGGER.warn("[Let Him Yap API] " + message, args);
    }
}