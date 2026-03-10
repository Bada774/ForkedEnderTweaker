package com.bada774.fet.utils;

import java.util.ArrayList;
import java.util.List;

import com.enderio.core.common.util.NNList;

import com.bada774.fet.recipe.RecipeInput;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.item.WeightedItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crazypants.enderio.base.recipe.IRecipeInput;
import crazypants.enderio.base.recipe.RecipeOutput;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.fluids.FluidStack;

public class RecipeUtils {
	public static IRecipeInput[] toEIOInputs(IIngredient[] inputs) {
		IRecipeInput[] ret = new IRecipeInput[inputs.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = toInput(inputs[i]);
		}
		return ret;
	}

	public static IRecipeInput toEIOInput(IIngredient ingredient) {
		if (ingredient == null)
			return null;
		NNList<IRecipeInput> list = toEIOInputsNN(new IIngredient[] { ingredient });
		return list.isEmpty() ? null : list.get(0);
	}

	public static NNList<IRecipeInput> toEIOInputsNN(IIngredient[] inputs) {
		NNList<IRecipeInput> ret = new NNList<>();
		for (IIngredient input : inputs) {
			ret.add(toInput(input));
		}
		return ret;
	}

	public static NNList<IRecipeInput> toEIOInputsNN(IRecipeInput[] inputs) {
		NNList<IRecipeInput> ret = new NNList<>();
		for (IRecipeInput input : inputs) {
			ret.add(input);
		}
		return ret;
	}

	public static RecipeOutput[] toEIOOutputs(IItemStack[] inputs, float[] chances, float[] xp) {
		RecipeOutput[] ret = new RecipeOutput[inputs.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = new RecipeOutput(CraftTweakerMC.getItemStack(inputs[i]), chances[i], xp[i]);
		}
		return ret;
	}

	public static RecipeOutput[] toEIOOutputs(WeightedItemStack[] inputs, float[] xp) {
		RecipeOutput[] ret = new RecipeOutput[inputs.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = new RecipeOutput(CraftTweakerMC.getItemStack(inputs[i].getStack()), inputs[i].getChance(), xp[i]);
		}
		return ret;
	}

	public static RecipeInput toInput(IIngredient ing) {
		return new RecipeInput(CraftTweakerMC.getIngredient(ing));
	}

	public static String getDisplayString(IIngredient... ings) {
		if (ings == null)
			return "null";
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < ings.length; i++) {
			if (i > 0)
				sb.append(", ");
			sb.append(ings[i] == null ? "null" : ings[i].toCommandString());
		}
		return sb.append("]").toString();
	}

	public static String getDisplayString(WeightedItemStack... ings) {
		if (ings == null)
			return "null";
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < ings.length; i++) {
			if (i > 0)
				sb.append(", ");
			if (ings[i] != null) {
				sb.append(ings[i].getStack().toCommandString())
						.append(" % ")
						.append(ings[i].getPercent());
			} else {
				sb.append("null");
			}
		}
		return sb.append("]").toString();
	}

	public static String getDisplayString(IItemStack... items) {
		if (items == null)
			return "null";
		StringBuilder sb = new StringBuilder("[");
		boolean first = true;
		for (IItemStack item : items) {
			if (item != null) {
				if (!first)
					sb.append(", ");
				sb.append(item.getDisplayName());
				first = false;
			}
		}
		return sb.append("]").toString();
	}

	public static boolean areInputsMatch(NNList<IRecipeInput> recipeInputs, NNList<IRecipeInput> targetInputs) {
		if (recipeInputs == null && targetInputs == null)
			return true;
		if (recipeInputs == null || targetInputs == null)
			return false;
		if (recipeInputs.size() != targetInputs.size())
			return false;

		List<ItemStack> rStacks = new ArrayList<>();
		for (IRecipeInput i : recipeInputs)
			rStacks.add(i.getInput());

		List<ItemStack> tStacks = new ArrayList<>();
		for (IRecipeInput i : targetInputs)
			tStacks.add(i.getInput());

		for (ItemStack t : tStacks) {
			boolean found = false;
			for (int i = 0; i < rStacks.size(); i++) {
				if (OreDictionary.itemMatches(t, rStacks.get(i), false)) {
					rStacks.remove(i);
					found = true;
					break;
				}
			}
			if (!found)
				return false;
		}
		return true;
	}

	public static String getConflictingOutputName(ItemStack stack) {
		if (stack == null || stack.isEmpty())
			return "Unknown Item";
		IItemStack ctStack = CraftTweakerMC.getIItemStack(stack);
		return getConflictingOutputName(ctStack);
	}

	public static String getConflictingOutputName(IItemStack ctStack) {
		if (ctStack == null || ctStack.isEmpty())
			return "Unknown Item";
		return ctStack.getDisplayName() + " (" + ctStack.toCommandString() + ")";
	}

	public static String getConflictingOutputName(FluidStack fluid) {
		if (fluid == null)
			return "Unknown Fluid";
		ILiquidStack ctLiquid = CraftTweakerMC.getILiquidStack(fluid);
		return ctLiquid.getDisplayName() + " (" + ctLiquid.toCommandString() + ")";
	}
}
