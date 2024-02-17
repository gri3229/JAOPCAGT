package thelm.jaopca.gt5.compat.gregtech;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import gregtech.api.util.GT_Recipe;
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
import thelm.jaopca.gt5.compat.immersiveengineering.ImmersiveEngineeringHelper;
import thelm.jaopca.items.ItemFormType;
import thelm.jaopca.utils.ApiImpl;
import thelm.jaopca.utils.MiscHelper;

@JAOPCAModule(modDependencies = "gregtech")
public class GregTechCrystalModule implements IModule {

	static final Set<String> BLACKLIST = GregTechModule.BLACKLIST;

	private static boolean ie = true;
	private static boolean rc = true;

	private final IForm exquisiteGemForm = ApiImpl.INSTANCE.newForm(this, "gregtech_exquisite_gem", ItemFormType.INSTANCE).
			setMaterialTypes(MaterialType.GEM, MaterialType.CRYSTAL).setSecondaryName("gemExquisite").setDefaultMaterialBlacklist(BLACKLIST);
	private final IForm flawlessGemForm = ApiImpl.INSTANCE.newForm(this, "gregtech_flawless_gem", ItemFormType.INSTANCE).
			setMaterialTypes(MaterialType.GEM, MaterialType.CRYSTAL).setSecondaryName("gemFlawless").setDefaultMaterialBlacklist(BLACKLIST);
	private final IForm flawedGemForm = ApiImpl.INSTANCE.newForm(this, "gregtech_flawed_gem", ItemFormType.INSTANCE).
			setMaterialTypes(MaterialType.GEM, MaterialType.CRYSTAL).setSecondaryName("gemFlawed").setDefaultMaterialBlacklist(BLACKLIST);
	private final IForm chippedGemForm = ApiImpl.INSTANCE.newForm(this, "gregtech_chipped_gem", ItemFormType.INSTANCE).
			setMaterialTypes(MaterialType.GEM, MaterialType.CRYSTAL).setSecondaryName("gemChipped").setDefaultMaterialBlacklist(BLACKLIST);
	private final IFormRequest formRequest = ApiImpl.INSTANCE.newFormRequest(this,
			exquisiteGemForm, flawlessGemForm, flawedGemForm, chippedGemForm).setGrouped(true);

	@Override
	public String getName() {
		return "gregtech_crystal";
	}

	@Override
	public Multimap<Integer, String> getModuleDependencies() {
		ImmutableSetMultimap.Builder<Integer, String> builder = ImmutableSetMultimap.builder();
		builder.put(0, "gregtech");
		builder.put(0, "dust");
		builder.put(0, "small_dust");
		return builder.build();
	}

	@Override
	public List<IFormRequest> getFormRequests() {
		return Collections.singletonList(formRequest);
	}

	@Override
	public Set<MaterialType> getMaterialTypes() {
		return EnumSet.of(MaterialType.GEM, MaterialType.CRYSTAL);
	}

	@Override
	public Set<String> getDefaultMaterialBlacklist() {
		return BLACKLIST;
	}

	@Override
	public void defineModuleConfig(IModuleData moduleData, IDynamicSpecConfig config) {
		ie = config.getDefinedBoolean("recipes.ie", ie, "Should the module add Immersive Engineering recipes.");
		rc = config.getDefinedBoolean("recipes.rc", rc, "Should the module add Railcraft recipes.");
	}

	@Override
	public void onInit(IModuleData moduleData, FMLInitializationEvent event) {
		JAOPCAApi api = ApiImpl.INSTANCE;
		GregTechHelper helper = GregTechHelper.INSTANCE;
		ImmersiveEngineeringHelper ieHelper = ImmersiveEngineeringHelper.INSTANCE;
		RailcraftHelper rcHelper = RailcraftHelper.INSTANCE;
		IMiscHelper miscHelper = MiscHelper.INSTANCE;
		IItemFormType itemFormType = ItemFormType.INSTANCE;
		for(IMaterial material : formRequest.getMaterials()) {
			String name = material.getName();

			IItemInfo exquisiteGemInfo = itemFormType.getMaterialFormInfo(exquisiteGemForm, material);
			String exquisiteGemOredict = miscHelper.getOredictName("gemExquisite", name);
			IItemInfo flawlessGemInfo = itemFormType.getMaterialFormInfo(flawlessGemForm, material);
			String flawlessGemOredict = miscHelper.getOredictName("gemFlawless", name);
			IItemInfo chippedGemInfo = itemFormType.getMaterialFormInfo(chippedGemForm, material);
			String flawedGemOredict = miscHelper.getOredictName("gemFlawed", name);
			String chippedGemOredict = miscHelper.getOredictName("gemChipped", name);
			IItemInfo flawedGemInfo = itemFormType.getMaterialFormInfo(flawedGemForm, material);
			String purifiedCrushedOredict = miscHelper.getOredictName("crushedPurified", name);
			String materialOredict = miscHelper.getOredictName(material.getType().getFormName(), name);
			String dustOredict = miscHelper.getOredictName("dust", name);
			String smallDustOredict = miscHelper.getOredictName("dustSmall", name);

			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.purified_crushed_to_gems", name),
					helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sSifterRecipes).
					input(purifiedCrushedOredict, 1).
					output(exquisiteGemInfo, 1, 300).
					output(flawlessGemInfo, 1, 1200).
					output(materialOredict, 1, 4500).
					output(flawedGemInfo, 1, 1400).
					output(chippedGemInfo, 1, 2800).
					output(dustOredict, 1, 3500).
					time(800).energy(16));

			api.registerShapedRecipe(
					miscHelper.getRecipeKey("gregtech.exquisite_gem_to_flawless_gem_hard_hammer", name),
					flawlessGemInfo, 2, new Object[] {
							"h", "X",
							'X', exquisiteGemOredict,
							'h', "craftingToolHardHammer",
					});
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.exquisite_gem_to_flawless_gem_hammer", name),
					helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sHammerRecipes).
					input(exquisiteGemOredict, 1).
					output(flawlessGemInfo, 2).
					time(64).energy(16));

			api.registerShapedRecipe(
					miscHelper.getRecipeKey("gregtech.flawless_gem_to_material_hard_hammer", name),
					materialOredict, 2, new Object[] {
							"h", "X",
							'X', flawlessGemOredict,
							'h', "craftingToolHardHammer",
					});
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.flawless_gem_to_material_hammer", name),
					helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sHammerRecipes).
					input(flawlessGemOredict, 1).
					output(materialOredict, 2).
					time(64).energy(16));

			api.registerShapedRecipe(
					miscHelper.getRecipeKey("gregtech.material_to_flawed_gem_hard_hammer", name),
					flawedGemInfo, 2, new Object[] {
							"h", "X",
							'X', materialOredict,
							'h', "craftingToolHardHammer",
					});
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.material_to_flawed_gem_hammer", name),
					helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sHammerRecipes).
					input(materialOredict, 1).
					output(flawedGemInfo, 2).
					time(64).energy(16));

			api.registerShapedRecipe(
					miscHelper.getRecipeKey("gregtech.flawed_gem_to_chipped_gem_hard_hammer", name),
					chippedGemInfo, 2, new Object[] {
							"h", "X",
							'X', flawedGemOredict,
							'h', "craftingToolHardHammer",
					});
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.flawed_gem_to_chipped_gem_hammer", name),
					helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sHammerRecipes).
					input(flawedGemOredict, 1).
					output(chippedGemInfo, 2).
					time(20).energy(16));

			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.exquisite_gem_to_dust_macerator", name),
					helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sMaceratorRecipes).
					input(exquisiteGemOredict, 1).
					output(dustOredict, 4).
					time(400).energy(2));
			if(Loader.isModLoaded("ImmersiveEngineering") && ie) {
				ieHelper.registerCrusherRecipe(
						miscHelper.getRecipeKey("gregtech.exquisite_gem_to_dust_crusher", name),
						exquisiteGemOredict, new Object[] {
								dustOredict, 4,
						}, 6000);
			}
			if(Loader.isModLoaded("Railcraft") && rc) {
				rcHelper.registerRockCrusherRecipe(
						miscHelper.getRecipeKey("gregtech.exquisite_gem_to_dust_rock_crusher", name),
						exquisiteGemOredict, 1, new Object[] {
								dustOredict, 4, 1F,
						});
			}
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.flawless_gem_to_dust_macerator", name),
					helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sMaceratorRecipes).
					input(flawlessGemOredict, 1).
					output(dustOredict, 2).
					time(400).energy(2));
			if(Loader.isModLoaded("ImmersiveEngineering") && ie) {
				ieHelper.registerCrusherRecipe(
						miscHelper.getRecipeKey("gregtech.flawless_gem_to_dust_crusher", name),
						exquisiteGemOredict, new Object[] {
								dustOredict, 2,
						}, 6000);
			}
			if(Loader.isModLoaded("Railcraft") && rc) {
				rcHelper.registerRockCrusherRecipe(
						miscHelper.getRecipeKey("gregtech.flawless_gem_to_dust_rock_crusher", name),
						exquisiteGemOredict, 1, new Object[] {
								dustOredict, 2, 1F,
						});
			}
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.flawed_gem_to_small_dust_macerator", name),
					helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sMaceratorRecipes).
					input(flawedGemOredict, 1).
					output(smallDustOredict, 2).
					time(400).energy(2));
			if(Loader.isModLoaded("ImmersiveEngineering") && ie) {
				ieHelper.registerCrusherRecipe(
						miscHelper.getRecipeKey("gregtech.flawed_gem_to_small_dust_crusher", name),
						flawedGemOredict, new Object[] {
								smallDustOredict, 2,
						}, 6000);
			}
			if(Loader.isModLoaded("Railcraft") && rc) {
				rcHelper.registerRockCrusherRecipe(
						miscHelper.getRecipeKey("gregtech.flawed_gem_to_small_dust_rock_crusher", name),
						flawedGemOredict, 1, new Object[] {
								smallDustOredict, 2, 1F,
						});
			}
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.chipped_gem_to_small_dust_macerator", name),
					helper.recipeSettings(GT_Recipe.GT_Recipe_Map.sMaceratorRecipes).
					input(chippedGemOredict, 1).
					output(smallDustOredict, 1).
					time(400).energy(2));
			if(Loader.isModLoaded("ImmersiveEngineering") && ie) {
				ieHelper.registerCrusherRecipe(
						miscHelper.getRecipeKey("gregtech.chipped_gem_to_small_dust_crusher", name),
						chippedGemOredict, new Object[] {
								smallDustOredict, 1,
						}, 6000);
			}
			if(Loader.isModLoaded("Railcraft") && rc) {
				rcHelper.registerRockCrusherRecipe(
						miscHelper.getRecipeKey("gregtech.chipped_gem_to_small_dust_rock_crusher", name),
						chippedGemOredict, 1, new Object[] {
								smallDustOredict, 1, 1F,
						});
			}
		}
	}
}
