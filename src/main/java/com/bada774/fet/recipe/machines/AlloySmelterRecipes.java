package com.bada774.fet.recipe.machines;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bada774.fet.utils.Logging;
import crazypants.enderio.base.recipe.alloysmelter.AlloyRecipeManager;
import crazypants.enderio.base.recipe.IManyToOneRecipe;
import crazypants.enderio.base.recipe.lookup.TriItemLookup;

public class AlloySmelterRecipes {

    private static final Field lookupField;
    private static final Method addRecipeMethod;

    private static final Logger LOGGER = LogManager.getLogger();

    static {
        try {
            lookupField = AlloyRecipeManager.class.getDeclaredField("lookup");
            lookupField.setAccessible(true);

            addRecipeMethod = AlloyRecipeManager.class.getDeclaredMethod("addRecipeToLookup",
                    TriItemLookup.class, IManyToOneRecipe.class);
            addRecipeMethod.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize AlloySmelter reflection helpers:\n", e);
        }

    }

    public static List<IManyToOneRecipe> getAlloyRecipes() {
        List<IManyToOneRecipe> alloySmelterRecipes = AlloyRecipeManager.getInstance().getRecipes();
        if (alloySmelterRecipes == null) {
            Logging.logError("AlloyRecipeManager returned a null recipe list!");
            return Collections.emptyList();
        }
        return alloySmelterRecipes;
    }

    public static TriItemLookup<IManyToOneRecipe> createLookup() {
        return new TriItemLookup<>();
    }

    public static void addRecipeToLookup(TriItemLookup<IManyToOneRecipe> lookup, IManyToOneRecipe recipe) {
        try {
            addRecipeMethod.invoke(null, lookup, recipe);
        } catch (Exception e) {
            Logging.logError("Error adding recipe to new lookup via reflection:\n" + e);
            LOGGER.error("Error adding recipe to new lookup via reflection: ", e);
        }
    }

    public static void commitChanges(TriItemLookup<IManyToOneRecipe> newLookup, List<IManyToOneRecipe> validRecipes) {
        AlloyRecipeManager manager = AlloyRecipeManager.getInstance();
        try {
            lookupField.set(manager, newLookup);

            List<IManyToOneRecipe> internalList = manager.getRecipes();
            if (internalList != null) {
                internalList.clear();
                internalList.addAll(validRecipes);
            }

        } catch (Exception e) {
            Logging.logError("Failed to commit Alloy Smelter changes:\n" + e);
            LOGGER.error("Failed to commit Alloy Smelter changes: ", e);
        }
    }
}
