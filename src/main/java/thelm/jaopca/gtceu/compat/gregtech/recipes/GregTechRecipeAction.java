package thelm.jaopca.gtceu.compat.gregtech.recipes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.GTRecipeFluidInput;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import thelm.jaopca.api.recipes.IRecipeAction;
import thelm.jaopca.gtceu.compat.gregtech.GregTechHelper;
import thelm.jaopca.utils.MiscHelper;

public class GregTechRecipeAction<R extends RecipeBuilder<R>> implements IRecipeAction {

	private static final Logger LOGGER = LogManager.getLogger();

	public final ResourceLocation key;
	public final RecipeMap<R> recipeMap;
	public final int circuitMeta;
	public final List<Pair<Object, Integer>> input;
	public final List<Pair<Object, Integer>> fluidInput;
	public final List<Pair<Object, Triple<Integer, Integer, Integer>>> output;
	public final List<Pair<Object, Integer>> fluidOutput;
	public final Consumer<R> additional;
	public final int time;
	public final int energy;

	public GregTechRecipeAction(ResourceLocation key, GregTechRecipeSettings<R> settings) {
		this.key = Objects.requireNonNull(key);
		recipeMap = settings.recipeMap;
		circuitMeta = settings.circuitMeta;
		input = settings.input;
		fluidInput = settings.fluidInput;
		output = settings.output;
		fluidOutput = settings.fluidOutput;
		additional = settings.additional;
		time = settings.time;
		energy = settings.energy;
	}

	@Override
	public boolean register() {
		List<GTRecipeInput> inputs = new ArrayList<>();
		List<GTRecipeInput> fluidInputs = new ArrayList<>();
		List<Triple<ItemStack, Integer, Integer>> outputs = new ArrayList<>();
		List<FluidStack> fluidOutputs = new ArrayList<>();
		for(Pair<Object, Integer> in : input) {
			GTRecipeInput ing = GregTechHelper.INSTANCE.getGTRecipeInput(in.getLeft(), in.getRight());
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
			fluidInputs.add(GTRecipeFluidInput.getOrCreate(ing, ing.amount));
		}
		if(inputs.isEmpty() && fluidInputs.isEmpty()) {
			throw new IllegalArgumentException("Empty ingredients in recipe "+key+": "+input+", "+fluidInput);
		}
		for(Pair<Object, Triple<Integer, Integer, Integer>> out : output) {
			ItemStack stack = MiscHelper.INSTANCE.getItemStack(out.getLeft(), out.getRight().getLeft());
			if(stack.isEmpty()) {
				LOGGER.warn("Empty output in recipe {}: {}", key, out);
				continue;
			}
			outputs.add(Triple.of(stack, out.getRight().getMiddle(), out.getRight().getRight()));
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
		R builder = recipeMap.recipeBuilder();
		builder.inputIngredients(inputs).fluidInputs(fluidInputs);
		if(circuitMeta != -1) {
			builder.circuitMeta(circuitMeta);
		}
		for(Triple<ItemStack, Integer, Integer> triple : outputs) {
			if(triple.getMiddle() <= 0 || triple.getMiddle() >= 10000) {
				builder.outputs(triple.getLeft());
			}
			else {
				builder.chancedOutput(triple.getLeft(), triple.getMiddle(), triple.getRight());
			}
		}
		builder.fluidOutputs(fluidOutputs);
		if(time != -1) {
			builder.duration(time);
		}
		if(energy != -1) {
			builder.EUt(energy);
		}
		additional.accept(builder);
		builder.buildAndRegister();
		return true;
	}
}
