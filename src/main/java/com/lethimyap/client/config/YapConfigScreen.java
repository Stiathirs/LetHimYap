package com.lethimyap.client.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class YapConfigScreen extends Screen {

    protected YapConfigScreen(Component title) {
        super(title);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}