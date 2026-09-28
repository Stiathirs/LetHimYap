package com.lethimyap.client.config;

import com.lethimyap.messages.PlaceholderRegistry;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class DialogueLineEditScreen extends YapConfigScreen {
    private final Screen parent;
    private final String originalText;
    private final Consumer<String> onDone;

    private String draftText;
    private int placeholderInsertPosition;
    private int cursorPositionToRestore = -1;

    private EditBox editBox;
    
    public DialogueLineEditScreen(
            Screen parent,
            String originalText,
            Consumer<String> onDone
    ) {
        super(
                Component.literal(
                        originalText.isEmpty()
                                ? "Add Dialogue Line"
                                : "Edit Dialogue Line"
                )
        );

        this.parent = parent;
        this.originalText =
                originalText == null ? "" : originalText;

        this.draftText = this.originalText;

        this.onDone = onDone;
    }

    @Override
    protected void init() {
        this.editBox = new EditBox(
                this.font,
                this.width / 2 - 150,
                this.height / 2 - 20,
                300,
                20,
                Component.literal("Dialogue Line")
        );
        
        editBox.setMaxLength(1024);
        editBox.setValue(draftText);

        if (cursorPositionToRestore >= 0) {
            int position = Math.min(
                    cursorPositionToRestore,
                    draftText.length()
            );

            editBox.setCursorPosition(position);
            editBox.setHighlightPos(position);

            cursorPositionToRestore = -1;
        }

        addRenderableWidget(editBox);

        Button placeholderButton =
                Button.builder(
                        Component.literal("+ Placeholder"),
                        button -> openPlaceholderMenu()
                ).bounds(
                        this.width / 2 - 102,
                        this.height / 2 + 12,
                        204,
                        20
                ).build();

        placeholderButton.setTooltip(
                Tooltip.create(
                        Component.literal(
                                "Insert a registered placeholder at the cursor."
                        )
                )
        );

        placeholderButton.active =
                !PlaceholderRegistry
                        .getRegisteredPlaceholderIds()
                        .isEmpty();

        addRenderableWidget(placeholderButton);

        addRenderableWidget(
                Button.builder(
                        Component.literal("Done"),
                        button -> finishEditing()
                ).bounds(
                        this.width / 2 - 102,
                        this.height / 2 + 36,
                        100,
                        20
                ).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("Cancel"),
                        button -> onClose()
                ).bounds(
                        this.width / 2 + 2,
                        this.height / 2 + 36,
                        100,
                        20
                ).build()
        );

        setInitialFocus(editBox);
    }

    private void openPlaceholderMenu() {
            draftText = editBox.getValue();
            placeholderInsertPosition = editBox.getCursorPosition();

            this.minecraft.setScreen(
                    new PlaceholderSelectionScreen(
                            this,
                            this::insertPlaceholder
                    )
            );
        }

        private void insertPlaceholder(String id) {
        if (id == null || id.isBlank()) {
            return;
        }

        String placeholder = "{" + id + "}";

        int position = Math.max(
                0,
                Math.min(
                        placeholderInsertPosition,
                        draftText.length()
                )
        );

        draftText =
                draftText.substring(0, position)
                        + placeholder
                        + draftText.substring(position);

        cursorPositionToRestore =
                position + placeholder.length();
    }

    private void finishEditing() {
        onDone.accept(editBox.getValue());
        this.minecraft.setScreen(parent);
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
                this.height / 2 - 52,
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
        this.minecraft.setScreen(parent);
    }
}