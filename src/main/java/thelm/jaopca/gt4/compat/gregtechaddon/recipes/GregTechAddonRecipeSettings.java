package thelm.jaopca.gt4.compat.gregtechaddon.recipes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;

import gregtechmod.api.recipe.RecipeFactory;
import gregtechmod.api.recipe.RecipeMap;

public class GregTechAddonRecipeSettings<R extends RecipeFactory<R>> {

	public final RecipeMap<R> recipeMap;
	public boolean shaped = false;
	public final List<Pair<Object, Integer>> input = new ArrayList<>();
	public final List<Pair<Object, Integer>> fluidInput = new ArrayList<>();
	public final List<Pair<Object, Pair<Integer, Integer>>> output = new ArrayList<>();
	public final List<Pair<Object, Integer>> fluidOutput = new ArrayList<>();
	public Consumer<R> additional = b->{};
	public int energy = -1;
	public int time = -1;

	public GregTechAddonRecipeSettings(RecipeMap<R> recipeMap) {
		this.recipeMap = recipeMap;
	}

	public GregTechAddonRecipeSettings<R> shaped(boolean shaped) {
		this.shaped = shaped;
		return this;
	}

	public GregTechAddonRecipeSettings<R> input(Object input) {
		this.input.add(Pair.of(input, 1));
		return this;
	}

	public GregTechAddonRecipeSettings<R> shape(Object shape) {
		this.input.add(Pair.of(shape, 0));
		return this;
	}

	public GregTechAddonRecipeSettings<R> input(Object input, int count) {
		this.input.add(Pair.of(input, count));
		return this;
	}

	public GregTechAddonRecipeSettings<R> fluidInput(Object fluidInput) {
		this.fluidInput.add(Pair.of(fluidInput, 1000));
		return this;
	}

	public GregTechAddonRecipeSettings<R> fluidInput(Object fluidInput, int amount) {
		this.fluidInput.add(Pair.of(fluidInput, amount));
		return this;
	}

	public GregTechAddonRecipeSettings<R> output(Object output) {
		this.output.add(Pair.of(output, Pair.of(1, 10000)));
		return this;
	}

	public GregTechAddonRecipeSettings<R> output(Object output, int count) {
		this.output.add(Pair.of(output, Pair.of(count, 10000)));
		return this;
	}

	public GregTechAddonRecipeSettings<R> output(Object output, int count, int chance) {
		this.output.add(Pair.of(output, Pair.of(count, chance)));
		return this;
	}

	public GregTechAddonRecipeSettings<R> fluidOutput(Object fluidOutput) {
		this.fluidOutput.add(Pair.of(fluidOutput, 1000));
		return this;
	}

	public GregTechAddonRecipeSettings<R> fluidOutput(Object fluidOutput, int amount) {
		this.fluidOutput.add(Pair.of(fluidOutput, amount));
		return this;
	}

	public GregTechAddonRecipeSettings<R> additional(Consumer<R> additional) {
		this.additional = this.additional.andThen(additional);
		return this;
	}

	public GregTechAddonRecipeSettings<R> energy(int energy) {
		this.energy = energy;
		return this;
	}

	public GregTechAddonRecipeSettings<R> time(int time) {
		this.time = time;
		return this;
	}
}
