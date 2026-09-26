package com.lethimyap.client;

import com.lethimyap.LetHimYap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

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

        String visibleText = ClientSpeechHudData.text;
        String fullText = ClientSpeechHudData.fullText;

        if (fullText == null || fullText.isEmpty()) {
            fullText = visibleText;
        }

        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();

        var clientConfig =
                com.lethimyap.messages.ClientDialogueConfig.get();

        int distance = Math.max(0, clientConfig.hudEdgeDistance);

        int maxWidth = Math.min(
                Math.max(1, clientConfig.hudMaxWidth),
                Math.max(1, screenWidth - 20)
        );

        List<FormattedCharSequence> fullLines =
                font.split(
                        Component.literal(fullText),
                        maxWidth
                );

        if (fullLines.isEmpty()) return;

        int lineHeight = font.lineHeight;

        /*
        * Only count lines that have begun appearing.
        *
        * We don't want the HUD to reserve the vertical space for five
        * future lines before the typewriter has reached them.
        */
        int remainingVisibleChars = visibleText.length();
        int visibleLineCount = 0;

        for (FormattedCharSequence line : fullLines) {
            if (remainingVisibleChars <= 0) break;

            String lineText = formattedSequenceToString(line);

            visibleLineCount++;

            remainingVisibleChars -= lineText.length();

            /*
            * Account for whitespace consumed by wrapping.
            */
            while (remainingVisibleChars > 0
                    && remainingVisibleChars < visibleText.length()) {

                int consumedIndex =
                        visibleText.length() - remainingVisibleChars;

                if (consumedIndex >= fullText.length()) break;
                if (!Character.isWhitespace(fullText.charAt(consumedIndex))) break;

                remainingVisibleChars--;
            }
        }

        visibleLineCount = Math.max(1, visibleLineCount);

        int y;

        if ("top".equalsIgnoreCase(clientConfig.hudAnchor)) {
            y = distance;
        } else {
            y = screenHeight
                    - distance
                    - ((visibleLineCount - 1) * lineHeight);
        }

        float fade = ClientSpeechHudData.alpha();
        if (fade <= 0.0f) return;

        int baseRgb = getRgb(ClientSpeechHudData.color.formatting);

        int fadedRgb = multiplyRgb(baseRgb, fade);

        // Alpha attempt. Seriously, why the heck doesn't this work? Even ChatGPT was like "idk bro" when I ran this one by it.
        int alpha = Math.max(
                0,
                Math.min(255, (int) (fade * 255.0f))
        );

        int finalColor = (alpha << 24) | fadedRgb;

        int charsRemaining = visibleText.length();

        for (int i = 0; i < fullLines.size(); i++) {
            if (charsRemaining <= 0) break;

            String fullLine = formattedSequenceToString(fullLines.get(i));

            int charsForLine =
                    Math.min(charsRemaining, fullLine.length());

            String visibleLine =
                    fullLine.substring(0, charsForLine);

            if (!visibleLine.isEmpty()) {
                int x =
                        (screenWidth - font.width(visibleLine)) / 2;

                int lineY =
                        y + (i * lineHeight);

                event.getGuiGraphics().drawString(
                        font,
                        visibleLine,
                        x,
                        lineY,
                        finalColor,
                        false
                );
            }

            charsRemaining -= charsForLine;

            /*
            * Skip whitespace in the original message that Font.split()
            * consumed when creating the line break.
            */
            int consumed =
                    visibleText.length() - charsRemaining;

            while (charsRemaining > 0
                    && consumed < fullText.length()
                    && Character.isWhitespace(fullText.charAt(consumed))) {

                charsRemaining--;
                consumed++;
            }
        }
    }

    private static String formattedSequenceToString(FormattedCharSequence sequence) {
        StringBuilder builder = new StringBuilder();

        sequence.accept((index, style, codePoint) -> {
            builder.appendCodePoint(codePoint);
            return true;
        });

        return builder.toString();
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