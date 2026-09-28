package com.lethimyap.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.List;

public class GeneralSettingsList
        extends ContainerObjectSelectionList<GeneralSettingsList.Entry> {

    public GeneralSettingsList(
            Minecraft minecraft,
            int width,
            int height,
            int top,
            int bottom
    ) {
        super(
                minecraft,
                width,
                height,
                top,
                bottom,
                26
        );
    }

    public void addSection(String title) {
        addEntry(
                new Entry(
                        title,
                        null,
                        true
                )
        );
    }

    public void addSetting(
        String label,
        AbstractWidget widget
) {
    addSetting(label, widget, null);
}

    public void addSetting(
            String label,
            AbstractWidget widget,
            String tooltip
    ) {
        if (tooltip != null && !tooltip.isBlank()) {
            widget.setTooltip(
                    Tooltip.create(
                            Component.literal(tooltip)
                    )
            );
        }

        addEntry(
                new Entry(
                        label,
                        widget,
                        false
                )
        );
    }

    @Override
    public int getRowWidth() {
        return 310;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.width / 2 + 166;
    }

    public class Entry
            extends ContainerObjectSelectionList.Entry<Entry> {

        private final String label;
        private final AbstractWidget widget;
        private final boolean section;

        private Entry(
                String label,
                AbstractWidget widget,
                boolean section
        ) {
            this.label = label;
            this.widget = widget;
            this.section = section;
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
            if (section) {
                graphics.drawString(
                        minecraft.font,
                        Component.literal(label),
                        left,
                        top + 9,
                        0xA0A0A0,
                        false
                );

                return;
            }

            int widgetWidth = 140;

            graphics.drawString(
                    minecraft.font,
                    Component.literal(label),
                    left,
                    top + 7,
                    0xFFFFFF,
                    false
            );

            widget.setX(
                    left + width - widgetWidth
            );

            widget.setY(
                    top + 2
            );

            widget.setWidth(widgetWidth);

            widget.render(
                    graphics,
                    mouseX,
                    mouseY,
                    partialTick
            );
        }

        @Override
        public List<? extends GuiEventListener> children() {
            if (widget == null) {
                return List.of();
            }

            return List.of(widget);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            if (widget == null) {
                return List.of();
            }

            return List.of(widget);
        }
    }
}