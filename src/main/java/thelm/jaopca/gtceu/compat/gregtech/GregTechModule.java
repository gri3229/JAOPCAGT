package thelm.jaopca.gtceu.compat.gregtech;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Streams;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
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
import thelm.jaopca.gtceu.compat.gregtech.items.JAOPCAWashableItem;
import thelm.jaopca.items.ItemFormType;
import thelm.jaopca.utils.ApiImpl;
import thelm.jaopca.utils.MiscHelper;

@JAOPCAModule(modDependencies = "gregtech@[2,)")
public class GregTechModule implements IModule {

	static final List<String> ALTS = Arrays.asList("Aluminum", "Quartz");
	static final Set<String> BLACKLIST = new TreeSet<>(ALTS);

	static {
		Streams.stream(GregTechAPI.MATERIAL_REGISTRY).
		forEach(m->BLACKLIST.add(m.toCamelCaseString()));
	}

	public GregTechModule() {
		ApiImpl.INSTANCE.registerUsedPlainPrefixes("ingotHot", "gemChipped", "gemFlawed", "gemFlawless",
				"gemExquisite", "dustImpure", "dustPure", "dustRegular");
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
		ImmutableSetMultimap.Builder builder = ImmutableSetMultimap.builder();
		builder.put(0, "dust");
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
	public void onInit(IModuleData moduleData, FMLInitializationEvent event) {
		JAOPCAApi api = ApiImpl.INSTANCE;
		GregTechHelper helper = GregTechHelper.INSTANCE;
		IMiscHelper miscHelper = MiscHelper.INSTANCE;
		IItemFormType itemFormType = ItemFormType.INSTANCE;
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
			String dustOredict = miscHelper.getOredictName("dust", name);
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
						miscHelper.getRecipeKey("gregtech.ore_to_crushed_forge_hammer", name),
						helper.recipeSettings(RecipeMaps.FORGE_HAMMER_RECIPES).
						input(oreOredict, 1).
						output(crushedInfo, material.getType().isDust() ? 2 : 1).
						time(10).energy(16));
			}
			{
				String extra1Oredict = material.getExtra(1).getType().isCrystalline() ? extra1MaterialOredict : extra1DustOredict;
				helper.registerGregTechRecipe(
						miscHelper.getRecipeKey("gregtech.ore_to_crushed_macerator", name),
						helper.recipeSettings(RecipeMaps.MACERATOR_RECIPES).
						input(oreOredict, 1).
						output(crushedInfo, material.getType().isDust() ? 4 : 2).
						output(extra1Oredict, 1, 1400, 850).
						output("dustStone", 1, 6700, 800).
						time(400));
			}
			// to_purified_crushed
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.crushed_to_purified_crushed_water_100", name),
					helper.recipeSettings(RecipeMaps.ORE_WASHER_RECIPES).
					input(crushedOredict, 1).
					fluidInput(Materials.Water.getFluid(), 100).
					circuitMeta(2).
					output(purifiedCrushedInfo, 1).
					time(8).energy(4));
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.crushed_to_purified_crushed_water_1000", name),
					helper.recipeSettings(RecipeMaps.ORE_WASHER_RECIPES).
					input(crushedOredict, 1).
					fluidInput(Materials.Water.getFluid(), 1000).
					circuitMeta(1).
					output(purifiedCrushedInfo, 1).
					output(extra1TinyDustOredict, 3).
					output("dustStone", 1));
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.crushed_to_purified_crushed_distilled_water", name),
					helper.recipeSettings(RecipeMaps.ORE_WASHER_RECIPES).
					input(crushedOredict, 1).
					fluidInput(Materials.DistilledWater.getFluid(), 100).
					output(purifiedCrushedInfo, 1).
					output(extra1TinyDustOredict, 3).
					output("dustStone", 1).
					time(200));
			// to_centrifuged_crushed
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.crushed_to_centrifuged_crushed", name),
					helper.recipeSettings(RecipeMaps.THERMAL_CENTRIFUGE_RECIPES).
					input(crushedOredict, 1).
					output(centrifugedCrushedInfo, 1).
					output(extra2TinyDustOredict, 3).
					output("dustStone", 1));
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.purified_crushed_to_centrifuged_crushed", name),
					helper.recipeSettings(RecipeMaps.THERMAL_CENTRIFUGE_RECIPES).
					input(purifiedCrushedOredict, 1).
					output(centrifugedCrushedInfo, 1).
					output(extra2TinyDustOredict, 3));
			// to_impure_dust
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.crushed_to_impure_dust_forge_hammer", name),
					helper.recipeSettings(RecipeMaps.FORGE_HAMMER_RECIPES).
					input(crushedOredict, 1).
					output(impureDustInfo, 1).
					time(10).energy(16));
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.crushed_to_impure_dust_macerator", name),
					helper.recipeSettings(RecipeMaps.MACERATOR_RECIPES).
					input(crushedOredict, 1).
					output(impureDustInfo, 1).
					output(extra1DustOredict, 1, 1400, 850).
					time(400));
			api.registerShapelessRecipe(
					miscHelper.getRecipeKey("gregtech.crushed_to_impure_dust_hard_hammer", name),
					impureDustInfo, 1, new Object[] {
							"craftingToolHardHammer", crushedOredict,
					});
			// to_pure_dust
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.purified_crushed_to_pure_dust_forge_hammer", name),
					helper.recipeSettings(RecipeMaps.FORGE_HAMMER_RECIPES).
					input(purifiedCrushedOredict, 1).
					output(pureDustInfo, 1).
					time(10).energy(16));
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.purified_crushed_to_pure_dust_macerator", name),
					helper.recipeSettings(RecipeMaps.MACERATOR_RECIPES).
					input(purifiedCrushedOredict, 1).
					output(pureDustInfo, 1).
					output(extra2DustOredict, 1, 1400, 850).
					time(400));
			api.registerShapelessRecipe(
					miscHelper.getRecipeKey("gregtech.purified_crushed_to_pure_dust_hard_hammer", name),
					pureDustInfo, 1, new Object[] {
							"craftingToolHardHammer", purifiedCrushedOredict,
					});
			// to_dust
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.centrifuged_crushed_to_dust_forge_hammer", name),
					helper.recipeSettings(RecipeMaps.FORGE_HAMMER_RECIPES).
					input(centrifugedCrushedOredict, 1).
					output(dustOredict, 1).
					time(10).energy(16));
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.centrifuged_crushed_to_dust_macerator", name),
					helper.recipeSettings(RecipeMaps.MACERATOR_RECIPES).
					input(centrifugedCrushedOredict, 1).
					output(dustOredict, 1).
					output(extra3DustOredict, 1, 1400, 850).
					time(400));
			api.registerShapelessRecipe(
					miscHelper.getRecipeKey("gregtech.centrifuged_crushed_to_dust_hard_hammer", name),
					dustOredict, 1, new Object[] {
							"craftingToolHardHammer", centrifugedCrushedOredict,
					});
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.impure_dust_to_dust_centrifuge", name),
					helper.recipeSettings(RecipeMaps.CENTRIFUGE_RECIPES).
					input(impureDustOredict, 1).
					output(dustOredict, 1).
					output(extra1TinyDustOredict, 1).
					time(400).energy(24));
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.impure_dust_to_dust_ore_washer", name),
					helper.recipeSettings(RecipeMaps.ORE_WASHER_RECIPES).
					input(impureDustOredict, 1).
					fluidInput(Materials.Water.getFluid(), 100).
					circuitMeta(2).
					output(dustOredict, 1).
					time(8).energy(4));
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.pure_dust_to_dust_centrifuge", name),
					helper.recipeSettings(RecipeMaps.CENTRIFUGE_RECIPES).
					input(pureDustOredict, 1).
					output(dustOredict, 1).
					output(extra2TinyDustOredict, 1).
					time(100).energy(5));
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.pure_dust_to_dust_ore_washer", name),
					helper.recipeSettings(RecipeMaps.ORE_WASHER_RECIPES).
					input(pureDustOredict, 1).
					fluidInput(Materials.Water.getFluid(), 100).
					circuitMeta(2).
					output(dustOredict, 1).
					time(8).energy(4));
			// to_material
			if(material.getType().isIngot()) {
				api.registerSmeltingRecipe(
						miscHelper.getRecipeKey("gregtech.crushed_to_material", name),
						crushedOredict, materialOredict, 1, 0F);
				api.registerSmeltingRecipe(
						miscHelper.getRecipeKey("gregtech.purified_crushed_to_material", name),
						purifiedCrushedOredict, materialOredict, 1, 0F);
				api.registerSmeltingRecipe(
						miscHelper.getRecipeKey("gregtech.centrifuged_crushed_to_material", name),
						centrifugedCrushedOredict, materialOredict, 1, 0F);
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
						helper.recipeSettings(RecipeMaps.FORGE_HAMMER_RECIPES).
						input(oreOredict, 1).
						output(crushedInfo, 1).
						time(10).energy(16));
			}
		}
	}
}
