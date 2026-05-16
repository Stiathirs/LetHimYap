package com.lethimyap.network;

import com.lethimyap.api.YapPriority;
import com.lethimyap.client.ClientDialogueSelector;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OfferDialoguePoolPacket {

    private final String poolId;
    private final boolean important;
    private final boolean pain;
    private final YapPriority priority;

    public OfferDialoguePoolPacket(String poolId, boolean important, boolean pain, YapPriority priority) {
        this.poolId = poolId;
        this.important = important;
        this.pain = pain;
        this.priority = priority;
    }

    public static void encode(OfferDialoguePoolPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.poolId);
        buf.writeBoolean(msg.important);
        buf.writeBoolean(msg.pain);
        buf.writeUtf(msg.priority.name());
    }

    public static OfferDialoguePoolPacket decode(FriendlyByteBuf buf) {
        return new OfferDialoguePoolPacket(
                buf.readUtf(),
                buf.readBoolean(),
                buf.readBoolean(),
                YapPriority.fromString(buf.readUtf())
        );
    }

    public static void handle(OfferDialoguePoolPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                ClientDialogueSelector.chooseAndSend(
                        msg.poolId,
                        msg.important,
                        msg.pain,
                        msg.priority
                )
        );

        ctx.get().setPacketHandled(true);
    }
}