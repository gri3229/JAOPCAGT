package thelm.jaopca.gtceu.compat.gtceu.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gregtechceu.gtceu.forge.GTCEuForge;

import thelm.jaopca.gtceu.compat.gtceu.GTCEuDataModule;

@Mixin(GTCEuForge.class)
public class GTCEuForgeMixin {

	@Inject(method = "<init>", at = @At("RETURN"))
	public void afterConstruct(CallbackInfo info) {
		GTCEuDataModule.onGTCEuInit();
	}
}
