package shadows.endertweaker.bada774.recipe;

import crazypants.enderio.base.recipe.alloysmelter.AlloyRecipeManager;
import crazypants.enderio.base.recipe.IManyToOneRecipe;
import crazypants.enderio.base.recipe.lookup.TriItemLookup;
import crafttweaker.CraftTweakerAPI;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class AlloySmelterRecipe {

    private static final Field lookupField;
    private static final Method addRecipeMethod;

    static {
        try {
            lookupField = AlloyRecipeManager.class.getDeclaredField("lookup");
            lookupField.setAccessible(true);

            addRecipeMethod = AlloyRecipeManager.class.getDeclaredMethod("addRecipeToLookup",
                    TriItemLookup.class, IManyToOneRecipe.class);
            addRecipeMethod.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize AlloySmelter reflection helpers: ", e);
        }

    }

    public static List<IManyToOneRecipe> getAlloyRecipes() {
        List<IManyToOneRecipe> alloySmelterRecipes = AlloyRecipeManager.getInstance().getRecipes();
        if (!alloySmelterRecipes.isEmpty())
            return alloySmelterRecipes;
        CraftTweakerAPI.logError("AlloyRecipeManager returned null recipe list!");
        return Collections.emptyList();
    }

    public static TriItemLookup<IManyToOneRecipe> createLookup() {
        return new TriItemLookup<>();
    }

    public static void addRecipeToLookup(TriItemLookup<IManyToOneRecipe> lookup, IManyToOneRecipe recipe) {
        try {
            addRecipeMethod.invoke(null, lookup, recipe);
        } catch (Exception e) {
            CraftTweakerAPI.logError("Error adding recipe to new lookup via reflection");
            e.printStackTrace();
        }
    }

    public static void setNewLookup(TriItemLookup<IManyToOneRecipe> newLookup) {
        try {
            lookupField.set(AlloyRecipeManager.getInstance(), newLookup);

        } catch (Exception e) {
            CraftTweakerAPI.logError("Failed to set Alloy Smelter recipe lookup: ", e);
            e.printStackTrace();
        }
    }
}
