package com.bada774.fet.utils;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;

public class ValidationUtils {

    public static boolean isInvalid(IItemStack stack) {
        return stack == null || stack.isEmpty();
    }

    public static boolean isInvalid(IIngredient ingredient) {
        return ingredient == null || ingredient.getItems() == null || ingredient.getItems().isEmpty();
    }

    public static boolean isInvalid(Object[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isInvalid(IItemStack[] array) {
        if (array == null || array.length == 0)
            return true;
        for (IItemStack item : array) {
            if (isInvalid(item))
                return true;
        }
        return false;
    }

    public static boolean isAllNullOrEmpty(IItemStack[] array) {
        if (array == null)
            return true;
        for (IItemStack item : array) {
            if (!isInvalid(item)) return false;
        }
        return true;
    }

    public static boolean isInvalid(float[] array) {
        return array == null || array.length == 0;
    }
}