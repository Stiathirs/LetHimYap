package com.lethimyap.messages;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Random;

public class PoolCooldownManager {
    private static final String NORMAL_POOL_COOLDOWNS = "lethimyap_pool_cooldowns";
    private static final String FORCED_POOL_COOLDOWNS = "lethimyap_forced_pool_cooldowns";
    private static final String GROUP_COOLDOWNS = "lethimyap_group_cooldowns";
    private static final String GROUP_LAST_TIERS = "lethimyap_group_last_tiers";

    private static final Random RANDOM = new Random();

    public static void tick(ServerPlayer player) {
        tickTag(player.getPersistentData().getCompound(NORMAL_POOL_COOLDOWNS));
        tickTag(player.getPersistentData().getCompound(FORCED_POOL_COOLDOWNS));
        tickTag(player.getPersistentData().getCompound(GROUP_COOLDOWNS));

        player.getPersistentData().put(
                NORMAL_POOL_COOLDOWNS,
                player.getPersistentData().getCompound(NORMAL_POOL_COOLDOWNS)
        );

        player.getPersistentData().put(
                FORCED_POOL_COOLDOWNS,
                player.getPersistentData().getCompound(FORCED_POOL_COOLDOWNS)
        );

        player.getPersistentData().put(
                GROUP_COOLDOWNS,
                player.getPersistentData().getCompound(GROUP_COOLDOWNS)
        );
    }

    public static boolean canUseNormal(
            ServerPlayer player,
            PoolOverrideConfig.PoolView view
    ) {
        if (player == null || view == null || view.pool == null) return false;

        String poolId = view.pool.id;
        String group = view.pool.group;

        if (isOnCooldown(player, NORMAL_POOL_COOLDOWNS, poolId)) {
            return false;
        }

        if (group == null || group.isBlank()) {
            return true;
        }

        if (!isOnCooldown(player, GROUP_COOLDOWNS, group)) {
            return true;
        }

        int lastTier = getLastTier(player, group);

        return view.tier > lastTier;
    }

    public static void applyNormal(
            ServerPlayer player,
            PoolOverrideConfig.PoolView view
    ) {
        if (player == null || view == null || view.pool == null) return;

        int cooldown = rollCooldown(
                view.cooldownMinTicks,
                view.cooldownMaxTicks
        );

        if (cooldown > 0) {
            setCooldown(player, NORMAL_POOL_COOLDOWNS, view.pool.id, cooldown);

            if (view.pool.group != null && !view.pool.group.isBlank()) {
                setCooldown(player, GROUP_COOLDOWNS, view.pool.group, cooldown);
                setLastTier(player, view.pool.group, view.tier);
            }
        }
    }

    public static boolean canUseForced(
            ServerPlayer player,
            PoolOverrideConfig.PoolView view
    ) {
        if (player == null || view == null || view.pool == null) return false;

        return !isOnCooldown(
                player,
                FORCED_POOL_COOLDOWNS,
                view.pool.id
        );
    }

    public static void applyForced(
            ServerPlayer player,
            PoolOverrideConfig.PoolView view
    ) {
        if (player == null || view == null || view.pool == null) return;

        int cooldown = rollCooldown(
                view.forcedCooldownMinTicks,
                view.forcedCooldownMaxTicks
        );

        if (cooldown > 0) {
            setCooldown(
                    player,
                    FORCED_POOL_COOLDOWNS,
                    view.pool.id,
                    cooldown
            );
        }
    }

    private static void tickTag(CompoundTag tag) {
        ArrayList<String> keys = new ArrayList<>(tag.getAllKeys());

        for (String key : keys) {
            int value = tag.getInt(key);

            if (value <= 1) {
                tag.remove(key);
            } else {
                tag.putInt(key, value - 1);
            }
        }
    }

    private static boolean isOnCooldown(
            ServerPlayer player,
            String tagName,
            String key
    ) {
        if (key == null || key.isBlank()) return false;

        CompoundTag tag = player.getPersistentData().getCompound(tagName);

        return tag.getInt(key) > 0;
    }

    private static void setCooldown(
            ServerPlayer player,
            String tagName,
            String key,
            int ticks
    ) {
        if (key == null || key.isBlank()) return;

        CompoundTag tag = player.getPersistentData().getCompound(tagName);
        tag.putInt(key, ticks);
        player.getPersistentData().put(tagName, tag);
    }

    private static int getLastTier(ServerPlayer player, String group) {
        CompoundTag tag = player.getPersistentData().getCompound(GROUP_LAST_TIERS);
        return tag.getInt(group);
    }

    private static void setLastTier(
            ServerPlayer player,
            String group,
            int tier
    ) {
        CompoundTag tag = player.getPersistentData().getCompound(GROUP_LAST_TIERS);
        tag.putInt(group, tier);
        player.getPersistentData().put(GROUP_LAST_TIERS, tag);
    }

    private static int rollCooldown(int min, int max) {
        min = Math.max(0, min);
        max = Math.max(min, max);

        if (max <= 0) {
            return 0;
        }

        if (min == max) {
            return min;
        }

        return min + RANDOM.nextInt((max - min) + 1);
    }

    public static int getNormalPoolCooldown(ServerPlayer player, String poolId) {
        return getCooldown(player, NORMAL_POOL_COOLDOWNS, poolId);
    }

    public static int getForcedPoolCooldown(ServerPlayer player, String poolId) {
        return getCooldown(player, FORCED_POOL_COOLDOWNS, poolId);
    }

    public static int getGroupCooldown(ServerPlayer player, String group) {
        return getCooldown(player, GROUP_COOLDOWNS, group);
    }

    public static int getGroupLastTier(ServerPlayer player, String group) {
        return getLastTier(player, group);
    }

    private static int getCooldown(
            ServerPlayer player,
            String tagName,
            String key
    ) {
        if (player == null) return 0;
        if (key == null || key.isBlank()) return 0;

        CompoundTag tag = player.getPersistentData().getCompound(tagName);

        return Math.max(0, tag.getInt(key));
    }

    public static void clear(ServerPlayer player) {
        if (player == null) return;

        player.getPersistentData().remove(NORMAL_POOL_COOLDOWNS);
        player.getPersistentData().remove(FORCED_POOL_COOLDOWNS);
        player.getPersistentData().remove(GROUP_COOLDOWNS);
        player.getPersistentData().remove(GROUP_LAST_TIERS);
    }
}