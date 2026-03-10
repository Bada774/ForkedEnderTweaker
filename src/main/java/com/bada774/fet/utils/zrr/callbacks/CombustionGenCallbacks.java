package com.bada774.fet.utils.zrr.callbacks;

import com.bada774.fet.CombustionGen;
import com.bada774.fet.recipe.machines.CombustionGenRecipes;
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
            if (action.addedFuel != null &&
                    CombustionGenRecipes.getFuels().get(action.logName) == action.addedFuel) {
                CombustionGenRecipes.getFuels().remove(action.logName);
            }
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
                CombustionGenRecipes.getFuels().put(action.logName, action.backupFuel);
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
            if (action.addedCoolant != null
                    && CombustionGenRecipes.getCoolants().get(action.logName) == action.addedCoolant) {
                CombustionGenRecipes.getCoolants().remove(action.logName);
            }
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

        @Override
        public void undo() {
            if (action.backupCoolant != null) {
                CombustionGenRecipes.getCoolants().put(action.logName, action.backupCoolant);
            }
        }

        @Override
        public boolean hasUndoMethod() {
            return true;
        }
    }
}
