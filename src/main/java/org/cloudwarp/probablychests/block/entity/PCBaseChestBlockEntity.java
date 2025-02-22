package org.cloudwarp.probablychests.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.ViewerCountManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.cloudwarp.probablychests.block.PCChestTypes;
import org.cloudwarp.probablychests.registry.PCProperties;
import org.cloudwarp.probablychests.screenhandlers.PCChestScreenHandler;
import org.cloudwarp.probablychests.utils.PCChestState;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class PCBaseChestBlockEntity extends LootableContainerBlockEntity implements GeoAnimatable {

    public static final RawAnimation CLOSED = RawAnimation.begin().then("closed", Animation.LoopType.LOOP);
    //public static final RawAnimation CLOSE = RawAnimation.begin().then("close").then("closed",Animation.LoopType.LOOP);
    public static final RawAnimation CLOSE = RawAnimation.begin().then("close", Animation.LoopType.HOLD_ON_LAST_FRAME);
    //public static final RawAnimation OPEN = RawAnimation.begin().then("open", false).then("opened",true);
    public static final RawAnimation OPEN = RawAnimation.begin().then("open", Animation.LoopType.HOLD_ON_LAST_FRAME);
    public static final RawAnimation OPENED = RawAnimation.begin().then("opened", Animation.LoopType.LOOP);
    public static final EnumProperty<PCChestState> CHEST_STATE = PCProperties.PC_CHEST_STATE;
    private static final String CONTROLLER_NAME = "chestController";
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public boolean isMimic = false;
    public boolean isNatural = false;
    public boolean hasBeenInteractedWith = false;
    public boolean hasMadeMimic = false;

    public boolean hasGoldLock = false;
    public boolean hasVoidLock = false;
    public boolean hasIronLock = false;
    public boolean isLocked = false;
    public UUID owner = null;
    private final ViewerCountManager stateManager = new ViewerCountManager() {

        @Override
        protected void onContainerOpen(World world, BlockPos pos, BlockState state) {
            PCBaseChestBlockEntity.playSound(world, pos, state, SoundEvents.BLOCK_CHEST_OPEN);
        }

        @Override
        protected void onContainerClose(World world, BlockPos pos, BlockState state) {
            PCBaseChestBlockEntity.playSound(world, pos, state, SoundEvents.BLOCK_CHEST_CLOSE);
        }

        @Override
        protected void onViewerCountUpdate(World world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
            PCBaseChestBlockEntity.this.onInvOpenOrClose(world, pos, state, oldViewerCount, newViewerCount);
        }

        @Override
        protected boolean isPlayerViewing(PlayerEntity player) {
            if (player.currentScreenHandler instanceof PCChestScreenHandler) {
                Inventory inventory = ((PCChestScreenHandler) player.currentScreenHandler).getInventory();
                return inventory == PCBaseChestBlockEntity.this;
            }
            return false;
        }
    };
    PCChestTypes type;
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(54, ItemStack.EMPTY);

    public PCBaseChestBlockEntity(PCChestTypes type, BlockPos pos, BlockState state) {
        super(type.getBlockEntityType(), pos, state);
        this.type = type;
        this.setHeldStacks(DefaultedList.ofSize(this.size(), ItemStack.EMPTY));
    }

    public static int getPlayersLookingInChestCount(BlockView world, BlockPos pos) {
        BlockEntity blockEntity;
        BlockState blockState = world.getBlockState(pos);
        if (blockState.hasBlockEntity() && (blockEntity = world.getBlockEntity(pos)) instanceof PCBaseChestBlockEntity) {
            return ((PCBaseChestBlockEntity) blockEntity).stateManager.getViewerCount();
        }
        return 0;
    }

    public static void copyInventory(PCBaseChestBlockEntity from, PCBaseChestBlockEntity to) {
        DefaultedList<ItemStack> defaultedList = from.getHeldStacks();
        from.setHeldStacks(to.getHeldStacks());
        to.setHeldStacks(defaultedList);
    }

    public static void playSound(World world, BlockPos pos, BlockState state, SoundEvent soundEvent) {
        double d = (double) pos.getX() + 0.5;
        double e = (double) pos.getY() + 0.5;
        double f = (double) pos.getZ() + 0.5;

        world.playSound(null, d, e, f, soundEvent, SoundCategory.BLOCKS, 0.5f, world.random.nextFloat() * 0.1f + 0.9f);
    }

    public static void playSound(World world, BlockPos pos, BlockState state, SoundEvent soundEvent, float pitchRange) {
        double d = (double) pos.getX() + 0.5;
        double e = (double) pos.getY() + 0.5;
        double f = (double) pos.getZ() + 0.5;

        world.playSound(null, d, e, f, soundEvent, SoundCategory.BLOCKS, 0.5f, world.random.nextFloat() * 0.1f + pitchRange);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.readLootTable(nbt)) {
            Inventories.readNbt(nbt, this.inventory, registryLookup);
        }
        this.isMimic = nbt.getBoolean("isMimic");
        this.hasGoldLock = nbt.getBoolean("hasGoldLock");
        this.hasVoidLock = nbt.getBoolean("hasVoidLock");
        this.hasIronLock = nbt.getBoolean("hasIronLock");
        this.isLocked = nbt.getBoolean("isLocked");
        this.isNatural = nbt.getBoolean("isNatural");
        this.hasBeenInteractedWith = nbt.getBoolean("hasBeenOpened");
        this.hasMadeMimic = nbt.getBoolean("hasMadeMimic");
        if (nbt.contains("pc_owner")) {
            this.owner = nbt.getUuid("pc_owner");
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        if (!this.writeLootTable(nbt)) {
            Inventories.writeNbt(nbt, this.inventory, registryLookup);
        }
        nbt.putBoolean("isMimic", this.isMimic);
        nbt.putBoolean("hasGoldLock", this.hasGoldLock);
        nbt.putBoolean("hasVoidLock", this.hasVoidLock);
        nbt.putBoolean("hasIronLock", this.hasIronLock);
        nbt.putBoolean("isLocked", this.isLocked);
        nbt.putBoolean("isNatural", this.isNatural);
        nbt.putBoolean("hasBeenOpened", this.hasBeenInteractedWith);
        nbt.putBoolean("hasMadeMimic", this.hasMadeMimic);
        if (this.owner != null) {
            nbt.putUuid("pc_owner", this.owner);
        }
    }

    @Override
    public void onOpen(PlayerEntity player) {
        if (!this.removed && !player.isSpectator()) {
            this.stateManager.openContainer(player, this.getWorld(), this.getPos(), this.getCachedState());
        }
    }

    @Override
    public void onClose(PlayerEntity player) {
        if (!this.removed && !player.isSpectator()) {
            this.stateManager.closeContainer(player, this.getWorld(), this.getPos(), this.getCachedState());
        }
    }

    @Override
    protected DefaultedList<ItemStack> getHeldStacks() {
        return this.inventory;
    }

    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    public void onScheduledTick() {
        if (!this.removed) {
            this.stateManager.updateViewerCount(this.getWorld(), this.getPos(), this.getCachedState());
        }
    }


    protected void onInvOpenOrClose(World world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
        Block block = state.getBlock();
        world.addSyncedBlockEvent(pos, block, 1, newViewerCount);
        if (oldViewerCount != newViewerCount) {
            if (newViewerCount > 0) {
                world.setBlockState(pos, state.with(CHEST_STATE, PCChestState.OPENED));
            } else {
                world.setBlockState(pos, state.with(CHEST_STATE, PCChestState.CLOSED));
            }
        }
    }


    @Override
    public boolean onSyncedBlockEvent(int type, int data) {
        if (type == 1) {
            return true;
        }
        return super.onSyncedBlockEvent(type, data);
    }

    public PCChestState getChestState() {
        return this.getCachedState().get(PCBaseChestBlockEntity.CHEST_STATE);
    }

    public void setChestState(PCChestState state) {
        this.getWorld().setBlockState(this.getPos(), this.getCachedState().with(CHEST_STATE, state));
    }

    @Override
    @Nullable
    public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity player) {
        if (!hasBeenInteractedWith && player.isSpectator()) {
            return null;
        }
        if (this.checkUnlocked(player)) {
            generateLoot(inventory.player);
            return PCChestScreenHandler.createScreenHandler(syncId, inventory, this);
        }
        return null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<PCBaseChestBlockEntity> controller = new AnimationController<>(this, CONTROLLER_NAME, 7, animationEvent -> switch (getChestState()) {
            case CLOSED -> {
                animationEvent.getController().setOverrideEasingType(EasingType.EASE_OUT_SINE);
                yield animationEvent.setAndContinue(CLOSED);
            }
            case OPENED -> animationEvent.setAndContinue(OPENED);
            default -> PlayState.CONTINUE;
        });
        controllers.add(controller);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object o) {
        return 0;
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory inventory) {
        return PCChestScreenHandler.createScreenHandler(syncId, inventory, this);
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public int size() {
        return 54;
    }

    public PCChestTypes type() {
        return type;
    }


}
