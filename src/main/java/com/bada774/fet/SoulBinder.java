package com.bada774.fet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bada774.fet.recipe.machines.SoulBinderRecipe;
import com.bada774.fet.utils.LateAction;
import com.bada774.fet.utils.Logging;
import com.bada774.fet.utils.RecipeUtils;
import com.bada774.fet.utils.ValidationUtils;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crazypants.enderio.base.recipe.IMachineRecipe;
import crazypants.enderio.base.recipe.MachineRecipeRegistry;
import crazypants.enderio.base.recipe.RecipeLevel;
import crazypants.enderio.base.recipe.soul.ISoulBinderRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass(SoulBinder.ZEN_CLASS)
@ZenRegister
public class SoulBinder {

	public static final String
			MACHINE_NAME = "SoulBinder",
			ITEM_TYPE = "recipe",

			ZEN_CLASS = "mods.enderio." + MACHINE_NAME,

			METHOD_ADD_RECIPE = "addRecipe",
			METHOD_REMOVE_RECIPE = "removeRecipe";

	@ZenMethod
	public static void addRecipe(IItemStack output, IIngredient input, String[] entities, int xp,
			@Optional int energyCost) {

		if (hasErrors(output, input, entities, xp, METHOD_ADD_RECIPE)) {
			return;
		}

		CraftTweakerAPI.apply(new AddRecipeAction(output, input, entities, xp, energyCost));
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
		public final IIngredient input;
		public final ResourceLocation[] entities;
		public final int xp;
		public final int energyCost;
		public final String logName;

		public ISoulBinderRecipe createdRecipe;

		public AddRecipeAction(IItemStack output, IIngredient input, String[] entities, int xp, int energyCost) {
			this.output = CraftTweakerMC.getItemStack(output);
			this.input = input;
			this.xp = xp;
			this.energyCost = energyCost <= 0 ? 5000 : energyCost;
			this.logName = output.getDisplayName();

			this.entities = new ResourceLocation[entities.length];
			for (int i = 0; i < entities.length; i++) {
				this.entities[i] = new ResourceLocation(entities[i]);
			}
		}

		private String checkConflict() {
			Map<String, ? extends IMachineRecipe> recipes = MachineRecipeRegistry.instance
					.getRecipesForMachine(MachineRecipeRegistry.SOULBINDER);

			if (recipes == null)
				return null;

			for (IMachineRecipe existing : recipes.values()) {
				if (!(existing instanceof ISoulBinderRecipe))
					continue;

				ISoulBinderRecipe sbRecipe = (ISoulBinderRecipe) existing;
				ItemStack exInput = sbRecipe.getInputStack();

				if (exInput == null || exInput.isEmpty())
					continue;

				if (!input.matches(CraftTweakerMC.getIItemStack(exInput)))
					continue;

				List<ResourceLocation> exSouls = sbRecipe.getSupportedSouls();
				if (exSouls == null || exSouls.isEmpty())
					continue;

				for (ResourceLocation entity : entities) {
					if (exSouls.contains(entity)) {
						return RecipeUtils.getConflictingOutputName(sbRecipe.getOutputStack());
					}
				}
			}
			return null;
		}

		@Override
		public void execute() {
			String conflictingName = checkConflict();

			if (conflictingName != null) {
				Logging.logValidationError(MACHINE_NAME, METHOD_ADD_RECIPE, String.format(
						"Failed to add %s for: %s\nA %s already exists for this item and entity combination!\nConflicting %s output: %s",
						ITEM_TYPE, logName, ITEM_TYPE, ITEM_TYPE, conflictingName));
				return;
			}

			this.createdRecipe = new SoulBinderRecipe(input, output, energyCost, xp, RecipeLevel.IGNORE, entities);

			MachineRecipeRegistry.instance.registerRecipe(this.createdRecipe);
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

		public List<IMachineRecipe> backupRecipes = new ArrayList<>();

		public RemoveRecipeAction(IItemStack output) {
			this.output = CraftTweakerMC.getItemStack(output);
			this.logName = output.getDisplayName();
		}

		@Override
		public void execute() {
			backupRecipes.clear();

			Map<String, ? extends IMachineRecipe> recipes = MachineRecipeRegistry.instance
					.getRecipesForMachine(MachineRecipeRegistry.SOULBINDER);

			for (IMachineRecipe recipe : recipes.values()) {
				if (recipe instanceof ISoulBinderRecipe) {
					if (OreDictionary.itemMatches(output, ((ISoulBinderRecipe) recipe).getOutputStack(), false)) {
						backupRecipes.add(recipe);
					}
				}
			}
			if (!backupRecipes.isEmpty()) {
				for (IMachineRecipe recipe : backupRecipes) {
					MachineRecipeRegistry.instance.removeRecipe(recipe);
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

	private static boolean hasErrors(IItemStack output, IIngredient input, String[] entities, int xp,
			String methodName) {
		if (hasErrors(output, methodName))
			return true;
		if (ValidationUtils.isInvalid(input)) {
			Logging.logValidationError(MACHINE_NAME, methodName, "Invalid input: null or empty");
			return true;
		}
		if (ValidationUtils.isInvalid(entities)) {
			Logging.logValidationError(MACHINE_NAME, methodName, "Entities list cannot be null or empty");
			return true;
		}
		if (xp < 0) {
			Logging.logValidationError(MACHINE_NAME, methodName, "XP levels cannot be negative");
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
