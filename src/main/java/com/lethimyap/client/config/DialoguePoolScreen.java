package com.lethimyap.client.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class DialoguePoolScreen extends YapConfigScreen {

    private final Screen parent;
    private final String group;

    public DialoguePoolScreen(
            Screen parent,
            String group
    ) {
        super(Component.literal(
                DialogueGroupScreen.prettify(group)
        ));

        this.parent = parent;
        this.group = group;
    }

    @Override
    protected void init() {
        List<String> pools =
                DialogueConfigBrowser.getPools(group);

        DialogueNavigationList list =
                new DialogueNavigationList(
                        this.minecraft,
                        this.width,
                        this.height,
                        42,
                        this.height - 36,
                        24,
                        pools,
                        pool -> this.minecraft.setScreen(
                                new DialogueLineScreen(
                                        this,
                                        group,
                                        pool
                                )
                        )
                );

        addRenderableWidget(list);

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
                Component.literal("pools." + group),
                this.width / 2,
                26,
                0x808080
        );

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}