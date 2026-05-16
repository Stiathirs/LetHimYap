package com.lethimyap.client;

import com.lethimyap.api.YapPriority;
import com.lethimyap.messages.ClientDialogueConfig;
import com.lethimyap.network.ModNetwork;
import com.lethimyap.network.SelectDialoguePacket;

public class ClientDialogueSelector {

    public static void chooseAndSend(
            String poolId,
            boolean important,
            boolean pain,
            YapPriority priority
    ) {
        ClientDialogueConfig config = ClientDialogueConfig.get();

        String message = config.pickMessage(poolId);

        if (message == null || message.isBlank()) {
            return;
        }

        ModNetwork.CHANNEL.sendToServer(
                new SelectDialoguePacket(
                        message,
                        config.dialogueColor.id,
                        important,
                        pain,
                        priority
                )
        );
    }
}