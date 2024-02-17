package thelm.jaopca.gt5.compat.gregtech.items;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
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
		if(entityItem.worldObj.isRemote) {
			return false;
		}
		int x = MathHelper.floor_double(entityItem.posX);
		int y = MathHelper.floor_double(entityItem.posY);
		int z = MathHelper.floor_double(entityItem.posZ);
		Block block = entityItem.worldObj.getBlock(x, y, z);
		int waterLevel = block instanceof BlockCauldron ? entityItem.worldObj.getBlockMetadata(x, y, z) : 0;
		if(waterLevel == 0) {
			return false;
		}
		entityItem.worldObj.setBlockMetadataWithNotify(x, y, z, waterLevel-1, 3);
		ItemStack replacementStack = MiscHelper.INSTANCE.getItemStack(washToOredict, entityItem.getEntityItem().stackSize, false);
		entityItem.setEntityItemStack(replacementStack);
		return false;
	}

	@SuppressWarnings("rawtypes")
	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean isAdvanced) {
		super.addInformation(stack, player, tooltip, isAdvanced);
		if(getForm().getSecondaryName().startsWith("dust")) {
			tooltip.add(StatCollector.translateToLocal("metaitem.01.tooltip.purify"));
		}
	}
}
