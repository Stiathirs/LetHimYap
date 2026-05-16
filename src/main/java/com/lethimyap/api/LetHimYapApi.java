package com.lethimyap.api;

import com.lethimyap.messages.PlayerMessageManager;
import com.lethimyap.messages.ServerMessageConfig;
import net.minecraft.server.level.ServerPlayer;

/**
 * Public API for Let Him Yap.
 *
 * Addon/compat mods should use this class instead of directly touching
 * Let Him Yap internals.
 *
 * General concepts:
 *
 * Pool:
 * A named dialogue category, such as "health.light", "event.fear_yelp",
 * or "mymod.corruption.low".
 *
 * Pool ID:
 * The string ID used by the server to request a dialogue line from the client.
 * The client looks up this same ID in client_dialogue.toml.
 *
 * Important:
 * Important dialogue can be saved as last words if completed or interrupted.
 * Pain barks are usually not important.
 *
 * Pain:
 * Pain dialogue uses pain interruption behavior and priority rules.
 * It is intended for damage, fear, panic, or sudden danger reactions.
 *
 * Priority:
 * Determines whether a new dialogue can interrupt the currently active one.
 *
 * NORMAL   - random ambient/status dialogue
 * EVENT    - forced events like eating, waking, drowning warnings
 * PAIN     - damage/fear reactions
 * CRITICAL - addon-controlled emergency override
 */
public class LetHimYapApi {

    /**
     * Forces the client to pick a line from a dialogue pool.
     *
     * This uses default priority behavior:
     * - pain=false -> EVENT
     * - pain=true  -> PAIN
     *
     * Use this for most simple forced dialogue.
     */
    public static void forceDialoguePool(
            ServerPlayer player,
            String poolId,
            boolean important,
            boolean pain
    ) {
        PlayerMessageManager.forcePool(
                player,
                poolId,
                important,
                pain,
                pain ? YapPriority.PAIN : YapPriority.EVENT
        );
    }

    /**
     * Forces the client to pick a line from a dialogue pool with explicit priority.
     *
     * Use this when your addon needs control over interruption behavior.
     *
     * Example:
     * YapPriority.CRITICAL can interrupt active pain dialogue.
     */
    public static void forceDialoguePool(
            ServerPlayer player,
            String poolId,
            boolean important,
            boolean pain,
            YapPriority priority
    ) {
        PlayerMessageManager.forcePool(
                player,
                poolId,
                important,
                pain,
                priority
        );
    }

    /**
     * Forces a specific speech line directly.
     *
     * This bypasses client-side pool selection.
     *
     * Prefer forceDialoguePool(...) when possible so players can customize
     * their own lines through client_dialogue.toml.
     */
    public static void forceSpeech(
            ServerPlayer player,
            String message,
            boolean important,
            boolean pain
    ) {
        PlayerMessageManager.forceSpeechWithPriority(
                player,
                message,
                important,
                pain,
                pain ? YapPriority.PAIN : YapPriority.EVENT
        );
    }

    /**
     * Forces a specific speech line directly with explicit priority.
     *
     * This is useful for scripted moments where the exact line matters.
     */
    public static void forceSpeech(
            ServerPlayer player,
            String message,
            boolean important,
            boolean pain,
            YapPriority priority
    ) {
        PlayerMessageManager.forceSpeechWithPriority(
                player,
                message,
                important,
                pain,
                priority
        );
    }

    /**
     * Registers a complete server-side dialogue pool.
     *
     * Server pools define:
     * - pool ID
     * - group
     * - tier
     * - weight
     * - condition
     * - important/forcedOnly behavior
     *
     * This does not add client-side dialogue lines by itself.
     * Pair it with registerClientDefaultLines(...).
     */
    public static void registerServerPool(ServerMessageConfig.Pool pool) {
        YapPoolRegistry.registerServerPool(pool);
    }

    /**
     * Adds default client-side dialogue lines for a pool.
     *
     * These are written to client_dialogue.toml if that pool does not already
     * have user-configured messages.
     *
     * Multiple mods can contribute lines to the same pool ID.
     */
    public static void registerClientDefaultLines(String poolId, String... lines) {
        YapPoolRegistry.registerClientDefaultLines(poolId, lines);
    }

    /**
     * Convenience helper for registering an NBT-number condition pool.
     *
     * The pool becomes eligible when the NBT value matches the comparison.
     *
     * Supported compare values:
     * - "below"
     * - "above"
     * - "equal"
     * - "not_equal"
     *
     * Example path:
     * "Root.ForgeCaps.thirst:thirst.thirst"
     * "ForgeData.mymod.some_value"
     */
    public static void registerNbtNumberPool(
            String id,
            String group,
            int tier,
            int weight,
            boolean important,
            boolean forcedOnly,
            String nbtPath,
            String compare,
            float value
    ) {
        YapPoolRegistry.registerServerPool(
                ServerMessageConfig.Pool.nbtNumber(
                        id,
                        group,
                        tier,
                        weight,
                        important,
                        forcedOnly,
                        nbtPath,
                        compare,
                        value
                )
        );
    }

    /**
     * Registers or updates a damage reaction pool.
     *
     * If the pool ID does not exist, it is created.
     * If the pool ID already exists, its minDamage/chance/important values
     * are updated.
     *
     * Add client-side lines with registerClientDefaultLines(...).
     */
    public static void registerOrMergeDamagePool(
            String id,
            float minDamage,
            double chance,
            boolean important
    ) {
        YapPoolRegistry.registerOrMergeDamagePool(
                id,
                minDamage,
                chance,
                important
        );
    }

    /**
     * Adds damage type exclusions to a damage reaction pool.
     *
     * Excluded damage types will never trigger this specific pool.
     *
     * Example:
     * "minecraft:fall"
     * "minecraft:starve"
     */
    public static void addDamagePoolExclusions(
            String id,
            String... damageTypes
    ) {
        YapPoolRegistry.addDamagePoolExclusions(id, damageTypes);
    }

    /**
     * Adds damage type exclusives to a damage reaction pool.
     *
     * If a pool has exclusive damage types, it can only trigger for those types.
     *
     * Example:
     * A fire pool may exclusively allow:
     * - "minecraft:in_fire"
     * - "minecraft:on_fire"
     * - "minecraft:lava"
     */
    public static void addDamagePoolExclusives(
            String id,
            String... damageTypes
    ) {
        YapPoolRegistry.addDamagePoolExclusives(id, damageTypes);
    }

    /**
     * Adds damage types to the configurable global damage blacklist.
     *
     * Blacklisted damage types never trigger generic damage reaction dialogue.
     *
     * These values are written into server_pool_overrides.toml so server owners
     * can remove or edit them.
     *
     * Use this when exclusion is recommended but not absolutely required.
     */
    public static void addGlobalDamageBlacklist(String... damageTypes) {
        YapPoolRegistry.addGlobalDamageBlacklist(damageTypes);
    }

    /**
     * Suppresses dialogue for this player under a unique addon-owned key.
     *
     * Use a namespaced key such as:
     * - "mymod:unconscious"
     * - "mymod:stunned"
     * - "mymod:cutscene"
     *
     * If multiple mods suppress dialogue at the same time, dialogue remains blocked
     * until all active suppression keys expire or are cleared.
     */
    public static void suppressDialogue(
            ServerPlayer player,
            String key,
            int ticks,
            boolean interruptCurrent
    ) {
        PlayerMessageManager.suppressDialogue(
                player,
                key,
                ticks,
                interruptCurrent
        );
    }

    /**
     * Clears a specific dialogue suppression key.
     */
    public static void clearDialogueSuppression(
            ServerPlayer player,
            String key
    ) {
        PlayerMessageManager.clearDialogueSuppression(player, key);
    }

    /**
     * Returns true if any active suppression key is currently blocking dialogue.
     */
    public static boolean isDialogueSuppressed(ServerPlayer player) {
        return PlayerMessageManager.isDialogueSuppressed(player);
    }

    /**
     * Applies a temporary keyed speech slowdown.
     *
     * The multiplier must be above 1.0.
     * It starts at the supplied multiplier and gradually returns to 1.0
     * over the supplied duration.
     *
     * Multiple active slowdowns can exist at once. The highest current multiplier wins.
     *
     * Use namespaced keys like:
     * - "mymod:waking_up"
     * - "mymod:stunned"
     * - "mymod:drugged"
     */
    public static void addSpeechSlowdown(
            ServerPlayer player,
            String key,
            int ticks,
            double multiplier
    ) {
        PlayerMessageManager.addSpeechSlowdown(
                player,
                key,
                ticks,
                multiplier
        );
    }

    /**
     * Clears a specific keyed speech slowdown.
     */
    public static void clearSpeechSlowdown(
            ServerPlayer player,
            String key
    ) {
        PlayerMessageManager.clearSpeechSlowdown(player, key);
    }

    /**
     * Adds adrenaline to the player.
     *
     * Adrenaline reduces typewriter slowdown temporarily.
     */
    public static void addAdrenaline(ServerPlayer player, double amount) {
        com.lethimyap.messages.AdrenalineManager.addAdrenaline(player, amount);
    }

    /**
     * Removes adrenaline from the player.
     */
    public static void removeAdrenaline(ServerPlayer player, double amount) {
        com.lethimyap.messages.AdrenalineManager.removeAdrenaline(player, amount);
    }

    /**
     * Sets adrenaline directly.
     */
    public static void setAdrenaline(ServerPlayer player, double amount) {
        com.lethimyap.messages.AdrenalineManager.setAdrenaline(player, amount);
    }

    /**
     * Adds damage types to the hard global damage blacklist.
     *
     * Hard-blacklisted damage types never trigger generic damage reaction dialogue
     * and are NOT written to server_pool_overrides.toml.
     *
     * Use this only when allowing the damage type to trigger generic pain dialogue
     * would break the behavior of your addon.
     */
    public static void addHardGlobalDamageBlacklist(String... damageTypes) {
        YapPoolRegistry.addHardGlobalDamageBlacklist(damageTypes);
    }

    /**
     * Returns the configured fear event radius.
     *
     * Addons can use this to match Let Him Yap's built-in explosion/lightning
     * fear behavior.
     */
    public static double getFearEventRadius() {
        return ServerMessageConfig.get().fearEventRadius;
    }

    /**
     * Returns the configured cooldown for fear events.
     *
     * This does not automatically apply cooldown logic to your addon.
     * It simply exposes the configured value.
     */
    public static int getFearEventCooldownTicks() {
        return ServerMessageConfig.get().fearEventCooldownTicks;
    }

    /**
     * Returns true if Let Him Yap's main dialogue system is enabled.
     */
    public static boolean isEnabled() {
        return ServerMessageConfig.get().enabled;
    }

    /**
     * Returns true if event dialogue is enabled.
     */
    public static boolean isEventDialoguesEnabled() {
        return ServerMessageConfig.get().eventDialoguesEnabled;
    }

    /**
     * Returns true if damage reaction dialogue is enabled.
     */
    public static boolean isDamageReactionsEnabled() {
        return ServerMessageConfig.get().damageReactionsEnabled;
    }

    /**
     * Returns true if insomnia warning dialogue is enabled.
     */
    public static boolean isInsomniaWarningsEnabled() {
        return ServerMessageConfig.get().insomniaWarningsEnabled;
    }

    /**
     * Returns true if the player currently has active dialogue.
     */
    public static boolean isPlayerCurrentlySpeaking(ServerPlayer player) {
        return PlayerMessageManager.isSpeaking(player);
    }

    /**
     * Returns whether a dialogue of the supplied priority would be allowed
     * to start for this player.
     *
     * This follows the same interruption rules as the actual dialogue system.
     */
    public static boolean canPlayerStartDialogue(
            ServerPlayer player,
            YapPriority priority
    ) {
        return PlayerMessageManager.canStartDialogue(player, priority);
    }
}