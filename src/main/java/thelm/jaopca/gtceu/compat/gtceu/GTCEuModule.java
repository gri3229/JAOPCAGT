package thelm.jaopca.gtceu.compat.gtceu;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix.OreType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import thelm.jaopca.api.JAOPCAApi;
import thelm.jaopca.api.forms.IForm;
import thelm.jaopca.api.forms.IFormRequest;
import thelm.jaopca.api.helpers.IMiscHelper;
import thelm.jaopca.api.ingredients.CompoundIngredientObject;
import thelm.jaopca.api.items.IItemFormType;
import thelm.jaopca.api.items.IItemInfo;
import thelm.jaopca.api.materials.IMaterial;
import thelm.jaopca.api.materials.MaterialType;
import thelm.jaopca.api.modules.IModule;
import thelm.jaopca.api.modules.IModuleData;
import thelm.jaopca.api.modules.JAOPCAModule;
import thelm.jaopca.gtceu.compat.gtceu.recipes.GTRecipeSettings;
import thelm.jaopca.items.ItemFormType;
import thelm.jaopca.utils.ApiImpl;
import thelm.jaopca.utils.MiscHelper;

@JAOPCAModule(modDependencies = "gtceu")
public class GTCEuModule implements IModule {

	static final List<String> ALTS = Arrays.asList("aluminum", "quartz");
	static final Set<String> BLACKLIST = new TreeSet<>(ALTS);

	private final IForm crushedOreForm = ApiImpl.INSTANCE.newForm(this, "gtceu_crushed_ores", ItemFormType.INSTANCE).
			setMaterialTypes(MaterialType.ORE).setSecondaryName("crushed_ores");
	private final IForm purifiedOreForm = ApiImpl.INSTANCE.newForm(this, "gtceu_purified_ores", ItemFormType.INSTANCE).
			setMaterialTypes(MaterialType.ORE).setSecondaryName("purified_ores");
	private final IForm refinedOreForm = ApiImpl.INSTANCE.newForm(this, "gtceu_refined_ores", ItemFormType.INSTANCE).
			setMaterialTypes(MaterialType.ORE).setSecondaryName("refined_ores");
	private final IForm impureDustForm = ApiImpl.INSTANCE.newForm(this, "gtceu_impure_dusts", ItemFormType.INSTANCE).
			setMaterialTypes(MaterialType.ORE).setSecondaryName("impure_dusts");
	private final IForm pureDustForm = ApiImpl.INSTANCE.newForm(this, "gtceu_pure_dusts", ItemFormType.INSTANCE).
			setMaterialTypes(MaterialType.ORE).setSecondaryName("pure_dusts");
	private IFormRequest formRequest;

	@Override
	public String getName() {
		return "gtceu";
	}

	@Override
	public Multimap<Integer, String> getModuleDependencies() {
		ImmutableSetMultimap.Builder<Integer, String> builder = ImmutableSetMultimap.builder();
		builder.put(0, "dusts");
		builder.put(1, "dusts");
		builder.put(1, "tiny_dusts");
		builder.put(2, "dusts");
		builder.put(2, "tiny_dusts");
		builder.put(3, "dusts");
		return builder.build();
	}

	@Override
	public List<IFormRequest> getFormRequests() {
		crushedOreForm.setDefaultMaterialBlacklist(BLACKLIST);
		purifiedOreForm.setDefaultMaterialBlacklist(BLACKLIST);
		refinedOreForm.setDefaultMaterialBlacklist(BLACKLIST);
		impureDustForm.setDefaultMaterialBlacklist(BLACKLIST);
		pureDustForm.setDefaultMaterialBlacklist(BLACKLIST);
		formRequest = ApiImpl.INSTANCE.newFormRequest(this,
				crushedOreForm, purifiedOreForm, refinedOreForm, impureDustForm, pureDustForm).setGrouped(true);
		return List.of(formRequest);
	}

	@Override
	public Set<MaterialType> getMaterialTypes() {
		return EnumSet.copyOf(Arrays.asList(MaterialType.ORE));
	}

	@Override
	public Set<String> getDefaultMaterialBlacklist() {
		return BLACKLIST;
	}

	@Override
	public void onCommonSetup(IModuleData moduleData, FMLCommonSetupEvent event) {
		JAOPCAApi api = ApiImpl.INSTANCE;
		GTCEuHelper helper = GTCEuHelper.INSTANCE;
		IMiscHelper miscHelper = MiscHelper.INSTANCE;
		IItemFormType itemFormType = ItemFormType.INSTANCE;
		ResourceLocation hardHammerLocation = new ResourceLocation("forge:tools/hammers");
		ResourceLocation stoneDustLocation = new ResourceLocation("forge:dusts/stone");
		ResourceLocation endstoneDustLocation = new ResourceLocation("forge:dusts/endstone");
		Function<TagPrefix, String> toGround = prefix->{
			if(prefix.name.equals("endstone")) {
				return "end_stone";
			}
			return FormattingUtil.toLowerCaseUnderscore(prefix.name);
		};
		CompoundIngredientObject allOreLocationsObj = CompoundIngredientObject.union(
				TagPrefix.ORES.entrySet().stream().
				map(entry->new ResourceLocation("forge:ores_in_ground/"+toGround.apply(entry.getKey()))).
				toArray());
		CompoundIngredientObject doubleOreLocationsObj = CompoundIngredientObject.union(
				TagPrefix.ORES.entrySet().stream().
				filter(entry->entry.getValue().isNether()).
				map(entry->new ResourceLocation("forge:ores_in_ground/"+toGround.apply(entry.getKey()))).
				toArray());
		for(IMaterial material : formRequest.getMaterials()) {
			String name = material.getName();
			String extra1 = material.getExtra(1).getName();
			String extra2 = material.getExtra(2).getName();
			String extra3 = material.getExtra(3).getName();

			IItemInfo crushedOreInfo = itemFormType.getMaterialFormInfo(crushedOreForm, material);
			ResourceLocation crushedOreLocation = miscHelper.getTagLocation("crushed_ores", name);
			IItemInfo purifiedOreInfo = itemFormType.getMaterialFormInfo(purifiedOreForm, material);
			ResourceLocation purifiedOreLocation = miscHelper.getTagLocation("purified_ores", name);
			IItemInfo refinedOreInfo = itemFormType.getMaterialFormInfo(refinedOreForm, material);
			ResourceLocation refinedOreLocation = miscHelper.getTagLocation("refined_ores", name);
			IItemInfo impureDustInfo = itemFormType.getMaterialFormInfo(impureDustForm, material);
			ResourceLocation impureDustLocation = miscHelper.getTagLocation("impure_dusts", name);
			IItemInfo pureDustInfo = itemFormType.getMaterialFormInfo(pureDustForm, material);
			ResourceLocation pureDustLocation = miscHelper.getTagLocation("pure_dusts", name);
			ResourceLocation oreLocation = miscHelper.getTagLocation("ores", name);
			ResourceLocation dustLocation = miscHelper.getTagLocation("dusts", name);
			ResourceLocation materialLocation = miscHelper.getTagLocation(material.getType().getFormName(), name);
			ResourceLocation extra1DustLocation = miscHelper.getTagLocation("dusts", extra1);
			ResourceLocation extra1MaterialLocation = miscHelper.getTagLocation(material.getExtra(1).getType().getFormName(), extra1);
			ResourceLocation extra1TinyDustLocation = miscHelper.getTagLocation("tiny_dusts", extra1);
			ResourceLocation extra2DustLocation = miscHelper.getTagLocation("dust", extra2);
			ResourceLocation extra2TinyDustLocation = miscHelper.getTagLocation("tiny_dusts", extra2);
			ResourceLocation extra3DustLocation = miscHelper.getTagLocation("dust", extra3);

			// to_crushed_ore
			if(!material.getType().isCrystalline()) {
				helper.registerGTRecipe(
						new ResourceLocation("jaopca", "gtceu.default_ore_to_crushed_ore_forge_hammer."+name),
						GTRecipeTypes.FORGE_HAMMER_RECIPES,
						helper.recipeSettings().
						itemInput(CompoundIngredientObject.difference(new Object[] {
								oreLocation,
								doubleOreLocationsObj,
						}), 1).
						itemOutput(crushedOreInfo, 1).
						duration(10).EUt(16));
				helper.registerGTRecipe(
						new ResourceLocation("jaopca", "gtceu.double_ore_to_crushed_ore_forge_hammer."+name),
						GTRecipeTypes.FORGE_HAMMER_RECIPES,
						helper.recipeSettings().
						itemInput(CompoundIngredientObject.intersection(new Object[] {
								oreLocation,
								doubleOreLocationsObj,
						}), 1).
						itemOutput(crushedOreInfo, 2).
						duration(10).EUt(16));
			}
			{
				ResourceLocation extra1Location = material.getExtra(1).getType().isCrystalline() ? extra1MaterialLocation : extra1DustLocation;
				helper.registerGTRecipe(
						new ResourceLocation("jaopca", "gtceu.default_ore_to_crushed_ore_forge_hammer."+name),
						GTRecipeTypes.MACERATOR_RECIPES,
						helper.recipeSettings().
						itemInput(CompoundIngredientObject.difference(new Object[] {
								oreLocation,
								allOreLocationsObj,
						}), 1).
						itemOutput(crushedOreInfo, 2).
						itemOutput(extra1Location, 1400, 850).
						duration(400).EUt(2));
				for(Map.Entry<TagPrefix, OreType> entry : TagPrefix.ORES.entrySet()){
					String ground = toGround.apply(entry.getKey());
					int multiplier = entry.getValue().isNether() ? 2 : 1;
					GTRecipeSettings settings = helper.recipeSettings().
							itemInput(CompoundIngredientObject.intersection(new Object[] {
									oreLocation,
									new ResourceLocation("forge:ores_in_ground/"+ground),
							}), 1).
							itemOutput(crushedOreInfo, 2*multiplier).
							itemOutput(extra1Location, 1400, 850).
							duration(400).EUt(2);
					if(GTRegistries.MATERIALS.containKey(ground)) {
						settings.itemOutput(new ResourceLocation("forge:dusts/"+ground));
					}
					else if(ground.equals("end_stone")) {
						settings.itemOutput(endstoneDustLocation);
					}
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu."+ground+"_ore_to_crushed_ore_forge_hammer."+name),
							GTRecipeTypes.MACERATOR_RECIPES, settings);
				}
			}
			if(material.getType() == MaterialType.INGOT) {
				ResourceLocation rawMaterialLocation = miscHelper.getTagLocation("raw_materials", material.getName());
				helper.registerGTRecipe(
						new ResourceLocation("jaopca", "gtceu.raw_material_to_crushed_ore_forge_hammer."+name),
						GTRecipeTypes.FORGE_HAMMER_RECIPES,
						helper.recipeSettings().
						itemInput(rawMaterialLocation, 1).
						itemOutput(crushedOreInfo, 1).
						duration(10).EUt(16));
				helper.registerGTRecipe(
						new ResourceLocation("jaopca", "gtceu.raw_material_to_crushed_ore_macerator."+name),
						GTRecipeTypes.MACERATOR_RECIPES,
						helper.recipeSettings().
						itemInput(rawMaterialLocation, 1).
						itemOutput(crushedOreInfo, 1).
						itemOutput(crushedOreInfo, 1, 5000, 750).
						itemOutput(crushedOreInfo, 1, 2500, 500).
						itemOutput(crushedOreInfo, 1, 1250, 250).
						duration(400).EUt(2));
			}
			// to_purified_ore
			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.crushed_ore_to_purified_ore_water_100."+name),
					GTRecipeTypes.ORE_WASHER_RECIPES,
					helper.recipeSettings().
					itemInput(crushedOreLocation, 1).
					circuitMeta(2).
					fluidInput(GTMaterials.Water.getFluid(), 100).
					itemOutput(purifiedOreInfo, 1).
					duration(8).EUt(4));
			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.crushed_ore_to_purified_ore_water_1000."+name),
					GTRecipeTypes.ORE_WASHER_RECIPES,
					helper.recipeSettings().
					itemInput(crushedOreLocation, 1).
					circuitMeta(1).
					fluidInput(GTMaterials.Water.getFluid(), 1000).
					itemOutput(purifiedOreInfo, 1).
					itemOutput(extra1TinyDustLocation, 3).
					itemOutput(stoneDustLocation, 1));
			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.crushed_ore_to_purified_ore_distilled_water."+name),
					GTRecipeTypes.ORE_WASHER_RECIPES,
					helper.recipeSettings().
					itemInput(crushedOreLocation, 1).
					circuitMeta(1).
					fluidInput(GTMaterials.DistilledWater.getFluid(), 1000).
					itemOutput(purifiedOreInfo, 1).
					itemOutput(extra1TinyDustLocation, 3).
					itemOutput(stoneDustLocation, 1).
					duration(200));
			// to_refined_ore
			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.crushed_ore_to_refined_ore."+name),
					GTRecipeTypes.THERMAL_CENTRIFUGE_RECIPES,
					helper.recipeSettings().
					itemInput(crushedOreLocation, 1).
					itemOutput(refinedOreInfo, 1).
					itemOutput(extra2TinyDustLocation, 3).
					itemOutput(stoneDustLocation, 1));
			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.purified_ore_to_refined_ore."+name),
					GTRecipeTypes.THERMAL_CENTRIFUGE_RECIPES,
					helper.recipeSettings().
					itemInput(purifiedOreLocation, 1).
					itemOutput(refinedOreInfo, 1).
					itemOutput(extra2TinyDustLocation, 3));
			// to_impure_dust
			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.crushed_ore_to_impure_dust_forge_hammer."+name),
					GTRecipeTypes.FORGE_HAMMER_RECIPES,
					helper.recipeSettings().
					itemInput(crushedOreLocation, 1).
					itemOutput(impureDustInfo, 1).
					duration(10).EUt(16));
			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.crushed_ore_to_impure_dust_macerator."+name),
					GTRecipeTypes.MACERATOR_RECIPES,
					helper.recipeSettings().
					itemInput(crushedOreLocation, 1).
					itemOutput(impureDustInfo, 1).
					itemOutput(extra1DustLocation, 1, 1400, 850).
					duration(400).EUt(2));
			api.registerShapelessRecipe(
					new ResourceLocation("jaopca", "gtceu.crushed_ore_to_impure_dust_hard_hammer."+name),
					impureDustInfo, 1, new Object[] {
							hardHammerLocation, crushedOreLocation,
					});
			// to_pure_dust
			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.purified_ore_to_pure_dust_forge_hammer."+name),
					GTRecipeTypes.FORGE_HAMMER_RECIPES,
					helper.recipeSettings().
					itemInput(purifiedOreLocation, 1).
					itemOutput(pureDustInfo, 1).
					duration(10).EUt(16));
			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.purified_ore_to_pure_dust_macerator."+name),
					GTRecipeTypes.MACERATOR_RECIPES,
					helper.recipeSettings().
					itemInput(purifiedOreLocation, 1).
					itemOutput(pureDustInfo, 1).
					itemOutput(extra2DustLocation, 1, 1400, 850).
					duration(400).EUt(2));
			api.registerShapelessRecipe(
					new ResourceLocation("jaopca", "gtceu.purified_ore_to_pure_dust_hard_hammer."+name),
					pureDustInfo, 1, new Object[] {
							hardHammerLocation, purifiedOreLocation,
					});
			// to_dust
			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.refined_ore_to_dust_forge_hammer."+name),
					GTRecipeTypes.FORGE_HAMMER_RECIPES,
					helper.recipeSettings().
					itemInput(refinedOreLocation, 1).
					itemOutput(dustLocation, 1).
					duration(10).EUt(16));
			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.refined_ore_to_dust_macerator."+name),
					GTRecipeTypes.MACERATOR_RECIPES,
					helper.recipeSettings().
					itemInput(refinedOreLocation, 1).
					itemOutput(dustLocation, 1).
					itemOutput(extra3DustLocation, 1, 1400, 850).
					duration(400).EUt(2));
			api.registerShapelessRecipe(
					new ResourceLocation("jaopca", "gtceu.refined_ore_to_dust_hard_hammer."+name),
					dustLocation, 1, new Object[] {
							hardHammerLocation, refinedOreLocation,
					});
			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.impure_dust_to_dust_centrifuge."+name),
					GTRecipeTypes.CENTRIFUGE_RECIPES,
					helper.recipeSettings().
					itemInput(impureDustLocation, 1).
					itemOutput(dustLocation, 1).
					itemOutput(extra1TinyDustLocation, 1).
					duration(400).EUt(24));
			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.impure_dust_to_dust_ore_washer."+name),
					GTRecipeTypes.ORE_WASHER_RECIPES,
					helper.recipeSettings().
					itemInput(impureDustLocation, 1).
					circuitMeta(2).
					fluidInput(GTMaterials.Water.getFluid(), 100).
					itemOutput(dustLocation, 1).
					duration(8).EUt(4));
			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.pure_dust_to_dust_centrifuge."+name),
					GTRecipeTypes.CENTRIFUGE_RECIPES,
					helper.recipeSettings().
					itemInput(pureDustLocation, 1).
					itemOutput(dustLocation, 1).
					itemOutput(extra2TinyDustLocation, 1).
					duration(100).EUt(5));
			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.pure_dust_to_dust_ore_washer."+name),
					GTRecipeTypes.ORE_WASHER_RECIPES,
					helper.recipeSettings().
					itemInput(pureDustLocation, 1).
					circuitMeta(2).
					fluidInput(GTMaterials.Water.getFluid(), 100).
					itemOutput(dustLocation, 1).
					duration(8).EUt(4));
			// to_material
			if(material.getType().isIngot()) {
				api.registerSmeltingRecipe(
						new ResourceLocation("jaopca", "gtceu.crushed_ore_to_material_smelting."+name),
						crushedOreLocation, materialLocation, 1, 0.5F, 200);
				api.registerSmeltingRecipe(
						new ResourceLocation("jaopca", "gtceu.purified_ore_to_material_smelting."+name),
						purifiedOreLocation, materialLocation, 1, 0.5F, 200);
				api.registerSmeltingRecipe(
						new ResourceLocation("jaopca", "gtceu.refined_ore_to_material_smelting."+name),
						refinedOreLocation, materialLocation, 1, 0.5F, 200);
				api.registerSmeltingRecipe(
						new ResourceLocation("jaopca", "gtceu.impure_dust_to_material_smelting."+name),
						impureDustLocation, materialLocation, 1, 0.5F, 200);
				api.registerSmeltingRecipe(
						new ResourceLocation("jaopca", "gtceu.pure_dust_to_material_smelting."+name),
						pureDustLocation, materialLocation, 1, 0.5F, 200);
			}
			if(material.getType().isCrystalline()) {
				helper.registerGTRecipe(
						new ResourceLocation("jaopca", "gtceu.default_ore_to_material."+name),
						GTRecipeTypes.FORGE_HAMMER_RECIPES,
						helper.recipeSettings().
						itemInput(CompoundIngredientObject.difference(new Object[] {
								oreLocation,
								doubleOreLocationsObj,
						})).
						itemOutput(materialLocation, 1).
						duration(10).EUt(16));
				helper.registerGTRecipe(
						new ResourceLocation("jaopca", "gtceu.double_ore_to_material."+name),
						GTRecipeTypes.FORGE_HAMMER_RECIPES,
						helper.recipeSettings().
						itemInput(CompoundIngredientObject.intersection(new Object[] {
								oreLocation,
								doubleOreLocationsObj,
						})).
						itemOutput(materialLocation, 2).
						duration(10).EUt(16));
			}
			// cauldron
			CauldronInteraction toPurifiedOre = (state, level, pos, player, hand, stack)->{
				if(!level.isClientSide) {
					if(!state.hasProperty(LayeredCauldronBlock.LEVEL) || state.getValue(LayeredCauldronBlock.LEVEL) == 0) {
						return InteractionResult.PASS;
					}
					player.setItemInHand(hand, MiscHelper.INSTANCE.getItemStack(purifiedOreInfo, stack.getCount()));
					player.awardStat(Stats.USE_CAULDRON);
					player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
					LayeredCauldronBlock.lowerFillLevel(state, level, pos);
				}
				return InteractionResult.sidedSuccess(level.isClientSide);
			};
			CauldronInteraction toDust = (state, level, pos, player, hand, stack)->{
				if(!level.isClientSide) {
					if(!state.hasProperty(LayeredCauldronBlock.LEVEL) || state.getValue(LayeredCauldronBlock.LEVEL) == 0) {
						return InteractionResult.PASS;
					}
					player.setItemInHand(hand, MiscHelper.INSTANCE.getItemStack(dustLocation, stack.getCount()));
					player.awardStat(Stats.USE_CAULDRON);
					player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
					LayeredCauldronBlock.lowerFillLevel(state, level, pos);
				}
				return InteractionResult.sidedSuccess(level.isClientSide);
			};
			CauldronInteraction.WATER.put(crushedOreInfo.asItem(), toPurifiedOre);
			CauldronInteraction.WATER.put(impureDustInfo.asItem(), toDust);
			CauldronInteraction.WATER.put(pureDustInfo.asItem(), toDust);
		}
	}
}
