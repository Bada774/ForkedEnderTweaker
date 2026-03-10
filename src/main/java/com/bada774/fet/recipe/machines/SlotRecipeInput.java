package com.bada774.fet.recipe.machines;

import crazypants.enderio.base.recipe.IRecipeInput;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class SlotRecipeInput implements IRecipeInput {
    private final IRecipeInput base;
    private final int slot;

    public SlotRecipeInput(IRecipeInput base, int slot) {
        this.base = base;
        this.slot = slot;
    }

    @Override
    public int getSlotNumber() {
        return slot;
    }

    @Override
    public IRecipeInput copy() {
        return new SlotRecipeInput(base.copy(), slot);
    }

    @Override
    public boolean isValid() {
        return base.isValid();
    }

    @Override
    public ItemStack getInput() {
        return base.getInput();
    }

    @Override
    public FluidStack getFluidInput() {
        return base.getFluidInput();
    }

    @Override
    public boolean isInput(ItemStack test) {
        return base.isInput(test);
    }

    @Override
    public boolean isInput(FluidStack test) {
        return base.isInput(test);
    }

    @Override
    public float getMulitplier() {
        return base.getMulitplier();
    }

    @Override
    public int getStackSize() {
        return base.getStackSize();
    }

    @Override
    public boolean isFluid() {
        return base.isFluid();
    }

    @Override
    public void shrinkStack(int count) {
        base.shrinkStack(count);
    }

    @Override
    public ItemStack[] getEquivelentInputs() {
        return base.getEquivelentInputs();
    }
}