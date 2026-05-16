package com.lethimyap.messages;

import com.lethimyap.network.ModNetwork;
import com.lethimyap.network.SyncFloatingTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class FloatingMessageManager {

    public static void set(ServerPlayer player, String message, DialogueColor color) {
        ServerMessageConfig config = ServerMessageConfig.get();

        ModNetwork.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new SyncFloatingTextPacket(
                        player.getUUID(),
                        message,
                        color.id,
                        config.floatingMessageTicks,
                        false
                )
        );
    }

    public static void tick(ServerPlayer player) {
        // Client handles duration and rendering.
    }

    public static void clear(ServerPlayer player) {
        ModNetwork.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new SyncFloatingTextPacket(
                        player.getUUID(),
                        "",
                        DialogueColor.WHITE.id,
                        0,
                        true
                )
        );
    }
}