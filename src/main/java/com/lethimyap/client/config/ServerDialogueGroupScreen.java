package com.lethimyap.client.config;

import java.util.List;

import com.lethimyap.client.ClientServerDialogueConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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
                group -> Minecraft.getInstance().setScreen(
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
        Minecraft.getInstance().setScreen(parent);
    }
}