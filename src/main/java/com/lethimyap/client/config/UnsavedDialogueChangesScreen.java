package com.lethimyap.client.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class UnsavedDialogueChangesScreen extends YapConfigScreen {

    private final DialogueLineScreen editor;

    public UnsavedDialogueChangesScreen(
            DialogueLineScreen editor
    ) {
        super(Component.literal("Unsaved Changes"));
        this.editor = editor;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        addRenderableWidget(
                Button.builder(
                        Component.literal("Save & Exit"),
                        button -> editor.saveAndExitFromConfirmation()
                ).bounds(
                        centerX - 100,
                        centerY - 12,
                        200,
                        20
                ).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("Discard Changes"),
                        button -> editor.discardAndExit()
                ).bounds(
                        centerX - 100,
                        centerY + 12,
                        200,
                        20
                ).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("Cancel"),
                        button -> onClose()
                ).bounds(
                        centerX - 100,
                        centerY + 36,
                        200,
                        20
                ).build()
        );
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(graphics);

        graphics.drawCenteredString(
                this.font,
                this.title,
                this.width / 2,
                this.height / 2 - 48,
                0xFFFFFF
        );

        graphics.drawCenteredString(
                this.font,
                Component.literal(
                        "Save your changes to this dialogue pool?"
                ),
                this.width / 2,
                this.height / 2 - 32,
                0xA0A0A0
        );

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(editor);
    }
}