package com.lethimyap.client;

import com.lethimyap.LetHimYap;
import com.lethimyap.messages.ClientDialogueConfig;
import com.lethimyap.network.ModNetwork;
import com.lethimyap.network.SyncVoiceSelectionPacket;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = LetHimYap.MODID,
        value = Dist.CLIENT
)
public class ClientConfigLoadEvents {

    @SubscribeEvent
    public static void onClientPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
        ClientDialogueConfig config =
                ClientDialogueConfig.get();

        ModNetwork.CHANNEL.sendToServer(
                new SyncVoiceSelectionPacket(
                        config.voice
                )
        );
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        ClientDialogueConfig.reloadIfChanged();
    }
}