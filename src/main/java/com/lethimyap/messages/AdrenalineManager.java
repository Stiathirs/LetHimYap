package com.lethimyap.messages;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class AdrenalineManager {

    public static final String ROOT_TAG = "lethimyap";
    public static final String ADRENALINE_TAG = "adrenaline";

    public static void tick(ServerPlayer player) {
        ServerMessageConfig config = ServerMessageConfig.get();

        double adrenaline = get(player);

        if (adrenaline <= 0) {
            set(player, 0);
            return;
        }

        adrenaline -= config.adrenalineDecayPerTick;

        set(player, Math.max(0, adrenaline));
    }

    public static void addFromDamage(ServerPlayer player, float damage) {
        ServerMessageConfig config = ServerMessageConfig.get();

        if (!config.adrenalineEnabled) return;
        if (damage <= 0) return;

        double adrenaline = get(player);

        adrenaline += damage * config.adrenalineGainPerDamage;

        set(player, Math.min(config.adrenalineMax, adrenaline));
    }

    public static double get(ServerPlayer player) {
        CompoundTag forgeData = player.getPersistentData();
        CompoundTag lethimyap = forgeData.getCompound(ROOT_TAG);

        return lethimyap.getDouble(ADRENALINE_TAG);
    }

    public static void set(ServerPlayer player, double value) {
        CompoundTag forgeData = player.getPersistentData();
        CompoundTag lethimyap = forgeData.getCompound(ROOT_TAG);

        lethimyap.putDouble(ADRENALINE_TAG, value);

        forgeData.put(ROOT_TAG, lethimyap);
    }

    public static double getSlowdownReduction(ServerPlayer player) {
        ServerMessageConfig config = ServerMessageConfig.get();

        if (!config.adrenalineEnabled) return 0.0;

        double adrenaline = get(player);
        double normalized = adrenaline / Math.max(1.0, config.adrenalineMax);

        normalized = Math.max(0.0, Math.min(1.0, normalized));

        return normalized * config.adrenalineMaxSlowdownReduction;
    }

    public static void addAdrenaline(ServerPlayer player, double amount) {
        if (player == null) return;

        ServerMessageConfig config = ServerMessageConfig.get();
        if (!config.adrenalineEnabled) return;

        double current = get(player);
        setAdrenaline(player, current + amount);
    }

    public static void removeAdrenaline(ServerPlayer player, double amount) {
        if (player == null) return;

        ServerMessageConfig config = ServerMessageConfig.get();
        if (!config.adrenalineEnabled) return;

        double current = get(player);
        setAdrenaline(player, current - amount);
    }

    public static void setAdrenaline(ServerPlayer player, double amount) {
        if (player == null) return;

        ServerMessageConfig config = ServerMessageConfig.get();

        double clamped =
                Math.max(0.0, Math.min(config.adrenalineMax, amount));

        player.getPersistentData().putDouble(ADRENALINE_TAG, clamped);
    }
}