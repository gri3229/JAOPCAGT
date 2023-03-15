package thelm.jaopca.gt4.compat.gregtechaddon;

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
import gregtechmod.api.enums.Materials;
import gregtechmod.common.recipe.RecipeMaps;
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
import thelm.jaopca.compat.ic2.IC2Helper;
import thelm.jaopca.compat.railcraft.RailcraftHelper;
import thelm.jaopca.config.ConfigHandler;
import thelm.jaopca.gt4.compat.thermalexpansion.ThermalExpansionHelper;
import thelm.jaopca.items.ItemFormType;
import thelm.jaopca.utils.ApiImpl;
import thelm.jaopca.utils.MiscHelper;

@JAOPCAModule(modDependencies = "gregtech_addon")
public class GregTechAddonModule implements IModule {

	static final Set<String> BLACKLIST = new TreeSet<>();

	static {
		Arrays.stream(Materials.values()).forEach(m->BLACKLIST.add(m.name()));
	}

	private static boolean rockCrusher = true;
	private static boolean pulverizer = true;

	public GregTechAddonModule() {
		ApiImpl.INSTANCE.registerUsedPlainPrefixes("ingotHot", "ingotDouble", "ingotTriple", "ingotQuadruple",
				"ingotQuintuple", "dustImpure", "dustPure");
		Stream.of("jaopca:ic2.ore_to_crushed.*", "jaopca:ic2.crushed_to_purified_crushed.*",
				"jaopca:ic2.crushed_to_material.*", "jaopca:ic2.purified_crushed_to_material.*",
				"jaopca:ic2.crushed_to_dust_macerator.*", "jaopca:ic2.purified_crushed_to_dust_macerator.*",
				"jaopca:ic2.crushed_to_dust_centrifuge.*", "jaopca:ic2.purified_crushed_to_dust_centrifuge.*").
		map(Pattern::compile).forEach(ConfigHandler.RECIPE_REGEX_BLACKLIST::add);
	}

	private final IForm crushedForm = ApiImpl.INSTANCE.newForm(this, "gregtech_addon_crushed", ItemFormType.INSTANCE).
			setMaterialTypes(MaterialType.ORE).setSecondaryName("crushed").setDefaultMaterialBlacklist(BLACKLIST);
	private final IForm purifiedCrushedForm = ApiImpl.INSTANCE.newForm(this, "gregtech_addon_purified_crushed", ItemFormType.INSTANCE).
			setMaterialTypes(MaterialType.ORE).setSecondaryName("crushedPurified").setDefaultMaterialBlacklist(BLACKLIST);
	private final IForm centrifugedCrushedForm = ApiImpl.INSTANCE.newForm(this, "gregtech_addon_centrifuged_crushed", ItemFormType.INSTANCE).
			setMaterialTypes(MaterialType.ORE).setSecondaryName("crushedCentrifuged").setDefaultMaterialBlacklist(BLACKLIST);
	private final IForm impureDustForm = ApiImpl.INSTANCE.newForm(this, "gregtech_addon_impure_dust", ItemFormType.INSTANCE).
			setMaterialTypes(MaterialType.ORE).setSecondaryName("dustImpure").setDefaultMaterialBlacklist(BLACKLIST);
	private final IForm pureDustForm = ApiImpl.INSTANCE.newForm(this, "gregtech_addon_pure_dust", ItemFormType.INSTANCE).
			setMaterialTypes(MaterialType.ORE).setSecondaryName("dustPure").setDefaultMaterialBlacklist(BLACKLIST);
	private final IFormRequest formRequest = ApiImpl.INSTANCE.newFormRequest(this,
			crushedForm, purifiedCrushedForm, centrifugedCrushedForm, impureDustForm, pureDustForm).setGrouped(true);

	@Override
	public String getName() {
		return "gregtech_addon";
	}

	@Override
	public Multimap<Integer, String> getModuleDependencies() {
		ImmutableSetMultimap.Builder builder = ImmutableSetMultimap.builder();
		builder.put(0, "dust");
		builder.put(0, "nugget");
		builder.put(1, "dust");
		builder.put(1, "small_dust");
		builder.put(1, "tiny_dust");
		builder.put(2, "dust");
		builder.put(2, "small_dust");
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
		rockCrusher = config.getDefinedBoolean("recipes.rockCrusher", rockCrusher, "Should the module add recipes to Railcraft's rock crusher.");
		pulverizer = config.getDefinedBoolean("recipes.pulverizer", pulverizer, "Should the module add recipes to Thermal Expansion's pulverizer.");
	}

	@Override
	public void onInit(IModuleData moduleData, FMLInitializationEvent event) {
		JAOPCAApi api = ApiImpl.INSTANCE;
		GregTechAddonHelper helper = GregTechAddonHelper.INSTANCE;
		IC2Helper ic2Helper = IC2Helper.INSTANCE;
		RailcraftHelper rcHelper = RailcraftHelper.INSTANCE;
		ThermalExpansionHelper teHelper = ThermalExpansionHelper.INSTANCE;
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
			String nuggetOredict = miscHelper.getOredictName("nugget", name);
			String materialOredict = miscHelper.getOredictName(material.getType().getFormName(), name);
			String extra1DustOredict = miscHelper.getOredictName("dust", extra1);
			String extra1SmallDustOredict = miscHelper.getOredictName("dustSmall", extra1);
			String extra1TinyDustOredict = miscHelper.getOredictName("dustTiny", extra1);
			String extra2DustOredict = miscHelper.getOredictName("dust", extra2);
			String extra2SmallDustOredict = miscHelper.getOredictName("dustSmall", extra2);
			String extra2TinyDustOredict = miscHelper.getOredictName("dustTiny", extra2);
			String extra3DustOredict = miscHelper.getOredictName("dust", extra3);
			// to_crushed
			api.registerShapedRecipe(
					miscHelper.getRecipeKey("gregtech_addon.ore_to_crushed_hard_hammer", name),
					crushedInfo, 1, new Object[] {
							"T", "O",
							'T', "craftingToolHardHammer",
							'O', oreOredict,
					});
			helper.registerGregTechAddonRecipe(
					miscHelper.getRecipeKey("gregtech_addon.ore_to_crushed_hammer", name),
					helper.recipeSettings(RecipeMaps.HAMMER).
					input(oreOredict, 1).
					output(crushedInfo, material.getType().isDust() ? 2 : 1).
					energy(10).time(16));
			ic2Helper.registerMaceratorRecipe(
					miscHelper.getRecipeKey("gregtech_addon.ore_to_crushed_macerator", name),
					oreOredict, 1, crushedInfo, 2);
			if(Loader.isModLoaded("Railcraft") && rockCrusher) {
				rcHelper.registerRockCrusherRecipe(
						miscHelper.getRecipeKey("gregtech_addon.ore_to_crushed_rock_crusher", name),
						oreOredict, 1, new Object[] {
								crushedInfo, 2, 1F,
								extra1DustOredict, 1, 0.1F
						});
			}
			if(Loader.isModLoaded("ThermalExpansion") && pulverizer) {
				teHelper.registerPulverizerRecipe(
						miscHelper.getRecipeKey("gregtech_addon.ore_to_crushed_pulverizer", name),
						oreOredict, 1, crushedInfo, 2, extra1DustOredict, 1, 10, 2400);
			}
			// to_purified_crushed
			if(!material.getType().isCrystalline()) {
				helper.registerGregTechAddonRecipe(
						miscHelper.getRecipeKey("gregtech_addon.ore_to_purified_crushed_grinder", name),
						helper.recipeSettings(RecipeMaps.GRINDER).
						input(oreOredict, 1).
						fluidInput(FluidRegistry.WATER, 1000).
						output(purifiedCrushedInfo, material.getType().isDust() ? 4 : 2).
						output(extra1SmallDustOredict, 1).
						output(extra2SmallDustOredict, 1).
						energy(120).time(100));
			}
			ic2Helper.registerOreWashingRecipe(
					miscHelper.getRecipeKey("gregtech_addon.crushed_to_purified_crushed", name),
					crushedOredict, 1, 1000, new Object[] {
							purifiedCrushedInfo, 1, extra1TinyDustOredict, 1, "dustStone", 1,
					});
			// to_centrifuged_crushed
			ic2Helper.registerCentrifugeRecipe(
					miscHelper.getRecipeKey("gregtech_addon.crushed_to_centrifuged_crushed", name),
					crushedOredict, 1, 2000, new Object[] {
							centrifugedCrushedInfo, 1, extra2TinyDustOredict, 1, "dustStone", 1,
					});
			ic2Helper.registerCentrifugeRecipe(
					miscHelper.getRecipeKey("gregtech_addon.purified_crushed_to_centrifuged_crushed", name),
					purifiedCrushedOredict, 1, 2000, new Object[] {
							centrifugedCrushedInfo, 1, extra2TinyDustOredict, 1,
					});
			// to_impure_dust
			ic2Helper.registerMaceratorRecipe(
					miscHelper.getRecipeKey("gregtech_addon.crushed_to_impure_dust_macerator", name),
					crushedOredict, 1, impureDustInfo, 1);
			if(Loader.isModLoaded("Railcraft") && rockCrusher) {
				rcHelper.registerRockCrusherRecipe(
						miscHelper.getRecipeKey("gregtech_addon.crushed_to_impure_dust_rock_crusher", name),
						crushedOredict, 1, new Object[] {
								impureDustInfo, 1, 1F,
								extra1DustOredict, 1, 0.1F
						});
			}
			if(Loader.isModLoaded("ThermalExpansion") && pulverizer) {
				teHelper.registerPulverizerRecipe(
						miscHelper.getRecipeKey("gregtech_addon.crushed_to_impure_dust_pulverizer", name),
						crushedOredict, 1, impureDustInfo, 1, extra1DustOredict, 1, 10, 2400);
			}
			// to_pure_dust
			ic2Helper.registerMaceratorRecipe(
					miscHelper.getRecipeKey("gregtech_addon.purified_crushed_to_pure_dust_macerator", name),
					purifiedCrushedOredict, 1, pureDustInfo, 1);
			if(Loader.isModLoaded("Railcraft") && rockCrusher) {
				rcHelper.registerRockCrusherRecipe(
						miscHelper.getRecipeKey("gregtech_addon.purified_crushed_to_pure_dust_rock_crusher", name),
						purifiedCrushedOredict, 1, new Object[] {
								pureDustInfo, 1, 1F,
								extra2DustOredict, 1, 0.1F
						});
			}
			if(Loader.isModLoaded("ThermalExpansion") && pulverizer) {
				teHelper.registerPulverizerRecipe(
						miscHelper.getRecipeKey("gregtech_addon.purified_crushed_to_pure_dust_pulverizer", name),
						purifiedCrushedOredict, 1, pureDustInfo, 1, extra2DustOredict, 1, 10, 2400);
			}
			// to_dust
			ic2Helper.registerMaceratorRecipe(
					miscHelper.getRecipeKey("gregtech_addon.centrifuged_crushed_to_dust_macerator", name),
					centrifugedCrushedOredict, 1, dustOredict, 1);
			if(Loader.isModLoaded("Railcraft") && rockCrusher) {
				rcHelper.registerRockCrusherRecipe(
						miscHelper.getRecipeKey("gregtech_addon.centrifuged_crushed_to_dust_rock_crusher", name),
						centrifugedCrushedOredict, 1, new Object[] {
								dustOredict, 1, 1F,
								extra3DustOredict, 1, 0.1F
						});
			}
			if(Loader.isModLoaded("ThermalExpansion") && pulverizer) {
				teHelper.registerPulverizerRecipe(
						miscHelper.getRecipeKey("gregtech_addon.centrifuged_crushed_to_dust_pulverizer", name),
						centrifugedCrushedOredict, 1, dustOredict, 1, extra3DustOredict, 1, 10, 2400);
			}
			helper.registerGregTechAddonRecipe(
					miscHelper.getRecipeKey("gregtech_addon.impure_dust_to_dust", name),
					helper.recipeSettings(RecipeMaps.CENTRIFUGE).
					input(impureDustOredict, 1).
					output(dustOredict, 1).
					output(extra1TinyDustOredict, 1).
					energy(5).time(800));
			helper.registerGregTechAddonRecipe(
					miscHelper.getRecipeKey("gregtech_addon.pure_dust_to_dust", name),
					helper.recipeSettings(RecipeMaps.CENTRIFUGE).
					input(pureDustOredict, 1).
					output(dustOredict, 1).
					output(extra2TinyDustOredict, 1).
					energy(5).time(800));
			// to_nugget
			if(material.getType().isIngot()) {
				api.registerSmeltingRecipe(
						miscHelper.getRecipeKey("gregtech_addon.crushed_to_nugget", name),
						crushedOredict, nuggetOredict, 10, 0F);
				api.registerSmeltingRecipe(
						miscHelper.getRecipeKey("gregtech_addon.purified_crushed_to_nugget", name),
						purifiedCrushedOredict, nuggetOredict, 10, 0F);
				api.registerSmeltingRecipe(
						miscHelper.getRecipeKey("gregtech_addon.centrifuged_crushed_to_nugget", name),
						centrifugedCrushedOredict, nuggetOredict, 10, 0F);
			}
			// to_material
			if(material.getType().isIngot()) {
				api.registerSmeltingRecipe(
						miscHelper.getRecipeKey("gregtech_addon.impure_dust_to_material", name),
						impureDustOredict, materialOredict, 1, 0F);
				api.registerSmeltingRecipe(
						miscHelper.getRecipeKey("gregtech_addon.pure_dust_to_material", name),
						pureDustOredict, materialOredict, 1, 0F);
			}
			if(material.getType().isCrystalline()) {
				helper.registerGregTechAddonRecipe(
						miscHelper.getRecipeKey("gregtech_addon.ore_to_material", name),
						helper.recipeSettings(RecipeMaps.GRINDER).
						input(oreOredict, 1).
						fluidInput(FluidRegistry.WATER, 1000).
						output(materialOredict, 1).
						output(extra1SmallDustOredict, 6).
						output(extra2SmallDustOredict, 2).
						energy(120).time(100));
			}
		}
	}
}
