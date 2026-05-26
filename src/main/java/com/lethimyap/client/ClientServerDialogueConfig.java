package com.lethimyap.client;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ClientServerDialogueConfig {
    private static final Random RANDOM = new Random();

    public static void mergeRuntimeLines(Map<String, List<String>> lines) {
        if (lines == null || lines.isEmpty()) return;

        Path file = getCurrentServerConfigFile();

        try {
            Files.createDirectories(file.getParent());

            try (CommentedFileConfig toml = CommentedFileConfig.builder(file)
                    .sync()
                    .autosave()
                    .preserveInsertionOrder()
                    .build()) {

                toml.load();

                for (Map.Entry<String, List<String>> entry : lines.entrySet()) {
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
}