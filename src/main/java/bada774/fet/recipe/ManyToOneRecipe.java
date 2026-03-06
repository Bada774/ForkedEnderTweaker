package bada774.fet.recipe;

import java.util.List;

import com.enderio.core.common.util.NNList;

import crazypants.enderio.base.recipe.BasicManyToOneRecipe;
import crazypants.enderio.base.recipe.IRecipeInput;
import crazypants.enderio.base.recipe.MachineRecipeInput;
import crazypants.enderio.base.recipe.Recipe;
import crazypants.enderio.base.recipe.RecipeBonusType;
import crazypants.enderio.base.recipe.RecipeLevel;
import crazypants.enderio.base.recipe.RecipeOutput;

public class ManyToOneRecipe extends BasicManyToOneRecipe {

	public ManyToOneRecipe(RecipeOutput output, int energyRequired, RecipeBonusType bonusType, RecipeLevel level,
			List<IRecipeInput> inputs) {
		super(new Recipe(output, energyRequired, bonusType, level, inputs.toArray(new IRecipeInput[0])));
	}

	@Override
	public boolean isInputForRecipe(NNList<MachineRecipeInput> machineInputs) {
		if (machineInputs == null || machineInputs.isEmpty())
			return false;

		for (IRecipeInput required : getInputs()) {
			if (required == null || !required.isValid())
				continue;

			boolean found = false;
			for (MachineRecipeInput inst : machineInputs) {
				if (inst != null && inst.slotNumber == required.getSlotNumber()) {
					if (required.isInput(inst.item)) {
						found = true;
						break;
					}
				}
			}
			if (!found)
				return false;
		}
		return true;
	}

}
