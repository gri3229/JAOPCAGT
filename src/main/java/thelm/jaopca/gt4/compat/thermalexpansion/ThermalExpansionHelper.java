package thelm.jaopca.gt4.compat.thermalexpansion;

import thelm.jaopca.gt4.compat.thermalexpansion.recipes.PulverizerRecipeAction;
import thelm.jaopca.gt4.compat.thermalexpansion.recipes.SmelterRecipeAction;
import thelm.jaopca.utils.ApiImpl;

public class ThermalExpansionHelper {

	public static final ThermalExpansionHelper INSTANCE = new ThermalExpansionHelper();

	private ThermalExpansionHelper() {}

	public boolean registerPulverizerRecipe(String key, Object input, int inputCount, Object output, int outputCount, Object secondOutput, int secondOutputCount, int secondOutputChance, int energy) {
		return ApiImpl.INSTANCE.registerLateRecipe(key, new PulverizerRecipeAction(key, input, inputCount, output, outputCount, secondOutput, secondOutputCount, secondOutputChance, energy));
	}

	public boolean registerPulverizerRecipe(String key, Object input, int inputCount, Object output, int outputCount, int energy) {
		return ApiImpl.INSTANCE.registerLateRecipe(key, new PulverizerRecipeAction(key, input, inputCount, output, outputCount, energy));
	}

	public boolean registerSmelterRecipe(String key, Object input, int inputCount, Object secondInput, int secondInputCount, Object output, int outputCount, Object secondOutput, int secondOutputCount, int secondOutputChance, int energy) {
		return ApiImpl.INSTANCE.registerLateRecipe(key, new SmelterRecipeAction(key, input, inputCount, secondInput, secondInputCount, output, outputCount, secondOutput, secondOutputCount, secondOutputChance, energy));
	}

	public boolean registerSmelterRecipe(String key, Object input, int inputCount, Object secondInput, int secondInputCount, Object output, int outputCount, int energy) {
		return ApiImpl.INSTANCE.registerLateRecipe(key, new SmelterRecipeAction(key, input, inputCount, secondInput, secondInputCount, output, outputCount, energy));
	}
}
