package com.lethimyap.network;

import com.lethimyap.messages.PlayerMessageManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncVoiceSelectionPacket {

    private final String voiceId;

    public SyncVoiceSelectionPacket(String voiceId) {
        this.voiceId = voiceId;
    }

    public static void encode(SyncVoiceSelectionPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.voiceId);
    }

    public static SyncVoiceSelectionPacket decode(FriendlyByteBuf buf) {
        return new SyncVoiceSelectionPacket(
                buf.readUtf()
        );
    }

    public static void handle(
            SyncVoiceSelectionPacket msg,
            Supplier<NetworkEvent.Context> ctx
    ) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();

            if (player == null) return;

            PlayerMessageManager.setPlayerVoice(
                    player,
                    msg.voiceId
            );
        });

        ctx.get().setPacketHandled(true);
    }
}