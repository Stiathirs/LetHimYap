package com.lethimyap.messages;

import java.util.HashSet;
import java.util.Set;

public class DefaultPoolDisabler {
    private static final Set<String> DISABLED_GROUPS = new HashSet<>();

    public static void disableGroup(String group) {
        if (group == null || group.isBlank()) return;
        DISABLED_GROUPS.add(group);
    }

    public static boolean isGroupDisabled(String group) {
        return group != null && DISABLED_GROUPS.contains(group);
    }

    public static void disableHealthPools() {
        disableGroup("health");
    }

    public static void disableHungerPools() {
        disableGroup("hunger");
    }

    public static void disableAirPools() {
        disableGroup("air");
    }
}