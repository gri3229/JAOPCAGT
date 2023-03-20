package thelm.jaopca.gtce.compat.gregtech;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Streams;

import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.type.Material;
import gregtech.common.MetaFluids;
import net.minecraftforge.fluids.FluidRegistry;
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
import thelm.jaopca.gtce.compat.gregtech.items.JAOPCAWashableItem;
import thelm.jaopca.items.ItemFormType;
import thelm.jaopca.utils.ApiImpl;
import thelm.jaopca.utils.MiscHelper;

@JAOPCAModule(modDependencies = "gregtech@(,2)")
public class GregTechModule implements IModule {

	static final List<String> ALTS = Arrays.asList("Aluminum", "Quartz");
	static final Set<String> BLACKLIST = new TreeSet<>(ALTS);

	static {
		Streams.stream(Material.MATERIAL_REGISTRY).
		forEach(m->BLACKLIST.add(m.toCamelCaseString()));
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
		builder.put(3, "tiny_dust");
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
			String extra1TinyDustOredict = miscHelper.getOredictName("dustTiny", extra1);
			String extra2DustOredict = miscHelper.getOredictName("dust", extra2);
			String extra2TinyDustOredict = miscHelper.getOredictName("dustTiny", extra2);
			String extra3DustOredict = miscHelper.getOredictName("dust", extra3);
			String extra3TinyDustOredict = miscHelper.getOredictName("dustTiny", extra3);
			// to_crushed
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.ore_to_crushed_forge_hammer", name),
					helper.recipeSettings(RecipeMaps.FORGE_HAMMER_RECIPES).
					input(oreOredict, 1).
					output(crushedInfo, material.getType().isDust() ? 2 : 1).
					time(100).energy(6));
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.ore_to_crushed_macerator", name),
					helper.recipeSettings(RecipeMaps.MACERATOR_RECIPES).
					input(oreOredict, 1).
					output(crushedInfo, material.getType().isDust() ? 4 : 2).
					output(extra1DustOredict, 1, 1400, 850).
					output("dustStone", 1, 6700, 800).
					time(400).energy(12));
			// to_purified_crushed
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.crushed_to_purified_crushed_water", name),
					helper.recipeSettings(RecipeMaps.ORE_WASHER_RECIPES).
					input(crushedOredict, 1).
					fluidInput(FluidRegistry.WATER, 1000).
					output(purifiedCrushedInfo, 1).
					output(extra1TinyDustOredict, 3).
					output("dustStone", 1));
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.crushed_to_purified_crushed_distilled_water", name),
					helper.recipeSettings(RecipeMaps.ORE_WASHER_RECIPES).
					input(crushedOredict, 1).
					fluidInput(MetaFluids.DISTILLED_WATER, 1000).
					output(purifiedCrushedInfo, 1).
					output(extra1TinyDustOredict, 3).
					output("dustStone", 1).
					time(300));
			// to_centrifuged_crushed
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.crushed_to_centrifuged_crushed", name),
					helper.recipeSettings(RecipeMaps.THERMAL_CENTRIFUGE_RECIPES).
					input(crushedOredict, 1).
					output(centrifugedCrushedInfo, 1).
					output(extra1TinyDustOredict, 3).
					output("dustStone", 1).
					time(2000));
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.purified_crushed_to_centrifuged_crushed", name),
					helper.recipeSettings(RecipeMaps.THERMAL_CENTRIFUGE_RECIPES).
					input(purifiedCrushedOredict, 1).
					output(centrifugedCrushedInfo, 1).
					output(extra2TinyDustOredict, 3).
					time(2000).energy(60));
			// to_impure_dust
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.crushed_to_impure_dust_forge_hammer", name),
					helper.recipeSettings(RecipeMaps.FORGE_HAMMER_RECIPES).
					input(crushedOredict, 1).
					output(impureDustInfo, 1).
					time(60).energy(8));
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.crushed_to_impure_dust_macerator", name),
					helper.recipeSettings(RecipeMaps.MACERATOR_RECIPES).
					input(crushedOredict, 1).
					output(impureDustInfo, 1).
					output(extra1DustOredict, 1, 1400, 850).
					time(200).energy(12));
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
					time(60).energy(8));
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.purified_crushed_to_pure_dust_macerator", name),
					helper.recipeSettings(RecipeMaps.MACERATOR_RECIPES).
					input(purifiedCrushedOredict, 1).
					output(pureDustInfo, 1).
					output(extra2DustOredict, 1, 1400, 850).
					time(200).energy(12));
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
					time(60).energy(8));
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.centrifuged_crushed_to_dust_macerator", name),
					helper.recipeSettings(RecipeMaps.MACERATOR_RECIPES).
					input(centrifugedCrushedOredict, 1).
					output(dustOredict, 1).
					output(extra3DustOredict, 1, 1400, 850).
					time(200).energy(12));
			api.registerShapelessRecipe(
					miscHelper.getRecipeKey("gregtech.centrifuged_crushed_to_dust_hard_hammer", name),
					dustOredict, 1, new Object[] {
							"craftingToolHardHammer", centrifugedCrushedOredict,
					});
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.impure_dust_to_dust", name),
					helper.recipeSettings(RecipeMaps.CENTRIFUGE_RECIPES).
					input(impureDustOredict, 1).
					output(dustOredict, 1).
					output(extra3TinyDustOredict, 3).
					time(400).energy(24));
			helper.registerGregTechRecipe(
					miscHelper.getRecipeKey("gregtech.pure_dust_to_dust", name),
					helper.recipeSettings(RecipeMaps.CENTRIFUGE_RECIPES).
					input(pureDustOredict, 1).
					output(dustOredict, 1).
					output(extra2TinyDustOredict, 3).
					time(400).energy(5));
			// to_material
			if(material.getType().isIngot()) {
				api.registerSmeltingRecipe(
						miscHelper.getRecipeKey("gregtech.impure_dust_to_material", name),
						impureDustOredict, materialOredict, 1, 0F);
				api.registerSmeltingRecipe(
						miscHelper.getRecipeKey("gregtech.pure_dust_to_material", name),
						pureDustOredict, materialOredict, 1, 0F);
			}
		}
	}
}
