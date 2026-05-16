package com.lethimyap.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestDialoguePacket {

    public RequestDialoguePacket() {
    }

    public static void encode(RequestDialoguePacket msg, FriendlyByteBuf buf) {
    }

    public static RequestDialoguePacket decode(FriendlyByteBuf buf) {
        return new RequestDialoguePacket();
    }

    public static void handle(RequestDialoguePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();

            if (player != null) {
                com.lethimyap.messages.PlayerMessageManager.handleClientDialogueRequest(player);
            }
        });

        ctx.get().setPacketHandled(true);
    }
}