package com.lethimyap.effect;

import com.lethimyap.api.LetHimYapApi;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class SilencedEffect extends MobEffect {

    public SilencedEffect() {
        super(MobEffectCategory.HARMFUL, 0x777777);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayer player) {
            LetHimYapApi.suppressDialogue(
                    player,
                    "lethimyap:silenced",
                    5,
                    true
            );
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}