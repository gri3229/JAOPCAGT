package thelm.jaopca.gtceu.compat.gtceu;

import java.util.ArrayList;
import java.util.Map;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterial;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix.LoaderType;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.pipelike.cable.Insulation;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.FluidPipeType;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import thelm.jaopca.api.data.IDataModule;
import thelm.jaopca.api.data.JAOPCADataModule;
import thelm.jaopca.utils.ApiImpl;

@JAOPCADataModule(modDependencies = "gtceu")
public class GTCEuDataModule implements IDataModule {

	private static final Object LOCK = new Object();
	private static boolean isGTCEuInit = false;

	public static void onGTCEuInit() {
		synchronized(LOCK) {
			isGTCEuInit = true;
			LOCK.notify();
		}
	}

	@Override
	public String getName() {
		return "gtceu";
	}

	@Override
	public void register() {
		//I'm not sure if this is a good idea
		//We need GTCEu to finish doing materials to infer tags and blacklists though
		//Code is taken from GTCEu itself
		synchronized(LOCK) {
			if(!isGTCEuInit) {
				try {
					LOCK.wait();
				}
				catch(InterruptedException e) {}
			}
		}
		//We add tag names that fit the existing scheme
		TagPrefix.oreEndstone.unformattedTagPath(LoaderType.FORGE, "ores_in_ground/end_stone");
		TagPrefix.ingotHot.defaultTagPath(LoaderType.FORGE, "hot_ingots/%s");
		TagPrefix.ingotHot.unformattedTagPath(LoaderType.FORGE, "hot_ingots");
		TagPrefix.gemChipped.defaultTagPath(LoaderType.FORGE, "chipped_gems/%s");
		TagPrefix.gemChipped.unformattedTagPath(LoaderType.FORGE, "chipped_gems");
		TagPrefix.gemFlawed.defaultTagPath(LoaderType.FORGE, "flawed_gems/%s");
		TagPrefix.gemFlawed.unformattedTagPath(LoaderType.FORGE, "flawed_gems");
		TagPrefix.gemFlawless.defaultTagPath(LoaderType.FORGE, "flawless_gems/%s");
		TagPrefix.gemFlawless.unformattedTagPath(LoaderType.FORGE, "flawless_gems");
		TagPrefix.gemExquisite.defaultTagPath(LoaderType.FORGE, "exquisite_gems/%s");
		TagPrefix.gemExquisite.unformattedTagPath(LoaderType.FORGE, "exquisite_gems");
		TagPrefix.dustSmall.defaultTagPath(LoaderType.FORGE, "small_dusts/%s");
		TagPrefix.dustSmall.unformattedTagPath(LoaderType.FORGE, "small_dusts");
		TagPrefix.dustTiny.defaultTagPath(LoaderType.FORGE, "tiny_dusts/%s");
		TagPrefix.dustTiny.unformattedTagPath(LoaderType.FORGE, "tiny_dusts");
		TagPrefix.dustImpure.defaultTagPath(LoaderType.FORGE, "impure_dusts/%s");
		TagPrefix.dustImpure.unformattedTagPath(LoaderType.FORGE, "impure_dusts");
		TagPrefix.dustPure.defaultTagPath(LoaderType.FORGE, "pure_dusts/%s");
		TagPrefix.dustPure.unformattedTagPath(LoaderType.FORGE, "pure_dusts");
		TagPrefix.plateDense.defaultTagPath(LoaderType.FORGE, "dense_plates/%s");
		TagPrefix.plateDense.unformattedTagPath(LoaderType.FORGE, "dense_plates");
		TagPrefix.plateDouble.defaultTagPath(LoaderType.FORGE, "double_plates/%s");
		TagPrefix.plateDouble.unformattedTagPath(LoaderType.FORGE, "double_plates");
		TagPrefix.rodLong.defaultTagPath(LoaderType.FORGE, "long_rods/%s");
		TagPrefix.rodLong.unformattedTagPath(LoaderType.FORGE, "long_rods");
		TagPrefix.springSmall.defaultTagPath(LoaderType.FORGE, "small_springs/%s");
		TagPrefix.springSmall.unformattedTagPath(LoaderType.FORGE, "small_springs");
		TagPrefix.wireFine.defaultTagPath(LoaderType.FORGE, "fine_wires/%s");
		TagPrefix.wireFine.unformattedTagPath(LoaderType.FORGE, "fine_wires");
		TagPrefix.gearSmall.defaultTagPath(LoaderType.FORGE, "small_gears/%s");
		TagPrefix.gearSmall.unformattedTagPath(LoaderType.FORGE, "small_gears");
		//Let JAOPCA know what item tags will be injected by GTCEu
		for(Map.Entry<UnificationEntry, ArrayList<ItemLike>> entry : ChemicalHelper.UNIFICATION_ENTRY_ITEM.entrySet()) {
			if(!entry.getValue().isEmpty()) {
				for(TagKey<Item> materialTag : entry.getKey().tagPrefix.getItemTags(entry.getKey().material)) {
					ApiImpl.INSTANCE.registerDefinedItemTag(materialTag.location());
				}
			}
		}
		for(GTToolType toolType : GTToolType.values()) {
			ApiImpl.INSTANCE.registerDefinedItemTag(toolType.itemTag.location());
		}
		for(MarkerMaterial color : MarkerMaterials.Color.VALUES) {
			for(TagKey<Item> materialTag : TagPrefix.lens.getItemTags(color)) {
				ApiImpl.INSTANCE.registerDefinedItemTag(materialTag.location());
			}
		}
		for(Material material : GTRegistries.MATERIALS) {
			if((material.hasProperty(PropertyKey.INGOT) || material.hasProperty(PropertyKey.GEM) || material.hasFlag(MaterialFlags.FORCE_GENERATE_BLOCK)) && !TagPrefix.block.isIgnored(material)) {
				for(TagKey<Item> materialTag : TagPrefix.block.getItemTags(material)) {
					ApiImpl.INSTANCE.registerDefinedItemTag(materialTag.location());
				}
			}
			if(material.hasProperty(PropertyKey.DUST) && material.hasFlag(MaterialFlags.GENERATE_FRAME)) {
				for(TagKey<Item> materialTag : TagPrefix.frameGt.getItemTags(material)) {
					ApiImpl.INSTANCE.registerDefinedItemTag(materialTag.location());
				}
			}
			if(material.hasProperty(PropertyKey.ORE)) {
				if(!TagPrefix.rawOreBlock.isIgnored(material) && TagPrefix.rawOreBlock.generationCondition().test(material)) {
					for(TagKey<Item> materialTag : TagPrefix.rawOreBlock.getItemTags(material)) {
						ApiImpl.INSTANCE.registerDefinedItemTag(materialTag.location());
					}
				}
				OreProperty oreProperty = material.getProperty(PropertyKey.ORE);
				for(Map.Entry<TagPrefix, TagPrefix.OreType> ore : TagPrefix.ORES.entrySet()) {
					if(ore.getKey().isIgnored(material)) {
						continue;
					}
					for(TagKey<Item> materialTag : ore.getKey().getItemTags(material)) {
						ApiImpl.INSTANCE.registerDefinedItemTag(materialTag.location());
					}
				}
			}
			for(Insulation insulation : Insulation.values()) {
				if(material.hasProperty(PropertyKey.WIRE) && !insulation.tagPrefix.isIgnored(material)) {
					for(TagKey<Item> materialTag : insulation.tagPrefix.getItemTags(material)) {
						ApiImpl.INSTANCE.registerDefinedItemTag(materialTag.location());
					}
				}
			}
			for(FluidPipeType fluidPipeType : FluidPipeType.values()) {
				if(material.hasProperty(PropertyKey.FLUID_PIPE) && !fluidPipeType.tagPrefix.isIgnored(material)) {
					for(TagKey<Item> materialTag : fluidPipeType.tagPrefix.getItemTags(material)) {
						ApiImpl.INSTANCE.registerDefinedItemTag(materialTag.location());
					}
				}
			}
			for(TagPrefix tagPrefix : TagPrefix.values()) {
				if(tagPrefix.doGenerateItem(material)) {
					for(TagKey<Item> materialTag : tagPrefix.getItemTags(material)) {
						ApiImpl.INSTANCE.registerDefinedItemTag(materialTag.location());
					}
				}
			}
			//Blacklists
			if(material.hasProperty(PropertyKey.ORE)) {
				GTCEuModule.BLACKLIST.add(material.getName());
			}
			GTCEuCompatModule.BLACKLIST.add(material.getName());
			if(material.hasFlag(MaterialFlags.GENERATE_PLATE)) {
				GTCEuCompatModule.PLATE_BLACKLIST.add(material.getName());
			}
			if(material.hasFlag(MaterialFlags.GENERATE_GEAR)) {
				GTCEuCompatModule.GEAR_BLACKLIST.add(material.getName());
			}
			if(material.hasFlag(MaterialFlags.GENERATE_ROD)) {
				GTCEuCompatModule.ROD_BLACKLIST.add(material.getName());
			}
			if(material.hasFlag(MaterialFlags.GENERATE_DENSE)) {
				GTCEuCompatModule.DENSE_BLACKLIST.add(material.getName());
			}
		}
	}
}
