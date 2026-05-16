package com.lethimyap.network;

import com.lethimyap.client.ClientLastWordsData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncLastWordsPacket {

    private final String lastWords;

    public SyncLastWordsPacket(String lastWords) {
        this.lastWords = lastWords;
    }

    public static void encode(SyncLastWordsPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.lastWords);
    }

    public static SyncLastWordsPacket decode(FriendlyByteBuf buf) {
        return new SyncLastWordsPacket(buf.readUtf());
    }

    public static void handle(SyncLastWordsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientLastWordsData.lastWords = msg.lastWords;
        });

        ctx.get().setPacketHandled(true);
    }
}