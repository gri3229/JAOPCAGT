package thelm.jaopca.gtceu.compat.gtceu;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.resources.ResourceLocation;
import thelm.jaopca.gtceu.compat.gtceu.recipes.GTRecipeSerializer;
import thelm.jaopca.gtceu.compat.gtceu.recipes.GTRecipeSettings;
import thelm.jaopca.utils.ApiImpl;

public class GTCEuHelper {

	public static final GTCEuHelper INSTANCE = new GTCEuHelper();

	private GTCEuHelper() {}

	public GTRecipeSettings recipeSettings() {
		return new GTRecipeSettings();
	}

	public boolean registerGTRecipe(ResourceLocation key, GTRecipeType recipeType, GTRecipeSettings settings) {
		return ApiImpl.INSTANCE.registerRecipe(key, new GTRecipeSerializer(key, recipeType, settings));
	}

	public boolean registerGTRecipe(ResourceLocation key, String recipeType, GTRecipeSettings settings) {
		return ApiImpl.INSTANCE.registerRecipe(key, new GTRecipeSerializer(key, recipeType, settings));
	}
}
