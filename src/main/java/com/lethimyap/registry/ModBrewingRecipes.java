package com.lethimyap.registry;

import com.lethimyap.LetHimYap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(
        modid = LetHimYap.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public class ModBrewingRecipes {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BrewingRecipeRegistry.addRecipe(
                    StrictNBTIngredient.of(
                            PotionUtils.setPotion(
                                    new ItemStack(Items.POTION),
                                    Potions.AWKWARD
                            )
                    ),
                    net.minecraft.world.item.crafting.Ingredient.of(Items.SLIME_BALL),
                    PotionUtils.setPotion(
                            new ItemStack(Items.POTION),
                            ModPotions.SILENCE.get()
                    )
            );

            BrewingRecipeRegistry.addRecipe(
                    StrictNBTIngredient.of(
                            PotionUtils.setPotion(
                                    new ItemStack(Items.POTION),
                                    ModPotions.SILENCE.get()
                            )
                    ),
                    net.minecraft.world.item.crafting.Ingredient.of(Items.REDSTONE),
                    PotionUtils.setPotion(
                            new ItemStack(Items.POTION),
                            ModPotions.LONG_SILENCE.get()
                    )
            );
        });
    }
}