package com.lethimyap.client;

import com.lethimyap.LetHimYap;
import com.lethimyap.messages.ClientDialogueConfig;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

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

        int screenWidth = event.getScreen().width;
        int height = event.getScreen().height;

        int maxWidth = Math.min(
                Math.max(1, config.lastWordsMaxWidth),
                Math.max(1, screenWidth - 20)
        );

        List<FormattedCharSequence> lines =
                mc.font.split(
                        Component.literal(text),
                        maxWidth
                );

        if (lines.isEmpty()) return;

        int lineHeight = mc.font.lineHeight;
        int blockHeight = lines.size() * lineHeight;

        int y;

        switch (config.lastWordsAnchor.toLowerCase()) {
            case "top" ->
                    // Top anchor grows downward.
                    y = config.lastWordsOffset;

            case "bottom" ->
                    // Bottom anchor grows upward.
                    y = height
                            - config.lastWordsOffset
                            - blockHeight
                            + lineHeight;

            default ->
                    // Keep the complete block centered around the configured
                    // center-relative position.
                    y = (height / 2)
                            + config.lastWordsOffset
                            - (blockHeight / 2)
                            + (lineHeight / 2);
        }

        for (int i = 0; i < lines.size(); i++) {
            event.getGuiGraphics().drawCenteredString(
                    mc.font,
                    lines.get(i),
                    x,
                    y + (i * lineHeight),
                    0xFFFFFF
            );
        }
    }
}