package com.lethimyap.client;

import com.lethimyap.LetHimYap;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

@Mod.EventBusSubscriber(
        modid = LetHimYap.MODID,
        value = Dist.CLIENT
)
public class ClientConfigCommand {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher =
                event.getDispatcher();

        dispatcher.register(
                Commands.literal("yap-config")
                        .then(Commands.literal("client")
                                .executes(ctx -> {
                                    openClientConfig();
                                    return 1;
                                })
                        )
                        .then(Commands.literal("server")
                                .executes(ctx -> {
                                    openServerConfig();
                                    return 1;
                                })
                        )
        );
    }

    private static void openClientConfig() {
        Path file = FMLPaths.CONFIGDIR.get()
                .resolve("lethimyap")
                .resolve("client_dialogue.toml");

        sendOpenFileMessage(
                "Let Him Yap client dialogue config:",
                file
        );
    }

    private static void openServerConfig() {
        Minecraft minecraft = Minecraft.getInstance();

        String serverLabel = getServerLabel(minecraft);

        Path folder = FMLPaths.CONFIGDIR.get()
                .resolve("lethimyap")
                .resolve("multiplayer")
                .resolve(makeServerFolderName(serverLabel));

        Path file = folder.resolve("client_dialogue.toml");

        try {
            Files.createDirectories(folder);
            if (!Files.exists(file)) {
            Files.writeString(
                    file,
                    """
                    # Let Him Yap server-specific dialogue config
                    #
                    # This file stores dialogue lines for runtime pools provided by this server.
                    # It is safe to edit.
                    #
                    # Runtime server pools will be added here when the server sends them.
                    
                    """
            );
        }
        } catch (Exception e) {
            e.printStackTrace();
        }

        sendOpenFileMessage(
                "Let Him Yap server dialogue config:",
                file
        );
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

    private static void sendOpenFileMessage(String title, Path file) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) return;

        Component link = Component.literal("[Open File]")
                .withStyle(Style.EMPTY
                        .withColor(ChatFormatting.AQUA)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(
                                ClickEvent.Action.OPEN_FILE,
                                file.toAbsolutePath().toString()
                        ))
                );

        minecraft.player.sendSystemMessage(Component.literal(title));
        minecraft.player.sendSystemMessage(link);
        minecraft.player.sendSystemMessage(
                Component.literal(file.toAbsolutePath().toString())
                        .withStyle(ChatFormatting.GRAY)
        );
    }
}