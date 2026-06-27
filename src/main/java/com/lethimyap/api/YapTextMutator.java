package com.lethimyap.api;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface YapTextMutator {
    String mutate(ServerPlayer player, String message);
}