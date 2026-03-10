package com.bada774.fet.utils;

import crazypants.enderio.base.recipe.IRecipe;
import crazypants.enderio.base.recipe.IRecipeInput;
import crazypants.enderio.base.recipe.vat.VatRecipeManager;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VatRecipeUtils {

    public static void removeRecipeSafely(IRecipe targetRecipe) {
        if (targetRecipe == null)
            return;

        boolean removed = VatRecipeManager.getInstance().getRecipes().remove(targetRecipe);

        if (!removed) {
            Iterator<IRecipe> iterator = VatRecipeManager.getInstance().getRecipes().iterator();
            while (iterator.hasNext()) {
                IRecipe current = iterator.next();
                if (isDuplicate(current, targetRecipe)) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    public static boolean isDuplicate(IRecipe existing, IRecipe target) {
        if (existing == target)
            return true;
        if (existing == null || target == null)
            return false;

        IRecipeInput exFluid = null, targetFluid = null;
        List<IRecipeInput> exSlot0 = new ArrayList<>();
        List<IRecipeInput> exSlot1 = new ArrayList<>();
        List<IRecipeInput> targetSlot0 = new ArrayList<>();
        List<IRecipeInput> targetSlot1 = new ArrayList<>();

        for (IRecipeInput in : existing.getInputs()) {
            if (in == null)
                continue;
            if (in.isFluid())
                exFluid = in;
            else if (in.getSlotNumber() == 0)
                exSlot0.add(in);
            else if (in.getSlotNumber() == 1)
                exSlot1.add(in);
        }

        for (IRecipeInput in : target.getInputs()) {
            if (in == null)
                continue;
            if (in.isFluid())
                targetFluid = in;
            else if (in.getSlotNumber() == 0)
                targetSlot0.add(in);
            else if (in.getSlotNumber() == 1)
                targetSlot1.add(in);
        }

        if (exFluid == null || targetFluid == null)
            return false;
        if (exFluid.getFluidInput() == null || targetFluid.getFluidInput() == null)
            return false;
        if (!exFluid.getFluidInput().isFluidEqual(targetFluid.getFluidInput()))
            return false;

        FluidStack exOut = getFluidOutput(existing);
        FluidStack targetOut = getFluidOutput(target);
        if (exOut == null || targetOut == null || !exOut.isFluidEqual(targetOut))
            return false;

        return compareInputLists(exSlot0, targetSlot0) && compareInputLists(exSlot1, targetSlot1);
    }

    private static FluidStack getFluidOutput(IRecipe recipe) {
        if (recipe.getOutputs() != null && recipe.getOutputs().length > 0 && recipe.getOutputs()[0] != null) {
            return recipe.getOutputs()[0].getFluidOutput();
        }
        return null;
    }

    private static boolean compareInputLists(List<IRecipeInput> list1, List<IRecipeInput> list2) {
        if (list1.isEmpty() && list2.isEmpty())
            return true;
        if (list1.isEmpty() || list2.isEmpty())
            return false;

        for (IRecipeInput item1 : list1) {
            boolean foundMatch = false;
            for (IRecipeInput item2 : list2) {
                if (item1.getInput() != null && item2.getInput() != null) {
                    if (OreDictionary.itemMatches(item1.getInput(), item2.getInput(), false)) {
                        foundMatch = true;
                        break;
                    }
                }
            }
            if (!foundMatch) {
                return false;
            }
        }
        return true;
    }
}