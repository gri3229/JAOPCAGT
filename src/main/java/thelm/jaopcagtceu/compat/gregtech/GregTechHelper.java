package thelm.jaopcagtceu.compat.gregtech;

import java.util.function.Supplier;

import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.GTRecipeFluidInput;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.recipes.ingredients.GTRecipeOreInput;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import thelm.jaopca.api.fluids.IFluidProvider;
import thelm.jaopca.api.items.IItemProvider;
import thelm.jaopca.utils.ApiImpl;
import thelm.jaopcagtceu.compat.gregtech.recipes.GregTechRecipeAction;
import thelm.jaopcagtceu.compat.gregtech.recipes.GregTechRecipeSettings;

public class GregTechHelper {

	public static final GregTechHelper INSTANCE = new GregTechHelper();

	private GregTechHelper() {}

	public GTRecipeInput getGTRecipeInput(Object obj, int amount) {
		GTRecipeInput ret = null;
		boolean notConsumed = false;
		if(amount <= 0) {
			notConsumed = true;
			amount = 1;
		}
		if(obj instanceof Supplier<?>) {
			ret = getGTRecipeInput(((Supplier<?>)obj).get(), amount);
		}
		if(obj instanceof String) {
			if(ApiImpl.INSTANCE.getOredict().contains(obj)) {
				ret = GTRecipeOreInput.getOrCreate((String)obj, amount);
			}
		}
		if(obj instanceof ItemStack) {
			ret = GTRecipeItemInput.getOrCreate((ItemStack)obj, amount);
		}
		if(obj instanceof Item) {
			ret = GTRecipeItemInput.getOrCreate(new ItemStack((Item)obj, amount));
		}
		if(obj instanceof Block) {
			ret = GTRecipeItemInput.getOrCreate(new ItemStack((Block)obj, amount));
		}
		if(obj instanceof IItemProvider) {
			ret = GTRecipeItemInput.getOrCreate(new ItemStack(((IItemProvider)obj).asItem(), amount));
		}
		if(obj instanceof FluidStack) {
			ret = GTRecipeFluidInput.getOrCreate((FluidStack)obj, amount);
		}
		if(obj instanceof Fluid) {
			ret = GTRecipeFluidInput.getOrCreate((Fluid)obj, amount);
		}
		if(obj instanceof IFluidProvider) {
			ret = GTRecipeFluidInput.getOrCreate(((IFluidProvider)obj).asFluid(), amount);
		}
		//if(obj instanceof Ingredient) {
		//	return GTRecipeItemInput.getOrCreate(((Ingredient)obj).getMatchingStacks(), count);
		//}
		if(obj instanceof GTRecipeInput) {
			ret = (GTRecipeInput)obj;
		}
		if(ret != null && notConsumed) {
			return ret.setNonConsumable();
		}
		return ret;
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
