package com.lethimyap.client.config;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

public class DialogueNavigationList
        extends ObjectSelectionList<DialogueNavigationList.Entry> {

    public DialogueNavigationList(
            Minecraft minecraft,
            int width,
            int height,
            int top,
            int bottom,
            int itemHeight,
            List<String> ids,
            Consumer<String> onSelected
    ) {
        this(
                minecraft,
                width,
                height,
                top,
                bottom,
                itemHeight,
                ids,
                onSelected,
                DialogueGroupScreen::prettify
        );
    }

    public DialogueNavigationList(
            Minecraft minecraft,
            int width,
            int height,
            int top,
            int bottom,
            int itemHeight,
            List<String> ids,
            Consumer<String> onSelected,
            Function<String, String> displayName
    ) {
        super(
                minecraft,
                width,
                height,
                top,
                bottom,
                itemHeight
        );

        for (String id : ids) {
            addEntry(
                    new Entry(
                            id,
                            onSelected,
                            displayName
                    )
            );
        }
    }

    @Override
    public int getRowWidth() {
        return 200;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.width / 2 + 110;
    }

    public class Entry extends ObjectSelectionList.Entry<Entry> {
        private final Button button;

        private Entry(
                String id,
                Consumer<String> onSelected,
                Function<String, String> displayName
        ) {
            this.button = Button.builder(
                    Component.literal(
                            displayName.apply(id)
                    ),
                    ignored -> onSelected.accept(id)
            ).bounds(0, 0, 200, 20)
            .build();
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
            int buttonLeft =
                    DialogueNavigationList.this.width / 2 - 100;

            button.setX(buttonLeft);
            button.setY(top);

            button.render(
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
            return button.mouseClicked(
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
            return button.mouseReleased(
                    mouseX,
                    mouseY,
                    mouseButton
            );
        }

        @Override
        public Component getNarration() {
            return button.getMessage();
        }
    }
}