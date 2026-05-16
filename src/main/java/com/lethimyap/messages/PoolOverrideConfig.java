package com.lethimyap.messages;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.lethimyap.api.YapPoolRegistry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
                        "Damage type IDs that should never trigger damage reaction dialogue."
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

                return new PoolView(
                        pool,
                        getBool(toml, base + ".enabled", true),
                        getInt(toml, base + ".weight", pool.weight),
                        getInt(toml, base + ".tier", pool.tier),
                        getBool(toml, base + ".important", pool.important),
                        getBool(toml, base + ".forcedOnly", pool.forcedOnly)
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

        public PoolView(
                ServerMessageConfig.Pool pool,
                boolean enabled,
                int weight,
                int tier,
                boolean important,
                boolean forcedOnly
        ) {
            this.pool = pool;
            this.enabled = enabled;
            this.weight = weight;
            this.tier = tier;
            this.important = important;
            this.forcedOnly = forcedOnly;
        }

        public static PoolView from(ServerMessageConfig.Pool pool) {
            return new PoolView(
                    pool,
                    true,
                    pool.weight,
                    pool.tier,
                    pool.important,
                    pool.forcedOnly
            );
        }

        public static PoolView disabled(ServerMessageConfig.Pool pool) {
            return new PoolView(pool, false, 0, 0, false, false);
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
}