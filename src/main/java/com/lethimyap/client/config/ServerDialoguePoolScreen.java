package com.lethimyap.client.config;

import com.lethimyap.client.ClientServerDialogueConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ServerDialoguePoolScreen extends YapConfigScreen {

    private final Screen parent;
    private final String group;

    private DialogueNavigationList poolList;

    public ServerDialoguePoolScreen(
            Screen parent,
            String group
    ) {
        super(Component.literal("Server Dialogue"));
        this.parent = parent;
        this.group = group;
    }

    @Override
    protected void init() {
        super.init();

        List<String> pools =
                ClientServerDialogueConfig.getCurrentServerPools(group);

        this.poolList = new DialogueNavigationList(
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
                                pool,
                                true
                        )
                )
        );

        addRenderableWidget(this.poolList);

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
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(guiGraphics);

        guiGraphics.drawCenteredString(
                this.font,
                this.title,
                this.width / 2,
                14,
                0xFFFFFF
        );

        guiGraphics.drawCenteredString(
                this.font,
                Component.literal(prettify(group)),
                this.width / 2,
                26,
                0xA0A0A0
        );

        super.render(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick
        );
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    private static String prettify(String id) {
        String[] words = id.split("_");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.isEmpty()) continue;

            if (!result.isEmpty()) {
                result.append(' ');
            }

            result.append(
                    Character.toUpperCase(word.charAt(0))
            );

            if (word.length() > 1) {
                result.append(word.substring(1));
            }
        }

        return result.toString();
    }
}