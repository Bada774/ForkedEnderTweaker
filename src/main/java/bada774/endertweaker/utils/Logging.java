package bada774.endertweaker.utils;

import java.util.List;

import crafttweaker.CraftTweakerAPI;

public class Logging {

    public static void logRemovalResult(String machineName, int count, String type,
            List<String> successLog, List<String> missingLog) {
        if (!successLog.isEmpty()) {
            String joinedSuccess = String.join(", ", successLog);
            CraftTweakerAPI
                    .logInfo("Removed " + count + " " + machineName + " recipes (" + type + ") for: " + joinedSuccess);
        }

        if (!missingLog.isEmpty()) {
            String joinedMissing = String.join(", ", missingLog);
            CraftTweakerAPI.logWarning("No " + machineName + " recipes (" + type + ") found for: " + joinedMissing);
        }
    }
}
