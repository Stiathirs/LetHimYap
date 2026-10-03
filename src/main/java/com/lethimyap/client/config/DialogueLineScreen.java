package com.lethimyap.client.config;

import java.util.ArrayList;
import java.util.List;

import com.lethimyap.client.ClientServerDialogueConfig;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DialogueLineScreen extends YapConfigScreen {

    private final Screen parent;

    private final String group;
    private final String pool;

    private final boolean serverSpecific;

    private final List<String> originalMessages;
    private final List<String> messages;

    private boolean dirty = false;

    public DialogueLineScreen(
            Screen parent,
            String group,
            String pool
    ) {
        this(
                parent,
                group,
                pool,
                false
        );
    }

    public DialogueLineScreen(
            Screen parent,
            String group,
            String pool,
            boolean serverSpecific
    ) {
        super(
                Component.literal(
                        DialogueGroupScreen.prettify(pool)
                )
        );

        this.parent = parent;
        this.group = group;
        this.pool = pool;
        this.serverSpecific = serverSpecific;

        this.originalMessages = new ArrayList<>(
                loadMessages()
        );

        this.messages = new ArrayList<>(
                originalMessages
        );
    }

    private List<String> loadMessages() {
        if (serverSpecific) {
            return ClientServerDialogueConfig.getMessages(
                    getFullPoolId()
            );
        }

        return DialogueConfigBrowser.getMessages(
                group,
                pool
        );
    }

    private String getFullPoolId() {
        return group + "." + pool;
    }

    private void updateDirtyState() {
        dirty = !messages.equals(originalMessages);
    }

    @Override
    protected void init() {
        buildEditorWidgets();
    }

    private void buildEditorWidgets() {
        clearWidgets();

        DialogueLineList list =
                new DialogueLineList(
                        this.minecraft,
                        this.width,
                        this.height,
                        42,
                        this.height - 60,
                        24,
                        messages,
                        this::editLine,
                        this::deleteLine
                );

        addRenderableWidget(list);

        addRenderableWidget(
                Button.builder(
                        Component.literal("+ Add Line"),
                        button -> addLine()
                ).bounds(
                        this.width / 2 - 152,
                        this.height - 52,
                        150,
                        20
                ).build()
        );

        List<String> defaults = getDefaultMessages();

        Button restoreButton =
                Button.builder(
                        Component.literal("Restore Defaults")
                                .withStyle(ChatFormatting.YELLOW),
                        button -> restoreDefaults()
                ).bounds(
                        this.width / 2 + 2,
                        this.height - 52,
                        150,
                        20
                ).build();

        restoreButton.active = !defaults.isEmpty();

        addRenderableWidget(restoreButton);

        if (dirty) {
            addRenderableWidget(
                    Button.builder(
                            Component.literal("Save & Exit")
                                    .withStyle(ChatFormatting.GREEN),
                            button -> saveAndExit()
                    ).bounds(
                            this.width / 2 - 102,
                            this.height - 28,
                            100,
                            20
                    ).build()
            );

            addRenderableWidget(
                    Button.builder(
                            Component.literal("Discard Changes")
                                    .withStyle(ChatFormatting.RED),
                            button -> discardAndExit()
                    ).bounds(
                            this.width / 2 + 2,
                            this.height - 28,
                            100,
                            20
                    ).build()
            );
        } else {
            addRenderableWidget(
                    Button.builder(
                            Component.literal("Done"),
                            button -> onClose()
                    ).bounds(
                            this.width / 2 - 100,
                            this.height - 28,
                            200,
                            20
                    ).build()
            );
        }
    }

    private void saveAndExit() {
        if (!dirty) {
            Minecraft.getInstance().setScreen(parent);
            return;
        }

        saveAndExitFromConfirmation();
    }

    private void editLine(int index) {
        if (index < 0 || index >= messages.size()) {
            return;
        }

        Minecraft.getInstance().setScreen(
                new DialogueLineEditScreen(
                        this,
                        messages.get(index),
                        updated -> {
                            messages.set(index, updated);
                            updateDirtyState();
                            buildEditorWidgets();
                        }
                )
        );
    }

    private void addLine() {
        Minecraft.getInstance().setScreen(
                new DialogueLineEditScreen(
                        this,
                        "",
                        added -> {
                            if (!added.isBlank()) {
                                messages.add(added);
                                updateDirtyState();
                                buildEditorWidgets();
                            }
                        }
                )
        );
    }

    private void restoreDefaults() {
        List<String> defaults = getDefaultMessages();

        if (defaults.isEmpty()) {
            return;
        }

        messages.clear();
        messages.addAll(defaults);

        updateDirtyState();
        buildEditorWidgets();
    }

    private List<String> getDefaultMessages() {
        if (serverSpecific) {
            return ClientServerDialogueConfig.getCurrentServerDefaultLines(
                    getFullPoolId()
            );
        }

        return DialogueConfigBrowser.getDefaultMessages(
                group,
                pool
        );
    }

    private void deleteLine(int index) {
        if (index < 0 || index >= messages.size()) {
            return;
        }

        messages.remove(index);
        updateDirtyState();
        buildEditorWidgets();
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
                14,
                0xFFFFFF
        );

        graphics.drawCenteredString(
                this.font,
                Component.literal(
                        group + "." + pool
                ),
                this.width / 2,
                26,
                0x808080
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
        if (!dirty) {
            Minecraft.getInstance().setScreen(parent);
            return;
        }

        Minecraft.getInstance().setScreen(
                new UnsavedDialogueChangesScreen(this)
        );
    }

    void saveAndExitFromConfirmation() {
        boolean saved = saveMessages();

        if (saved) {
            Minecraft.getInstance().setScreen(parent);
        } else {
            /*
            * Return to the editor with the working copy intact.
            * We'll add an actual visible error message shortly.
            */
            Minecraft.getInstance().setScreen(this);
        }
    }

    private boolean saveMessages() {
        if (serverSpecific) {
            return ClientServerDialogueConfig.saveMessages(
                    getFullPoolId(),
                    messages
            );
        }

        return DialogueConfigBrowser.saveMessages(
                group,
                pool,
                messages
        );
    }

    void discardAndExit() {
        Minecraft.getInstance().setScreen(parent);
    }
}