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
import shadows.endertweaker.bada774.recipe.AlloySmelterRecipe;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

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
	public static void removeRecipe(IItemStack output) {
		if (output == null) {
			CraftTweakerAPI.logError("Cannot remove recipe for null from alloy smelter.");
			return;
		}
		EnderTweaker.REMOVALS.add(() -> {
			List<IManyToOneRecipe> alloyRecipes = AlloySmelterRecipe.getAlloyRecipes();
			if (alloyRecipes.isEmpty())
				return;
			ItemStack targetStack = CraftTweakerMC.getItemStack(output);

			TriItemLookup<IManyToOneRecipe> newLookup = AlloySmelterRecipe.createLookup();

			int removedCount = 0;

			for (IManyToOneRecipe recipe : alloyRecipes) {
				if (recipe != null && OreDictionary.itemMatches(targetStack, recipe.getOutput(), false)) {
					removedCount++;
				} else {
					AlloySmelterRecipe.addRecipeToLookup(newLookup, recipe);
				}
			}
			if (removedCount > 0) {
				AlloySmelterRecipe.setNewLookup(newLookup);
				CraftTweakerAPI.logInfo("Removed " + removedCount + " recipes for " + output.getDisplayName());
			} else {
				CraftTweakerAPI.logError("No Alloy Smelter recipes found for " + output.getDisplayName());
			}
		});
	}

	@ZenMethod
	public static void removeByInputs(IItemStack... inputs) {
		if (inputs == null || inputs.length > 3) {
			CraftTweakerAPI.logError("Cannot remove recipe for null from alloy smelter.");
			return;
		}
		EnderTweaker.REMOVALS.add(() -> {
			List<IManyToOneRecipe> alloyRecipes = AlloySmelterRecipe.getAlloyRecipes();
			NNList<MachineRecipeInput> targetInputs = new NNList<>();

			for (int i = 0; i < inputs.length; i++) {
				targetInputs.add(new MachineRecipeInput(i, CraftTweakerMC.getItemStack(inputs[i])));
			}

			TriItemLookup<IManyToOneRecipe> newLookup = AlloySmelterRecipe.createLookup();

			int removedCount = 0;

			for (IManyToOneRecipe recipe : alloyRecipes) {
				if (recipe != null && recipe.isInputForRecipe(targetInputs)) {
					removedCount++;
				} else {
					AlloySmelterRecipe.addRecipeToLookup(newLookup, recipe);
				}
			}
			if (removedCount > 0) {
				AlloySmelterRecipe.setNewLookup(newLookup);
				CraftTweakerAPI.logInfo("Removed " + removedCount + " recipes matching given inputs.");
			} else {
				CraftTweakerAPI.logError("No Alloy Smelter recipes found matching given inputs.");
			}
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
