package com.lethimyap.client.config;

import com.lethimyap.messages.PlaceholderRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class PlaceholderSelectionScreen extends YapConfigScreen {

    private final Screen parent;
    private final Consumer<String> onSelected;

    private DialogueNavigationList placeholderList;

    public PlaceholderSelectionScreen(
            Screen parent,
            Consumer<String> onSelected
    ) {
        super(Component.literal("Insert Placeholder"));

        this.parent = parent;
        this.onSelected = onSelected;
    }

    @Override
    protected void init() {
        List<String> placeholders =
                PlaceholderRegistry.getRegisteredPlaceholderIds();

        this.placeholderList =
                new DialogueNavigationList(
                        this.minecraft,
                        this.width,
                        this.height,
                        42,
                        this.height - 36,
                        24,
                        placeholders,
                        this::selectPlaceholder,
                        Function.identity()
                );

        addRenderableWidget(placeholderList);

        addRenderableWidget(
                Button.builder(
                        Component.literal("Back"),
                        button -> onClose()
                ).bounds(
                        this.width / 2 - 100,
                        this.height - 28,
                        200,
                        20
                ).build()
        );
    }

    private void selectPlaceholder(String id) {
        onSelected.accept(id);
        this.minecraft.setScreen(parent);
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
                14,
                0xFFFFFF
        );

        graphics.drawCenteredString(
                this.font,
                Component.literal("Select a placeholder to insert"),
                this.width / 2,
                26,
                0x808080
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
        this.minecraft.setScreen(parent);
    }
}