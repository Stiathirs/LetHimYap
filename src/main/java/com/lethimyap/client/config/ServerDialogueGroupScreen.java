package com.lethimyap.client.config;

import com.lethimyap.client.ClientServerDialogueConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ServerDialogueGroupScreen extends YapConfigScreen {

    private final Screen parent;
    private DialogueNavigationList groupList;

    public ServerDialogueGroupScreen(Screen parent) {
        super(Component.literal("Server Dialogue"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        List<String> groups =
                ClientServerDialogueConfig.getCurrentServerGroups();

        this.groupList = new DialogueNavigationList(
                this.minecraft,
                this.width,
                this.height,
                42,
                this.height - 36,
                24,
                groups,
                group -> this.minecraft.setScreen(
                        new ServerDialoguePoolScreen(
                                this,
                                group
                        )
                )
        );

        addRenderableWidget(this.groupList);

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
                20,
                0xFFFFFF
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