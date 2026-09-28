package com.lethimyap.client;

import com.lethimyap.LetHimYap;
import com.lethimyap.client.config.ClientConfigScreen;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
                        .executes(ctx -> {
                            openConfig();
                            return 1;
                        })
        );
    }

    private static void openConfig() {
        Minecraft minecraft =
                Minecraft.getInstance();

        minecraft.setScreen(
                new ClientConfigScreen(null)
        );
    }
}