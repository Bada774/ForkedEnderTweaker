package bada774.endertweaker.utils;

import bada774.endertweaker.EnderTweaker;

import crafttweaker.IAction;

public abstract class LateAction implements IAction {
    @Override
    public void apply() {
        if (EnderTweaker.LOAD_COMPLETE) {
            try {
                execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            EnderTweaker.LATE_QUEUE.add(() -> {
                try {
                    execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public abstract void execute();
}
