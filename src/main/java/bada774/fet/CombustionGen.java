package bada774.fet;

import java.util.Map;

import bada774.fet.recipe.machines.CombustionGenRecipes;
import bada774.fet.utils.LateAction;
import bada774.fet.utils.Logging;
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

@ZenClass("mods.enderio.CombustionGen")
@ZenRegister
public class CombustionGen {

	private final static String MACHINE_NAME = "CombustionGen";

	private static final String METHOD_ADD_FUEL = "addFuel",
			METHOD_REMOVE_FUEL = "removeFuel",
			METHOD_ADD_COOLANT = "addCoolant",
			METHOD_REMOVE_COOLANT = "removeCoolant";

	private static final String TYPE_FUEL = "fuel",
			TYPE_COOLANT = "coolant";

	@ZenMethod
	public static void addFuel(ILiquidStack fuel, int powerPerCycleRF, int totalBurnTime) {

		if (hasErrors(fuel, powerPerCycleRF, totalBurnTime, METHOD_ADD_FUEL, TYPE_FUEL)) {
			return;
		}
		CraftTweakerAPI.apply(new AddFuelAction(fuel, powerPerCycleRF, totalBurnTime));
	}

	@ZenMethod
	public static void removeFuel(ILiquidStack fuel) {

		if (hasErrors(fuel, METHOD_REMOVE_FUEL, TYPE_FUEL)) {
			return;
		}
		CraftTweakerAPI.apply(new RemoveFuelAction(fuel));
	}

	@ZenMethod
	public static void addCoolant(ILiquidStack coolant, float degreesCoolingPerMB) {

		if (hasErrors(coolant, degreesCoolingPerMB, METHOD_ADD_COOLANT, TYPE_COOLANT)) {
			return;
		}
		CraftTweakerAPI.apply(new AddCoolantAction(coolant, degreesCoolingPerMB));
	}

	@ZenMethod
	public static void removeCoolant(ILiquidStack coolant) {

		if (hasErrors(coolant, METHOD_REMOVE_COOLANT, TYPE_COOLANT)) {
			return;
		}
		CraftTweakerAPI.apply(new RemoveCoolantAction(coolant));
	}

	public static class AddFuelAction extends LateAction {
		public final Fluid fluid;
		public final int powerPerCycleRF;
		public final int totalBurnTime;
		public final String logName;

		public IFluidFuel addedFuel;

		AddFuelAction(ILiquidStack fuel, int powerPerCycleRF, int totalBurnTime) {
			this.fluid = CraftTweakerMC.getLiquidStack(fuel).getFluid();
			this.powerPerCycleRF = powerPerCycleRF;
			this.totalBurnTime = totalBurnTime;
			this.logName = CraftTweakerMC.getLiquidStack(fuel).getLocalizedName();
		}

		@Override
		public void execute() {
			String conflictType = checkConflict(fluid);

			if (conflictType != null) {
				Logging.logValidationError(MACHINE_NAME, METHOD_ADD_FUEL, String.format(
						"Failed to add %s for: %s\nFluid already registered as a %s!", TYPE_FUEL, logName,
						conflictType));
				return;
			}

			FluidFuelRegister.instance.addFuel(fluid, powerPerCycleRF, totalBurnTime);

			this.addedFuel = CombustionGenRecipes.getFuels().get(logName);

			Logging.logAddition(MACHINE_NAME, METHOD_ADD_FUEL, TYPE_FUEL, logName);
		}

		@Override
		public String describe() {
			return String.format("Adding %s %s by %s: %s", MACHINE_NAME, TYPE_FUEL, METHOD_ADD_FUEL, logName);
		}
	}

	public static class RemoveFuelAction extends LateAction {
		public final String logName;
		public IFluidFuel backupFuel;

		RemoveFuelAction(ILiquidStack fuel) {
			this.logName = CraftTweakerMC.getLiquidStack(fuel).getLocalizedName();
		}

		@Override
		public void execute() {
			Map<String, IFluidFuel> fuels = CombustionGenRecipes.getFuels();
			this.backupFuel = fuels.remove(logName);

			if (this.backupFuel != null) {
				Logging.logRemoval(MACHINE_NAME, METHOD_REMOVE_FUEL, TYPE_FUEL, logName, null);
			} else
				Logging.logRemoval(MACHINE_NAME, METHOD_REMOVE_FUEL, TYPE_FUEL, null, logName);

		}

		@Override
		public String describe() {
			return String.format("Removing %s %s by %s: %s", MACHINE_NAME, TYPE_FUEL, METHOD_REMOVE_FUEL, logName);
		}
	}

	public static class AddCoolantAction extends LateAction {
		public final Fluid fluid;
		public final float degreesCoolingPerMB;
		public final String logName;

		public IFluidCoolant addedCoolant;

		AddCoolantAction(ILiquidStack coolant, float degreesCoolingPerMB) {
			this.fluid = CraftTweakerMC.getLiquidStack(coolant).getFluid();
			this.degreesCoolingPerMB = degreesCoolingPerMB;
			this.logName = CraftTweakerMC.getLiquidStack(coolant).getLocalizedName();
		}

		@Override
		public void execute() {
			String conflictType = checkConflict(fluid);
			if (conflictType != null) {
				Logging.logValidationError(MACHINE_NAME, METHOD_ADD_COOLANT, String.format(
						"Failed to add %s for: %s\nFluid already registered as a %s!", TYPE_COOLANT, logName,
						conflictType));
				return;
			}

			FluidFuelRegister.instance.addCoolant(fluid, degreesCoolingPerMB);

			this.addedCoolant = CombustionGenRecipes.getCoolants().get(logName);

			Logging.logAddition(MACHINE_NAME, METHOD_ADD_COOLANT, TYPE_COOLANT, logName);
		}

		@Override
		public String describe() {
			return String.format("Adding %s %s by %s: %s", MACHINE_NAME, TYPE_COOLANT, METHOD_ADD_COOLANT, logName);
		}
	}

	public static class RemoveCoolantAction extends LateAction {
		public final String logName;
		public IFluidCoolant backupCoolant;

		RemoveCoolantAction(ILiquidStack coolant) {
			this.logName = CraftTweakerMC.getLiquidStack(coolant).getLocalizedName();
		}

		@Override
		public void execute() {
			Map<String, IFluidCoolant> coolants = CombustionGenRecipes.getCoolants();
			this.backupCoolant = coolants.remove(logName);

			if (this.backupCoolant != null) {
				Logging.logRemoval(MACHINE_NAME, METHOD_REMOVE_COOLANT, TYPE_COOLANT, logName, null);
			} else {
				Logging.logRemoval(MACHINE_NAME, METHOD_REMOVE_COOLANT, TYPE_COOLANT, null, logName);
			}
		}

		@Override
		public String describe() {
			return String.format("Removing %s %s by %s: %s", MACHINE_NAME, TYPE_COOLANT, METHOD_REMOVE_COOLANT,
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
			return TYPE_FUEL;
		if (FluidFuelRegister.instance.getCoolant(fluid) != null)
			return TYPE_COOLANT;
		return null;
	}

}
