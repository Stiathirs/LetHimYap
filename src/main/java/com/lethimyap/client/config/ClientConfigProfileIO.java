package com.lethimyap.client.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lethimyap.messages.ClientDialogueConfig;
import com.lethimyap.api.YapPoolRegistry;

import java.util.ArrayList;
import java.util.List;

public final class ClientConfigProfileIO {

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create();

    private ClientConfigProfileIO() {
    }

    public static String exportToJson() {
        return GSON.toJson(createSnapshot());
    }

    public static ImportResult parseImport(String json) {
        if (json == null || json.isBlank()) {
            return ImportResult.failure(
                    "The clipboard is empty."
            );
        }

        try {
            JsonElement rootElement =
                    GSON.fromJson(json, JsonElement.class);

            if (rootElement == null
                    || !rootElement.isJsonObject()) {
                return ImportResult.failure(
                        "The profile must be a JSON object."
                );
            }

            JsonObject root =
                    rootElement.getAsJsonObject();

            String structureError =
                    validateRootStructure(root);

            if (structureError != null) {
                return ImportResult.failure(
                        structureError
                );
            }

            ClientConfigProfile profile =
                    GSON.fromJson(
                            root,
                            ClientConfigProfile.class
                    );

            String valueError =
                    validateValues(profile);

            if (valueError != null) {
                return ImportResult.failure(
                        valueError
                );
            }

            return ImportResult.success(profile);

        } catch (JsonParseException
                | IllegalStateException
                | UnsupportedOperationException e) {

            return ImportResult.failure(
                    "The clipboard does not contain valid profile JSON."
            );
        }
    }

    private static String validateRootStructure(
            JsonObject root
    ) {
        JsonElement format =
                root.get("format");

        if (format == null
                || !format.isJsonPrimitive()
                || !format.getAsJsonPrimitive()
                        .isString()) {
            return "The profile is missing a valid format.";
        }

        if (!"lethimyap-client-profile".equals(
                format.getAsString()
        )) {
            return "This is not a Let Him Yap client profile.";
        }

        JsonElement version =
                root.get("version");

        if (version == null
                || !version.isJsonPrimitive()
                || !version.getAsJsonPrimitive()
                        .isNumber()) {
            return "The profile is missing a valid version.";
        }

        int versionNumber;

        try {
            versionNumber =
                    version.getAsInt();
        } catch (NumberFormatException e) {
            return "The profile version is invalid.";
        }

        if (versionNumber != 1) {
            return "Unsupported client profile version: "
                    + versionNumber;
        }

        JsonElement settings =
                root.get("settings");

        if (settings != null
                && !settings.isJsonObject()) {
            return "\"settings\" must be a JSON object.";
        }

        JsonElement pools =
                root.get("pools");

        if (pools != null
                && !pools.isJsonObject()) {
            return "\"pools\" must be a JSON object.";
        }

        if (pools != null) {
            String poolError =
                    validatePoolStructure(
                            pools.getAsJsonObject()
                    );

            if (poolError != null) {
                return poolError;
            }
        }

        return null;
    }

    private static String validatePoolStructure(
            JsonObject pools
    ) {
        for (var entry : pools.entrySet()) {
            String poolId =
                    entry.getKey();

            JsonElement value =
                    entry.getValue();

            if (poolId == null
                    || poolId.isBlank()) {
                return "A dialogue pool has an empty ID.";
            }

            if (value == null
                    || !value.isJsonArray()) {
                return "Dialogue pool \""
                        + poolId
                        + "\" must contain an array of lines.";
            }

            JsonArray lines =
                    value.getAsJsonArray();

            for (JsonElement line : lines) {
                if (line == null
                        || !line.isJsonPrimitive()
                        || !line.getAsJsonPrimitive()
                                .isString()) {
                    return "Dialogue pool \""
                            + poolId
                            + "\" contains a non-text line.";
                }
            }
        }

        return null;
    }

    private static String validateValues(
            ClientConfigProfile profile
    ) {
        if (profile == null) {
            return "The profile could not be read.";
        }

        ClientConfigProfile.Settings settings =
                profile.settings;

        if (settings == null) {
            settings =
                    new ClientConfigProfile.Settings();

            profile.settings =
                    settings;
        }

        if (profile.pools == null) {
            profile.pools =
                    new java.util.LinkedHashMap<>();
        }

        if (settings.dialogueColor != null) {
            String color =
                    settings.dialogueColor
                            .toLowerCase();

            if (!color.equals("white")
                    && !color.equals("red")
                    && !color.equals("gold")
                    && !color.equals("yellow")
                    && !color.equals("green")
                    && !color.equals("aqua")
                    && !color.equals("blue")
                    && !color.equals("purple")) {

                return "Unknown dialogue color: "
                        + settings.dialogueColor;
            }
        }

        if (settings.hudAnchor != null
                && !settings.hudAnchor.equalsIgnoreCase("top")
                && !settings.hudAnchor.equalsIgnoreCase("bottom")) {

            return "HUD anchor must be \"top\" or \"bottom\".";
        }

        if (settings.lastWordsAnchor != null
                && !settings.lastWordsAnchor.equalsIgnoreCase("center")
                && !settings.lastWordsAnchor.equalsIgnoreCase("top")
                && !settings.lastWordsAnchor.equalsIgnoreCase("bottom")) {

            return "Last Words anchor must be \"center\", \"top\", or \"bottom\".";
        }

        if (settings.overheadYOffset != null
                && !Double.isFinite(
                        settings.overheadYOffset
                )) {
            return "Overhead Y Offset must be a finite number.";
        }

        if (settings.overheadScale != null
                && !Float.isFinite(
                        settings.overheadScale
                )) {
            return "Overhead Scale must be a finite number.";
        }

        return null;
    }

    private static ClientConfigProfile createSnapshot() {
        ClientDialogueConfig config =
                ClientDialogueConfig.get();

        ClientConfigProfile profile =
                new ClientConfigProfile();

        writeChangedSettings(profile, config);
        writeChangedPools(profile);

        return profile;
    }

    private static void writeChangedSettings(
            ClientConfigProfile profile,
            ClientDialogueConfig config
    ) {
        ClientConfigProfile.Settings settings =
                profile.settings;

        /*
         * Only settings that differ from Let Him Yap's defaults are
         * written into the profile.
         */

        if (config.dialogueColor != ClientDialogueConfig.DEFAULT_DIALOGUE_COLOR) settings.dialogueColor = config.dialogueColor.name().toLowerCase();
        if (!ClientDialogueConfig.DEFAULT_VOICE.equals(config.voice)) settings.voice = config.voice;

        if (!config.hudAnchor.equals(ClientDialogueConfig.DEFAULT_HUD_ANCHOR)) settings.hudAnchor = config.hudAnchor;
        if (config.hudEdgeDistance != ClientDialogueConfig.DEFAULT_HUD_EDGE_DISTANCE) settings.hudEdgeDistance = config.hudEdgeDistance;
        if (config.hudMaxWidth != ClientDialogueConfig.DEFAULT_HUD_MAX_WIDTH) settings.hudMaxWidth = config.hudMaxWidth;

        if (Double.compare(config.overheadYOffset, ClientDialogueConfig.DEFAULT_OVERHEAD_Y_OFFSET) != 0) settings.overheadYOffset = config.overheadYOffset;
        if (Float.compare(config.overheadScale, ClientDialogueConfig.DEFAULT_OVERHEAD_SCALE) != 0) settings.overheadScale = config.overheadScale;
        if (config.overheadMaxWidth != ClientDialogueConfig.DEFAULT_OVERHEAD_MAX_WIDTH) settings.overheadMaxWidth = config.overheadMaxWidth;

        if (!config.lastWordsAnchor.equals(ClientDialogueConfig.DEFAULT_LAST_WORDS_ANCHOR)) settings.lastWordsAnchor = config.lastWordsAnchor;
        if (config.lastWordsOffset != ClientDialogueConfig.DEFAULT_LAST_WORDS_OFFSET) settings.lastWordsOffset = config.lastWordsOffset;
        if (config.lastWordsMaxWidth != ClientDialogueConfig.DEFAULT_LAST_WORDS_MAX_WIDTH) settings.lastWordsMaxWidth = config.lastWordsMaxWidth;
    }

    private static void writeChangedPools(
            ClientConfigProfile profile
    ) {
        for (String group :
                DialogueConfigBrowser.getGroups()) {

            for (String pool :
                    DialogueConfigBrowser.getPools(group)) {

                String poolId = group + "." + pool;

                List<String> currentLines = DialogueConfigBrowser.getMessages(group, pool);

                /*
                * Check Let Him Yap's built-in defaults first.
                */
                List<String> defaultLines = ClientDialogueConfig.getBuiltInDefaultLines(poolId);

                /*
                * An empty result means this isn't a built-in pool.
                * Check addon-registered defaults next.
                */
                if (defaultLines.isEmpty()) defaultLines = YapPoolRegistry.getClientDefaultLines(poolId);

                /*
                * If we found an authoritative default, only export
                * the pool when the user has actually changed it.
                */
                if (defaultLines != null && !defaultLines.isEmpty()) {

                    if (!currentLines.equals(defaultLines)) {
                        profile.pools.put(poolId, List.copyOf(currentLines));
                    }

                    continue;
                }

                /*
                * No current default exists for this pool.
                *
                * Preserve it because it may belong to an addon that
                * isn't currently installed.
                */
                profile.pools.put(poolId, List.copyOf(currentLines));
            }
        }
    }

    public record ImportResult(
            ClientConfigProfile profile,
            String error
    ) {

        public boolean isValid() {
            return profile != null && error == null;
        }

        public static ImportResult success(
                ClientConfigProfile profile
        ) {
            return new ImportResult(profile, null);
        }

        public static ImportResult failure(
                String error
        ) {
            return new ImportResult(null, error);
        }
    }
    public static List<String> findUnknownPools(
            ClientConfigProfile profile
    ) {
        List<String> unknownPools = new ArrayList<>();

        if (profile == null || profile.pools == null || profile.pools.isEmpty()) return unknownPools;

        for (String poolId : profile.pools.keySet()) {
            if (!isKnownPool(poolId)) unknownPools.add(poolId);
        }

        return unknownPools;
    }

    private static boolean isKnownPool(
            String poolId
    ) {
        /*
        * Let Him Yap built-in pool.
        */
        List<String> builtInDefaults = ClientDialogueConfig.getBuiltInDefaultLines(poolId);

        if (!builtInDefaults.isEmpty()) return true;

        /*
        * Currently registered addon pool.
        */
        List<String> addonDefaults = YapPoolRegistry.getClientDefaultLines(poolId);

        return addonDefaults != null && !addonDefaults.isEmpty();
    }

    public static ClientConfigProfile copyProfile(
            ClientConfigProfile source
    ) {
        ClientConfigProfile copy =
                new ClientConfigProfile();

        copy.format = source.format;

        copy.version = source.version;

        copy.settings.dialogueColor = source.settings.dialogueColor;

        copy.settings.voice = source.settings.voice;

        copy.settings.hudAnchor = source.settings.hudAnchor;
        copy.settings.hudEdgeDistance = source.settings.hudEdgeDistance;
        copy.settings.hudMaxWidth = source.settings.hudMaxWidth;
        
        copy.settings.overheadYOffset = source.settings.overheadYOffset;
        copy.settings.overheadScale = source.settings.overheadScale;
        copy.settings.overheadMaxWidth = source.settings.overheadMaxWidth;

        copy.settings.lastWordsAnchor = source.settings.lastWordsAnchor;
        copy.settings.lastWordsOffset = source.settings.lastWordsOffset;
        copy.settings.lastWordsMaxWidth = source.settings.lastWordsMaxWidth;

        for (var entry : source.pools.entrySet()) {
            copy.pools.put(
                    entry.getKey(),
                    List.copyOf(entry.getValue())
            );
        }

        return copy;
    }
}