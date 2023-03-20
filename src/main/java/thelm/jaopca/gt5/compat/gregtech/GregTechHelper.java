package thelm.jaopca.gt5.compat.gregtech;

import gregtech.api.util.GT_Recipe;
import thelm.jaopca.gt5.compat.gregtech.recipes.GregTechRecipeAction;
import thelm.jaopca.gt5.compat.gregtech.recipes.GregTechRecipeSettings;
import thelm.jaopca.utils.ApiImpl;

public class GregTechHelper {

	public static final GregTechHelper INSTANCE = new GregTechHelper();

	private GregTechHelper() {}

	public GregTechRecipeSettings recipeSettings(GT_Recipe.GT_Recipe_Map recipeMap) {
		return new GregTechRecipeSettings(recipeMap);
	}

	public GregTechRecipeSettings recipeSettings(String recipeMapName) {
		return new GregTechRecipeSettings(GT_Recipe.GT_Recipe_Map.sMappings.stream().filter(m->m.mUnlocalizedName.equals(recipeMapName)).findAny().get());
	}

	public boolean registerGregTechRecipe(String key, GregTechRecipeSettings settings) {
		return ApiImpl.INSTANCE.registerLateRecipe(key, new GregTechRecipeAction(key, settings));
	}
}
