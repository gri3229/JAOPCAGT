package thelm.jaopcagtceu.compat.gregtech.items;

import java.util.List;

import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.jaopca.api.forms.IForm;
import thelm.jaopca.api.items.IItemFormSettings;
import thelm.jaopca.api.materials.IMaterial;
import thelm.jaopca.items.JAOPCAItem;
import thelm.jaopca.utils.MiscHelper;

public class JAOPCAWashableItem extends JAOPCAItem {

	private final String washToOredict;

	public JAOPCAWashableItem(IForm form, IMaterial material, IItemFormSettings settings, String washToPrefix) {
		super(form, material, settings);
		washToOredict = MiscHelper.INSTANCE.getOredictName(washToPrefix, material.getName());
	}

	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {
		if(entityItem.getEntityWorld().isRemote) {
			return false;
		}
		BlockPos blockPos = new BlockPos(entityItem);
		IBlockState blockState = entityItem.getEntityWorld().getBlockState(blockPos);
		int waterLevel = blockState.getBlock() instanceof BlockCauldron ? blockState.getValue(BlockCauldron.LEVEL) : 0;
		if(waterLevel == 0) {
			return false;
		}
		entityItem.getEntityWorld().setBlockState(blockPos, blockState.withProperty(BlockCauldron.LEVEL, waterLevel-1));
		ItemStack replacementStack = MiscHelper.INSTANCE.getItemStack(washToOredict, entityItem.getItem().getCount());
		entityItem.setItem(replacementStack);
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		if(washToOredict.startsWith("dust")) {
			tooltip.add(I18n.translateToLocal("metaitem.dust.tooltip.purify"));
		}
	}
}
