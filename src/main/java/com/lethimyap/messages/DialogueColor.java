package com.lethimyap.messages;

import net.minecraft.ChatFormatting;

public enum DialogueColor {
    WHITE("white", ChatFormatting.WHITE),
    RED("red", ChatFormatting.RED),
    GOLD("gold", ChatFormatting.GOLD),
    YELLOW("yellow", ChatFormatting.YELLOW),
    GREEN("green", ChatFormatting.GREEN),
    AQUA("aqua", ChatFormatting.AQUA),
    BLUE("blue", ChatFormatting.BLUE),
    PURPLE("purple", ChatFormatting.LIGHT_PURPLE);

    public final String id;
    public final ChatFormatting formatting;

    DialogueColor(String id, ChatFormatting formatting) {
        this.id = id;
        this.formatting = formatting;
    }

    public static DialogueColor fromString(String value) {
        if (value == null) return WHITE;

        for (DialogueColor color : values()) {
            if (color.id.equalsIgnoreCase(value)) {
                return color;
            }
        }

        return WHITE;
    }
}