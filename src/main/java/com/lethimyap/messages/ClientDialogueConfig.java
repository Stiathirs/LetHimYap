package com.lethimyap.messages;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.lethimyap.api.YapPoolRegistry;
import com.lethimyap.client.config.ClientConfigProfile;
import com.lethimyap.client.config.DialogueConfigBrowser;
import com.lethimyap.network.ModNetwork;
import com.lethimyap.network.SyncVoiceSelectionPacket;

import net.minecraft.client.Minecraft;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

public class ClientDialogueConfig {

    private static final Random RANDOM = new Random();
    private static ClientDialogueConfig INSTANCE;

    public static final DialogueColor DEFAULT_DIALOGUE_COLOR = DialogueColor.WHITE;

    public static final String DEFAULT_HUD_ANCHOR = "bottom";
    public static final int DEFAULT_HUD_EDGE_DISTANCE = 68;
    public static final int DEFAULT_HUD_MAX_WIDTH = 320;

    public static final double DEFAULT_OVERHEAD_Y_OFFSET = 0.75D;
    public static final float DEFAULT_OVERHEAD_SCALE = 0.025F;
    public static final int DEFAULT_OVERHEAD_MAX_WIDTH = 180;

    public static final String DEFAULT_LAST_WORDS_ANCHOR = "center";
    public static final int DEFAULT_LAST_WORDS_OFFSET = -20;
    public static final int DEFAULT_LAST_WORDS_MAX_WIDTH = 300;

    public static final String DEFAULT_VOICE = "lethimyap:click";

    public String hudAnchor = DEFAULT_HUD_ANCHOR;
    public int hudEdgeDistance = DEFAULT_HUD_EDGE_DISTANCE;
    public String lastWordsAnchor = DEFAULT_LAST_WORDS_ANCHOR;
    public int lastWordsOffset = DEFAULT_LAST_WORDS_OFFSET;
    public int hudMaxWidth = DEFAULT_HUD_MAX_WIDTH;
    public int overheadMaxWidth = DEFAULT_OVERHEAD_MAX_WIDTH;
    public int lastWordsMaxWidth = DEFAULT_LAST_WORDS_MAX_WIDTH;
    public double overheadYOffset = DEFAULT_OVERHEAD_Y_OFFSET;
    public float overheadScale = DEFAULT_OVERHEAD_SCALE;

    public String voice = DEFAULT_VOICE;

    public DialogueColor dialogueColor = DialogueColor.WHITE;

    private CommentedFileConfig toml;

    private static long LAST_MODIFIED = 0L;

    public static ClientDialogueConfig get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }

        return INSTANCE;
    }

    public static boolean saveGeneralSettings(
            String dialogueColor,
            String hudAnchor,
            int hudEdgeDistance,
            int hudMaxWidth,
            double overheadYOffset,
            float overheadScale,
            int overheadMaxWidth,
            String lastWordsAnchor,
            int lastWordsOffset,
            int lastWordsMaxWidth
    ) {
        try {
            ClientDialogueConfig config = get();

            if (config.toml == null) {
                return false;
            }

            config.toml.set("client.dialogueColor", dialogueColor);

            config.toml.set("rendering.hudAnchor", hudAnchor);
            config.toml.set("rendering.hudEdgeDistance", hudEdgeDistance);
            config.toml.set("rendering.hudMaxWidth", hudMaxWidth);

            config.toml.set("rendering.overheadYOffset", overheadYOffset);
            config.toml.set("rendering.overheadScale", overheadScale);
            config.toml.set("rendering.overheadMaxWidth", overheadMaxWidth);

            config.toml.set("lastWords.anchor", lastWordsAnchor);
            config.toml.set("lastWords.offset", lastWordsOffset);
            config.toml.set("lastWords.maxWidth", lastWordsMaxWidth);

            config.toml.save();

            /*
            * Update the live config immediately.
            *
            * Don't reload the file here. We already have the authoritative
            * values and the existing CommentedFileConfig remains valid.
            */
            config.dialogueColor = DialogueColor.fromString(dialogueColor);

            config.hudAnchor = hudAnchor;
            config.hudEdgeDistance = hudEdgeDistance;
            config.hudMaxWidth = hudMaxWidth;
            
            config.overheadYOffset = overheadYOffset;
            config.overheadScale = overheadScale;
            config.overheadMaxWidth = overheadMaxWidth;
            
            config.lastWordsAnchor = lastWordsAnchor;
            config.lastWordsOffset = lastWordsOffset;
            config.lastWordsMaxWidth = lastWordsMaxWidth;

            Path file = Path.of("config", "lethimyap", "client_dialogue.toml");

            LAST_MODIFIED = getLastModified(file);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void reload() {
        if (INSTANCE != null
                && INSTANCE.toml != null) {
            try {
                INSTANCE.toml.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        INSTANCE = load();
    }

    public static boolean applyProfile(ClientConfigProfile profile) {
        if (profile == null) {
            return false;
        }

        try {
            ClientDialogueConfig config = get();

            if (config.toml == null) {
                return false;
            }

            ClientConfigProfile.Settings settings =
                    profile.settings != null
                            ? profile.settings
                            : new ClientConfigProfile.Settings();

            /*
            * Build the imported settings from current defaults.
            * A missing field means "use the current default".
            */
            String dialogueColor = settings.dialogueColor != null ? settings.dialogueColor : DEFAULT_DIALOGUE_COLOR.name().toLowerCase();
            String importedVoice = profile.settings != null && profile.settings.voice != null && !profile.settings.voice.isBlank() ? profile.settings.voice : DEFAULT_VOICE;

            String hudAnchor = settings.hudAnchor != null ? settings.hudAnchor : DEFAULT_HUD_ANCHOR;
            int hudEdgeDistance = settings.hudEdgeDistance != null ? settings.hudEdgeDistance : DEFAULT_HUD_EDGE_DISTANCE;
            int hudMaxWidth = settings.hudMaxWidth != null ? settings.hudMaxWidth : DEFAULT_HUD_MAX_WIDTH;

            double overheadYOffset = settings.overheadYOffset != null ? settings.overheadYOffset : DEFAULT_OVERHEAD_Y_OFFSET;
            float overheadScale = settings.overheadScale != null ? settings.overheadScale : DEFAULT_OVERHEAD_SCALE;
            int overheadMaxWidth = settings.overheadMaxWidth != null ? settings.overheadMaxWidth : DEFAULT_OVERHEAD_MAX_WIDTH;

            String lastWordsAnchor = settings.lastWordsAnchor != null ? settings.lastWordsAnchor : DEFAULT_LAST_WORDS_ANCHOR;
            int lastWordsOffset = settings.lastWordsOffset != null ? settings.lastWordsOffset : DEFAULT_LAST_WORDS_OFFSET;
            int lastWordsMaxWidth = settings.lastWordsMaxWidth != null ? settings.lastWordsMaxWidth : DEFAULT_LAST_WORDS_MAX_WIDTH;

            /*
            * Apply general settings.
            */
            config.toml.set("client.dialogueColor", dialogueColor);
            config.toml.set("client.voice", importedVoice);

            config.toml.set("rendering.hudAnchor", hudAnchor);
            config.toml.set("rendering.hudEdgeDistance", hudEdgeDistance);
            config.toml.set("rendering.hudMaxWidth", hudMaxWidth);

            config.toml.set("rendering.overheadYOffset", overheadYOffset);
            config.toml.set("rendering.overheadScale", overheadScale);
            config.toml.set("rendering.overheadMaxWidth", overheadMaxWidth);

            config.toml.set("lastWords.anchor", lastWordsAnchor);
            config.toml.set("lastWords.offset", lastWordsOffset);
            config.toml.set("lastWords.maxWidth", lastWordsMaxWidth);

            /*
            * Reset every currently known dialogue pool to its
            * authoritative default.
            *
            * DialogueConfigBrowser gives us everything currently
            * represented in the client dialogue config. For each pool,
            * check built-in defaults first, then addon defaults.
            */
            for (String group : DialogueConfigBrowser.getGroups()) {
                for (String pool : DialogueConfigBrowser.getPools(group)) {
                    String poolId = group + "." + pool;
                    String path = "pools." + poolId + ".messages";

                    List<String> builtInDefaults = getBuiltInDefaultLines(poolId);

                    if (!builtInDefaults.isEmpty()) {
                        config.toml.set(path, List.copyOf(builtInDefaults));
                        continue;
                    }

                    List<String> addonDefaults = YapPoolRegistry.getClientDefaultLines(poolId);

                    if (addonDefaults != null && !addonDefaults.isEmpty()) {
                        config.toml.set(path, List.copyOf(addonDefaults));
                    }
                }
            }

            /*
            * Overlay the sparse profile after resetting known pools.
            *
            * An explicitly empty list is intentional and must remain
            * different from an omitted pool.
            */
            if (profile.pools != null) {
                for (var entry : profile.pools.entrySet()) {
                    config.toml.set(
                            "pools." + entry.getKey() + ".messages",
                            List.copyOf(entry.getValue())
                    );
                }
            }

            config.toml.save();

            /*
            * Reload so all live consumers immediately see the
            * imported configuration.
            */
            reload();

            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.getConnection() != null) {
                ModNetwork.CHANNEL.sendToServer(
                        new SyncVoiceSelectionPacket(
                                get().voice
                        )
                );
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean removePools(List<String> poolIds) {
        if (poolIds == null || poolIds.isEmpty()) return true;

        try {
            ClientDialogueConfig config = get();

            if (config.toml == null) return false;

            for (String poolId : poolIds) {
                config.toml.remove("pools." + poolId + ".messages");
            }

            config.toml.save();
            reload();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static ClientDialogueConfig load() {
        Path folder = Path.of("config", "lethimyap");
        Path file = folder.resolve("client_dialogue.toml");

        try {
            Files.createDirectories(folder);

            if (!Files.exists(file)) {
                writeDefault(file);
            }

            ClientDialogueConfig config = new ClientDialogueConfig();

            config.toml = CommentedFileConfig.builder(file)
                    .sync()
                    .autosave()
                    .preserveInsertionOrder()
                    .build();

            config.toml.load();

            if (!config.toml.contains("client.voice")) config.toml.set("client.voice", DEFAULT_VOICE);

            if (!config.toml.contains("rendering.hudMaxWidth")) config.toml.set("rendering.hudMaxWidth", DEFAULT_HUD_MAX_WIDTH);
            if (!config.toml.contains("rendering.overheadMaxWidth")) config.toml.set("rendering.overheadMaxWidth", DEFAULT_OVERHEAD_MAX_WIDTH);
            if (!config.toml.contains("lastWords.maxWidth")) config.toml.set("lastWords.maxWidth", DEFAULT_LAST_WORDS_MAX_WIDTH);

            writeBuiltInDefaults(config.toml);
            YapPoolRegistry.writeMissingClientDefaults(config.toml);

            config.toml.save();

            config.dialogueColor = DialogueColor.fromString(getString(config.toml, "client.dialogueColor", "white"));

            config.voice = getString(config.toml, "client.voice", DEFAULT_VOICE);

            config.hudAnchor = getString(config.toml, "rendering.hudAnchor", config.hudAnchor);
            config.hudEdgeDistance = getInt(config.toml, "rendering.hudEdgeDistance", config.hudEdgeDistance);
            config.overheadYOffset = getDouble(config.toml, "rendering.overheadYOffset", config.overheadYOffset);
            config.overheadScale = (float) getDouble(config.toml, "rendering.overheadScale", config.overheadScale);

            config.hudMaxWidth = getInt(config.toml, "rendering.hudMaxWidth", config.hudMaxWidth);
            config.overheadMaxWidth = getInt(config.toml, "rendering.overheadMaxWidth", config.overheadMaxWidth);

            config.lastWordsAnchor = getString(config.toml, "lastWords.anchor", config.lastWordsAnchor);
            config.lastWordsOffset = getInt(config.toml, "lastWords.offset", config.lastWordsOffset);
            config.lastWordsMaxWidth = getInt(config.toml, "lastWords.maxWidth", config.lastWordsMaxWidth);

            if (!config.hudAnchor.equalsIgnoreCase("top") && !config.hudAnchor.equalsIgnoreCase("bottom")) config.hudAnchor = "bottom";

            LAST_MODIFIED = getLastModified(file);
            return config;

        } catch (Exception e) {
            e.printStackTrace();
            ClientDialogueConfig fallback = new ClientDialogueConfig();
            fallback.dialogueColor = DEFAULT_DIALOGUE_COLOR;
            return fallback;
        }
    }

    public String pickMessage(String poolId) {
        if (toml == null || poolId == null || poolId.isEmpty()) {
            return "";
        }

        String serverMessage =
                com.lethimyap.client.ClientServerDialogueConfig.pickMessage(poolId);

        if (serverMessage != null && !serverMessage.isBlank()) {
            return serverMessage;
        }

        Object value = toml.get("pools." + poolId + ".messages");

        if (!(value instanceof List<?> list) || list.isEmpty()) {
            return "";
        }

        Object picked = list.get(RANDOM.nextInt(list.size()));

        return picked instanceof String s ? s : "";
    }

    private static void writeDefault(Path file) {
        try (CommentedFileConfig toml = CommentedFileConfig.builder(file)
                .sync()
                .autosave()
                .preserveInsertionOrder()
                .build()) {

            toml.load();

            toml.setComment("client", "Client-side Let Him Yap settings. Valid dialogue colors: white, red, gold, yellow, green, aqua, blue, purple.");
            toml.set("client.dialogueColor", "white");
            toml.set("client.voice", DEFAULT_VOICE);

            toml.setComment("rendering", "Client-side dialogue rendering settings. hudAnchor can be \"top\" or \"bottom\".");
            toml.set("rendering.hudAnchor", "bottom");
            toml.set("rendering.hudEdgeDistance", 68);
            toml.set("rendering.hudMaxWidth", 320);
            toml.set("rendering.overheadYOffset", 0.75D);
            toml.set("rendering.overheadScale", 0.025F);
            toml.set("rendering.overheadMaxWidth", 180);

            toml.setComment(
                    "lastWords",
                    """
                    Death screen last words positioning.

                    Anchor modes:
                    - center : relative to screen center
                    - top    : relative to top edge
                    - bottom : relative to bottom edge

                    Offset behavior:
                    - positive values move downward
                    - negative values move upward
                    """
            );

            toml.set("lastWords.anchor", "center");
            toml.set("lastWords.offset", -20);
            toml.set("lastWords.maxWidth", 300);

            writeBuiltInDefaults(toml);

            YapPoolRegistry.writeMissingClientDefaults(toml);
            toml.save();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void writeBuiltInDefaults(Config toml) {
        putMessagesIfMissing(toml, "health.light",
                "I'm a bit beat up. I should probably take care of that.",
                "Ow... I need to watch myself.",
                "I need to take better care of myself.",
                "Can't let the pain get the better of me.",
                "I need to take a moment.");

        putMessagesIfMissing(toml, "health.hurt",
                "I need to tend to my wounds.",
                "I'm badly hurt...",
                "This is getting dangerous.",
                "I shouldn't let these wounds get any worse.",
                "I shouldn't keep going like this.",
                "I should take care of my wounds before things get dangerous.");

        putMessagesIfMissing(toml, "health.near_death",
                "N-no... this... can't be... be how... I die...",
                "I can't keep going... T-the pain... it's... too much...",
                "I-is this... how... I die..?",
                "My hands... t-they're... red... Is... t-that all... my blood..?",
                "I-I'm scared... I-I... don't want... want to die... h-help...",
                "P-please... someone... h-help me... I feel... c-cold...",
                "Help... please... someone... I... I don't... w-want to die... alone...");

        putMessagesIfMissing(toml, "hunger.peckish",
                "I could go for something to eat right now.",
                "Could do for a snack right about now.",
                "I'm kinda hungry. Time for a snack?",
                "A bit to eat would be pretty nice.",
                "A little food would be kinda nice right now.",
                "Hmm, I should eat something.");

        putMessagesIfMissing(toml, "hunger.hungry",
                "I'm so hungry... I need to eat something.",
                "I'd love to have something to eat right about now.",
                "I need to eat something.",
                "My stomach is growling...",
                "Really could do with a nice meal right now...",
                "Maybe I can take a moment to eat something?");

        putMessagesIfMissing(toml, "hunger.starving",
                "I'm starving...",
                "I need food... n-now...",
                "I can't keep m-moving on an empty stomach...",
                "I'm s-so hungry...",
                "I-I feel weak... need food...",
                "I c-could eat a horse right now...");

        putMessagesIfMissing(toml, "air.drowning",
                "I need air!",
                "I can't breathe!",
                "I need to get to the surface!",
                "I'm going to drown!",
                "I feel lightheaded..!",
                "My lungs are burning up!",
                "I need to breathe!");

        putMessagesIfMissing(toml, "damage.small",
                "Ow!",
                "Ah!",
                "Tch!",
                "Gh!",
                "Oof!");

        putMessagesIfMissing(toml, "damage.medium",
                "Gah!",
                "That hurt!",
                "Ngh!",
                "Ow, dang it!");

        putMessagesIfMissing(toml, "damage.heavy",
                "OW, that hurt!",
                "AH! That hurts really bad!",
                "IT HURTS!");

        putMessagesIfMissing(toml, "damage.lethal",
                "AAAAAAAH!!",
                "OOOOOW!",
                "AAAAAAAAAAAAAAAAAH!!",
                "WHY DOES IT HURT SO MUCH!?",
                "THE PAIN IS UNBEARABLE, MAKE IT STOP!!",
                "AAAAAAAAH IT HURTS SO MUCH!!");

        putMessagesIfMissing(toml, "damage.fire",
                "It burns!",
                "I'm on fire!",
                "Hot hot hot!");

        putMessagesIfMissing(toml, "damage.lava",
                "AAAAAAAAAAAAAAAA!!");

        putMessagesIfMissing(toml, "damage.lightning",
                "AH!",
                "What the hell was that?!",
                "I just got struck by lightning!");

        putMessagesIfMissing(toml, "damage.explosion",
                "That explosion was too close!",
                "My ears are ringing!",
                "I nearly got blown apart!");

        putMessagesIfMissing(toml, "event.wake_up",
                "Mmn... I'm awake.",
                "Another day...",
                "Rise and shine.",
                "Awake and aware.",
                "Sleep really does wonders, huh?",
                "I actually got some good sleep.",
                "Another day, another trial.",
                "I wonder what will happen today?",
                "Time to get up...",
                "Time flies, doesn't it? Time to get up.",
                "One more... no, I should get up.");

        putMessagesIfMissing(toml, "event.sleep_interrupted",
                "Huh? What happened?",
                "Can't I get any rest?",
                "Ugh... I'm awake.",
                "Why am I awake?",
                "Something woke me up...",
                "I can't ever catch a break, can I?");

        putMessagesIfMissing(toml, "event.eat_good",
                "That actually hit the spot.",
                "I needed that.",
                "That was pretty good.",
                "That was... like, REALLY good.",
                "That hits the spot!",
                "Why don't I always eat stuff like this?",
                "It's so good!",
                "This is delicious!",
                "Yummy!",
                "I like this!");

        putMessagesIfMissing(toml, "event.eat_bad",
                "Ugh... that was a mistake.",
                "That tasted awful...",
                "I shouldn't have eaten that.",
                "What did I just eat?",
                "I... why did I do that?",
                "What is wrong with me?",
                "Yuck! This sucks!",
                "This tastes... bad.",
                "I'm gonna be sick...",
                "I feel like I'm going to throw up...");

        putMessagesIfMissing(toml, "event.eat_neutral",
                "It'll do.",
                "Better than nothing.",
                "At least it's food.",
                "Could be worse.",
                "Not the worst thing I've eaten.",
                "It's edible.",
                "Bland.",
                "Meh.",
                "I'd've liked something better.");

        putMessagesIfMissing(toml, "event.insomnia_warning",
                "I should sleep soon...",
                "I'm starting to feel exhausted.",
                "If I don't rest soon, something bad might happen.",
                "Why do I feel eyes on me from the sky?",
                "I really should rest...",
                "I can barely stay awake...",
                "Are those spectres real, or is that just the sleep deprivation?",
                "I need to sleep...",
                "I feel like I'm being watched from the corners of my consciousness...");

        putMessagesIfMissing(toml, "event.fear_yelp",
                "AH!!",
                "WHAT WAS THAT!?",
                "THAT WAS TOO CLOSE!",
                "THAT ALMOST HIT ME!",
                "THAT SCARED THE HECK OUT OF ME!",
                "WHAT IN THE WORLD!?",
                "THAT WAS WAY TOO CLOSE!");

    }

    private static void putMessagesIfMissing(Config toml, String poolId, String... messages) {
        String path = "pools." + poolId + ".messages";

        if (!toml.contains(path)) {
            toml.set(path, List.of(messages));
        }
    }
    
    public static List<String> getBuiltInDefaultLines(
            String poolId
    ) {
        if (poolId == null || poolId.isBlank()) {
            return List.of();
        }

        Config defaults =
                Config.inMemory();

        writeBuiltInDefaults(defaults);

        Object value =
                defaults.get(
                        "pools." + poolId + ".messages"
                );

        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        return list.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .toList();
    }

    public static boolean setVoice(String voiceId) {
        if (voiceId == null || voiceId.isBlank()) {
            return false;
        }

        try {
            ClientDialogueConfig config = get();

            if (config.toml == null) {
                return false;
            }

            config.toml.set(
                    "client.voice",
                    voiceId
            );

            config.toml.save();
            config.voice = voiceId;

            Path file =
                    Path.of(
                            "config",
                            "lethimyap",
                            "client_dialogue.toml"
                    );

            LAST_MODIFIED = getLastModified(file);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void putMessages(CommentedFileConfig toml, String poolId, String... messages) {
        toml.set("pools." + poolId + ".messages", List.of(messages));
    }

    private static String getString(CommentedFileConfig toml, String path, String fallback) {
        Object value = toml.get(path);
        return value instanceof String s ? s : fallback;
    }

    private static int getInt(CommentedFileConfig toml, String path, int fallback) {
        Object value = toml.get(path);
        return value instanceof Number n ? n.intValue() : fallback;
    }

    private static double getDouble(CommentedFileConfig toml, String path, double fallback) {
        Object value = toml.get(path);
        return value instanceof Number n ? n.doubleValue() : fallback;
    }

    public static void writeMissingExternalDefaults() {
        if (INSTANCE == null || INSTANCE.toml == null) return;

        YapPoolRegistry.writeMissingClientDefaults(INSTANCE.toml);
        INSTANCE.toml.save();

        Path file = Path.of("config", "lethimyap", "client_dialogue.toml");
        LAST_MODIFIED = getLastModified(file);
    }

    public static void reloadIfChanged() {
        Path file = Path.of("config", "lethimyap", "client_dialogue.toml");

        long modified = getLastModified(file);

        if (modified <= 0L) return;

        if (modified != LAST_MODIFIED) {
            reload();
        }
    }

    private static long getLastModified(Path file) {
        try {
            if (!Files.exists(file)) return 0L;

            return Files.getLastModifiedTime(file).toMillis();
        } catch (Exception e) {
            return 0L;
        }
    }
}