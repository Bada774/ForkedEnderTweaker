package com.bada774.fet;

import java.util.Arrays;

import com.google.common.base.Strings;

import com.bada774.fet.recipe.RecipeInput;
import com.bada774.fet.recipe.machines.SagMillRecipe;
import com.bada774.fet.utils.LateAction;
import com.bada774.fet.utils.Logging;
import com.bada774.fet.utils.RecipeUtils;
import com.bada774.fet.utils.ValidationUtils;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.item.WeightedItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crazypants.enderio.base.recipe.IRecipe;
import crazypants.enderio.base.recipe.Recipe;
import crazypants.enderio.base.recipe.RecipeBonusType;
import crazypants.enderio.base.recipe.RecipeLevel;
import crazypants.enderio.base.recipe.RecipeOutput;
import crazypants.enderio.base.recipe.sagmill.SagMillRecipeManager;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass(SagMill.ZEN_CLASS)
@ZenRegister
public class SagMill {

	public final static String
			MACHINE_NAME = "SagMill",
			ITEM_TYPE = "recipe",

			ZEN_CLASS = "mods.enderio." + MACHINE_NAME,

			METHOD_ADD_RECIPE = "addRecipe",
			METHOD_REMOVE_RECIPE = "removeRecipe";

	@ZenMethod
	public static void addRecipe(IItemStack[] output, float[] chances, IIngredient input, @Optional String bonusType,
			@Optional int energyCost, @Optional float[] xp) {

		if (xp == null) {
			xp = new float[output.length];
			Arrays.fill(xp, 0);
		}

		if (hasErrors(output, chances, input, xp, bonusType, METHOD_ADD_RECIPE))
			return;

		CraftTweakerAPI.apply(new AddRecipeAction(output, chances, input, bonusType, energyCost, xp));

	}

	@ZenMethod
	public static void addRecipe(WeightedItemStack[] output, IIngredient input, @Optional String bonusType,
			@Optional int energyCost, @Optional float[] xp) {

		if (ValidationUtils.isInvalid(output)) {
			Logging.logValidationError(MACHINE_NAME, METHOD_ADD_RECIPE, "Invalid output: null or empty");
			return;
		}

		IItemStack[] stacks = new IItemStack[output.length];
		float[] chances = new float[output.length];

		for (int i = 0; i < output.length; i++) {
			if (output[i] == null) {
				Logging.logValidationError(MACHINE_NAME, METHOD_ADD_RECIPE,
						"Invalid output: element at index " + i + " is null");
				return;
			}
			stacks[i] = output[i].getStack();
			chances[i] = output[i].getChance();
		}

		addRecipe(stacks, chances, input, bonusType, energyCost, xp);
	}

	@ZenMethod
	public static void removeRecipe(IItemStack input) {

		if (ValidationUtils.isInvalid(input)) {
			Logging.logValidationError(MACHINE_NAME, METHOD_REMOVE_RECIPE, "Input item cannot be null or empty");
			return;
		}

		CraftTweakerAPI.apply(new RemoveRecipeAction(input));
	}

	public static class AddRecipeAction extends LateAction {
		public final RecipeOutput[] output;
		public final RecipeInput eioInput;
		public final int energyCost;
		public final RecipeBonusType bonusType;
		public final String logName;

		public Recipe createdRecipe;

		public AddRecipeAction(IItemStack[] output, float[] chances, IIngredient input, String bonusType,
				int energyCost, float[] xp) {
			this.eioInput = new RecipeInput(CraftTweakerMC.getIngredient(input));
			this.energyCost = energyCost <= 0 ? 5000 : energyCost;
			this.output = RecipeUtils.toEIOOutputs(output, chances, xp);
			this.bonusType = RecipeBonusType.valueOf(Strings.isNullOrEmpty(bonusType) ? "NONE" : bonusType);
			this.logName = (input != null && input.getItems() != null && !input.getItems().isEmpty())
					? input.getItems().get(0).getDisplayName()
					: "Unknown Input";

		}

		private boolean checkConflict() {
			return SagMillRecipeManager.getInstance().getRecipeForInput(RecipeLevel.IGNORE,
					eioInput.getInput()) != null;
		}

		@Override
		public void execute() {
			if (checkConflict()) {
				Logging.logValidationError(MACHINE_NAME, METHOD_ADD_RECIPE, String.format(
						"Failed to add %s for: %s\nA %s already exists for this input!",
						ITEM_TYPE, logName, ITEM_TYPE));
				return;
			}

			this.createdRecipe = new SagMillRecipe(eioInput, energyCost, bonusType, RecipeLevel.IGNORE, output);

			SagMillRecipeManager.getInstance().addRecipe(this.createdRecipe);

			Logging.logAddition(MACHINE_NAME, METHOD_ADD_RECIPE, ITEM_TYPE, logName);
		}

		@Override
		public String describe() {
			return String.format("Adding %s %s by %s for: %s", MACHINE_NAME, METHOD_ADD_RECIPE, ITEM_TYPE,
					logName);
		}
	}

	public static class RemoveRecipeAction extends LateAction {
		public final ItemStack inputStack;
		public final String logName;
		public Recipe backupRecipe;

		public RemoveRecipeAction(IItemStack input) {
			this.inputStack = CraftTweakerMC.getItemStack(input);
			this.logName = input.getDisplayName();
		}

		@Override
		public void execute() {
			IRecipe recipe = SagMillRecipeManager.getInstance().getRecipeForInput(RecipeLevel.IGNORE, inputStack);
			if (recipe instanceof Recipe) {
				this.backupRecipe = (Recipe) recipe;
				SagMillRecipeManager.getInstance().getRecipes().remove((Recipe) recipe);
				Logging.logRemoval(MACHINE_NAME, METHOD_REMOVE_RECIPE, ITEM_TYPE, logName, null);
			} else {
				Logging.logRemoval(MACHINE_NAME, METHOD_REMOVE_RECIPE, ITEM_TYPE, null, logName);
			}
		}

		@Override
		public String describe() {
			return String.format("Removing %s %s by %s for: %s", MACHINE_NAME, METHOD_REMOVE_RECIPE, ITEM_TYPE,
					logName);
		}
	}

	private static boolean hasErrors(IItemStack[] output, float[] chances, IIngredient input, float[] xp,
			String bonusType,
			String methodName) {
		if (ValidationUtils.isInvalid(output) || ValidationUtils.isInvalid(chances) || ValidationUtils.isInvalid(xp)) {
			Logging.logValidationError(MACHINE_NAME, methodName,
					"Outputs, chances, or XP arrays cannot be null or empty");
			return true;
		}
		if (output.length > 4) {
			Logging.logValidationError(MACHINE_NAME, methodName,
					"Invalid output: more than four entries");
			return true;
		}
		if (output.length != chances.length || output.length != xp.length) {
			Logging.logValidationError(MACHINE_NAME, methodName,
					"Array length mismatch between outputs, chances, and xp");
			return true;
		}
		if (ValidationUtils.isInvalid(input)) {
			Logging.logValidationError(MACHINE_NAME, methodName, "Invalid input: null or empty");
			return true;
		}
		try {
			RecipeBonusType.valueOf(Strings.isNullOrEmpty(bonusType) ? "NONE" : bonusType);
		} catch (IllegalArgumentException e) {
			Logging.logValidationError(MACHINE_NAME, methodName, String.format(
					"Invalid bonus type: %s\nValid values: NONE, MULTIPLY_OUTPUT, CHANCE_ONLY", bonusType));
			return true;
		}
		return false;
	}

}
