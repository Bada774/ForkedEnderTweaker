package com.bada774.fet;

import java.util.Map;

import com.bada774.fet.recipe.machines.CombustionGenRecipes;
import com.bada774.fet.utils.LateAction;
import com.bada774.fet.utils.Logging;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crazypants.enderio.base.fluid.FluidFuelRegister;
import crazypants.enderio.base.fluid.IFluidCoolant;
import crazypants.enderio.base.fluid.IFluidFuel;
import net.minecraftforge.fluids.Fluid;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass(CombustionGen.ZEN_CLASS)
@ZenRegister
public class CombustionGen {

	public final static String
			MACHINE_NAME = "CombustionGen",
			FUEL_TYPE = "fuel",
			COOLANT_TYPE = "coolant",

			ZEN_CLASS = "mods.enderio." + MACHINE_NAME,

			METHOD_ADD_FUEL = "addFuel",
			METHOD_REMOVE_FUEL = "removeFuel",
			METHOD_ADD_COOLANT = "addCoolant",
			METHOD_REMOVE_COOLANT = "removeCoolant";

	@ZenMethod
	public static void addFuel(ILiquidStack fuel, int powerPerCycleRF, int totalBurnTime) {

		if (hasErrors(fuel, powerPerCycleRF, totalBurnTime, METHOD_ADD_FUEL, FUEL_TYPE)) {
			return;
		}
		CraftTweakerAPI.apply(new AddFuelAction(fuel, powerPerCycleRF, totalBurnTime));
	}

	@ZenMethod
	public static void removeFuel(ILiquidStack fuel) {

		if (hasErrors(fuel, METHOD_REMOVE_FUEL, FUEL_TYPE)) {
			return;
		}
		CraftTweakerAPI.apply(new RemoveFuelAction(fuel));
	}

	@ZenMethod
	public static void addCoolant(ILiquidStack coolant, float degreesCoolingPerMB) {

		if (hasErrors(coolant, degreesCoolingPerMB, METHOD_ADD_COOLANT, COOLANT_TYPE)) {
			return;
		}
		CraftTweakerAPI.apply(new AddCoolantAction(coolant, degreesCoolingPerMB));
	}

	@ZenMethod
	public static void removeCoolant(ILiquidStack coolant) {

		if (hasErrors(coolant, METHOD_REMOVE_COOLANT, COOLANT_TYPE)) {
			return;
		}
		CraftTweakerAPI.apply(new RemoveCoolantAction(coolant));
	}

	public static class AddFuelAction extends LateAction {
		public final Fluid fluid;
		public final int powerPerCycleRF;
		public final int totalBurnTime;
		public final String fluidName;
		public final String logName;

		public IFluidFuel addedFuel;



		AddFuelAction(ILiquidStack fuel, int powerPerCycleRF, int totalBurnTime) {
			this.fluid = CraftTweakerMC.getLiquidStack(fuel).getFluid();
			this.powerPerCycleRF = powerPerCycleRF;
			this.totalBurnTime = totalBurnTime;
			this.fluidName = fluid.getName();

			this.logName = CraftTweakerMC.getLiquidStack(fuel).getLocalizedName();
		}

		@Override
		public void execute() {
			String conflictType = checkConflict(fluid);

			if (conflictType != null) {
				Logging.logValidationError(MACHINE_NAME, METHOD_ADD_FUEL, String.format(
						"Failed to add %s for: %s\nFluid already registered as a %s!", FUEL_TYPE, logName,
						conflictType));
				return;
			}

			FluidFuelRegister.instance.addFuel(fluid, powerPerCycleRF, totalBurnTime);

			this.addedFuel = CombustionGenRecipes.getFuels().get(fluidName);

			Logging.logAddition(MACHINE_NAME, METHOD_ADD_FUEL, FUEL_TYPE, logName);
		}

		@Override
		public String describe() {
			return String.format("Adding %s %s by %s: %s", MACHINE_NAME, FUEL_TYPE, METHOD_ADD_FUEL, logName);
		}
	}

	public static class RemoveFuelAction extends LateAction {
		public final String fluidName;

		public final String logName;
		public IFluidFuel backupFuel;

		RemoveFuelAction(ILiquidStack fuel) {
			this.fluidName = CraftTweakerMC.getLiquidStack(fuel).getFluid().getName();

			this.logName = CraftTweakerMC.getLiquidStack(fuel).getLocalizedName();
		}

		@Override
		public void execute() {
			Map<String, IFluidFuel> fuels = CombustionGenRecipes.getFuels();
			this.backupFuel = fuels.remove(fluidName);

			if (this.backupFuel != null) {
				Logging.logRemoval(MACHINE_NAME, METHOD_REMOVE_FUEL, FUEL_TYPE, logName, null);
			} else
				Logging.logRemoval(MACHINE_NAME, METHOD_REMOVE_FUEL, FUEL_TYPE, null, logName);

		}

		@Override
		public String describe() {
			return String.format("Removing %s %s by %s: %s", MACHINE_NAME, FUEL_TYPE, METHOD_REMOVE_FUEL, logName);
		}
	}

	public static class AddCoolantAction extends LateAction {
		public final Fluid fluid;
		public final float degreesCoolingPerMB;
		public final String fluidName;

		public final String logName;

		public IFluidCoolant addedCoolant;

		AddCoolantAction(ILiquidStack coolant, float degreesCoolingPerMB) {
			this.fluid = CraftTweakerMC.getLiquidStack(coolant).getFluid();
			this.degreesCoolingPerMB = degreesCoolingPerMB;
			this.fluidName = CraftTweakerMC.getLiquidStack(coolant).getFluid().getName();

			this.logName = CraftTweakerMC.getLiquidStack(coolant).getLocalizedName();
		}

		@Override
		public void execute() {
			String conflictType = checkConflict(fluid);
			if (conflictType != null) {
				Logging.logValidationError(MACHINE_NAME, METHOD_ADD_COOLANT, String.format(
						"Failed to add %s for: %s\nFluid already registered as a %s!", COOLANT_TYPE, logName,
						conflictType));
				return;
			}

			FluidFuelRegister.instance.addCoolant(fluid, degreesCoolingPerMB);

			this.addedCoolant = CombustionGenRecipes.getCoolants().get(fluidName);

			Logging.logAddition(MACHINE_NAME, METHOD_ADD_COOLANT, COOLANT_TYPE, logName);
		}

		@Override
		public String describe() {
			return String.format("Adding %s %s by %s: %s", MACHINE_NAME, COOLANT_TYPE, METHOD_ADD_COOLANT, logName);
		}
	}

	public static class RemoveCoolantAction extends LateAction {
		public final String fluidName;

		public final String logName;
		public IFluidCoolant backupCoolant;

		RemoveCoolantAction(ILiquidStack coolant) {
			this.fluidName = CraftTweakerMC.getLiquidStack(coolant).getFluid().getName();

			this.logName = CraftTweakerMC.getLiquidStack(coolant).getLocalizedName();
		}

		@Override
		public void execute() {
			Map<String, IFluidCoolant> coolants = CombustionGenRecipes.getCoolants();
			this.backupCoolant = coolants.remove(fluidName);

			if (this.backupCoolant != null) {
				Logging.logRemoval(MACHINE_NAME, METHOD_REMOVE_COOLANT, COOLANT_TYPE, logName, null);
			} else {
				Logging.logRemoval(MACHINE_NAME, METHOD_REMOVE_COOLANT, COOLANT_TYPE, null, logName);
			}
		}

		@Override
		public String describe() {
			return String.format("Removing %s %s by %s: %s", MACHINE_NAME, COOLANT_TYPE, METHOD_REMOVE_COOLANT,
					logName);
		}
	}

	private static boolean hasErrors(ILiquidStack liquid, String methodName, String itemType) {
		if (liquid == null) {
			Logging.logValidationError(MACHINE_NAME, methodName, String.format("Invalid %s: null", itemType));
			return true;
		}
		return false;
	}

	private static boolean hasErrors(ILiquidStack liquid, int power, int time, String methodName, String itemType) {
		if (hasErrors(liquid, methodName, itemType))
			return true;
		if (power < 0 || time < 0) {
			Logging.logValidationError(MACHINE_NAME, methodName, "Power and Time must be >= 0");
			return true;
		}
		return false;
	}

	private static boolean hasErrors(ILiquidStack liquid, float degrees, String methodName, String itemType) {
		if (hasErrors(liquid, methodName, itemType))
			return true;

		if (degrees < 0) {
			Logging.logValidationError(MACHINE_NAME, methodName, "Degrees cooling must be > 0");
			return true;
		}
		return false;
	}

	private static String checkConflict(Fluid fluid) {
		if (FluidFuelRegister.instance.getFuel(fluid) != null)
			return FUEL_TYPE;
		if (FluidFuelRegister.instance.getCoolant(fluid) != null)
			return COOLANT_TYPE;
		return null;
	}

}
