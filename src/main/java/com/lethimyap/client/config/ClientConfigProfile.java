package com.lethimyap.client.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Portable representation of the user's Let Him Yap client configuration.
 *
 * Settings and dialogue pools are stored as overrides from Let Him Yap's
 * defaults. Missing values therefore mean "use the default", not
 * "preserve the currently configured value".
 */
public class ClientConfigProfile {

    public String format = "lethimyap-client-profile";
    public int version = 1;

    public Settings settings = new Settings();

    /**
     * Dialogue pools whose lines differ from their registered defaults.
     *
     * Keys are complete pool IDs, such as:
     *
     * health.light
     * event.wake_up
     * someaddon.custom_pool
     */
    public Map<String, List<String>> pools =
            new LinkedHashMap<>();

    public static class Settings {

        /*
         * These intentionally use nullable wrapper types.
         *
         * null means that the profile does not override that setting and
         * the Let Him Yap default should be used during import.
         */

        public String dialogueColor;
        public String voice;

        public String hudAnchor;
        public Integer hudEdgeDistance;
        public Integer hudMaxWidth;

        public Double overheadYOffset;
        public Float overheadScale;
        public Integer overheadMaxWidth;

        public String lastWordsAnchor;
        public Integer lastWordsOffset;
        public Integer lastWordsMaxWidth;
    }
}