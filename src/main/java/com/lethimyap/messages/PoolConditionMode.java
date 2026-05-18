package com.lethimyap.messages;

public enum PoolConditionMode {
    AND,
    OR,
    XOR;

    public static PoolConditionMode fromString(String value) {
        if (value == null || value.isBlank()) {
            return AND;
        }

        for (PoolConditionMode mode : values()) {
            if (mode.name().equalsIgnoreCase(value)) {
                return mode;
            }
        }

        return AND;
    }
}