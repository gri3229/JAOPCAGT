package thelm.jaopca.gtceu.compat.gtceu;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;
import java.util.TreeSet;

import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import thelm.jaopca.api.JAOPCAApi;
import thelm.jaopca.api.config.IDynamicSpecConfig;
import thelm.jaopca.api.helpers.IMiscHelper;
import thelm.jaopca.api.materials.IMaterial;
import thelm.jaopca.api.materials.MaterialType;
import thelm.jaopca.api.modules.IModule;
import thelm.jaopca.api.modules.IModuleData;
import thelm.jaopca.api.modules.JAOPCAModule;
import thelm.jaopca.gtceu.compat.gtceu.recipes.GTRecipeSettings;
import thelm.jaopca.utils.ApiImpl;
import thelm.jaopca.utils.MiscHelper;

@JAOPCAModule(modDependencies = "gtceu@[1.1,)")
public class GTCEuCompatModule implements IModule {

	static final Set<String> BLACKLIST = new TreeSet<>(GTCEuModule.ALTS);
	static final Set<String> PLATE_BLACKLIST = new TreeSet<>(GTCEuModule.ALTS);
	static final Set<String> GEAR_BLACKLIST = new TreeSet<>(GTCEuModule.ALTS);
	static final Set<String> ROD_BLACKLIST = new TreeSet<>(GTCEuModule.ALTS);
	static final Set<String> DENSE_BLACKLIST = new TreeSet<>(GTCEuModule.ALTS);

	@Override
	public String getName() {
		return "gtceu_compat";
	}

	private static Set<String> configAutoclaveToCrystalBlacklist = new TreeSet<>();
	private static Set<String> configImplosionToCrystalBlacklist = new TreeSet<>();
	private static Set<String> configToMaterialBlacklist = new TreeSet<>();
	private static Set<String> configToStorageBlockBlacklist = new TreeSet<>();
	private static Set<String> configToNuggetBlacklist = new TreeSet<>();
	private static Set<String> configToPlateBlacklist = new TreeSet<>();
	private static Set<String> configToDensePlateBlacklist = new TreeSet<>();
	private static Set<String> configToGearBlacklist = new TreeSet<>();
	private static Set<String> configToRodBlacklist = new TreeSet<>();
	private static Set<String> configSmallDustPackingBlacklist = new TreeSet<>();
	private static Set<String> configTinyDustPackingBlacklist = new TreeSet<>();
	private static Set<String> configMortarToDustBlacklist = new TreeSet<>();
	private static Set<String> configRecyclingBlacklist = new TreeSet<>();

	@Override
	public Set<MaterialType> getMaterialTypes() {
		return EnumSet.allOf(MaterialType.class);
	}

	@Override
	public void defineModuleConfig(IModuleData moduleData, IDynamicSpecConfig config) {
		IMiscHelper helper = MiscHelper.INSTANCE;
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
				config.getDefinedStringList("recipes.toStorageBlockMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configToStorageBlockBlacklist);
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
				config.getDefinedStringList("recipes.toRodMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configToRodBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.smallDustPackingMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configSmallDustPackingBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.tinyDustPackingMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configTinyDustPackingBlacklist);
		helper.caclulateMaterialSet(
				config.getDefinedStringList("recipes.recyclingMaterialBlacklist", new ArrayList<>(),
						helper.configMaterialPredicate(), ""),
				configRecyclingBlacklist);
	}

	@Override
	public void onCommonSetup(IModuleData moduleData, FMLCommonSetupEvent event) {
		JAOPCAApi api = ApiImpl.INSTANCE;
		GTCEuHelper helper = GTCEuHelper.INSTANCE;
		IMiscHelper miscHelper = MiscHelper.INSTANCE;
		Set<ResourceLocation> itemTags = api.getItemTags();
		ResourceLocation darkAshSmallDustLocation = new ResourceLocation("forge:small_dusts/dark_ash");
		ResourceLocation fileLocation = new ResourceLocation("forge:tools/files");
		ResourceLocation hardHammerLocation = new ResourceLocation("forge:tools/hammers");
		ResourceLocation wrenchLocation = new ResourceLocation("forge:tools/wrenches");
		Item rodExtruderMold = ForgeRegistries.ITEMS.getValue(new ResourceLocation("gtceu:rod_extruder_mold"));
		Item plateExtruderMold = ForgeRegistries.ITEMS.getValue(new ResourceLocation("gtceu:plate_extruder_mold"));
		Item blockExtruderMold = ForgeRegistries.ITEMS.getValue(new ResourceLocation("gtceu:block_extruder_mold"));
		Item gearExtruderMold = ForgeRegistries.ITEMS.getValue(new ResourceLocation("gtceu:gear_extruder_mold"));
		Item nuggetCastingMold = ForgeRegistries.ITEMS.getValue(new ResourceLocation("gtceu:nugget_casting_mold"));
		Item ingotCastingMold = ForgeRegistries.ITEMS.getValue(new ResourceLocation("gtceu:ingot_casting_mold"));
		Item blockCastingMold = ForgeRegistries.ITEMS.getValue(new ResourceLocation("gtceu:block_casting_mold"));
		Item gearCastingMold = ForgeRegistries.ITEMS.getValue(new ResourceLocation("gtceu:gear_casting_mold"));
		for(IMaterial material : moduleData.getMaterials()) {
			MaterialType type = material.getType();
			String name = material.getName();
			if(type.isCrystalline() && !BLACKLIST.contains(name) && !configAutoclaveToCrystalBlacklist.contains(name)) {
				ResourceLocation dustLocation = miscHelper.getTagLocation("dusts", name);
				ResourceLocation materialLocation = miscHelper.getTagLocation(type.getFormName(), name);
				if(itemTags.contains(dustLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.dust_to_material_autoclave_water."+name),
							GTRecipeTypes.AUTOCLAVE_RECIPES,
							helper.recipeSettings().
							itemInput(dustLocation, 1).
							fluidInput(GTMaterials.Water.getFluid(), 250).
							itemOutput(materialLocation, 1, 7000, 1000).
							duration(1200).EUt(24));
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.dust_to_material_autoclave_distilled_water."+name),
							GTRecipeTypes.AUTOCLAVE_RECIPES,
							helper.recipeSettings().
							itemInput(dustLocation, 1).
							fluidInput(GTMaterials.DistilledWater.getFluid(), 50).
							itemOutput(materialLocation).
							duration(600).EUt(24));
				}
			}
			if(type.isCrystalline() && !BLACKLIST.contains(name) && !configImplosionToCrystalBlacklist.contains(name)) {
				ResourceLocation dustLocation = miscHelper.getTagLocation("dusts", name);
				ResourceLocation materialLocation = miscHelper.getTagLocation(type.getFormName(), name);
				if(itemTags.contains(dustLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.dust_to_material_implosion_tnt."+name),
							GTRecipeTypes.IMPLOSION_RECIPES,
							helper.recipeSettings().
							itemInput(dustLocation, 4).
							itemOutput(materialLocation, 3).
							itemOutput(darkAshSmallDustLocation, 1).
							explosivesAmount(2));
				}
			}
			if(type.isDust() && !PLATE_BLACKLIST.contains(name) && !configToPlateBlacklist.contains(name)) {
				ResourceLocation materialLocation = miscHelper.getTagLocation(type.getFormName(), name);
				ResourceLocation plateLocation = miscHelper.getTagLocation("plates", name);
				if(itemTags.contains(plateLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.material_to_plate_compressor."+name),
							GTRecipeTypes.COMPRESSOR_RECIPES,
							helper.recipeSettings().
							itemInput(materialLocation, 1).
							itemOutput(plateLocation, 1));
				}
			}
			if(!BLACKLIST.contains(name) && !configSmallDustPackingBlacklist.contains(name)) {
				ResourceLocation smallDustLocation = miscHelper.getTagLocation("small_dusts", name);
				ResourceLocation dustLocation = miscHelper.getTagLocation("dusts", name);
				if(itemTags.contains(smallDustLocation) && itemTags.contains(dustLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.small_dust_to_dust."+name),
							GTRecipeTypes.PACKER_RECIPES,
							helper.recipeSettings().
							itemInput(smallDustLocation, 4).
							circuitMeta(1).
							itemOutput(dustLocation, 1));
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.dust_to_small_dust."+name),
							GTRecipeTypes.PACKER_RECIPES,
							helper.recipeSettings().
							itemInput(dustLocation, 1).
							circuitMeta(2).
							itemOutput(smallDustLocation, 4));
				}
			}
			if(!BLACKLIST.contains(name) && !configTinyDustPackingBlacklist.contains(name)) {
				ResourceLocation tinyDustLocation = miscHelper.getTagLocation("tiny_dusts", name);
				ResourceLocation dustLocation = miscHelper.getTagLocation("dusts", name);
				if(itemTags.contains(tinyDustLocation) && itemTags.contains(dustLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.tiny_dust_to_dust."+name),
							GTRecipeTypes.PACKER_RECIPES,
							helper.recipeSettings().
							itemInput(tinyDustLocation, 9).
							circuitMeta(1).
							itemOutput(dustLocation, 1));
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.dust_to_tiny_dust."+name),
							GTRecipeTypes.PACKER_RECIPES,
							helper.recipeSettings().
							itemInput(dustLocation, 1).
							circuitMeta(1).
							itemOutput(tinyDustLocation, 9));
				}
			}
			if(type.isIngot() && !ROD_BLACKLIST.contains(name) && !configToRodBlacklist.contains(name)) {
				ResourceLocation materialLocation = miscHelper.getTagLocation(type.getFormName(), name);
				ResourceLocation stickLocation = miscHelper.getTagLocation("rods", name);
				if(itemTags.contains(stickLocation)) {
					api.registerShapedRecipe(
							new ResourceLocation("jaopca", "gtceu.material_to_rod_file."+name),
							stickLocation, 1, new Object[] {
									"f ", " X",
									'X', materialLocation,
									'f', fileLocation,
							});
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.material_to_rod_extruder."+name),
							GTRecipeTypes.EXTRUDER_RECIPES,
							helper.recipeSettings().
							itemInput(materialLocation, 1).
							notConsumable(rodExtruderMold).
							itemOutput(stickLocation, 2).
							duration(200).EUt(42));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToNuggetBlacklist.contains(name)) {
				ResourceLocation materialLocation = miscHelper.getTagLocation(type.getFormName(), name);
				ResourceLocation nuggetLocation = miscHelper.getTagLocation("nuggets", name);
				if(itemTags.contains(nuggetLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.material_to_nugget."+name),
							GTRecipeTypes.ALLOY_SMELTER_RECIPES,
							helper.recipeSettings().
							itemInput(materialLocation, 1).
							notConsumable(nuggetCastingMold).
							itemOutput(nuggetLocation, 9).
							duration(100).EUt(7));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToMaterialBlacklist.contains(name)) {
				ResourceLocation storageBlockLocation = miscHelper.getTagLocation("storage_blocks", name);
				ResourceLocation materialLocation = miscHelper.getTagLocation(type.getFormName(), name);
				if(itemTags.contains(storageBlockLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.storage_block_to_material_alloy_smelter."+name),
							GTRecipeTypes.ALLOY_SMELTER_RECIPES,
							helper.recipeSettings().
							itemInput(storageBlockLocation, 1).
							notConsumable(ingotCastingMold).
							itemOutput(materialLocation, (material.isSmallStorageBlock() ? 4 : 9)).
							duration(900).EUt(7));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToStorageBlockBlacklist.contains(name)) {
				ResourceLocation materialLocation = miscHelper.getTagLocation(type.getFormName(), name);
				ResourceLocation storageBlockLocation = miscHelper.getTagLocation("storage_blocks", name);
				if(itemTags.contains(storageBlockLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.material_to_storage_block_compressor."+name),
							GTRecipeTypes.COMPRESSOR_RECIPES,
							helper.recipeSettings().
							itemInput(materialLocation, (material.isSmallStorageBlock() ? 4 : 9)).
							itemOutput(storageBlockLocation, 1).
							duration(300).EUt(2));
				}
			}
			if(type.isIngot() && !PLATE_BLACKLIST.contains(name) && !configToPlateBlacklist.contains(name)) {
				ResourceLocation materialLocation = miscHelper.getTagLocation(type.getFormName(), name);
				ResourceLocation plateLocation = miscHelper.getTagLocation("plates", name);
				if(itemTags.contains(plateLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.material_to_plate_bender."+name),
							GTRecipeTypes.BENDER_RECIPES,
							helper.recipeSettings().
							itemInput(materialLocation, 1).
							circuitMeta(1).
							itemOutput(plateLocation, 1).
							duration(100).EUt(24));
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.material_to_plate_forge_hammer."+name),
							GTRecipeTypes.FORGE_HAMMER_RECIPES,
							helper.recipeSettings().
							itemInput(materialLocation, 3).
							itemOutput(plateLocation, 2).
							duration(100).EUt(16));
					api.registerShapedRecipe(
							new ResourceLocation("jaopca", "gtceu.material_to_plate_hard_hammer."+name),
							plateLocation, 1, new Object[] {
									"h", "I", "I",
									'I', materialLocation,
									'h', hardHammerLocation,
							});
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.material_to_plate_extruder."+name),
							GTRecipeTypes.EXTRUDER_RECIPES,
							helper.recipeSettings().
							itemInput(materialLocation, 1).
							notConsumable(plateExtruderMold).
							itemOutput(plateLocation, 1).
							duration(100).EUt(56));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToMaterialBlacklist.contains(name)) {
				ResourceLocation nuggetLocation = miscHelper.getTagLocation("nuggets", name);
				ResourceLocation materialLocation = miscHelper.getTagLocation(type.getFormName(), name);
				if(itemTags.contains(nuggetLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.nugget_to_material_compressor."+name),
							GTRecipeTypes.COMPRESSOR_RECIPES,
							helper.recipeSettings().
							itemInput(nuggetLocation, 9).
							itemOutput(materialLocation, 1).
							duration(300).EUt(2));
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.nugget_to_material_alloy_smelter."+name),
							GTRecipeTypes.ALLOY_SMELTER_RECIPES,
							helper.recipeSettings().
							itemInput(nuggetLocation, 9).
							notConsumable(ingotCastingMold).
							itemOutput(materialLocation, 1).
							duration(100).EUt(7));
				}
			}
			if(!PLATE_BLACKLIST.contains(name) && !configToPlateBlacklist.contains(name)) {
				ResourceLocation storageBlockLocation = miscHelper.getTagLocation("storage_blocks", name);
				ResourceLocation plateLocation = miscHelper.getTagLocation("plates", name);
				if(itemTags.contains(storageBlockLocation) && itemTags.contains(plateLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.storage_block_to_plate."+name),
							GTRecipeTypes.CUTTER_RECIPES,
							helper.recipeSettings().
							itemInput(storageBlockLocation, 1).
							itemOutput(plateLocation, (material.isSmallStorageBlock() ? 4 : 9)).
							duration(800).EUt(30));
				}
			}
			if(type.isIngot() && !BLACKLIST.contains(name) && !configToStorageBlockBlacklist.contains(name)) {
				ResourceLocation materialLocation = miscHelper.getTagLocation(type.getFormName(), name);
				ResourceLocation storageBlockLocation = miscHelper.getTagLocation("storage_blocks", name);
				if(itemTags.contains(storageBlockLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.material_to_storage_block_extruder."+name),
							GTRecipeTypes.EXTRUDER_RECIPES,
							helper.recipeSettings().
							itemInput(materialLocation, (material.isSmallStorageBlock() ? 4 : 9)).
							notConsumable(blockExtruderMold).
							itemOutput(storageBlockLocation, 1).
							duration(10).EUt(56));
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.material_to_storage_block_alloy_smelter."+name),
							GTRecipeTypes.ALLOY_SMELTER_RECIPES,
							helper.recipeSettings().
							itemInput(materialLocation, (material.isSmallStorageBlock() ? 4 : 9)).
							notConsumable(blockCastingMold).
							itemOutput(storageBlockLocation, 1).
							duration(5).EUt(28));
				}
			}
			if(type.isCrystalline() && !BLACKLIST.contains(name) && !configToStorageBlockBlacklist.contains(name)) {
				ResourceLocation materialLocation = miscHelper.getTagLocation(type.getFormName(), name);
				ResourceLocation storageBlockLocation = miscHelper.getTagLocation("storage_blocks", name);
				if(itemTags.contains(storageBlockLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.material_to_block_compressor."+name),
							GTRecipeTypes.COMPRESSOR_RECIPES,
							helper.recipeSettings().
							itemInput(materialLocation, (material.isSmallStorageBlock() ? 4 : 9)).
							itemOutput(storageBlockLocation, 1).
							duration(300).EUt(2));
				}
			}
			if(type.isCrystalline() && !BLACKLIST.contains(name) && !configToMaterialBlacklist.contains(name)) {
				ResourceLocation storageBlockLocation = miscHelper.getTagLocation("storage_blocks", name);
				ResourceLocation materialLocation = miscHelper.getTagLocation(type.getFormName(), name);
				if(itemTags.contains(storageBlockLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.block_to_material_forge_hammer."+name),
							GTRecipeTypes.FORGE_HAMMER_RECIPES,
							helper.recipeSettings().
							itemInput(storageBlockLocation, 1).
							itemOutput(materialLocation, (material.isSmallStorageBlock() ? 4 : 9)).
							duration(100).EUt(24));
				}
			}
			if(type.isIngot() && !GEAR_BLACKLIST.contains(name) && !configToGearBlacklist.contains(name)) {
				ResourceLocation materialLocation = miscHelper.getTagLocation(type.getFormName(), name);
				ResourceLocation gearLocation = miscHelper.getTagLocation("gears", name);
				if(itemTags.contains(gearLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.material_to_gear_extruder."+name),
							GTRecipeTypes.EXTRUDER_RECIPES,
							helper.recipeSettings().
							itemInput(materialLocation, 4).
							notConsumable(gearExtruderMold).
							itemOutput(gearLocation, 1).
							duration(500).EUt(56));
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.material_to_gear_alloy_smelter."+name),
							GTRecipeTypes.ALLOY_SMELTER_RECIPES,
							helper.recipeSettings().
							itemInput(materialLocation, 8).
							notConsumable(gearCastingMold).
							itemOutput(gearLocation, 1).
							duration(1000).EUt(14));
				}
			}
			if(!(PLATE_BLACKLIST.contains(name) && ROD_BLACKLIST.contains(name)) && !configToGearBlacklist.contains(name)) {
				ResourceLocation plateLocation = miscHelper.getTagLocation("plates", name);
				ResourceLocation rodLocation = miscHelper.getTagLocation("rods", name);
				ResourceLocation gearLocation = miscHelper.getTagLocation("gears", name);
				if(itemTags.contains(plateLocation) && itemTags.contains(rodLocation) && itemTags.contains(gearLocation)) {
					api.registerShapedRecipe(
							new ResourceLocation("jaopca", "gtceu.plate_stick_to_gear."+name),
							gearLocation, 1, new Object[] {
									"RPR", "PwP", "RPR",
									'P', plateLocation,
									'R', rodLocation,
									'w', wrenchLocation,
							});
				}
			}
			if(type.isIngot() && !DENSE_BLACKLIST.contains(name) && !configToDensePlateBlacklist.contains(name)) {
				ResourceLocation plateLocation = miscHelper.getTagLocation("plates", name);
				ResourceLocation densePlateLocation = miscHelper.getTagLocation("dense_plates", name);
				if(itemTags.contains(plateLocation) && itemTags.contains(densePlateLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.plate_to_dense_plate."+name),
							GTRecipeTypes.BENDER_RECIPES,
							helper.recipeSettings().
							itemInput(plateLocation, 9).
							circuitMeta(9).
							itemOutput(densePlateLocation, 1).
							duration(900).EUt(96));
				}
			}
			if(type.isIngot() && !DENSE_BLACKLIST.contains(name) && !configToDensePlateBlacklist.contains(name)) {
				ResourceLocation materialLocation = miscHelper.getTagLocation(type.getFormName(), name);
				ResourceLocation densePlateLocation = miscHelper.getTagLocation("dense_plates", name);
				if(itemTags.contains(densePlateLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.plate_to_dense_plate."+name),
							GTRecipeTypes.BENDER_RECIPES,
							helper.recipeSettings().
							itemInput(materialLocation, 9).
							circuitMeta(9).
							itemOutput(densePlateLocation, 1).
							duration(900).EUt(96));
				}
			}
			if(!type.isDust() && !ROD_BLACKLIST.contains(name) && !configToRodBlacklist.contains(name)) {
				ResourceLocation materialLocation = miscHelper.getTagLocation(type.getFormName(), name);
				ResourceLocation rodLocation = miscHelper.getTagLocation("rods", name);
				ResourceLocation smallDustLocation = miscHelper.getTagLocation("small_dusts", name);
				if(itemTags.contains(rodLocation)) {
					GTRecipeSettings settings = helper.recipeSettings().
							itemInput(materialLocation, 1).
							duration(200).EUt(16);
					if(ConfigHolder.INSTANCE.recipes.harderRods) {
						settings.itemOutput(rodLocation, 1);
						if(itemTags.contains(smallDustLocation)) {
							settings.itemOutput(smallDustLocation, 2);
						}
					}
					else {
						settings.itemOutput(rodLocation, 2);
					}
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.material_to_stick_lathe."+name),
							GTRecipeTypes.LATHE_RECIPES, settings);
				}
			}
			if(!type.isDust() && !BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				ResourceLocation materialLocation = miscHelper.getTagLocation(type.getFormName(), name);
				ResourceLocation dustLocation = miscHelper.getTagLocation("dusts", name);
				if(itemTags.contains(dustLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.material_to_dust_macerator."+name),
							GTRecipeTypes.MACERATOR_RECIPES,
							helper.recipeSettings().
							itemInput(materialLocation, 1).
							itemOutput(dustLocation, 1).
							duration(100).EUt(2));
				}
			}
			if(!BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				ResourceLocation storageBlockLocation = miscHelper.getTagLocation("storage_blocks", name);
				ResourceLocation dustLocation = miscHelper.getTagLocation("dusts", name);
				if(itemTags.contains(storageBlockLocation) && itemTags.contains(dustLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.block_to_dust."+name),
							GTRecipeTypes.MACERATOR_RECIPES,
							helper.recipeSettings().
							itemInput(storageBlockLocation, 1).
							itemOutput(dustLocation, (material.isSmallStorageBlock() ? 4 : 9)).
							duration(100*(material.isSmallStorageBlock() ? 4 : 9)).EUt(2));
				}
			}
			if(!BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				ResourceLocation nuggetLocation = miscHelper.getTagLocation("nuggets", name);
				ResourceLocation tinyDustLocation = miscHelper.getTagLocation("tiny_dusts", name);
				if(itemTags.contains(nuggetLocation) && itemTags.contains(tinyDustLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.nugget_to_tiny_dust."+name),
							GTRecipeTypes.MACERATOR_RECIPES,
							helper.recipeSettings().
							itemInput(nuggetLocation, 1).
							itemOutput(tinyDustLocation, 1).
							duration(11).EUt(2));
				}
			}
			if(!PLATE_BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				ResourceLocation plateLocation = miscHelper.getTagLocation("plates", name);
				ResourceLocation dustLocation = miscHelper.getTagLocation("dusts", name);
				if(itemTags.contains(plateLocation) && itemTags.contains(dustLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.plate_to_dust."+name),
							GTRecipeTypes.MACERATOR_RECIPES,
							helper.recipeSettings().
							itemInput(plateLocation, 1).
							itemOutput(dustLocation, 1).
							duration(100).EUt(2));
				}
			}
			if(!DENSE_BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				ResourceLocation densePlateLocation = miscHelper.getTagLocation("dense_plates", name);
				ResourceLocation dustLocation = miscHelper.getTagLocation("dusts", name);
				if(itemTags.contains(densePlateLocation) && itemTags.contains(dustLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.dense_plate_to_dust."+name),
							GTRecipeTypes.MACERATOR_RECIPES,
							helper.recipeSettings().
							itemInput(densePlateLocation, 1).
							itemOutput(dustLocation, 9).
							duration(900).EUt(2));
				}
			}
			if(!GEAR_BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				ResourceLocation gearLocation = miscHelper.getTagLocation("gears", name);
				ResourceLocation dustLocation = miscHelper.getTagLocation("dusts", name);
				if(itemTags.contains(gearLocation) && itemTags.contains(dustLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.gear_to_dust."+name),
							GTRecipeTypes.MACERATOR_RECIPES,
							helper.recipeSettings().
							itemInput(gearLocation, 1).
							itemOutput(dustLocation, 4).
							duration(400).EUt(2));
				}
			}
			if(!ROD_BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				ResourceLocation rodLocation = miscHelper.getTagLocation("rods", name);
				ResourceLocation smallDustLocation = miscHelper.getTagLocation("small_dusts", name);
				if(itemTags.contains(rodLocation) && itemTags.contains(smallDustLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.stick_to_small_dust."+name),
							GTRecipeTypes.MACERATOR_RECIPES,
							helper.recipeSettings().
							itemInput(rodLocation, 1).
							itemOutput(smallDustLocation, 2).
							duration(50).EUt(2));
				}
			}
			if(type.isIngot() && !PLATE_BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				ResourceLocation plateLocation = miscHelper.getTagLocation("plate", name);
				ResourceLocation materialLocation = miscHelper.getTagLocation(type.getFormName(), name);
				if(itemTags.contains(plateLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.plate_to_material."+name),
							GTRecipeTypes.ARC_FURNACE_RECIPES,
							helper.recipeSettings().
							itemInput(plateLocation, 1).
							itemOutput(materialLocation, 1).
							duration(100).EUt(30));
				}
			}
			if(type.isIngot() && !DENSE_BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				ResourceLocation densePlateLocation = miscHelper.getTagLocation("dense_plates", name);
				ResourceLocation materialLocation = miscHelper.getTagLocation(type.getFormName(), name);
				if(itemTags.contains(densePlateLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.dense_plate_to_material."+name),
							GTRecipeTypes.ARC_FURNACE_RECIPES,
							helper.recipeSettings().
							itemInput(densePlateLocation, 1).
							itemOutput(materialLocation, 9).
							duration(900).EUt(30));
				}
			}
			if(type.isIngot() && !GEAR_BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				ResourceLocation gearLocation = miscHelper.getTagLocation("gear", name);
				ResourceLocation materialLocation = miscHelper.getTagLocation(type.getFormName(), name);
				if(itemTags.contains(gearLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.gear_to_material."+name),
							GTRecipeTypes.ARC_FURNACE_RECIPES,
							helper.recipeSettings().
							itemInput(gearLocation, 1).
							itemOutput(materialLocation, 4).
							duration(400).EUt(30));
				}
			}
			if(type.isIngot() && !ROD_BLACKLIST.contains(name) && !configRecyclingBlacklist.contains(name)) {
				ResourceLocation rodLocation = miscHelper.getTagLocation("rods", name);
				ResourceLocation nuggetLocation = miscHelper.getTagLocation("nugget", name);
				if(itemTags.contains(rodLocation) && itemTags.contains(nuggetLocation)) {
					helper.registerGTRecipe(
							new ResourceLocation("jaopca", "gtceu.rod_to_nugget."+name),
							GTRecipeTypes.ARC_FURNACE_RECIPES,
							helper.recipeSettings().
							itemInput(rodLocation, 1).
							itemOutput(nuggetLocation, 4).
							duration(50).EUt(30));
				}
			}
		}
	}
}
