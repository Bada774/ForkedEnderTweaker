package bada774.endertweaker;

import java.util.ArrayList;
import java.util.List;

import bada774.endertweaker.utils.Logging;
import bada774.endertweaker.utils.RecipeUtils;
import bada774.endertweaker.recipe.machines.AlloySmelterRecipe;
import bada774.endertweaker.utils.LateAction;

import com.enderio.core.common.util.NNList;

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
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.enderio.AlloySmelter")
@ZenRegister
public class AlloySmelter {

	@ZenMethod
	public static void addRecipe(IItemStack output, IIngredient[] input, @Optional int energyCost, @Optional float xp) {
		if (hasErrors(output, input))
			return;
		CraftTweakerAPI.apply(new AddRecipeAction(output, input, energyCost, xp));
	}

	@ZenMethod
	public static void removeRecipe(IItemStack... outputs) {
		if (outputs == null || outputs.length == 0)
			return;
		CraftTweakerAPI.apply(new RemoveByOutputAction(outputs));
	}

	@ZenMethod
	public static void removeByInputs(IItemStack[] inputs) {
		CraftTweakerAPI.apply(new RemoveByInputsAction(new IItemStack[][] { inputs }));
	}

	@ZenMethod
	public static void removeByInputs(IItemStack[][] inputs) {
		CraftTweakerAPI.apply(new RemoveByInputsAction(inputs));
	}

	public static boolean hasErrors(IItemStack output, IIngredient[] input) {
		if (output == null || output.isEmpty()) {
			CraftTweakerAPI.logError("Invalid output (empty or null) in Alloy Smelter recipe: " + output);
			return true;
		}
		if (input.length > 3) {
			CraftTweakerAPI.logError("Invalid Alloy Smelter input, must be between 1 and 3 inputs. Provided: "
					+ RecipeUtils.getDisplayString(input));
			return true;
		}
		return false;
	}

	public static class AddRecipeAction extends LateAction {
		public final ItemStack output;
		public final NNList<IRecipeInput> inputs;
		public final int energyCost;
		public final float xp;
		public final String recipeName;

		AddRecipeAction(IItemStack output, IIngredient[] ctInputs, int energyCost, float xp) {
			this.output = CraftTweakerMC.getItemStack(output);
			this.inputs = RecipeUtils.toEIOInputsNN(ctInputs);
			this.energyCost = energyCost <= 0 ? 5000 : energyCost;
			this.xp = xp;
			this.recipeName = output.getDisplayName();
		}

		@Override
		public void execute() {
			if (AlloyRecipeManager.getInstance() == null)
				return;

			for (IManyToOneRecipe recipe : AlloySmelterRecipe.getAlloyRecipes()) {
				if (recipe != null && OreDictionary.itemMatches(output, recipe.getOutput(),
						true)
						&& RecipeUtils.areInputsMatch(RecipeUtils.toEIOInputsNN(recipe.getInputs()),
								inputs)) {
					return;
				}
			}
			AlloyRecipeManager.getInstance().addRecipe(true, inputs, output, energyCost, xp, RecipeLevel.IGNORE);
		}

		@Override
		public String describe() {
			return "Adding Alloy Smelter recipe for: " + recipeName;
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
			List<IManyToOneRecipe> alloyRecipes = AlloySmelterRecipe.getAlloyRecipes();
			if (alloyRecipes.isEmpty())
				return;

			List<ItemStack> targetOutputs = new ArrayList<>();
			List<String> targetNames = new ArrayList<>();

			for (IItemStack output : outputs) {
				if (output != null && !output.isEmpty()) {
					targetOutputs.add(CraftTweakerMC.getItemStack(output));
					targetNames.add(output.getDisplayName());
				} else {
					CraftTweakerAPI
							.logError("Invalid output null in Alloy Smelter recipe removal: " + output);
				}
			}

			if (targetOutputs.isEmpty())
				return;

			TriItemLookup<IManyToOneRecipe> newLookup = AlloySmelterRecipe.createLookup();
			List<IManyToOneRecipe> validRecipes = new ArrayList<>();

			backupRecipes.clear();
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
					AlloySmelterRecipe.addRecipeToLookup(newLookup, recipe);
					validRecipes.add(recipe);
				}
			}

			if (removedCount > 0) {
				AlloySmelterRecipe.commitChanges(newLookup, validRecipes);
			}

			List<String> successRemovedList = new ArrayList<>();
			List<String> missingList = new ArrayList<>();

			for (int i = 0; i < foundMatch.length; i++) {
				if (foundMatch[i]) {
					successRemovedList.add(targetNames.get(i));
				} else {
					missingList.add(targetNames.get(i));
				}
			}
			Logging.logRemovalResult("Alloy Smelter", removedCount, "removeRecipe",
					successRemovedList, missingList);
		}

		@Override
		public String describe() {
			return "Removing Alloy Smelter recipes by output";
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
			List<IManyToOneRecipe> alloyRecipes = AlloySmelterRecipe.getAlloyRecipes();
			if (alloyRecipes.isEmpty())
				return;

			List<NNList<MachineRecipeInput>> targetsList = new ArrayList<>();
			List<IItemStack[]> validInputs = new ArrayList<>();

			for (IItemStack[] filterInput : inputs) {
				if (filterInput == null || filterInput.length == 0
						|| filterInput.length > 3) {
					CraftTweakerAPI.logError(
							"Invalid Alloy Smelter recipe inputs: " + ((filterInput == null) ? "null"
									: RecipeUtils.getDisplayString(filterInput)));
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

			TriItemLookup<IManyToOneRecipe> newLookup = AlloySmelterRecipe.createLookup();
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
					AlloySmelterRecipe.addRecipeToLookup(newLookup, recipe);
					validRecipes.add(recipe);
				}
			}

			if (removedCount > 0) {
				AlloySmelterRecipe.commitChanges(newLookup, validRecipes);
			}

			List<String> successLog = new ArrayList<>();
			List<String> missingLog = new ArrayList<>();

			for (int i = 0; i < foundMatch.length; i++) {
				StringBuilder sbName = new StringBuilder("[");
				boolean firstItem = true;
				for (IItemStack item : validInputs.get(i)) {
					if (item != null) {
						if (!firstItem)
							sbName.append(", ");
						sbName.append(item.getDisplayName());
						firstItem = false;
					}
				}
				sbName.append("]");
				if (foundMatch[i]) {
					successLog.add(sbName.toString());
				} else {
					missingLog.add(sbName.toString());
				}
			}

			Logging.logRemovalResult("Alloy Smelter", removedCount, "removeByInputs", successLog, missingLog);
		}

		@Override
		public String describe() {
			return "Removing Alloy Smelter recipes by inputs";
		}

	}
}
