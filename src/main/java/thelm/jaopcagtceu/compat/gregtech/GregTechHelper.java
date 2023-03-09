package thelm.jaopcagtceu.compat.gregtech;

import java.util.function.Supplier;

import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.recipes.ingredients.GTRecipeOreInput;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thelm.jaopca.api.items.IItemProvider;
import thelm.jaopca.utils.ApiImpl;
import thelm.jaopcagtceu.compat.gregtech.recipes.GregTechRecipeAction;
import thelm.jaopcagtceu.compat.gregtech.recipes.GregTechRecipeSettings;

public class GregTechHelper {

	public static final GregTechHelper INSTANCE = new GregTechHelper();

	private GregTechHelper() {}

	public GTRecipeInput getGTRecipeInput(Object obj, int count) {
		if(obj instanceof Supplier<?>) {
			return getGTRecipeInput(((Supplier<?>)obj).get(), count);
		}
		if(obj instanceof String) {
			if(ApiImpl.INSTANCE.getOredict().contains(obj)) {
				return GTRecipeOreInput.getOrCreate((String)obj, count);
			}
		}
		if(obj instanceof ItemStack) {
			return GTRecipeItemInput.getOrCreate((ItemStack)obj, count);
		}
		if(obj instanceof Item) {
			return GTRecipeItemInput.getOrCreate(new ItemStack((Item)obj, count));
		}
		if(obj instanceof Block) {
			return GTRecipeItemInput.getOrCreate(new ItemStack((Block)obj, count));
		}
		if(obj instanceof IItemProvider) {
			return GTRecipeItemInput.getOrCreate(new ItemStack(((IItemProvider)obj).asItem(), count));
		}
		//if(obj instanceof Ingredient) {
		//	return GTRecipeItemInput.getOrCreate(((Ingredient)obj).getMatchingStacks(), count);
		//}
		if(obj instanceof GTRecipeInput) {
			return (GTRecipeInput)obj;
		}
		return null;
	}

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
