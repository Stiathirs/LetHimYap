package com.lethimyap.network;

import com.lethimyap.client.ClientDialogueTimingData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncDialogueTimingPacket {

    private final boolean enabled;
    private final int minTicks;
    private final int maxTicks;

    public SyncDialogueTimingPacket(boolean enabled, int minTicks, int maxTicks) {
        this.enabled = enabled;
        this.minTicks = minTicks;
        this.maxTicks = maxTicks;
    }

    public static void encode(SyncDialogueTimingPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.enabled);
        buf.writeInt(msg.minTicks);
        buf.writeInt(msg.maxTicks);
    }

    public static SyncDialogueTimingPacket decode(FriendlyByteBuf buf) {
        return new SyncDialogueTimingPacket(
                buf.readBoolean(),
                buf.readInt(),
                buf.readInt()
        );
    }

    public static void handle(SyncDialogueTimingPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientDialogueTimingData.update(
                    msg.enabled,
                    msg.minTicks,
                    msg.maxTicks
            );
        });

        ctx.get().setPacketHandled(true);
    }
}