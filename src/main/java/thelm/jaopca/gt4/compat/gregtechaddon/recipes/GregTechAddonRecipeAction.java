package thelm.jaopca.gt4.compat.gregtechaddon.recipes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gregtechmod.api.recipe.Ingredient;
import gregtechmod.api.recipe.RecipeFactory;
import gregtechmod.api.recipe.RecipeMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import thelm.jaopca.api.recipes.IRecipeAction;
import thelm.jaopca.gt4.compat.gregtechaddon.GregTechAddonHelper;
import thelm.jaopca.utils.MiscHelper;

public class GregTechAddonRecipeAction<R extends RecipeFactory<R>> implements IRecipeAction {

	private static final Logger LOGGER = LogManager.getLogger();

	public final String key;
	public final RecipeMap<R> recipeMap;
	public final boolean shaped;
	public final List<Pair<Object, Integer>> input;
	public final List<Pair<Object, Integer>> fluidInput;
	public final List<Pair<Object, Pair<Integer, Integer>>> output;
	public final List<Pair<Object, Integer>> fluidOutput;
	public final Consumer<R> additional;
	public final int energy;
	public final int time;

	public GregTechAddonRecipeAction(String key, GregTechAddonRecipeSettings<R> settings) {
		this.key = Objects.requireNonNull(key);
		recipeMap = settings.recipeMap;
		shaped = settings.shaped;
		input = settings.input;
		fluidInput = settings.fluidInput;
		output = settings.output;
		fluidOutput = settings.fluidOutput;
		additional = settings.additional;
		energy = settings.energy;
		time = settings.time;
	}

	@Override
	public boolean register() {
		List<Ingredient> inputs = new ArrayList<>();
		List<FluidStack> fluidInputs = new ArrayList<>();
		List<Pair<ItemStack, Integer>> outputs = new ArrayList<>();
		List<FluidStack> fluidOutputs = new ArrayList<>();
		for(Pair<Object, Integer> in : input) {
			Ingredient ing = GregTechAddonHelper.INSTANCE.getIngredient(in.getLeft(), in.getRight());
			if(ing == null) {
				throw new IllegalArgumentException("Empty ingredient in recipe "+key+": "+in);
			}
			inputs.add(ing);
		}
		for(Pair<Object, Integer> in : fluidInput) {
			FluidStack ing = MiscHelper.INSTANCE.getFluidStack(in.getLeft(), in.getRight());
			if(ing == null) {
				throw new IllegalArgumentException("Empty ingredient in recipe "+key+": "+in);
			}
			fluidInputs.add(ing);
		}
		if(inputs.isEmpty() && fluidInputs.isEmpty()) {
			throw new IllegalArgumentException("Empty ingredients in recipe "+key+": "+input+", "+fluidInput);
		}
		for(Pair<Object, Pair<Integer, Integer>> out : output) {
			ItemStack stack = MiscHelper.INSTANCE.getItemStack(out.getLeft(), out.getRight().getLeft(), false);
			if(stack == null) {
				LOGGER.warn("Empty output in recipe {}: {}", key, out);
				continue;
			}
			outputs.add(Pair.of(stack, out.getRight().getRight()));
		}
		for(Pair<Object, Integer> out : fluidOutput) {
			FluidStack stack = MiscHelper.INSTANCE.getFluidStack(out.getLeft(), out.getRight());
			if(stack == null) {
				LOGGER.warn("Empty output in recipe {}: {}", key, out);
				continue;
			}
			fluidOutputs.add(stack);
		}
		if(outputs.isEmpty() && fluidOutputs.isEmpty()) {
			throw new IllegalArgumentException("Empty outputs in recipe "+key+": "+output+", "+fluidOutput);
		}
		R builder = recipeMap.factory();
		builder.setShaped(shaped);
		for(Ingredient in : inputs) {
			builder.input(in);
		}
		for(FluidStack in : fluidInputs) {
			builder.input(in);
		}
		for(Pair<ItemStack, Integer> pair : outputs) {
			if(pair.getRight() <= 0 || pair.getRight() >= 10000) {
				builder.outputs(pair.getLeft());
			}
			else {
				builder.chanced(pair.getLeft(), pair.getRight());
			}
		}
		for(FluidStack out : fluidOutputs) {
			builder.outputs(out);
		}
		if(energy != -1) {
			builder.EUt(energy);
		}
		if(time != -1) {
			builder.duration(time);
		}
		additional.accept(builder);
		builder.buildAndRegister();
		return true;
	}
}
