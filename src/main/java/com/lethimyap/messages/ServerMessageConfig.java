package com.lethimyap.messages;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class ServerMessageConfig {

    private static ServerMessageConfig INSTANCE;

    public boolean enabled = true;

    public int maxDialogueLength = 120;
    public static final int DEFAULT_MAX_DIALOGUE_LENGTH = 120;

    public int minTicksBetweenMessages = 400;
    public int maxTicksBetweenMessages = 1200;

    public int floatingMessageTicks = 80;
    public int floatingMessageDeathTicks = 60;
    public int floatingMessageFadeTicks = 20;

    public boolean typewriterEnabled = true;
    public int typewriterMinDelayTicks = 1;
    public int typewriterMaxDelayTicks = 7;
    public double typewriterHealthSlowdownPower = 3.0;
    public double typewriterSlowdownStartHealthPercent = 40.0;

    public boolean typewriterSoundEnabled = true;
    public float typewriterSoundVolume = 1.5f;
    public float typewriterSoundPitchMin = 0.9f;
    public float typewriterSoundPitchMax = 1.15f;

    // This cannot be changed. It shouldn't be. Things WILL break if it is.
    public String typewriterSound = "lethimyap:yap";

    public boolean adrenalineEnabled = true;
    public double adrenalineMax = 100.0;
    public double adrenalineGainPerDamage = 6.0;
    public double adrenalineDecayPerTick = 0.05;
    public double adrenalineMaxSlowdownReduction = 0.85;

    public boolean deathInterruptEnabled = true;
    public String deathInterruptSuffix = "-";
    public int deathPartialMinCharsToSave = 8;

    public boolean damageReactionsEnabled = true;
    public boolean eventDialoguesEnabled = true;

    public double neutralFoodDialogueChance = 0.5;

    public boolean insomniaWarningsEnabled = true;
    public int insomniaWarningTicks = 60000;
    public int insomniaRepeatTicks = 6000;

    public double fearEventRadius = 8.0;
    public int fearEventCooldownTicks = 100;

    public boolean forcedAirMessagesEnabled = true;
    public int forcedAirThreshold = 120;

    public final ArrayList<Pool> pools = new ArrayList<>();
    public final ArrayList<DamagePool> damagePools = new ArrayList<>();

    public static ServerMessageConfig get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }

        return INSTANCE;
    }

    public static void reload() {
        INSTANCE = load();
    }

    private static ServerMessageConfig load() {
        Path folder = Path.of("config", "lethimyap");
        Path file = folder.resolve("server_messages.toml");

        try {
            Files.createDirectories(folder);

            if (!Files.exists(file)) {
                ServerMessageConfig defaults = createDefault();
                defaults.writeDefaultToml(file);
                return defaults;
            }

            ServerMessageConfig config = createDefault();

            try (CommentedFileConfig toml = CommentedFileConfig.builder(file)
                    .sync()
                    .autosave()
                    .preserveInsertionOrder()
                    .build()) {

                toml.load();

                config.enabled = getBool(toml, "general.enabled", config.enabled);
                
                config.maxDialogueLength = getInt(toml, "general.maxDialogueLength", config.maxDialogueLength);

                if (config.maxDialogueLength <= 0) {
                    config.maxDialogueLength = DEFAULT_MAX_DIALOGUE_LENGTH;
                }

                config.minTicksBetweenMessages = getInt(toml, "timing.minTicksBetweenMessages", config.minTicksBetweenMessages);
                config.maxTicksBetweenMessages = getInt(toml, "timing.maxTicksBetweenMessages", config.maxTicksBetweenMessages);

                config.floatingMessageTicks = getInt(toml, "floating.floatingMessageTicks", config.floatingMessageTicks);
                config.floatingMessageDeathTicks = getInt(toml, "floating.floatingMessageDeathTicks", config.floatingMessageDeathTicks);
                config.floatingMessageFadeTicks = getInt(toml, "floating.floatingMessageFadeTicks", config.floatingMessageFadeTicks);

                config.typewriterEnabled = getBool(toml, "typewriter.enabled", config.typewriterEnabled);
                config.typewriterMinDelayTicks = getInt(toml, "typewriter.minDelayTicks", config.typewriterMinDelayTicks);
                config.typewriterMaxDelayTicks = getInt(toml, "typewriter.maxDelayTicks", config.typewriterMaxDelayTicks);
                config.typewriterHealthSlowdownPower = getDouble(toml, "typewriter.healthSlowdownPower", config.typewriterHealthSlowdownPower);
                config.typewriterSlowdownStartHealthPercent =
                        getDouble(toml, "typewriter.slowdownStartHealthPercent", config.typewriterSlowdownStartHealthPercent);

                config.typewriterSoundEnabled = getBool(toml, "sound.enabled", config.typewriterSoundEnabled);
                config.typewriterSoundVolume = (float) getDouble(toml, "sound.volume", config.typewriterSoundVolume);
                config.typewriterSoundPitchMin = (float) getDouble(toml, "sound.pitchMin", config.typewriterSoundPitchMin);
                config.typewriterSoundPitchMax = (float) getDouble(toml, "sound.pitchMax", config.typewriterSoundPitchMax);

                config.adrenalineEnabled = getBool(toml, "adrenaline.enabled", config.adrenalineEnabled);
                config.adrenalineMax = getDouble(toml, "adrenaline.max", config.adrenalineMax);
                config.adrenalineGainPerDamage = getDouble(toml, "adrenaline.gainPerDamage", config.adrenalineGainPerDamage);
                config.adrenalineDecayPerTick = getDouble(toml, "adrenaline.decayPerTick", config.adrenalineDecayPerTick);
                config.adrenalineMaxSlowdownReduction = getDouble(toml, "adrenaline.maxSlowdownReduction", config.adrenalineMaxSlowdownReduction);

                config.deathInterruptEnabled = getBool(toml, "death.interruptEnabled", config.deathInterruptEnabled);
                config.deathInterruptSuffix = getString(toml, "death.interruptSuffix", config.deathInterruptSuffix);
                config.deathPartialMinCharsToSave = getInt(toml, "death.partialMinCharsToSave", config.deathPartialMinCharsToSave);

                config.damageReactionsEnabled = getBool(toml, "events.damageReactionsEnabled", config.damageReactionsEnabled);
                config.eventDialoguesEnabled = getBool(toml, "events.eventDialoguesEnabled", config.eventDialoguesEnabled);
                config.neutralFoodDialogueChance = getDouble(toml, "events.neutralFoodDialogueChance", config.neutralFoodDialogueChance);
                config.insomniaWarningsEnabled = getBool(toml, "events.insomniaWarningsEnabled", config.insomniaWarningsEnabled);
                config.insomniaWarningTicks = getInt(toml, "events.insomniaWarningTicks", config.insomniaWarningTicks);
                config.insomniaRepeatTicks = getInt(toml, "events.insomniaRepeatTicks", config.insomniaRepeatTicks);
                config.fearEventRadius = getDouble(toml, "events.fearEventRadius", config.fearEventRadius);
                config.fearEventCooldownTicks = getInt(toml, "events.fearEventCooldownTicks", config.fearEventCooldownTicks);
                config.forcedAirMessagesEnabled = getBool(toml, "events.forcedAirMessagesEnabled", config.forcedAirMessagesEnabled);
                config.forcedAirThreshold = getInt(toml, "events.forcedAirThreshold", config.forcedAirThreshold);
            }

            INSTANCE = config;
            PoolOverrideConfig.generateOrUpdate();

            return config;

        } catch (Exception e) {
            e.printStackTrace();
            return createDefault();
        }
    }

    private void writeDefaultToml(Path file) throws Exception {
        try (CommentedFileConfig toml = CommentedFileConfig.builder(file)
                .sync()
                .autosave()
                .preserveInsertionOrder()
                .build()) {

            toml.load();

            toml.setComment("general", "Master toggle.");
            toml.set("general.enabled", enabled);

            toml.setComment("general", "How long a dialogue can be before getting truncated.");
            toml.set("general.maxDialogueLength", maxDialogueLength);

            toml.setComment("timing", "Server-authoritative timing ranges. Clients can roll within these ranges.");
            toml.set("timing.minTicksBetweenMessages", minTicksBetweenMessages);
            toml.set("timing.maxTicksBetweenMessages", maxTicksBetweenMessages);

            toml.setComment("floating", "Floating text duration and fade behavior.");
            toml.set("floating.floatingMessageTicks", floatingMessageTicks);
            toml.set("floating.floatingMessageDeathTicks", floatingMessageDeathTicks);
            toml.set("floating.floatingMessageFadeTicks", floatingMessageFadeTicks);

            toml.setComment("typewriter", "Server-side typewriter pacing.");
            toml.set("typewriter.enabled", typewriterEnabled);
            toml.set("typewriter.minDelayTicks", typewriterMinDelayTicks);
            toml.set("typewriter.maxDelayTicks", typewriterMaxDelayTicks);
            toml.set("typewriter.healthSlowdownPower", typewriterHealthSlowdownPower);
            toml.set("typewriter.slowdownStartHealthPercent", typewriterSlowdownStartHealthPercent);

            toml.setComment("adrenaline", "Adrenaline affects typewriter slowdown after taking damage.");
            toml.set("adrenaline.enabled", adrenalineEnabled);
            toml.set("adrenaline.max", adrenalineMax);
            toml.set("adrenaline.gainPerDamage", adrenalineGainPerDamage);
            toml.set("adrenaline.decayPerTick", adrenalineDecayPerTick);
            toml.set("adrenaline.maxSlowdownReduction", adrenalineMaxSlowdownReduction);

            toml.setComment("sound", "Sound is played from the speaking player. Volume 1.5 is roughly 24 blocks.");
            toml.set("sound.enabled", typewriterSoundEnabled);
            toml.set("sound.volume", typewriterSoundVolume);
            toml.set("sound.pitchMin", typewriterSoundPitchMin);
            toml.set("sound.pitchMax", typewriterSoundPitchMax);

            toml.setComment("death", "Last words and death interruption settings.");
            toml.set("death.interruptEnabled", deathInterruptEnabled);
            toml.set("death.interruptSuffix", deathInterruptSuffix);
            toml.set("death.partialMinCharsToSave", deathPartialMinCharsToSave);

            toml.setComment("events", "Server-side event dialogue conditions.");
            toml.set("events.damageReactionsEnabled", damageReactionsEnabled);
            toml.set("events.eventDialoguesEnabled", eventDialoguesEnabled);
            toml.set("events.neutralFoodDialogueChance", neutralFoodDialogueChance);
            toml.set("events.insomniaWarningsEnabled", insomniaWarningsEnabled);
            toml.set("events.insomniaWarningTicks", insomniaWarningTicks);
            toml.set("events.insomniaRepeatTicks", insomniaRepeatTicks);
            toml.set("events.fearEventRadius", fearEventRadius);
            toml.set("events.fearEventCooldownTicks", fearEventCooldownTicks);
            toml.set("events.forcedAirMessagesEnabled", forcedAirMessagesEnabled);
            toml.set("events.forcedAirThreshold", forcedAirThreshold);

            toml.save();
        }
    }

    private static ServerMessageConfig createDefault() {
        ServerMessageConfig config = new ServerMessageConfig();

        config.pools.add(Pool.healthPercent("health.light", "health", 1, 10, true, false, 75));
        config.pools.add(Pool.healthPercent("health.hurt", "health", 2, 20, true, false, 40));
        config.pools.add(Pool.healthPercent("health.near_death", "health", 3, 1000000, true, false, 20));

        config.pools.add(Pool.hunger("hunger.peckish", "hunger", 1, 10, true, false, 16));
        config.pools.add(Pool.hunger("hunger.hungry", "hunger", 2, 20, true, false, 10));
        config.pools.add(Pool.hunger("hunger.starving", "hunger", 3, 50, true, false, 4));

        config.pools.add(Pool.air("air.drowning", "air", 1, true, 120));

        DamagePool small = new DamagePool("damage.small", 1.0f, 0.65, false);
        small.excludedDamageTypes.add("minecraft:fall");

        DamagePool medium = new DamagePool("damage.medium", 4.0f, 0.85, false);

        DamagePool heavy = new DamagePool("damage.heavy", 8.0f, 1.0, false);

        DamagePool lethal = new DamagePool("damage.lethal", 14.0f, 1.0, false);

        DamagePool fire = new DamagePool("damage.fire", 1.0f, 1.0, false);
        fire.exclusiveDamageTypes.add("minecraft:campfire");
        fire.exclusiveDamageTypes.add("minecraft:hot_floor");
        fire.exclusiveDamageTypes.add("minecraft:in_fire");
        fire.exclusiveDamageTypes.add("minecraft:on_fire");

        DamagePool lava = new DamagePool("damage.lava", 1.0f, 1.0, false);
        lava.exclusiveDamageTypes.add("minecraft:lava");

        DamagePool lightning = new DamagePool("damage.lightning", 1.0f, 1.0, false);
        lightning.exclusiveDamageTypes.add("minecraft:lightning_bolt");

        DamagePool explosion = new DamagePool("damage.explosion", 1.0f, 1.0, false);
        explosion.exclusiveDamageTypes.add("minecraft:explosion");
        explosion.exclusiveDamageTypes.add("minecraft:player_explosion");

        config.damagePools.add(small);
        config.damagePools.add(medium);
        config.damagePools.add(heavy);
        config.damagePools.add(lethal);
        config.damagePools.add(fire);
        config.damagePools.add(lava);
        config.damagePools.add(lightning);
        config.damagePools.add(explosion);

        return config;
    }

    public static class Pool {
        public String id;
        public String group;
        public int tier;
        public int weight;
        public boolean important = true;
        public boolean forcedOnly = false;
        public Condition condition = new Condition();

        public static Pool healthPercent(String id, String group, int tier, int weight, boolean important, boolean forcedOnly, float below) {
            Pool pool = base(id, group, tier, weight, important, forcedOnly);
            pool.condition = Condition.healthPercentBelow(below);
            return pool;
        }

        public static Pool hunger(String id, String group, int tier, int weight, boolean important, boolean forcedOnly, float below) {
            Pool pool = base(id, group, tier, weight, important, forcedOnly);
            pool.condition = Condition.hungerBelow(below);
            return pool;
        }

        public static Pool air(String id, String group, int tier, boolean important, float below) {
            Pool pool = base(id, group, tier, 0, important, true);
            pool.condition = Condition.airBelow(below);
            return pool;
        }

        public static Pool nbtNumber(String id, String group, int tier, int weight, boolean important, boolean forcedOnly,
                                     String path, String compare, float value) {
            Pool pool = base(id, group, tier, weight, important, forcedOnly);
            pool.condition = Condition.nbtNumber(path, compare, value);
            return pool;
        }

        private static Pool base(String id, String group, int tier, int weight, boolean important, boolean forcedOnly) {
            Pool pool = new Pool();
            pool.id = id;
            pool.group = group;
            pool.tier = tier;
            pool.weight = weight;
            pool.important = important;
            pool.forcedOnly = forcedOnly;
            return pool;
        }

        public boolean matches(ServerPlayer player) {
            return condition == null || condition.matches(player);
        }
    }

    public static class DamagePool {
        public String id;
        public float minDamage;
        public double chance;
        public boolean important;

        public ArrayList<String> excludedDamageTypes = new ArrayList<>();
        public ArrayList<String> exclusiveDamageTypes = new ArrayList<>();

        public DamagePool(String id, float minDamage, double chance, boolean important) {
            this.id = id;
            this.minDamage = minDamage;
            this.chance = chance;
            this.important = important;
        }
    }

    public static class Condition {
        public String type = "always";
        public float below = 0;
        public String nbtPath = "";
        public String compare = "below";
        public float value = 0;

        public static Condition healthPercentBelow(float percent) {
            Condition condition = new Condition();
            condition.type = "health_percent";
            condition.below = percent;
            return condition;
        }

        public static Condition hungerBelow(float hunger) {
            Condition condition = new Condition();
            condition.type = "hunger";
            condition.below = hunger;
            return condition;
        }

        public static Condition airBelow(float air) {
            Condition condition = new Condition();
            condition.type = "air";
            condition.below = air;
            return condition;
        }

        public static Condition nbtNumber(String path, String compare, float value) {
            Condition condition = new Condition();
            condition.type = "nbt_number";
            condition.nbtPath = path;
            condition.compare = compare;
            condition.value = value;
            return condition;
        }

        public boolean matches(ServerPlayer player) {
            return switch (type) {
                case "always" -> true;
                case "health_percent" -> ((player.getHealth() / player.getMaxHealth()) * 100f) <= below;
                case "health" -> player.getHealth() <= below;
                case "hunger" -> {
                    FoodData food = player.getFoodData();
                    yield food.getFoodLevel() <= below;
                }
                case "air" -> player.getAirSupply() <= below;
                case "nbt_number" -> matchesNbtNumber(player);
                default -> false;
            };
        }

        private boolean matchesNbtNumber(ServerPlayer player) {
            double found = NbtPathReader.getNumber(player, nbtPath, Double.NaN);
            if (Double.isNaN(found)) return false;

            return switch (compare) {
                case "below" -> found <= value;
                case "above" -> found >= value;
                case "equal" -> found == value;
                case "not_equal" -> found != value;
                default -> false;
            };
        }
    }

    private static boolean getBool(CommentedFileConfig toml, String path, boolean fallback) {
        Object value = toml.get(path);
        return value instanceof Boolean b ? b : fallback;
    }

    private static int getInt(CommentedFileConfig toml, String path, int fallback) {
        Object value = toml.get(path);
        return value instanceof Number n ? n.intValue() : fallback;
    }

    private static double getDouble(CommentedFileConfig toml, String path, double fallback) {
        Object value = toml.get(path);
        return value instanceof Number n ? n.doubleValue() : fallback;
    }

    private static String getString(CommentedFileConfig toml, String path, String fallback) {
        Object value = toml.get(path);
        return value instanceof String s ? s : fallback;
    }
}