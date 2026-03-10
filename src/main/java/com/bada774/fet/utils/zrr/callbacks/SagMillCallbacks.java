package com.bada774.fet.utils.zrr.callbacks;

import com.bada774.fet.SagMill;
import crazypants.enderio.base.recipe.sagmill.SagMillRecipeManager;
import youyihj.zenrecipereloading.module.PlainModule;
import youyihj.zenutils.api.reload.ActionReloadCallback;

public class SagMillCallbacks {
    public static void register(PlainModule module) {
        module.addCallbackFactory(SagMill.AddRecipeAction.class, AddRecipe::new);
        module.addCallbackFactory(SagMill.RemoveRecipeAction.class, RemoveRecipe::new);
    }

    public static class AddRecipe extends ActionReloadCallback<SagMill.AddRecipeAction> {
        public AddRecipe(SagMill.AddRecipeAction action) {
            super(action);
        }

        @Override
        public void undo() {
            if (action.createdRecipe != null) {
                SagMillRecipeManager.getInstance().getRecipes().remove(action.createdRecipe);
            }
        }

        @Override
        public boolean hasUndoMethod() {
            return true;
        }
    }

    public static class RemoveRecipe extends ActionReloadCallback<SagMill.RemoveRecipeAction> {
        public RemoveRecipe(SagMill.RemoveRecipeAction action) {
            super(action);
        }

        @Override
        public void undo() {
            if (action.backupRecipe != null) {
                SagMillRecipeManager.getInstance().addRecipe(action.backupRecipe);
            }
        }

        @Override
        public boolean hasUndoMethod() {
            return true;
        }
    }
}
