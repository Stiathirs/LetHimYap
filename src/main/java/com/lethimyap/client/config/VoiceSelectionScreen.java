package com.lethimyap.client.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.lethimyap.api.YapVoice;
import com.lethimyap.api.YapVoiceRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;

public class VoiceSelectionScreen extends YapConfigScreen {
    private final Screen parent;
    private final Consumer<String> onSelect;

    private String hoveredDescription;

    private EditBox searchBox;
    private VoiceList voiceList;
    private String searchText = "";

    private final VoicePreviewController preview = new VoicePreviewController();

    public VoiceSelectionScreen(
            Screen parent,
            Consumer<String> onSelect
    ) {
        super(Component.literal("Select Voice"));

        this.parent = parent;
        this.onSelect = onSelect;
    }

    @Override
    protected void init() {
        searchBox = new EditBox(font, width / 2 - 145, 30, 290, 20, Component.literal("Search voices"));
        searchBox.setBordered(true);
        searchBox.setHint(ConfigSearch.voiceHint());
        searchBox.setValue(searchText);
        searchBox.setFormatter(ConfigSearch::formatVoiceEditBox);

        voiceList = new VoiceList(
                minecraft,
                width,
                height,
                54,
                height - 36,
                24,
                YapVoiceRegistry.getVoices(),
                this
        );

        searchBox.setResponder(value -> {
            searchText = value;
            voiceList.setSearch(value);
        });

        addRenderableWidget(voiceList);
        addRenderableWidget(searchBox);

        addRenderableWidget(
                Button.builder(Component.literal("Cancel"), button -> onClose())
                        .bounds(width / 2 - 100, height - 28, 200, 20)
                        .build()
        );
    }

    @Override
    public void tick() {
        super.tick();
        preview.tick();
    }

    private void preview(YapVoice voice) {
        preview.start(
                voice.id().toString()
        );
    }

    private void choose(YapVoice voice) {
        preview.stop();

        onSelect.accept(
                voice.id().toString()
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
        hoveredDescription = null;

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
                16,
                0xFFFFFF
        );

        if (hoveredDescription != null
                && !hoveredDescription.isBlank()) {

            graphics.renderTooltip(
                    font,
                    font.split(
                            Component.literal(
                                    hoveredDescription
                            ),
                            240
                    ),
                    mouseX,
                    mouseY
            );
        }
    }

    @Override
    public void onClose() {
        preview.stop();
        minecraft.setScreen(parent);
    }

    private static class VoiceList
            extends ContainerObjectSelectionList<VoiceList.Entry> {

        private final List<YapVoice> voices;
        private final VoiceSelectionScreen screen;

        public VoiceList(
                Minecraft minecraft,
                int width,
                int height,
                int top,
                int bottom,
                int itemHeight,
                List<YapVoice> voices,
                VoiceSelectionScreen screen
        ) {
            super(minecraft, width, height, top, bottom, itemHeight);

            this.voices = new ArrayList<>(voices);
            this.screen = screen;

            rebuild("");
        }

        private void setSearch(String search) {
            rebuild(search);
        }

        private void rebuild(String search) {
            clearEntries();

            Map<String, List<YapVoice>> groupedVoices = new LinkedHashMap<>();

            ConfigSearch.Query query = ConfigSearch.parse(search);
            String explicitModId = null;

            if (query.hasModFilter()) {
                for (YapVoice voice : voices) {
                    String namespace = voice.id().getNamespace();

                    if (ConfigSearch.matchesModId(search, namespace)) {
                        explicitModId = namespace;
                        break;
                    }
                }
            }

            for (YapVoice voice : voices) {
                String namespace = voice.id().getNamespace();
                String modName = getModName(namespace);
                String searchableText = voice.displayName() + " " + voice.description();

                if (explicitModId != null && !namespace.equalsIgnoreCase(explicitModId)) continue;
                if (!ConfigSearch.matches(search, searchableText, namespace, modName)) continue;

                groupedVoices.computeIfAbsent(namespace, ignored -> new ArrayList<>()).add(voice);
            }

            List<YapVoice> defaultVoices = groupedVoices.remove("lethimyap");

            if (defaultVoices != null) {
                addEntry(new HeaderEntry(getModName("lethimyap"), screen));

                for (YapVoice voice : defaultVoices)
                    addEntry(new VoiceEntry(voice, screen));
            }

            List<Map.Entry<String, List<YapVoice>>> sortedGroups = new ArrayList<>(groupedVoices.entrySet());

            sortedGroups.sort((a, b) -> {
                int nameCompare = getModName(a.getKey()).compareToIgnoreCase(getModName(b.getKey()));

                if (nameCompare != 0) return nameCompare;

                return a.getKey().compareToIgnoreCase(b.getKey());
            });

            for (Map.Entry<String, List<YapVoice>> group : sortedGroups) {
                addEntry(new HeaderEntry(getModName(group.getKey()), screen));

                for (YapVoice voice : group.getValue())
                    addEntry(new VoiceEntry(voice, screen));
            }
        }

        private static String getModName(String namespace) {
            return ModList.get().getModContainerById(namespace)
                    .map(container -> container.getModInfo().getDisplayName())
                    .orElse(namespace);
        }

        private abstract static class Entry
                extends ContainerObjectSelectionList.Entry<Entry> {
        }

        private static class HeaderEntry extends Entry {

            private final String name;
            private final VoiceSelectionScreen screen;

            private HeaderEntry(
                    String name,
                    VoiceSelectionScreen screen
            ) {
                this.name = name;
                this.screen = screen;
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
                graphics.drawCenteredString(
                        screen.font,
                        name,
                        left + width / 2,
                        top + 7,
                        0xA0A0A0
                );
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

        private static class VoiceEntry extends Entry {

            private final YapVoice voice;
            private final VoiceSelectionScreen screen;

            private final Button selectButton;
            private final Button previewButton;

            private VoiceEntry(
                    YapVoice voice,
                    VoiceSelectionScreen screen
            ) {
                this.voice = voice;
                this.screen = screen;

                selectButton =
                        Button.builder(
                                Component.literal(voice.displayName()),
                                clicked -> screen.choose(voice)
                        ).bounds(
                                0,
                                0,
                                174,
                                20
                        ).build();

                previewButton =
                        Button.builder(
                                Component.literal("▶"),
                                clicked -> screen.preview(voice)
                        ).bounds(
                                0,
                                0,
                                20,
                                20
                        ).build();
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
                int totalWidth = 200;
                int x = left + (width - totalWidth) / 2;

                selectButton.setX(x);
                selectButton.setY(top);

                previewButton.setX(x + 180);
                previewButton.setY(top);

                selectButton.render(graphics, mouseX, mouseY, partialTick);
                previewButton.render(graphics, mouseX, mouseY, partialTick);

                if (selectButton.isMouseOver(mouseX, mouseY)) {
                    screen.hoveredDescription = voice.description();
                } else if (previewButton.isMouseOver(mouseX, mouseY)) {
                    screen.hoveredDescription = "Preview";
                }
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return List.of(
                        selectButton,
                        previewButton
                );
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of(
                        selectButton,
                        previewButton
                );
            }
        }
    }
}