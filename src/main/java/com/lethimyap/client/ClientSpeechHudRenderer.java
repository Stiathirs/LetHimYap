package com.lethimyap.client;

import com.lethimyap.LetHimYap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = LetHimYap.MODID,
        value = Dist.CLIENT
)
public class ClientSpeechHudRenderer {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        ClientSpeechHudData.tick();
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null) return;
        if (!mc.options.getCameraType().isFirstPerson()) return;
        if (ClientSpeechHudData.text == null || ClientSpeechHudData.text.isEmpty()) return;

        Font font = mc.font;
        String text = ClientSpeechHudData.text;

        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();

        int x = (screenWidth - font.width(text)) / 2;
        var clientConfig = com.lethimyap.messages.ClientDialogueConfig.get();

        int distance = Math.max(0, clientConfig.hudEdgeDistance);

        int y;

        if ("top".equalsIgnoreCase(clientConfig.hudAnchor)) {
            y = distance;
        } else {
            y = screenHeight - distance;
        }

        float fade = ClientSpeechHudData.alpha();
        if (fade <= 0.0f) return;

        int baseRgb = getRgb(ClientSpeechHudData.color.formatting);

        // Brightness fallback
        int fadedRgb = multiplyRgb(baseRgb, fade);

        // Alpha attempt. Seriously, why the heck doesn't this work? Even ChatGPT was like "idk bro"
        int alpha = Math.max(0, Math.min(255, (int) (fade * 255.0f)));

        int finalColor = (alpha << 24) | fadedRgb;

        event.getGuiGraphics().drawString(
                font,
                text,
                x,
                y,
                finalColor,
                false
        );
    }

    private static int getRgb(ChatFormatting formatting) {
        Integer color = formatting.getColor();
        return color == null ? 0xFFFFFF : color;
    }

    private static int multiplyRgb(int rgb, float multiplier) {
        multiplier = Math.max(0.0f, Math.min(1.0f, multiplier));

        int r = (int) (((rgb >> 16) & 255) * multiplier);
        int g = (int) (((rgb >> 8) & 255) * multiplier);
        int b = (int) ((rgb & 255) * multiplier);

        return (r << 16) | (g << 8) | b;
    }
}