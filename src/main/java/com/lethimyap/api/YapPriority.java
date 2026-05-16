package com.lethimyap.api;

public enum YapPriority {
    NORMAL(0),
    EVENT(1),
    PAIN(2),
    CRITICAL(3);

    public final int level;

    YapPriority(int level) {
        this.level = level;
    }

    public static YapPriority fromString(String value) {
        if (value == null) return NORMAL;

        for (YapPriority priority : values()) {
            if (priority.name().equalsIgnoreCase(value)) {
                return priority;
            }
        }

        return NORMAL;
    }
}