package com.lethimyap.client;

import java.util.List;

import org.joml.Matrix4f;

import com.lethimyap.LetHimYap;
import com.lethimyap.messages.ClientDialogueConfig;
import com.lethimyap.messages.ServerMessageConfig;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = LetHimYap.MODID,
        value = Dist.CLIENT
)
public class ClientFloatingTextRenderer {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        ClientFloatingTextData.tick();
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;

        if (level == null || mc.player == null) return;

        float partialTick = event.getPartialTick();

        for (Player player : level.players()) {
            ClientFloatingTextData.FloatingText floatingText =
                    ClientFloatingTextData.get(player.getUUID());

            if (floatingText == null || floatingText.text.isEmpty()) continue;

            boolean isSelf = player == mc.player;
            boolean firstPerson = mc.options.getCameraType().isFirstPerson();

            // In first person, don't show your own overhead speech.
            // Other players' overhead speech still renders.
            if (isSelf && firstPerson) {
                continue;
            }

            renderTextAbovePlayer(event, player, floatingText, partialTick);
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

    private static void renderTextAbovePlayer(
            RenderLevelStageEvent event,
            Player player,
            ClientFloatingTextData.FloatingText floatingText,
            float partialTick
    ) {
        Minecraft mc = Minecraft.getInstance();

        double x = Mth.lerp(partialTick, player.xOld, player.getX());
        double y = Mth.lerp(partialTick, player.yOld, player.getY())
                + player.getBbHeight()
                + ClientDialogueConfig.get().overheadYOffset;
        double z = Mth.lerp(partialTick, player.zOld, player.getZ());

        double camX = event.getCamera().getPosition().x;
        double camY = event.getCamera().getPosition().y;
        double camZ = event.getCamera().getPosition().z;

        PoseStack poseStack = event.getPoseStack();

        poseStack.pushPose();

        poseStack.translate(
                x - camX,
                y - camY,
                z - camZ
        );

        poseStack.mulPose(event.getCamera().rotation());
        float scale = ClientDialogueConfig.get().overheadScale;
        poseStack.scale(-scale, -scale, scale);

        Font font = mc.font;

        String visibleText = floatingText.text;
        String fullText = floatingText.fullText;

        if (fullText == null || fullText.isEmpty()) {
            fullText = visibleText;
        }

        int maxWidth = Math.max(
                1,
                ClientDialogueConfig.get().overheadMaxWidth
        );

        List<FormattedCharSequence> fullLines =
                font.split(
                        Component.literal(fullText),
                        maxWidth
                );

        if (fullLines.isEmpty()) {
            poseStack.popPose();
            return;
        }

        Matrix4f matrix = poseStack.last().pose();

        float alpha = floatingText.alpha(
                ServerMessageConfig.get().floatingMessageFadeTicks
        );

        int baseRgb = getRgb(floatingText.color.formatting);
        int fadedRgb = multiplyRgb(baseRgb, alpha);

        int a = Math.max(
                0,
                Math.min(255, (int) (alpha * 255.0f))
        );

        int color = (a << 24) | fadedRgb;

        MultiBufferSource.BufferSource buffer =
                mc.renderBuffers().bufferSource();

        int lineHeight = font.lineHeight;

        /*
        * Determine how many predetermined lines have actually begun
        * appearing so the overhead block grows upward as it types.
        */
        int remainingVisibleChars = visibleText.length();
        int visibleLineCount = 0;

        for (FormattedCharSequence line : fullLines) {
            if (remainingVisibleChars <= 0) break;

            String lineText = formattedSequenceToString(line);

            visibleLineCount++;
            remainingVisibleChars -= lineText.length();

            /*
            * Font.split() may consume whitespace at a wrap boundary.
            * Account for that whitespace in the original message.
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

        int charsRemaining = visibleText.length();

        for (int i = 0; i < fullLines.size(); i++) {
            if (charsRemaining <= 0) break;

            String fullLine =
                    formattedSequenceToString(fullLines.get(i));

            int charsForLine =
                    Math.min(charsRemaining, fullLine.length());

            String visibleLine =
                    fullLine.substring(0, charsForLine);

            if (!visibleLine.isEmpty()) {
                float width =
                        -font.width(visibleLine) / 2.0F;

                /*
                * Keep the lowest currently-visible line at the normal
                * overhead position and grow additional lines upward.
                */
                float lineY =
                        (i - (visibleLineCount - 1)) * lineHeight;

                font.drawInBatch(
                        visibleLine,
                        width,
                        lineY,
                        color,
                        false,
                        matrix,
                        buffer,
                        Font.DisplayMode.SEE_THROUGH,
                        0,
                        LightTexture.FULL_BRIGHT
                );
            }

            charsRemaining -= charsForLine;

            int consumed =
                    visibleText.length() - charsRemaining;

            /*
            * Skip whitespace from the original message that Font.split()
            * consumed at the line boundary.
            */
            while (charsRemaining > 0
                    && consumed < fullText.length()
                    && Character.isWhitespace(fullText.charAt(consumed))) {

                charsRemaining--;
                consumed++;
            }
        }

        buffer.endBatch();

poseStack.popPose();
}

    private static int getRgb(ChatFormatting formatting) {
        Integer color = formatting.getColor();

        if (color == null) {
            return 0xFFFFFF;
        }

        return color;
    }

    private static int multiplyRgb(int rgb, float multiplier) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        r = Math.max(0, Math.min(255, (int) (r * multiplier)));
        g = Math.max(0, Math.min(255, (int) (g * multiplier)));
        b = Math.max(0, Math.min(255, (int) (b * multiplier)));

        return (r << 16) | (g << 8) | b;
    }
}