package com.bada774.fet;

import java.util.ArrayList;
import java.util.List;

import com.enderio.core.common.util.NNList;

import com.bada774.fet.recipe.ManyToOneRecipe;
import com.bada774.fet.recipe.machines.SlotRecipeInput;
import com.bada774.fet.utils.LateAction;
import com.bada774.fet.utils.Logging;
import com.bada774.fet.utils.RecipeUtils;
import com.bada774.fet.utils.ValidationUtils;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crazypants.enderio.base.recipe.IManyToOneRecipe;
import crazypants.enderio.base.recipe.IRecipeInput;
import crazypants.enderio.base.recipe.RecipeBonusType;
import crazypants.enderio.base.recipe.RecipeLevel;
import crazypants.enderio.base.recipe.RecipeOutput;
import crazypants.enderio.base.recipe.slicensplice.SliceAndSpliceRecipeManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.enderio.SliceNSplice")
@ZenRegister
public class SliceNSplice {
	private final static String MACHINE_NAME = "SliceNSplice",
			ITEM_TYPE = "recipe";

	private static final String METHOD_ADD_RECIPE = "addRecipe",
			METHOD_REMOVE_RECIPE = "removeRecipe";

	@ZenMethod
	public static void addRecipe(IItemStack output, IIngredient[] inputs, @Optional int energyCost,
			@Optional float xp) {

		if (hasErrors(output, inputs, METHOD_ADD_RECIPE))
			return;
		CraftTweakerAPI.apply(new AddRecipeAction(output, inputs, energyCost, xp));
	}

	@ZenMethod
	public static void removeRecipe(IItemStack output) {

		if (hasErrors(output, METHOD_REMOVE_RECIPE)) {
			return;
		}
		CraftTweakerAPI.apply(new RemoveRecipeAction(output));
	}

	public static class AddRecipeAction extends LateAction {
		public final ItemStack output;
		public final NNList<IRecipeInput> inputs;
		public final int energyCost;
		public final float xp;
		public final String logName;

		public IManyToOneRecipe createdRecipe;

		public AddRecipeAction(IItemStack output, IIngredient[] inputs, int energyCost, float xp) {
			this.output = CraftTweakerMC.getItemStack(output);
			this.energyCost = energyCost <= 0 ? 5000 : energyCost;
			this.xp = xp;
			this.logName = output.getDisplayName();

			this.inputs = new NNList<>();
			for (int i = 0; i < inputs.length; i++) {
				if (inputs[i] != null) {
					IRecipeInput converted = RecipeUtils.toEIOInput(inputs[i]);
					if (converted != null) {
						this.inputs.add(new SlotRecipeInput(converted, i));
					}
				}
			}
		}

		private String checkConflict(ManyToOneRecipe newRecipe) {
			for (IManyToOneRecipe recipe : SliceAndSpliceRecipeManager.getInstance().getRecipes()) {
				if (recipe != null && isDuplicate(recipe, newRecipe)) {
					return RecipeUtils.getConflictingOutputName(recipe.getOutput());
				}
			}
			return null;
		}

		private boolean isDuplicate(IManyToOneRecipe existing, IManyToOneRecipe created) {
			IRecipeInput[] exInputs = existing.getInputs();
			IRecipeInput[] crInputs = created.getInputs();

			if (exInputs.length != crInputs.length)
				return false;

			for (IRecipeInput crIn : crInputs) {
				if (crIn == null)
					continue;
				boolean foundMatch = false;
				for (IRecipeInput exIn : exInputs) {
					if (exIn != null && exIn.getSlotNumber() == crIn.getSlotNumber()) {
						if (OreDictionary.itemMatches(exIn.getInput(), crIn.getInput(), false)) {
							foundMatch = true;
							break;
						}
					}
				}
				if (!foundMatch)
					return false;
			}
			return true;
		}

		@Override
		public void execute() {
			RecipeOutput out = new RecipeOutput(output, 1, xp);
			ManyToOneRecipe newRecipe = new ManyToOneRecipe(out, energyCost, RecipeBonusType.NONE, RecipeLevel.IGNORE,
					inputs);

			String conflictingName = checkConflict(newRecipe);

			if (conflictingName != null) {
				Logging.logValidationError(MACHINE_NAME, METHOD_ADD_RECIPE, String.format(
						"Failed to add %s for: %s\nA %s already exists for these exact inputs!\nConflicting %s output: %s",
						ITEM_TYPE, logName, ITEM_TYPE, ITEM_TYPE, conflictingName));
				return;
			}

			this.createdRecipe = newRecipe;

			SliceAndSpliceRecipeManager.getInstance().addRecipe(this.createdRecipe);
			Logging.logAddition(MACHINE_NAME, METHOD_ADD_RECIPE, ITEM_TYPE, logName);
		}

		@Override
		public String describe() {
			return String.format("Adding %s %s by %s for: %s", MACHINE_NAME, ITEM_TYPE, METHOD_ADD_RECIPE, logName);
		}
	}

	public static class RemoveRecipeAction extends LateAction {
		public final ItemStack output;
		public final String logName;

		public List<IManyToOneRecipe> backupRecipes = new ArrayList<>();

		public RemoveRecipeAction(IItemStack output) {
			this.output = CraftTweakerMC.getItemStack(output);
			this.logName = output.getDisplayName();
		}

		@Override
		public void execute() {
			backupRecipes.clear();

			List<IManyToOneRecipe> allRecipes = SliceAndSpliceRecipeManager.getInstance().getRecipes();

			for (IManyToOneRecipe recipe : allRecipes) {
				if (OreDictionary.itemMatches(output, recipe.getOutput(), false)) {
					backupRecipes.add(recipe);
				}
			}

			if (!backupRecipes.isEmpty()) {
				for (IManyToOneRecipe r : backupRecipes) {
					SliceAndSpliceRecipeManager.getInstance().getRecipes().remove(r);
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

	private static boolean hasErrors(IItemStack output, IIngredient[] input, String methodName) {
		if (ValidationUtils.isInvalid(output)) {
			Logging.logValidationError(MACHINE_NAME, methodName, "Invalid output: empty or null");
			return true;
		}
		if (ValidationUtils.isInvalid(input)) {
			Logging.logValidationError(MACHINE_NAME, methodName, "Invalid input array: empty or null");
			return true;
		}
		if (input.length > 6) {
			Logging.logValidationError(MACHINE_NAME, methodName, String.format(
					"Invalid input array size. %s requires 1-6 inputs.\nProvided: %d", MACHINE_NAME, input.length));
			return true;
		}
		return false;
	}

	private static boolean hasErrors(IItemStack output, String methodName) {
		if (ValidationUtils.isInvalid(output)) {
			Logging.logValidationError(MACHINE_NAME, methodName, "Output cannot be null or empty");
			return true;
		}
		return false;
	}

}
