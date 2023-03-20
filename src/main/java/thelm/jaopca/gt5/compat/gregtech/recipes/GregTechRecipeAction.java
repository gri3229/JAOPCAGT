package thelm.jaopca.gt5.compat.gregtech.recipes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gregtech.api.util.GT_Recipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import thelm.jaopca.api.recipes.IRecipeAction;
import thelm.jaopca.utils.MiscHelper;

public class GregTechRecipeAction implements IRecipeAction {

	private static final Logger LOGGER = LogManager.getLogger();

	public final String key;
	public final GT_Recipe.GT_Recipe_Map recipeMap;
	public final List<Pair<Object, Integer>> input;
	public final List<Pair<Object, Integer>> fluidInput;
	public final List<Pair<Object, Pair<Integer, Integer>>> output;
	public final List<Pair<Object, Integer>> fluidOutput;
	public final int time;
	public final int energy;
	public final int specialVal;

	public GregTechRecipeAction(String key, GregTechRecipeSettings settings) {
		this.key = Objects.requireNonNull(key);
		recipeMap = settings.recipeMap;
		input = settings.input;
		fluidInput = settings.fluidInput;
		output = settings.output;
		fluidOutput = settings.fluidOutput;
		time = settings.time;
		energy = settings.energy;
		specialVal = settings.specialVal;
	}

	@Override
	public boolean register() {
		List<ItemStack> inputs = new ArrayList<>();
		List<FluidStack> fluidInputs = new ArrayList<>();
		List<ItemStack> outputs = new ArrayList<>();
		List<Integer> chances = new ArrayList<>();
		List<FluidStack> fluidOutputs = new ArrayList<>();
		for(Pair<Object, Integer> in : input) {
			ItemStack ing = MiscHelper.INSTANCE.getItemStack(in.getLeft(), in.getRight(), true);
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
			outputs.add(stack);
			chances.add(out.getRight().getRight());
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
		ItemStack[] ins = inputs.stream().toArray(ItemStack[]::new);
		FluidStack[] fIns = fluidInputs.stream().toArray(FluidStack[]::new);
		ItemStack[] outs = outputs.stream().toArray(ItemStack[]::new);
		int[] cs = chances.stream().mapToInt(i->i).toArray();
		FluidStack[] fOuts = fluidOutputs.stream().toArray(FluidStack[]::new);
		return recipeMap.addRecipe(true, ins, outs, null, cs, fIns, fOuts, time, energy, specialVal) != null;
	}
}
