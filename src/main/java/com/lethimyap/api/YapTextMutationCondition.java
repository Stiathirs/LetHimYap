package com.lethimyap.api;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface YapTextMutationCondition {
    boolean shouldApply(ServerPlayer player);
}