package bada774.endertweaker.utils.zrr.callbacks;

import bada774.endertweaker.Enchanter;
import crazypants.enderio.base.recipe.MachineRecipeRegistry;
import youyihj.zenrecipereloading.module.PlainModule;
import youyihj.zenutils.api.reload.ActionReloadCallback;

public class EnchanterCallbacks {
    public static void register(PlainModule module) {
        module.addCallbackFactory(Enchanter.AddRecipeAction.class, AddRecipe::new);
        module.addCallbackFactory(Enchanter.RemoveRecipeAction.class, RemoveRecipe::new);
    }

    public static class AddRecipe extends ActionReloadCallback<Enchanter.AddRecipeAction> {
        public AddRecipe(Enchanter.AddRecipeAction action) {
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

    public static class RemoveRecipe extends ActionReloadCallback<Enchanter.RemoveRecipeAction> {
        public RemoveRecipe(Enchanter.RemoveRecipeAction action) {
            super(action);
        }

        @Override
        public void undo() {
            if (action.backupRecipe != null) {
                MachineRecipeRegistry.instance.registerRecipe(action.backupRecipe);
            }
        }

        @Override
        public boolean hasUndoMethod() {
            return true;
        }
    }
}
