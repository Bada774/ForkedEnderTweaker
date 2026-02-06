package bada774.endertweaker;

import java.util.Map;

import bada774.endertweaker.recipe.machines.CombustionGenRecipe;
import bada774.endertweaker.utils.LateAction;

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

	@ZenMethod
	public static void addFuel(ILiquidStack fuel, int powerPerCycleRF, int totalBurnTime) {
		if (fuel == null) {
			CraftTweakerAPI.logError("Cannot add fuel to Combustion Generator: null");
			return;
		}
		CraftTweakerAPI.apply(new AddFuelAction(fuel, powerPerCycleRF, totalBurnTime));
	}

	@ZenMethod
	public static void removeFuel(ILiquidStack fuel) {
		if (fuel == null) {
			CraftTweakerAPI.logError("Cannot remove fuel from Combustion Generator: null");
			return;
		}
		CraftTweakerAPI.apply(new RemoveFuelAction(fuel));
	}

	@ZenMethod
	public static void addCoolant(ILiquidStack coolant, float degreesCoolingPerMB) {
		if (coolant == null) {
			CraftTweakerAPI.logError("Cannot add coolant to Combustion Generator: null");
			return;
		}
		CraftTweakerAPI.apply(new AddCoolantAction(coolant, degreesCoolingPerMB));
	}

	@ZenMethod
	public static void removeCoolant(ILiquidStack coolant) {
		if (coolant == null) {
			CraftTweakerAPI.logError("Cannot remove coolant from Combustion Generator: null");
			return;
		}
		CraftTweakerAPI.apply(new RemoveCoolantAction(coolant));
	}

	public static class AddFuelAction extends LateAction {
		public final Fluid fluid;
		public final int powerPerCycleRF;
		public final int totalBurnTime;
		public final String fluidName;

		AddFuelAction(ILiquidStack fuel, int powerPerCycleRF, int totalBurnTime) {
			this.fluid = CraftTweakerMC.getLiquidStack(fuel).getFluid();
			this.powerPerCycleRF = powerPerCycleRF;
			this.totalBurnTime = totalBurnTime;
			this.fluidName = fluid.getName();
		}

		@Override
		public void execute() {
			FluidFuelRegister.instance.addFuel(fluid, powerPerCycleRF, totalBurnTime);
		}

		@Override
		public String describe() {
			return "Adding Combustion Gen fuel: " + fluidName;
		}
	}

	public static class RemoveFuelAction extends LateAction {
		public final String fluidName;
		public IFluidFuel backupFuel;

		RemoveFuelAction(ILiquidStack fuel) {
			this.fluidName = CraftTweakerMC.getLiquidStack(fuel).getFluid().getName();
		}

		@Override
		public void execute() {
			Map<String, IFluidFuel> fuels = CombustionGenRecipe.getFuels();
			if (fuels.containsKey(fluidName)) {
				backupFuel = fuels.get(fluidName);
				fuels.remove(fluidName);
			} else
				CraftTweakerAPI.logWarning("No Combustion Gen fuel found: " + fluidName);

		}

		@Override
		public String describe() {
			return "Removing Combustion Gen fuel: " + fluidName;
		}
	}

	public static class AddCoolantAction extends LateAction {
		public final Fluid fluid;
		public final float degreesCoolingPerMB;
		public final String fluidName;

		AddCoolantAction(ILiquidStack coolant, float degreesCoolingPerMB) {
			this.fluid = CraftTweakerMC.getLiquidStack(coolant).getFluid();
			this.degreesCoolingPerMB = degreesCoolingPerMB;
			this.fluidName = fluid.getName();
		}

		@Override
		public void execute() {
			FluidFuelRegister.instance.addCoolant(fluid, degreesCoolingPerMB);
		}

		@Override
		public String describe() {
			return "Adding Combustion Gen coolant: " + fluidName;
		}
	}

	public static class RemoveCoolantAction extends LateAction {
		public final String fluidName;
		public IFluidCoolant backupCoolant;

		RemoveCoolantAction(ILiquidStack coolant) {
			this.fluidName = CraftTweakerMC.getLiquidStack(coolant).getFluid().getName();
		}

		@Override
		public void execute() {
			Map<String, IFluidCoolant> coolants = CombustionGenRecipe.getCoolants();
			if (coolants.containsKey(fluidName)) {
				backupCoolant = coolants.get(fluidName);
				coolants.remove(fluidName);
			} else
				CraftTweakerAPI.logWarning("No Combustion Gen coolant found: " + fluidName);

		}

		@Override
		public String describe() {
			return "Removing Combustion Gen coolant: " + fluidName;
		}
	}
}
