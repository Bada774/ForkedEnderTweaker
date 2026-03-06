package bada774.fet;

import java.util.Map;

import com.enderio.core.common.util.NNList;
import com.enderio.core.common.util.stackable.Things;

import bada774.fet.recipe.machines.EnchanterRecipes;
import bada774.fet.utils.LateAction;
import bada774.fet.utils.Logging;
import bada774.fet.utils.ValidationUtils;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.enchantments.IEnchantmentDefinition;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crazypants.enderio.base.recipe.IMachineRecipe;
import crazypants.enderio.base.recipe.MachineRecipeRegistry;
import crazypants.enderio.base.recipe.RecipeLevel;
import crazypants.enderio.base.recipe.enchanter.EnchanterRecipe;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.enderio.Enchanter")
@ZenRegister
public class Enchanter {

	private final static String MACHINE_NAME = "Enchanter",
			ITEM_TYPE = "recipe";

	private static final String METHOD_ADD_RECIPE = "addRecipe",
			METHOD_REMOVE_RECIPE = "removeRecipe";

	@ZenMethod
	public static void addRecipe(IEnchantmentDefinition output, IIngredient input, int amountPerLevel,
			double costMultiplier) {

		if (hasErrors(output, input, amountPerLevel, costMultiplier, METHOD_ADD_RECIPE)) {
			return;
		}
		CraftTweakerAPI.apply(new AddRecipeAction(output, input, amountPerLevel, costMultiplier));
	}

	@ZenMethod
	public static void removeRecipe(IEnchantmentDefinition output) {

		if (hasErrors(output, METHOD_REMOVE_RECIPE)) {
			return;
		}
		CraftTweakerAPI.apply(new RemoveRecipeAction(output));
	}

	public static class AddRecipeAction extends LateAction {
		public final Enchantment enchantment;
		public final IIngredient input;
		public final int amountPerLevel;
		public final double costMultiplier;
		public final String logName;

		public EnchanterRecipe createdRecipe;

		public AddRecipeAction(IEnchantmentDefinition output, IIngredient input, int amountPerLevel,
				double costMultiplier) {
			this.enchantment = (Enchantment) output.getInternal();
			this.input = input;
			this.amountPerLevel = amountPerLevel;
			this.costMultiplier = costMultiplier;
			this.logName = output.getName();
		}

		private String checkConflict() {
			Map<String, ? extends IMachineRecipe> recipes = MachineRecipeRegistry.instance
					.getRecipesForMachine(MachineRecipeRegistry.ENCHANTER);

			if (recipes == null)
				return null;

			for (IMachineRecipe existing : recipes.values()) {
				if (!(existing instanceof EnchanterRecipe))
					continue;

				EnchanterRecipe enchRecipe = (EnchanterRecipe) existing;
				Things exInput = enchRecipe.getInput();

				if (exInput == null || !exInput.isValid())
					continue;

				for (IItemStack ctItem : input.getItems()) {
					ItemStack mcStack = CraftTweakerMC.getItemStack(ctItem);

					if (exInput.contains(mcStack)) {
						return enchRecipe.getEnchantment().getRegistryName().toString();
					}
				}
			}
			return null;
		}

		@Override
		public void execute() {

			Things thing = new Things().add(new NNList<>(CraftTweakerMC.getIngredient(input).getMatchingStacks()));

			if (thing.isEmpty()) {
				Logging.logValidationError(MACHINE_NAME, METHOD_ADD_RECIPE,
						"Input ingredient matches no items: " + (input != null ? input.toCommandString() : "null"));
				return;
			}

			String conflictingName = checkConflict();

			if (conflictingName != null) {
				Logging.logValidationError(MACHINE_NAME, METHOD_ADD_RECIPE, String.format(
						"Failed to add %s for: %s\nThe input is already used for another enchantment: %s",
						ITEM_TYPE, logName, conflictingName));
				return;
			}

			this.createdRecipe = new EnchanterRecipe(RecipeLevel.IGNORE, thing,
					amountPerLevel, enchantment, costMultiplier);

			MachineRecipeRegistry.instance.registerRecipe(this.createdRecipe);
			Logging.logAddition(MACHINE_NAME, METHOD_ADD_RECIPE, ITEM_TYPE, logName);
		}

		@Override
		public String describe() {
			return String.format("Adding %s %s by %s for: %s", MACHINE_NAME, METHOD_ADD_RECIPE, ITEM_TYPE,
					logName);
		}
	}

	public static class RemoveRecipeAction extends LateAction {
		public final Enchantment enchantment;
		public final String logName;

		public EnchanterRecipe backupRecipe;

		public RemoveRecipeAction(IEnchantmentDefinition output) {
			this.enchantment = (Enchantment) output.getInternal();
			this.logName = output.getName();
		}

		@Override
		public void execute() {
			EnchanterRecipe recipe = EnchanterRecipes.getRecipeByEnchantment(enchantment);
			if (recipe != null) {
				this.backupRecipe = recipe;
				MachineRecipeRegistry.instance.removeRecipe(recipe);
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

	private static boolean hasErrors(IEnchantmentDefinition output, IIngredient input, int amount, double cost,
			String methodName) {
		if (hasErrors(output, methodName))
			return true;

		if (ValidationUtils.isInvalid(input)) {
			Logging.logValidationError(MACHINE_NAME, methodName, "Input ingredient is null or empty");
			return true;
		}
		if (amount <= 0 || cost <= 0) {
			Logging.logValidationError(MACHINE_NAME, methodName, "Amount per level and Cost multiplier must be > 0");
			return true;
		}

		int maxStackSize = 0;
		for (IItemStack item : input.getItems()) {
			if (item.getMaxStackSize() > maxStackSize)
				maxStackSize = item.getMaxStackSize();
		}

		if (amount > maxStackSize) {
			Logging.logValidationError(MACHINE_NAME, methodName, String.format(
					"Amount per level (%d) exceeds the maximum stack size (%d) of the inputs", amount, maxStackSize));
			return true;
		}
		return false;
	}

	private static boolean hasErrors(IEnchantmentDefinition output, String methodName) {
		if (output == null || output.getInternal() == null) {
			Logging.logValidationError(MACHINE_NAME, methodName, "Enchantment cannot be null");
			return true;
		}
		return false;
	}
}
