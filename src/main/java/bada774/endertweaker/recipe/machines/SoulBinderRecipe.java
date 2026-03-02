package bada774.endertweaker.recipe.machines;

import java.util.List;

import javax.annotation.Nonnull;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crazypants.enderio.base.recipe.RecipeLevel;
import crazypants.enderio.base.recipe.soul.AbstractSoulBinderRecipe;
import crazypants.enderio.util.CapturedMob;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class SoulBinderRecipe extends AbstractSoulBinderRecipe {

	protected final IIngredient input;
	protected final ItemStack output;

	public SoulBinderRecipe(IIngredient input, ItemStack output, int energy, int xp, RecipeLevel level,
			ResourceLocation... entities) {

		super(energy, xp, generateId(input, output, entities), level, entities);
		this.input = input;
		this.output = output;
	}

	private static String generateId(IIngredient input, ItemStack output, ResourceLocation... entities) {
		String inputName = "unknown";
		if (input != null) {
			List<IItemStack> items = input.getItems();
			if (items != null && !items.isEmpty()) {
				inputName = items.get(0).getDefinition().getId().replace(':', '_');
			}
		}

		String outputName = "unknown";
		if (output != null && output.getItem().getRegistryName() != null) {
			outputName = output.getItem().getRegistryName().toString().replace(':', '_') + "_" + output.getItemDamage();
		}

		String entityName = "generic";
		if (entities != null && entities.length > 0 && entities[0] != null) {
			entityName = entities[0].toString().replace(':', '_');
		}

		return String.format("et_soulbinder_%s_to_%s_using_%s", inputName, outputName, entityName);
	}

	@Nonnull
	@Override
	public ItemStack getInputStack() {
		ItemStack stack = CraftTweakerMC.getItemStack(input.getItems().get(0));
		return stack == null ? ItemStack.EMPTY : stack;
	}

	@Nonnull
	@Override
	public ItemStack getOutputStack() {
		return output.copy();
	}

	@Nonnull
	@Override
	public ItemStack getOutputStack(@Nonnull ItemStack input, @Nonnull CapturedMob mobType) {
		return getOutputStack();
	}

	@Override
	protected boolean isValidInputItem(@Nonnull ItemStack item) {
		return input.matches(CraftTweakerMC.getIItemStack(item));
	}

}
