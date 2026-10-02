package com.lethimyap.client.config;

import com.lethimyap.messages.ClientDialogueConfig;
import com.lethimyap.messages.DialogueColor;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GeneralSettingsScreen extends YapConfigScreen {

    private final Screen parent;

    // Original values
    private final String originalDialogueColor;
    private final String originalHudAnchor;
    private final int originalHudEdgeDistance;
    private final int originalHudMaxWidth;
    private final double originalOverheadYOffset;
    private final float originalOverheadScale;
    private final int originalOverheadMaxWidth;
    private final String originalLastWordsAnchor;
    private final int originalLastWordsOffset;
    private final int originalLastWordsMaxWidth;

    // Working values
    private String dialogueColor;
    private String hudAnchor;
    private int hudEdgeDistance;
    private int hudMaxWidth;
    private double overheadYOffset;
    private float overheadScale;
    private int overheadMaxWidth;
    private String lastWordsAnchor;
    private int lastWordsOffset;
    private int lastWordsMaxWidth;

    private boolean dirty = false;

    private EditBox hudEdgeDistanceBox;
    private EditBox hudMaxWidthBox;
    private EditBox overheadYOffsetBox;
    private EditBox overheadScaleBox;
    private EditBox overheadMaxWidthBox;
    private EditBox lastWordsOffsetBox;
    private EditBox lastWordsMaxWidthBox;

    public GeneralSettingsScreen(Screen parent) {
        super(Component.literal("General Settings"));

        this.parent = parent;

        ClientDialogueConfig config =
                ClientDialogueConfig.get();

        this.originalDialogueColor =
                config.dialogueColor.id;

        this.originalHudAnchor =
                config.hudAnchor;

        this.originalHudEdgeDistance =
                config.hudEdgeDistance;

        this.originalHudMaxWidth =
                config.hudMaxWidth;

        this.originalOverheadYOffset =
                config.overheadYOffset;

        this.originalOverheadScale =
                config.overheadScale;

        this.originalOverheadMaxWidth =
                config.overheadMaxWidth;

        this.originalLastWordsAnchor =
                config.lastWordsAnchor;

        this.originalLastWordsOffset =
                config.lastWordsOffset;

        this.originalLastWordsMaxWidth =
                config.lastWordsMaxWidth;

        resetWorkingValues();
    }

    private void resetWorkingValues() {
        dialogueColor =
                originalDialogueColor;

        hudAnchor =
                originalHudAnchor;

        hudEdgeDistance =
                originalHudEdgeDistance;

        hudMaxWidth =
                originalHudMaxWidth;

        overheadYOffset =
                originalOverheadYOffset;

        overheadScale =
                originalOverheadScale;

        overheadMaxWidth =
                originalOverheadMaxWidth;

        lastWordsAnchor =
                originalLastWordsAnchor;

        lastWordsOffset =
                originalLastWordsOffset;

        lastWordsMaxWidth =
                originalLastWordsMaxWidth;

        dirty = false;
    }

    @Override
    protected void init() {
        buildWidgets();
    }

    private void buildWidgets() {
        clearWidgets();

        GeneralSettingsList list =
                new GeneralSettingsList(
                        this.minecraft,
                        this.width,
                        this.height,
                        32,
                        this.height - 36
                );

        // -------------------------------------------------
        // Appearance
        // -------------------------------------------------

        list.addSection("Appearance");

        Button colorButton =
                Button.builder(
                        getDialogueColorLabel(),
                        button -> {
                            dialogueColor =
                                    nextDialogueColor(
                                            dialogueColor
                                    );

                            button.setMessage(
                                    getDialogueColorLabel()
                            );

                            updateDirtyState();
                        }
                ).bounds(
                        0,
                        0,
                        140,
                        20
                ).build();

        list.addSetting(
                "Dialogue Color",
                colorButton,
                "Controls the color used for your dialogue text."
        );

        // -------------------------------------------------
        // HUD
        // -------------------------------------------------

        list.addSection("HUD");

        Button hudAnchorButton =
                Button.builder(
                        Component.literal(
                                prettify(hudAnchor)
                        ),
                        button -> {
                            hudAnchor =
                                    "top".equalsIgnoreCase(
                                            hudAnchor
                                    )
                                            ? "bottom"
                                            : "top";

                            button.setMessage(
                                    Component.literal(
                                            prettify(hudAnchor)
                                    )
                            );

                            updateDirtyState();
                        }
                ).bounds(
                        0,
                        0,
                        140,
                        20
                ).build();

        list.addSetting(
                "Anchor",
                hudAnchorButton,
                "Controls whether first-person dialogue appears near the top or bottom of the screen."
        );

        hudEdgeDistanceBox =
                createIntegerBox(
                        hudEdgeDistance,
                        "Edge Distance"
                );

        list.addSetting(
                "Edge Distance",
                hudEdgeDistanceBox,
                "Distance in pixels from the selected screen edge.\nHigher values move dialogue farther from the anchor point."
        );

        hudMaxWidthBox =
                createIntegerBox(
                        hudMaxWidth,
                        "HUD Max Width"
                );

        list.addSetting(
                "Max Width",
                hudMaxWidthBox,
                "Maximum width of HUD dialogue in pixels before it wraps onto another line."
        );

        // -------------------------------------------------
        // Overhead Dialogue
        // -------------------------------------------------

        list.addSection("Overhead Dialogue");

        overheadYOffsetBox =
                createDoubleBox(
                        overheadYOffset,
                        "Y Offset"
                );

        list.addSetting(
                "Y Offset",
                overheadYOffsetBox,
                "Vertical position of dialogue above players.\nHigher values move the text upward."
        );

        overheadScaleBox =
                createFloatBox(
                        overheadScale,
                        "Scale"
                );

        list.addSetting(
                "Scale",
                overheadScaleBox,
                "Controls the size of dialogue above players.\nHigher values make the text larger.\nVery small adjustments recommended."
        );

        overheadMaxWidthBox =
                createIntegerBox(
                        overheadMaxWidth,
                        "Overhead Max Width"
                );

        list.addSetting(
                "Max Width",
                overheadMaxWidthBox,
                "Maximum width of overhead dialogue before it wraps onto another line."
        );

        // -------------------------------------------------
        // Last Words
        // -------------------------------------------------

        list.addSection("Last Words");

        Button lastWordsAnchorButton =
                Button.builder(
                        Component.literal(
                                prettify(lastWordsAnchor)
                        ),
                        button -> {
                            lastWordsAnchor =
                                    nextLastWordsAnchor(
                                            lastWordsAnchor
                                    );

                            button.setMessage(
                                    Component.literal(
                                            prettify(
                                                    lastWordsAnchor
                                            )
                                    )
                            );

                            updateDirtyState();
                        }
                ).bounds(
                        0,
                        0,
                        140,
                        20
                ).build();

        list.addSetting(
                "Anchor",
                lastWordsAnchorButton,
                "Controls whether last words are positioned relative to the top, center, or bottom of the death screen."
        );

        lastWordsOffsetBox =
                createIntegerBox(
                        lastWordsOffset,
                        "Offset"
                );

        list.addSetting(
                "Offset",
                lastWordsOffsetBox,
                "Offsets last words from their selected anchor position.\nPositive is down, negative is up."
        );

        lastWordsMaxWidthBox =
                createIntegerBox(
                        lastWordsMaxWidth,
                        "Last Words Max Width"
                );

        list.addSetting(
                "Max Width",
                lastWordsMaxWidthBox,
                "Maximum width of last words before they wrap onto another line."
        );

        addRenderableWidget(list);

        buildExitButtons();
    }

    private Button doneButton;
    private Button saveButton;
    private Button discardButton;

    private EditBox createIntegerBox(
            int value,
            String hint
    ) {
        EditBox box =
                new EditBox(
                        this.font,
                        0,
                        0,
                        140,
                        20,
                        Component.literal(hint)
                );

        box.setValue(
                Integer.toString(value)
        );

        box.setResponder(text -> {
            try {
                Integer.parseInt(text);

                readWorkingValues();
                updateDirtyState();

            } catch (NumberFormatException ignored) {
            }
        });

        return box;
    }

    private EditBox createDoubleBox(
            double value,
            String hint
    ) {
        EditBox box =
                new EditBox(
                        this.font,
                        0,
                        0,
                        140,
                        20,
                        Component.literal(hint)
                );

        box.setValue(
                Double.toString(value)
        );

        box.setResponder(text -> {
            try {
                Double.parseDouble(text);

                readWorkingValues();
                updateDirtyState();

            } catch (NumberFormatException ignored) {
            }
        });

        return box;
    }

    private EditBox createFloatBox(
            float value,
            String hint
    ) {
        EditBox box =
                new EditBox(
                        this.font,
                        0,
                        0,
                        140,
                        20,
                        Component.literal(hint)
                );

        // Float.toString prevents values such as
        // 0.02500000037252903 from appearing.
        box.setValue(
                Float.toString(value)
        );

        box.setResponder(text -> {
            try {
                Float.parseFloat(text);

                readWorkingValues();
                updateDirtyState();

            } catch (NumberFormatException ignored) {
            }
        });

        return box;
    }

    private void readWorkingValues() {
        hudEdgeDistance =
                parseInt(
                        hudEdgeDistanceBox,
                        hudEdgeDistance
                );

        hudMaxWidth =
                parseInt(
                        hudMaxWidthBox,
                        hudMaxWidth
                );

        overheadYOffset =
                parseDouble(
                        overheadYOffsetBox,
                        overheadYOffset
                );

        overheadScale =
                parseFloat(
                        overheadScaleBox,
                        overheadScale
                );

        overheadMaxWidth =
                parseInt(
                        overheadMaxWidthBox,
                        overheadMaxWidth
                );

        lastWordsOffset =
                parseInt(
                        lastWordsOffsetBox,
                        lastWordsOffset
                );

        lastWordsMaxWidth =
                parseInt(
                        lastWordsMaxWidthBox,
                        lastWordsMaxWidth
                );
    }

    private int parseInt(
            EditBox box,
            int fallback
    ) {
        if (box == null) {
            return fallback;
        }

        try {
            return Integer.parseInt(
                    box.getValue()
            );

        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private double parseDouble(
            EditBox box,
            double fallback
    ) {
        if (box == null) {
            return fallback;
        }

        try {
            return Double.parseDouble(
                    box.getValue()
            );

        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private float parseFloat(
            EditBox box,
            float fallback
    ) {
        if (box == null) {
            return fallback;
        }

        try {
            return Float.parseFloat(
                    box.getValue()
            );

        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private void updateDirtyState() {
        dirty =
                !dialogueColor.equalsIgnoreCase(
                        originalDialogueColor
                )
                || !hudAnchor.equalsIgnoreCase(
                        originalHudAnchor
                )
                || hudEdgeDistance
                        != originalHudEdgeDistance
                || hudMaxWidth
                        != originalHudMaxWidth
                || Double.compare(
                        overheadYOffset,
                        originalOverheadYOffset
                ) != 0
                || Float.compare(
                        overheadScale,
                        originalOverheadScale
                ) != 0
                || overheadMaxWidth
                        != originalOverheadMaxWidth
                || !lastWordsAnchor.equalsIgnoreCase(
                        originalLastWordsAnchor
                )
                || lastWordsOffset
                        != originalLastWordsOffset
                || lastWordsMaxWidth
                        != originalLastWordsMaxWidth;

        updateExitButtons();
    }

    private void buildExitButtons() {
        int y = this.height - 28;

        doneButton =
                Button.builder(
                        Component.literal("Done"),
                        button -> onClose()
                ).bounds(
                        this.width / 2 - 100,
                        y,
                        200,
                        20
                ).build();

        saveButton =
                Button.builder(
                        Component.literal("Save & Exit")
                                .withStyle(ChatFormatting.GREEN),
                        button -> saveAndExit()
                ).bounds(
                        this.width / 2 - 152,
                        y,
                        150,
                        20
                ).build();

        discardButton =
                Button.builder(
                        Component.literal("Discard Changes")
                                .withStyle(ChatFormatting.RED),
                        button -> discardAndExit()
                ).bounds(
                        this.width / 2 + 2,
                        y,
                        150,
                        20
                ).build();

        addRenderableWidget(doneButton);
        addRenderableWidget(saveButton);
        addRenderableWidget(discardButton);

        updateExitButtons();
    }

    private void updateExitButtons() {
        if (doneButton == null
                || saveButton == null
                || discardButton == null) {
            return;
        }

        doneButton.visible = !dirty;
        doneButton.active = !dirty;

        saveButton.visible = dirty;
        saveButton.active = dirty;

        discardButton.visible = dirty;
        discardButton.active = dirty;
    }

    private void saveAndExit() {
        readWorkingValues();
        updateDirtyState();

        boolean saved =
                ClientDialogueConfig.saveGeneralSettings(
                        dialogueColor,
                        hudAnchor,
                        hudEdgeDistance,
                        hudMaxWidth,
                        overheadYOffset,
                        overheadScale,
                        overheadMaxWidth,
                        lastWordsAnchor,
                        lastWordsOffset,
                        lastWordsMaxWidth
                );

        if (saved) {
            Minecraft.getInstance().setScreen(parent);
        }
    }

    private void discardAndExit() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void onClose() {
        if (!dirty) {
            Minecraft.getInstance().setScreen(parent);
            return;
        }

        // Temporary behavior.
        // We'll give this a confirmation screen next.
        discardAndExit();
    }

    private Component getDialogueColorLabel() {
        DialogueColor color =
                DialogueColor.fromString(
                        dialogueColor
                );

        return Component.literal(
                        prettify(dialogueColor)
                )
                .withStyle(color.formatting);
    }

    private static String nextDialogueColor(
            String current
    ) {
        String[] colors = {
                "white",
                "red",
                "gold",
                "yellow",
                "green",
                "aqua",
                "blue",
                "purple"
        };

        for (int i = 0; i < colors.length; i++) {
            if (colors[i].equalsIgnoreCase(current)) {
                return colors[
                        (i + 1) % colors.length
                        ];
            }
        }

        return "white";
    }

    private static String nextLastWordsAnchor(
            String current
    ) {
        if ("center".equalsIgnoreCase(current)) {
            return "top";
        }

        if ("top".equalsIgnoreCase(current)) {
            return "bottom";
        }

        return "center";
    }

    private static String prettify(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return Character.toUpperCase(
                value.charAt(0)
        ) + value.substring(1);
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(graphics);

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        graphics.drawCenteredString(
                this.font,
                this.title,
                this.width / 2,
                14,
                0xFFFFFF
        );
    }
}