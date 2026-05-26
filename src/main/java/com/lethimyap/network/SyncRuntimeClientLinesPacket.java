package com.lethimyap.network;

import com.lethimyap.client.ClientServerDialogueConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SyncRuntimeClientLinesPacket {
    private final Map<String, List<String>> lines;

    public SyncRuntimeClientLinesPacket(Map<String, List<String>> lines) {
        this.lines = lines;
    }

    public static void encode(SyncRuntimeClientLinesPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.lines.size());

        msg.lines.forEach((poolId, poolLines) -> {
            buf.writeUtf(poolId);
            buf.writeInt(poolLines.size());

            for (String line : poolLines) {
                buf.writeUtf(line);
            }
        });
    }

    public static SyncRuntimeClientLinesPacket decode(FriendlyByteBuf buf) {
        int poolCount = buf.readInt();

        Map<String, List<String>> lines = new LinkedHashMap<>();

        for (int i = 0; i < poolCount; i++) {
            String poolId = buf.readUtf();
            int lineCount = buf.readInt();

            ArrayList<String> poolLines = new ArrayList<>();

            for (int j = 0; j < lineCount; j++) {
                poolLines.add(buf.readUtf());
            }

            lines.put(poolId, poolLines);
        }

        return new SyncRuntimeClientLinesPacket(lines);
    }

    public static void handle(SyncRuntimeClientLinesPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientServerDialogueConfig.mergeRuntimeLines(msg.lines);
        });

        ctx.get().setPacketHandled(true);
    }
}