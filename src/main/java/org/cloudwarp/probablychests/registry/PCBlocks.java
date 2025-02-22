package org.cloudwarp.probablychests.registry;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org.cloudwarp.probablychests.ProbablyChests;
import org.cloudwarp.probablychests.block.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class PCBlocks {
	private static final Map<Block, Identifier> BLOCKS = new LinkedHashMap<>();
	private static final Map<Item, Identifier> ITEMS = new LinkedHashMap<>();

	//---------------------------

	public static final Block LUSH_CHEST    = create("lush_chest",  new PCChestBlock(PCChestTypes.LUSH.setting(),     PCChestTypes.LUSH), true);
	public static final Block NORMAL_CHEST  = create("normal_chest", new PCChestBlock(PCChestTypes.NORMAL.setting(),  PCChestTypes.NORMAL), true);
	public static final Block ROCKY_CHEST   = create("rocky_chest",  new PCChestBlock(PCChestTypes.ROCKY.setting(),   PCChestTypes.ROCKY), true);
	public static final Block STONE_CHEST   = create("stone_chest",  new PCChestBlock(PCChestTypes.STONE.setting(),   PCChestTypes.STONE), true);
	public static final Block GOLD_CHEST    = create("gold_chest",   new PCChestBlock(PCChestTypes.GOLD.setting(),    PCChestTypes.GOLD), true);
	public static final Block NETHER_CHEST  = create("nether_chest", new PCChestBlock(PCChestTypes.NETHER.setting(),  PCChestTypes.NETHER), true);
	public static final Block SHADOW_CHEST  = create("shadow_chest", new PCChestBlock(PCChestTypes.SHADOW.setting(),  PCChestTypes.SHADOW), true);
	public static final Block ICE_CHEST     = create("ice_chest",    new PCChestBlock(PCChestTypes.ICE.setting(),     PCChestTypes.ICE), true);
	public static final Block CORAL_CHEST   = create("coral_chest",  new PCChestBlock(PCChestTypes.CORAL.setting(),   PCChestTypes.CORAL), true);

	//---------------------------

	public static final Block LUSH_POT = create("lush_pot", new PCPot(PCPotTypes.LUSH.setting(), PCVoxelShapes.POT_VOXELSHAPE), true);
	public static final Block NORMAL_POT = create("normal_pot", new PCPot(PCPotTypes.NORMAL.setting(), PCVoxelShapes.POT_VOXELSHAPE), true);
	public static final Block ROCKY_POT = create("rocky_pot", new PCPot(PCPotTypes.ROCKY.setting(), PCVoxelShapes.POT_VOXELSHAPE), true);
	public static final Block NETHER_POT = create("nether_pot", new PCPot(PCPotTypes.NETHER.setting(), PCVoxelShapes.POT_VOXELSHAPE), true);

	// ------------------------------

	private static <T extends Block> T create (String name, T block, boolean createItem) {
		BLOCKS.put(block, new Identifier(ProbablyChests.MOD_ID, name));
		if (createItem) {
			BlockItem blockItem = new BlockItem(block, new Item.Settings());
			ITEMS.put(blockItem, BLOCKS.get(block));
			ItemGroupEvents.modifyEntriesEvent(PCItemGroups.ITEM_GROUP).register(content -> {
				content.add(blockItem);
			});
		}
		return block;
	}

	public static void init () {
		BLOCKS.keySet().forEach(block -> Registry.register(Registries.BLOCK, BLOCKS.get(block), block));
		ITEMS.keySet().forEach(item -> Registry.register(Registries.ITEM, ITEMS.get(item), item));
	}

}
