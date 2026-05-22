package com.lethimyap.messages;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.lethimyap.api.YapPoolRegistry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

public class ClientDialogueConfig {

    private static final Random RANDOM = new Random();
    private static ClientDialogueConfig INSTANCE;

    public String hudAnchor = "bottom";
    public int hudEdgeDistance = 68;

    public String lastWordsAnchor = "center";
    public int lastWordsOffset = -20;

    public double overheadYOffset = 0.65D;
    public float overheadScale = 0.025F;

    public DialogueColor dialogueColor = DialogueColor.WHITE;

    private CommentedFileConfig toml;

    private static long LAST_MODIFIED = 0L;

    public static ClientDialogueConfig get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }

        return INSTANCE;
    }

    public static void reload() {
        INSTANCE = load();
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

            writeBuiltInDefaults(config.toml);
            YapPoolRegistry.writeMissingClientDefaults(config.toml);

            config.toml.save();

            config.dialogueColor = DialogueColor.fromString(
                    getString(config.toml, "client.dialogueColor", "white")
            );

            config.hudAnchor = getString(config.toml, "rendering.hudAnchor", config.hudAnchor);
            config.hudEdgeDistance = getInt(config.toml, "rendering.hudEdgeDistance", config.hudEdgeDistance);
            config.overheadYOffset = getDouble(config.toml, "rendering.overheadYOffset", config.overheadYOffset);
            config.overheadScale = (float) getDouble(config.toml, "rendering.overheadScale", config.overheadScale);

            config.lastWordsAnchor =
                    getString(config.toml, "lastWords.anchor", config.lastWordsAnchor);

            config.lastWordsOffset =
                    getInt(config.toml, "lastWords.offset", config.lastWordsOffset);

            if (!config.hudAnchor.equalsIgnoreCase("top")
                    && !config.hudAnchor.equalsIgnoreCase("bottom")) {
                config.hudAnchor = "bottom";
            }

            LAST_MODIFIED = getLastModified(file);
            return config;

        } catch (Exception e) {
            e.printStackTrace();
            ClientDialogueConfig fallback = new ClientDialogueConfig();
            fallback.dialogueColor = DialogueColor.WHITE;
            return fallback;
        }
    }

    public String pickMessage(String poolId) {
        if (toml == null || poolId == null || poolId.isEmpty()) {
            return "";
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

            toml.setComment("client", "Client-side dialogue customization. Valid colors: white, red, gold, yellow, green, aqua, blue, purple.");
            toml.set("client.dialogueColor", "white");

            toml.setComment("rendering", "Client-side dialogue rendering settings. hudAnchor can be \"top\" or \"bottom\".");
            toml.set("rendering.hudAnchor", "bottom");
            toml.set("rendering.hudEdgeDistance", 68);
            toml.set("rendering.overheadYOffset", 0.75D);
            toml.set("rendering.overheadScale", 0.025F);

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

            writeBuiltInDefaults(toml);

            YapPoolRegistry.writeMissingClientDefaults(toml);
            toml.save();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeBuiltInDefaults(CommentedFileConfig toml) {
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

    private static void putMessagesIfMissing(CommentedFileConfig toml, String poolId, String... messages) {
        String path = "pools." + poolId + ".messages";

        if (!toml.contains(path)) {
            toml.set(path, List.of(messages));
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