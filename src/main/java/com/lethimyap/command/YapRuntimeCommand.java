package com.lethimyap.command;

import com.lethimyap.LetHimYap;
import com.lethimyap.messages.PoolCondition;
import com.lethimyap.messages.PoolConditionMode;
import com.lethimyap.messages.RuntimePoolRegistry;
import com.lethimyap.messages.ServerMessageConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LetHimYap.MODID)
public class YapRuntimeCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("yap-runtime")
                        .requires(source -> source.hasPermission(2))

                        .then(Commands.literal("clear")
                                .executes(ctx -> {
                                    RuntimePoolRegistry.clear();

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("Cleared Let Him Yap runtime pools."),
                                            true
                                    );

                                    return 1;
                                })
                        )

                        .then(Commands.literal("pool")
                                .then(Commands.literal("always")
                                        .then(Commands.argument("id", StringArgumentType.string())
                                                .then(Commands.argument("group", StringArgumentType.string())
                                                        .then(Commands.argument("tier", IntegerArgumentType.integer())
                                                                .then(Commands.argument("weight", IntegerArgumentType.integer())
                                                                        .then(Commands.argument("important", BoolArgumentType.bool())
                                                                                .then(Commands.argument("forcedOnly", BoolArgumentType.bool())
                                                                                        .executes(ctx -> {
                                                                                            String id = StringArgumentType.getString(ctx, "id");
                                                                                            String group = StringArgumentType.getString(ctx, "group");
                                                                                            int tier = IntegerArgumentType.getInteger(ctx, "tier");
                                                                                            int weight = IntegerArgumentType.getInteger(ctx, "weight");
                                                                                            boolean important = BoolArgumentType.getBool(ctx, "important");
                                                                                            boolean forcedOnly = BoolArgumentType.getBool(ctx, "forcedOnly");

                                                                                            RuntimePoolRegistry.registerPool(
                                                                                                    ServerMessageConfig.Pool.always(
                                                                                                            id,
                                                                                                            group,
                                                                                                            tier,
                                                                                                            weight,
                                                                                                            important,
                                                                                                            forcedOnly
                                                                                                    )
                                                                                            );

                                                                                            ctx.getSource().sendSuccess(
                                                                                                    () -> Component.literal("Registered runtime always pool: " + id),
                                                                                                    true
                                                                                            );

                                                                                            return 1;
                                                                                        })
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )

                                .then(Commands.literal("nbt_number")
                                        .then(Commands.argument("id", StringArgumentType.string())
                                                .then(Commands.argument("group", StringArgumentType.string())
                                                        .then(Commands.argument("tier", IntegerArgumentType.integer())
                                                                .then(Commands.argument("weight", IntegerArgumentType.integer())
                                                                        .then(Commands.argument("important", BoolArgumentType.bool())
                                                                                .then(Commands.argument("forcedOnly", BoolArgumentType.bool())
                                                                                        .then(Commands.argument("nbtPath", StringArgumentType.string())
                                                                                                .then(Commands.argument("compare", StringArgumentType.string())
                                                                                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                                                                                                .executes(ctx -> {
                                                                                                                    String id = StringArgumentType.getString(ctx, "id");
                                                                                                                    String group = StringArgumentType.getString(ctx, "group");
                                                                                                                    int tier = IntegerArgumentType.getInteger(ctx, "tier");
                                                                                                                    int weight = IntegerArgumentType.getInteger(ctx, "weight");
                                                                                                                    boolean important = BoolArgumentType.getBool(ctx, "important");
                                                                                                                    boolean forcedOnly = BoolArgumentType.getBool(ctx, "forcedOnly");
                                                                                                                    String nbtPath = StringArgumentType.getString(ctx, "nbtPath");
                                                                                                                    String compare = StringArgumentType.getString(ctx, "compare");
                                                                                                                    double value = DoubleArgumentType.getDouble(ctx, "value");

                                                                                                                    RuntimePoolRegistry.registerPool(
                                                                                                                            ServerMessageConfig.Pool.custom(
                                                                                                                                    id,
                                                                                                                                    group,
                                                                                                                                    tier,
                                                                                                                                    weight,
                                                                                                                                    important,
                                                                                                                                    forcedOnly,
                                                                                                                                    PoolConditionMode.AND,
                                                                                                                                    PoolCondition.nbtNumber(
                                                                                                                                            nbtPath,
                                                                                                                                            compare,
                                                                                                                                            value
                                                                                                                                    )
                                                                                                                            )
                                                                                                                    );

                                                                                                                    ctx.getSource().sendSuccess(
                                                                                                                            () -> Component.literal("Registered runtime NBT pool: " + id),
                                                                                                                            true
                                                                                                                    );

                                                                                                                    return 1;
                                                                                                                })
                                                                                                        )
                                                                                                )
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )

                        .then(Commands.literal("line")
                                .then(Commands.literal("add")
                                        .then(Commands.argument("pool", StringArgumentType.string())
                                                .then(Commands.argument("line", StringArgumentType.greedyString())
                                                        .executes(ctx -> {
                                                            String pool = StringArgumentType.getString(ctx, "pool");
                                                            String line = StringArgumentType.getString(ctx, "line");

                                                            RuntimePoolRegistry.addClientLine(pool, line);

                                                            ctx.getSource().sendSuccess(
                                                                    () -> Component.literal("Added runtime default line for pool: " + pool),
                                                                    true
                                                            );

                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                        )
        );
    }
}