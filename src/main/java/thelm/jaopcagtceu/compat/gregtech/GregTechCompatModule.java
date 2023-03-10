package thelm.jaopcagtceu.compat.gregtech;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.Streams;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.common.ConfigHolder;
import gregtech.common.items.MetaItems;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import thelm.jaopca.api.JAOPCAApi;
import thelm.jaopca.api.config.IDynamicSpecConfig;
import thelm.jaopca.api.helpers.IMiscHelper;
import thelm.jaopca.api.materials.IMaterial;
import thelm.jaopca.api.materials.MaterialType;
import thelm.jaopca.api.modules.IModule;
import thelm.jaopca.api.modules.IModuleData;
import thelm.jaopca.api.modules.JAOPCAModule;
import thelm.jaopca.utils.ApiImpl;
import thelm.jaopca.utils.MiscHelper;
import thelm.jaopcagtceu.compat.gregtech.recipes.GregTechRecipeSettings;

@JAOPCAModule(modDependencies = "gregtech@[2,)")
public class GregTechCompatModule implements IModule {

	static final Set<String> BLACKLIST = GregTechModule.BLACKLIST;
	static final Set<String> PLATE_BLACKLIST = new TreeSet<>(GregTechModule.ALTS);
	static final Set<String> STICK_BLACKLIST = new TreeSet<>(GregTechModule.ALTS);
	static final Set<String> MOLTEN_BLACKLIST = new TreeSet<>(GregTechModule.ALTS);

	static {
		Streams.stream(GregTechAPI.MATERIAL_REGISTRY).
		filter(m->m.hasFlag(MaterialFlags.GENERATE_PLATE)).
		forEach(m->PLATE_BLACKLIST.add(m.toCamelCaseString()));
		Streams.stream(GregTechAPI.MATERIAL_REGISTRY).
		filter(m->m.hasFlag(MaterialFlags.GENERATE_ROD)).
		forEach(m->STICK_BLACKLIST.add(m.toCamelCaseString()));
		Streams.stream(GregTechAPI.MATERIAL_REGISTRY).
		filter(m->m.hasFluid()).
		forEach(m->MOLTEN_BLACKLIST.add(m.toCamelCaseString()));
	}

	private static Set<String> configAutoclaveToCrystalBlacklist = new TreeSet<>();
	private static Set<String> configImplosionToCrystalBlacklist = new TreeSet<>();
	private static Set<String> configToMaterialBlacklist = new TreeSet<>();
	private static Set<String> configToBlockBlacklist = new TreeSet<>();
	private static Set<String> configToNuggetBlacklist = new TreeSet<>();
	private static Set<String> configToPlateBlacklist = new TreeSet<>();
	private static Set<String> configToDensePlateBlacklist = new TreeSet<>();
	private static Set<String> configToGearBlacklist = new TreeSet<>();
	private static Set<String> configToStickBlacklist = new TreeSet<>();
	private static Set<String> configSmallDustPackingBlacklist = new TreeSet<>();
	private static Set<String> configTinyDustPackingBlacklist = new TreeSet<>();
	private static Set<String> configMortarToDustBlacklist = new TreeSet<>();
	private static Set<String> configRecyclingBlacklist = new TreeSet<>();

	private static boolean jaopcaOnly = true;

	@Override
	public String getName() {
		return "gregtech_compat";
	}

	@Override
	public Set<MaterialType> getMaterialTypes() {
		return EnumSet.allOf(MaterialType.class);
	}

	@Override
	public void defineModuleConfig(IModuleData moduleData, IDynamicSpecConfig config) {
		IMiscHelper helper = MiscHelper.INSTANCE;
		jaopcaOnly = config.getDefinedBoolean("recipes.jaopcaOnly", jaopcaOnly, "Should the module only add molten recipes for materials with JAOPCA molten fluids.");
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.autoclaveToCrystalMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configAutoclaveToCrystalBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.implosionToCrystalMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configImplosionToCrystalBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.toMaterialMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configToMaterialBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.toBlockMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configToBlockBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.toNuggetMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configToNuggetBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.toPlateMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configToPlateBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.toDensePlateMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configToDensePlateBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.toGearMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configToGearBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.toStickMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configToStickBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.smallDustPackingMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configSmallDustPackingBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.tinyDustPackingMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configTinyDustPackingBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.mortarToDustMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configMortarToDustBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.recyclingMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configRecyclingBlacklist);
	}

	@Override
	public void onInit(IModuleData moduleData, FMLInitializationEvent event) {
		JAOPCAApi api = ApiImpl.INSTANCE;
		GregTechHelper helper = GregTechHelper.INSTANCE;
		IMiscHelper miscHelper = MiscHelper.INSTANCE;
		Set<String> oredict = api.getOredict();
		Set<IMaterial> moltenMaterials = api.getForm("molten").getMaterials();
		for(IMaterial material : moduleData.getMaterials()) {
			MaterialType type = material.getType();
			String name = material.getName();
			if(type.isCrystalline() && !BLACKLIST.contains(name) && !configAutoclaveToCrystalBlacklist.contains(name)) {
				String dustOredict = miscHelper.getOredictName("dust", name);
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				if(oredict.contains(dustOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.dust_to_material_autoclave_water", material.getName()),
							helper.recipeSettings(RecipeMaps.AUTOCLAVE_RECIPES).
							input(dustOredict, 1).
							fluidInput(Materials.Water.getFluid(), 250).
							output(materialOredict, 1, 7000, 1000).
							time(1200).energy(24));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.dust_to_material_autoclave_distilled_water", material.getName()),
							helper.recipeSettings(RecipeMaps.AUTOCLAVE_RECIPES).
							input(dustOredict, 1).
							fluidInput(Materials.DistilledWater.getFluid(), 50).
							output(materialOredict).
							time(600).energy(24));
				}
			}
			if(type.isCrystalline() && !BLACKLIST.contains(name) && !configAutoclaveToCrystalBlacklist.contains(name)) {
				String dustOredict = miscHelper.getOredictName("dust", name);
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				if(oredict.contains(dustOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.dust_to_material_implosion_tnt", material.getName()),
							helper.recipeSettings(RecipeMaps.IMPLOSION_RECIPES).
							input(dustOredict, 4).
							additional(b->b.explosivesAmount(2)).
							output(materialOredict, 3).
							output("dustSmallDarkAsh", 1));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.dust_to_material_implosion_dynamite", material.getName()),
							helper.recipeSettings(RecipeMaps.IMPLOSION_RECIPES).
							input(dustOredict, 4).
							additional(b->b.explosivesType(MetaItems.DYNAMITE.getStackForm())).
							output(materialOredict, 3).
							output("dustSmallDarkAsh", 1));
				}
			}
			if(type.isDust() && !PLATE_BLACKLIST.contains(name) && !configToPlateBlacklist.contains(name)) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String plateOredict = miscHelper.getOredictName("plate", name);
				if(oredict.contains(plateOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_plate_compressor", material.getName()),
							helper.recipeSettings(RecipeMaps.COMPRESSOR_RECIPES).
							input(materialOredict, 1).
							output(plateOredict, 1));
				}
			}
			if(!BLACKLIST.contains(name) && !configSmallDustPackingBlacklist.contains(name)) {
				String smallDustOredict = miscHelper.getOredictName("dustSmall", name);
				String dustOredict = miscHelper.getOredictName("dust", name);
				if(oredict.contains(smallDustOredict) && oredict.contains(dustOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.small_dust_to_dust", material.getName()),
							helper.recipeSettings(RecipeMaps.PACKER_RECIPES).
							input(smallDustOredict, 4).
							circuitMeta(1).
							output(dustOredict, 1));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.dust_to_small_dust", material.getName()),
							helper.recipeSettings(RecipeMaps.PACKER_RECIPES).
							input(smallDustOredict, 1).
							circuitMeta(2).
							output(dustOredict, 4));
				}
			}
			if(!BLACKLIST.contains(name) && !configTinyDustPackingBlacklist.contains(name)) {
				String tinyDustOredict = miscHelper.getOredictName("dustTiny", name);
				String dustOredict = miscHelper.getOredictName("dust", name);
				if(oredict.contains(tinyDustOredict) && oredict.contains(dustOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.tiny_dust_to_dust", material.getName()),
							helper.recipeSettings(RecipeMaps.PACKER_RECIPES).
							input(tinyDustOredict, 9).
							circuitMeta(1).
							output(dustOredict, 1));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.dust_to_tiny_dust", material.getName()),
							helper.recipeSettings(RecipeMaps.PACKER_RECIPES).
							input(dustOredict, 1).
							circuitMeta(1).
							output(tinyDustOredict, 9));
				}
			}
			if(!type.isDust() && !BLACKLIST.contains(name) && !configMortarToDustBlacklist.contains(name)) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String dustOredict = miscHelper.getOredictName("dust", name);
				if(oredict.contains(dustOredict)) {
					api.registerShapedRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_dust_mortar", material.getName()),
							dustOredict, 1, new Object[] {
									"X", "m",
									'X', materialOredict,
									'm', "craftingToolMortar",
							});
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToStickBlacklist.contains(name)) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String stickOredict = miscHelper.getOredictName("stick", name);
				if(oredict.contains(stickOredict)) {
					api.registerShapedRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_stick_file", material.getName()),
							stickOredict, 1, new Object[] {
									"f ", " X",
									'X', materialOredict,
									'f', "craftingToolFile",
							});
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_stick_extruder", material.getName()),
							helper.recipeSettings(RecipeMaps.EXTRUDER_RECIPES).
							input(materialOredict, 1).
							input(MetaItems.SHAPE_EXTRUDER_ROD.getStackForm(), 0).
							output(stickOredict, 2).
							time(200).energy(42));
				}
			}
			if(type.isIngot() && !MOLTEN_BLACKLIST.contains(name) && !configToMaterialBlacklist.contains(name) && (!jaopcaOnly || moltenMaterials.contains(material))) {
				String moltenName = miscHelper.getFluidName("", name);
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				if(FluidRegistry.isFluidRegistered(moltenName)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.molten_to_ingot", material.getName()),
							helper.recipeSettings(RecipeMaps.FLUID_SOLIDFICATION_RECIPES).
							input(MetaItems.SHAPE_MOLD_INGOT.getStackForm(), 0).
							fluidInput(moltenName, 144).
							output(materialOredict, 1).
							time(20).energy(7));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToNuggetBlacklist.contains(name)) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String nuggetOredict = miscHelper.getOredictName("nugget", name);
				if(oredict.contains(nuggetOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_nugget_alloy_smelter", material.getName()),
							helper.recipeSettings(RecipeMaps.ALLOY_SMELTER_RECIPES).
							input(materialOredict, 1).
							input(MetaItems.SHAPE_MOLD_NUGGET.getStackForm(), 0).
							output(nuggetOredict, 9).
							time(100).energy(7));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToMaterialBlacklist.contains(name)) {
				String blockOredict = miscHelper.getOredictName("block", name);
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				if(oredict.contains(blockOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.block_to_material_alloy_smelter", material.getName()),
							helper.recipeSettings(RecipeMaps.ALLOY_SMELTER_RECIPES).
							input(blockOredict, 1).
							input(MetaItems.SHAPE_MOLD_INGOT.getStackForm(), 0).
							output(materialOredict, (material.isSmallStorageBlock() ? 4 : 9)).
							time(900).energy(7));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToBlockBlacklist.contains(name)) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String blockOredict = miscHelper.getOredictName("block", name);
				if(oredict.contains(blockOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_block_compressor", material.getName()),
							helper.recipeSettings(RecipeMaps.COMPRESSOR_RECIPES).
							input(materialOredict, (material.isSmallStorageBlock() ? 4 : 9)).
							output(blockOredict, 1).
							time(300).energy(2));
				}
			}
			if(type.isIngot() && !PLATE_BLACKLIST.contains(name) && !configToPlateBlacklist.contains(name)) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String plateOredict = miscHelper.getOredictName("plate", name);
				if(oredict.contains(plateOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_plate_bender", material.getName()),
							helper.recipeSettings(RecipeMaps.BENDER_RECIPES).
							input(materialOredict, 1).
							circuitMeta(1).
							output(plateOredict, 1).
							time(100).energy(24));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_plate_forge_hammer", material.getName()),
							helper.recipeSettings(RecipeMaps.FORGE_HAMMER_RECIPES).
							input(materialOredict, 3).
							output(plateOredict, 2).
							time(100).energy(16));
					api.registerShapedRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_plate_hard_hammer", material.getName()),
							plateOredict, 1, new Object[] {
									"h", "I", "I",
									'I', materialOredict,
									'h', "craftingToolHardHammer",
							});
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_plate_extruder", material.getName()),
							helper.recipeSettings(RecipeMaps.EXTRUDER_RECIPES).
							input(materialOredict, 1).
							input(MetaItems.SHAPE_EXTRUDER_PLATE.getStackForm(), 0).
							output(plateOredict, 1).
							time(100).energy(56));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToMaterialBlacklist.contains(name)) {
				String nuggetOredict = miscHelper.getOredictName("nugget", name);
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				if(oredict.contains(nuggetOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.nugget_to_material_compressor", material.getName()),
							helper.recipeSettings(RecipeMaps.COMPRESSOR_RECIPES).
							input(nuggetOredict, 9).
							output(materialOredict, 1).
							time(300).energy(2));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.nugget_to_material_alloy_smelter", material.getName()),
							helper.recipeSettings(RecipeMaps.ALLOY_SMELTER_RECIPES).
							input(nuggetOredict, 9).
							input(MetaItems.SHAPE_MOLD_INGOT.getStackForm(), 0).
							output(materialOredict, 1).
							time(100).energy(7));
				}
			}
			if(type.isIngot() && !MOLTEN_BLACKLIST.contains(name) && !configToNuggetBlacklist.contains(name) && (!jaopcaOnly || moltenMaterials.contains(material))) {
				String moltenName = miscHelper.getFluidName("", name);
				String nuggetOredict = miscHelper.getOredictName("nugget", name);
				if(FluidRegistry.isFluidRegistered(moltenName) && oredict.contains(nuggetOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.molten_to_nugget", material.getName()),
							helper.recipeSettings(RecipeMaps.FLUID_SOLIDFICATION_RECIPES).
							input(MetaItems.SHAPE_MOLD_NUGGET.getStackForm(), 0).
							fluidInput(moltenName, 144).
							output(nuggetOredict, 9).
							time(100).energy(7));
				}
			}
			if(type.isIngot() && !MOLTEN_BLACKLIST.contains(name) && !configToBlockBlacklist.contains(name) && (!jaopcaOnly || moltenMaterials.contains(material))) {
				String moltenName = miscHelper.getFluidName("", name);
				String blockOredict = miscHelper.getOredictName("block", name);
				if(FluidRegistry.isFluidRegistered(moltenName) && oredict.contains(blockOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.molten_to_block", material.getName()),
							helper.recipeSettings(RecipeMaps.FLUID_SOLIDFICATION_RECIPES).
							input(MetaItems.SHAPE_MOLD_BLOCK.getStackForm(), 0).
							fluidInput(moltenName, 144*(material.isSmallStorageBlock() ? 4 : 9)).
							output(blockOredict, 1).
							time(100).energy(7));
				}
			}
			if(!PLATE_BLACKLIST.contains(name) && !configToPlateBlacklist.contains(name)) {
				String blockOredict = miscHelper.getOredictName("block", name);
				String plateOredict = miscHelper.getOredictName("plate", name);
				if(oredict.contains(blockOredict) && oredict.contains(plateOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.block_to_plate", material.getName()),
							helper.recipeSettings(RecipeMaps.CUTTER_RECIPES).
							input(blockOredict, 1).
							output(plateOredict, (material.isSmallStorageBlock() ? 4 : 9)).
							time(800).energy(30));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToBlockBlacklist.contains(name)) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String blockOredict = miscHelper.getOredictName("block", name);
				if(oredict.contains(blockOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_block_extruder", material.getName()),
							helper.recipeSettings(RecipeMaps.EXTRUDER_RECIPES).
							input(materialOredict, (material.isSmallStorageBlock() ? 4 : 9)).
							input(MetaItems.SHAPE_EXTRUDER_BLOCK.getStackForm(), 0).
							output(blockOredict, 1).
							time(10).energy(56));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_block_alloy_smelter", material.getName()),
							helper.recipeSettings(RecipeMaps.ALLOY_SMELTER_RECIPES).
							input(materialOredict, (material.isSmallStorageBlock() ? 4 : 9)).
							input(MetaItems.SHAPE_MOLD_BLOCK.getStackForm(), 0).
							output(blockOredict, 1).
							time(5).energy(28));
				}
			}
			if(type.isCrystalline() && !BLACKLIST.contains(name) && !configToBlockBlacklist.contains(name)) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String blockOredict = miscHelper.getOredictName("block", name);
				if(oredict.contains(blockOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_block_compressor", material.getName()),
							helper.recipeSettings(RecipeMaps.COMPRESSOR_RECIPES).
							input(materialOredict, (material.isSmallStorageBlock() ? 4 : 9)).
							output(blockOredict, 1).
							time(300).energy(2));
				}
			}
			if(type.isCrystalline() && !BLACKLIST.contains(name) && !configToMaterialBlacklist.contains(name)) {
				String blockOredict = miscHelper.getOredictName("block", name);
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				if(oredict.contains(blockOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.block_to_material_forge_hammer", material.getName()),
							helper.recipeSettings(RecipeMaps.FORGE_HAMMER_RECIPES).
							input(blockOredict, 1).
							output(materialOredict, (material.isSmallStorageBlock() ? 4 : 9)).
							time(100).energy(24));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToGearBlacklist.contains(name)) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String gearOredict = miscHelper.getOredictName("gear", name);
				if(oredict.contains(gearOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_gear_extruder", material.getName()),
							helper.recipeSettings(RecipeMaps.EXTRUDER_RECIPES).
							input(materialOredict, 4).
							input(MetaItems.SHAPE_EXTRUDER_GEAR.getStackForm(), 0).
							output(gearOredict, 1).
							time(500).energy(56));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_gear_alloy_smelter", material.getName()),
							helper.recipeSettings(RecipeMaps.ALLOY_SMELTER_RECIPES).
							input(materialOredict, 8).
							input(MetaItems.SHAPE_MOLD_GEAR.getStackForm(), 0).
							output(gearOredict, 1).
							time(1000).energy(14));
				}
			}
			if(type.isIngot() && !MOLTEN_BLACKLIST.contains(name) && !configToGearBlacklist.contains(name) && (!jaopcaOnly || moltenMaterials.contains(material))) {
				String moltenName = miscHelper.getFluidName("", name);
				String gearOredict = miscHelper.getOredictName("gear", name);
				if(FluidRegistry.isFluidRegistered(moltenName) && oredict.contains(gearOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.molten_to_gear", material.getName()),
							helper.recipeSettings(RecipeMaps.FLUID_SOLIDFICATION_RECIPES).
							input(MetaItems.SHAPE_MOLD_GEAR.getStackForm(), 0).
							fluidInput(moltenName, 576).
							output(gearOredict, 1).
							time(100).energy(7));
				}
			}
			if(!(PLATE_BLACKLIST.contains(name) && STICK_BLACKLIST.contains(name)) && !configToGearBlacklist.contains(name)) {
				String plateOredict = miscHelper.getOredictName("plate", name);
				String stickOredict = miscHelper.getOredictName("stick", name);
				String gearOredict = miscHelper.getOredictName("gear", name);
				if(oredict.contains(plateOredict) && oredict.contains(stickOredict) && oredict.contains(gearOredict)) {
					api.registerShapedRecipe(
							miscHelper.getRecipeKey("gregtech.plate_stick_to_gear", material.getName()),
							gearOredict, 1, new Object[] {
									"RPR", "PwP", "RPR",
									'P', plateOredict,
									'R', stickOredict,
									'w', "craftingToolWrench",
							});
				}
			}
			if(type.isIngot() && !MOLTEN_BLACKLIST.contains(name) && !configToPlateBlacklist.contains(name) && (!jaopcaOnly || moltenMaterials.contains(material))) {
				String moltenName = miscHelper.getFluidName("", name);
				String plateOredict = miscHelper.getOredictName("plate", name);
				if(FluidRegistry.isFluidRegistered(moltenName) && oredict.contains(plateOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.molten_to_plate", material.getName()),
							helper.recipeSettings(RecipeMaps.FLUID_SOLIDFICATION_RECIPES).
							input(MetaItems.SHAPE_MOLD_PLATE.getStackForm(), 0).
							fluidInput(moltenName, 144).
							output(plateOredict, 1).
							time(40).energy(7));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToDensePlateBlacklist.contains(name)) {
				String plateOredict = miscHelper.getOredictName("plate", name);
				String densePlateOredict = miscHelper.getOredictName("plateDense", name);
				if(oredict.contains(plateOredict) && oredict.contains(densePlateOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.plate_to_dense_plate", material.getName()),
							helper.recipeSettings(RecipeMaps.BENDER_RECIPES).
							input(plateOredict, 9).
							circuitMeta(9).
							output(densePlateOredict, 1).
							time(900).energy(96));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToDensePlateBlacklist.contains(name)) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String densePlateOredict = miscHelper.getOredictName("plateDense", name);
				if(oredict.contains(densePlateOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_dense_plate", material.getName()),
							helper.recipeSettings(RecipeMaps.BENDER_RECIPES).
							input(materialOredict, 9).
							circuitMeta(9).
							output(densePlateOredict, 1).
							time(900).energy(96));
				}
			}
			if(!type.isDust() && !BLACKLIST.contains(name) && !configToStickBlacklist.contains(name)) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String stickOredict = miscHelper.getOredictName("stick", name);
				String smallDustOredict = miscHelper.getOredictName("dustSmall", name);
				if(oredict.contains(stickOredict)) {
					GregTechRecipeSettings<?> settings = helper.recipeSettings(RecipeMaps.LATHE_RECIPES).
							input(materialOredict, 1).
							time(200).energy(16);
					if(ConfigHolder.recipes.harderRods) {
						settings.output(stickOredict, 1);
						if(oredict.contains(smallDustOredict)) {
							settings.output(smallDustOredict, 2);
						}
					}
					else {
						settings.output(stickOredict, 2);
					}
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_stick_lathe", material.getName()),
							settings);
				}
			}
			if(!type.isDust() && !BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String dustOredict = miscHelper.getOredictName("dust", name);
				if(oredict.contains(dustOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_dust_macerator", material.getName()),
							helper.recipeSettings(RecipeMaps.MACERATOR_RECIPES).
							input(materialOredict, 1).
							output(dustOredict, 1).
							time(100).energy(2));
				}
			}
			if(!BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				String blockOredict = miscHelper.getOredictName("block", name);
				String dustOredict = miscHelper.getOredictName("dust", name);
				if(oredict.contains(blockOredict) && oredict.contains(dustOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.block_to_dust", material.getName()),
							helper.recipeSettings(RecipeMaps.MACERATOR_RECIPES).
							input(blockOredict, 1).
							output(dustOredict, (material.isSmallStorageBlock() ? 4 : 9)).
							time(100*(material.isSmallStorageBlock() ? 4 : 9)).energy(2));
				}
			}
			if(!BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				String nuggetOredict = miscHelper.getOredictName("nugget", name);
				String tinyDustOredict = miscHelper.getOredictName("dustTiny", name);
				if(oredict.contains(nuggetOredict) && oredict.contains(tinyDustOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.nugget_to_tiny_dust", material.getName()),
							helper.recipeSettings(RecipeMaps.MACERATOR_RECIPES).
							input(nuggetOredict, 1).
							output(tinyDustOredict, 1).
							time(11).energy(2));
				}
			}
			if(!BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				String plateOredict = miscHelper.getOredictName("plate", name);
				String dustOredict = miscHelper.getOredictName("dust", name);
				if(oredict.contains(plateOredict) && oredict.contains(dustOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.plate_to_dust", material.getName()),
							helper.recipeSettings(RecipeMaps.MACERATOR_RECIPES).
							input(plateOredict, 1).
							output(dustOredict, 1).
							time(100).energy(2));
				}
			}
			if(!BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				String densePlateOredict = miscHelper.getOredictName("plateDense", name);
				String dustOredict = miscHelper.getOredictName("dust", name);
				if(oredict.contains(densePlateOredict) && oredict.contains(dustOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.dense_plate_to_dust", material.getName()),
							helper.recipeSettings(RecipeMaps.MACERATOR_RECIPES).
							input(densePlateOredict, 1).
							output(dustOredict, 9).
							time(900).energy(2));
				}
			}
			if(!BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				String gearOredict = miscHelper.getOredictName("gear", name);
				String dustOredict = miscHelper.getOredictName("dust", name);
				if(oredict.contains(gearOredict) && oredict.contains(dustOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.gear_to_dust_macerator", material.getName()),
							helper.recipeSettings(RecipeMaps.MACERATOR_RECIPES).
							input(gearOredict, 1).
							output(dustOredict, 4).
							time(400).energy(2));
				}
			}
			if(!BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				String stickOredict = miscHelper.getOredictName("stick", name);
				String smallDustOredict = miscHelper.getOredictName("dustSmall", name);
				if(oredict.contains(stickOredict) && oredict.contains(smallDustOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.stick_to_small_dust", material.getName()),
							helper.recipeSettings(RecipeMaps.MACERATOR_RECIPES).
							input(stickOredict, 1).
							output(smallDustOredict, 2).
							time(50).energy(2));
				}
			}
			if(type.isIngot() && !MOLTEN_BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name) && (!jaopcaOnly || moltenMaterials.contains(material))) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String moltenName = miscHelper.getFluidName("", name);
				if(FluidRegistry.isFluidRegistered(moltenName)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_molten", material.getName()),
							helper.recipeSettings(RecipeMaps.EXTRACTOR_RECIPES).
							input(materialOredict, 1).
							fluidOutput(moltenName, 144).
							time(100).energy(30));
				}
			}
			if(type.isIngot() && !MOLTEN_BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name) && (!jaopcaOnly || moltenMaterials.contains(material))) {
				String blockOredict = miscHelper.getOredictName("block", name);
				String moltenName = miscHelper.getFluidName("", name);
				if(oredict.contains(blockOredict) && FluidRegistry.isFluidRegistered(moltenName)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.block_to_molten", material.getName()),
							helper.recipeSettings(RecipeMaps.EXTRACTOR_RECIPES).
							input(blockOredict, 1).
							fluidOutput(moltenName, 144*(material.isSmallStorageBlock() ? 4 : 9)).
							time(100*(material.isSmallStorageBlock() ? 4 : 9)).energy(30));
				}
			}
			if(type.isIngot() && !MOLTEN_BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name) && (!jaopcaOnly || moltenMaterials.contains(material))) {
				String nuggetOredict = miscHelper.getOredictName("nugget", name);
				String moltenName = miscHelper.getFluidName("", name);
				if(oredict.contains(nuggetOredict) && FluidRegistry.isFluidRegistered(moltenName)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.nugget_to_molten", material.getName()),
							helper.recipeSettings(RecipeMaps.EXTRACTOR_RECIPES).
							input(nuggetOredict, 1).
							fluidOutput(moltenName, 16).
							time(11).energy(30));
				}
			}
			if(type.isIngot() && !MOLTEN_BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name) && (!jaopcaOnly || moltenMaterials.contains(material))) {
				String plateOredict = miscHelper.getOredictName("plate", name);
				String moltenName = miscHelper.getFluidName("", name);
				if(oredict.contains(plateOredict) && FluidRegistry.isFluidRegistered(moltenName)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.plate_to_molten", material.getName()),
							helper.recipeSettings(RecipeMaps.EXTRACTOR_RECIPES).
							input(plateOredict, 1).
							fluidOutput(moltenName, 144).
							time(100).energy(30));
				}
			}
			if(type.isIngot() && !MOLTEN_BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name) && (!jaopcaOnly || moltenMaterials.contains(material))) {
				String densePlateOredict = miscHelper.getOredictName("plateDense", name);
				String moltenName = miscHelper.getFluidName("", name);
				if(oredict.contains(densePlateOredict) && FluidRegistry.isFluidRegistered(moltenName)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.dense_plate_to_molten", material.getName()),
							helper.recipeSettings(RecipeMaps.EXTRACTOR_RECIPES).
							input(densePlateOredict, 1).
							fluidOutput(moltenName, 1296).
							time(900).energy(30));
				}
			}
			if(type.isIngot() && !MOLTEN_BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name) && (!jaopcaOnly || moltenMaterials.contains(material))) {
				String gearOredict = miscHelper.getOredictName("gear", name);
				String moltenName = miscHelper.getFluidName("", name);
				if(oredict.contains(gearOredict) && FluidRegistry.isFluidRegistered(moltenName)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.gear_to_molten", material.getName()),
							helper.recipeSettings(RecipeMaps.EXTRACTOR_RECIPES).
							input(gearOredict, 1).
							fluidOutput(moltenName, 576).
							time(400).energy(30));
				}
			}
			if(type.isIngot() && !MOLTEN_BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name) && (!jaopcaOnly || moltenMaterials.contains(material))) {
				String stickOredict = miscHelper.getOredictName("stick", name);
				String moltenName = miscHelper.getFluidName("", name);
				if(oredict.contains(stickOredict) && FluidRegistry.isFluidRegistered(moltenName)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.stick_to_molten", material.getName()),
							helper.recipeSettings(RecipeMaps.EXTRACTOR_RECIPES).
							input(stickOredict, 1).
							fluidOutput(moltenName, 72).
							time(50).energy(30));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				String plateOredict = miscHelper.getOredictName("plate", name);
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				if(oredict.contains(plateOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.plate_to_material", material.getName()),
							helper.recipeSettings(RecipeMaps.ARC_FURNACE_RECIPES).
							input(plateOredict, 1).
							output(materialOredict, 1).
							time(100).energy(30));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				String densePlateOredict = miscHelper.getOredictName("plateDense", name);
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				if(oredict.contains(densePlateOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.dense_plate_to_material", material.getName()),
							helper.recipeSettings(RecipeMaps.ARC_FURNACE_RECIPES).
							input(densePlateOredict, 1).
							output(materialOredict, 9).
							time(900).energy(30));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				String gearOredict = miscHelper.getOredictName("gear", name);
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				if(oredict.contains(gearOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.gear_to_material", material.getName()),
							helper.recipeSettings(RecipeMaps.ARC_FURNACE_RECIPES).
							input(gearOredict, 1).
							output(materialOredict, 4).
							time(400).energy(30));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				String stickOredict = miscHelper.getOredictName("stick", name);
				String nuggetOredict = miscHelper.getOredictName("nugget", name);
				if(oredict.contains(stickOredict) && oredict.contains(nuggetOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.stick_to_nugget", material.getName()),
							helper.recipeSettings(RecipeMaps.ARC_FURNACE_RECIPES).
							input(stickOredict, 1).
							output(nuggetOredict, 4).
							time(50).energy(30));
				}
			}
		}
	}
}
