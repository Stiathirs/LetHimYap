package com.lethimyap.client;

import com.lethimyap.LetHimYap;
import com.lethimyap.messages.ClientDialogueConfig;
import com.lethimyap.network.CustomSpeechPacket;
import com.lethimyap.network.ModNetwork;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@Mod.EventBusSubscriber(
        modid = LetHimYap.MODID,
        value = Dist.CLIENT
)
public class ClientYapCommand {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                literal("yap")
                        .then(argument("message", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String message = StringArgumentType.getString(ctx, "message");

                                    ClientDialogueConfig config = ClientDialogueConfig.get();

                                    ModNetwork.CHANNEL.sendToServer(
                                            new CustomSpeechPacket(
                                                    message,
                                                    config.dialogueColor.id
                                            )
                                    );

                                    return 1;
                                })
                        )
        );
    }
}