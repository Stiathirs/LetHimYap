package com.lethimyap.client.config;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

public class DialogueLineList
        extends ObjectSelectionList<DialogueLineList.Entry> {

    private static final int TOTAL_WIDTH = 300;

    private static final int BUTTON_SIZE = 20;
    private static final int GAP = 2;

    public DialogueLineList(
            Minecraft minecraft,
            int width,
            int height,
            int top,
            int bottom,
            int itemHeight,
            List<String> messages,
            Consumer<Integer> onEdit,
            Consumer<Integer> onDelete
    ) {
        super(
                minecraft,
                width,
                height,
                top,
                bottom,
                itemHeight
        );

        for (int i = 0; i < messages.size(); i++) {
            addEntry(
                    new Entry(
                            i,
                            messages.get(i),
                            onEdit,
                            onDelete
                    )
            );
        }
    }

    @Override
    public int getRowWidth() {
        return TOTAL_WIDTH;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.width / 2 + (TOTAL_WIDTH / 2) + 10;
    }

    public class Entry
            extends ObjectSelectionList.Entry<Entry> {

        private final int index;

        private final String message;

        private final Button editButton;
        private final Button deleteButton;

        private Entry(
                int index,
                String message,
                Consumer<Integer> onEdit,
                Consumer<Integer> onDelete
        ) {
            this.index = index;
            this.message = message == null ? "" : message;

            this.editButton = Button.builder(
                    Component.literal("🖉")
                            .withStyle(ChatFormatting.YELLOW),
                    button -> onEdit.accept(index)
            ).bounds(
                    0,
                    0,
                    BUTTON_SIZE,
                    BUTTON_SIZE
            ).build();

            this.deleteButton = Button.builder(
                    Component.literal("🗑")
                            .withStyle(ChatFormatting.RED),
                    button -> onDelete.accept(index)
            ).bounds(
                    0,
                    0,
                    BUTTON_SIZE,
                    BUTTON_SIZE
            ).build();
        }

        @Override
        public void render(
                GuiGraphics graphics,
                int rowIndex,
                int top,
                int left,
                int width,
                int height,
                int mouseX,
                int mouseY,
                boolean hovered,
                float partialTick
        ) {
            int rowLeft =
                    DialogueLineList.this.width / 2
                            - TOTAL_WIDTH / 2;

            int actionsWidth =
                    BUTTON_SIZE + GAP + BUTTON_SIZE;

            int textWidth =
                    TOTAL_WIDTH
                            - actionsWidth
                            - GAP;

            String displayedMessage =
                    createPreview(
                            message,
                            textWidth
                    );

            int textY =
                    top + (BUTTON_SIZE - minecraft.font.lineHeight) / 2;

            graphics.drawString(
                    minecraft.font,
                    displayedMessage,
                    rowLeft,
                    textY,
                    0xFFFFFF,
                    true
            );

            int editX =
                    rowLeft
                            + TOTAL_WIDTH
                            - actionsWidth;

            editButton.setX(editX);
            editButton.setY(top);

            deleteButton.setX(
                    editX + BUTTON_SIZE + GAP
            );
            deleteButton.setY(top);

            editButton.render(
                    graphics,
                    mouseX,
                    mouseY,
                    partialTick
            );

            deleteButton.render(
                    graphics,
                    mouseX,
                    mouseY,
                    partialTick
            );
        }

        @Override
        public boolean mouseClicked(
                double mouseX,
                double mouseY,
                int mouseButton
        ) {
            if (editButton.mouseClicked(
                    mouseX,
                    mouseY,
                    mouseButton
            )) {
                return true;
            }

            return deleteButton.mouseClicked(
                    mouseX,
                    mouseY,
                    mouseButton
            );
        }

        @Override
        public boolean mouseReleased(
                double mouseX,
                double mouseY,
                int mouseButton
        ) {
            boolean handled = false;

            handled |= editButton.mouseReleased(
                    mouseX,
                    mouseY,
                    mouseButton
            );

            handled |= deleteButton.mouseReleased(
                    mouseX,
                    mouseY,
                    mouseButton
            );

            return handled;
        }

        @Override
        public Component getNarration() {
            return Component.literal(
                    "Dialogue line " + (index + 1)
            );
        }
    }

    private String createPreview(
            String message,
            int availableWidth
    ) {
        if (message == null || message.isEmpty()) {
            return "<empty>";
        }

        if (minecraft.font.width(message) <= availableWidth) {
            return message;
        }

        String suffix = "...";
        int suffixWidth = minecraft.font.width(suffix);

        String trimmed =
                minecraft.font.plainSubstrByWidth(
                        message,
                        Math.max(
                                0,
                                availableWidth - suffixWidth
                        )
                );

        return trimmed + suffix;
    }
}