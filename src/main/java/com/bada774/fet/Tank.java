package com.bada774.fet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.enderio.core.common.util.NNList;
import com.enderio.core.common.util.stackable.Things;

import com.bada774.fet.utils.LateAction;
import com.bada774.fet.utils.Logging;
import com.bada774.fet.utils.RecipeUtils;
import com.bada774.fet.utils.ValidationUtils;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crazypants.enderio.base.recipe.IMachineRecipe;
import crazypants.enderio.base.recipe.MachineRecipeRegistry;
import crazypants.enderio.base.recipe.RecipeLevel;
import crazypants.enderio.base.recipe.tank.TankMachineRecipe;
import crazypants.enderio.base.recipe.tank.TankMachineRecipe.Logic;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass(Tank.ZEN_CLASS)
@ZenRegister
public class Tank {

	public static final String MACHINE_NAME = "Tank",
			ITEM_TYPE = "recipe",

			ZEN_CLASS = "mods.enderio." + MACHINE_NAME,

			METHOD_ADD_RECIPE = "addRecipe",
			METHOD_REMOVE_RECIPE = "removeRecipe";

	@ZenMethod
	public static void addRecipe(boolean fill, IIngredient input, ILiquidStack fluid, IItemStack output) {

		if (hasErrors(input, fluid, METHOD_ADD_RECIPE))
			return;

		CraftTweakerAPI.apply(new AddRecipeAction(fill, input, fluid, output));
	}

	@ZenMethod
	public static void removeRecipe(boolean fill, IItemStack input, @Optional ILiquidStack fluid) {

		if (hasErrors(input, METHOD_REMOVE_RECIPE))
			return;

		CraftTweakerAPI.apply(new RemoveRecipeAction(fill, input, fluid));
	}

	public static class AddRecipeAction extends LateAction {
		public final boolean fill;
		public final IIngredient input;
		public final FluidStack fluid;
		public final ItemStack output;

		public final String logName;

		public TankMachineRecipe createdRecipe;

		private final String fillingMode;

		public AddRecipeAction(boolean fill, IIngredient input, ILiquidStack fluid, IItemStack output) {
			this.fill = fill;
			this.input = input;
			this.fluid = CraftTweakerMC.getLiquidStack(fluid);
			this.output = output != null ? CraftTweakerMC.getItemStack(output) : ItemStack.EMPTY;
			this.fillingMode = fill ? "Filling" : "Emptying";

			String outName = output != null ? output.getDisplayName() : "None (Consumed)";
			this.logName = String.format("%s (Fluid: %s, Mode: %s)", outName, this.fluid.getLocalizedName(),
					fillingMode);

		}

		private String checkConflict() {
			String registryId = fill ? MachineRecipeRegistry.TANK_FILLING : MachineRecipeRegistry.TANK_EMPTYING;
			Map<String, ? extends IMachineRecipe> recipes = MachineRecipeRegistry.instance
					.getRecipesForMachine(registryId);

			if (recipes == null || ValidationUtils.isInvalid(input))
				return null;

			for (IMachineRecipe existing : recipes.values()) {
				if (!(existing instanceof TankMachineRecipe))
					continue;
				TankMachineRecipe tankRecipe = (TankMachineRecipe) existing;

				Things exInput = tankRecipe.getInput();

				if (exInput == null || !exInput.isValid())
					continue;

				boolean itemMatches = false;
				for (IItemStack item : input.getItems()) {
					if (exInput.contains(CraftTweakerMC.getItemStack(item))) {
						itemMatches = true;
						break;
					}
				}
				if (!itemMatches)
					continue;

				if (fill) {
					if (tankRecipe.getFluid() == null || tankRecipe.getFluid().getFluid() == null)
						continue;
					if (!tankRecipe.getFluid().getFluid().equals(fluid.getFluid()))
						continue;
				}

				Things exOutput = tankRecipe.getOutput();
				ItemStack outStack = (exOutput != null && exOutput.getItemStacks() != null
						&& !exOutput.getItemStacks().isEmpty())
								? exOutput.getItemStacks().get(0)
								: ItemStack.EMPTY;

				return RecipeUtils.getConflictingOutputName(outStack);
			}
			return null;
		}

		@Override
		public void execute() {

			if (this.input != null && this.input.getItems() != null) {
				for (IItemStack iStack : this.input.getItems()) {
					ItemStack mcStack = CraftTweakerMC.getItemStack(iStack);
					if (mcStack != null && !mcStack.isEmpty()
							&& mcStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
						Logging.logValidationError(MACHINE_NAME, METHOD_ADD_RECIPE, String.format(
								"Failed to add %s for: %s\nThe input item '%s' is a native Forge fluid container (e.g. a bucket)\nEnderIO Tank handles these automatically via Forge mechanics",
								ITEM_TYPE, logName, iStack.getDisplayName()));
						return;
					}
				}
			}

			String conflictingName = checkConflict();

			if (conflictingName != null) {
				Logging.logValidationError(MACHINE_NAME, METHOD_ADD_RECIPE, String.format(
						"Failed to add %s for: %s\nA %s already exists for this exact input and fluid in %s mode!\nConflicting %s output: %s",
						ITEM_TYPE, logName, ITEM_TYPE, fillingMode, ITEM_TYPE, conflictingName));
				return;
			}

			Things inThing = RecipeUtils.buildThings(input);
			Things outThing = new Things();
			if (!this.output.isEmpty()) {
				outThing.add(this.output);
			}

			String inputDisplayName = "Unknown Input";
			if (input != null && input.getItems() != null && !input.getItems().isEmpty()) {
				inputDisplayName = input.getItems().get(0).getDisplayName();
			}
			String uid = String.format("FET_Tank: %s, %s with %s", inputDisplayName, fillingMode,
					fluid.getLocalizedName());

			this.createdRecipe = new TankMachineRecipe(uid, fill, inThing, fluid, outThing, Logic.NONE,
					RecipeLevel.IGNORE);

			MachineRecipeRegistry.instance.registerRecipe(this.createdRecipe);

			Logging.logAddition(MACHINE_NAME, METHOD_ADD_RECIPE, ITEM_TYPE, logName);

		}

		@Override
		public String describe() {
			return String.format("Adding %s %s %s for: %s", MACHINE_NAME, fillingMode, ITEM_TYPE, logName);
		}

	}

	public static class RemoveRecipeAction extends LateAction {
		public final boolean fill;
		public final ItemStack inputStack;
		public final FluidStack fluid;

		public final String logName;

		public List<IMachineRecipe> backupRecipes = new ArrayList<>();

		private final String fillingMode;

		public RemoveRecipeAction(boolean fill, IItemStack input, ILiquidStack fluid) {
			this.fill = fill;
			this.inputStack = CraftTweakerMC.getItemStack(input);
			this.fluid = fluid != null ? CraftTweakerMC.getLiquidStack(fluid) : null;
			this.fillingMode = fill ? "Filling" : "Emptying";

			String fluidDesc = fluid != null ? this.fluid.getLocalizedName() : "Any Fluid";
			this.logName = String.format("Input: %s (Fluid: %s, Mode: %s)", input.getDisplayName(), fluidDesc,
					fillingMode);
		}

		@Override
		public void execute() {
			backupRecipes.clear();

			if (this.inputStack != null && !this.inputStack.isEmpty()
					&& this.inputStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
				Logging.logValidationError(MACHINE_NAME, METHOD_REMOVE_RECIPE, String.format(
						"Cannot remove %s for '%s'.\nIt is a native Forge fluid container (e.g. a bucket)\nEnderIO Tank handles these automatically via Forge mechanics",
						ITEM_TYPE, inputStack.getDisplayName()));
				return;
			}

			String registryId = fill ? MachineRecipeRegistry.TANK_FILLING : MachineRecipeRegistry.TANK_EMPTYING;

			Map<String, ? extends IMachineRecipe> recipes = MachineRecipeRegistry.instance
					.getRecipesForMachine(registryId);

			for (IMachineRecipe recipe : recipes.values()) {
				if (recipe instanceof TankMachineRecipe) {
					TankMachineRecipe tankRecipe = (TankMachineRecipe) recipe;
					Things recipeInput = tankRecipe.getInput();

					if (recipeInput != null && recipeInput.contains(inputStack)) {
						if (this.fluid == null || (tankRecipe.getFluid() != null &&
								tankRecipe.getFluid().getFluid() != null &&
								tankRecipe.getFluid().getFluid().equals(this.fluid.getFluid()))) {

							backupRecipes.add(recipe);
						}
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
			return String.format("Removing %s %s %s for: %s", MACHINE_NAME, fillingMode, ITEM_TYPE, logName);
		}
	}

	private static boolean hasErrors(IIngredient input, ILiquidStack fluid, String methodName) {
		if (ValidationUtils.isInvalid(input)) {
			Logging.logValidationError(MACHINE_NAME, methodName, "Invalid input: null or empty");
			return true;
		}
		if (fluid == null) {
			Logging.logValidationError(MACHINE_NAME, methodName, "Invalid fluid: null");
			return true;
		}
		return false;
	}

	private static boolean hasErrors(IItemStack input, String methodName) {
		if (ValidationUtils.isInvalid(input)) {
			Logging.logValidationError(MACHINE_NAME, methodName, "Input item cannot be null or empty");
			return true;
		}
		return false;
	}
}
