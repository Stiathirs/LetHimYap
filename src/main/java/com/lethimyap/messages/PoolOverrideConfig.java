package com.lethimyap.messages;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.lethimyap.api.YapPoolRegistry;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PoolOverrideConfig {

    private static final Path FILE =
            Path.of("config", "lethimyap", "server_pool_overrides.toml");

    public static void generateOrUpdate() {
        try {
            Files.createDirectories(FILE.getParent());

            try (CommentedFileConfig toml = CommentedFileConfig.builder(FILE)
                    .sync()
                    .preserveInsertionOrder()
                    .build()) {

                toml.load();

                toml.setComment(
                        "pools",
                        "Optional overrides for any registered normal dialogue pool. Works with built-in pools and addon mod pools."
                );

                ArrayList<ServerMessageConfig.Pool> allPools = new ArrayList<>();
                allPools.addAll(ServerMessageConfig.get().pools);
                allPools.addAll(YapPoolRegistry.getServerPools());

                for (ServerMessageConfig.Pool pool : allPools) {
                    if (pool == null || pool.id == null || pool.id.isBlank()) continue;

                    String base = "pools." + pool.id;

                    putIfMissing(toml, base + ".enabled", true);
                    putIfMissing(toml, base + ".weight", pool.weight);
                    putIfMissing(toml, base + ".tier", pool.tier);
                    putIfMissing(toml, base + ".important", pool.important);
                    putIfMissing(toml, base + ".forcedOnly", pool.forcedOnly);
                    putIfMissing(toml, base + ".conditionMode", pool.conditionMode.name());
                    putIfMissing(toml, base + ".conditions", writeConditions(pool.conditions));
                }

                toml.setComment(
                        "damagePools",
                        "Optional overrides for damage reaction pools. Damage type IDs look like minecraft:fall, minecraft:lava, minecraft:on_fire, etc."
                );

                ArrayList<ServerMessageConfig.DamagePool> allDamagePools = new ArrayList<>();
                allDamagePools.addAll(ServerMessageConfig.get().damagePools);
                allDamagePools.addAll(YapPoolRegistry.getDamagePools());

                for (ServerMessageConfig.DamagePool pool : allDamagePools) {
                    if (pool == null || pool.id == null || pool.id.isBlank()) continue;

                    String base = "damagePools." + pool.id;

                    putIfMissing(toml, base + ".enabled", true);
                    putIfMissing(toml, base + ".minDamage", pool.minDamage);
                    putIfMissing(toml, base + ".chance", pool.chance);
                    putIfMissing(toml, base + ".important", pool.important);
                    putIfMissing(toml, base + ".excludedDamageTypes", pool.excludedDamageTypes);
                    putIfMissing(toml, base + ".exclusiveDamageTypes", pool.exclusiveDamageTypes);
                }

                toml.setComment(
                        "globalDamageBlacklist",
                        "Damage type IDs that should never trigger generic damage reaction dialogue."
                );

                putIfMissing(
                        toml,
                        "globalDamageBlacklist.damageTypes",
                        YapPoolRegistry.getGlobalDamageBlacklist()
                );

                toml.save();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PoolView apply(ServerMessageConfig.Pool pool) {
        if (pool == null || pool.id == null || pool.id.isBlank()) {
            return PoolView.disabled(pool);
        }

        try {
            if (!Files.exists(FILE)) {
                generateOrUpdate();
            }

            try (CommentedFileConfig toml = CommentedFileConfig.builder(FILE)
                    .sync()
                    .build()) {

                toml.load();

                String base = "pools." + pool.id;

                PoolConditionMode conditionMode =
                        PoolConditionMode.fromString(
                                getString(
                                        toml,
                                        base + ".conditionMode",
                                        pool.conditionMode.name()
                                )
                        );

                ArrayList<PoolCondition> conditions =
                        getConditions(
                                toml,
                                base + ".conditions",
                                pool.conditions
                        );

                return new PoolView(
                        pool,
                        getBool(toml, base + ".enabled", true),
                        getInt(toml, base + ".weight", pool.weight),
                        getInt(toml, base + ".tier", pool.tier),
                        getBool(toml, base + ".important", pool.important),
                        getBool(toml, base + ".forcedOnly", pool.forcedOnly),
                        conditionMode,
                        conditions
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            return PoolView.from(pool);
        }
    }

    public static DamagePoolView applyDamagePool(ServerMessageConfig.DamagePool pool) {
        if (pool == null || pool.id == null || pool.id.isBlank()) {
            return null;
        }

        try {
            if (!Files.exists(FILE)) {
                generateOrUpdate();
            }

            try (CommentedFileConfig toml = CommentedFileConfig.builder(FILE)
                    .sync()
                    .build()) {

                toml.load();

                String base = "damagePools." + pool.id;

                return new DamagePoolView(
                        pool,
                        getBool(toml, base + ".enabled", true),
                        (float) getDouble(toml, base + ".minDamage", pool.minDamage),
                        getDouble(toml, base + ".chance", pool.chance),
                        getBool(toml, base + ".important", pool.important),
                        getStringList(toml, base + ".excludedDamageTypes", pool.excludedDamageTypes),
                        getStringList(toml, base + ".exclusiveDamageTypes", pool.exclusiveDamageTypes)
                );
            }

        } catch (Exception e) {
            e.printStackTrace();

            return new DamagePoolView(
                    pool,
                    true,
                    pool.minDamage,
                    pool.chance,
                    pool.important,
                    pool.excludedDamageTypes,
                    pool.exclusiveDamageTypes
            );
        }
    }

    public static class PoolView {
        public final ServerMessageConfig.Pool pool;
        public final boolean enabled;
        public final int weight;
        public final int tier;
        public final boolean important;
        public final boolean forcedOnly;
        public final PoolConditionMode conditionMode;
        public final ArrayList<PoolCondition> conditions;

        public PoolView(
                ServerMessageConfig.Pool pool,
                boolean enabled,
                int weight,
                int tier,
                boolean important,
                boolean forcedOnly,
                PoolConditionMode conditionMode,
                ArrayList<PoolCondition> conditions
        ) {
            this.pool = pool;
            this.enabled = enabled;
            this.weight = weight;
            this.tier = tier;
            this.important = important;
            this.forcedOnly = forcedOnly;
            this.conditionMode = conditionMode == null
                    ? PoolConditionMode.AND
                    : conditionMode;
            this.conditions = conditions == null
                    ? new ArrayList<>()
                    : conditions;
        }

        public static PoolView from(ServerMessageConfig.Pool pool) {
            return new PoolView(
                    pool,
                    true,
                    pool.weight,
                    pool.tier,
                    pool.important,
                    pool.forcedOnly,
                    pool.conditionMode,
                    copyConditions(pool.conditions)
            );
        }

        public static PoolView disabled(ServerMessageConfig.Pool pool) {
            return new PoolView(
                    pool,
                    false,
                    0,
                    0,
                    false,
                    false,
                    PoolConditionMode.AND,
                    new ArrayList<>()
            );
        }

        public boolean matches(ServerPlayer player) {
            if (conditions == null || conditions.isEmpty()) {
                return true;
            }

            return switch (conditionMode) {
                case AND -> matchesAnd(player);
                case OR -> matchesOr(player);
                case XOR -> matchesXor(player);
            };
        }

        private boolean matchesAnd(ServerPlayer player) {
            for (PoolCondition condition : conditions) {
                if (condition == null) continue;

                if (!condition.matches(player)) {
                    return false;
                }
            }

            return true;
        }

        private boolean matchesOr(ServerPlayer player) {
            for (PoolCondition condition : conditions) {
                if (condition == null) continue;

                if (condition.matches(player)) {
                    return true;
                }
            }

            return false;
        }

        private boolean matchesXor(ServerPlayer player) {
            int matches = 0;

            for (PoolCondition condition : conditions) {
                if (condition == null) continue;

                if (condition.matches(player)) {
                    matches++;
                }
            }

            return matches == 1;
        }
    }

    public static class DamagePoolView {
        public final ServerMessageConfig.DamagePool pool;
        public final boolean enabled;
        public final float minDamage;
        public final double chance;
        public final boolean important;
        public final List<String> excludedDamageTypes;
        public final List<String> exclusiveDamageTypes;

        public DamagePoolView(
                ServerMessageConfig.DamagePool pool,
                boolean enabled,
                float minDamage,
                double chance,
                boolean important,
                List<String> excludedDamageTypes,
                List<String> exclusiveDamageTypes
        ) {
            this.pool = pool;
            this.enabled = enabled;
            this.minDamage = minDamage;
            this.chance = chance;
            this.important = important;
            this.excludedDamageTypes = excludedDamageTypes;
            this.exclusiveDamageTypes = exclusiveDamageTypes;
        }
    }

    private static void putIfMissing(CommentedFileConfig toml, String path, Object value) {
        if (!toml.contains(path)) {
            toml.set(path, value);
        }
    }

    private static List<Config> writeConditions(List<PoolCondition> conditions) {
        ArrayList<Config> result = new ArrayList<>();

        if (conditions == null || conditions.isEmpty()) {
            conditions = List.of(PoolCondition.always());
        }

        for (PoolCondition condition : conditions) {
            if (condition == null) continue;

            Config config = Config.inMemory();

            config.set("type", condition.type);

            if (condition.source != null && !condition.source.isBlank()) {
                config.set("source", condition.source);
            }

            if (condition.nbtPath != null && !condition.nbtPath.isBlank()) {
                config.set("nbtPath", condition.nbtPath);
            }

            config.set("compare", condition.compare);
            config.set("value", condition.value);

            result.add(config);
        }

        if (result.isEmpty()) {
            Config config = Config.inMemory();
            config.set("type", "always");
            config.set("compare", "below_or_equal");
            config.set("value", 0.0);
            result.add(config);
        }

        return result;
    }

    private static ArrayList<PoolCondition> getConditions(
            CommentedFileConfig toml,
            String path,
            List<PoolCondition> fallback
    ) {
        Object value = toml.get(path);

        if (!(value instanceof List<?> list)) {
            return copyConditions(fallback);
        }

        ArrayList<PoolCondition> result = new ArrayList<>();

        for (Object entry : list) {
            PoolCondition condition = readCondition(entry);

            if (condition != null) {
                result.add(condition);
            }
        }

        if (result.isEmpty()) {
            return copyConditions(fallback);
        }

        return result;
    }

    private static PoolCondition readCondition(Object entry) {
        if (entry instanceof UnmodifiableConfig config) {
            PoolCondition condition = new PoolCondition();

            condition.type = getConfigString(config, "type", condition.type);
            condition.source = getConfigString(config, "source", condition.source);
            condition.nbtPath = getConfigString(config, "nbtPath", condition.nbtPath);
            condition.compare = getConfigString(config, "compare", condition.compare);
            condition.value = getConfigDouble(config, "value", condition.value);

            return condition;
        }

        if (entry instanceof Map<?, ?> map) {
            PoolCondition condition = new PoolCondition();

            Object type = map.get("type");
            if (type instanceof String s) {
                condition.type = s;
            }

            Object source = map.get("source");
            if (source instanceof String s) {
                condition.source = s;
            }

            Object nbtPath = map.get("nbtPath");
            if (nbtPath instanceof String s) {
                condition.nbtPath = s;
            }

            Object compare = map.get("compare");
            if (compare instanceof String s) {
                condition.compare = s;
            }

            Object value = map.get("value");
            if (value instanceof Number n) {
                condition.value = n.doubleValue();
            }

            return condition;
        }

        return null;
    }

    private static ArrayList<PoolCondition> copyConditions(List<PoolCondition> conditions) {
        ArrayList<PoolCondition> result = new ArrayList<>();

        if (conditions != null) {
            for (PoolCondition condition : conditions) {
                if (condition == null) continue;

                PoolCondition copy = new PoolCondition();
                copy.type = condition.type;
                copy.source = condition.source;
                copy.nbtPath = condition.nbtPath;
                copy.compare = condition.compare;
                copy.value = condition.value;

                result.add(copy);
            }
        }

        if (result.isEmpty()) {
            result.add(PoolCondition.always());
        }

        return result;
    }

    private static String getConfigString(
            UnmodifiableConfig config,
            String path,
            String fallback
    ) {
        Object value = config.get(path);
        return value instanceof String s ? s : fallback;
    }

    private static double getConfigDouble(
            UnmodifiableConfig config,
            String path,
            double fallback
    ) {
        Object value = config.get(path);
        return value instanceof Number n ? n.doubleValue() : fallback;
    }

    private static boolean getBool(CommentedFileConfig toml, String path, boolean fallback) {
        Object value = toml.get(path);
        return value instanceof Boolean b ? b : fallback;
    }

    private static int getInt(CommentedFileConfig toml, String path, int fallback) {
        Object value = toml.get(path);
        return value instanceof Number n ? n.intValue() : fallback;
    }

    private static double getDouble(CommentedFileConfig toml, String path, double fallback) {
        Object value = toml.get(path);
        return value instanceof Number n ? n.doubleValue() : fallback;
    }

    private static String getString(CommentedFileConfig toml, String path, String fallback) {
        Object value = toml.get(path);
        return value instanceof String s ? s : fallback;
    }

    private static List<String> getStringList(
            CommentedFileConfig toml,
            String path,
            List<String> fallback
    ) {
        Object value = toml.get(path);

        if (!(value instanceof List<?> list)) {
            return fallback;
        }

        ArrayList<String> result = new ArrayList<>();

        for (Object entry : list) {
            if (entry instanceof String s) {
                result.add(s);
            }
        }

        return result;
    }

    public static boolean isDamageTypeGloballyBlacklisted(String damageType) {
        if (damageType == null || damageType.isBlank()) {
            return false;
        }

        if (YapPoolRegistry.getHardGlobalDamageBlacklist().contains(damageType)) {
            return true;
        }

        try {
            if (!Files.exists(FILE)) {
                generateOrUpdate();
            }

            try (CommentedFileConfig toml = CommentedFileConfig.builder(FILE)
                    .sync()
                    .build()) {

                toml.load();

                List<String> damageTypes = getStringList(
                        toml,
                        "globalDamageBlacklist.damageTypes",
                        YapPoolRegistry.getGlobalDamageBlacklist()
                );

                return damageTypes.contains(damageType);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return YapPoolRegistry.getGlobalDamageBlacklist().contains(damageType);
        }
    }

    public static void reload() {
        generateOrUpdate();
    }
}
