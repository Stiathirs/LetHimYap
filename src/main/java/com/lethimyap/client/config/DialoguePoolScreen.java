package com.lethimyap.client.config;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;

public class DialoguePoolScreen extends YapConfigScreen {

    private final Screen parent;
    private final String group;

    private final String searchText;

    public DialoguePoolScreen(Screen parent, String group, String searchText) {
        super(Component.literal(DialogueGroupScreen.prettify(group)));

        this.parent = parent;
        this.group = group;
        this.searchText = searchText;
    }

    @Override
    protected void init() {
        List<String> pools = new ArrayList<>();

        for (String pool : DialogueConfigBrowser.getPools(group))
            if (DialogueConfigBrowser.poolMatchesSearch(group, pool, searchText)) pools.add(pool);

        DialogueNavigationList list =
                new DialogueNavigationList(
                        this.minecraft,
                        this.width,
                        this.height,
                        42,
                        this.height - 36,
                        24,
                        pools,
                        pool -> Minecraft.getInstance().setScreen(
                                new DialogueLineScreen(this, group, pool)
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
        Minecraft.getInstance().setScreen(parent);
    }
}