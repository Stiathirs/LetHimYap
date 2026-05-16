package com.lethimyap.network;

import com.lethimyap.client.ClientSpeechHudData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncSpeechHudPacket {

    private final String message;
    private final String color;
    private final int ticks;

    public SyncSpeechHudPacket(String message, String color, int ticks) {
        this.message = message;
        this.color = color;
        this.ticks = ticks;
    }

    public static void encode(SyncSpeechHudPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.message);
        buf.writeUtf(msg.color);
        buf.writeInt(msg.ticks);
    }

    public static SyncSpeechHudPacket decode(FriendlyByteBuf buf) {
        return new SyncSpeechHudPacket(
                buf.readUtf(),
                buf.readUtf(),
                buf.readInt()
        );
    }

    public static void handle(SyncSpeechHudPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                ClientSpeechHudData.set(msg.message, msg.color, msg.ticks)
        );

        ctx.get().setPacketHandled(true);
    }
}