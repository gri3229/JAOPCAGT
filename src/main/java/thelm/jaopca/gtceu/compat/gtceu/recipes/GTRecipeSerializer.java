package thelm.jaopca.gtceu.compat.gtceu.recipes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonElement;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.data.recipe.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.IntersectionIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import thelm.jaopca.api.recipes.IRecipeSerializer;
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
			// Because GT currently breaks on difference ingredients, we need to use the resolved item set if it's not guaranteed to work with GT
			Pair<Ingredient, Set<Item>> pair = MiscHelper.INSTANCE.getIngredientResolved(in.getLeft());
			Ingredient ing = pair.getLeft();
			if(ing == null) {
				throw new IllegalArgumentException("Empty ingredient in recipe "+key+": "+in);
			}
			switch(ing.getCustomIngredient()) {
			case DataComponentIngredient c -> {}
			case IntCircuitIngredient c -> {}
			case IntersectionIngredient c -> {}
			case null -> {}
			default -> ing = Ingredient.of(pair.getRight().stream().map(ItemStack::new));
			}
			itemInputs.add(new Content(
					new SizedIngredient(ing, in.getRight().getLeft()),
					in.getRight().getMiddle()/10000F, in.getRight().getRight()/10000F,
					builder.slotName, builder.uiName));
		}
		for(Pair<Object, Triple<Integer, Integer, Integer>> in : fluidInput) {
			FluidIngredient ing = MiscHelper.INSTANCE.getFluidIngredient(in.getLeft());
			if(ing == null) {
				throw new IllegalArgumentException("Empty ingredient in recipe "+key+": "+in);
			}
			fluidInputs.add(new Content(
					new SizedFluidIngredient(ing, in.getRight().getLeft()),
					in.getRight().getMiddle()/10000F, in.getRight().getRight()/10000F,
					builder.slotName, builder.uiName));
		}
		for(Pair<Object, Triple<Integer, Integer, Integer>> out : itemOutput) {
			ItemStack stack = MiscHelper.INSTANCE.getItemStack(out.getLeft(), 1);
			if(stack.isEmpty()) {
				LOGGER.warn("Empty output in recipe {}: {}", key, out);
				continue;
			}
			itemOutputs.add(new Content(
					SizedIngredient.of(stack.getItem(), out.getRight().getLeft()),
					out.getRight().getMiddle()/10000F, out.getRight().getRight()/10000F,
					builder.slotName, builder.uiName));
		}
		for(Pair<Object, Triple<Integer, Integer, Integer>> out : fluidOutput) {
			FluidStack stack = MiscHelper.INSTANCE.getFluidStack(out.getLeft(), out.getRight().getLeft());
			if(stack.isEmpty()) {
				LOGGER.warn("Empty output in recipe {}: {}", key, out);
				continue;
			}
			fluidOutputs.add(new Content(
					SizedFluidIngredient.of(stack),
					out.getRight().getMiddle()/10000F, out.getRight().getRight()/10000F,
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
		GTRecipe recipe = builder.build();
		return MiscHelper.INSTANCE.serializeRecipe(recipe);
	}
}
