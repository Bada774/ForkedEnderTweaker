package com.bada774.fet.utils;

import com.bada774.fet.EnderTweaker;
import crafttweaker.IAction;

public abstract class LateAction implements IAction {
    @Override
    public void apply() {
        if (EnderTweaker.LOAD_COMPLETE) {
            try {
                execute();
            } catch (Exception e) {
                Logging.logError("Unhandled exception during action execution:\n" + e);
                e.printStackTrace();
            }
        } else {
            EnderTweaker.LATE_QUEUE.add(() -> {
                try {
                    execute();
                } catch (Exception e) {
                    Logging.logError("Unhandled exception during action execution:\n" + e);
                    e.printStackTrace();
                }
            });
        }
    }

    public abstract void execute();
}
