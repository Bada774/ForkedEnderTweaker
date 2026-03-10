package com.bada774.fet.utils.zrr.callbacks;

import java.util.ArrayList;
import java.util.List;

import com.enderio.core.common.util.NNList;

import com.bada774.fet.AlloySmelter;
import com.bada774.fet.recipe.machines.AlloySmelterRecipes;
import com.bada774.fet.utils.RecipeUtils;
import crazypants.enderio.base.recipe.IManyToOneRecipe;
import crazypants.enderio.base.recipe.IRecipeInput;
import crazypants.enderio.base.recipe.lookup.TriItemLookup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import youyihj.zenrecipereloading.module.PlainModule;
import youyihj.zenutils.api.reload.ActionReloadCallback;

public class AlloySmelterCallbacks {

    public static void register(PlainModule module) {
        module.addCallbackFactory(AlloySmelter.AddRecipeAction.class, AddRecipe::new);
        module.addCallbackFactory(AlloySmelter.RemoveByOutputAction.class, RemoveByOutput::new);
        module.addCallbackFactory(AlloySmelter.RemoveByInputsAction.class, RemoveByInputs::new);
    }

    public static class AddRecipe extends ActionReloadCallback<AlloySmelter.AddRecipeAction> {
        public AddRecipe(AlloySmelter.AddRecipeAction action) {
            super(action);
        }

        @Override
        public void undo() {
            if (action.recipeCreated) {
                NNList<IRecipeInput> targetInputs = action.inputs;
                ItemStack targetOutput = action.output;

                List<IManyToOneRecipe> currentAlloy = AlloySmelterRecipes.getAlloyRecipes();
                TriItemLookup<IManyToOneRecipe> newLookup = AlloySmelterRecipes.createLookup();
                List<IManyToOneRecipe> valid = new ArrayList<>();
                boolean removed = false;

                for (IManyToOneRecipe r : currentAlloy) {
                    if (OreDictionary.itemMatches(targetOutput, r.getOutput(), true)
                            && RecipeUtils.areInputsMatch(RecipeUtils.toEIOInputsNN(r.getInputs()), targetInputs)) {
                        removed = true;
                    } else {
                        AlloySmelterRecipes.addRecipeToLookup(newLookup, r);
                        valid.add(r);
                    }
                }
                if (removed) {
                    AlloySmelterRecipes.commitChanges(newLookup, valid);
                }
            }
        }

        @Override
        public boolean hasUndoMethod() {
            return true;
        }
    }

    public static class RemoveByOutput extends ActionReloadCallback<AlloySmelter.RemoveByOutputAction> {
        public RemoveByOutput(AlloySmelter.RemoveByOutputAction action) {
            super(action);
        }

        @Override
        public void undo() {
            restoreFromBackup(action.backupRecipes);
        }

        @Override
        public boolean hasUndoMethod() {
            return true;
        }
    }

    public static class RemoveByInputs extends ActionReloadCallback<AlloySmelter.RemoveByInputsAction> {
        public RemoveByInputs(AlloySmelter.RemoveByInputsAction action) {
            super(action);
        }

        public void undo() {
            restoreFromBackup(action.backupRecipes);
        }

        @Override
        public boolean hasUndoMethod() {
            return true;
        }
    }

    private static void restoreFromBackup(List<IManyToOneRecipe> backup) {
        if (backup == null || backup.isEmpty())
            return;

        List<IManyToOneRecipe> current = AlloySmelterRecipes.getAlloyRecipes();
        TriItemLookup<IManyToOneRecipe> lookup = AlloySmelterRecipes.createLookup();
        List<IManyToOneRecipe> valid = new ArrayList<>(current);

        for (IManyToOneRecipe r : current)
            AlloySmelterRecipes.addRecipeToLookup(lookup, r);

        for (IManyToOneRecipe r : backup) {
            AlloySmelterRecipes.addRecipeToLookup(lookup, r);
            valid.add(r);
        }
        AlloySmelterRecipes.commitChanges(lookup, valid);
    }
}
