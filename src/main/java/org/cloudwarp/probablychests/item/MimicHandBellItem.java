package org.cloudwarp.probablychests.item;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cloudwarp.probablychests.interfaces.PlayerEntityAccess;
import org.cloudwarp.probablychests.registry.PCSounds;
import org.cloudwarp.probablychests.registry.PCStatistics;

import java.util.List;

public class MimicHandBellItem extends Item {
	public MimicHandBellItem (Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock (ItemUsageContext context) {

		World world = context.getWorld();
		ItemPlacementContext itemPlacementContext = new ItemPlacementContext(context);
		BlockPos blockPos = itemPlacementContext.getBlockPos();

		if (world instanceof ServerWorld serverWorld && context.getPlayer() instanceof  ServerPlayerEntity player) {
			blockPos = blockPos.offset(context.getSide().getOpposite());
			if(serverWorld.getBlockState(blockPos).isOf(Blocks.AMETHYST_CLUSTER)){
				int amount = ((PlayerEntityAccess) player).abandonMimics();
				if(amount > 0){
					player.increaseStat(PCStatistics.ABANDONED_MIMICS,amount);
					Criteria.ITEM_USED_ON_BLOCK.trigger(player,blockPos,context.getStack());
				}
				playSound(world,blockPos,PCSounds.BELL_HIT_1);
			}
		}
		return ActionResult.success(world.isClient);
	}
	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		if(Screen.hasShiftDown()){
			tooltip.add(Text.translatable("item.probablychests.mimicHandBell.tooltip.shift"));
			tooltip.add(Text.translatable("item.probablychests.mimicHandBell.tooltip.shift2"));
			tooltip.add(Text.translatable("item.probablychests.mimicHandBell.tooltip.shift3"));
		}else{
			tooltip.add(Text.translatable("item.probablychests.shift.tooltip"));
		}
	}
	static void playSound (World world, BlockPos pos, SoundEvent soundEvent) {
		double d = (double) pos.getX() + 0.5;
		double e = (double) pos.getY() + 0.5;
		double f = (double) pos.getZ() + 0.5;

		world.playSound(null, d, e, f, soundEvent, SoundCategory.BLOCKS, 0.8f, world.random.nextFloat() * 0.1f + 0.9f);
	}
}
