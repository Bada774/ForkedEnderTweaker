package bada774.endertweaker.utils.ZRR.callbacks;

import bada774.endertweaker.CombustionGen;
import bada774.endertweaker.recipe.machines.CombustionGenRecipe;

import youyihj.zenrecipereloading.module.PlainModule;
import youyihj.zenutils.api.reload.ActionReloadCallback;

public class CombustionGenCallbacks {

    public static void register(PlainModule module) {
        module.addCallbackFactory(CombustionGen.AddFuelAction.class, AddFuel::new);
        module.addCallbackFactory(CombustionGen.RemoveFuelAction.class, RemoveFuel::new);
        module.addCallbackFactory(CombustionGen.AddCoolantAction.class, AddCoolant::new);
        module.addCallbackFactory(CombustionGen.RemoveCoolantAction.class, RemoveCoolant::new);
    }

    public static class AddFuel extends ActionReloadCallback<CombustionGen.AddFuelAction> {
        public AddFuel(CombustionGen.AddFuelAction action) {
            super(action);
        }

        @Override
        public void undo() {
            CombustionGenRecipe.getFuels().remove(action.fluidName);
        }

        @Override
        public boolean hasUndoMethod() {
            return true;
        }
    }

    public static class RemoveFuel extends ActionReloadCallback<CombustionGen.RemoveFuelAction> {
        public RemoveFuel(CombustionGen.RemoveFuelAction action) {
            super(action);
        }

        @Override
        public void undo() {
            if (action.backupFuel != null) {
                CombustionGenRecipe.getFuels().put(action.fluidName, action.backupFuel);
            }
        }

        @Override
        public boolean hasUndoMethod() {
            return true;
        }
    }

    public static class AddCoolant extends ActionReloadCallback<CombustionGen.AddCoolantAction> {
        public AddCoolant(CombustionGen.AddCoolantAction action) {
            super(action);
        }

        @Override
        public void undo() {
            CombustionGenRecipe.getCoolants().remove(action.fluidName);
        }

        @Override
        public boolean hasUndoMethod() {
            return true;
        }
    }

    public static class RemoveCoolant extends ActionReloadCallback<CombustionGen.RemoveCoolantAction> {
        public RemoveCoolant(CombustionGen.RemoveCoolantAction action) {
            super(action);
        }

        public void undo() {
            if (action.backupCoolant != null) {
                CombustionGenRecipe.getCoolants().put(action.fluidName, action.backupCoolant);
            }
        }

        @Override
        public boolean hasUndoMethod() {
            return true;
        }
    }
}
