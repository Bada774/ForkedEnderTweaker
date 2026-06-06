package com.bada774.fet.utils.dumping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.bada774.fet.recipe.machines.AlloySmelterRecipes;
import com.bada774.fet.recipe.machines.CombustionGenRecipes;
import com.bada774.fet.utils.RecipeUtils;
import com.enderio.core.common.util.NNList;

import crazypants.enderio.base.fluid.IFluidCoolant;
import crazypants.enderio.base.fluid.IFluidFuel;
import crazypants.enderio.base.recipe.IRecipe;
import crazypants.enderio.base.recipe.IMachineRecipe;
import crazypants.enderio.base.recipe.IManyToOneRecipe;
import crazypants.enderio.base.recipe.IRecipeInput;
import crazypants.enderio.base.recipe.MachineRecipeInput;
import crazypants.enderio.base.recipe.MachineRecipeRegistry;
import crazypants.enderio.base.recipe.Recipe;
import crazypants.enderio.base.recipe.RecipeLevel;
import crazypants.enderio.base.recipe.RecipeOutput;
import crazypants.enderio.base.recipe.alloysmelter.AlloyRecipeManager;
import crazypants.enderio.base.recipe.enchanter.EnchanterRecipe;
import crazypants.enderio.base.recipe.sagmill.SagMillRecipeManager;
import crazypants.enderio.base.recipe.slicensplice.SliceAndSpliceRecipeManager;
import crazypants.enderio.base.recipe.soul.AbstractSoulBinderRecipe;
import crazypants.enderio.base.recipe.tank.TankMachineRecipe;
import crazypants.enderio.base.recipe.vat.VatRecipeManager;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MachineDumper {

    private static final Logger LOGGER = LogManager.getLogger();

    public static void dumpAll() {
        dumpAlloySmelter();
        dumpCombustion();
        dumpEnchanter();
        dumpSliceNSplice();
        dumpSagMill();
        dumpSoulBinder();
        dumpTank();
        dumpVat();
    }

    public static void dumpAlloySmelter() {
        try {
            AlloyRecipeManager manager = AlloyRecipeManager.getInstance();
            List<IManyToOneRecipe> recipes = AlloySmelterRecipes.getAlloyRecipes();
            header("AlloySmelter", recipes.size());
            for (IManyToOneRecipe recipe : recipes) {
                if (recipe == null) continue;

                NNList<MachineRecipeInput> testInputs = new NNList<>();
                for (IRecipeInput input : recipe.getInputs())
                    testInputs.add(new MachineRecipeInput(0, input.getInput()));
                boolean inLookup = manager.getRecipeForInputs(RecipeLevel.IGNORE, testInputs) != null;

                String inputs = Arrays.stream(recipe.getInputs())
                    .filter(i -> i != null)
                    .map(MachineDumper::inputLine)
                    .collect(Collectors.joining(", "));

                LOGGER.info("[FET_DUMP_ALLOY] synthetic={} dedupe={} | {} -> {} | energy={}",
                        recipe.isSynthetic(), recipe.isDedupeInput(), inputs,
                        stackInfo(recipe.getOutput()), recipe.getEnergyRequired());
            }
        } catch (Exception e) {
            LOGGER.error("[FET_DUMP_ALLOY] Failed to dump AlloySmelter recipes", e);
        }
    }

    public static void dumpCombustion() {
        try {
            Map<String, IFluidFuel> fuels = CombustionGenRecipes.getFuels();
            Map<String, IFluidCoolant> coolants = CombustionGenRecipes.getCoolants();

            List<String> validFuels = new ArrayList<>();
            fuels.forEach((name, fuel) -> {
                if (fuel != null) {
                    validFuels.add(String.format("[FET_DUMP_COMBUSTION] FUEL | %s | impl=%s | rf/cycle=%d | totalBurn=%d",
                            name,
                            fuel.getClass().getSimpleName(),
                            fuel.getPowerPerCycle(),
                            fuel.getTotalBurningTime()));
                }
            });

            List<String> validCoolants = new ArrayList<>();
            coolants.forEach((name, coolant) -> {
                if (coolant != null) {
                    validCoolants.add(String.format("[FET_DUMP_COMBUSTION] COOLANT | %s | impl=%s | cooling=%f/mb",
                            name,
                            coolant.getClass().getSimpleName(),
                            coolant.getDegreesCoolingPerMB()));
                }
            });

            LOGGER.info("[FET_DUMP_COMBUSTION] === CombustionGen ({} fuels, {} coolants) ===",
                    validFuels.size(), validCoolants.size());

            validFuels.forEach(LOGGER::info);
            validCoolants.forEach(LOGGER::info);
        } catch (Exception e) {
            LOGGER.error("[FET_DUMP_COMBUSTION] Failed to dump CombustionGen recipes", e);
        }
    }

    public static void dumpEnchanter() {
        try {
            Map<String, ? extends IMachineRecipe> recipes = MachineRecipeRegistry.instance
                    .getRecipesForMachine(MachineRecipeRegistry.ENCHANTER);
            header("Enchanter", recipes.size());
            for (IMachineRecipe recipe : recipes.values()) {
                if (!(recipe instanceof EnchanterRecipe)) continue;
                EnchanterRecipe enchanterRecipe = (EnchanterRecipe) recipe;

                String enchantment = enchanterRecipe.getEnchantment().getName();

                ItemStack catalyst = RecipeUtils.thingsToStack(enchanterRecipe.getInput());
                LOGGER.info("[FET_DUMP_ENCHANTER] class={} | enchantment={} | catalyst={} | itemsPerLevel={}",
                        recipe.getClass().getSimpleName(), enchantment, stackInfo(catalyst), ((EnchanterRecipe) recipe).getItemsPerLevel());
            }
        } catch (Exception e) {
            LOGGER.error("[FET_DUMP_ENCHANTER] Failed to dump Enchanter recipes", e);
        }
    }

    public static void dumpSagMill() {
        try {
            List<Recipe> recipes = SagMillRecipeManager.getInstance().getRecipes();
            header("SagMill", recipes.size());
            for (Recipe r : recipes) {
                if (r == null) continue;
                IRecipeInput input = r.getInputs()[0];
                String line = inputLine(input)
                        + " -> " + recipeOutputLines(r.getOutputs())
                        + " | energy=" + r.getEnergyRequired()
                        + " | bonus=" + r.getBonusType().name();
                LOGGER.info("[FET_DUMP_SAGMILL] {}", line);
            }
        } catch (Exception e) {
            LOGGER.error("[FET_DUMP_SAGMILL] Failed to dump SagMill recipes", e);
        }
    }

    public static void dumpSliceNSplice() {
        try {
            List<IManyToOneRecipe> recipes = SliceAndSpliceRecipeManager.getInstance().getRecipes();
            header("SliceNSplice", recipes.size());
            for (IManyToOneRecipe recipe : recipes) {
                if (recipe == null) continue;
                String inputs = Arrays.stream(recipe.getInputs())
                        .filter(i -> i != null)
                        .map(MachineDumper::inputLine)
                        .collect(Collectors.joining(", "));
                LOGGER.info("[FET_DUMP_SLICE] {} -> {} | energy={}",
                        inputs, stackInfo(recipe.getOutput()), recipe.getEnergyRequired());
            }
        } catch (Exception e) {
            LOGGER.error("[FET_DUMP_SLICE] Failed to dump SliceNSplice recipes", e);
        }
    }

    public static void dumpSoulBinder() {
        try {
            Map<String, ? extends IMachineRecipe> recipes = MachineRecipeRegistry.instance
                .getRecipesForMachine(MachineRecipeRegistry.SOULBINDER);
            header("SoulBinder", recipes.size());
            for (IMachineRecipe recipe : recipes.values()) {
                if (!(recipe instanceof AbstractSoulBinderRecipe)) continue;
                AbstractSoulBinderRecipe soulBinderRecipe = (AbstractSoulBinderRecipe) recipe;
                LOGGER.info("[FET_DUMP_SOULBINDER] class={} | {} -> {}",
                        recipe.getClass().getSimpleName(),
                        stackInfo(soulBinderRecipe.getInputStack()),
                        stackInfo(soulBinderRecipe.getOutputStack()));
            }
        } catch (Exception e) {
            LOGGER.error("[FET_DUMP_SOULBINDER] Failed to dump SoulBinder recipes", e);
        }
    }

    public static void dumpTank() {
        try {
            dumpTankRegistry(MachineRecipeRegistry.TANK_FILLING,  "FILL");
            dumpTankRegistry(MachineRecipeRegistry.TANK_EMPTYING, "EMPTY");
        } catch (Exception e) {
            LOGGER.error("[FET_DUMP_TANK] Failed to dump Tank recipes", e);
        }
    }

    public static void dumpVat() {
        try {
            List<IRecipe> recipes = VatRecipeManager.getInstance().getRecipes();
            header("Vat", recipes.size());
            for (IRecipe recipe : recipes) {
                if (recipe == null) continue;


                String itemInputs = Arrays.stream(recipe.getInputs())
                    .filter(i -> i != null && !i.isFluid())
                    .map(i -> inputLine(i)
                        + "(slot=" + i.getSlotNumber()
                        + ", x" + i.getMulitplier() + ")")
                    .collect(Collectors.joining(", "));

                String fluidInput = Arrays.stream(recipe.getInputs())
                    .filter(i -> i != null && i.isFluid() && i.getFluidInput() != null)
                    .map(i -> fluidInfo(i.getFluidInput()))
                    .findFirst().orElse("none");

                String output = "none";
                RecipeOutput[] outputs = recipe.getOutputs();
                if (outputs.length > 0 && outputs[0] != null && outputs[0].getFluidOutput() != null)
                    output = fluidInfo(outputs[0].getFluidOutput());

                LOGGER.info("[FET_DUMP_VAT] items=[{}] + {} -> {} | energy={}",
                    itemInputs, fluidInput, output, recipe.getEnergyRequired());
            }
        } catch (Exception e) {
            LOGGER.error("[FET_DUMP_VAT] Failed to dump Vat recipes", e);
        }
    }

    private static void dumpTankRegistry(String registryId, String tag) {
        Map<String, ? extends IMachineRecipe> recipes = MachineRecipeRegistry.instance
            .getRecipesForMachine(registryId);
        LOGGER.info("[FET_DUMP_TANK_{}] === Tank {} ({} recipes) ===", tag, tag, recipes.size());
        for (IMachineRecipe recipe : recipes.values()) {
            if (!(recipe instanceof TankMachineRecipe)) continue;
            TankMachineRecipe tankRecipe = (TankMachineRecipe) recipe;
            FluidStack fluid = tankRecipe.getFluid();
            LOGGER.info("[FET_DUMP_TANK_{}] class={} | {} + {} -> {}",
                    tag,
                    recipe.getClass().getSimpleName(),
                    stackInfo(RecipeUtils.thingsToStack(tankRecipe.getInput())),
                    fluidInfo(fluid),
                    stackInfo(RecipeUtils.thingsToStack(tankRecipe.getOutput())));
        }
    }

    private static String inputLine(IRecipeInput input) {
        return input.getClass().getSimpleName() + "(" + stackInfo(input.getInput()) + ")";
    }

    private static String stackInfo(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "EMPTY";
        String id = stack.getItem().getRegistryName() != null
            ? stack.getItem().getRegistryName().toString()
            : "unknown";
        return stack.getDisplayName() + " (" + id + ":" + stack.getMetadata() + ")";
    }

    private static String fluidInfo(FluidStack fluid) {
        if (fluid == null) return "null";
        return fluid.getLocalizedName() + "*" + fluid.amount;
    }

    private static String recipeOutputLines(RecipeOutput[] outputs) {
        if (outputs == null) return "none";
        return Arrays.stream(outputs)
            .filter(output -> output != null && !output.getOutput().isEmpty())
            .map(output -> {
                int chance = Math.round(output.getChance() * 100);
                return stackInfo(output.getOutput()) + (chance < 100 ? " (" + chance + "%)" : "");
            })
            .collect(Collectors.joining(", "));
    }

    private static void header(String tag, int count) {
        LOGGER.info("[FET_DUMP_{}] === {} ({} recipes) ===", tag, tag, count);
    }
}
