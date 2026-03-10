package com.bada774.fet.recipe;

import javax.annotation.Nonnull;

import crafttweaker.CraftTweakerAPI;
import crazypants.enderio.base.recipe.IRecipeInput;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

public class RecipeInput implements IRecipeInput {

	protected final Ingredient ing;

	public RecipeInput(Ingredient ing) {
		this.ing = ing;
		if (ing.getMatchingStacks().length == 0) {
			CraftTweakerAPI.logInfo("FET received an empty ingredient\nThis may cause problems");
			CraftTweakerAPI.logInfo(ing.toString());
		}
	}

	@Nonnull
	@Override
	public IRecipeInput copy() {
		return new RecipeInput(ing);
	}

	@Override
	public boolean isFluid() {
		return false;
	}

	@Nonnull
	@Override
	public ItemStack getInput() {
		return ing.getMatchingStacks().length == 0 ? ItemStack.EMPTY : ing.getMatchingStacks()[0].copy();
	}

	@Override
	public FluidStack getFluidInput() {
		return null;
	}

	@Override
	public float getMulitplier() {
		return 0;
	}

	@Override
	public int getSlotNumber() {
		return -1;
	}

	@Override
	public boolean isInput(@Nonnull ItemStack test) {
		return ing.apply(test);
	}

	@Override
	public boolean isInput(FluidStack test) {
		return false;
	}

	@Override
	public ItemStack[] getEquivelentInputs() {
		ItemStack[] org = ing.getMatchingStacks();
		ItemStack[] copy = new ItemStack[org.length];
		for (int i = 0; i < copy.length; i++)
			copy[i] = org[i].copy();
		return copy;
	}

	@Override
	public boolean isValid() {
		return ing != null && ing.getMatchingStacks().length > 0;
	}

	@Override
	public void shrinkStack(int count) {
	}

	@Override
	public int getStackSize() {
		return ing.getMatchingStacks().length == 0 ? 0 : ing.getMatchingStacks()[0].getCount();
	}

}
