package thelm.jaopca.gt5.compat.gregtech.recipes;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import gregtech.api.util.GT_Recipe;

public class GregTechRecipeSettings {

	public final GT_Recipe.GT_Recipe_Map recipeMap;
	public final List<Pair<Object, Integer>> input = new ArrayList<>();
	public final List<Pair<Object, Integer>> fluidInput = new ArrayList<>();
	public final List<Pair<Object, Pair<Integer, Integer>>> output = new ArrayList<>();
	public final List<Pair<Object, Integer>> fluidOutput = new ArrayList<>();
	public int time = 0;
	public int energy = 0;
	public int specialVal = 0;

	public GregTechRecipeSettings(GT_Recipe.GT_Recipe_Map recipeMap) {
		this.recipeMap = recipeMap;
	}

	public GregTechRecipeSettings input(Object input) {
		this.input.add(Pair.of(input, 1));
		return this;
	}

	public GregTechRecipeSettings shape(Object shape) {
		this.input.add(Pair.of(shape, 0));
		return this;
	}

	public GregTechRecipeSettings input(Object input, int count) {
		this.input.add(Pair.of(input, count));
		return this;
	}

	public GregTechRecipeSettings fluidInput(Object fluidInput) {
		this.fluidInput.add(Pair.of(fluidInput, 1000));
		return this;
	}

	public GregTechRecipeSettings fluidInput(Object fluidInput, int amount) {
		this.fluidInput.add(Pair.of(fluidInput, amount));
		return this;
	}

	public GregTechRecipeSettings output(Object output) {
		this.output.add(Pair.of(output, Pair.of(1, 10000)));
		return this;
	}

	public GregTechRecipeSettings output(Object output, int count) {
		this.output.add(Pair.of(output, Pair.of(count, 10000)));
		return this;
	}

	public GregTechRecipeSettings output(Object output, int count, int chance) {
		this.output.add(Pair.of(output, Pair.of(count, chance)));
		return this;
	}

	public GregTechRecipeSettings fluidOutput(Object fluidOutput) {
		this.fluidOutput.add(Pair.of(fluidOutput, 1000));
		return this;
	}

	public GregTechRecipeSettings fluidOutput(Object fluidOutput, int amount) {
		this.fluidOutput.add(Pair.of(fluidOutput, amount));
		return this;
	}

	public GregTechRecipeSettings time(int time) {
		this.time = time;
		return this;
	}

	public GregTechRecipeSettings energy(int energy) {
		this.energy = energy;
		return this;
	}

	public GregTechRecipeSettings specialVal(int specialVal) {
		this.specialVal = specialVal;
		return this;
	}
}
