package com.lethimyap.client;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ClientServerDialogueConfig {
    private static final Random RANDOM = new Random();

    /*
    * The dialogue defaults supplied by the currently connected server.
    *
    * This is intentionally separate from client_dialogue.toml:
    *
    * CURRENT_SERVER_DEFAULTS = what the server says currently exists
    * client_dialogue.toml    = the player's persistent customizations
    */
    private static final Map<String, List<String>> CURRENT_SERVER_DEFAULTS =
            new LinkedHashMap<>();

    public static void mergeRuntimeLines(Map<String, List<String>> lines) {
        CURRENT_SERVER_DEFAULTS.clear();

        if (lines == null || lines.isEmpty()) return;

        for (Map.Entry<String, List<String>> entry : lines.entrySet()) {
            String poolId = entry.getKey();
            List<String> poolLines = entry.getValue();

            if (poolId == null || poolId.isBlank() || poolLines == null || poolLines.isEmpty()) continue;

            CURRENT_SERVER_DEFAULTS.put(
                    poolId,
                    List.copyOf(poolLines)
            );
        }

        if (CURRENT_SERVER_DEFAULTS.isEmpty()) {
            return;
        }

        Path file = getCurrentServerConfigFile();

        try {
            Files.createDirectories(file.getParent());

            try (CommentedFileConfig toml = CommentedFileConfig.builder(file)
                    .sync()
                    .autosave()
                    .preserveInsertionOrder()
                    .build()) {

                toml.load();

                for (Map.Entry<String, List<String>> entry : CURRENT_SERVER_DEFAULTS.entrySet()) {
                    String poolId = entry.getKey();
                    List<String> poolLines = entry.getValue();

                    if (poolId == null || poolId.isBlank()) continue;
                    if (poolLines == null || poolLines.isEmpty()) continue;

                    String path = "pools." + poolId + ".messages";

                    if (!toml.contains(path)) {
                        toml.set(path, poolLines);
                    }
                }

                toml.save();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String pickMessage(String poolId) {
        if (poolId == null || poolId.isBlank()) return "";

        Path file = getCurrentServerConfigFile();

        if (!Files.exists(file)) return "";

        try (CommentedFileConfig toml = CommentedFileConfig.builder(file)
                .sync()
                .build()) {

            toml.load();

            Object value = toml.get("pools." + poolId + ".messages");

            if (!(value instanceof List<?> list) || list.isEmpty()) {
                return "";
            }

            Object picked = list.get(RANDOM.nextInt(list.size()));

            return picked instanceof String s ? s : "";

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static Path getCurrentServerConfigFile() {
        return getCurrentServerConfigFolder()
                .resolve("client_dialogue.toml");
    }

    public static Path getCurrentServerConfigFolder() {
        Minecraft minecraft = Minecraft.getInstance();

        String serverLabel = getServerLabel(minecraft);

        return FMLPaths.CONFIGDIR.get()
                .resolve("lethimyap")
                .resolve("multiplayer")
                .resolve(makeServerFolderName(serverLabel));
    }

    private static String getServerLabel(Minecraft minecraft) {
        if (minecraft.getCurrentServer() != null
                && minecraft.getCurrentServer().ip != null
                && !minecraft.getCurrentServer().ip.isBlank()) {
            return minecraft.getCurrentServer().ip;
        }

        if (minecraft.getSingleplayerServer() != null) {
            return "singleplayer_"
                    + minecraft.getSingleplayerServer()
                    .getWorldData()
                    .getLevelName();
        }

        return "unknown_server";
    }

    private static String makeServerFolderName(String serverLabel) {
        String clean = serverLabel
                .toLowerCase()
                .replaceAll("[^a-z0-9._-]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+|_+$", "");

        if (clean.isBlank()) {
            clean = "unknown_server";
        }

        return clean + "_" + shortHash(serverLabel);
    }

    private static String shortHash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");

            byte[] bytes = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < 4; i++) {
                builder.append(String.format("%02x", bytes[i]));
            }

            return builder.toString();

        } catch (Exception e) {
            return "00000000";
        }
    }

    public static List<String> getCurrentServerGroups() {
        if (CURRENT_SERVER_DEFAULTS.isEmpty()) {
            return Collections.emptyList();
        }

        ArrayList<String> groups = new ArrayList<>();

        for (String poolId : CURRENT_SERVER_DEFAULTS.keySet()) {
            int separator = poolId.indexOf('.');

            if (separator <= 0) {
                continue;
            }

            String group = poolId.substring(0, separator);

            if (!groups.contains(group)) {
                groups.add(group);
            }
        }

        return groups;
    }

    public static List<String> getCurrentServerPools(
            String group
    ) {
        if (group == null || group.isBlank()) {
            return Collections.emptyList();
        }

        ArrayList<String> pools = new ArrayList<>();

        String prefix = group + ".";

        for (String poolId : CURRENT_SERVER_DEFAULTS.keySet()) {
            if (!poolId.startsWith(prefix)) {
                continue;
            }

            String pool =
                    poolId.substring(prefix.length());

            if (!pool.isBlank()) {
                pools.add(pool);
            }
        }

        return pools;
    }

    public static List<String> getCurrentServerDefaultLines(
            String poolId
    ) {
        if (poolId == null || poolId.isBlank()) {
            return Collections.emptyList();
        }

        List<String> defaults =
                CURRENT_SERVER_DEFAULTS.get(poolId);

        if (defaults == null) {
            return Collections.emptyList();
        }

        return List.copyOf(defaults);
    }

    public static List<String> getMessages(
            String poolId
    ) {
        if (poolId == null || poolId.isBlank()) {
            return Collections.emptyList();
        }

        Path file = getCurrentServerConfigFile();

        if (!Files.isRegularFile(file)) {
            return Collections.emptyList();
        }

        try (CommentedFileConfig toml =
                    CommentedFileConfig.builder(file)
                            .sync()
                            .build()) {

            toml.load();

            Object value =
                    toml.get(
                            "pools."
                                    + poolId
                                    + ".messages"
                    );

            if (!(value instanceof List<?> list)) {
                return Collections.emptyList();
            }

            ArrayList<String> messages =
                    new ArrayList<>();

            for (Object item : list) {
                if (item instanceof String message) {
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
            String poolId,
            List<String> messages
    ) {
        if (poolId == null || poolId.isBlank()
                || messages == null) {
            return false;
        }

        /*
        * Only allow editing pools actually supplied by the
        * currently connected server.
        */
        if (!CURRENT_SERVER_DEFAULTS.containsKey(poolId)) {
            return false;
        }

        Path file = getCurrentServerConfigFile();

        try {
            Files.createDirectories(file.getParent());

            try (CommentedFileConfig toml =
                        CommentedFileConfig.builder(file)
                                .sync()
                                .preserveInsertionOrder()
                                .build()) {

                toml.load();

                toml.set(
                        "pools."
                                + poolId
                                + ".messages",
                        new ArrayList<>(messages)
                );

                toml.save();
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}