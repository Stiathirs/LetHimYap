package com.lethimyap.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.lethimyap.LetHimYap;
import com.lethimyap.messages.ClientDialogueConfig;
import com.lethimyap.messages.ServerMessageConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

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
        if (mc.level == null || mc.player == null) return;

        float partialTick = event.getPartialTick();

        for (Player player : mc.level.players()) {
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
        float width = -font.width(floatingText.text) / 2.0F;

        Matrix4f matrix = poseStack.last().pose();

        MultiBufferSource.BufferSource buffer =
                mc.renderBuffers().bufferSource();

        ServerMessageConfig config = ServerMessageConfig.get();

        float alpha = floatingText.alpha(config.floatingMessageFadeTicks);
        int a = (int) (alpha * 255.0f);

        int rgb = getRgb(floatingText.color.formatting);
        int color = (a << 24) | rgb;

        font.drawInBatch(
                floatingText.text,
                width,
                0,
                color,
                false,
                matrix,
                buffer,
                Font.DisplayMode.SEE_THROUGH,
                0,
                LightTexture.FULL_BRIGHT
        );

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
}