package com.lethimyap.network;

import com.lethimyap.client.ClientFloatingTextData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncFloatingTextPacket {

    private final UUID playerId;
    private final String message;
    private final String color;
    private final int ticks;
    private final boolean clear;

    public SyncFloatingTextPacket(UUID playerId, String message, String color, int ticks, boolean clear) {
        this.playerId = playerId;
        this.message = message;
        this.color = color;
        this.ticks = ticks;
        this.clear = clear;
    }

    public static void encode(SyncFloatingTextPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerId);
        buf.writeUtf(msg.message);
        buf.writeUtf(msg.color);
        buf.writeInt(msg.ticks);
        buf.writeBoolean(msg.clear);
    }

    public static SyncFloatingTextPacket decode(FriendlyByteBuf buf) {
        return new SyncFloatingTextPacket(
                buf.readUUID(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readInt(),
                buf.readBoolean()
        );
    }

    public static void handle(SyncFloatingTextPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (msg.clear) {
                ClientFloatingTextData.clear(msg.playerId);
            } else {
                ClientFloatingTextData.set(msg.playerId, msg.message, msg.color, msg.ticks);
            }
        });

        ctx.get().setPacketHandled(true);
    }
}