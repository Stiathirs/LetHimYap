package com.lethimyap.client.config;

import com.lethimyap.client.config.ClientConfigProfileIO;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ClientConfigScreen extends YapConfigScreen {

    private final Screen parent;

    public ClientConfigScreen(Screen parent) {
        super(Component.literal("Let Him Yap"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = this.height / 2 - 77;

        addRenderableWidget(
                Button.builder(
                        Component.literal("General Settings"),
                        button -> this.minecraft.setScreen(
                                new GeneralSettingsScreen(this)
                        )
                )
                .bounds(centerX - 100,startY,200,20)
                .build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("Dialogue Lines"),
                        button -> this.minecraft.setScreen(
                                new DialogueGroupScreen(this)
                        )
                )
                .bounds(centerX - 100,startY + 24,200,20)
                .build()
        );

        boolean hasWorld =
                this.minecraft.level != null
                        && this.minecraft.player != null
                        && this.minecraft.getConnection() != null;

        Button serverDialogueButton =
                Button.builder(
                        Component.literal("Server Dialogue"),
                        button -> this.minecraft.setScreen(
                                new ServerDialogueGroupScreen(this)
                        )
                )
                .bounds(centerX - 100, startY + 48, 200, 20)
                .build();

        serverDialogueButton.active = hasWorld;

        if (!hasWorld) {
            serverDialogueButton.setTooltip(
                    Tooltip.create(
                            Component.literal(
                                    "Join a world to edit server-specific datapack dialogue."
                            )
                    )
            );
        }

        addRenderableWidget(serverDialogueButton);

        addRenderableWidget(
                Button.builder(
                        Component.literal("Voice"),
                        button -> this.minecraft.setScreen(
                                new VoiceConfigScreen(this)
                        )
                )
                .bounds(centerX - 100, startY + 72, 200, 20)
                .build()
        );

        Button importButton =
                Button.builder(
                        Component.literal("Import"),
                        button -> {
                            String json = this.minecraft.keyboardHandler.getClipboard();

                            ClientConfigProfileIO.ImportResult result = ClientConfigProfileIO.parseImport(json);

                            if (!result.isValid()) {
                                SystemToast.add(
                                        this.minecraft.getToasts(),
                                        SystemToast.SystemToastIds.PACK_LOAD_FAILURE,
                                        Component.literal("Import Failed"),
                                        Component.literal(result.error())
                                );

                                return;
                            }

                            List<String> unknownPools =
                                    ClientConfigProfileIO.findUnknownPools(result.profile());

                            this.minecraft.setScreen(
                                    new ClientProfileImportConfirmScreen(this, result.profile(), unknownPools)
                            );
                        }
                )
                .bounds(this.width / 2 - 100, startY + 110, 99, 20)
                .build();

        importButton.setTooltip(
                Tooltip.create(
                        Component.literal(
                                "Import client settings and dialogue lines from the clipboard."
                        )
                )
        );

        addRenderableWidget(importButton);

        addRenderableWidget(
                Button.builder(
                        Component.literal("Export"),
                        button -> {
                            String json =
                                    ClientConfigProfileIO.exportToJson();

                            this.minecraft.keyboardHandler
                                    .setClipboard(json);
                        }
                )
                .bounds(this.width / 2 + 1, startY + 110, 99, 20)
                .tooltip(
                        Tooltip.create(
                                Component.literal(
                                        "Copy client settings and dialogue lines to the clipboard."
                                )
                        )
                )
                .build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("Done"),
                        button -> onClose()
                ).bounds(centerX - 100, startY + 134, 200, 20)
                .build()
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
                20,
                0xFFFFFF
        );

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}