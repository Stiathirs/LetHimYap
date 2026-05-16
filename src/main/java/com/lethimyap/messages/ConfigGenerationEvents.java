package com.lethimyap.messages;

import com.lethimyap.LetHimYap;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Legit the only reason this file exists is because I was writing the default config WHILE ADDONS WERE STILL ADDING STUFF
// Dawg. Just let them add their pools first, THEN write the config.

@Mod.EventBusSubscriber(modid = LetHimYap.MODID)
public class ConfigGenerationEvents {

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        PoolOverrideConfig.generateOrUpdate();
    }
}