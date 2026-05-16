package com.lethimyap.network;

import com.lethimyap.api.YapPriority;
import com.lethimyap.messages.DialogueColor;
import com.lethimyap.messages.PlayerMessageManager;
import com.lethimyap.messages.ServerMessageConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CustomSpeechPacket {
    private final String message;
    private final String color;

    public CustomSpeechPacket(String message, String color) {
        this.message = message;
        this.color = color;
    }

    public static void encode(CustomSpeechPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.message);
        buf.writeUtf(msg.color);
    }

    public static CustomSpeechPacket decode(FriendlyByteBuf buf) {
        return new CustomSpeechPacket(buf.readUtf(), buf.readUtf());
    }

    public static void handle(CustomSpeechPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (msg.message == null || msg.message.isBlank()) return;

            int maxLength = ServerMessageConfig.get().maxDialogueLength;

            String text = msg.message.length() > maxLength
                    ? msg.message.substring(0, maxLength)
                    : msg.message;

            PlayerMessageManager.handleClientDialogueSelection(
                    player,
                    text,
                    DialogueColor.fromString(msg.color),
                    true,
                    false,
                    YapPriority.EVENT
            );
        });

        ctx.get().setPacketHandled(true);
    }
}