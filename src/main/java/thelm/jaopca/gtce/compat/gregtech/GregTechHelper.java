package thelm.jaopca.gtce.compat.gregtech;

import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.util.ResourceLocation;
import thelm.jaopca.gtce.compat.gregtech.recipes.GregTechRecipeAction;
import thelm.jaopca.gtce.compat.gregtech.recipes.GregTechRecipeSettings;
import thelm.jaopca.utils.ApiImpl;

public class GregTechHelper {

	public static final GregTechHelper INSTANCE = new GregTechHelper();

	private GregTechHelper() {}

	public <R extends RecipeBuilder<R>> GregTechRecipeSettings<R> recipeSettings(RecipeMap<R> recipeMap) {
		return new GregTechRecipeSettings<>(recipeMap);
	}

	public GregTechRecipeSettings<?> recipeSettings(String recipeMapName) {
		return new GregTechRecipeSettings<>(RecipeMap.getByName(recipeMapName));
	}

	public <R extends RecipeBuilder<R>> boolean registerGregTechRecipe(ResourceLocation key, GregTechRecipeSettings settings) {
		return ApiImpl.INSTANCE.registerRecipe(key, new GregTechRecipeAction(key, settings));
	}
}
