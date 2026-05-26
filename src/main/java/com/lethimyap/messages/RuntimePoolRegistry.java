package com.lethimyap.messages;

import com.lethimyap.api.YapPoolRegistry;
import com.lethimyap.network.ModNetwork;
import com.lethimyap.network.SyncRuntimeClientLinesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class RuntimePoolRegistry {
    private static final Map<String, ServerMessageConfig.Pool> POOLS = new LinkedHashMap<>();
    private static final Map<String, List<String>> CLIENT_LINES = new LinkedHashMap<>();

    public static void clear() {
        POOLS.clear();
        CLIENT_LINES.clear();
    }

    public static void registerPool(ServerMessageConfig.Pool pool) {
        if (pool == null || pool.id == null || pool.id.isBlank()) return;

        POOLS.put(pool.id, pool);
        YapPoolRegistry.registerServerPool(pool);
    }

    public static void addClientLine(String poolId, String line) {
        if (poolId == null || poolId.isBlank()) return;
        if (line == null || line.isBlank()) return;

        CLIENT_LINES.computeIfAbsent(poolId, id -> new ArrayList<>()).add(line);

        syncToAll();
    }

    public static Map<String, List<String>> getClientLines() {
        return CLIENT_LINES;
    }

    public static void syncTo(ServerPlayer player) {
        if (player == null) return;
        if (CLIENT_LINES.isEmpty()) return;

        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncRuntimeClientLinesPacket(CLIENT_LINES)
        );
    }

    public static void syncToAll() {
        var server = ServerLifecycleHooks.getCurrentServer();

        if (server == null) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncTo(player);
        }
    }
}