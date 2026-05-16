package com.lethimyap.api;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.lethimyap.messages.ClientDialogueConfig;
import com.lethimyap.messages.ServerMessageConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class YapPoolRegistry {

    private static final Map<String, ServerMessageConfig.Pool> SERVER_POOLS =
            new LinkedHashMap<>();

    private static final Map<String, ServerMessageConfig.DamagePool> DAMAGE_POOLS =
            new LinkedHashMap<>();

    private static final Map<String, ArrayList<String>> CLIENT_DEFAULT_LINES =
            new LinkedHashMap<>();

    private static final ArrayList<String> GLOBAL_DAMAGE_BLACKLIST =
            new ArrayList<>();

    private static final ArrayList<String> HARD_GLOBAL_DAMAGE_BLACKLIST =
            new ArrayList<>();

    public static void registerServerPool(ServerMessageConfig.Pool pool) {
        if (pool == null || pool.id == null || pool.id.isBlank()) return;

        SERVER_POOLS.put(pool.id, pool);
    }

    public static Collection<ServerMessageConfig.Pool> getServerPools() {
        return SERVER_POOLS.values();
    }

    public static void registerOrMergeDamagePool(
            String id,
            float minDamage,
            double chance,
            boolean important
    ) {
        if (id == null || id.isBlank()) return;

        ServerMessageConfig.DamagePool existing = DAMAGE_POOLS.get(id);

        if (existing == null) {
            DAMAGE_POOLS.put(
                    id,
                    new ServerMessageConfig.DamagePool(
                            id,
                            minDamage,
                            chance,
                            important
                    )
            );
            return;
        }

        existing.minDamage = minDamage;
        existing.chance = chance;
        existing.important = important;
    }

    public static void addDamagePoolExclusions(String id, String... damageTypes) {
        ServerMessageConfig.DamagePool pool = DAMAGE_POOLS.get(id);
        if (pool == null || damageTypes == null) return;

        for (String damageType : damageTypes) {
            if (damageType == null || damageType.isBlank()) continue;

            if (!pool.excludedDamageTypes.contains(damageType)) {
                pool.excludedDamageTypes.add(damageType);
            }
        }
    }

    public static void addDamagePoolExclusives(String id, String... damageTypes) {
        ServerMessageConfig.DamagePool pool = DAMAGE_POOLS.get(id);
        if (pool == null || damageTypes == null) return;

        for (String damageType : damageTypes) {
            if (damageType == null || damageType.isBlank()) continue;

            if (!pool.exclusiveDamageTypes.contains(damageType)) {
                pool.exclusiveDamageTypes.add(damageType);
            }
        }
    }

    public static Collection<ServerMessageConfig.DamagePool> getDamagePools() {
        return DAMAGE_POOLS.values();
    }

    public static void addGlobalDamageBlacklist(String... damageTypes) {
        if (damageTypes == null) return;

        for (String damageType : damageTypes) {
            if (damageType == null || damageType.isBlank()) continue;

            if (!GLOBAL_DAMAGE_BLACKLIST.contains(damageType)) {
                GLOBAL_DAMAGE_BLACKLIST.add(damageType);
            }
        }
    }

    public static void addHardGlobalDamageBlacklist(String... damageTypes) {
        if (damageTypes == null) return;

        for (String damageType : damageTypes) {
            if (damageType == null || damageType.isBlank()) continue;

            if (!HARD_GLOBAL_DAMAGE_BLACKLIST.contains(damageType)) {
                HARD_GLOBAL_DAMAGE_BLACKLIST.add(damageType);
            }
        }
    }

    public static ArrayList<String> getHardGlobalDamageBlacklist() {
        return HARD_GLOBAL_DAMAGE_BLACKLIST;
    }

    public static ArrayList<String> getGlobalDamageBlacklist() {
        return GLOBAL_DAMAGE_BLACKLIST;
    }

    public static void registerClientDefaultLines(String poolId, String... lines) {
        if (poolId == null || poolId.isBlank()) return;
        if (lines == null || lines.length == 0) return;

        ArrayList<String> list =
                CLIENT_DEFAULT_LINES.computeIfAbsent(poolId, ignored -> new ArrayList<>());

        for (String line : lines) {
            if (line == null || line.isBlank()) continue;

            if (!list.contains(line)) {
                list.add(line);
            }
        }

        ClientDialogueConfig.writeMissingExternalDefaults();
    }

    public static void writeMissingClientDefaults(CommentedFileConfig toml) {
        if (toml == null) return;

        for (Map.Entry<String, ArrayList<String>> entry : CLIENT_DEFAULT_LINES.entrySet()) {
            String path = "pools." + entry.getKey() + ".messages";

            if (!toml.contains(path)) {
                toml.set(path, List.copyOf(entry.getValue()));
            }
        }

        toml.save();
    }

    public static void registerVanillaGlobalDamageBlacklistDefaults() {
        addGlobalDamageBlacklist(
                "minecraft:campfire",
                "minecraft:explosion",
                "minecraft:freeze",
                "minecraft:hot_floor",
                "minecraft:in_fire",
                "minecraft:in_wall",
                "minecraft:lava",
                "minecraft:lightning_bolt",
                "minecraft:indirect_magic",
                "minecraft:magic",
                "minecraft:on_fire",
                "minecraft:outside_of_world",
                "minecraft:outside_border",
                "minecraft:player_explosion",
                "minecraft:starve",
                "minecraft:wither"
        );
    }
}