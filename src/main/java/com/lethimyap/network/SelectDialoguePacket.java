package com.lethimyap.network;

import com.lethimyap.api.YapPriority;
import com.lethimyap.messages.DialogueColor;
import com.lethimyap.messages.PlayerMessageManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SelectDialoguePacket {

    private final String message;
    private final String color;
    private final boolean important;
    private final boolean pain;
    private final YapPriority priority;

    public SelectDialoguePacket(
            String message,
            String color,
            boolean important,
            boolean pain,
            YapPriority priority
    ) {
        this.message = message;
        this.color = color;
        this.important = important;
        this.pain = pain;
        this.priority = priority;
    }

    public static void encode(SelectDialoguePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.message);
        buf.writeUtf(msg.color);
        buf.writeBoolean(msg.important);
        buf.writeBoolean(msg.pain);
        buf.writeUtf(msg.priority.name());
    }

    public static SelectDialoguePacket decode(FriendlyByteBuf buf) {
        return new SelectDialoguePacket(
                buf.readUtf(),
                buf.readUtf(),
                buf.readBoolean(),
                buf.readBoolean(),
                YapPriority.fromString(buf.readUtf())
        );
    }

    public static void handle(SelectDialoguePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();

            if (player == null) return;
            if (msg.message == null || msg.message.isBlank()) return;

            PlayerMessageManager.handleClientDialogueSelection(
                    player,
                    msg.message,
                    DialogueColor.fromString(msg.color),
                    msg.important,
                    msg.pain,
                    msg.priority
            );
        });

        ctx.get().setPacketHandled(true);
    }
}