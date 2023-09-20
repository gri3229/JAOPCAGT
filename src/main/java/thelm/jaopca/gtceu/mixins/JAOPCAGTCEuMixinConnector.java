package thelm.jaopca.gtceu.mixins;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

import thelm.jaopca.gtceu.JAOPCAGTCEu;

public class JAOPCAGTCEuMixinConnector implements IMixinConnector {

	@Override
	public void connect() {
		JAOPCAGTCEu.mixinLoaded = true;
		Mixins.addConfiguration("jaopca.gtceu.mixins.json");
	}
}
