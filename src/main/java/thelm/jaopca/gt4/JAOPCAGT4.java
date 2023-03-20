package thelm.jaopca.gt4;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(
		modid = JAOPCAGT4.MOD_ID,
		name = JAOPCAGT4.NAME,
		version = JAOPCAGT4.VERSION,
		dependencies = JAOPCAGT4.DEPENDENCIES
		)
public class JAOPCAGT4 {

	public static final String MOD_ID = "jaopcagt4";
	public static final String NAME = "JAOPCAGT4";
	public static final String VERSION = "1.7.10-1.0.0.2";
	public static final String DEPENDENCIES = "required-before:gregtech_addon;required-before:jaopca@[1.7.10-W.0.3,);";
}
