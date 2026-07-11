package com.lethimyap.messages;

import com.lethimyap.LetHimYap;
import com.lethimyap.api.YapPoolRegistry;
import com.lethimyap.api.YapPriority;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = LetHimYap.MODID)
public class DamageReactionEvents {

    private static final Random RANDOM = new Random();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        float damage = event.getAmount();

        // Adrenaline should respond to damage independently of whether
        // damage dialogue is enabled, blacklisted, or randomly selected.
        AdrenalineManager.addFromDamage(player, damage);

        ServerMessageConfig config = ServerMessageConfig.get();
        if (!config.damageReactionsEnabled) return;

        PoolOverrideConfig.DamagePoolView view =
                getPool(config, event, damage);

        if (view == null) return;
        if (RANDOM.nextDouble() > view.chance) return;

        PlayerMessageManager.forcePool(
                player,
                view.pool.id,
                view.important,
                true,
                YapPriority.PAIN
        );
    }

    private static PoolOverrideConfig.DamagePoolView getPool(
            ServerMessageConfig config,
            LivingDamageEvent event,
            float damage
    ) {
        PoolOverrideConfig.DamagePoolView bestExclusive = null;
        PoolOverrideConfig.DamagePoolView bestGeneric = null;

        ArrayList<ServerMessageConfig.DamagePool> allPools = new ArrayList<>();
        allPools.addAll(config.damagePools);
        allPools.addAll(YapPoolRegistry.getDamagePools());

        for (ServerMessageConfig.DamagePool pool : allPools) {
            if (pool == null) continue;
            if (pool.id == null || pool.id.isBlank()) continue;

            PoolOverrideConfig.DamagePoolView view =
                    PoolOverrideConfig.applyDamagePool(pool);

            if (view == null) continue;
            if (!view.enabled) continue;
            if (damage < view.minDamage) continue;
            if (!damageTypeMatches(view, event)) continue;

            boolean exclusive = !view.exclusiveDamageTypes.isEmpty();

            if (exclusive) {
                if (bestExclusive == null || view.minDamage > bestExclusive.minDamage) {
                    bestExclusive = view;
                }
            } else {
                if (bestGeneric == null || view.minDamage > bestGeneric.minDamage) {
                    bestGeneric = view;
                }
            }
        }

        if (bestExclusive != null) {
            return bestExclusive;
        }

        if (isGloballyBlacklisted(event)) {
            return null;
        }

        return bestGeneric;
    }

    private static boolean isGloballyBlacklisted(LivingDamageEvent event) {
        String damageType = getDamageTypeId(event);

        return PoolOverrideConfig.isDamageTypeGloballyBlacklisted(damageType);
    }

    private static boolean damageTypeMatches(
            PoolOverrideConfig.DamagePoolView view,
            LivingDamageEvent event
    ) {
        String damageType = getDamageTypeId(event);

        if (contains(view.excludedDamageTypes, damageType)) {
            return false;
        }

        if (!view.exclusiveDamageTypes.isEmpty()
                && !contains(view.exclusiveDamageTypes, damageType)) {
            return false;
        }

        return true;
    }

    private static String getDamageTypeId(LivingDamageEvent event) {
        return event.getSource()
                .typeHolder()
                .unwrapKey()
                .map(key -> key.location().toString())
                .orElse("");
    }

    private static boolean contains(List<String> list, String value) {
        for (String entry : list) {
            if (entry.equals(value)) {
                return true;
            }
        }

        return false;
    }
}