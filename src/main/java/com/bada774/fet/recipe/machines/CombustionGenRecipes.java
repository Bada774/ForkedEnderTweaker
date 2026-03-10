package com.bada774.fet.recipe.machines;

import java.util.Map;

import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import crazypants.enderio.base.fluid.FluidFuelRegister;
import crazypants.enderio.base.fluid.IFluidCoolant;
import crazypants.enderio.base.fluid.IFluidFuel;

public class CombustionGenRecipes {

    private static Map<String, IFluidFuel> fuels;
    private static Map<String, IFluidCoolant> coolants;

    static {
        try {
            fuels = ObfuscationReflectionHelper.getPrivateValue(FluidFuelRegister.class, FluidFuelRegister.instance,
                    "fuels");
            coolants = ObfuscationReflectionHelper.getPrivateValue(FluidFuelRegister.class, FluidFuelRegister.instance,
                    "coolants");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize CombustionGenRecipes reflection helpers:\n", e);
        }
    }

    public static Map<String, IFluidFuel> getFuels() {
        return fuels;
    }

    public static Map<String, IFluidCoolant> getCoolants() {
        return coolants;
    }
}
