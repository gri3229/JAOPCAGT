package thelm.jaopca.gtceu.compat.gtceu.recipes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalLong;

import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonElement;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.mojang.serialization.Codec;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import thelm.jaopca.api.recipes.IRecipeSerializer;
import thelm.jaopca.gtceu.compat.gtceu.GTCEuHelper;
import thelm.jaopca.ingredients.EmptyIngredient;
import thelm.jaopca.utils.MiscHelper;

public class GTRecipeSerializer implements IRecipeSerializer {

	private static final Logger LOGGER = LogManager.getLogger();

	public final ResourceLocation key;
	public final GTRecipeType recipeType;
	public final OptionalLong euInput;
	public final OptionalLong euOutput;
	public final OptionalLong euTick;
	public final List<Pair<Object, Triple<Integer, Integer, Integer>>> itemInput;
	public final List<Pair<Object, Triple<Integer, Integer, Integer>>> itemOutput;
	public final List<Pair<Object, Triple<Integer, Integer, Integer>>> fluidInput;
	public final List<Pair<Object, Triple<Integer, Integer, Integer>>> fluidOutput;
	public final OptionalDouble stressInput;
	public final OptionalDouble stressOutput;
	public final CompoundTag data;
	public final List<RecipeCondition> conditions;

	public GTRecipeSerializer(ResourceLocation key, String recipeType, GTRecipeSettings settings) {
		this(key, GTRecipeTypes.get(recipeType), settings);
	}

	public GTRecipeSerializer(ResourceLocation key, GTRecipeType recipeType, GTRecipeSettings settings) {
		this.key = Objects.requireNonNull(key);
		this.recipeType = Objects.requireNonNull(recipeType);
		this.euInput = settings.euInput;
		this.euOutput = settings.euOutput;
		this.euTick = settings.euTick;
		this.itemInput = settings.itemInput;
		this.itemOutput = settings.itemOutput;
		this.fluidInput = settings.fluidInput;
		this.fluidOutput = settings.fluidOutput;
		this.stressInput = settings.stressInput;
		this.stressOutput = settings.stressOutput;
		this.data = settings.data;
		this.conditions = settings.conditions;
	}

	@Override
	public JsonElement get() {
		GTRecipeBuilder builder = recipeType.recipeBuilder(key);

		List<Content> itemInputs = new ArrayList<>();
		List<Content> fluidInputs = new ArrayList<>();
		List<Content> itemOutputs = new ArrayList<>();
		List<Content> fluidOutputs = new ArrayList<>();
		for(Pair<Object, Triple<Integer, Integer, Integer>> in : itemInput) {
			Ingredient ing = MiscHelper.INSTANCE.getIngredient(in.getLeft());
			if(ing == EmptyIngredient.INSTANCE) {
				throw new IllegalArgumentException("Empty ingredient in recipe "+key+": "+in);
			}
			itemInputs.add(new Content(
					SizedIngredient.create(ing, in.getRight().getLeft()),
					in.getRight().getMiddle(), builder.maxChance, in.getRight().getRight(),
					builder.slotName, builder.uiName));
		}
		for(Pair<Object, Triple<Integer, Integer, Integer>> in : fluidInput) {
			FluidIngredient ing = GTCEuHelper.INSTANCE.getFluidIngredient(in.getLeft(), in.getRight().getLeft());
			if(ing == null) {
				throw new IllegalArgumentException("Empty ingredient in recipe "+key+": "+in);
			}
			fluidInputs.add(new Content(
					ing,
					in.getRight().getMiddle(), builder.maxChance, in.getRight().getRight(),
					builder.slotName, builder.uiName));
		}
		for(Pair<Object, Triple<Integer, Integer, Integer>> out : itemOutput) {
			ItemStack stack = MiscHelper.INSTANCE.getItemStack(out.getLeft(), out.getRight().getLeft());
			if(stack.isEmpty()) {
				LOGGER.warn("Empty output in recipe {}: {}", key, out);
				continue;
			}
			itemOutputs.add(new Content(
					SizedIngredient.create(stack),
					out.getRight().getMiddle(), builder.maxChance, out.getRight().getRight(),
					builder.slotName, builder.uiName));
		}
		for(Pair<Object, Triple<Integer, Integer, Integer>> out : fluidOutput) {
			var stack = MiscHelper.INSTANCE.getFluidStack(out.getLeft(), out.getRight().getLeft());
			if(stack.isEmpty()) {
				LOGGER.warn("Empty output in recipe {}: {}", key, out);
				continue;
			}
			fluidOutputs.add(new Content(
					FluidStack.create(stack.getFluid(), stack.getAmount(), stack.getTag()),
					out.getRight().getMiddle(), builder.maxChance, out.getRight().getRight(),
					builder.slotName, builder.uiName));
		}

		if(euInput.isPresent()) {
			builder.inputEU(euInput.getAsLong());
		}
		if(euTick.isPresent()) {
			builder.EUt(euTick.getAsLong());
		}
		if(euOutput.isPresent()) {
			builder.outputEU(euOutput.getAsLong());
		}
		builder.input.computeIfAbsent(ItemRecipeCapability.CAP, c->new ArrayList<>()).addAll(itemInputs);
		builder.input.computeIfAbsent(FluidRecipeCapability.CAP, c->new ArrayList<>()).addAll(fluidInputs);
		builder.output.computeIfAbsent(ItemRecipeCapability.CAP, c->new ArrayList<>()).addAll(itemOutputs);
		builder.output.computeIfAbsent(FluidRecipeCapability.CAP, c->new ArrayList<>()).addAll(fluidOutputs);
		if(stressInput.isPresent()) {
			builder.inputStress((float)stressInput.getAsDouble());
		}
		if(stressOutput.isPresent()) {
			builder.outputStress((float)stressOutput.getAsDouble());
		}
		builder.data.merge(data);
		builder.conditions.addAll(conditions);

		MutableObject<FinishedRecipe> ref = new MutableObject<>();
		builder.save(ref::setValue);
		return ref.getValue().serializeRecipe();
	}
}
