package bada774.fet.recipe.machines;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crazypants.enderio.base.recipe.IRecipeInput;
import crazypants.enderio.base.recipe.RecipeInput;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class VatRecipeInput extends RecipeInput {

	private final IIngredient ingredient;

	public VatRecipeInput(IIngredient ingredient, int slot, float mult) {
		super(getItemStack(ingredient), false, mult, slot);
		this.ingredient = ingredient;
	}

	public static IRecipeInput[] createInputsArray(FluidStack fluid, float fluidMult,
			IIngredient[] slot1, float[] mult1, IIngredient[] slot2, float[] mult2) {

		IRecipeInput[] inArray = new IRecipeInput[1 + slot1.length + slot2.length];
		int x = 0;

		for (int i = 0; i < slot1.length; i++) {
			inArray[x++] = new VatRecipeInput(slot1[i], 0, mult1[i]);
		}
		for (int i = 0; i < slot2.length; i++) {
			inArray[x++] = new VatRecipeInput(slot2[i], 1, mult2[i]);
		}

		inArray[x++] = new RecipeInput(fluid, fluidMult);

		return inArray;
	}

	@Override
	public RecipeInput copy() {
		return new VatRecipeInput(this.ingredient, this.getSlotNumber(), this.getMulitplier());
	}

	@Override
	public boolean isInput(ItemStack test) {
		if (test == null || test.isEmpty()) {
			return false;
		}
		return ingredient.matches(CraftTweakerMC.getIItemStack(test));
	}

	@Override
	public boolean isInput(FluidStack test) {
		return false;
	}

	@Override
	public ItemStack getInput() {
		return getItemStack(ingredient);
	}

	@Override
	public ItemStack[] getEquivelentInputs() {
		if (ingredient == null || ingredient.getItems() == null)
			return new ItemStack[0];
		ItemStack[] stacks = new ItemStack[ingredient.getItems().size()];
		for (int i = 0; i < ingredient.getItems().size(); i++) {
			stacks[i] = CraftTweakerMC.getItemStack(ingredient.getItems().get(i));
		}
		return stacks;
	}

	private static ItemStack getItemStack(IIngredient ingredient) {
		if (ingredient != null && ingredient.getItems() != null && !ingredient.getItems().isEmpty()) {
			return CraftTweakerMC.getItemStack(ingredient.getItems().get(0));
		}
		return ItemStack.EMPTY;
	}
}