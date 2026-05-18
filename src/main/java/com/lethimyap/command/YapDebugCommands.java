package com.lethimyap.command;

import java.util.ArrayList;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.lethimyap.LetHimYap;
import com.lethimyap.api.LetHimYapApi;
import com.lethimyap.api.YapPoolRegistry;
import com.lethimyap.messages.PlayerMessageManager;
import com.lethimyap.messages.PoolOverrideConfig;
import com.lethimyap.messages.ServerMessageConfig;
import net.minecraft.network.chat.Component;
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

                        .then(Commands.literal("reload")
                                .requires(source -> source.hasPermission(2))
                                .executes(ctx -> {
                                    ServerMessageConfig.reload();
                                    PoolOverrideConfig.reload();

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("Reloaded Let Him Yap server configs."),
                                            true
                                    );

                                    return 1;
                                })
                        )

                        .then(Commands.literal("pools")
                                .requires(source -> source.hasPermission(2))
                                .executes(ctx -> {
                                    ServerMessageConfig config = ServerMessageConfig.get();

                                    ArrayList<String> ids = new ArrayList<>();

                                    for (ServerMessageConfig.Pool pool : config.pools) {
                                        if (pool != null && pool.id != null && !pool.id.isBlank()) {
                                            ids.add(pool.id);
                                        }
                                    }

                                    for (ServerMessageConfig.Pool pool : YapPoolRegistry.getServerPools()) {
                                        if (pool != null && pool.id != null && !pool.id.isBlank()) {
                                            ids.add(pool.id);
                                        }
                                    }

                                    ids.sort(String::compareTo);

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("Registered pools (" + ids.size() + "):"),
                                            false
                                    );

                                    for (String id : ids) {
                                        ctx.getSource().sendSuccess(
                                                () -> Component.literal("- " + id),
                                                false
                                        );
                                    }

                                    return ids.size();
                                })
                        )

                        .then(Commands.literal("availablepools")
                                .requires(source -> source.hasPermission(2))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    ServerMessageConfig config = ServerMessageConfig.get();

                                    var pools = PlayerMessageManager.getBestAvailablePools(player, config);

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("Available pools for " + player.getGameProfile().getName() + " (" + pools.size() + "):"),
                                            false
                                    );

                                    for (PoolOverrideConfig.PoolView view : pools) {
                                        ctx.getSource().sendSuccess(
                                                () -> Component.literal(
                                                        "- " + view.pool.id
                                                                + " | group=" + view.pool.group
                                                                + " | tier=" + view.tier
                                                                + " | weight=" + view.weight
                                                                + " | important=" + view.important
                                                ),
                                                false
                                        );
                                    }

                                    return pools.size();
                                })
                        )
        );
    }
}