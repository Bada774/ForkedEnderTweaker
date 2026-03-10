package com.bada774.fet.recipe.machines;

import java.util.Map;

import crazypants.enderio.base.recipe.IMachineRecipe;
import crazypants.enderio.base.recipe.MachineRecipeRegistry;
import crazypants.enderio.base.recipe.enchanter.EnchanterRecipe;
import net.minecraft.enchantment.Enchantment;

public class EnchanterRecipes {
    public static EnchanterRecipe getRecipeByEnchantment(Enchantment enchantment) {
        if (enchantment == null)
            return null;

        Map<String, ? extends IMachineRecipe> recipes = MachineRecipeRegistry.instance
                .getRecipesForMachine(MachineRecipeRegistry.ENCHANTER);

        for (IMachineRecipe recipe : recipes.values()) {
            if (recipe instanceof EnchanterRecipe) {
                EnchanterRecipe enchanterRecipe = (EnchanterRecipe) recipe;

                if (enchanterRecipe.getEnchantment() == enchantment) {
                    return enchanterRecipe;
                }
            }
        }
        return null;

    }

}
