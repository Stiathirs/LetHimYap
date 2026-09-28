package com.lethimyap.client.config;

import com.lethimyap.api.YapVoice;
import com.lethimyap.api.YapVoiceRegistry;
import com.lethimyap.messages.ClientDialogueConfig;
import com.lethimyap.network.ModNetwork;
import com.lethimyap.network.SyncVoiceSelectionPacket;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;


public class VoiceConfigScreen extends YapConfigScreen {

    private final Screen parent;

    private String originalVoice;
    private String selectedVoice;

    private Button voiceButton;
    private Button doneButton;
    private Button saveButton;
    private Button discardButton;

    private final VoicePreviewController preview = new VoicePreviewController();

    public VoiceConfigScreen(Screen parent) {
        super(Component.literal("Voice"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (originalVoice == null) {
            originalVoice =
                    ClientDialogueConfig.get().voice;

            selectedVoice =
                    originalVoice;
        }

        int centerX = width / 2;
        int startY = height / 2 - 45;

        voiceButton =
                addRenderableWidget(
                        Button.builder(
                                getVoiceName(),
                                button -> minecraft.setScreen(
                                        new VoiceSelectionScreen(
                                                this,
                                                selectedVoice,
                                                this::selectVoice
                                        )
                                )
                        ).bounds(
                                centerX - 100,
                                startY,
                                200,
                                20
                        ).build()
                );

        addRenderableWidget(
                Button.builder(
                        Component.literal("▶ Preview"),
                        button -> previewVoice()
                ).bounds(
                        centerX - 100,
                        startY + 28,
                        200,
                        20
                ).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("Restore Default"),
                        button -> selectVoice(
                                YapVoiceRegistry.DEFAULT_VOICE_ID.toString()
                        )
                ).bounds(
                        centerX - 100,
                        startY + 56,
                        200,
                        20
                ).build()
        );

        doneButton =
                addRenderableWidget(
                        Button.builder(
                                Component.literal("Done"),
                                button -> onClose()
                        ).bounds(
                                centerX - 100,
                                height - 28,
                                200,
                                20
                        ).build()
                );

        saveButton =
                addRenderableWidget(
                        Button.builder(
                                Component.literal("Save & Exit"),
                                button -> saveAndExit()
                        ).bounds(
                                centerX - 100,
                                height - 28,
                                99,
                                20
                        ).build()
                );

        discardButton =
                addRenderableWidget(
                        Button.builder(
                                Component.literal("Discard Changes"),
                                button -> discardAndExit()
                        ).bounds(
                                centerX + 1,
                                height - 28,
                                99,
                                20
                        ).build()
                );

        updateButtons();
    }

    private void selectVoice(String voiceId) {
        selectedVoice = voiceId;
        preview.stop();

        voiceButton.setMessage(getVoiceName());
        updateButtons();
    }

    private Component getVoiceName() {
        YapVoice voice =
                YapVoiceRegistry.get(selectedVoice);

        if (voice != null) {
            return Component.literal(
                    voice.displayName()
            );
        }

        return Component.literal(
                "Unavailable (" + selectedVoice + ")"
        ).withStyle(
                ChatFormatting.YELLOW
        );
    }

    private boolean isDirty() {
        return !selectedVoice.equals(
                originalVoice
        );
    }

    private void updateButtons() {
        boolean dirty = isDirty();

        doneButton.visible = !dirty;
        saveButton.visible = dirty;
        discardButton.visible = dirty;
    }

    private void saveAndExit() {
        if (!ClientDialogueConfig.setVoice(selectedVoice)) return;

        if (minecraft.getConnection() != null) {
            ModNetwork.CHANNEL.sendToServer(
                    new SyncVoiceSelectionPacket(selectedVoice)
            );
        }

        originalVoice = selectedVoice;
        minecraft.setScreen(parent);
    }

    private void discardAndExit() {
        minecraft.setScreen(parent);
    }

    private void previewVoice() {
        preview.start(selectedVoice);
    }

    @Override
    public void tick() {
        super.tick();
        preview.tick();
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
                font,
                title,
                width / 2,
                24,
                0xFFFFFF
        );

        graphics.drawCenteredString(
                font,
                Component.literal("Selected Voice"),
                width / 2,
                height / 2 - 61,
                0xA0A0A0
        );

        if (!preview.getVisibleText().isEmpty()) {
            graphics.drawCenteredString(
                    font,
                    Component.literal(preview.getVisibleText()),
                    width / 2,
                    height / 2 + 44,
                    0xFFFFFF
            );
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}