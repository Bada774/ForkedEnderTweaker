package bada774.endertweaker.utils.ZRR.callbacks;

import bada774.endertweaker.Tank;
import crazypants.enderio.base.recipe.IMachineRecipe;
import crazypants.enderio.base.recipe.MachineRecipeRegistry;
import youyihj.zenrecipereloading.module.PlainModule;
import youyihj.zenutils.api.reload.ActionReloadCallback;

public class TankCallbacks {

    public static void register(PlainModule module) {
        module.addCallbackFactory(Tank.AddRecipeAction.class, AddRecipe::new);
        module.addCallbackFactory(Tank.RemoveRecipeAction.class, RemoveRecipe::new);
    }

    public static class AddRecipe extends ActionReloadCallback<Tank.AddRecipeAction> {
        public AddRecipe(Tank.AddRecipeAction action) {
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

    public static class RemoveRecipe extends ActionReloadCallback<Tank.RemoveRecipeAction> {
        public RemoveRecipe(Tank.RemoveRecipeAction action) {
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
