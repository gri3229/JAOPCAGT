package thelm.jaopca.gt4.compat.gregtechaddon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.TreeSet;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import gregtechmod.api.enums.GT_Items;
import gregtechmod.api.enums.Materials;
import gregtechmod.common.recipe.RecipeMaps;
import ic2.core.Ic2Items;
import net.minecraft.init.Blocks;
import thelm.jaopca.api.JAOPCAApi;
import thelm.jaopca.api.config.IDynamicSpecConfig;
import thelm.jaopca.api.helpers.IMiscHelper;
import thelm.jaopca.api.materials.IMaterial;
import thelm.jaopca.api.materials.MaterialType;
import thelm.jaopca.api.modules.IModule;
import thelm.jaopca.api.modules.IModuleData;
import thelm.jaopca.api.modules.JAOPCAModule;
import thelm.jaopca.compat.ic2.IC2Helper;
import thelm.jaopca.compat.railcraft.RailcraftHelper;
import thelm.jaopca.gt4.compat.gregtechaddon.recipes.GregTechAddonRecipeSettings;
import thelm.jaopca.gt4.compat.thermalexpansion.ThermalExpansionHelper;
import thelm.jaopca.utils.ApiImpl;
import thelm.jaopca.utils.MiscHelper;

@JAOPCAModule(modDependencies = "gregtech_addon")
public class GregTechAddonCompatModule implements IModule {

	static final Set<String> BLACKLIST = GregTechAddonModule.BLACKLIST;
	static final Set<String> GEAR_BLACKLIST = new TreeSet<>();

	static {
		Arrays.stream(Materials.values()).filter(m->(m.mTypes & 128) != 0).forEach(m->BLACKLIST.add(m.name()));
	}

	private static Set<String> configToCrystalBlacklist = new TreeSet<>();
	private static Set<String> configToMaterialBlacklist = new TreeSet<>();
	private static Set<String> configToDustBlacklist = new TreeSet<>();
	private static Set<String> configToBlockBlacklist = new TreeSet<>();
	private static Set<String> configToPlateBlacklist = new TreeSet<>();
	private static Set<String> configToDensePlateBlacklist = new TreeSet<>();
	private static Set<String> configToGearBlacklist = new TreeSet<>();
	private static Set<String> configToStickBlacklist = new TreeSet<>();

	private static boolean rockCrusher = true;
	private static boolean pulverizer = true;
	private static boolean smelter = true;

	@Override
	public String getName() {
		return "gregtech_addon_compat";
	}

	@Override
	public Set<MaterialType> getMaterialTypes() {
		return EnumSet.allOf(MaterialType.class);
	}

	@Override
	public void defineModuleConfig(IModuleData moduleData, IDynamicSpecConfig config) {
		IMiscHelper helper = MiscHelper.INSTANCE;
		rockCrusher = config.getDefinedBoolean("recipes.rockCrusher", rockCrusher, "Should the module add recipes to Railcraft's rock crusher.");
		pulverizer = config.getDefinedBoolean("recipes.pulverizer", pulverizer, "Should the module add recipes to Thermal Expansion's pulverizer.");
		smelter = config.getDefinedBoolean("recipes.smelter", smelter, "Should the module add recipes to Thermal Expansion's induction smelter.");
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.toCrystalMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configToCrystalBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.toMaterialMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configToMaterialBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.toDustMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configToDustBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.toBlockMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configToBlockBlacklist);
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
	}

	@Override
	public void onInit(IModuleData moduleData, FMLInitializationEvent event) {
		JAOPCAApi api = ApiImpl.INSTANCE;
		GregTechAddonHelper helper = GregTechAddonHelper.INSTANCE;
		IC2Helper ic2Helper = IC2Helper.INSTANCE;
		RailcraftHelper rcHelper = RailcraftHelper.INSTANCE;
		ThermalExpansionHelper teHelper = ThermalExpansionHelper.INSTANCE;
		IMiscHelper miscHelper = MiscHelper.INSTANCE;
		Set<String> oredict = api.getOredict();
		for(IMaterial material : moduleData.getMaterials()) {
			MaterialType type = material.getType();
			String name = material.getName();
			if(type.isCrystalline() && !BLACKLIST.contains(name) && !configToCrystalBlacklist.contains(name)) {
				String dustOredict = miscHelper.getOredictName("dust", name);
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				if(oredict.contains(dustOredict)) {
					helper.registerGregTechAddonRecipe(
							miscHelper.getRecipeKey("gregtech_addon.dust_to_material", name),
							helper.recipeSettings(RecipeMaps.IMPLOSION_COMPRESSOR).
							input(dustOredict, 4).
							input(Ic2Items.industrialTnt, 24).
							output(materialOredict, 3).
							output("dustDarkAsh", 12).
							energy(30).time(20));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToPlateBlacklist.contains(name)) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String plateOredict = miscHelper.getOredictName("plate", name);
				if(oredict.contains(plateOredict)) {
					helper.registerGregTechAddonRecipe(
							miscHelper.getRecipeKey("gregtech_addon.material_to_plate_hammer", name),
							helper.recipeSettings(RecipeMaps.HAMMER).
							input(materialOredict, 2).
							output(plateOredict, 1).
							energy(16).time(100));
					helper.registerGregTechAddonRecipe(
							miscHelper.getRecipeKey("gregtech_addon.material_to_plate_bending", name),
							helper.recipeSettings(RecipeMaps.BENDING).shaped(true).
							input(materialOredict, 1).
							input(GT_Items.Circuit_Integrated.getWithDamage(0, 1), 0).
							output(plateOredict, 1).
							energy(24).time(200));
					helper.registerGregTechAddonRecipe(
							miscHelper.getRecipeKey("gregtech_addon.material_to_plate_extruding", name),
							helper.recipeSettings(RecipeMaps.EXTRUDING).shaped(true).
							input(materialOredict, 1).
							input(GT_Items.Shape_Extruder_Plate.get(0), 0).
							output(plateOredict, 1).
							energy(128).time(100));
					helper.registerGregTechAddonRecipe(
							miscHelper.getRecipeKey("gregtech_addon.material_to_plate_alloy_smelting", name),
							helper.recipeSettings(RecipeMaps.ALLOY_SMELTING).
							input(materialOredict, 2).
							input(GT_Items.Shape_Mold_Plate.get(0), 0).
							output(plateOredict, 1).
							energy(32).time(200));
					api.registerShapedRecipe(
							miscHelper.getRecipeKey("gregtech_addon.material_to_plate_hard_hammer", name),
							plateOredict, 1, new Object[] {
									"H", "I", "I",
									'H', "craftingToolHardHammer",
									'I', materialOredict,
							});
					api.registerShapelessRecipe(
							miscHelper.getRecipeKey("gregtech_addon.material_to_plate_forge_hammer", name),
							plateOredict, 1, new Object[] {
									"craftingToolForgeHammer", materialOredict,
							});
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToDensePlateBlacklist.contains(name)) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String densePlateOredict = miscHelper.getOredictName("plateDense", name);
				if(oredict.contains(densePlateOredict)) {
					helper.registerGregTechAddonRecipe(
							miscHelper.getRecipeKey("gregtech_addon.material_to_dense_plate", name),
							helper.recipeSettings(RecipeMaps.BENDING).
							input(materialOredict, 9).
							input(GT_Items.Circuit_Integrated.getWithDamage(0, 9), 0).
							output(densePlateOredict, 1).
							energy(24).time(1800));
				}
			}
			if(type.isIngot() && !GEAR_BLACKLIST.contains(name) && !configToGearBlacklist.contains(name)) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String gearOredict = miscHelper.getOredictName("gear", name);
				if(oredict.contains(gearOredict)) {
					helper.registerGregTechAddonRecipe(
							miscHelper.getRecipeKey("gregtech_addon.material_to_gear_extruding", name),
							helper.recipeSettings(RecipeMaps.EXTRUDING).shaped(true).
							input(materialOredict, 4).
							input(GT_Items.Shape_Extruder_Gear.get(0), 0).
							output(gearOredict, 1).
							energy(128).time(500));
					helper.registerGregTechAddonRecipe(
							miscHelper.getRecipeKey("gregtech_addon.material_to_gear_alloy_smelting", name),
							helper.recipeSettings(RecipeMaps.ALLOY_SMELTING).
							input(materialOredict, 8).
							input(GT_Items.Shape_Mold_Gear.get(0), 0).
							output(gearOredict, 1).
							energy(32).time(1000));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToStickBlacklist.contains(name)) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String stickOredict = miscHelper.getOredictName("stick", name);
				String smallDustOredict = miscHelper.getOredictName("dustSmall", name);
				if(oredict.contains(stickOredict)) {
					GregTechAddonRecipeSettings<?> settings = helper.recipeSettings(RecipeMaps.LATHE).
							input(materialOredict, 1).
							output(stickOredict, 1).
							energy(16).time(500);
					if(oredict.contains(smallDustOredict)) {
						settings.output(smallDustOredict, 2);
					}
					helper.registerGregTechAddonRecipe(
							miscHelper.getRecipeKey("gregtech_addon.material_to_stick_lathe", name),
							settings);
					helper.registerGregTechAddonRecipe(
							miscHelper.getRecipeKey("gregtech_addon.material_to_stick_extruding", name),
							helper.recipeSettings(RecipeMaps.EXTRUDING).shaped(true).
							input(materialOredict, 1).
							input(GT_Items.Shape_Extruder_Rod.get(0), 0).
							output(stickOredict, 2).
							energy(96).time(200));
				}
			}
			if(!BLACKLIST.contains(name) && !configToPlateBlacklist.contains(name)) {
				String blockOredict = miscHelper.getOredictName("block", name);
				String plateOredict = miscHelper.getOredictName("plate", name);
				if(oredict.contains(blockOredict) && oredict.contains(plateOredict)) {
					helper.registerGregTechAddonRecipe(
							miscHelper.getRecipeKey("gregtech_addon.block_to_plate", name),
							helper.recipeSettings(RecipeMaps.CUTTING).
							input(blockOredict, 1).
							output(plateOredict, 9).
							energy(30).time(1000));
				}
			}
			if(!BLACKLIST.contains(name) && !configToDensePlateBlacklist.contains(name)) {
				String plateOredict = miscHelper.getOredictName("plate", name);
				String densePlateOredict = miscHelper.getOredictName("plateDense", name);
				if(oredict.contains(plateOredict) && oredict.contains(densePlateOredict)) {
					helper.registerGregTechAddonRecipe(
							miscHelper.getRecipeKey("gregtech_addon.plate_to_dense_plate", name),
							helper.recipeSettings(RecipeMaps.BENDING).
							input(plateOredict, 9).
							input(GT_Items.Circuit_Integrated.getWithDamage(0, 9), 0).
							output(densePlateOredict, 1).
							energy(24).time(1800));
				}
			}
			if(!GEAR_BLACKLIST.contains(name) && !configToGearBlacklist.contains(name)) {
				String plateOredict = miscHelper.getOredictName("plate", name);
				String stickOredict = miscHelper.getOredictName("stick", name);
				String gearOredict = miscHelper.getOredictName("gear", name);
				if(oredict.contains(plateOredict) && oredict.contains(stickOredict) && oredict.contains(gearOredict)) {
					api.registerShapedRecipe(
							miscHelper.getRecipeKey("gregtech_addon.plate_stick_to_gear", name),
							gearOredict, 1, new Object[] {
									"SPS", "PTP", "SPS",
									'P', plateOredict,
									'S', stickOredict,
									'T', "craftingToolWrench",
							});
				}
			}
			if(!type.isDust() && !BLACKLIST.contains(name) && !configToDustBlacklist.contains(name)) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String dustOredict = miscHelper.getOredictName("dust", name);
				if(oredict.contains(dustOredict)) {
					if(Loader.isModLoaded("Railcraft") && rockCrusher) {
						rcHelper.registerRockCrusherRecipe(
								miscHelper.getRecipeKey("gregtech_addon.material_to_dust_rock_crusher", name),
								materialOredict, 1, new Object[] {
										dustOredict, 1, 1F,
								});
					}
				}
			}
			if(!BLACKLIST.contains(name) && !configToDustBlacklist.contains(name)) {
				String blockOredict = miscHelper.getOredictName("block", name);
				String dustOredict = miscHelper.getOredictName("dust", name);
				if(oredict.contains(blockOredict) && oredict.contains(dustOredict)) {
					ic2Helper.registerMaceratorRecipe(
							miscHelper.getRecipeKey("gregtech_addon.block_to_dust_macerator", name),
							blockOredict, 1, dustOredict, 9);
					if(Loader.isModLoaded("Railcraft") && rockCrusher) {
						rcHelper.registerRockCrusherRecipe(
								miscHelper.getRecipeKey("gregtech_addon.block_to_dust_rock_crusher", name),
								blockOredict, 1, new Object[] {
										dustOredict, 9, 1F,
								});
					}
					if(Loader.isModLoaded("ThermalExpansion") && pulverizer) {
						teHelper.registerPulverizerRecipe(
								miscHelper.getRecipeKey("gregtech_addon.block_to_dust_pulverizer", name),
								blockOredict, 1, dustOredict, 9, 2400);
					}
				}
			}
			if(!BLACKLIST.contains(name) && !configToDustBlacklist.contains(name)) {
				String nuggetOredict = miscHelper.getOredictName("nugget", name);
				String tinyDustOredict = miscHelper.getOredictName("dustTiny", name);
				if(oredict.contains(nuggetOredict) && oredict.contains(tinyDustOredict)) {
					ic2Helper.registerMaceratorRecipe(
							miscHelper.getRecipeKey("gregtech_addon.nugget_to_tiny_dust_macerator", name),
							nuggetOredict, 1, tinyDustOredict, 1);
					if(Loader.isModLoaded("Railcraft") && rockCrusher) {
						rcHelper.registerRockCrusherRecipe(
								miscHelper.getRecipeKey("gregtech_addon.nugget_to_tiny_dust_rock_crusher", name),
								nuggetOredict, 1, new Object[] {
										tinyDustOredict, 1, 1F,
								});
					}
					if(Loader.isModLoaded("ThermalExpansion") && pulverizer) {
						teHelper.registerPulverizerRecipe(
								miscHelper.getRecipeKey("gregtech_addon.nugget_to_tiny_dust_pulverizer", name),
								nuggetOredict, 1, tinyDustOredict, 1, 2400);
					}
				}
			}
			if(!BLACKLIST.contains(name) && !configToDustBlacklist.contains(name)) {
				String plateOredict = miscHelper.getOredictName("plate", name);
				String dustOredict = miscHelper.getOredictName("dust", name);
				if(oredict.contains(plateOredict) && oredict.contains(dustOredict)) {
					ic2Helper.registerMaceratorRecipe(
							miscHelper.getRecipeKey("gregtech_addon.plate_to_dust_macerator", name),
							plateOredict, 1, dustOredict, 1);
					if(Loader.isModLoaded("Railcraft") && rockCrusher) {
						rcHelper.registerRockCrusherRecipe(
								miscHelper.getRecipeKey("gregtech_addon.plate_to_dust_rock_crusher", name),
								plateOredict, 1, new Object[] {
										dustOredict, 1, 1F,
								});
					}
					if(Loader.isModLoaded("ThermalExpansion") && pulverizer) {
						teHelper.registerPulverizerRecipe(
								miscHelper.getRecipeKey("gregtech_addon.plate_to_dust_pulverizer", name),
								plateOredict, 1, dustOredict, 1, 2400);
					}
				}
			}
			if(!BLACKLIST.contains(name) && !configToDustBlacklist.contains(name)) {
				String densePlateOredict = miscHelper.getOredictName("plateDense", name);
				String dustOredict = miscHelper.getOredictName("dust", name);
				if(oredict.contains(densePlateOredict) && oredict.contains(dustOredict)) {
					ic2Helper.registerMaceratorRecipe(
							miscHelper.getRecipeKey("gregtech_addon.dense_plate_to_dust_macerator", name),
							densePlateOredict, 1, dustOredict, 9);
					if(Loader.isModLoaded("Railcraft") && rockCrusher) {
						rcHelper.registerRockCrusherRecipe(
								miscHelper.getRecipeKey("gregtech_addon.dense_plate_to_dust_rock_crusher", name),
								densePlateOredict, 1, new Object[] {
										dustOredict, 9, 1F,
								});
					}
					if(Loader.isModLoaded("ThermalExpansion") && pulverizer) {
						teHelper.registerPulverizerRecipe(
								miscHelper.getRecipeKey("gregtech_addon.dense_plate_to_dust_pulverizer", name),
								densePlateOredict, 1, dustOredict, 9, 2400);
					}
				}
			}
			if(!BLACKLIST.contains(name) && !configToDustBlacklist.contains(name)) {
				String gearOredict = miscHelper.getOredictName("gear", name);
				String dustOredict = miscHelper.getOredictName("dust", name);
				if(oredict.contains(gearOredict) && oredict.contains(dustOredict)) {
					ic2Helper.registerMaceratorRecipe(
							miscHelper.getRecipeKey("gregtech_addon.gear_to_dust_macerator", name),
							gearOredict, 1, dustOredict, 4);
					if(Loader.isModLoaded("Railcraft") && rockCrusher) {
						rcHelper.registerRockCrusherRecipe(
								miscHelper.getRecipeKey("gregtech_addon.gear_to_dust_rock_crusher", name),
								gearOredict, 1, new Object[] {
										dustOredict, 4, 1F,
								});
					}
					if(Loader.isModLoaded("ThermalExpansion") && pulverizer) {
						teHelper.registerPulverizerRecipe(
								miscHelper.getRecipeKey("gregtech_addon.gear_to_dust_pulverizer", name),
								gearOredict, 1, dustOredict, 4, 2400);
					}
				}
			}
			if(!BLACKLIST.contains(name) && !configToDustBlacklist.contains(name)) {
				String stickOredict = miscHelper.getOredictName("stick", name);
				String smallDustOredict = miscHelper.getOredictName("dustSmall", name);
				if(oredict.contains(stickOredict) && oredict.contains(smallDustOredict)) {
					ic2Helper.registerMaceratorRecipe(
							miscHelper.getRecipeKey("gregtech_addon.stick_to_small_dust_macerator", name),
							stickOredict, 1, smallDustOredict, 2);
					if(Loader.isModLoaded("Railcraft") && rockCrusher) {
						rcHelper.registerRockCrusherRecipe(
								miscHelper.getRecipeKey("gregtech_addon.stick_to_small_dust_rock_crusher", name),
								stickOredict, 1, new Object[] {
										smallDustOredict, 2, 1F,
								});
					}
					if(Loader.isModLoaded("ThermalExpansion") && pulverizer) {
						teHelper.registerPulverizerRecipe(
								miscHelper.getRecipeKey("gregtech_addon.stick_to_small_dust_pulverizer", name),
								stickOredict, 1, smallDustOredict, 2, 2400);
					}
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToMaterialBlacklist.contains(name)) {
				String nuggetOredict = miscHelper.getOredictName("nugget", name);
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				if(oredict.contains(nuggetOredict)) {
					helper.registerGregTechAddonRecipe(
							miscHelper.getRecipeKey("gregtech_addon.nugget_to_material", name),
							helper.recipeSettings(RecipeMaps.ALLOY_SMELTING).
							input(nuggetOredict, 9).
							output(materialOredict, 1).
							energy(2).time(200));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToMaterialBlacklist.contains(name)) {
				String smallDustOredict = miscHelper.getOredictName("dustSmall", name);
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				if(oredict.contains(smallDustOredict)) {
					helper.registerGregTechAddonRecipe(
							miscHelper.getRecipeKey("gregtech_addon.small_dust_to_material_alloy_smelting", name),
							helper.recipeSettings(RecipeMaps.ALLOY_SMELTING).
							input(smallDustOredict, 4).
							output(materialOredict, 1).
							energy(3).time(130));
					if(Loader.isModLoaded("ThermalExpansion") && smelter) {
						teHelper.registerSmelterRecipe(
								miscHelper.getRecipeKey("gregtech_addon.small_dust_to_material_smelter", name),
								smallDustOredict, 4, Blocks.sand, 1, materialOredict, 1, 800);
					}
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToMaterialBlacklist.contains(name)) {
				String tinyDustOredict = miscHelper.getOredictName("dustTiny", name);
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				if(oredict.contains(tinyDustOredict)) {
					helper.registerGregTechAddonRecipe(
							miscHelper.getRecipeKey("gregtech_addon.tiny_dust_to_material_alloy_smelting", name),
							helper.recipeSettings(RecipeMaps.ALLOY_SMELTING).
							input(tinyDustOredict, 9).
							output(materialOredict, 1).
							energy(3).time(130));
					if(Loader.isModLoaded("ThermalExpansion") && smelter) {
						teHelper.registerSmelterRecipe(
								miscHelper.getRecipeKey("gregtech_addon.tiny_dust_to_material_smelter", name),
								tinyDustOredict, 9, Blocks.sand, 1, materialOredict, 1, 800);
					}
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToMaterialBlacklist.contains(name)) {
				String blockOredict = miscHelper.getOredictName("block", name);
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				if(oredict.contains(blockOredict)) {
					helper.registerGregTechAddonRecipe(
							miscHelper.getRecipeKey("gregtech_addon.block_to_material_alloy_smelting", name),
							helper.recipeSettings(RecipeMaps.ALLOY_SMELTING).
							input(blockOredict, 1).
							output(materialOredict, 9).
							energy(3).time(160));
					if(Loader.isModLoaded("ThermalExpansion") && smelter) {
						teHelper.registerSmelterRecipe(
								miscHelper.getRecipeKey("gregtech_addon.block_to_material_smelter", name),
								blockOredict, 1, Blocks.sand, 1, materialOredict, 9, 800);
					}
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToMaterialBlacklist.contains(name)) {
				String plateOredict = miscHelper.getOredictName("plate", name);
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				if(oredict.contains(plateOredict)) {
					helper.registerGregTechAddonRecipe(
							miscHelper.getRecipeKey("gregtech_addon.plate_to_material_alloy_smelting", name),
							helper.recipeSettings(RecipeMaps.ALLOY_SMELTING).
							input(plateOredict, 1).
							output(materialOredict, 1).
							energy(3).time(160));
					if(Loader.isModLoaded("ThermalExpansion") && smelter) {
						teHelper.registerSmelterRecipe(
								miscHelper.getRecipeKey("gregtech_addon.plate_to_material_smelter", name),
								plateOredict, 1, Blocks.sand, 1, materialOredict, 1, 800);
					}
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToMaterialBlacklist.contains(name)) {
				String densePlateOredict = miscHelper.getOredictName("plateDense", name);
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				if(oredict.contains(densePlateOredict)) {
					helper.registerGregTechAddonRecipe(
							miscHelper.getRecipeKey("gregtech_addon.dense_plate_to_material_alloy_smelting", name),
							helper.recipeSettings(RecipeMaps.ALLOY_SMELTING).
							input(densePlateOredict, 1).
							output(materialOredict, 9).
							energy(3).time(160));
					if(Loader.isModLoaded("ThermalExpansion") && smelter) {
						teHelper.registerSmelterRecipe(
								miscHelper.getRecipeKey("gregtech_addon.dense_plate_to_material_smelter", name),
								densePlateOredict, 1, Blocks.sand, 1, materialOredict, 9, 800);
					}
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToMaterialBlacklist.contains(name)) {
				String gearOredict = miscHelper.getOredictName("gear", name);
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				if(oredict.contains(gearOredict)) {
					helper.registerGregTechAddonRecipe(
							miscHelper.getRecipeKey("gregtech_addon.gear_to_material_alloy_smelting", name),
							helper.recipeSettings(RecipeMaps.ALLOY_SMELTING).
							input(gearOredict, 1).
							output(materialOredict, 4).
							energy(3).time(160));
					if(Loader.isModLoaded("ThermalExpansion") && smelter) {
						teHelper.registerSmelterRecipe(
								miscHelper.getRecipeKey("gregtech_addon.gear_to_material_smelter", name),
								gearOredict, 1, Blocks.sand, 1, materialOredict, 4, 800);
					}
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToMaterialBlacklist.contains(name)) {
				String stickOredict = miscHelper.getOredictName("stick", name);
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				if(oredict.contains(stickOredict)) {
					helper.registerGregTechAddonRecipe(
							miscHelper.getRecipeKey("gregtech_addon.stick_to_material_alloy_smelting", name),
							helper.recipeSettings(RecipeMaps.ALLOY_SMELTING).
							input(stickOredict, 2).
							output(materialOredict, 1).
							energy(3).time(160));
					if(Loader.isModLoaded("ThermalExpansion") && smelter) {
						teHelper.registerSmelterRecipe(
								miscHelper.getRecipeKey("gregtech_addon.stick_to_material_smelter", name),
								stickOredict, 2, Blocks.sand, 1, materialOredict, 1, 800);
					}
				}
			}
		}
	}
}
