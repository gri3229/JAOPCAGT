package thelm.jaopca.gtceu.compat.gtceu;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import thelm.jaopca.api.JAOPCAApi;
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
import thelm.jaopca.gtceu.compat.gtceu.recipes.GTRecipeSettings;
import thelm.jaopca.items.ItemFormType;
import thelm.jaopca.utils.ApiImpl;
import thelm.jaopca.utils.MiscHelper;

@JAOPCAModule(modDependencies = "gtceu")
public class GTCEuCrystalModule implements IModule {

	static final Set<String> BLACKLIST = GTCEuModule.BLACKLIST;

	private final IForm exquisiteGemForm = ApiImpl.INSTANCE.newForm(this, "gtceu_exquisite_gems", ItemFormType.INSTANCE).
			setMaterialTypes(MaterialType.GEM, MaterialType.CRYSTAL).setSecondaryName("exquisite_gems");
	private final IForm flawlessGemForm = ApiImpl.INSTANCE.newForm(this, "gtceu_flawless_gems", ItemFormType.INSTANCE).
			setMaterialTypes(MaterialType.GEM, MaterialType.CRYSTAL).setSecondaryName("flawless_gems");
	private final IForm flawedGemForm = ApiImpl.INSTANCE.newForm(this, "gtceu_flawed_gems", ItemFormType.INSTANCE).
			setMaterialTypes(MaterialType.GEM, MaterialType.CRYSTAL).setSecondaryName("flawed_gems");
	private final IForm chippedGemForm = ApiImpl.INSTANCE.newForm(this, "gtceu_chipped_gems", ItemFormType.INSTANCE).
			setMaterialTypes(MaterialType.GEM, MaterialType.CRYSTAL).setSecondaryName("chipped_gems");
	private IFormRequest formRequest;

	@Override
	public String getName() {
		return "gtceu_crystal";
	}

	@Override
	public Multimap<Integer, String> getModuleDependencies() {
		ImmutableSetMultimap.Builder<Integer, String> builder = ImmutableSetMultimap.builder();
		builder.put(0, "gtceu");
		builder.put(0, "dusts");
		builder.put(0, "small_dusts");
		return builder.build();
	}

	@Override
	public List<IFormRequest> getFormRequests() {
		exquisiteGemForm.setDefaultMaterialBlacklist(BLACKLIST);
		flawlessGemForm.setDefaultMaterialBlacklist(BLACKLIST);
		flawedGemForm.setDefaultMaterialBlacklist(BLACKLIST);
		chippedGemForm.setDefaultMaterialBlacklist(BLACKLIST);
		formRequest = ApiImpl.INSTANCE.newFormRequest(this,
				exquisiteGemForm, flawlessGemForm, flawedGemForm, chippedGemForm).setGrouped(true);
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
	public void onCommonSetup(IModuleData moduleData, FMLCommonSetupEvent event) {
		JAOPCAApi api = ApiImpl.INSTANCE;
		GTCEuHelper helper = GTCEuHelper.INSTANCE;
		IMiscHelper miscHelper = MiscHelper.INSTANCE;
		IItemFormType itemFormType = ItemFormType.INSTANCE;
		ResourceLocation whiteLensLocation = new ResourceLocation("forge:lenses/white");
		ResourceLocation hardHammerLocation = new ResourceLocation("forge:tools/hammers");
		for(IMaterial material : formRequest.getMaterials()) {
			String name = material.getName();

			IItemInfo exquisiteGemInfo = itemFormType.getMaterialFormInfo(exquisiteGemForm, material);
			ResourceLocation exquisiteGemLocation = miscHelper.getTagLocation("exquisite_gems", name);
			IItemInfo flawlessGemInfo = itemFormType.getMaterialFormInfo(flawlessGemForm, material);
			ResourceLocation flawlessGemLocation = miscHelper.getTagLocation("flawless_gems", name);
			IItemInfo flawedGemInfo = itemFormType.getMaterialFormInfo(flawedGemForm, material);
			ResourceLocation flawedGemLocation = miscHelper.getTagLocation("flawed_gems", name);
			IItemInfo chippedGemInfo = itemFormType.getMaterialFormInfo(chippedGemForm, material);
			ResourceLocation chippedGemLocation = miscHelper.getTagLocation("chipped_gems", name);
			ResourceLocation purifiedOreLocation = miscHelper.getTagLocation("purified_ores", name);
			ResourceLocation materialLocation = miscHelper.getTagLocation(material.getType().getFormName(), name);
			ResourceLocation dustLocation = miscHelper.getTagLocation("dusts", name);
			ResourceLocation smallDustLocation = miscHelper.getTagLocation("small_dusts", name);

			{
				GTRecipeSettings settings = helper.recipeSettings().
						itemInput(purifiedOreLocation, 1).
						itemOutput(exquisiteGemInfo, 1, 500, 150).
						itemOutput(flawlessGemInfo, 1, 1500, 200).
						itemOutput(materialLocation, 1, 5000, 1000).
						itemOutput(dustLocation, 1, 2500, 500).
						duration(400).EUt(16);
				if(ConfigHolder.INSTANCE.recipes.generateLowQualityGems) {
					settings.
					itemOutput(flawedGemInfo, 1, 2000, 500).
					itemOutput(chippedGemInfo, 1, 3000, 350);
				}
				helper.registerGTRecipe(
						new ResourceLocation("jaopca", "gtceu.purified_ore_to_gems."+name),
						GTRecipeTypes.SIFTER_RECIPES, settings);
			}

			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.flawless_gem_to_exquisite_gem."+name),
					GTRecipeTypes.LASER_ENGRAVER_RECIPES,
					helper.recipeSettings().
					itemInput(flawlessGemLocation, 2).
					notConsumable(whiteLensLocation).
					itemOutput(exquisiteGemInfo, 1).
					duration(300).EUt(240));

			api.registerShapelessRecipe(
					new ResourceLocation("jaopca", "gtceu.exquisite_gem_to_flawless_gem_hard_hammer."+name),
					flawlessGemInfo, 2, new Object[] {
							hardHammerLocation, exquisiteGemLocation,
					});
			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.exquisite_gem_to_flawless_gem_cutter."+name),
					GTRecipeTypes.CUTTER_RECIPES,
					helper.recipeSettings().
					itemInput(exquisiteGemLocation, 1).
					itemOutput(flawlessGemInfo, 2).
					duration(20).EUt(16));
			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.material_to_flawless_gem."+name),
					GTRecipeTypes.LASER_ENGRAVER_RECIPES,
					helper.recipeSettings().
					itemInput(materialLocation, 2).
					notConsumable(whiteLensLocation).
					itemOutput(flawlessGemInfo, 1).
					duration(300).EUt(240));

			api.registerShapelessRecipe(
					new ResourceLocation("jaopca", "gtceu.flawless_gem_to_material_hard_hammer."+name),
					materialLocation, 2, new Object[] {
							hardHammerLocation, flawlessGemLocation,
					});
			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.flawless_gem_to_material_cutter."+name),
					GTRecipeTypes.CUTTER_RECIPES,
					helper.recipeSettings().
					itemInput(flawlessGemLocation, 1).
					itemOutput(materialLocation, 2).
					duration(20).EUt(16));
			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.flawed_gem_to_material."+name),
					GTRecipeTypes.LASER_ENGRAVER_RECIPES,
					helper.recipeSettings().
					itemInput(flawedGemLocation, 2).
					notConsumable(whiteLensLocation).
					itemOutput(materialLocation, 1).
					duration(300).EUt(240));

			api.registerShapelessRecipe(
					new ResourceLocation("jaopca", "gtceu.material_to_flawed_gem_hard_hammer."+name),
					flawedGemLocation, 2, new Object[] {
							hardHammerLocation, materialLocation,
					});
			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.material_to_flawed_gem_cutter."+name),
					GTRecipeTypes.CUTTER_RECIPES,
					helper.recipeSettings().
					itemInput(materialLocation, 1).
					itemOutput(flawedGemLocation, 2).
					duration(20).EUt(16));
			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.chipped_gem_to_flawed_gem."+name),
					GTRecipeTypes.LASER_ENGRAVER_RECIPES,
					helper.recipeSettings().
					itemInput(chippedGemLocation, 2).
					notConsumable(whiteLensLocation).
					itemOutput(flawedGemLocation, 1).
					duration(300).EUt(240));

			api.registerShapelessRecipe(
					new ResourceLocation("jaopca", "gtceu.flawed_gem_to_chipped_gem_hard_hammer."+name),
					chippedGemLocation, 2, new Object[] {
							hardHammerLocation, flawedGemLocation,
					});
			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.flawed_gem_to_chipped_gem_cutter."+name),
					GTRecipeTypes.CUTTER_RECIPES,
					helper.recipeSettings().
					itemInput(flawedGemLocation, 1).
					itemOutput(chippedGemLocation, 2).
					duration(20).EUt(16));

			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.exquisite_gem_to_dust."+name),
					GTRecipeTypes.MACERATOR_RECIPES,
					helper.recipeSettings().
					itemInput(exquisiteGemLocation, 1).
					itemOutput(dustLocation, 4).
					duration(400).EUt(2));
			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.flawless_gem_to_dust."+name),
					GTRecipeTypes.MACERATOR_RECIPES,
					helper.recipeSettings().
					itemInput(flawlessGemLocation, 1).
					itemOutput(dustLocation, 2).
					duration(200).EUt(2));
			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.flawed_gem_to_small_dust."+name),
					GTRecipeTypes.MACERATOR_RECIPES,
					helper.recipeSettings().
					itemInput(flawedGemLocation, 1).
					itemOutput(smallDustLocation, 2).
					duration(50).EUt(2));
			helper.registerGTRecipe(
					new ResourceLocation("jaopca", "gtceu.chipped_gem_to_small_dust."+name),
					GTRecipeTypes.MACERATOR_RECIPES,
					helper.recipeSettings().
					itemInput(chippedGemLocation, 1).
					itemOutput(smallDustLocation, 1).
					duration(25).EUt(2));
		}
	}
}
