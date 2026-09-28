package com.lethimyap.client.config;

import com.lethimyap.messages.ClientDialogueConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;


import java.util.List;

public class ClientProfileUnknownPoolsScreen
        extends Screen {

    private final Screen parent;
    private final ClientConfigProfile profile;
    private final List<String> unknownPools;

    private String hoveredPoolId;

    private UnknownPoolList poolList;

    public ClientProfileUnknownPoolsScreen(
            Screen parent,
            ClientConfigProfile profile,
            List<String> unknownPools
    ) {
        super(Component.literal("Unused Dialogue Pools"));

        this.parent = parent;
        this.profile = profile;
        this.unknownPools = List.copyOf(unknownPools);
    }

    @Override
    protected void init() {
        int listTop = 58;
        int listBottom = height - 40;

        poolList =
                new UnknownPoolList(
                        minecraft,
                        width,
                        height,
                        listTop,
                        listBottom,
                        20,
                        this
                );

        for (String poolId : unknownPools) {
            poolList.addPool(poolId);
        }

        addRenderableWidget(poolList);

        int buttonY =
                height - 28;

        addRenderableWidget(
                Button.builder(
                        Component.literal("Keep"),
                        button -> keepPools()
                )
                .bounds(
                        width / 2 - 154,
                        buttonY,
                        100,
                        20
                )
                .build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("Remove"),
                        button -> removePools()
                )
                .bounds(
                        width / 2 - 50,
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
                        width / 2 + 54,
                        buttonY,
                        100,
                        20
                )
                .build()
        );
    }

    private void keepPools() {
        boolean success =
                ClientDialogueConfig.applyProfile(profile);

        finishImport(success);
    }

    private void removePools() {
        ClientConfigProfile filtered =
                copyWithoutUnknownPools();

        boolean success =
                ClientDialogueConfig.applyProfile(filtered);

        if (success) {
            success =
                    ClientDialogueConfig.removePools(
                            unknownPools
                    );
        }

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
        } else {
            SystemToast.add(
                    minecraft.getToasts(),
                    SystemToast.SystemToastIds.PACK_LOAD_FAILURE,
                    Component.literal("Import Failed"),
                    Component.literal(
                            "The client profile could not be applied."
                    )
            );
        }

        minecraft.setScreen(parent);
    }

    private ClientConfigProfile copyWithoutUnknownPools() {
        ClientConfigProfile copy =
                ClientConfigProfileIO.copyProfile(profile);

        for (String poolId : unknownPools) {
            copy.pools.remove(poolId);
        }

        return copy;
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        /*
         * Entries will set this again while the list renders
         * if a truncated pool ID is currently hovered.
         */
        hoveredPoolId = null;

        renderBackground(graphics);

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        graphics.drawCenteredString(
                font,
                title,
                width / 2,
                18,
                0xFFFFFF
        );

        graphics.drawCenteredString(
                font,
                Component.literal(
                        "These dialogue pools are not currently registered."
                ),
                width / 2,
                34,
                0xA0A0A0
        );

        /*
         * Render the tooltip last so the footer and buttons
         * cannot draw over it.
         */
        if (hoveredPoolId != null) {
            Component tooltipText =
                    Component.literal(hoveredPoolId);

            List<FormattedCharSequence> tooltipLines =
                    font.split(
                            tooltipText,
                            Math.min(
                                    300,
                                    width - 40
                            )
                    );

            graphics.renderTooltip(
                    font,
                    tooltipLines,
                    mouseX,
                    mouseY
            );
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }


    private static class UnknownPoolList
            extends ContainerObjectSelectionList<
                    UnknownPoolList.Entry> {

        private final ClientProfileUnknownPoolsScreen screen;

        public UnknownPoolList(
                Minecraft minecraft,
                int width,
                int height,
                int top,
                int bottom,
                int itemHeight,
                ClientProfileUnknownPoolsScreen screen
        ) {
            super(
                    minecraft,
                    width,
                    height,
                    top,
                    bottom,
                    itemHeight
            );

            this.screen = screen;
        }

        public void addPool(String poolId) {
            addEntry(
                    new Entry(poolId)
            );
        }

        @Override
        public int getRowWidth() {
            return 310;
        }

        @Override
        protected int getScrollbarPosition() {
            return width / 2 + 166;
        }


        private class Entry
                extends ContainerObjectSelectionList.Entry<Entry> {

            private final String poolId;

            private Entry(String poolId) {
                this.poolId = poolId;
            }

            @Override
            public void render(
                    GuiGraphics graphics,
                    int index,
                    int top,
                    int left,
                    int width,
                    int height,
                    int mouseX,
                    int mouseY,
                    boolean hovered,
                    float partialTick
            ) {
                int maxTextWidth =
                        UnknownPoolList.this.getRowWidth() - 16;

                Component fullText =
                        Component.literal(poolId);

                boolean truncated =
                        minecraft.font.width(fullText)
                                > maxTextWidth;

                Component displayText;

                if (truncated) {
                    String shortened =
                            minecraft.font.plainSubstrByWidth(
                                    poolId,
                                    maxTextWidth
                                            - minecraft.font.width("...")
                            );

                    displayText =
                            Component.literal(
                                    shortened + "..."
                            );
                } else {
                    displayText =
                            fullText;
                }

                graphics.drawCenteredString(
                        minecraft.font,
                        displayText,
                        UnknownPoolList.this.width / 2,
                        top + 6,
                        0xFFFF55
                );

                /*
                 * Don't render the tooltip here.
                 *
                 * Tell the screen which pool is hovered so it can
                 * render the tooltip after every other widget.
                 */
                if (hovered && truncated) {
                    screen.hoveredPoolId =
                            poolId;
                }
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return List.of();
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of();
            }
        }
    }
}