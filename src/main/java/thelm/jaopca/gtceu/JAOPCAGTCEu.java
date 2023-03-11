package thelm.jaopca.gtceu;

import net.minecraftforge.fml.common.Mod;

@Mod(
		modid = JAOPCAGTCEu.MOD_ID,
		name = JAOPCAGTCEu.NAME,
		version = JAOPCAGTCEu.VERSION,
		dependencies = JAOPCAGTCEu.DEPENDENCIES
		)
public class JAOPCAGTCEu {

	public static final String MOD_ID = "jaopcagtce";
	public static final String NAME = "JAOPCAGTCE";
	public static final String VERSION = "1.12.2-1.0.0.1";
	public static final String DEPENDENCIES = "required-before:jaopca@[1.12.2-2.3.6,);required-before:gregtech@[2,)";
}
