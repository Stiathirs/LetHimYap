package com.lethimyap.mixin;

import com.lethimyap.client.ClientLastWordsData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public class DeathScreenMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void lethimyap$renderLastWords(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick,
            CallbackInfo ci
    ) {
        if (ClientLastWordsData.lastWords == null
                || ClientLastWordsData.lastWords.isEmpty()) {
            return;
        }

        DeathScreen screen = (DeathScreen) (Object) this;
        Minecraft mc = Minecraft.getInstance();

        if (mc.font == null) return;

        Component text = Component.literal(
                "Last words: " + ClientLastWordsData.lastWords
        );

        int x = screen.width / 2;

        // Vanilla-ish placement: below the score line, above the buttons.
        // This avoids hard anchoring to the bottom of the screen.
        int y = screen.height / 2 - 20;

        guiGraphics.drawCenteredString(
                mc.font,
                text,
                x,
                y,
                0xFFFFFF
        );
    }
}