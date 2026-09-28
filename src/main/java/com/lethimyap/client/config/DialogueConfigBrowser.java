package com.lethimyap.client.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.lethimyap.api.YapPoolRegistry;
import com.lethimyap.messages.ClientDialogueConfig;

import net.minecraftforge.fml.ModList;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DialogueConfigBrowser {

    private static final Path CLIENT_DIALOGUE_FILE =
            Path.of("config", "lethimyap", "client_dialogue.toml");

    private DialogueConfigBrowser() {
    }

    public static List<String> getGroups() {
        ensureConfigExists();

        if (!Files.isRegularFile(CLIENT_DIALOGUE_FILE)) {
            return Collections.emptyList();
        }

        try (CommentedFileConfig config =
                    CommentedFileConfig.builder(CLIENT_DIALOGUE_FILE)
                            .sync()
                            .preserveInsertionOrder()
                            .build()) {

            config.load();

            Object poolsObject = config.get("pools");

            if (!(poolsObject instanceof Config pools)) {
                return Collections.emptyList();
            }

            List<String> groups = new ArrayList<>();

            collectGroups(
                    pools,
                    "",
                    groups
            );

            return groups;

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private static void collectGroups(
            Config config,
            String path,
            List<String> groups
    ) {
        boolean containsPools = false;

        for (String key : config.valueMap().keySet()) {
            Object value = config.get(key);

            if (!(value instanceof Config child)) {
                continue;
            }

            if (child.contains("messages")) {
                containsPools = true;
                continue;
            }

            String childPath =
                    path.isEmpty()
                            ? key
                            : path + "." + key;

            collectGroups(
                    child,
                    childPath,
                    groups
            );
        }

        if (containsPools && !path.isEmpty()) {
            groups.add(path);
        }
    }

    public static List<String> getPools(String group) {
        if (group == null || group.isBlank()) {
            return Collections.emptyList();
        }

        ensureConfigExists();

        if (!Files.isRegularFile(CLIENT_DIALOGUE_FILE)) {
            return Collections.emptyList();
        }

        try (CommentedFileConfig config =
                     CommentedFileConfig.builder(CLIENT_DIALOGUE_FILE)
                             .sync()
                             .preserveInsertionOrder()
                             .build()) {

            config.load();

            Object groupObject =
                    config.get("pools." + group);

            if (!(groupObject instanceof Config groupConfig)) {
                return Collections.emptyList();
            }

            List<String> pools = new ArrayList<>();

            for (String key : groupConfig.valueMap().keySet()) {
                Object value = groupConfig.get(key);

                if (!(value instanceof Config poolConfig)) {
                    continue;
                }

                if (poolConfig.contains("messages")) {
                    pools.add(key);
                }
            }

            return pools;

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static String getGroupDisplayName(String group) {
        if (group == null || group.isBlank()) {
            return "";
        }

        int separator = group.indexOf(':');

        /*
        * Built-in/non-namespaced group.
        */
        if (separator < 0) {
            return prettifyName(group);
        }

        String namespace = group.substring(0, separator);
        String groupName = group.substring(separator + 1);

        String addonName = ModList.get()
                .getModContainerById(namespace)
                .map(container ->
                        container.getModInfo().getDisplayName())
                .orElseGet(() -> prettifyName(namespace));

        return prettifyName(groupName)
                + " ("
                + addonName
                + ")";
    }

    private static String prettifyName(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String normalized =
                value.replace('_', ' ')
                        .replace('-', ' ');

        StringBuilder result =
                new StringBuilder(normalized.length());

        boolean capitalizeNext = true;

        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);

            if (Character.isWhitespace(c)) {
                result.append(c);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(
                        Character.toUpperCase(c)
                );
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    public static List<String> getMessages(
            String group,
            String pool
    ) {
        if (group == null || group.isBlank()
                || pool == null || pool.isBlank()) {
            return Collections.emptyList();
        }

        ensureConfigExists();

        if (!Files.isRegularFile(CLIENT_DIALOGUE_FILE)) {
            return Collections.emptyList();
        }

        try (CommentedFileConfig config =
                    CommentedFileConfig.builder(CLIENT_DIALOGUE_FILE)
                            .sync()
                            .preserveInsertionOrder()
                            .build()) {

            config.load();

            Object messagesObject =
                    config.get("pools." + group + "." + pool + ".messages");

            if (!(messagesObject instanceof List<?> list)) {
                return Collections.emptyList();
            }

            List<String> messages = new ArrayList<>();

            for (Object value : list) {
                if (value instanceof String message) {
                    messages.add(message);
                }
            }

            return messages;

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static boolean saveMessages(
            String group,
            String pool,
            List<String> messages
    ) {
        if (group == null || group.isBlank()
                || pool == null || pool.isBlank()
                || messages == null) {
            return false;
        }

        ensureConfigExists();

        if (!Files.isRegularFile(CLIENT_DIALOGUE_FILE)) {
            return false;
        }

        try (CommentedFileConfig config =
                    CommentedFileConfig.builder(CLIENT_DIALOGUE_FILE)
                            .sync()
                            .preserveInsertionOrder()
                            .build()) {

            config.load();

            String path =
                    "pools."
                            + group
                            + "."
                            + pool
                            + ".messages";

            /*
            * Copy the working list rather than handing NightConfig the mutable
            * list owned by the screen.
            *
            * An empty list is valid and intentionally disables dialogue for
            * this pool.
            */
            config.set(
                    path,
                    new ArrayList<>(messages)
            );

            config.save();

            /*
            * Refresh the runtime representation only after the file was
            * successfully written.
            */
            ClientDialogueConfig.reload();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void ensureConfigExists() {
        if (Files.isRegularFile(CLIENT_DIALOGUE_FILE)) {
            return;
        }

        /*
         * ClientDialogueConfig owns creation/default population of this file.
         * Initializing/reloading it here avoids maintaining a second set of
         * defaults inside the config GUI.
         */
        ClientDialogueConfig.reload();
    }

    public static List<String> getDefaultMessages(
            String group,
            String pool
    ) {
        if (group == null || group.isBlank()
                || pool == null || pool.isBlank()) {
            return Collections.emptyList();
        }

        String poolId =
                group + "." + pool;

        List<String> defaults =
                new ArrayList<>();

        /*
        * LHY's own built-in defaults.
        */
        defaults.addAll(
                ClientDialogueConfig.getBuiltInDefaultLines(
                        poolId
                )
        );

        /*
        * Defaults contributed through the public addon API.
        */
        defaults.addAll(
                YapPoolRegistry.getClientDefaultLines(
                        poolId
                )
        );

        return defaults;
    }
}