package thelm.jaopca.gt5.compat.immersiveengineering;

import thelm.jaopca.gt5.compat.immersiveengineering.recipes.CrusherRecipeAction;
import thelm.jaopca.utils.ApiImpl;

public class ImmersiveEngineeringHelper {

	public static final ImmersiveEngineeringHelper INSTANCE = new ImmersiveEngineeringHelper();

	private ImmersiveEngineeringHelper() {}

	public boolean registerCrusherRecipe(String key, Object input, Object[] output, int energy) {
		return ApiImpl.INSTANCE.registerRecipe(key, new CrusherRecipeAction(key, input, output, energy));
	}
}
