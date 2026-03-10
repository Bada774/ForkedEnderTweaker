package com.bada774.fet.utils;

import java.util.Collections;
import java.util.List;

import crafttweaker.CraftTweakerAPI;
import net.minecraft.util.text.TextFormatting;

public class Logging {

    public static void logAddition(String machineName, String methodType, String itemType, String recipeName) {
        logInfo(String.format("[%s | %s] Added %s for: %s",
                machineName, methodType, itemType, recipeName));
    }

    public static void logRemoval(String machineName, String methodType, String itemType,
            String successString, String missingString) {
        List<String> successList = singletonListOrEmpty(successString);
        List<String> missingList = singletonListOrEmpty(missingString);

        logRemoval(machineName, methodType, itemType, successList, missingList);
    }

    public static void logRemoval(String machineName, String methodType, String itemType,
            List<String> successList, List<String> missingList) {

        if (!successList.isEmpty()) {
            String joined = String.join(", ", successList);

            String msg;
            if (successList.size() > 1) {
                msg = String.format("[%s | %s] Removed %d %ss for: %s",
                        machineName, methodType, successList.size(), itemType, joined);
            } else {
                msg = String.format("[%s | %s] Removed %s for: %s",
                        machineName, methodType, itemType, joined);
            }

            logInfo(msg);
        }

        if (!missingList.isEmpty()) {
            String joined = String.join(", ", missingList);

            String msg;
            if (missingList.size() > 1) {
                msg = String.format("[%s | %s] %d %ss not found for: %s",
                        machineName, methodType, missingList.size(), itemType, joined);
            } else {
                msg = String.format("[%s | %s] No %s found for: %s",
                        machineName, methodType, itemType, joined);
            }

            logWarning(msg);
        }
    }

    public static void logWarning(String message) {
        CraftTweakerAPI.logWarning(fixFormatting(message, TextFormatting.YELLOW));
    }

    public static void logError(String message) {
        CraftTweakerAPI.logError(fixFormatting(message, TextFormatting.RED));
    }

    public static void logInfo(String message) {
        CraftTweakerAPI.logInfo(message);
    }

    public static void logValidationError(String machineName, String methodName, String errorDetails) {
        logError(String.format("[%s | %s] %s", machineName, methodName, errorDetails));
    }

    public static String fixFormatting(String message, TextFormatting color) {
        if (message == null)
            return "";
        return message.replace("\n", "\n" + color.toString());
    }

    private static List<String> singletonListOrEmpty(String str) {
        return (str != null && !str.isEmpty())
                ? Collections.singletonList(str)
                : Collections.emptyList();
    }
}
