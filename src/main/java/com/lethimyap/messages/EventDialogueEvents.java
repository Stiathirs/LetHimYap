package com.lethimyap.messages;

import com.lethimyap.LetHimYap;
import com.lethimyap.api.LetHimYapApi;
import com.lethimyap.api.YapPriority;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = LetHimYap.MODID)
public class EventDialogueEvents {

    private static final Random RANDOM = new Random();

    private static final String INSOMNIA_WARNING_COOLDOWN =
            "lethimyap_insomnia_warning_cooldown";

    public static final String FEAR_COOLDOWN =
            "lethimyap_fear_cooldown";

    private static final String SLEEP_START_TIME =
            "lethimyap_sleep_start_time";

    private static final String SLEEP_LAST_GAME_TIME =
            "lethimyap_sleep_last_game_time";

    private static final String SLEEP_EXTRA_TICKS =
            "lethimyap_sleep_extra_ticks";

    private static final String WAKE_UP_SLOWDOWN_KEY =
            "lethimyap:waking_up";

    @SubscribeEvent
    public static void onSleepInBed(PlayerSleepInBedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        player.getPersistentData().putLong(
                SLEEP_START_TIME,
                player.level().getGameTime()
        );
    }

    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ServerMessageConfig config = ServerMessageConfig.get();
        if (!config.eventDialoguesEnabled) return;

        player.getPersistentData().putInt(INSOMNIA_WARNING_COOLDOWN, 0);

        boolean successfulSleep = !event.updateLevel();

        if (successfulSleep) {
            long sleepStart = player.getPersistentData().getLong(SLEEP_START_TIME);
            long now = player.level().getGameTime();

            if (sleepStart > 0 && now > sleepStart) {
                long sleptTicks = now - sleepStart;

                double adrenalineLost =
                        sleptTicks * config.adrenalineDecayPerTick;

                LetHimYapApi.removeAdrenaline(player, adrenalineLost);
            }

            LetHimYapApi.addSpeechSlowdown(
                    player,
                    WAKE_UP_SLOWDOWN_KEY,
                    100,
                    3.0
            );

            PlayerMessageManager.forcePool(
                    player,
                    "event.wake_up",
                    true,
                    false,
                    YapPriority.EVENT
            );
        } else {
            PlayerMessageManager.forcePool(
                    player,
                    "event.sleep_interrupted",
                    false,
                    false,
                    YapPriority.EVENT
            );
        }

        player.getPersistentData().remove(SLEEP_START_TIME);
    }

    @SubscribeEvent
    public static void onEatFood(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack stack = event.getItem();
        FoodProperties food = stack.getFoodProperties(player);

        if (food == null) return;

        ServerMessageConfig config = ServerMessageConfig.get();
        if (!config.eventDialoguesEnabled) return;

        FoodQuality quality = getFoodQuality(food);

        switch (quality) {
            case GOOD -> PlayerMessageManager.forcePool(
                    player,
                    "event.eat_good",
                    true,
                    false,
                    YapPriority.EVENT
            );

            case BAD -> PlayerMessageManager.forcePool(
                    player,
                    "event.eat_bad",
                    true,
                    false,
                    YapPriority.EVENT
            );

            case NEUTRAL -> {
                if (RANDOM.nextDouble() <= config.neutralFoodDialogueChance) {
                    PlayerMessageManager.forcePool(
                            player,
                            "event.eat_neutral",
                            true,
                            false,
                            YapPriority.EVENT
                    );
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        ServerMessageConfig config = ServerMessageConfig.get();
        if (!config.eventDialoguesEnabled) return;

        trackSleepingTimeSkip(player);

        tickFearCooldown(player);

        if (!config.insomniaWarningsEnabled) return;

        int timeSinceRest = player.getStats()
                .getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));

        if (timeSinceRest < config.insomniaWarningTicks) {
            player.getPersistentData().putInt(INSOMNIA_WARNING_COOLDOWN, 0);
            return;
        }

        int cooldown = player.getPersistentData().getInt(INSOMNIA_WARNING_COOLDOWN);

        if (cooldown > 0) {
            player.getPersistentData().putInt(INSOMNIA_WARNING_COOLDOWN, cooldown - 1);
            return;
        }

        PlayerMessageManager.forcePool(
                player,
                "event.insomnia_warning",
                true,
                false,
                YapPriority.EVENT
        );

        player.getPersistentData().putInt(
                INSOMNIA_WARNING_COOLDOWN,
                Math.max(1, config.insomniaRepeatTicks)
        );
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        ServerMessageConfig config = ServerMessageConfig.get();
        if (!config.eventDialoguesEnabled) return;

        double radiusSqr = config.fearEventRadius * config.fearEventRadius;

        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(event.getExplosion().getPosition()) <= radiusSqr) {
                tryFearYelp(player, config);
            }
        }
    }

    @SubscribeEvent
    public static void onLightning(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getEntity() instanceof LightningBolt lightning)) return;

        ServerMessageConfig config = ServerMessageConfig.get();
        if (!config.eventDialoguesEnabled) return;

        double radiusSqr = config.fearEventRadius * config.fearEventRadius;

        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(lightning) <= radiusSqr) {
                tryFearYelp(player, config);
            }
        }
    }

    private static void tickFearCooldown(ServerPlayer player) {
        int fearCooldown = player.getPersistentData().getInt(FEAR_COOLDOWN);

        if (fearCooldown > 0) {
            player.getPersistentData().putInt(FEAR_COOLDOWN, fearCooldown - 1);
        }
    }

    private static void tryFearYelp(ServerPlayer player, ServerMessageConfig config) {
        if (isFearOnCooldown(player)) return;

        PlayerMessageManager.forcePool(
                player,
                "event.fear_yelp",
                true,
                true,
                YapPriority.PAIN
        );

        triggerFearCooldown(player);
    }

    private static FoodQuality getFoodQuality(FoodProperties food) {
        boolean hasPositive = false;
        boolean hasNegative = false;

        for (Pair<MobEffectInstance, Float> pair : food.getEffects()) {
            MobEffectInstance effect = pair.getFirst();

            if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                hasNegative = true;
            } else if (effect.getEffect().getCategory() == MobEffectCategory.BENEFICIAL) {
                hasPositive = true;
            }
        }

        if (hasNegative) return FoodQuality.BAD;
        if (hasPositive || food.getNutrition() >= 6) return FoodQuality.GOOD;

        return FoodQuality.NEUTRAL;
    }

    private static void trackSleepingTimeSkip(ServerPlayer player) {
        if (!player.isSleeping()) {
            player.getPersistentData().remove(SLEEP_LAST_GAME_TIME);
            return;
        }

        long now = player.level().getGameTime();

        if (!player.getPersistentData().contains(SLEEP_LAST_GAME_TIME)) {
            player.getPersistentData().putLong(SLEEP_LAST_GAME_TIME, now);
            return;
        }

        long last = player.getPersistentData().getLong(SLEEP_LAST_GAME_TIME);
        long delta = now - last;

        player.getPersistentData().putLong(SLEEP_LAST_GAME_TIME, now);

        if (delta > 1) {
            long extra = delta - 1;

            long currentExtra =
                    player.getPersistentData().getLong(SLEEP_EXTRA_TICKS);

            player.getPersistentData().putLong(
                    SLEEP_EXTRA_TICKS,
                    currentExtra + extra
            );
        }
    }

    public static int getFearCooldown(ServerPlayer player) {
        if (player == null) return 0;

        return Math.max(
                0,
                player.getPersistentData().getInt(FEAR_COOLDOWN)
        );
    }

    public static boolean isFearOnCooldown(ServerPlayer player) {
        return getFearCooldown(player) > 0;
    }

    public static void triggerFearCooldown(ServerPlayer player) {
        if (player == null) return;

        ServerMessageConfig config = ServerMessageConfig.get();

        player.getPersistentData().putInt(
                FEAR_COOLDOWN,
                Math.max(1, config.fearEventCooldownTicks)
        );
    }

    private enum FoodQuality {
        GOOD,
        BAD,
        NEUTRAL
    }
}