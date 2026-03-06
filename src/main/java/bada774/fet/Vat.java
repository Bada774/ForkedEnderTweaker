package bada774.fet;

import java.util.ArrayList;
import java.util.List;

import bada774.fet.recipe.machines.VatRecipeInput;
import bada774.fet.utils.LateAction;
import bada774.fet.utils.Logging;
import bada774.fet.utils.VatRecipeUtils;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crazypants.enderio.base.recipe.IRecipe;
import crazypants.enderio.base.recipe.IRecipeInput;
import crazypants.enderio.base.recipe.Recipe;
import crazypants.enderio.base.recipe.RecipeBonusType;
import crazypants.enderio.base.recipe.RecipeLevel;
import crazypants.enderio.base.recipe.RecipeOutput;
import crazypants.enderio.base.recipe.vat.VatRecipe;
import crazypants.enderio.base.recipe.vat.VatRecipeManager;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.enderio.Vat")
@ZenRegister
public class Vat {
	private static final String MACHINE_NAME = "Vat",
			ITEM_TYPE = "recipe";

	private static final String METHOD_ADD_RECIPE = "addRecipe",
			METHOD_REMOVE_RECIPE = "removeRecipe";

	@ZenMethod
	public static void addRecipe(ILiquidStack output, float inMult, ILiquidStack input, IIngredient[] slot1Solids,
			float[] slot1Mults, IIngredient[] slot2Solids, float[] slot2Mults, @Optional int energyCost) {

		if (hasErrors(output, input, slot1Solids, slot1Mults, slot2Solids, slot2Mults, METHOD_ADD_RECIPE)) {
			return;
		}
		CraftTweakerAPI.apply(new AddRecipeAction(output, inMult, input, slot1Solids, slot1Mults, slot2Solids,
				slot2Mults, energyCost));
	}

	@ZenMethod
	public static void removeRecipe(ILiquidStack output) {

		if (hasErrors(output, METHOD_REMOVE_RECIPE)) {
			return;
		}
		CraftTweakerAPI.apply(new RemoveRecipeAction(output));
	}

	public static class AddRecipeAction extends LateAction {
		public final FluidStack output;
		public final FluidStack input;
		public final float inMult;
		public final IIngredient[] slot1Solids;
		public final float[] slot1Mults;
		public final IIngredient[] slot2Solids;
		public final float[] slot2Mults;
		public final int energyCost;

		public final String logName;

		public IRecipe createdRecipe;

		AddRecipeAction(ILiquidStack output, float inMult, ILiquidStack input, IIngredient[] slot1Solids,
				float[] slot1Mults, IIngredient[] slot2Solids, float[] slot2Mults, int energyCost) {
			this.output = CraftTweakerMC.getLiquidStack(output);
			this.input = CraftTweakerMC.getLiquidStack(input);
			this.inMult = inMult <= 0 ? 1 : inMult;
			this.slot1Solids = slot1Solids;
			this.slot1Mults = slot1Mults;
			this.slot2Solids = slot2Solids;
			this.slot2Mults = slot2Mults;
			this.energyCost = energyCost <= 0 ? 5000 : energyCost;

			this.logName = this.output.getLocalizedName();
		}

		private String checkConflict(VatRecipe newRecipe) {
			for (IRecipe existing : VatRecipeManager.getInstance().getRecipes()) {
				if (existing == null)
					continue;
				if (VatRecipeUtils.isDuplicate(existing, newRecipe)) {
					if (existing.getOutputs() != null
							&& existing.getOutputs().length > 0
							&& existing.getOutputs()[0].getFluidOutput() != null) {
						return existing.getOutputs()[0].getFluidOutput().getLocalizedName();
					}
					return "Unknown Fluid";
				}
			}
			return null;
		}

		@Override
		public void execute() {
			IRecipeInput[] inArray = VatRecipeInput.createInputsArray(input, inMult, slot1Solids, slot1Mults,
					slot2Solids, slot2Mults);
			RecipeOutput out = new RecipeOutput(output);
			Recipe baseRecipe = new Recipe(out, energyCost, RecipeBonusType.NONE, RecipeLevel.IGNORE, inArray);
			VatRecipe newRecipe = new VatRecipe(baseRecipe);

			String conflictingName = checkConflict(newRecipe);

			if (conflictingName != null) {
				Logging.logValidationError(MACHINE_NAME, METHOD_ADD_RECIPE,
						String.format(
								"Failed to add %s for: %s\nA %s already exists for these exact inputs!\nConflicting %s output: %s",
								ITEM_TYPE, logName, ITEM_TYPE, ITEM_TYPE, conflictingName));
				return;
			}

			this.createdRecipe = newRecipe;

			VatRecipeManager.getInstance().addRecipe(this.createdRecipe);
			Logging.logAddition(MACHINE_NAME, METHOD_ADD_RECIPE, ITEM_TYPE, logName);

		}

		@Override
		public String describe() {
			return String.format("Adding %s %s by %s for: %s", MACHINE_NAME, ITEM_TYPE, METHOD_ADD_RECIPE, logName);
		}

	}

	public static class RemoveRecipeAction extends LateAction {
		public final FluidStack output;
		public final String logName;

		public List<IRecipe> backupRecipes = new ArrayList<>();

		public RemoveRecipeAction(ILiquidStack output) {
			this.output = CraftTweakerMC.getLiquidStack(output);
			this.logName = this.output.getLocalizedName();
		}

		@Override
		public void execute() {
			backupRecipes.clear();

			List<IRecipe> recipes = VatRecipeManager.getInstance().getRecipes();

			for (IRecipe recipe : recipes) {
				if (recipe != null
						&& recipe.getOutputs()[0] != null
						&& recipe.getOutputs()[0].getFluidOutput() != null
						&& recipe.getOutputs()[0].getFluidOutput().isFluidEqual(output)) {
					backupRecipes.add(recipe);
				}
			}

			if (!backupRecipes.isEmpty()) {
				for (IRecipe recipe : backupRecipes) {
					VatRecipeManager.getInstance().getRecipes().remove(recipe);
				}
				Logging.logRemoval(MACHINE_NAME, METHOD_REMOVE_RECIPE, ITEM_TYPE, logName, null);
			} else {
				Logging.logRemoval(MACHINE_NAME, METHOD_REMOVE_RECIPE, ITEM_TYPE, null, logName);
			}
		}

		@Override
		public String describe() {
			return String.format("Removing %s %s by %s for: %s", MACHINE_NAME, ITEM_TYPE, METHOD_REMOVE_RECIPE,
					logName);
		}

	}

	private static boolean hasErrors(ILiquidStack output, ILiquidStack input, IIngredient[] slot1Solids,
			float[] slot1Mults, IIngredient[] slot2Solids, float[] slot2Mults, String methodName) {

		if (output == null) {
			Logging.logValidationError(MACHINE_NAME, methodName, "Invalid output: null");
			return true;
		}
		if (input == null) {
			Logging.logValidationError(MACHINE_NAME, methodName, "Invalid fluid input: null");
			return true;
		}
		if (slot1Solids.length != slot1Mults.length) {
			Logging.logValidationError(MACHINE_NAME, methodName,
					"Slot 1 mismatch: Solids count (" + slot1Solids.length + ") != Multipliers count ("
							+ slot1Mults.length + ")");
			return true;
		}
		if (slot2Solids.length != slot2Mults.length) {
			Logging.logValidationError(MACHINE_NAME, methodName,
					"Slot 2 mismatch: Solids count (" + slot2Solids.length + ") != Multipliers count ("
							+ slot2Mults.length + ")");
			return true;
		}
		return false;
	}

	private static boolean hasErrors(ILiquidStack output, String methodName) {
		if (output == null) {
			Logging.logValidationError(MACHINE_NAME, methodName, "Output cannot be null");
			return true;
		}
		return false;
	}
}
