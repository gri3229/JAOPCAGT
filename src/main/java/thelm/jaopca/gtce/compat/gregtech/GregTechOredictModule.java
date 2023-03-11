package thelm.jaopca.gtce.compat.gregtech;

import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.loaders.MaterialInfoLoader;
import gregtech.loaders.OreDictionaryLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import thelm.jaopca.api.oredict.IOredictModule;
import thelm.jaopca.api.oredict.JAOPCAOredictModule;
import thelm.jaopca.utils.ApiImpl;
import thelm.jaopca.utils.MiscHelper;

@JAOPCAOredictModule(modDependencies = "gregtech@(,2)")
public class GregTechOredictModule implements IOredictModule {

	@Override
	public String getName() {
		return "gregtech";
	}

	@Override
	public void register() {
		MetaItems.registerOreDict();
		MetaBlocks.registerOreDict();
		OreDictionaryLoader.init();
		MaterialInfoLoader.init();
	}
}
