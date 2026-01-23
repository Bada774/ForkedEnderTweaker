package shadows.endertweaker;

import com.enderio.core.common.util.NNList;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crazypants.enderio.base.recipe.IManyToOneRecipe;
import crazypants.enderio.base.recipe.MachineRecipeInput;
import crazypants.enderio.base.recipe.RecipeLevel;
import crazypants.enderio.base.recipe.alloysmelter.AlloyRecipeManager;
import crazypants.enderio.base.recipe.lookup.TriItemLookup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import shadows.endertweaker.bada774.Logging;
import shadows.endertweaker.bada774.recipe.AlloySmelterRecipe;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.List;

@ZenClass("mods.enderio.AlloySmelter")
@ZenRegister
public class AlloySmelter {

	@ZenMethod
	public static void addRecipe(IItemStack output, IIngredient[] input, @Optional int energyCost, @Optional float xp) {
		if (hasErrors(output, input))
			return;
		EnderTweaker.ADDITIONS.add(() -> AlloyRecipeManager.getInstance().addRecipe(true,
				RecipeUtils.toEIOInputsNN(input),
				CraftTweakerMC.getItemStack(output), energyCost <= 0 ? 5000 : energyCost, xp, RecipeLevel.IGNORE));
	}

	@ZenMethod
	public static void removeRecipe(IItemStack... outputs) {
		if (outputs == null || outputs.length == 0) {
			CraftTweakerAPI.logError("Cannot remove recipes for null from Alloy Smelter.");
			return;
		}
		EnderTweaker.REMOVALS.add(() -> {
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
					CraftTweakerAPI.logError("Invalid output null in Alloy Smelter recipe removal: " + output);
				}
			}

			if (targetOutputs.isEmpty())
				return;

			boolean[] foundMatch = new boolean[targetOutputs.size()];

			TriItemLookup<IManyToOneRecipe> newLookup = AlloySmelterRecipe.createLookup();

			int removedCount = 0;
			for (IManyToOneRecipe recipe : alloyRecipes) {
				if (recipe == null)
					continue;

				boolean shouldRemove = false;
				for (int i = 0; i < targetOutputs.size(); i++) {
					if (OreDictionary.itemMatches(targetOutputs.get(i), recipe.getOutput(), false)) {
						shouldRemove = true;
						foundMatch[i] = true;
						break;
					}
				}
				if (shouldRemove) {
					removedCount++;
				} else {
					AlloySmelterRecipe.addRecipeToLookup(newLookup, recipe);
				}
			}

			if (removedCount > 0) {
				AlloySmelterRecipe.setNewLookup(newLookup);
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
			Logging.logRemovalResult("Alloy Smelter", removedCount, "removeRecipe", successRemovedList, missingList);
		});
	}

	@ZenMethod
	public static void removeByInputs(IItemStack[] inputs) {
		executeRemoveByInputs(new IItemStack[][] { inputs }, "removeByInputs");
	}

	@ZenMethod
	public static void removeMultipleByInputs(IItemStack[][] inputs) {
		executeRemoveByInputs(inputs, "removeMultipleByInputs");
	}

	public static void executeRemoveByInputs(IItemStack[][] inputs, String callerName) {
		if (inputs == null || inputs.length == 0) {
			CraftTweakerAPI.logError("Cannot remove recipes by inputs: inputs are null.");
			return;
		}
		EnderTweaker.REMOVALS.add(() -> {
			List<IManyToOneRecipe> alloyRecipes = AlloySmelterRecipe.getAlloyRecipes();
			if (alloyRecipes.isEmpty())
				return;

			List<NNList<MachineRecipeInput>> targetsList = new ArrayList<>();
			List<IItemStack[]> validInputs = new ArrayList<>();

			for (IItemStack[] filterInput : inputs) {
				if (filterInput == null || filterInput.length == 0
						|| filterInput.length > 3) {
					CraftTweakerAPI.logError(
							"Invalid Alloy Smelter recipe inputs: " + RecipeUtils.getDisplayString(filterInput));
					continue;
				}

				NNList<MachineRecipeInput> targetInputs = new NNList<>();

				for (int i = 0; i < filterInput.length; i++) {
					if (filterInput[i] != null) {
						targetInputs.add(new MachineRecipeInput(i, CraftTweakerMC.getItemStack(filterInput[i])));
					}
				}
				targetsList.add(targetInputs);
				validInputs.add(filterInput);
			}

			if (targetsList.isEmpty())
				return;

			boolean[] foundMatch = new boolean[targetsList.size()];

			TriItemLookup<IManyToOneRecipe> newLookup = AlloySmelterRecipe.createLookup();

			int removedCount = 0;
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
					removedCount++;
				} else {
					AlloySmelterRecipe.addRecipeToLookup(newLookup, recipe);
				}
			}

			if (removedCount > 0) {
				AlloySmelterRecipe.setNewLookup(newLookup);
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

			Logging.logRemovalResult("Alloy Smelter", removedCount, callerName, successLog, missingLog);
		});
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

}
