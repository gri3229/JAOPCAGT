package thelm.jaopca.gt5.compat.gregtech;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import gregtech.GT_Mod;
import gregtech.api.enums.Materials;
import gregtech.api.util.GT_Recipe;
import net.minecraftforge.fluids.FluidRegistry;
import thelm.jaopca.api.JAOPCAApi;
import thelm.jaopca.api.config.IDynamicSpecConfig;
import thelm.jaopca.api.forms.IForm;
import thelm.jaopca.api.forms.IFormRequest;
import thelm.jaopca.api.helpers.IMiscHelper;
import thelm.jaopca.api.items.IItemFormType;
import thelm.jaopca.api.items.IItemInfo;
import thelm.jaopca.api.materials.IMaterial;
import thelm.jaopca.api.materials.MaterialType;
import thelm.jaopca.api.modules.IModule;
import thelm.jaopca.api.modules.IModuleData;
import thelm.jaopca.api.modules.JAOPCAModule;
import thelm.jaopca.compat.magneticraft.MagneticraftHelper;
import thelm.jaopca.compat.railcraft.RailcraftHelper;
import thelm.jaopca.config.ConfigHandler;
import thelm.jaopca.gt5.compat.gregtech.items.JAOPCAWashableItem;
import thelm.jaopca.gt5.compat.immersiveengineering.ImmersiveEngineeringHelper;
import thelm.jaopca.items.ItemFormType;
import thelm.jaopca.utils.ApiImpl;
import thelm.jaopca.utils.MiscHelper;

@JAOPCAModule(modDependencies = "gregtech")
public class GregTechModule implements IModule {

	static final List<String> ALTS = Arrays.asList("Aluminum");
	static final Set<String> BLACKLIST = new TreeSet<>(ALTS);

	static {
		Arrays.stream(Materials.values()).forEach(m->BLACKLIST.add(m.mName));
	}

	private static Set<String> configClumpBlacklist = new TreeSet<>();
	private static Set<String> configShardBlacklist = new TreeSet<>();
	private static Set<String> configDirtyGravelBlacklist = new TreeSet<>();
	private static Set<String> configCleanGravelBlacklist = new TreeSet<>();
	private static Set<String> configReducedBlacklist = new TreeSet<>();
	private static Set<String> configCrystalBlacklist = new TreeSet<>();
	private static Set<String> configCrystallineBlacklist = new TreeSet<>();

	private static boolean mc = true;
	private static boolean ie = true;
	private static boolean rc = true;

	public GregTechModule() {
		Stream.of("jaopca:ic2.ore_to_crushed.*", "jaopca:ic2.crushed_to_purified_crushed.*",
				"jaopca:ic2.crushed_to_material.*", "jaopca:ic2.purified_crushed_to_material.*",
				"jaopca:ic2.crushed_to_dust_macerator.*", "jaopca:ic2.purified_crushed_to_dust_macerator.*",
				"jaopca:ic2.crushed_to_dust_centrifuge.*", "jaopca:ic2.purified_crushed_to_dust_centrifuge.*").
		map(Pattern::compile).forEach(ConfigHandler.RECIPE_REGEX_BLACKLIST::add);
	}

	private final IForm crushedForm = ApiImpl.INSTANCE.newForm(this, "gregtech_crushed", ItemFormType.INSTANCE).
			setMaterialTypes(MaterialType.ORE).setSecondaryName("crushed").setDefaultMaterialBlacklist(BLACKLIST).
			setSettings(ItemFormType.INSTANCE.getNewSettings().
					setItemCreator((f, m, s)->new JAOPCAWashableItem(f, m, s, "crushedPurified")));
	private final IForm purifiedCrushedForm = ApiImpl.INSTANCE.newForm(this, "gregtech_purified_crushed", ItemFormType.INSTANCE).
			setMaterialTypes(MaterialType.ORE).setSecondaryName("crushedPurified").setDefaultMaterialBlacklist(BLACKLIST);
	private final IForm centrifugedCrushedForm = ApiImpl.INSTANCE.newForm(this, "gregtech_centrifuged_crushed", ItemFormType.INSTANCE).
			setMaterialTypes(MaterialType.ORE).setSecondaryName("crushedCentrifuged").setDefaultMaterialBlacklist(BLACKLIST);
	private final IForm impureDustForm = ApiImpl.INSTANCE.newForm(this, "gregtech_impure_dust", ItemFormType.INSTANCE).
			setMaterialTypes(MaterialType.ORE).setSecondaryName("dustImpure").setDefaultMaterialBlacklist(BLACKLIST).
			setSettings(ItemFormType.INSTANCE.getNewSettings().
					setItemCreator((f, m, s)->new JAOPCAWashableItem(f, m, s, "dust")));
	private final IForm pureDustForm = ApiImpl.INSTANCE.newForm(this, "gregtech_pure_dust", ItemFormType.INSTANCE).
			setMaterialTypes(MaterialType.ORE).setSecondaryName("dustPure").setDefaultMaterialBlacklist(BLACKLIST).
			setSettings(ItemFormType.INSTANCE.getNewSettings().
					setItemCreator((f, m, s)->new JAOPCAWashableItem(f, m, s, "dust")));
	private final IFormRequest formRequest = ApiImpl.INSTANCE.newFormRequest(this,
			crushedForm, purifiedCrushedForm, centrifugedCrushedForm, impureDustForm, pureDustForm).setGrouped(true);

	@Override
	public String getName() {
		return "gregtech";
	}

	@Override
	public Multimap<Integer, String> getModuleDependencies() {
		ImmutableSetMultimap.Builder<Integer, String> builder = ImmutableSetMultimap.builder();
		builder.put(0, "dust");
		builder.put(0, "nugget");
		builder.put(1, "dust");
		builder.put(1, "tiny_dust");
		builder.put(2, "dust");
		builder.put(2, "tiny_dust");
		builder.put(3, "dust");
		return builder.build();
	}

	@Override
	public List<IFormRequest> getFormRequests() {
		return Collections.singletonList(formRequest);
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
	public void defineModuleConfig(IModuleData moduleData, IDynamicSpecConfig config) {
		IMiscHelper helper = MiscHelper.INSTANCE;
		mc = config.getDefinedBoolean("recipes.mc", mc, "Should the module add Magneticraft recipes.");
		ie = config.getDefinedBoolean("recipes.ie", ie, "Should the module add Immersive Engineering recipes.");
		rc = config.getDefinedBoolean("recipes.rc", rc, "Should the module add Railcraft recipes.");
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.clumpMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configClumpBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.shardMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configShardBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.dirtyGravelMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configDirtyGravelBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.cleanGravelMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configCleanGravelBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.reducedMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configReducedBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.crystalMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configCrystalBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.crystallineMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configCrystallineBlacklist);
	}

	@Override
	public void onInit(IModuleData moduleData, FMLInitializationEvent event) {
		JAOPCAApi api = ApiImpl.INSTANCE;
		GregTechHelper helper = GregTechHelper.INSTANCE;
		MagneticraftHelper mcHelper = MagneticraftHelper.INSTANCE;
		ImmersiveEngineeringHelper ieHelper = ImmersiveEngineeringHelper.INSTANCE;
		RailcraftHelper rcHelper = RailcraftHelper.INSTANCE;
		IMiscHelper miscHelper = MiscHelper.INSTANCE;
		IItemFormType itemFormType = ItemFormType.INSTANCE;
		Set<String> oredict = api.getOredict();
		float mcMul = (float)GT_Mod.gregtechproxy.mMagneticraftBonusOutputPercent/100;
		for(IMaterial material : formRequest.getMaterials()) {
			String name = material.getName();
			String extra1 = material.getExtra(1).getName();
			String extra2 = material.getExtra(2).getName();
			String extra3 = material.getExtra(3).getName();

			IItemInfo crushedInfo = itemFormType.getMaterialFormInfo(crushedForm, material);
			String crushedOredict = miscHelper.getOredictName("crushed", name);
			IItemInfo purifiedCrushedInfo = itemFormType.getMaterialFormInfo(purifiedCrushedForm, material);
			String purifiedCrushedOredict = miscHelper.getOredictName("crushedPurified", name);
			IItemInfo centrifugedCrushedInfo = itemFormType.getMaterialFormInfo(centrifugedCrushedForm, material);
			String centrifugedCrushedOredict = miscHelper.getOredictName("crushedCentrifuged", name);
			IItemInfo impureDustInfo = itemFormType.getMaterialFormInfo(impureDustForm, material);
			String impureDustOredict = miscHelper.getOredictName("dustImpure", name);
			IItemInfo pureDustInfo = itemFormType.getMaterialFormInfo(pureDustForm, material);
			String pureDustOredict = miscHelper.getOredictName("dustPure", name);
			String oreOredict = miscHelper.getOredictName("ore", name);
			String clumpOredict = miscHelper.getOredictName("clump", name);
			String shardOredict = miscHelper.getOredictName("shard", name);
			String dirtyGravelOredict = miscHelper.getOredictName("dirtyGravel", name);
			String cleanGravelOredict = miscHelper.getOredictName("cleanGravel", name);
			String reducedOredict = miscHelper.getOredictName("reduced", name);
			String crystalOredict = miscHelper.getOredictName("crystal", name);
			String crystallineOredict = miscHelper.getOredictName("crystalline", name);
			String dustOredict = miscHelper.getOredictName("dust", name);
			String nuggetOredict = miscHelper.getOredictName("nugget", name);
			String materialOredict = miscHelper.getOredictName(material.getType().getFormName(), name);
			String extra1DustOredict = miscHelper.getOredictName("dust", extra1);
			String extra1MaterialOredict = miscHelper.getOredictName(material.getExtra(1).getType().getFormName(), extra1);
			String extra1TinyDustOredict = miscHelper.getOredictName("dustTiny", extra1);
			String extra2DustOredict = miscHelper.getOredictName("dust", extra2);
			String extra2TinyDustOredict = miscHelper.getOredictName("dustTiny", extra2);
			String extra3DustOredict = miscHelper.getOredictName("dust", extra3);
			// to_crushed
			if(!material.getType().isCrystalline()) {
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.ore_to_crushed_hammer", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sHammerRecipes).
						input(oreOredict, 1).
						output(crushedInfo, material.getType().isDust() ? 2 : 1).
						time(10).energy(16));
			}
			{
				String extra1Oredict = material.getExtra(1).getType().isCrystalline() ? extra1MaterialOredict : extra1DustOredict;
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.ore_to_crushed_macerator", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sMaceratorRecipes).
						input(oreOredict, 1).
						output(crushedInfo, material.getType().isDust() ? 4 : 2).
						output(extra1Oredict, 1, 1000).
						output("dustStone", 1, 5000).
						time(400).energy(2));
			}
			if(Loader.isModLoaded("Railcraft") && rc) {
				rcHelper.registerRockCrusherRecipe(
						miscHelper.getRecipeKey("gregtech.ore_to_crushed_rock_crusher", name),
						oreOredict, 1, new Object[] {
								crushedInfo, (material.getType().isDust() ? 4 : 2), 1F,
								extra1DustOredict, 1, 0.1F,
								"dustStone", 1, 0.5F,
						});
			}
			// to_purified_crushed
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.crushed_to_purified_crushed", name),
					helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sOreWasherRecipes).
					input(crushedOredict, 1).
					output(purifiedCrushedInfo, 1).
					output(extra1TinyDustOredict, 1).
					output("dustStone", 1).
					time(500).energy(16));
			// to_centrifuged_crushed
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.crushed_to_centrifuged_crushed", name),
					helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sThermalCentrifugeRecipes).
					input(crushedOredict, 1).
					output(centrifugedCrushedInfo, 1).
					output(extra2TinyDustOredict, 1).
					output("dustStone", 1).
					time(500).energy(48));
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.purified_crushed_to_centrifuged_crushed", name),
					helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sThermalCentrifugeRecipes).
					input(purifiedCrushedOredict, 1).
					output(centrifugedCrushedInfo, 1).
					output(extra2TinyDustOredict, 1).
					time(500).energy(48));
			// to_impure_dust
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.crushed_to_impure_dust_hammer", name),
					helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sHammerRecipes).
					input(crushedOredict, 1).
					output(impureDustInfo, 1).
					time(10).energy(16));
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.crushed_to_impure_dust_macerator", name),
					helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sMaceratorRecipes).
					input(crushedOredict, 1).
					output(impureDustInfo, 1).
					output(extra1DustOredict, 1, 1000).
					time(400).energy(2));
			if(Loader.isModLoaded("Magneticraft") && mc) {
				mcHelper.registerGrinderRecipe(
						miscHelper.getRecipeKey("gregtech.crushed_to_impure_dust_grinder", name),
						crushedOredict, impureDustInfo, 1, extra1DustOredict, 1, 0.1F*mcMul);
			}
			if(Loader.isModLoaded("ImmersiveEngineering") && ie) {
				ieHelper.registerCrusherRecipe(
						miscHelper.getRecipeKey("gregtech.crushed_to_impure_dust_crusher", name),
						crushedOredict, new Object[] {
								impureDustInfo, 1,
								extra1DustOredict, 1, 0.15F,
						}, 6000);
			}
			if(Loader.isModLoaded("Railcraft") && rc) {
				rcHelper.registerRockCrusherRecipe(
						miscHelper.getRecipeKey("gregtech.crushed_to_impure_dust_rock_crusher", name),
						crushedOredict, 1, new Object[] {
								impureDustInfo, 1, 1F,
								extra1DustOredict, 1, 0.1F,
						});
			}
			api.registerShapedRecipe(
					miscHelper.getRecipeKey("gregtech.crushed_to_impure_dust_hard_hammer", name),
					impureDustInfo, 1, new Object[] {
							"h", "X",
							'X', crushedOredict,
							'h', "craftingToolHardHammer",
					});
			if(!configClumpBlacklist.contains(name) && oredict.contains(clumpOredict)) {
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.clump_to_impure_dust_hammer", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sHammerRecipes).
						input(clumpOredict, 1).
						output(impureDustInfo, 1).
						time(10).energy(16));
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.clump_to_impure_dust_macerator", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sMaceratorRecipes).
						input(clumpOredict, 1).
						output(impureDustInfo, 1).
						output(extra1DustOredict, 1, 1000).
						time(400).energy(2));
				if(Loader.isModLoaded("ImmersiveEngineering") && ie) {
					ieHelper.registerCrusherRecipe(
							miscHelper.getRecipeKey("gregtech.clump_to_impure_dust_crusher", name),
							clumpOredict, new Object[] {
									impureDustInfo, 1,
									extra1DustOredict, 1, 0.15F,
							}, 6000);
				}
				if(Loader.isModLoaded("Railcraft") && rc) {
					rcHelper.registerRockCrusherRecipe(
							miscHelper.getRecipeKey("gregtech.clump_to_impure_dust_rock_crusher", name),
							clumpOredict, 1, new Object[] {
									impureDustInfo, 1, 1F,
									extra1DustOredict, 1, 0.1F,
							});
				}
				api.registerShapedRecipe(
						miscHelper.getRecipeKey("gregtech.clump_to_impure_dust_hard_hammer", name),
						impureDustInfo, 1, new Object[] {
								"h", "X",
								'X', clumpOredict,
								'h', "craftingToolHardHammer",
						});
			}
			if(!configShardBlacklist.contains(name) && oredict.contains(shardOredict)) {
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.shard_to_impure_dust_hammer", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sHammerRecipes).
						input(shardOredict, 1).
						output(impureDustInfo, 1).
						time(10).energy(16));
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.shard_to_impure_dust_macerator", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sMaceratorRecipes).
						input(shardOredict, 1).
						output(impureDustInfo, 1).
						output(extra1DustOredict, 1, 1000).
						time(400).energy(2));
				if(Loader.isModLoaded("ImmersiveEngineering") && ie) {
					ieHelper.registerCrusherRecipe(
							miscHelper.getRecipeKey("gregtech.shard_to_impure_dust_crusher", name),
							shardOredict, new Object[] {
									impureDustInfo, 1,
									extra1DustOredict, 1, 0.15F,
							}, 6000);
				}
				if(Loader.isModLoaded("Railcraft") && rc) {
					rcHelper.registerRockCrusherRecipe(
							miscHelper.getRecipeKey("gregtech.shard_to_impure_dust_rock_crusher", name),
							shardOredict, 1, new Object[] {
									impureDustInfo, 1, 1F,
									extra1DustOredict, 1, 0.1F,
							});
				}
				api.registerShapedRecipe(
						miscHelper.getRecipeKey("gregtech.shard_to_impure_dust_hard_hammer", name),
						impureDustInfo, 1, new Object[] {
								"h", "X",
								'X', shardOredict,
								'h', "craftingToolHardHammer",
						});
			}
			if(!configDirtyGravelBlacklist.contains(name) && oredict.contains(dirtyGravelOredict)) {
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.dirty_gravel_to_impure_dust_hammer", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sHammerRecipes).
						input(dirtyGravelOredict, 1).
						output(impureDustInfo, 1).
						time(10).energy(16));
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.dirty_gravel_to_impure_dust_macerator", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sMaceratorRecipes).
						input(dirtyGravelOredict, 1).
						output(impureDustInfo, 1).
						output(extra1DustOredict, 1, 1000).
						time(400).energy(2));
				if(Loader.isModLoaded("ImmersiveEngineering") && ie) {
					ieHelper.registerCrusherRecipe(
							miscHelper.getRecipeKey("gregtech.dirty_gravel_to_impure_dust_crusher", name),
							dirtyGravelOredict, new Object[] {
									impureDustInfo, 1,
									extra1DustOredict, 1, 0.15F,
							}, 6000);
				}
				if(Loader.isModLoaded("Railcraft") && rc) {
					rcHelper.registerRockCrusherRecipe(
							miscHelper.getRecipeKey("gregtech.dirty_gravel_to_impure_dust_rock_crusher", name),
							dirtyGravelOredict, 1, new Object[] {
									impureDustInfo, 1, 1F,
									extra1DustOredict, 1, 0.1F,
							});
				}
				api.registerShapedRecipe(
						miscHelper.getRecipeKey("gregtech.dirty_gravel_to_impure_dust_hard_hammer", name),
						impureDustInfo, 1, new Object[] {
								"h", "X",
								'X', dirtyGravelOredict,
								'h', "craftingToolHardHammer",
						});
			}
			// to_pure_dust
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.purified_crushed_to_pure_dust_hammer", name),
					helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sHammerRecipes).
					input(purifiedCrushedOredict, 1).
					output(pureDustInfo, 1).
					time(10).energy(16));
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.purified_crushed_to_pure_dust_macerator", name),
					helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sMaceratorRecipes).
					input(purifiedCrushedOredict, 1).
					output(pureDustInfo, 1).
					output(extra2DustOredict, 1, 1000).
					time(400).energy(2));
			if(Loader.isModLoaded("Magneticraft") && mc) {
				mcHelper.registerGrinderRecipe(
						miscHelper.getRecipeKey("gregtech.purified_crushed_to_pure_dust_grinder", name),
						purifiedCrushedOredict, pureDustInfo, 1, extra2DustOredict, 1, 0.1F*mcMul);
			}
			if(Loader.isModLoaded("ImmersiveEngineering") && ie) {
				ieHelper.registerCrusherRecipe(
						miscHelper.getRecipeKey("gregtech.purified_crushed_to_pure_dust_crusher", name),
						purifiedCrushedOredict, new Object[] {
								pureDustInfo, 1,
								extra2DustOredict, 1, 0.15F,
						}, 6000);
			}
			if(Loader.isModLoaded("Railcraft") && rc) {
				rcHelper.registerRockCrusherRecipe(
						miscHelper.getRecipeKey("gregtech.purified_crushed_to_pure_dust_rock_crusher", name),
						purifiedCrushedOredict, 1, new Object[] {
								pureDustInfo, 1, 1F,
								extra2DustOredict, 1, 0.1F,
						});
			}
			api.registerShapedRecipe(
					miscHelper.getRecipeKey("gregtech.purified_crushed_to_pure_dust_hard_hammer", name),
					pureDustInfo, 1, new Object[] {
							"h", "X",
							'X', purifiedCrushedOredict,
							'h', "craftingToolHardHammer",
					});
			if(!configClumpBlacklist.contains(name) && oredict.contains(clumpOredict)) {
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.clump_to_pure_dust", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sOreWasherRecipes).
						input(clumpOredict, 1).
						output(pureDustInfo, 1).
						output(extra1TinyDustOredict, 1).
						output("dustStone", 1).
						time(500).energy(16));
			}
			if(!configShardBlacklist.contains(name) && oredict.contains(shardOredict)) {
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.shard_to_pure_dust", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sOreWasherRecipes).
						input(shardOredict, 1).
						output(pureDustInfo, 1).
						output(extra1TinyDustOredict, 1).
						output("dustStone", 1).
						time(500).energy(16));
			}
			if(!configDirtyGravelBlacklist.contains(name) && oredict.contains(dirtyGravelOredict)) {
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.dirty_gravel_to_pure_dust", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sOreWasherRecipes).
						input(dirtyGravelOredict, 1).
						output(pureDustInfo, 1).
						output(extra1TinyDustOredict, 1).
						output("dustStone", 1).
						time(500).energy(16));
			}
			if(!configCleanGravelBlacklist.contains(name) && oredict.contains(cleanGravelOredict)) {
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.clean_gravel_to_pure_dust_hammer", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sHammerRecipes).
						input(cleanGravelOredict, 1).
						output(pureDustInfo, 1).
						time(10).energy(16));
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.clean_gravel_to_pure_dust_macerator", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sMaceratorRecipes).
						input(cleanGravelOredict, 1).
						output(pureDustInfo, 1).
						output(extra2DustOredict, 1, 1000).
						time(400).energy(2));
				if(Loader.isModLoaded("ImmersiveEngineering") && ie) {
					ieHelper.registerCrusherRecipe(
							miscHelper.getRecipeKey("gregtech.clean_gravel_to_pure_dust_crusher", name),
							cleanGravelOredict, new Object[] {
									pureDustInfo, 1,
									extra2DustOredict, 1, 0.15F,
							}, 6000);
				}
				if(Loader.isModLoaded("Railcraft") && rc) {
					rcHelper.registerRockCrusherRecipe(
							miscHelper.getRecipeKey("gregtech.clean_gravel_to_pure_dust_rock_crusher", name),
							cleanGravelOredict, 1, new Object[] {
									pureDustInfo, 1, 1F,
									extra2DustOredict, 1, 0.1F,
							});
				}
				api.registerShapedRecipe(
						miscHelper.getRecipeKey("gregtech.clean_gravel_to_pure_dust_hard_hammer", name),
						pureDustInfo, 1, new Object[] {
								"h", "X",
								'X', cleanGravelOredict,
								'h', "craftingToolHardHammer",
						});
			}
			if(!configReducedBlacklist.contains(name) && oredict.contains(reducedOredict)) {
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.reduced_to_pure_dust_hammer", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sHammerRecipes).
						input(reducedOredict, 1).
						output(pureDustInfo, 1).
						time(10).energy(16));
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.reduced_to_pure_dust_macerator", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sMaceratorRecipes).
						input(reducedOredict, 1).
						output(pureDustInfo, 1).
						output(extra2DustOredict, 1, 1000).
						time(400).energy(2));
				if(Loader.isModLoaded("ImmersiveEngineering") && ie) {
					ieHelper.registerCrusherRecipe(
							miscHelper.getRecipeKey("gregtech.reduced_to_pure_dust_crusher", name),
							reducedOredict, new Object[] {
									pureDustInfo, 1,
									extra2DustOredict, 1, 0.15F,
							}, 6000);
				}
				if(Loader.isModLoaded("Railcraft") && rc) {
					rcHelper.registerRockCrusherRecipe(
							miscHelper.getRecipeKey("gregtech.reduced_to_pure_dust_rock_crusher", name),
							reducedOredict, 1, new Object[] {
									pureDustInfo, 1, 1F,
									extra2DustOredict, 1, 0.1F,
							});
				}
				api.registerShapedRecipe(
						miscHelper.getRecipeKey("gregtech.reduced_to_pure_dust_hard_hammer", name),
						pureDustInfo, 1, new Object[] {
								"h", "X",
								'X', reducedOredict,
								'h', "craftingToolHardHammer",
						});
			}
			// to_dust
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.centrifuge_crushed_to_dust_hammer", name),
					helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sHammerRecipes).
					input(centrifugedCrushedOredict, 1).
					output(dustOredict, 1).
					time(10).energy(16));
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.centrifuge_crushed_to_dust_macerator", name),
					helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sMaceratorRecipes).
					input(centrifugedCrushedOredict, 1).
					output(dustOredict, 1).
					output(extra3DustOredict, 1, 1000).
					time(400).energy(2));
			if(Loader.isModLoaded("Magneticraft") && mc) {
				mcHelper.registerGrinderRecipe(
						miscHelper.getRecipeKey("gregtech.centrifuge_crushed_to_dust_grinder", name),
						centrifugedCrushedOredict, dustOredict, 1, extra3DustOredict, 1, 0.1F*mcMul);
			}
			if(Loader.isModLoaded("ImmersiveEngineering") && ie) {
				ieHelper.registerCrusherRecipe(
						miscHelper.getRecipeKey("gregtech.centrifuge_crushed_to_dust_crusher", name),
						centrifugedCrushedOredict, new Object[] {
								dustOredict, 1,
								extra3DustOredict, 1, 0.15F,
						}, 6000);
			}
			if(Loader.isModLoaded("Railcraft") && rc) {
				rcHelper.registerRockCrusherRecipe(
						miscHelper.getRecipeKey("gregtech.centrifuge_crushed_to_dust_rock_crusher", name),
						centrifugedCrushedOredict, 1, new Object[] {
								dustOredict, 1, 1F,
								extra3DustOredict, 1, 0.1F,
						});
			}
			api.registerShapedRecipe(
					miscHelper.getRecipeKey("gregtech.centrifuge_crushed_to_dust_hard_hammer", name),
					dustOredict, 1, new Object[] {
							"h", "X",
							'X', centrifugedCrushedOredict,
							'h', "craftingToolHardHammer",
					});
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.impure_dust_to_dust", name),
					helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sCentrifugeRecipes).
					input(impureDustOredict, 1).
					output(dustOredict, 1).
					output(extra1TinyDustOredict, 1).
					time(800).energy(5));
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.pure_dust_to_dust", name),
					helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sCentrifugeRecipes).
					input(pureDustOredict, 1).
					output(dustOredict, 1).
					output(extra2TinyDustOredict, 1).
					time(800).energy(5));
			if(!configClumpBlacklist.contains(name) && oredict.contains(clumpOredict)) {
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.clump_to_dust", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sThermalCentrifugeRecipes).
						input(clumpOredict, 1).
						output(dustOredict, 1).
						output(extra2TinyDustOredict, 1).
						output("dustStone", 1).
						time(500).energy(48));
			}
			if(!configShardBlacklist.contains(name) && oredict.contains(shardOredict)) {
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.shard_to_dust", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sThermalCentrifugeRecipes).
						input(shardOredict, 1).
						output(dustOredict, 1).
						output(extra2TinyDustOredict, 1).
						output("dustStone", 1).
						time(500).energy(48));
			}
			if(!configDirtyGravelBlacklist.contains(name) && oredict.contains(dirtyGravelOredict)) {
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.dirty_gravel_to_dust", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sThermalCentrifugeRecipes).
						input(dirtyGravelOredict, 1).
						output(dustOredict, 1).
						output(extra2TinyDustOredict, 1).
						output("dustStone", 1).
						time(500).energy(48));
			}
			if(!material.getType().isCrystal() && !configCrystalBlacklist.contains(name) && oredict.contains(crystalOredict)) {
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.crystal_to_dust_hammer", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sHammerRecipes).
						input(crystalOredict, 1).
						output(dustOredict, 1).
						time(10).energy(16));
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.crystal_to_dust_macerator", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sMaceratorRecipes).
						input(crystalOredict, 1).
						output(dustOredict, 1).
						time(400).energy(2));
				if(Loader.isModLoaded("ImmersiveEngineering") && ie) {
					ieHelper.registerCrusherRecipe(
							miscHelper.getRecipeKey("gregtech.crystal_to_dust_crusher", name),
							crystalOredict, new Object[] {
									dustOredict, 1,
							}, 6000);
				}
				if(Loader.isModLoaded("Railcraft") && rc) {
					rcHelper.registerRockCrusherRecipe(
							miscHelper.getRecipeKey("gregtech.crystal_to_dust_rock_crusher", name),
							crystalOredict, 1, new Object[] {
									dustOredict, 1, 1F,
							});
				}
				api.registerShapedRecipe(
						miscHelper.getRecipeKey("gregtech.crystal_to_dust_hard_hammer", name),
						dustOredict, 1, new Object[] {
								"h", "X",
								'X', crystalOredict,
								'h', "craftingToolHardHammer",
						});
			}
			if(!configCrystallineBlacklist.contains(name) && oredict.contains(crystallineOredict)) {
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.crystalline_to_dust_hammer", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sHammerRecipes).
						input(crystallineOredict, 1).
						output(dustOredict, 1).
						time(10).energy(16));
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.crystalline_to_dust_macerator", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sMaceratorRecipes).
						input(crystallineOredict, 1).
						output(dustOredict, 1).
						time(400).energy(2));
				if(Loader.isModLoaded("ImmersiveEngineering") && ie) {
					ieHelper.registerCrusherRecipe(
							miscHelper.getRecipeKey("gregtech.crystalline_to_dust_crusher", name),
							crystallineOredict, new Object[] {
									dustOredict, 1,
							}, 6000);
				}
				if(Loader.isModLoaded("Railcraft") && rc) {
					rcHelper.registerRockCrusherRecipe(
							miscHelper.getRecipeKey("gregtech.crystalline_to_dust_rock_crusher", name),
							crystallineOredict, 1, new Object[] {
									dustOredict, 1, 1F,
							});
				}
				api.registerShapedRecipe(
						miscHelper.getRecipeKey("gregtech.crystalline_to_dust_hard_hammer", name),
						dustOredict, 1, new Object[] {
								"h", "X",
								'X', crystallineOredict,
								'h', "craftingToolHardHammer",
						});
			}
			// to_nugget
			if(material.getType().isIngot()) {
				api.registerSmeltingRecipe(
						miscHelper.getRecipeKey("gregtech.crushed_to_material", name),
						crushedOredict, nuggetOredict, 10, 0F);
				api.registerSmeltingRecipe(
						miscHelper.getRecipeKey("gregtech.purified_crushed_to_material", name),
						purifiedCrushedOredict, nuggetOredict, 10, 0F);
				api.registerSmeltingRecipe(
						miscHelper.getRecipeKey("gregtech.centrifuged_crushed_to_material", name),
						centrifugedCrushedOredict, nuggetOredict, 10, 0F);
			}
			// to_material
			if(material.getType().isIngot()) {
				api.registerSmeltingRecipe(
						miscHelper.getRecipeKey("gregtech.impure_dust_to_material", name),
						impureDustOredict, materialOredict, 1, 0F);
				api.registerSmeltingRecipe(
						miscHelper.getRecipeKey("gregtech.pure_dust_to_material", name),
						pureDustOredict, materialOredict, 1, 0F);
			}
			if(material.getType().isCrystalline()) {
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.ore_to_material", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sHammerRecipes).
						input(oreOredict, 1).
						output(materialOredict, 1).
						time(10).energy(16));
				if(Loader.isModLoaded("Magneticraft") && mc) {
					mcHelper.registerSifterRecipe(
							miscHelper.getRecipeKey("gregtech.purified_crushed_to_material", name),
							purifiedCrushedOredict, materialOredict, 1, dustOredict, 1, 0.2F);
				}
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.impure_dust_to_material_water", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sAutoclaveRecipes).
						input(impureDustOredict, 1).
						fluidInput(FluidRegistry.WATER, 200).
						output(materialOredict, 1, 9000).
						time(2000).energy(24));
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.impure_dust_to_material_distilled_water", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sAutoclaveRecipes).
						input(impureDustOredict, 1).
						fluidInput("ic2distilledwater", 200).
						output(materialOredict, 1, 9500).
						time(1500).energy(24));
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.pure_dust_to_material_water", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sAutoclaveRecipes).
						input(pureDustOredict, 1).
						fluidInput(FluidRegistry.WATER, 200).
						output(materialOredict, 1, 9000).
						time(2000).energy(24));
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.pure_dust_to_material_distilled_water", name),
						helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sAutoclaveRecipes).
						input(pureDustOredict, 1).
						fluidInput("ic2distilledwater", 200).
						output(materialOredict, 1, 9500).
						time(1500).energy(24));
			}
		}
	}
}
