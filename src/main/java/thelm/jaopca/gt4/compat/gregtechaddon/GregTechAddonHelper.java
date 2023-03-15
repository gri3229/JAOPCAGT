package thelm.jaopca.gt4.compat.gregtechaddon;

import java.util.function.Supplier;

import gregtechmod.api.recipe.Ingredient;
import gregtechmod.api.recipe.RecipeFactory;
import gregtechmod.api.recipe.RecipeMap;
import gregtechmod.common.recipe.RecipeEntry;
import gregtechmod.integration.crafttweaker.recipe.CTRecipeMaps;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thelm.jaopca.api.items.IItemProvider;
import thelm.jaopca.gt4.compat.gregtechaddon.recipes.GregTechAddonRecipeAction;
import thelm.jaopca.gt4.compat.gregtechaddon.recipes.GregTechAddonRecipeSettings;
import thelm.jaopca.utils.ApiImpl;

public class GregTechAddonHelper {

	public static final GregTechAddonHelper INSTANCE = new GregTechAddonHelper();

	private GregTechAddonHelper() {}

	public Ingredient getIngredient(Object obj, int amount) {
		if(obj instanceof Supplier<?>) {
			return getIngredient(((Supplier<?>)obj).get(), amount);
		}
		if(obj instanceof String) {
			if(ApiImpl.INSTANCE.getOredict().contains(obj)) {
				return RecipeEntry.oreDict((String)obj, amount);
			}
		}
		if(obj instanceof ItemStack) {
			return RecipeEntry.singleton((ItemStack)obj, amount);
		}
		if(obj instanceof Item) {
			return RecipeEntry.singleton(new ItemStack((Item)obj, amount));
		}
		if(obj instanceof Block) {
			return RecipeEntry.singleton(new ItemStack((Block)obj, amount));
		}
		if(obj instanceof IItemProvider) {
			return RecipeEntry.singleton(new ItemStack(((IItemProvider)obj).asItem(), amount));
		}
		if(obj instanceof Ingredient) {
			return (Ingredient)obj;
		}
		return null;
	}

	public <R extends RecipeFactory<R>> GregTechAddonRecipeSettings<R> recipeSettings(RecipeMap<R> recipeMap) {
		return new GregTechAddonRecipeSettings<>(recipeMap);
	}

	public GregTechAddonRecipeSettings<?> recipeSettings(String recipeMapName) {
		return new GregTechAddonRecipeSettings<>(CTRecipeMaps.getRecipeMap(recipeMapName));
	}

	public <R extends RecipeFactory<R>> boolean registerGregTechAddonRecipe(String key, GregTechAddonRecipeSettings settings) {
		return ApiImpl.INSTANCE.registerLateRecipe(key, new GregTechAddonRecipeAction(key, settings));
	}
}
