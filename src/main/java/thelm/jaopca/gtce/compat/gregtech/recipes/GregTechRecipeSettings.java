package thelm.jaopca.gtce.compat.gregtech.recipes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;

public class GregTechRecipeSettings<R extends RecipeBuilder<R>> {

	public final RecipeMap<R> recipeMap;
	public final List<Pair<Object, Integer>> input = new ArrayList<>();
	public final List<Pair<Object, Integer>> fluidInput = new ArrayList<>();
	public final List<Pair<Object, Triple<Integer, Integer, Integer>>> output = new ArrayList<>();
	public final List<Pair<Object, Integer>> fluidOutput = new ArrayList<>();
	public Consumer<R> additional = b->{};
	public int time = -1;
	public int energy = -1;

	public GregTechRecipeSettings(RecipeMap<R> recipeMap) {
		this.recipeMap = recipeMap;
	}

	public GregTechRecipeSettings<R> input(Object input) {
		this.input.add(Pair.of(input, 1));
		return this;
	}

	public GregTechRecipeSettings<R> shape(Object shape) {
		this.input.add(Pair.of(shape, 0));
		return this;
	}

	public GregTechRecipeSettings<R> input(Object input, int count) {
		this.input.add(Pair.of(input, count));
		return this;
	}

	public GregTechRecipeSettings<R> fluidInput(Object fluidInput) {
		this.fluidInput.add(Pair.of(fluidInput, 1000));
		return this;
	}

	public GregTechRecipeSettings<R> fluidInput(Object fluidInput, int amount) {
		this.fluidInput.add(Pair.of(fluidInput, amount));
		return this;
	}

	public GregTechRecipeSettings<R> output(Object output) {
		this.output.add(Pair.of(output, Triple.of(1, 10000, 0)));
		return this;
	}

	public GregTechRecipeSettings<R> output(Object output, int count) {
		this.output.add(Pair.of(output, Triple.of(count, 10000, 0)));
		return this;
	}

	public GregTechRecipeSettings<R> output(Object output, int chance, int tierBoost) {
		this.output.add(Pair.of(output, Triple.of(1, chance, tierBoost)));
		return this;
	}

	public GregTechRecipeSettings<R> output(Object output, int count, int chance, int tierBoost) {
		this.output.add(Pair.of(output, Triple.of(count, chance, tierBoost)));
		return this;
	}

	public GregTechRecipeSettings<R> fluidOutput(Object fluidOutput) {
		this.fluidOutput.add(Pair.of(fluidOutput, 1000));
		return this;
	}

	public GregTechRecipeSettings<R> fluidOutput(Object fluidOutput, int amount) {
		this.fluidOutput.add(Pair.of(fluidOutput, amount));
		return this;
	}

	public GregTechRecipeSettings<R> additional(Consumer<R> additional) {
		this.additional = this.additional.andThen(additional);
		return this;
	}

	public GregTechRecipeSettings<R> time(int time) {
		this.time = time;
		return this;
	}

	public GregTechRecipeSettings<R> energy(int energy) {
		this.energy = energy;
		return this;
	}
}
