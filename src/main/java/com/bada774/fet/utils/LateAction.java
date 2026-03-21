package com.bada774.fet.utils;

import com.bada774.fet.ForkedEnderTweaker;
import crafttweaker.IAction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class LateAction implements IAction {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void apply() {
        if (ForkedEnderTweaker.LOAD_COMPLETE) {
            try {
                execute();
            } catch (Exception e) {
                Logging.logError("FET: Unhandled exception during action execution. Check logs for further info.");
                LOGGER.error("Unhandled exception during action execution:", e);
            }
        } else {
            ForkedEnderTweaker.LATE_QUEUE.add(() -> {
                try {
                    execute();
                } catch (Exception e) {
                    Logging.logError("FET: Unhandled exception during action execution. Check logs for further info.");
                    LOGGER.error("Unhandled exception during action execution:", e);
                }
            });
        }
    }

    public abstract void execute();
}
