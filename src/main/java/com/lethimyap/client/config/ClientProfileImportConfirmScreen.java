package com.lethimyap.client.config;

import com.lethimyap.messages.ClientDialogueConfig;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ClientProfileImportConfirmScreen
        extends Screen {

    private final Screen parent;
    private final ClientConfigProfile profile;
    private final List<String> unknownPools;

    public ClientProfileImportConfirmScreen(
            Screen parent,
            ClientConfigProfile profile,
            List<String> unknownPools
    ) {
        super(Component.literal("Import Profile"));

        this.parent = parent;
        this.profile = profile;
        this.unknownPools = List.copyOf(unknownPools);
    }

    @Override
    protected void init() {
        int buttonY =
                height / 2 + 24;

        addRenderableWidget(
                Button.builder(
                        Component.literal("Import"),
                        button -> confirmImport()
                )
                .bounds(
                        width / 2 - 102,
                        buttonY,
                        100,
                        20
                )
                .build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("Cancel"),
                        button ->
                                minecraft.setScreen(parent)
                )
                .bounds(
                        width / 2 + 2,
                        buttonY,
                        100,
                        20
                )
                .build()
        );
    }

    private void confirmImport() {
        if (!unknownPools.isEmpty()) {
            minecraft.setScreen(
                    new ClientProfileUnknownPoolsScreen(
                            parent,
                            profile,
                            unknownPools
                    )
            );

            return;
        }

        boolean success =
                ClientDialogueConfig.applyProfile(
                        profile
                );

        finishImport(success);
    }

    private void finishImport(boolean success) {
        if (success) {
            SystemToast.add(
                    minecraft.getToasts(),
                    SystemToast.SystemToastIds.PERIODIC_NOTIFICATION,
                    Component.literal("Profile Imported"),
                    Component.literal(
                            "Client settings and dialogue were imported."
                    )
            );

            minecraft.setScreen(parent);
            return;
        }

        SystemToast.add(
                minecraft.getToasts(),
                SystemToast.SystemToastIds.PACK_LOAD_FAILURE,
                Component.literal("Import Failed"),
                Component.literal(
                        "The client profile could not be applied."
                )
        );

        minecraft.setScreen(parent);
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
                font,
                title,
                width / 2,
                height / 2 - 44,
                0xFFFFFF
        );

        graphics.drawCenteredString(
                font,
                Component.literal(
                        "This will replace your current client settings"
                ),
                width / 2,
                height / 2 - 18,
                0xA0A0A0
        );

        graphics.drawCenteredString(
                font,
                Component.literal(
                        "and dialogue overrides with this profile."
                ),
                width / 2,
                height / 2 - 6,
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
        minecraft.setScreen(parent);
    }
}