package thelm.jaopca.gtceu;

import net.minecraftforge.fml.common.Mod;

@Mod(
		modid = JAOPCAGTCEu.MOD_ID,
		name = JAOPCAGTCEu.NAME,
		version = JAOPCAGTCEu.VERSION,
		dependencies = JAOPCAGTCEu.DEPENDENCIES
		)
public class JAOPCAGTCEu {

	public static final String MOD_ID = "jaopcagtceu";
	public static final String NAME = "JAOPCAGTCEu";
	public static final String VERSION = "1.12.2-0@VERSION@";
	public static final String DEPENDENCIES = "required-before:jaopca@[1.12.2-2.3.6,);required-before:gregtech@[2,)";
}
