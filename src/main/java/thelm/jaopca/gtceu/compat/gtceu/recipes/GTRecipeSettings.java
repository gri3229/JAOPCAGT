package thelm.jaopca.gtceu.compat.gtceu.recipes;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.recipe.condition.BiomeCondition;
import com.gregtechceu.gtceu.common.recipe.condition.CleanroomCondition;
import com.gregtechceu.gtceu.common.recipe.condition.DimensionCondition;
import com.gregtechceu.gtceu.common.recipe.condition.PositionYCondition;
import com.gregtechceu.gtceu.common.recipe.condition.RPMCondition;
import com.gregtechceu.gtceu.common.recipe.condition.RainingCondition;
import com.gregtechceu.gtceu.common.recipe.condition.ThunderCondition;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

public class GTRecipeSettings {

	public OptionalLong euInput = OptionalLong.empty();
	public OptionalLong euOutput = OptionalLong.empty();
	public OptionalLong euTick = OptionalLong.empty();
	public List<Pair<Object, Triple<Integer, Integer, Integer>>> itemInput = new ArrayList<>();
	public List<Pair<Object, Triple<Integer, Integer, Integer>>> itemOutput = new ArrayList<>();
	public List<Pair<Object, Triple<Integer, Integer, Integer>>> fluidInput = new ArrayList<>();
	public List<Pair<Object, Triple<Integer, Integer, Integer>>> fluidOutput = new ArrayList<>();
	public OptionalDouble stressInput = OptionalDouble.empty();
	public OptionalDouble stressOutput = OptionalDouble.empty();
	public CompoundTag data = new CompoundTag();
	public List<RecipeCondition> conditions = new ArrayList<>();
	public OptionalInt duration = OptionalInt.empty();

	public GTRecipeSettings euInput(long eu) {
		euInput = OptionalLong.of(eu);
		return this;
	}

	public GTRecipeSettings EUt(long eu) {
		euTick = OptionalLong.of(eu);
		return this;
	}

	public GTRecipeSettings euOutput(long eu) {
		euOutput = OptionalLong.of(eu);
		return this;
	}

	public GTRecipeSettings itemInput(Object input) {
		return itemInput(input, 1, 10000, 0);
	}

	public GTRecipeSettings itemInput(Object input, int count) {
		return itemInput(input, count, 10000, 0);
	}

	public GTRecipeSettings itemInput(Object input, int chance, int tierChanceBoost) {
		return itemInput(input, 1, chance, tierChanceBoost);
	}

	public GTRecipeSettings itemInput(Object input, int count, int chance, int tierChanceBoost) {
		itemInput.add(Pair.of(input, Triple.of(count, chance, tierChanceBoost)));
		return this;
	}

	public GTRecipeSettings notConsumable(Object input) {
		return itemInput(input, 1, 0, 0);
	}

	public GTRecipeSettings circuitMeta(int configuration) {
		return notConsumable(StrictNBTIngredient.of(IntCircuitBehaviour.stack(configuration)));
	}

	public GTRecipeSettings itemOutput(Object output) {
		return itemOutput(output, 1, 10000, 0);
	}

	public GTRecipeSettings itemOutput(Object output, int count) {
		return itemOutput(output, count, 10000, 0);
	}

	public GTRecipeSettings itemOutput(Object output, int chance, int tierChanceBoost) {
		return itemOutput(output, 1, chance, tierChanceBoost);
	}

	public GTRecipeSettings itemOutput(Object output, int count, int chance, int tierChanceBoost) {
		itemOutput.add(Pair.of(output, Triple.of(count, chance, tierChanceBoost)));
		return this;
	}

	public GTRecipeSettings fluidInput(Object input, int amount) {
		return fluidInput(input, amount, 10000, 0);
	}

	public GTRecipeSettings fluidInput(Object input, int amount, int chance, int tierChanceBoost) {
		fluidInput.add(Pair.of(input, Triple.of(amount, chance, tierChanceBoost)));
		return this;
	}

	public GTRecipeSettings fluidOutput(Object output, int amount) {
		return fluidOutput(output, amount, 10000, 0);
	}

	public GTRecipeSettings fluidOutput(Object output, int amount, int chance, int tierChanceBoost) {
		fluidOutput.add(Pair.of(output, Triple.of(amount, chance, tierChanceBoost)));
		return this;
	}

	public GTRecipeSettings stressInput(float stress) {
		stressInput = OptionalDouble.of(stress);
		return this;
	}

	public GTRecipeSettings stressOutput(float stress) {
		stressOutput = OptionalDouble.of(stress);
		return this;
	}

	public GTRecipeSettings addData(String key, Tag data) {
		this.data.put(key, data);
		return this;
	}

	public GTRecipeSettings addData(String key, int data) {
		this.data.putInt(key, data);
		return this;
	}

	public GTRecipeSettings addData(String key, long data) {
		this.data.putLong(key, data);
		return this;
	}

	public GTRecipeSettings addData(String key, String data) {
		this.data.putString(key, data);
		return this;
	}

	public GTRecipeSettings addData(String key, Float data) {
		this.data.putFloat(key, data);
		return this;
	}

	public GTRecipeSettings addData(String key, boolean data) {
		this.data.putBoolean(key, data);
		return this;
	}

	public GTRecipeSettings blastFurnaceTemp(int blastTemp) {
		return addData("ebf_temp", blastTemp);
	}

	public GTRecipeSettings explosivesAmount(int explosivesAmount) {
		return addData("explosives_amount", explosivesAmount);
	}

	public GTRecipeSettings explosivesType(ItemStack explosivesType) {
		return addData("explosives_type", explosivesType.save(new CompoundTag()));
	}

	public GTRecipeSettings solderMultiplier(int multiplier) {
		return addData("solderMultiplier", multiplier);
	}

	public GTRecipeSettings disableDistilleryRecipes(boolean flag) {
		return addData("disable_distillery", flag);
	}

	public GTRecipeSettings fusionStartEU(long eu) {
		return addData("eu_to_start",  eu);
	}

	public GTRecipeSettings addCondition(RecipeCondition condition) {
		conditions.add(condition);
		return this;
	}

	public GTRecipeSettings cleanroom(CleanroomType cleanroomType) {
		return addCondition(new CleanroomCondition(cleanroomType));
	}

	public GTRecipeSettings dimension(ResourceLocation dimension, boolean reverse) {
		return addCondition(new DimensionCondition(dimension).setReverse(reverse));
	}

	public GTRecipeSettings dimension(ResourceLocation dimension) {
		return dimension(dimension, false);
	}

	public GTRecipeSettings biome(ResourceLocation biome, boolean reverse) {
		return addCondition(new BiomeCondition(biome).setReverse(reverse));
	}

	public GTRecipeSettings biome(ResourceLocation biome) {
		return biome(biome, false);
	}

	public GTRecipeSettings rain(float level, boolean reverse) {
		return addCondition(new RainingCondition(level).setReverse(reverse));
	}

	public GTRecipeSettings rain(float level) {
		return rain(level, false);
	}

	public GTRecipeSettings thunder(float level, boolean reverse) {
		return addCondition(new ThunderCondition(level).setReverse(reverse));
	}

	public GTRecipeSettings thunder(float level) {
		return thunder(level, false);
	}

	public GTRecipeSettings posY(int min, int max, boolean reverse) {
		return addCondition(new PositionYCondition(min, max).setReverse(reverse));
	}

	public GTRecipeSettings posY(int min, int max) {
		return posY(min, max, false);
	}

	public GTRecipeSettings rpm(float rpm, boolean reverse) {
		return addCondition(new RPMCondition(rpm).setReverse(reverse));
	}

	public GTRecipeSettings rpm(float rpm) {
		return rpm(rpm, false);
	}

	public GTRecipeSettings duration(int duration) {
		this.duration = OptionalInt.of(duration);
		return this;
	}
}
