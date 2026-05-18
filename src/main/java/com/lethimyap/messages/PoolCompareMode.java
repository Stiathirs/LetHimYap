package com.lethimyap.messages;

public enum PoolCompareMode {
    ABOVE,
    ABOVE_OR_EQUAL,
    BELOW,
    BELOW_OR_EQUAL,
    EQUAL,
    NOT_EQUAL;

    public static PoolCompareMode fromString(String value) {
        if (value == null || value.isBlank()) {
            return BELOW_OR_EQUAL;
        }

        return switch (value.toLowerCase()) {
            case "above" -> ABOVE;
            case "above_or_equal", "aboveorequal", "at_least", ">=" -> ABOVE_OR_EQUAL;
            case "below" -> BELOW;
            case "below_or_equal", "beloworequal", "at_most", "<=" -> BELOW_OR_EQUAL;
            case "equal", "equals", "==" -> EQUAL;
            case "not_equal", "notequal", "!=" -> NOT_EQUAL;
            default -> BELOW_OR_EQUAL;
        };
    }

    public boolean compare(double found, double target) {
        return switch (this) {
            case ABOVE -> found > target;
            case ABOVE_OR_EQUAL -> found >= target;
            case BELOW -> found < target;
            case BELOW_OR_EQUAL -> found <= target;
            case EQUAL -> found == target;
            case NOT_EQUAL -> found != target;
        };
    }

    public String id() {
        return switch (this) {
            case ABOVE -> "above";
            case ABOVE_OR_EQUAL -> "above_or_equal";
            case BELOW -> "below";
            case BELOW_OR_EQUAL -> "below_or_equal";
            case EQUAL -> "equal";
            case NOT_EQUAL -> "not_equal";
        };
    }
}