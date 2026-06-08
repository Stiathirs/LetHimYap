package com.lethimyap.api;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface YapPlaceholderResolver {
    Object resolve(ServerPlayer player);
}