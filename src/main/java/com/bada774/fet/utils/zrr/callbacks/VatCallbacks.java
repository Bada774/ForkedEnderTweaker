package com.bada774.fet.utils.zrr.callbacks;

import com.bada774.fet.Vat;
import com.bada774.fet.utils.VatRecipeUtils;
import crazypants.enderio.base.recipe.IRecipe;
import crazypants.enderio.base.recipe.vat.VatRecipeManager;
import youyihj.zenrecipereloading.module.PlainModule;
import youyihj.zenutils.api.reload.ActionReloadCallback;

public class VatCallbacks {

    public static void register(PlainModule module) {
        module.addCallbackFactory(Vat.AddRecipeAction.class, AddRecipe::new);
        module.addCallbackFactory(Vat.RemoveRecipeAction.class, RemoveRecipe::new);
    }

    public static class AddRecipe extends ActionReloadCallback<Vat.AddRecipeAction> {
        public AddRecipe(Vat.AddRecipeAction action) {
            super(action);
        }

        @Override
        public void undo() {
            if (action.createdRecipe != null) {
                VatRecipeUtils.removeRecipeSafely(action.createdRecipe);
            }
        }

        @Override
        public boolean hasUndoMethod() {
            return true;
        }
    }

    public static class RemoveRecipe extends ActionReloadCallback<Vat.RemoveRecipeAction> {
        public RemoveRecipe(Vat.RemoveRecipeAction action) {
            super(action);
        }

        @Override
        public void undo() {
            if (action.backupRecipes != null && !action.backupRecipes.isEmpty()) {
                for (IRecipe recipe : action.backupRecipes) {
                    VatRecipeManager.getInstance().addRecipe(recipe);
                }
            }
        }

        @Override
        public boolean hasUndoMethod() {
            return true;
        }
    }
}