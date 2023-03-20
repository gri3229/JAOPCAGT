package thelm.jaopca.gt5;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(
		modid = JAOPCAGT5.MOD_ID,
		name = JAOPCAGT5.NAME,
		version = JAOPCAGT5.VERSION,
		dependencies = JAOPCAGT5.DEPENDENCIES
		)
public class JAOPCAGT5 {

	public static final String MOD_ID = "jaopcagt5";
	public static final String NAME = "JAOPCAGT5";
	public static final String VERSION = "1.7.10-1.0.0.0";
	public static final String DEPENDENCIES = "required-before:gregtech;required-before:jaopca@[1.7.10-W.0.3,);";
}
