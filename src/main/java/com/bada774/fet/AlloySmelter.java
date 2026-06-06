package com.bada774.fet;

import java.util.ArrayList;
import java.util.List;

import com.enderio.core.common.util.NNList;

import com.bada774.fet.recipe.machines.AlloySmelterRecipes;
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
import crazypants.enderio.base.recipe.MachineRecipeInput;
import crazypants.enderio.base.recipe.RecipeLevel;
import crazypants.enderio.base.recipe.alloysmelter.AlloyRecipeManager;
import crazypants.enderio.base.recipe.lookup.TriItemLookup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass(AlloySmelter.ZEN_CLASS)
@ZenRegister
public class AlloySmelter {

	private static final Logger LOGGER = LogManager.getLogger();

	public static final String
			MACHINE_NAME = "AlloySmelter",
			ITEM_TYPE = "recipe",

			ZEN_CLASS = "mods.enderio." + MACHINE_NAME,

			METHOD_ADD_RECIPE = "addRecipe",
			METHOD_REMOVE_RECIPE = "removeRecipe",
			METHOD_REMOVE_BY_INPUTS = "removeByInputs";

	@ZenMethod
	public static void addRecipe(IItemStack output, IIngredient[] input, @Optional int energyCost, @Optional float xp) {

		if (hasErrors(output, input, METHOD_ADD_RECIPE))
			return;
		CraftTweakerAPI.apply(new AddRecipeAction(output, input, energyCost, xp));
	}

	@ZenMethod
	public static void removeRecipe(IItemStack... outputs) {

		if (hasErrors(outputs, METHOD_REMOVE_RECIPE))
			return;
		CraftTweakerAPI.apply(new RemoveByOutputAction(outputs));
	}

	@ZenMethod
	public static void removeByInputs(IItemStack[][] inputs) {

		if (hasErrors(inputs, METHOD_REMOVE_BY_INPUTS))
			return;
		CraftTweakerAPI.apply(new RemoveByInputsAction(inputs));
	}

	@ZenMethod
	public static void removeByInputs(IItemStack[] inputs) {
		removeByInputs(new IItemStack[][] { inputs });
	}

	public static class AddRecipeAction extends LateAction {
		public final ItemStack output;
		// Raw CraftTweaker inputs, kept as-is. They're turned into EnderIO inputs in execute() from now,
		// not in the constructor (explanation why it was changed see in execute()).
		public final IIngredient[] ctInputs;
		public NNList<IRecipeInput> inputs;
		public final int energyCost;
		public final float xp;
		public final String logName;

		public boolean recipeCreated = false;

		AddRecipeAction(IItemStack output, IIngredient[] ctInputs, int energyCost, float xp) {
			this.output = CraftTweakerMC.getItemStack(output);
			this.ctInputs = ctInputs;
			this.energyCost = energyCost <= 0 ? 5000 : energyCost;
			this.xp = xp;
			this.logName = output.getDisplayName();
		}

		private String checkConflict() {
			if (AlloyRecipeManager.getInstance() == null)
				return null;

			for (IManyToOneRecipe recipe : AlloySmelterRecipes.getAlloyRecipes()) {
				try {
					if (recipe != null
							&& RecipeUtils.areInputsMatch(RecipeUtils.toEIOInputsNN(recipe.getInputs()), inputs)) {

						return RecipeUtils.getConflictingOutputName(recipe.getOutput());
					}
				} catch (Exception e) {
					// A foreign recipe's inputs may not be fully baked yet. Don't abort recipes.
					LOGGER.debug("[FET] AlloySmelter conflict scan skipped a recipe", e);
				}
			}
			return null;
		}

		@Override
		public void execute() {
			// Resolve the inputs here, at drain time. Doing it in the constructor (when CraftTweaker first
			// parses the script) runs before EnderCore bakes its Things, and the inputs come out empty -
			// recipe never makes it into the machine on the startup, only after `/ct reload`.
			this.inputs = RecipeUtils.toEIOInputsNN(ctInputs);

			String conflictingName = checkConflict();

			if (conflictingName != null) {
				Logging.logValidationError(MACHINE_NAME, METHOD_ADD_RECIPE, String.format(
						"Failed to add %s for: %s\nA %s already exists for these exact inputs!\nConflicting %s output: %s",
						ITEM_TYPE, logName, ITEM_TYPE, ITEM_TYPE, conflictingName));
				return;
			}

			AlloyRecipeManager.getInstance().addRecipe(true, inputs, output, energyCost, xp, RecipeLevel.IGNORE);
			this.recipeCreated = true;

			Logging.logAddition(
					MACHINE_NAME, METHOD_ADD_RECIPE, ITEM_TYPE, logName);
		}

		@Override
		public String describe() {
			return String.format("Adding %s %ss by %s for: %s", MACHINE_NAME, ITEM_TYPE, METHOD_ADD_RECIPE, logName);
		}
	}

	public static class RemoveByOutputAction extends LateAction {
		public final IItemStack[] outputs;
		public final List<IManyToOneRecipe> backupRecipes = new ArrayList<>();

		public RemoveByOutputAction(IItemStack[] outputs) {
			this.outputs = outputs;
		}

		@Override
		public void execute() {
			backupRecipes.clear();

			List<IManyToOneRecipe> alloyRecipes = AlloySmelterRecipes.getAlloyRecipes();
			if (alloyRecipes.isEmpty())
				return;

			List<ItemStack> targetOutputs = new ArrayList<>();
			List<String> targetNames = new ArrayList<>();

			for (IItemStack output : outputs) {
				targetOutputs.add(CraftTweakerMC.getItemStack(output));
				targetNames.add(output.getDisplayName());
			}

			TriItemLookup<IManyToOneRecipe> newLookup = AlloySmelterRecipes.createLookup();
			List<IManyToOneRecipe> validRecipes = new ArrayList<>();

			int removedCount = 0;
			boolean[] foundMatch = new boolean[targetOutputs.size()];

			for (IManyToOneRecipe recipe : alloyRecipes) {
				if (recipe == null)
					continue;

				boolean shouldRemove = false;
				for (int i = 0; i < targetOutputs.size(); i++) {
					if (OreDictionary.itemMatches(targetOutputs.get(i), recipe.getOutput(),
							false)) {
						shouldRemove = true;
						foundMatch[i] = true;
						break;
					}
				}
				if (shouldRemove) {
					backupRecipes.add(recipe);
					removedCount++;
				} else {
					AlloySmelterRecipes.addRecipeToLookup(newLookup, recipe);
					validRecipes.add(recipe);
				}
			}

			if (removedCount > 0) {
				AlloySmelterRecipes.commitChanges(newLookup, validRecipes);
			}

			List<String> successList = new ArrayList<>();
			List<String> missingList = new ArrayList<>();

			for (int i = 0; i < foundMatch.length; i++) {
				if (foundMatch[i])
					successList.add(targetNames.get(i));
				else
					missingList.add(targetNames.get(i));
			}
			Logging.logRemoval(MACHINE_NAME, METHOD_REMOVE_RECIPE, ITEM_TYPE,
					successList, missingList);
		}

		@Override
		public String describe() {
			return String.format("Removing %s %ss by %s", MACHINE_NAME, ITEM_TYPE, METHOD_REMOVE_RECIPE);
		}
	}

	public static class RemoveByInputsAction extends LateAction {
		public final IItemStack[][] inputs;
		public final List<IManyToOneRecipe> backupRecipes = new ArrayList<>();

		public RemoveByInputsAction(IItemStack[][] inputs) {
			this.inputs = inputs;
		}

		@Override
		public void execute() {
			List<IManyToOneRecipe> alloyRecipes = AlloySmelterRecipes.getAlloyRecipes();
			if (alloyRecipes.isEmpty())
				return;

			List<NNList<MachineRecipeInput>> targetsList = new ArrayList<>();
			List<IItemStack[]> validInputs = new ArrayList<>();

			for (IItemStack[] filterInput : inputs) {
				if (ValidationUtils.isInvalid(filterInput) || filterInput.length > 3) {
					Logging.logValidationError(MACHINE_NAME, METHOD_REMOVE_BY_INPUTS,
							String.format("Invalid %s inputs, must be between 1 and 3 inputs", ITEM_TYPE));
					continue;
				}

				NNList<MachineRecipeInput> targetInputs = new NNList<>();

				for (int i = 0; i < filterInput.length; i++) {
					if (filterInput[i] != null) {
						targetInputs.add(new MachineRecipeInput(i,
								CraftTweakerMC.getItemStack(filterInput[i])));
					}
				}
				targetsList.add(targetInputs);
				validInputs.add(filterInput);
			}

			if (targetsList.isEmpty())
				return;

			TriItemLookup<IManyToOneRecipe> newLookup = AlloySmelterRecipes.createLookup();
			List<IManyToOneRecipe> validRecipes = new ArrayList<>();
			backupRecipes.clear();

			int removedCount = 0;
			boolean[] foundMatch = new boolean[targetsList.size()];

			for (IManyToOneRecipe recipe : alloyRecipes) {
				if (recipe == null)
					continue;

				boolean shouldRemove = false;
				for (int i = 0; i < targetsList.size(); i++) {
					if (recipe.isInputForRecipe(targetsList.get(i))) {
						shouldRemove = true;
						foundMatch[i] = true;
						break;
					}
				}

				if (shouldRemove) {
					backupRecipes.add(recipe);
					removedCount++;
				} else {
					AlloySmelterRecipes.addRecipeToLookup(newLookup, recipe);
					validRecipes.add(recipe);
				}
			}

			if (removedCount > 0) {
				AlloySmelterRecipes.commitChanges(newLookup, validRecipes);
			}

			List<String> successList = new ArrayList<>();
			List<String> missingList = new ArrayList<>();

			for (int i = 0; i < foundMatch.length; i++) {

				String recipeString = RecipeUtils.getDisplayString(validInputs.get(i));
				if (foundMatch[i])
					successList.add(recipeString);
				else
					missingList.add(recipeString);
			}

			Logging.logRemoval(MACHINE_NAME, METHOD_REMOVE_BY_INPUTS, ITEM_TYPE,
					successList, missingList);
		}

		@Override
		public String describe() {
			return String.format("Removing %s %ss by %s", MACHINE_NAME, ITEM_TYPE, METHOD_REMOVE_BY_INPUTS);
		}
	}

	private static boolean hasErrors(IItemStack output, IIngredient[] input, String methodName) {
		if (ValidationUtils.isInvalid(output)) {
			Logging.logValidationError(MACHINE_NAME, methodName,
					String.format("Invalid output (empty or null) for %s", ITEM_TYPE));
			return true;
		}
		if (ValidationUtils.isInvalid(input)) {
			Logging.logValidationError(MACHINE_NAME, methodName,
					String.format("Invalid input array (empty or null) for output: %s", output.getDisplayName()));
			return true;
		}
		if (input.length > 3) {
			Logging.logValidationError(MACHINE_NAME, methodName,
					String.format("Invalid input for %s, must be between 1 and 3 inputs", ITEM_TYPE));
			return true;
		}
		for (IIngredient ing : input) {
			if (ValidationUtils.isInvalid(ing)) {
				Logging.logValidationError(MACHINE_NAME, methodName,
						"Invalid input: one of the ingredients is null or empty");
				return true;
			}
		}

		return false;
	}

	private static boolean hasErrors(IItemStack[] array, String methodName) {
		if (ValidationUtils.isInvalid(array)) {
			Logging.logValidationError(MACHINE_NAME, methodName, "No items provided or array contains empty items");
			return true;
		}
		return false;
	}

	private static boolean hasErrors(IItemStack[][] array, String methodName) {
		if (ValidationUtils.isInvalid(array)) {
			Logging.logValidationError(MACHINE_NAME, methodName, "No item arrays provided");
			return true;
		}
		return false;
	}
}
