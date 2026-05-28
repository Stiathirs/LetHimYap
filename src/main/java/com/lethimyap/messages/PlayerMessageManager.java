package com.lethimyap.messages;

import com.lethimyap.api.YapPoolRegistry;
import com.lethimyap.api.YapPriority;
import com.lethimyap.network.ModNetwork;
import com.lethimyap.network.OfferDialoguePoolPacket;
import com.lethimyap.network.SyncLastWordsPacket;
import com.lethimyap.network.SyncSpeechHudPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class PlayerMessageManager {

    private static final String LAST_WORDS_TAG = "lethimyap_last_words";
    private static final String RESPAWN_GRACE_TAG = "lethimyap_respawn_grace";
    private static final String AIR_FORCED_USED_TAG = "lethimyap_air_forced_used";
    private static final String PAIN_DIALOGUE_GRACE_TAG = "lethimyap_pain_dialogue_grace";
    private static final String SUPPRESSIONS_TAG = "lethimyap_dialogue_suppressions";
    private static final String SLOWDOWNS_TAG = "lethimyap_dialogue_slowdowns";

    private static final Random RANDOM = new Random();
    private static final Map<UUID, ActiveThought> ACTIVE = new HashMap<>();

    public static void tick(ServerPlayer player) {
        ServerMessageConfig config = ServerMessageConfig.get();
        if (!config.enabled) return;

        if (!player.isAlive()) {
            return;
        }

        int respawnGrace = player.getPersistentData().getInt(RESPAWN_GRACE_TAG);
        if (respawnGrace > 0) {
            player.getPersistentData().putInt(RESPAWN_GRACE_TAG, respawnGrace - 1);
            return;
        }

        tickDialogueSuppressions(player);
        tickSpeechSlowdowns(player);
        PoolCooldownManager.tick(player);

        if (isDialogueSuppressed(player)) {
            return;
        }

        int painGrace = player.getPersistentData().getInt(PAIN_DIALOGUE_GRACE_TAG);
        if (painGrace > 0) {
            player.getPersistentData().putInt(PAIN_DIALOGUE_GRACE_TAG, painGrace - 1);
        }

        if (tryForcedAirMessage(player, config)) {
            return;
        }

        ActiveThought active = ACTIVE.get(player.getUUID());

        if (active != null) {
            tickActiveThought(player, active, config);
        }
    }

    public static void handleClientDialogueRequest(ServerPlayer player) {
        ServerMessageConfig config = ServerMessageConfig.get();

        if (!config.enabled) return;
        if (!player.isAlive()) return;

        int respawnGrace = player.getPersistentData().getInt(RESPAWN_GRACE_TAG);
        if (respawnGrace > 0) return;

        if (isDialogueSuppressed(player)) return;

        int painGrace = player.getPersistentData().getInt(PAIN_DIALOGUE_GRACE_TAG);
        if (painGrace > 0) return;

        ActiveThought active = ACTIVE.get(player.getUUID());
        if (active != null) return;

        offerRandomDialoguePool(player, config);
    }

    private static void offerRandomDialoguePool(ServerPlayer player, ServerMessageConfig config) {
        List<PoolOverrideConfig.PoolView> availablePools =
                getBestAvailablePools(player, config);

        if (availablePools.isEmpty()) return;

        PoolOverrideConfig.PoolView chosenView =
                chooseWeightedPool(availablePools);

        if (chosenView == null
                || chosenView.pool == null
                || chosenView.pool.id == null
                || chosenView.pool.id.isBlank()) {
            return;
        }

        PoolCooldownManager.applyNormal(player, chosenView);

        offerPool(
                player,
                chosenView.pool.id,
                chosenView.important,
                false,
                YapPriority.NORMAL
        );
    }

    private static void offerPool(
            ServerPlayer player,
            String poolId,
            boolean important,
            boolean pain,
            YapPriority priority
    ) {
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new OfferDialoguePoolPacket(poolId, important, pain, priority)
        );
    }

    public static void handleClientDialogueSelection(
            ServerPlayer player,
            String message,
            DialogueColor color,
            boolean important,
            boolean pain,
            YapPriority priority
    ) {
        if (!player.isAlive()) return;
        if (message == null || message.isBlank()) return;

        if (isDialogueSuppressed(player)) return;

        int maxLength = ServerMessageConfig.get().maxDialogueLength;

        if (message.length() > maxLength) {
            message = message.substring(0, maxLength);
        }

        startThought(
                player,
                message,
                important,
                pain ? ActiveThought.ThoughtType.PAIN : ActiveThought.ThoughtType.NORMAL,
                color,
                priority
        );
    }

    private static void startThought(
            ServerPlayer player,
            String message,
            boolean important,
            ActiveThought.ThoughtType type,
            DialogueColor color,
            YapPriority priority
    ) {
        ServerMessageConfig config = ServerMessageConfig.get();

        ActiveThought current = ACTIVE.get(player.getUUID());

        // Lower-priority dialogue cannot interrupt higher-priority dialogue.
        // Equal priority is allowed to replace equal priority.
        if (current != null && priority.level < current.priority.level) {
            return;
        }

        // Pain or higher interruptions preserve unfinished important text as possible last words.
        if (current != null
                && priority.level >= YapPriority.PAIN.level
                && current.priority.level <= priority.level
                && !current.finished
                && current.visibleMessage != null
                && !current.visibleMessage.isEmpty()) {

            String interruptedText = current.visibleMessage + config.deathInterruptSuffix;

            if (current.important
                    && interruptedText.length() >= config.deathPartialMinCharsToSave) {
                saveLastWords(player, interruptedText);
            }
        }

        ACTIVE.remove(player.getUUID());

        ActiveThought thought = new ActiveThought(
                message,
                UnderwaterSpeechFilter.garble(message),
                config.floatingMessageTicks,
                important,
                type,
                color,
                priority
        );

        if (config.typewriterEnabled) {
            ACTIVE.put(player.getUUID(), thought);
        } else {
            String displayed = UnderwaterSpeechFilter.shouldMuffleSpeech(player)
                    ? thought.garbledFullMessage
                    : thought.fullMessage;

            thought.visibleMessage = displayed;
            show(player, displayed, color);

            if (important) {
                saveLastWords(player, displayed);
            }

            if (type == ActiveThought.ThoughtType.PAIN) {
                player.getPersistentData().putInt(PAIN_DIALOGUE_GRACE_TAG, 20);
            }
        }
    }

    private static void tickActiveThought(
            ServerPlayer player,
            ActiveThought thought,
            ServerMessageConfig config
    ) {
        if (!thought.finished) {
            if (thought.nextCharDelay > 0) {
                thought.nextCharDelay--;
            }

            if (thought.nextCharDelay > 0) {
                return;
            }

            if (thought.index < thought.fullMessage.length()) {
                boolean muffled = UnderwaterSpeechFilter.shouldMuffleSpeech(player);

                thought.appendNextCharacter(muffled);

                showActiveThought(player, thought);

                char addedChar = thought.fullMessage.charAt(thought.index - 1);

                playTypewriterSound(player, addedChar, config);

                thought.nextCharDelay = getDelayForHealth(player, config);
            }

            if (thought.index >= thought.fullMessage.length()) {
                thought.finished = true;

                if (thought.important) {
                    saveLastWords(player, thought.visibleMessage);
                }
            }

            return;
        }

        thought.remainingVisibleTicks--;

        if (thought.remainingVisibleTicks <= 0) {
            ACTIVE.remove(player.getUUID());

            if (thought.type == ActiveThought.ThoughtType.PAIN) {
                player.getPersistentData().putInt(PAIN_DIALOGUE_GRACE_TAG, 20);
            }
        }
    }

    public static void forcePool(ServerPlayer player, String poolId, boolean important, boolean pain) {
        forcePool(
                player,
                poolId,
                important,
                pain,
                pain ? YapPriority.PAIN : YapPriority.EVENT
        );
    }

    public static void forcePool(
            ServerPlayer player,
            String poolId,
            boolean important,
            boolean pain,
            YapPriority priority
    ) {
        if (!player.isAlive()) return;
        if (isDialogueSuppressed(player)) return;

        PoolOverrideConfig.PoolView view = getPoolViewById(poolId);

        if (view != null) {
            if (!PoolCooldownManager.canUseForced(player, view)) {
                return;
            }

            PoolCooldownManager.applyForced(player, view);
        }

        offerPool(player, poolId, important, pain, priority);
    }

    public static void forceSay(ServerPlayer player, String message) {
        forceSpeechWithPriority(player, message, true, false, YapPriority.EVENT);
    }

    public static void forceSay(ServerPlayer player, String message, boolean important) {
        forceSpeechWithPriority(player, message, important, false, YapPriority.EVENT);
    }

    public static void forcePainSay(ServerPlayer player, String message, boolean important) {
        forceSpeechWithPriority(player, message, important, true, YapPriority.PAIN);
    }

    public static void forceSpeechWithPriority(
            ServerPlayer player,
            String message,
            boolean important,
            boolean pain,
            YapPriority priority
    ) {
        if (isDialogueSuppressed(player)) return;

        handleClientDialogueSelection(
                player,
                message,
                DialogueColor.WHITE,
                important,
                pain,
                priority
        );
    }

    public static void interruptOnDeath(ServerPlayer player) {
        ServerMessageConfig config = ServerMessageConfig.get();

        ActiveThought thought = ACTIVE.get(player.getUUID());
        if (thought == null) return;
        if (!config.deathInterruptEnabled) return;
        if (thought.finished) return;

        if (thought.visibleMessage == null || thought.visibleMessage.isEmpty()) {
            ACTIVE.remove(player.getUUID());
            return;
        }

        String interrupted = thought.visibleMessage + config.deathInterruptSuffix;

        thought.visibleMessage = interrupted;
        thought.finished = true;
        thought.remainingVisibleTicks = config.floatingMessageDeathTicks;

        showActiveThought(player, thought);

        if (thought.important && interrupted.length() >= config.deathPartialMinCharsToSave) {
            saveLastWords(player, interrupted);
        }

        ACTIVE.remove(player.getUUID());
    }

    public static String getLastWords(ServerPlayer player) {
        return player.getPersistentData().getString(LAST_WORDS_TAG);
    }

    public static void saveLastWords(ServerPlayer player, String message) {
        player.getPersistentData().putString(LAST_WORDS_TAG, message);

        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncLastWordsPacket(message)
        );
    }

    public static void startRespawnGrace(ServerPlayer player) {
        player.getPersistentData().putInt(RESPAWN_GRACE_TAG, 20);

        player.getPersistentData().putString(LAST_WORDS_TAG, "");

        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncLastWordsPacket("")
        );
    }

    private static boolean tryForcedAirMessage(
            ServerPlayer player,
            ServerMessageConfig config
    ) {
        if (!config.forcedAirMessagesEnabled) return false;
        if (DefaultPoolDisabler.isGroupDisabled("air")) return false;

        boolean lowAir = player.getAirSupply() <= config.forcedAirThreshold;

        if (!lowAir) {
            player.getPersistentData().putBoolean(AIR_FORCED_USED_TAG, false);
            return false;
        }

        boolean alreadyUsed =
                player.getPersistentData().getBoolean(AIR_FORCED_USED_TAG);

        if (alreadyUsed) return false;

        player.getPersistentData().putBoolean(
                AIR_FORCED_USED_TAG,
                true
        );

        PoolOverrideConfig.PoolView bestAirView = null;
        int bestAirTier = Integer.MIN_VALUE;

        ArrayList<ServerMessageConfig.Pool> allPools = new ArrayList<>();
        allPools.addAll(config.pools);
        allPools.addAll(YapPoolRegistry.getServerPools());

        for (ServerMessageConfig.Pool pool : allPools) {
            if (pool == null) continue;
            if (pool.group == null) continue;

            PoolOverrideConfig.PoolView view =
                    PoolOverrideConfig.apply(pool);

            if (view == null) continue;
            if (!view.enabled) continue;
            if (!view.forcedOnly) continue;
            if (!"air".equals(pool.group)) continue;
            if (!view.matches(player)) continue;
            if (!PoolCooldownManager.canUseForced(player, view)) continue;

            if (bestAirView == null || view.tier > bestAirTier) {
                bestAirView = view;
                bestAirTier = view.tier;
            }
        }

        if (bestAirView == null
                || bestAirView.pool == null
                || bestAirView.pool.id == null
                || bestAirView.pool.id.isBlank()) {
            return false;
        }

        PoolCooldownManager.applyForced(player, bestAirView);

        offerPool(
                player,
                bestAirView.pool.id,
                bestAirView.important,
                false,
                YapPriority.EVENT
        );

        return true;
    }

    private static void showActiveThought(ServerPlayer player, ActiveThought thought) {
        show(player, thought.visibleMessage, thought.color);
    }

    private static void show(ServerPlayer player, String message, DialogueColor color) {
        ServerMessageConfig config = ServerMessageConfig.get();

        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncSpeechHudPacket(
                        message,
                        color.id,
                        config.floatingMessageTicks
                )
        );

        FloatingMessageManager.set(player, message, color);
    }

    private static int getDelayForHealth(
            ServerPlayer player,
            ServerMessageConfig config
    ) {
        float healthPercent =
                (player.getHealth() / player.getMaxHealth()) * 100.0f;

        healthPercent = Math.max(0f, Math.min(100f, healthPercent));

        int min = Math.max(1, config.typewriterMinDelayTicks);
        int max = Math.max(min, config.typewriterMaxDelayTicks);

        double start = Math.max(
                1.0,
                Math.min(100.0, config.typewriterSlowdownStartHealthPercent)
        );

        int delay;

        if (healthPercent >= start) {
            delay = min;
        } else {
            double danger = 1.0 - (healthPercent / start);
            double exponent = Math.max(1.0, config.typewriterHealthSlowdownPower);

            double scaledDanger =
                    (Math.exp(exponent * danger) - 1.0)
                            / (Math.exp(exponent) - 1.0);

            delay = min + (int) Math.round((max - min) * scaledDanger);
        }

        double adrenalineReduction =
                AdrenalineManager.getSlowdownReduction(player);

        delay = (int) Math.round(delay * (1.0 - adrenalineReduction));

        double speechSlowdown =
                getSpeechSlowdownMultiplier(player);

        delay = (int) Math.round(delay * speechSlowdown);

        return Math.max(min, delay);
    }

    private static void playTypewriterSound(
            ServerPlayer player,
            char addedChar,
            ServerMessageConfig config
    ) {
        if (!config.typewriterSoundEnabled) return;
        if (Character.isWhitespace(addedChar)) return;

        ResourceLocation soundId = ResourceLocation.tryParse(config.typewriterSound);

        if (soundId == null) return;

        SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(soundId);

        if (sound == null) return;

        float minPitch = config.typewriterSoundPitchMin;
        float maxPitch = Math.max(minPitch, config.typewriterSoundPitchMax);

        float pitch = minPitch + RANDOM.nextFloat() * (maxPitch - minPitch);

        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                sound,
                SoundSource.PLAYERS,
                config.typewriterSoundVolume,
                pitch
        );
    }

    public static List<PoolOverrideConfig.PoolView> getBestAvailablePools(
        ServerPlayer player,
        ServerMessageConfig config
    ) {
        Map<String, PoolOverrideConfig.PoolView> bestByGroup = new HashMap<>();

        ArrayList<ServerMessageConfig.Pool> allPools = new ArrayList<>();
        allPools.addAll(config.pools);
        allPools.addAll(YapPoolRegistry.getServerPools());

        for (ServerMessageConfig.Pool pool : allPools) {
            PoolOverrideConfig.PoolView view = PoolOverrideConfig.apply(pool);

            if (DefaultPoolDisabler.isGroupDisabled(pool.group)) continue;

            if (!view.enabled) continue;
            if (view.forcedOnly) continue;
            if (!view.matches(player)) continue;
            if (!PoolCooldownManager.canUseNormal(player, view)) continue;

            PoolOverrideConfig.PoolView current = bestByGroup.get(pool.group);

            if (current == null || view.tier > current.tier) {
                bestByGroup.put(pool.group, view);
            }
        }

        return new ArrayList<>(bestByGroup.values());
    }

    private static PoolOverrideConfig.PoolView chooseWeightedPool(
        List<PoolOverrideConfig.PoolView> pools
) {
    int totalWeight = 0;

    for (PoolOverrideConfig.PoolView pool : pools) {
        totalWeight += Math.max(0, pool.weight);
    }

    if (totalWeight <= 0) return null;

    int roll = RANDOM.nextInt(totalWeight);

    for (PoolOverrideConfig.PoolView pool : pools) {
        roll -= Math.max(0, pool.weight);

        if (roll < 0) {
            return pool;
        }
    }

    return null;
}

    public static boolean isSpeaking(ServerPlayer player) {
        return ACTIVE.containsKey(player.getUUID());
    }

    public static boolean canStartDialogue(ServerPlayer player, YapPriority priority) {
        if (player == null || !player.isAlive()) return false;

        ActiveThought current = ACTIVE.get(player.getUUID());

        if (current == null) return true;

        return priority.level >= current.priority.level;
    }

    public static void suppressDialogue(
            ServerPlayer player,
            String key,
            int ticks,
            boolean interruptCurrent
    ) {
        if (player == null) return;
        if (key == null || key.isBlank()) return;

        int duration = Math.max(0, ticks);

        net.minecraft.nbt.CompoundTag suppressions =
                player.getPersistentData().getCompound(SUPPRESSIONS_TAG);

        if (duration <= 0) {
            suppressions.remove(key);
        } else {
            suppressions.putInt(key, duration);
        }

        player.getPersistentData().put(SUPPRESSIONS_TAG, suppressions);

        if (interruptCurrent) {
            ACTIVE.remove(player.getUUID());
            FloatingMessageManager.clear(player);
        }
    }

    public static void clearDialogueSuppression(ServerPlayer player, String key) {
        if (player == null) return;
        if (key == null || key.isBlank()) return;

        net.minecraft.nbt.CompoundTag suppressions =
                player.getPersistentData().getCompound(SUPPRESSIONS_TAG);

        suppressions.remove(key);

        player.getPersistentData().put(SUPPRESSIONS_TAG, suppressions);
    }

    public static boolean isDialogueSuppressed(ServerPlayer player) {
        if (player == null) return true;

        net.minecraft.nbt.CompoundTag suppressions =
                player.getPersistentData().getCompound(SUPPRESSIONS_TAG);

        for (String key : suppressions.getAllKeys()) {
            if (suppressions.getInt(key) > 0) {
                return true;
            }
        }

        return false;
    }

    private static void tickDialogueSuppressions(ServerPlayer player) {
        net.minecraft.nbt.CompoundTag suppressions =
                player.getPersistentData().getCompound(SUPPRESSIONS_TAG);

        if (suppressions.isEmpty()) return;

        java.util.ArrayList<String> toRemove = new java.util.ArrayList<>();

        for (String key : suppressions.getAllKeys()) {
            int ticks = suppressions.getInt(key) - 1;

            if (ticks <= 0) {
                toRemove.add(key);
            } else {
                suppressions.putInt(key, ticks);
            }
        }

        for (String key : toRemove) {
            suppressions.remove(key);
        }

        player.getPersistentData().put(SUPPRESSIONS_TAG, suppressions);
    }

    public static void addSpeechSlowdown(
            ServerPlayer player,
            String key,
            int ticks,
            double multiplier
    ) {
        if (player == null) return;
        if (key == null || key.isBlank()) return;
        if (ticks <= 0) return;
        if (multiplier <= 1.0) return;

        net.minecraft.nbt.CompoundTag slowdowns =
                player.getPersistentData().getCompound(SLOWDOWNS_TAG);

        net.minecraft.nbt.CompoundTag entry =
                new net.minecraft.nbt.CompoundTag();

        entry.putInt("ticks", ticks);
        entry.putInt("maxTicks", ticks);
        entry.putDouble("multiplier", multiplier);

        slowdowns.put(key, entry);

        player.getPersistentData().put(SLOWDOWNS_TAG, slowdowns);
    }

    public static void clearSpeechSlowdown(ServerPlayer player, String key) {
        if (player == null) return;
        if (key == null || key.isBlank()) return;

        net.minecraft.nbt.CompoundTag slowdowns =
                player.getPersistentData().getCompound(SLOWDOWNS_TAG);

        slowdowns.remove(key);

        player.getPersistentData().put(SLOWDOWNS_TAG, slowdowns);
    }

    private static void tickSpeechSlowdowns(ServerPlayer player) {
        net.minecraft.nbt.CompoundTag slowdowns =
                player.getPersistentData().getCompound(SLOWDOWNS_TAG);

        if (slowdowns.isEmpty()) return;

        java.util.ArrayList<String> toRemove = new java.util.ArrayList<>();

        for (String key : slowdowns.getAllKeys()) {
            net.minecraft.nbt.CompoundTag entry = slowdowns.getCompound(key);

            int ticks = entry.getInt("ticks") - 1;

            if (ticks <= 0) {
                toRemove.add(key);
            } else {
                entry.putInt("ticks", ticks);
                slowdowns.put(key, entry);
            }
        }

        for (String key : toRemove) {
            slowdowns.remove(key);
        }

        player.getPersistentData().put(SLOWDOWNS_TAG, slowdowns);
    }

    private static double getSpeechSlowdownMultiplier(ServerPlayer player) {
        net.minecraft.nbt.CompoundTag slowdowns =
                player.getPersistentData().getCompound(SLOWDOWNS_TAG);

        if (slowdowns.isEmpty()) return 1.0;

        double highest = 1.0;

        for (String key : slowdowns.getAllKeys()) {
            net.minecraft.nbt.CompoundTag entry = slowdowns.getCompound(key);

            int ticks = entry.getInt("ticks");
            int maxTicks = Math.max(1, entry.getInt("maxTicks"));
            double multiplier = Math.max(1.0, entry.getDouble("multiplier"));

            if (ticks <= 0) continue;

            double progress = ticks / (double) maxTicks;

            // Starts at multiplier, eases back down to 1.0.
            double currentMultiplier =
                    1.0 + ((multiplier - 1.0) * progress);

            if (currentMultiplier > highest) {
                highest = currentMultiplier;
            }
        }

        return highest;
    }

    private static PoolOverrideConfig.PoolView getPoolViewById(String poolId) {
        if (poolId == null || poolId.isBlank()) return null;

        ServerMessageConfig config = ServerMessageConfig.get();

        ArrayList<ServerMessageConfig.Pool> allPools = new ArrayList<>();
        allPools.addAll(config.pools);
        allPools.addAll(YapPoolRegistry.getServerPools());

        for (ServerMessageConfig.Pool pool : allPools) {
            if (pool == null || pool.id == null) continue;
            if (!pool.id.equals(poolId)) continue;

            return PoolOverrideConfig.apply(pool);
        }

        return null;
    }
}