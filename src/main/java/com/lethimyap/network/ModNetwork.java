package com.lethimyap.network;

import com.lethimyap.LetHimYap;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(LetHimYap.MODID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private static int id = 0;

    public static void register() {
        CHANNEL.registerMessage(
                id++,
                SyncLastWordsPacket.class,
                SyncLastWordsPacket::encode,
                SyncLastWordsPacket::decode,
                SyncLastWordsPacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                SyncFloatingTextPacket.class,
                SyncFloatingTextPacket::encode,
                SyncFloatingTextPacket::decode,
                SyncFloatingTextPacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                SyncSpeechHudPacket.class,
                SyncSpeechHudPacket::encode,
                SyncSpeechHudPacket::decode,
                SyncSpeechHudPacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                RequestDialoguePacket.class,
                RequestDialoguePacket::encode,
                RequestDialoguePacket::decode,
                RequestDialoguePacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                OfferDialoguePoolPacket.class,
                OfferDialoguePoolPacket::encode,
                OfferDialoguePoolPacket::decode,
                OfferDialoguePoolPacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                SelectDialoguePacket.class,
                SelectDialoguePacket::encode,
                SelectDialoguePacket::decode,
                SelectDialoguePacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                SyncDialogueTimingPacket.class,
                SyncDialogueTimingPacket::encode,
                SyncDialogueTimingPacket::decode,
                SyncDialogueTimingPacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                CustomSpeechPacket.class,
                CustomSpeechPacket::encode,
                CustomSpeechPacket::decode,
                CustomSpeechPacket::handle
        );
    }
}