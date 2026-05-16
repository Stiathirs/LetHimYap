package com.lethimyap.client;

import com.lethimyap.LetHimYap;
import com.lethimyap.messages.ClientDialogueConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = LetHimYap.MODID,
        value = Dist.CLIENT
)
public class ClientDeathScreenText {

    @SubscribeEvent
    public static void onRenderScreen(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof DeathScreen)) return;

        if (ClientLastWordsData.lastWords == null
                || ClientLastWordsData.lastWords.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        String text = "Last words: " + ClientLastWordsData.lastWords;

        int x = event.getScreen().width / 2;

        ClientDialogueConfig config = ClientDialogueConfig.get();

        int y;
        int height = event.getScreen().height;

        switch (config.lastWordsAnchor.toLowerCase()) {
            case "top" -> y = config.lastWordsOffset;

            case "bottom" -> y = height - config.lastWordsOffset;

            default -> y = (height / 2) + config.lastWordsOffset;
        }

        event.getGuiGraphics().drawCenteredString(
                mc.font,
                text,
                x,
                y,
                0xFFFFFF
        );
    }
}