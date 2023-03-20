package thelm.jaopca.gt5.compat.gregtech;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.TreeSet;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.util.GT_Recipe;
import ic2.core.Ic2Items;
import net.minecraft.init.Blocks;
import net.minecraftforge.fluids.FluidRegistry;
import thelm.jaopca.api.JAOPCAApi;
import thelm.jaopca.api.config.IDynamicSpecConfig;
import thelm.jaopca.api.helpers.IMiscHelper;
import thelm.jaopca.api.materials.IMaterial;
import thelm.jaopca.api.materials.MaterialType;
import thelm.jaopca.api.modules.IModule;
import thelm.jaopca.api.modules.IModuleData;
import thelm.jaopca.api.modules.JAOPCAModule;
import thelm.jaopca.compat.railcraft.RailcraftHelper;
import thelm.jaopca.gt5.compat.gregtech.recipes.GregTechRecipeSettings;
import thelm.jaopca.gt5.compat.immersiveengineering.ImmersiveEngineeringHelper;
import thelm.jaopca.utils.ApiImpl;
import thelm.jaopca.utils.MiscHelper;

@JAOPCAModule(modDependencies = "gregtech")
public class GregTechCompatModule implements IModule {

	static final Set<String> BLACKLIST = GregTechModule.BLACKLIST;
	static final Set<String> GEAR_BLACKLIST = new TreeSet<>();

	static {
		Arrays.stream(Materials.values()).filter(m->(m.mTypes & 128) != 0).forEach(m->BLACKLIST.add(m.mName));
	}

	private static Set<String> configAutoclaveToCrystalBlacklist = new TreeSet<>();
	private static Set<String> configImplosionToCrystalBlacklist = new TreeSet<>();
	private static Set<String> configToMaterialBlacklist = new TreeSet<>();
	private static Set<String> configToDustBlacklist = new TreeSet<>();
	private static Set<String> configToBlockBlacklist = new TreeSet<>();
	private static Set<String> configToNuggetBlacklist = new TreeSet<>();
	private static Set<String> configToPlateBlacklist = new TreeSet<>();
	private static Set<String> configToDensePlateBlacklist = new TreeSet<>();
	private static Set<String> configToGearBlacklist = new TreeSet<>();
	private static Set<String> configToStickBlacklist = new TreeSet<>();
	private static Set<String> configSmallDustBoxingBlacklist = new TreeSet<>();
	private static Set<String> configTinyDustBoxingBlacklist = new TreeSet<>();
	private static Set<String> configMortarToDustBlacklist = new TreeSet<>();
	private static Set<String> configRecyclingBlacklist = new TreeSet<>();

	private static boolean ie = true;
	private static boolean rc = true;

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
		ie = config.getDefinedBoolean("recipes.ie", ie, "Should the module add Immersive Engineering recipes.");
		rc = config.getDefinedBoolean("recipes.rc", rc, "Should the module add Railcraft recipes.");
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
				config.getDefinedStringList("recipes.toDustMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configToDustBlacklist);
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
				config.getDefinedStringList("recipes.smallDustBoxingMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configSmallDustBoxingBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.tinyDustBoxingMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configTinyDustBoxingBlacklist);
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
		ImmersiveEngineeringHelper ieHelper = ImmersiveEngineeringHelper.INSTANCE;
		RailcraftHelper rcHelper = RailcraftHelper.INSTANCE;
		IMiscHelper miscHelper = MiscHelper.INSTANCE;
		Set<String> oredict = api.getOredict();
		for(IMaterial material : moduleData.getMaterials()) {
			MaterialType type = material.getType();
			String name = material.getName();
			if(type.isCrystalline() && !BLACKLIST.contains(name) && !configAutoclaveToCrystalBlacklist.contains(name)) {
				String dustOredict = miscHelper.getOredictName("dust", name);
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				if(oredict.contains(dustOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.dust_to_material_autoclave_water", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sAutoclaveRecipes).
							input(dustOredict, 1).
							fluidInput(FluidRegistry.WATER, 200).
							output(materialOredict, 1, 9000).
							time(2000).energy(24));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.dust_to_material_autoclave_distilled_water", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sAutoclaveRecipes).
							input(dustOredict, 1).
							fluidInput("ic2distilledwater", 200).
							output(materialOredict, 1, 9500).
							time(1500).energy(24));
				}
			}
			if(type.isCrystalline() && !BLACKLIST.contains(name) && !configImplosionToCrystalBlacklist.contains(name)) {
				String dustOredict = miscHelper.getOredictName("dust", name);
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				if(oredict.contains(dustOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.dust_to_material_implosion_powderbarrel", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sImplosionRecipes).
							input(dustOredict, 4).
							input(ItemList.Block_Powderbarrel.get(1), 48).
							output(materialOredict, 3).
							output("dustTinyDarkAsh", 12).
							time(20).energy(30));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.dust_to_material_implosion_dynamite", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sImplosionRecipes).
							input(dustOredict, 4).
							input(Ic2Items.dynamite, 12).
							output(materialOredict, 3).
							output("dustTinyDarkAsh", 12).
							time(20).energy(30));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.dust_to_material_implosion_tnt", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sImplosionRecipes).
							input(dustOredict, 4).
							input(Blocks.tnt, 12).
							output(materialOredict, 3).
							output("dustTinyDarkAsh", 12).
							time(20).energy(30));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.dust_to_material_implosion_industrial_tnt", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sImplosionRecipes).
							input(dustOredict, 4).
							input(Ic2Items.industrialTnt, 12).
							output(materialOredict, 3).
							output("dustTinyDarkAsh", 12).
							time(20).energy(30));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configMortarToDustBlacklist.contains(name)) {
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
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToBlockBlacklist.contains(name)) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String blockOredict = miscHelper.getOredictName("block", name);
				if(oredict.contains(blockOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_block_extruder", name),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sExtruderRecipes).
							input(materialOredict, 9).
							input(ItemList.Shape_Extruder_Block.get(0), 0).
							output(blockOredict, 1).
							time(10).energy(128));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_block_alloy_smelter", name),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sAlloySmelterRecipes).
							input(materialOredict, 9).
							input(ItemList.Shape_Mold_Block.get(0), 0).
							output(blockOredict, 1).
							time(5).energy(64));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToBlockBlacklist.contains(name)) {
				String nuggetOredict = miscHelper.getOredictName("nugget", name);
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				if(oredict.contains(nuggetOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.nugget_to_material", name),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sAlloySmelterRecipes).
							input(nuggetOredict, 9).
							input(ItemList.Shape_Mold_Ingot.get(0), 0).
							output(materialOredict, 1).
							time(200).energy(2));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToNuggetBlacklist.contains(name)) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String nuggetOredict = miscHelper.getOredictName("nugget", name);
				if(oredict.contains(nuggetOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_nugget", name),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sAlloySmelterRecipes).
							input(materialOredict, 1).
							input(ItemList.Shape_Mold_Nugget.get(0), 0).
							output(nuggetOredict, 9).
							time(100).energy(1));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToPlateBlacklist.contains(name)) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String plateOredict = miscHelper.getOredictName("plate", name);
				if(oredict.contains(plateOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_plate_hammer", name),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sHammerRecipes).
							input(materialOredict, 2).
							output(plateOredict, 1).
							time(100).energy(16));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_plate_bender", name),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sBenderRecipes).
							input(materialOredict, 1).
							input(ItemList.Circuit_Integrated.getWithDamage(0, 1), 0).
							output(plateOredict, 1).
							time(200).energy(24));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_plate_extruding", name),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sExtruderRecipes).
							input(materialOredict, 1).
							input(ItemList.Shape_Extruder_Plate.get(0), 0).
							output(plateOredict, 1).
							time(100).energy(128));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_plate_alloy_smelter", name),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sAlloySmelterRecipes).
							input(materialOredict, 2).
							input(ItemList.Shape_Mold_Plate.get(0), 0).
							output(plateOredict, 1).
							time(200).energy(32));
					api.registerShapedRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_plate_hard_hammer", name),
							plateOredict, 1, new Object[] {
									"h", "X", "X",
									'X', materialOredict,
									'h', "craftingToolHardHammer",
							});
					api.registerShapedRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_plate_forge_hammer", name),
							plateOredict, 1, new Object[] {
									"H", "X",
									'X', materialOredict,
									'H', "craftingToolForgeHammer",
							});
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToDensePlateBlacklist.contains(name)) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String densePlateOredict = miscHelper.getOredictName("plateDense", name);
				if(oredict.contains(densePlateOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_dense_plate", name),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sBenderRecipes).
							input(materialOredict, 9).
							input(ItemList.Circuit_Integrated.getWithDamage(0, 9), 0).
							output(densePlateOredict, 1).
							time(1800).energy(24));
				}
			}
			if(!BLACKLIST.contains(name) && !configToDensePlateBlacklist.contains(name)) {
				String plateOredict = miscHelper.getOredictName("plate", name);
				String densePlateOredict = miscHelper.getOredictName("plateDense", name);
				if(oredict.contains(plateOredict) && oredict.contains(densePlateOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.plate_to_dense_plate", name),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sBenderRecipes).
							input(plateOredict, 9).
							input(ItemList.Circuit_Integrated.getWithDamage(0, 9), 0).
							output(densePlateOredict, 1).
							time(1800).energy(24));
				}
			}
			if(!type.isIngot() && !BLACKLIST.contains(name) && !configMortarToDustBlacklist.contains(name)) {
				String plateOredict = miscHelper.getOredictName("plate", name);
				String dustOredict = miscHelper.getOredictName("dust", name);
				if(oredict.contains(dustOredict)) {
					api.registerShapedRecipe(
							miscHelper.getRecipeKey("gregtech.plate_to_dust_mortar", material.getName()),
							dustOredict, 1, new Object[] {
									"X", "m",
									'X', plateOredict,
									'm', "craftingToolMortar",
							});
				}
			}
			if(type.isIngot() && !GEAR_BLACKLIST.contains(name) && !configToGearBlacklist.contains(name)) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String gearOredict = miscHelper.getOredictName("gear", name);
				if(oredict.contains(gearOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_gear_extruder", name),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sExtruderRecipes).
							input(materialOredict, 4).
							input(ItemList.Shape_Extruder_Gear.get(0), 0).
							output(gearOredict, 1).
							time(500).energy(128));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_gear_alloy_smelter", name),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sAlloySmelterRecipes).
							input(materialOredict, 8).
							input(ItemList.Shape_Mold_Gear.get(0), 0).
							output(gearOredict, 1).
							time(1000).energy(32));
				}
			}
			if(!GEAR_BLACKLIST.contains(name) && !configToGearBlacklist.contains(name)) {
				String plateOredict = miscHelper.getOredictName("plate", name);
				String stickOredict = miscHelper.getOredictName("stick", name);
				String gearOredict = miscHelper.getOredictName("gear", name);
				if(oredict.contains(plateOredict) && oredict.contains(stickOredict) && oredict.contains(gearOredict)) {
					api.registerShapedRecipe(
							miscHelper.getRecipeKey("gregtech.plate_stick_to_gear", name),
							gearOredict, 1, new Object[] {
									"SPS", "PwP", "SPS",
									'P', plateOredict,
									'S', stickOredict,
									'w', "craftingToolWrench",
							});
				}
			}
			if(!type.isDust() && !BLACKLIST.contains(name) && !configToStickBlacklist.contains(name)) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String stickOredict = miscHelper.getOredictName("stick", name);
				String smallDustOredict = miscHelper.getOredictName("dustSmall", name);
				if(oredict.contains(stickOredict)) {
					GregTechRecipeSettings settings = helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sLatheRecipes).
							input(materialOredict, 1).
							output(stickOredict, 1).
							time(500).energy(16);
					if(oredict.contains(smallDustOredict)) {
						settings.output(smallDustOredict, 2);
					}
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_stick_lathe", name),
							settings);
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToStickBlacklist.contains(name)) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String stickOredict = miscHelper.getOredictName("stick", name);
				if(oredict.contains(stickOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_stick_extruder", name),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sExtruderRecipes).
							input(materialOredict, 1).
							input(ItemList.Shape_Extruder_Rod.get(0), 0).
							output(stickOredict, 2).
							time(200).energy(96));
					api.registerShapedRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_stick_file", material.getName()),
							stickOredict, 1, new Object[] {
									"f ", " X",
									'X', materialOredict,
									'f', "craftingToolFile",
							});
				}
			}
			if(!BLACKLIST.contains(name) && !configSmallDustBoxingBlacklist.contains(name)) {
				String smallDustOredict = miscHelper.getOredictName("dustSmall", name);
				String dustOredict = miscHelper.getOredictName("dust", name);
				if(oredict.contains(smallDustOredict) && oredict.contains(dustOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.small_dust_to_dust", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sBoxinatorRecipes).
							input(smallDustOredict, 4).
							input(ItemList.Schematic_Dust.get(0), 0).
							output(dustOredict, 1));
				}
			}
			if(!BLACKLIST.contains(name) && !configTinyDustBoxingBlacklist.contains(name)) {
				String tinyDustOredict = miscHelper.getOredictName("dustTiny", name);
				String dustOredict = miscHelper.getOredictName("dust", name);
				if(oredict.contains(tinyDustOredict) && oredict.contains(dustOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.tiny_dust_to_dust", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sBoxinatorRecipes).
							input(tinyDustOredict, 9).
							input(ItemList.Schematic_Dust.get(0), 0).
							output(dustOredict, 1));
				}
			}
			if(!type.isDust() && !BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				String dustOredict = miscHelper.getOredictName("dust", name);
				if(oredict.contains(dustOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.material_to_dust_macerator", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sMaceratorRecipes).
							input(materialOredict, 1).
							output(dustOredict, 1).
							time(100).energy(4));
					if(Loader.isModLoaded("Railcraft") && rc) {
						rcHelper.registerRockCrusherRecipe(
								miscHelper.getRecipeKey("gregtech.material_to_dust_rock_crusher", name),
								materialOredict, 1, new Object[] {
										dustOredict, 1, 1F,
								});
					}
				}
			}
			if(!BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				String blockOredict = miscHelper.getOredictName("block", name);
				String dustOredict = miscHelper.getOredictName("dust", name);
				if(oredict.contains(blockOredict) && oredict.contains(dustOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.block_to_dust_macerator", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sMaceratorRecipes).
							input(blockOredict, 1).
							output(dustOredict, (material.isSmallStorageBlock() ? 4 : 9)).
							time(100*(material.isSmallStorageBlock() ? 4 : 9)).energy(4));
					if(Loader.isModLoaded("ImmersiveEngineering") && ie) {
						ieHelper.registerCrusherRecipe(
								miscHelper.getRecipeKey("gregtech.block_to_dust_crusher", name),
								blockOredict, new Object[] {
										dustOredict, (material.isSmallStorageBlock() ? 4 : 9),
								}, 6000);
					}
					if(Loader.isModLoaded("Railcraft") && rc) {
						rcHelper.registerRockCrusherRecipe(
								miscHelper.getRecipeKey("gregtech.block_to_dust_rock_crusher", name),
								blockOredict, 1, new Object[] {
										dustOredict, 9, 1F,
								});
					}
				}
			}
			if(!BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				String nuggetOredict = miscHelper.getOredictName("nugget", name);
				String tinyDustOredict = miscHelper.getOredictName("dustTiny", name);
				if(oredict.contains(nuggetOredict) && oredict.contains(tinyDustOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.nugget_to_tiny_dust_macerator", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sMaceratorRecipes).
							input(nuggetOredict, 1).
							output(tinyDustOredict, 1).
							time(11).energy(4));
					if(Loader.isModLoaded("ImmersiveEngineering") && ie) {
						ieHelper.registerCrusherRecipe(
								miscHelper.getRecipeKey("gregtech.nugget_to_tiny_dust_crusher", name),
								nuggetOredict, new Object[] {
										tinyDustOredict, 1,
								}, 6000);
					}
					if(Loader.isModLoaded("Railcraft") && rc) {
						rcHelper.registerRockCrusherRecipe(
								miscHelper.getRecipeKey("gregtech.nugget_to_tiny_dust_rock_crusher", name),
								nuggetOredict, 1, new Object[] {
										tinyDustOredict, 1, 1F,
								});
					}
				}
			}
			if(!BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				String plateOredict = miscHelper.getOredictName("plate", name);
				String dustOredict = miscHelper.getOredictName("dust", name);
				if(oredict.contains(plateOredict) && oredict.contains(dustOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.plate_to_dust_macerator", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sMaceratorRecipes).
							input(plateOredict, 1).
							output(dustOredict, 1).
							time(100).energy(4));
					if(Loader.isModLoaded("ImmersiveEngineering") && ie) {
						ieHelper.registerCrusherRecipe(
								miscHelper.getRecipeKey("gregtech.plate_to_dust_crusher", name),
								plateOredict, new Object[] {
										dustOredict, 1,
								}, 6000);
					}
					if(Loader.isModLoaded("Railcraft") && rc) {
						rcHelper.registerRockCrusherRecipe(
								miscHelper.getRecipeKey("gregtech.plate_to_dust_rock_crusher", name),
								plateOredict, 1, new Object[] {
										dustOredict, 1, 1F,
								});
					}
				}
			}
			if(!BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				String densePlateOredict = miscHelper.getOredictName("plateDense", name);
				String dustOredict = miscHelper.getOredictName("dust", name);
				if(oredict.contains(densePlateOredict) && oredict.contains(dustOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.dense_plate_to_dust_macerator", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sMaceratorRecipes).
							input(densePlateOredict, 1).
							output(dustOredict, 9).
							time(900).energy(4));
					if(Loader.isModLoaded("ImmersiveEngineering") && ie) {
						ieHelper.registerCrusherRecipe(
								miscHelper.getRecipeKey("gregtech.dense_plate_to_dust_crusher", name),
								densePlateOredict, new Object[] {
										dustOredict, 9,
								}, 6000);
					}
					if(Loader.isModLoaded("Railcraft") && rc) {
						rcHelper.registerRockCrusherRecipe(
								miscHelper.getRecipeKey("gregtech.dense_plate_to_dust_rock_crusher", name),
								densePlateOredict, 1, new Object[] {
										dustOredict, 9, 1F,
								});
					}
				}
			}
			if(!GEAR_BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				String gearOredict = miscHelper.getOredictName("gear", name);
				String dustOredict = miscHelper.getOredictName("dust", name);
				if(oredict.contains(gearOredict) && oredict.contains(dustOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.gear_to_dust_macerator", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sMaceratorRecipes).
							input(gearOredict, 1).
							output(dustOredict, 4).
							time(400).energy(4));
					if(Loader.isModLoaded("ImmersiveEngineering") && ie) {
						ieHelper.registerCrusherRecipe(
								miscHelper.getRecipeKey("gregtech.gear_to_dust_crusher", name),
								gearOredict, new Object[] {
										dustOredict, 4,
								}, 6000);
					}
					if(Loader.isModLoaded("Railcraft") && rc) {
						rcHelper.registerRockCrusherRecipe(
								miscHelper.getRecipeKey("gregtech.gear_to_dust_rock_crusher", name),
								gearOredict, 1, new Object[] {
										dustOredict, 4, 1F,
								});
					}
				}
			}
			if(!BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				String stickOredict = miscHelper.getOredictName("stick", name);
				String smallDustOredict = miscHelper.getOredictName("dustSmall", name);
				if(oredict.contains(stickOredict) && oredict.contains(smallDustOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.stick_to_small_dust_macerator", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sMaceratorRecipes).
							input(stickOredict, 1).
							output(smallDustOredict, 2).
							time(50).energy(4));
					if(Loader.isModLoaded("ImmersiveEngineering") && ie) {
						ieHelper.registerCrusherRecipe(
								miscHelper.getRecipeKey("gregtech.stick_to_small_dust_crusher", name),
								stickOredict, new Object[] {
										smallDustOredict, 2,
								}, 6000);
					}
					if(Loader.isModLoaded("Railcraft") && rc) {
						rcHelper.registerRockCrusherRecipe(
								miscHelper.getRecipeKey("gregtech.stick_to_small_dust_rock_crusher", name),
								stickOredict, 1, new Object[] {
										smallDustOredict, 2, 1F,
								});
					}
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				String plateOredict = miscHelper.getOredictName("plate", name);
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				if(oredict.contains(plateOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.plate_to_material_alloy_smelter", name),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sAlloySmelterRecipes).
							input(plateOredict, 1).
							input(ItemList.Shape_Mold_Ingot.get(0), 0).
							output(materialOredict, 1).
							time(130).energy(3));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.plate_to_material_arc_oxygen", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sArcFurnaceRecipes).
							input(plateOredict, 1).
							fluidInput(Materials.Oxygen.mGas, 100).
							output(materialOredict, 1).
							time(100).energy(96));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.plate_to_material_arc_argon", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sPlasmaArcFurnaceRecipes).
							input(plateOredict, 1).
							fluidInput(Materials.Argon.mPlasma, 1).
							output(materialOredict, 1).
							fluidOutput(Materials.Argon.mFluid, 1).
							time(6).energy(32));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.plate_to_material_arc_nitrogen", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sPlasmaArcFurnaceRecipes).
							input(plateOredict, 1).
							fluidInput(Materials.Nitrogen.mPlasma, 1).
							output(materialOredict, 1).
							fluidOutput(Materials.Nitrogen.mFluid, 1).
							time(6).energy(32));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				String densePlateOredict = miscHelper.getOredictName("plateDense", name);
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				if(oredict.contains(densePlateOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.dense_plate_to_material_alloy_smelter", name),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sAlloySmelterRecipes).
							input(densePlateOredict, 1).
							input(ItemList.Shape_Mold_Ingot.get(0), 0).
							output(materialOredict, 9).
							time(130).energy(3));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.dense_plate_to_material_arc_oxygen", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sArcFurnaceRecipes).
							input(densePlateOredict, 1).
							fluidInput(Materials.Oxygen.mGas, 900).
							output(materialOredict, 9).
							time(900).energy(96));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.dense_plate_to_material_arc_argon", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sPlasmaArcFurnaceRecipes).
							input(densePlateOredict, 1).
							fluidInput(Materials.Argon.mPlasma, 1).
							output(materialOredict, 9).
							fluidOutput(Materials.Argon.mFluid, 1).
							time(56).energy(32));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.dense_plate_to_material_arc_nitrogen", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sPlasmaArcFurnaceRecipes).
							input(densePlateOredict, 1).
							fluidInput(Materials.Nitrogen.mPlasma, 1).
							output(materialOredict, 9).
							fluidOutput(Materials.Nitrogen.mFluid, 1).
							time(56).energy(32));
				}
			}
			if(type.isIngot() && !GEAR_BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				String gearOredict = miscHelper.getOredictName("gear", name);
				String materialOredict = miscHelper.getOredictName(type.getFormName(), name);
				if(oredict.contains(gearOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.gear_to_material_alloy_smelter", name),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sAlloySmelterRecipes).
							input(gearOredict, 1).
							input(ItemList.Shape_Mold_Ingot.get(0), 0).
							output(materialOredict, 4).
							time(130).energy(3));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.gear_to_material_arc_oxygen", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sArcFurnaceRecipes).
							input(gearOredict, 1).
							fluidInput(Materials.Oxygen.mGas, 400).
							output(materialOredict, 4).
							time(400).energy(96));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.gear_to_material_arc_argon", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sPlasmaArcFurnaceRecipes).
							input(gearOredict, 1).
							fluidInput(Materials.Argon.mPlasma, 1).
							output(materialOredict, 4).
							fluidOutput(Materials.Argon.mFluid, 1).
							time(25).energy(32));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.gear_to_material_arc_nitrogen", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sPlasmaArcFurnaceRecipes).
							input(gearOredict, 1).
							fluidInput(Materials.Nitrogen.mPlasma, 1).
							output(materialOredict, 4).
							fluidOutput(Materials.Nitrogen.mFluid, 1).
							time(25).energy(32));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				String stickOredict = miscHelper.getOredictName("stick", name);
				String nuggetOredict = miscHelper.getOredictName("nugget", name);
				if(oredict.contains(stickOredict) && oredict.contains(nuggetOredict)) {
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.stick_to_nugget_alloy_smelter", name),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sAlloySmelterRecipes).
							input(stickOredict, 1).
							input(ItemList.Shape_Mold_Nugget.get(0), 0).
							output(nuggetOredict, 4).
							time(130).energy(3));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.stick_to_nugget_arc_oxygen", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sArcFurnaceRecipes).
							input(stickOredict, 1).
							fluidInput(Materials.Oxygen.mGas, 50).
							output(nuggetOredict, 4).
							time(50).energy(96));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.stick_to_nugget_arc_argon", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sPlasmaArcFurnaceRecipes).
							input(stickOredict, 1).
							fluidInput(Materials.Argon.mPlasma, 1).
							output(nuggetOredict, 4).
							fluidOutput(Materials.Argon.mFluid, 1).
							time(3).energy(32));
					helper.registerGregTechRecipe(
							miscHelper.getRecipeKey("gregtech.stick_to_nugget_arc_nitrogen", material.getName()),
							helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sPlasmaArcFurnaceRecipes).
							input(stickOredict, 1).
							fluidInput(Materials.Nitrogen.mPlasma, 1).
							output(nuggetOredict, 4).
							fluidOutput(Materials.Nitrogen.mFluid, 1).
							time(3).energy(32));
				}
			}
		}
	}
}
