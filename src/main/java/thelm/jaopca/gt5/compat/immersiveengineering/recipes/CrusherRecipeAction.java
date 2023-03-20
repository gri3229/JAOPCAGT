package thelm.jaopca.gt5.compat.immersiveengineering.recipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import blusunrize.immersiveengineering.common.IERecipes;
import net.minecraft.item.ItemStack;
import thelm.jaopca.api.recipes.IRecipeAction;
import thelm.jaopca.utils.MiscHelper;

public class CrusherRecipeAction implements IRecipeAction {

	private static final Logger LOGGER = LogManager.getLogger();

	public final String key;
	public final Object input;
	public final Object[] output;
	public final int energy;

	public CrusherRecipeAction(String key, Object input, Object[] output, int energy) {
		this.key = Objects.requireNonNull(key);
		this.input = input;
		this.output = output;
		this.energy = energy;
	}

	@Override
	public boolean register() {
		Object ing = input instanceof String ? input : MiscHelper.INSTANCE.getItemStacks(input, 1, true);
		if(ing == null) {
			throw new IllegalArgumentException("Empty ingredient in recipe "+key+": "+input);
		}
		ItemStack result = null;
		List<Pair<ItemStack, Float>> secondary = new ArrayList<>();
		int i = 0;
		while(i < output.length) {
			Object out = output[i];
			++i;
			Integer count = 1;
			if(i < output.length && output[i] instanceof Integer) {
				count = (Integer)output[i];
				++i;
			}
			Float chance = 1F;
			if(i < output.length && output[i] instanceof Float) {
				chance = (Float)output[i];
				++i;
			}
			ItemStack is = MiscHelper.INSTANCE.getItemStack(out, count, false);
			if(is == null) {
				LOGGER.warn("Empty output in recipe {}: {}", key, out);
				continue;
			}
			if(result == null) {
				result = is;
			}
			else {
				secondary.add(Pair.of(is, chance));
			}
		}
		if(result == null) {
			throw new IllegalArgumentException("Empty outputs in recipe "+key+": "+Arrays.deepToString(output));
		}
		Object[] arr = secondary.stream().flatMap(p->Stream.of(p.getLeft(), p.getRight())).toArray();
		IERecipes.addCrusherRecipe(result, ing, energy, arr);
		return true;
	}
}
