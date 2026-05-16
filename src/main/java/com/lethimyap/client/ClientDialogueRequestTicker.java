package com.lethimyap.client;

import com.lethimyap.LetHimYap;
import com.lethimyap.network.ModNetwork;
import com.lethimyap.network.RequestDialoguePacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = LetHimYap.MODID,
        value = Dist.CLIENT
)
public class ClientDialogueRequestTicker {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null) return;
        if (!mc.player.isAlive()) return;

        if (ClientDialogueTimingData.tickAndShouldRequest()) {
            ModNetwork.CHANNEL.sendToServer(new RequestDialoguePacket());
        }
    }
}