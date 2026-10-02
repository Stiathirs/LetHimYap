package com.lethimyap.client.config;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;

public class DialogueGroupScreen extends YapConfigScreen {

    private final Screen parent;

    public DialogueGroupScreen(Screen parent) {
        super(Component.literal("Dialogue Lines"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        List<String> groups =
                DialogueConfigBrowser.getGroups();

        DialogueNavigationList list =
                new DialogueNavigationList(
                        this.minecraft,
                        this.width,
                        this.height,
                        42,
                        this.height - 36,
                        24,
                        groups,
                        group -> Minecraft.getInstance().setScreen(new DialoguePoolScreen(this,group))
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
                20,
                0xFFFFFF
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
        Minecraft.getInstance().setScreen(parent);
    }

    static String prettify(String id) {
        if (id == null || id.isBlank()) {
            return "";
        }

        /*
        * Colon-separated addon group.
        * Kept for compatibility with IDs such as:
        * missingaddon:injury
        */
        int namespaceSeparator =
                id.indexOf(':');

        if (namespaceSeparator >= 0) {
            String namespace =
                    id.substring(
                            0,
                            namespaceSeparator
                    );

            String group =
                    id.substring(
                            namespaceSeparator + 1
                    );

            return formatAddonGroup(
                    namespace,
                    group
            );
        }

        /*
        * Dot-separated addon group.
        *
        * Example:
        * lethimdrink.thirst
        */
        int groupSeparator =
                id.indexOf('.');

        if (groupSeparator >= 0) {
            String namespace =
                    id.substring(
                            0,
                            groupSeparator
                    );

            String group =
                    id.substring(
                            groupSeparator + 1
                    );

            return formatAddonGroup(
                    namespace,
                    group
            );
        }

        return prettifySimple(id);
    }

    private static String formatAddonGroup(
            String namespace,
            String group
    ) {
        String addonName =
                ModList.get()
                        .getModContainerById(namespace)
                        .map(container ->
                                container.getModInfo().getDisplayName()
                        )
                        .orElseGet(() ->
                                prettifySimple(namespace)
                        );

        return prettifySimple(group)
                + " ("
                + addonName
                + ")";
    }

    private static String prettifySimple(String id) {
        if (id == null || id.isBlank()) {
            return "";
        }

        String[] words =
                id.replace('-', '_')
                        .split("_");

        StringBuilder result =
                new StringBuilder();

        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }

            if (!result.isEmpty()) {
                result.append(' ');
            }

            result.append(
                    Character.toUpperCase(
                            word.charAt(0)
                    )
            );

            if (word.length() > 1) {
                result.append(
                        word.substring(1)
                );
            }
        }

        return result.toString();
    }
}