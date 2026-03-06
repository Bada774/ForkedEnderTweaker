package bada774.fet.utils.zrr.callbacks;

import bada774.fet.SoulBinder;
import crazypants.enderio.base.recipe.IMachineRecipe;
import crazypants.enderio.base.recipe.MachineRecipeRegistry;
import youyihj.zenrecipereloading.module.PlainModule;
import youyihj.zenutils.api.reload.ActionReloadCallback;

public class SoulBinderCallbacks {
    public static void register(PlainModule module) {
        module.addCallbackFactory(SoulBinder.AddRecipeAction.class, AddRecipe::new);
        module.addCallbackFactory(SoulBinder.RemoveRecipeAction.class, RemoveRecipe::new);
    }

    public static class AddRecipe extends ActionReloadCallback<SoulBinder.AddRecipeAction> {
        public AddRecipe(SoulBinder.AddRecipeAction action) {
            super(action);
        }

        @Override
        public void undo() {
            if (action.createdRecipe != null) {
                MachineRecipeRegistry.instance.removeRecipe(action.createdRecipe);
            }
        }

        @Override
        public boolean hasUndoMethod() {
            return true;
        }
    }

    public static class RemoveRecipe extends ActionReloadCallback<SoulBinder.RemoveRecipeAction> {
        public RemoveRecipe(SoulBinder.RemoveRecipeAction action) {
            super(action);
        }

        @Override
        public void undo() {
            if (action.backupRecipes != null && !action.backupRecipes.isEmpty()) {
                for (IMachineRecipe recipe : action.backupRecipes) {
                    MachineRecipeRegistry.instance.registerRecipe(recipe);
                }
            }
        }

        @Override
        public boolean hasUndoMethod() {
            return true;
        }
    }
}
