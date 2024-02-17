package thelm.jaopca.gtceu.compat.gtceu;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.google.gson.JsonElement;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.lowdragmc.lowdraglib.side.fluid.forge.FluidHelperImpl;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import thelm.jaopca.api.helpers.IMiscHelper;
import thelm.jaopca.gtceu.compat.gtceu.recipes.GTRecipeSerializer;
import thelm.jaopca.gtceu.compat.gtceu.recipes.GTRecipeSettings;
import thelm.jaopca.utils.ApiImpl;
import thelm.jaopca.utils.MiscHelper;

public class GTCEuHelper {

	public static final GTCEuHelper INSTANCE = new GTCEuHelper();

	private GTCEuHelper() {}

	public GTRecipeSettings recipeSettings() {
		return new GTRecipeSettings();
	}

	public FluidIngredient getFluidIngredient(Object obj, int amount) {
		return getFluidIngredientResolved(obj, amount).getLeft();
	}

	public Pair<FluidIngredient, Set<Fluid>> getFluidIngredientResolved(Object obj, int amount) {
		FluidIngredient ing = null;
		Set<Fluid> fluids = new HashSet<>();
		IMiscHelper helper = MiscHelper.INSTANCE;
		if(obj instanceof Supplier<?>) {
			Pair<FluidIngredient, Set<Fluid>> pair = getFluidIngredientResolved(((Supplier<?>)obj).get(), amount);
			ing = pair.getLeft();
			fluids.addAll(pair.getRight());
		}
		else if(obj instanceof FluidIngredient) {
			ing = (FluidIngredient)obj;
			// We can't know what fluids the ingredient can have so assume all
			fluids.addAll(ForgeRegistries.FLUIDS.getValues());
		}
		else if(obj instanceof String) {
			ResourceLocation location = new ResourceLocation((String)obj);
			ing = FluidIngredient.of(helper.getFluidTagKey(location), amount);
			fluids.addAll(helper.getFluidTagValues(location));
		}
		else if(obj instanceof ResourceLocation location) {
			ing = FluidIngredient.of(helper.getFluidTagKey(location), amount);
			fluids.addAll(helper.getFluidTagValues(location));
		}
		else if(obj instanceof TagKey key) {
			ing = FluidIngredient.of(key, amount);
			fluids.addAll(helper.getFluidTagValues(key.location()));
		}
		else if(obj instanceof FluidStack stack) {
			ing = FluidIngredient.of(FluidHelperImpl.toFluidStack(stack));
			fluids.add(stack.getFluid());
		}
		else if(obj instanceof FluidStack[] stacks) {
			ing = FluidIngredient.of(Arrays.stream(stacks).map(FluidHelperImpl::toFluidStack).
					toArray(com.lowdragmc.lowdraglib.side.fluid.FluidStack[]::new));
			Arrays.stream(stacks).map(FluidStack::getFluid).forEach(fluids::add);
		}
		else if(obj instanceof Fluid fluid) {
			ing = FluidIngredient.of(amount, fluid);
			fluids.add(fluid);
		}
		else if(obj instanceof Fluid[] fluidz) {
			ing = FluidIngredient.of(amount, fluidz);
			Collections.addAll(fluids, fluidz);
		}
		else if(obj instanceof JsonElement) {
			ing = FluidIngredient.fromJson((JsonElement)obj);
			// We can't know what fluids the ingredient can have so assume all
			fluids.addAll(ForgeRegistries.FLUIDS.getValues());
		}
		return Pair.of(fluids.isEmpty() ? null : ing, fluids);
	}

	public boolean registerGTRecipe(ResourceLocation key, GTRecipeType recipeType, GTRecipeSettings settings) {
		return ApiImpl.INSTANCE.registerRecipe(key, new GTRecipeSerializer(key, recipeType, settings));
	}

	public boolean registerGTRecipe(ResourceLocation key, String recipeType, GTRecipeSettings settings) {
		return ApiImpl.INSTANCE.registerRecipe(key, new GTRecipeSerializer(key, recipeType, settings));
	}
}
