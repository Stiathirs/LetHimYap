package com.lethimyap.messages;

import net.minecraft.server.level.ServerPlayer;

public class NbtPathReader {

    public static double getNumber(ServerPlayer player, String path, double fallback) {
        try {
            String[] parts = path.split("\\.");

            if (parts.length == 0) return fallback;

            net.minecraft.nbt.CompoundTag tag;

            if (parts[0].equals("ForgeData")) {
                tag = player.getPersistentData();
                return readFrom(tag, parts, 1, fallback);
            }

            if (parts[0].equals("Root")) {
                tag = new net.minecraft.nbt.CompoundTag();
                player.saveWithoutId(tag);
                return readFrom(tag, parts, 1, fallback);
            }

            return fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    private static double readFrom(
            net.minecraft.nbt.CompoundTag tag,
            String[] parts,
            int index,
            double fallback
    ) {
        net.minecraft.nbt.Tag current = tag;

        for (int i = index; i < parts.length; i++) {
            if (!(current instanceof net.minecraft.nbt.CompoundTag compound)) {
                return fallback;
            }

            if (!compound.contains(parts[i])) {
                return fallback;
            }

            current = compound.get(parts[i]);
        }

        if (current instanceof net.minecraft.nbt.NumericTag number) {
            return number.getAsDouble();
        }

        return fallback;
    }
}