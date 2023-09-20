package thelm.jaopca.gtceu.compat.gregtech;

import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.items.ToolItems;
import gregtech.loaders.MaterialInfoLoader;
import gregtech.loaders.OreDictionaryLoader;
import thelm.jaopca.api.oredict.IOredictModule;
import thelm.jaopca.api.oredict.JAOPCAOredictModule;

@JAOPCAOredictModule(modDependencies = "gregtech@[2,)")
public class GregTechOredictModule implements IOredictModule {

	@Override
	public String getName() {
		return "gregtech";
	}

	@Override
	public void register() {
		MetaItems.registerOreDict();
        ToolItems.registerOreDict();
		MetaBlocks.registerOreDict();
		OreDictionaryLoader.init();
		MaterialInfoLoader.init();
	}
}
