package bada774.endertweaker.utils.zrr.callbacks;

import bada774.endertweaker.SliceNSplice;

import crazypants.enderio.base.recipe.IManyToOneRecipe;
import crazypants.enderio.base.recipe.slicensplice.SliceAndSpliceRecipeManager;
import youyihj.zenrecipereloading.module.PlainModule;
import youyihj.zenutils.api.reload.ActionReloadCallback;

public class SliceNSpliceCallbacks {

    public static void register(PlainModule module) {
        module.addCallbackFactory(SliceNSplice.AddRecipeAction.class, AddRecipe::new);
        module.addCallbackFactory(SliceNSplice.RemoveRecipeAction.class, RemoveRecipe::new);
    }

    public static class AddRecipe extends ActionReloadCallback<SliceNSplice.AddRecipeAction> {
        public AddRecipe(SliceNSplice.AddRecipeAction action) {
            super(action);
        }

        @Override
        public void undo() {
            if (action.createdRecipe != null) {
                SliceAndSpliceRecipeManager.getInstance().getRecipes().remove(action.createdRecipe);
            }
        }

        @Override
        public boolean hasUndoMethod() {
            return true;
        }
    }

    public static class RemoveRecipe extends ActionReloadCallback<SliceNSplice.RemoveRecipeAction> {
        public RemoveRecipe(SliceNSplice.RemoveRecipeAction action) {
            super(action);
        }

        @Override
        public void undo() {
            if (action.backupRecipes != null && !action.backupRecipes.isEmpty()) {
                for (IManyToOneRecipe recipe : action.backupRecipes) {
                    SliceAndSpliceRecipeManager.getInstance().addRecipe(recipe);
                }
            }
        }

        @Override
        public boolean hasUndoMethod() {
            return true;
        }
    }
}
