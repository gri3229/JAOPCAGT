package thelm.jaopca.gtceu.compat.gtceu.items;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import thelm.jaopca.api.forms.IForm;
import thelm.jaopca.api.items.IItemFormSettings;
import thelm.jaopca.api.materials.IMaterial;
import thelm.jaopca.items.JAOPCAItem;
import thelm.jaopca.utils.MiscHelper;

public class JAOPCAWashableItem extends JAOPCAItem {

	private final ResourceLocation washToLocation;

	public JAOPCAWashableItem(IForm form, IMaterial material, IItemFormSettings settings, String washToForm) {
		super(form, material, settings);
		washToLocation = MiscHelper.INSTANCE.getTagLocation(washToForm, material.getName());
	}

	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
		if(entity.getLevel().isClientSide) {
			return false;
		}
		BlockPos blockPos = entity.blockPosition();
		BlockState blockState = entity.getLevel().getBlockState(blockPos);
		int waterLevel = blockState.getBlock() == Blocks.WATER_CAULDRON ? blockState.getValue(LayeredCauldronBlock.LEVEL) : 0;
		if(waterLevel == 0) {
			return false;
		}
		LayeredCauldronBlock.lowerFillLevel(blockState, entity.getLevel(), blockPos);
		ItemStack replacementStack = MiscHelper.INSTANCE.getItemStack(washToLocation, entity.getItem().getCount());
		entity.setItem(replacementStack);
		return false;
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
		super.appendHoverText(stack, level, tooltip, isAdvanced);
		if(getForm().getSecondaryName().contains("crushed")) {
			tooltip.add(Component.translatable("metaitem.crushed.tooltip.purify"));
		}
		if(getForm().getSecondaryName().contains("dust")) {
			tooltip.add(Component.translatable("metaitem.dust.tooltip.purify"));
		}
	}
}
