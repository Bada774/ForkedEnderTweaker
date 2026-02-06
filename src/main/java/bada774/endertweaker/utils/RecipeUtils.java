package bada774.endertweaker.utils;

import java.util.ArrayList;
import java.util.List;

import bada774.endertweaker.recipe.RecipeInput;

import com.enderio.core.common.util.NNList;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.item.WeightedItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crazypants.enderio.base.recipe.IRecipeInput;
import crazypants.enderio.base.recipe.RecipeOutput;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class RecipeUtils {
	public static IRecipeInput[] toEIOInputs(IIngredient[] inputs) {
		IRecipeInput[] ret = new IRecipeInput[inputs.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = toInput(inputs[i]);
		}
		return ret;
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
		StringBuilder sb = new StringBuilder("[");
		for (IIngredient i : ings)
			sb.append(i == null ? i : i.toCommandString() + ",");
		sb.replace(sb.length() - 1, sb.length(), "");
		return sb.append("]").toString();
	}

	public static String getDisplayString(WeightedItemStack... ings) {
		StringBuilder sb = new StringBuilder("[");
		for (WeightedItemStack i : ings)
			sb.append(i == null ? i : i.getStack().toCommandString() + " % " + i.getPercent() + ",");
		sb.replace(sb.length() - 1, sb.length(), "");
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
}
