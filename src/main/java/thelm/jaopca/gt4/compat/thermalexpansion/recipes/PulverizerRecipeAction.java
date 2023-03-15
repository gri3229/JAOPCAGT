package thelm.jaopca.gt4.compat.thermalexpansion.recipes;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cofh.thermalexpansion.util.crafting.PulverizerManager;
import net.minecraft.item.ItemStack;
import thelm.jaopca.api.recipes.IRecipeAction;
import thelm.jaopca.utils.MiscHelper;

public class PulverizerRecipeAction implements IRecipeAction {

	private static final Logger LOGGER = LogManager.getLogger();

	public final String key;
	public final Object input;
	public final int inputCount;
	public final Object output;
	public final int outputCount;
	public final Object secondOutput;
	public final int secondOutputCount;
	public final int secondOutputChance;
	public final int energy;

	public PulverizerRecipeAction(String key, Object input, int inputCount, Object output, int outputCount, int energy) {
		this(key, input, inputCount, output, outputCount, null, 0, 0, energy);
	}

	public PulverizerRecipeAction(String key, Object input, int inputCount, Object output, int outputCount, Object secondOutput, int secondOutputCount, int secondOutputChance, int energy) {
		this.key = Objects.requireNonNull(key);
		this.input = input;
		this.inputCount = inputCount;
		this.output = output;
		this.outputCount = outputCount;
		this.secondOutput = secondOutput;
		this.secondOutputCount = secondOutputCount;
		this.secondOutputChance = secondOutputChance;
		this.energy = energy;
	}

	@Override
	public boolean register() {
		ItemStack ing = MiscHelper.INSTANCE.getItemStack(input, inputCount, true);
		if(ing == null) {
			throw new IllegalArgumentException("Empty ingredient in recipe "+key+": "+input);
		}
		ItemStack stack1 = MiscHelper.INSTANCE.getItemStack(output, outputCount, false);
		ItemStack stack2 = MiscHelper.INSTANCE.getItemStack(secondOutput, secondOutputCount, false);
		if(stack1 == null && stack2 == null) {
			throw new IllegalArgumentException("Empty outputs in recipe "+key+": "+output+", "+secondOutput);
		}
		return PulverizerManager.addRecipe(energy, ing, stack1, stack2, secondOutputChance, true);
	}
}
