package com.lethimyap.command;

import com.lethimyap.LetHimYap;
import com.lethimyap.api.LetHimYapApi;
import com.lethimyap.messages.PlayerMessageManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LetHimYap.MODID)
public class YapDebugCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("yap-debug")
                        .requires(source -> source.hasPermission(2))

                        .then(Commands.literal("trigger")
                                .executes(ctx -> {
                                    ServerPlayer player =
                                            ctx.getSource().getPlayerOrException();

                                    PlayerMessageManager.handleClientDialogueRequest(player);

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal(
                                                    "Triggered normal dialogue selection."
                                            ),
                                            false
                                    );

                                    return 1;
                                })
                        )

                        .then(Commands.literal("trigger_player")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> {
                                            ServerPlayer target =
                                                    EntityArgument.getPlayer(ctx, "target");

                                            PlayerMessageManager.handleClientDialogueRequest(target);

                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal(
                                                            "Triggered normal dialogue for "
                                                                    + target.getName().getString()
                                                    ),
                                                    false
                                            );

                                            return 1;
                                        })
                                )
                        )

                        .then(Commands.literal("force")
                                .then(Commands.argument("poolId", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            ServerPlayer player =
                                                    ctx.getSource().getPlayerOrException();

                                            String poolId =
                                                    StringArgumentType.getString(ctx, "poolId");

                                            LetHimYapApi.forceDialoguePool(
                                                    player,
                                                    poolId,
                                                    true,
                                                    false
                                            );

                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal(
                                                            "Forced dialogue pool: " + poolId
                                                    ),
                                                    false
                                            );

                                            return 1;
                                        })
                                )
                        )

                        .then(Commands.literal("force_player")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .then(Commands.argument("poolId", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    ServerPlayer target =
                                                            EntityArgument.getPlayer(ctx, "target");

                                                    String poolId =
                                                            StringArgumentType.getString(ctx, "poolId");

                                                    LetHimYapApi.forceDialoguePool(
                                                            target,
                                                            poolId,
                                                            true,
                                                            false
                                                    );

                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.literal(
                                                                    "Forced "
                                                                            + target.getName().getString()
                                                                            + " to use dialogue pool: "
                                                                            + poolId
                                                            ),
                                                            false
                                                    );

                                                    return 1;
                                                })
                                        )
                                )
                        )

                        .then(Commands.literal("force_pain")
                                .then(Commands.argument("poolId", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            ServerPlayer player =
                                                    ctx.getSource().getPlayerOrException();

                                            String poolId =
                                                    StringArgumentType.getString(ctx, "poolId");

                                            LetHimYapApi.forceDialoguePool(
                                                    player,
                                                    poolId,
                                                    false,
                                                    true
                                            );

                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal(
                                                            "Forced pain dialogue pool: " + poolId
                                                    ),
                                                    false
                                            );

                                            return 1;
                                        })
                                )
                        )

                        .then(Commands.literal("say")
                                .then(Commands.argument("message", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            ServerPlayer player =
                                                    ctx.getSource().getPlayerOrException();

                                            String message =
                                                    StringArgumentType.getString(ctx, "message");

                                            LetHimYapApi.forceSpeech(
                                                    player,
                                                    message,
                                                    true,
                                                    false
                                            );

                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal(
                                                            "Forced speech: " + message
                                                    ),
                                                    false
                                            );

                                            return 1;
                                        })
                                )
                        )
        );
    }
}