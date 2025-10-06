package ru.blatfan.blatblock.common.block.autogenerator;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import ru.blatfan.blatapi.common.block.BlockISSimpleInventory;
import ru.blatfan.blatapi.utils.BaseItemStackHandler;
import ru.blatfan.blatapi.utils.ItemHelper;
import ru.blatfan.blatblock.BlatBlock;
import ru.blatfan.blatblock.common.BBRegistry;
import ru.blatfan.blatblock.common.block.blatgenerator.BlatGeneratorBlockEntity;
import ru.blatfan.blatblock.common.data.BlatBlockLayer;
import ru.blatfan.blatblock.common.data.BBLayerManager;
import ru.blatfan.blatblock.common.events.GeneratorEvents;
import ru.blatfan.blatblock.common.item.GeneratorUpgradeItem;
import ru.blatfan.blatblock.common.network.BBHandler;
import ru.blatfan.blatblock.common.network.BBParticlePacket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AutoGeneratorBlockEntity extends BlockISSimpleInventory {
    private int progress = 0;
    private int progressMax = 1;
    private ResourceLocation cachedBBL = null;
    private int cachedBBLLevel = 0;
    private final float[] cachedModifiers = new float[GeneratorUpgradeItem.Type.values().length];
    private boolean modifiersCached = false;
    
    private static final int STORAGE_SLOTS = 27;
    private static final int UPGRADE_SLOTS_START = 27;
    private static final int UPGRADE_SLOTS_END = 31;
    private static final int TOTAL_SLOTS = 31;
    
    public AutoGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(BBRegistry.BLOCK_ENTITIES.AUTO_GENERATOR.get(), pos, state);
    }
    
    @Override
    protected BaseItemStackHandler createItemHandler() {
        return BaseItemStackHandler.create(TOTAL_SLOTS, this::setChanged);
    }
    
    public int tickInterval(AutoGeneratorBlock.Type type) {
        float speedMod = getCachedModifier(GeneratorUpgradeItem.Type.SPEED);
        int baseRate = switch (type) {
            case BASIC -> BlatBlock.ConfigCommon.BASIC_TICK_RATE.get();
            case IMPROVED -> BlatBlock.ConfigCommon.IMPROVED_TICK_RATE.get();
            case PERFECT -> BlatBlock.ConfigCommon.PERFECT_TICK_RATE.get();
        };
        return Math.max(1, Math.round(baseRate * 20 / speedMod));
    }
    
    public void tick(Level level, BlockPos pos, AutoGeneratorBlock.Type type) {
        if (level.isClientSide) return;
        
        if (cachedBBLLevel == -1) cachedBBL = null;
        if (cachedBBL == null) updateBBLData();
        
        if (cachedBBL != null && hasFreeSlots() > -1) {
            progressMax = tickInterval(type);
            progress++;
        } else
            resetProgress();
        
        if (progress >= progressMax && cachedBBL != null) {
            progress = 0;
            generateItems(level, pos);
        }
    }
    
    private void generateItems(Level level, BlockPos pos) {
        BlatBlockLayer layer = BBLayerManager.get(cachedBBL);
        if (layer == BBLayerManager.NULL_BBL) return;
        
        Random random = new Random();
        generateBlocks(level, pos, layer, random);
        generateEntityDrops(level, pos, layer, random);
    }
    
    private void generateBlocks(Level level, BlockPos pos, BlatBlockLayer layer, Random random) {
        BlockState dropState = layer.getRandBlock(random, cachedBBLLevel);
        if (dropState.getBlock() instanceof LiquidBlock) return;
        
        ItemStack drop = GeneratorEvents.dropStack(
            dropState.getBlock(),
            getCachedModifier(GeneratorUpgradeItem.Type.TAG) >= 1
        );
        
        if (drop.isEmpty()) return;
        
        int fortuneAmount = random.nextInt(Math.max(1, (int) getCachedModifier(GeneratorUpgradeItem.Type.FORTUNE)));
        drop.setCount(Math.max(1, fortuneAmount));
        
        addItemToStorage(drop);
        sendParticlePacket(level, pos, drop);
    }
    
    private void generateEntityDrops(Level level, BlockPos pos, BlatBlockLayer layer, Random random) {
        float entityMod = getCachedModifier(GeneratorUpgradeItem.Type.ENTITY);
        if (entityMod <= 1) return;
        
        EntityType<?> entityType = layer.getRandEntity(random, cachedBBLLevel);
        if (entityType == null) return;
        
        LivingEntity entity = (LivingEntity) entityType.create(level);
        if (entity == null) return;
        
        DamageSource damageSource = level.damageSources().generic();
        List<ItemEntity> drops = new ArrayList<>();
        
        entity.captureDrops(drops);
        entity.dropFromLootTable(damageSource, true);
        entity.dropCustomDeathLoot(damageSource, random.nextInt((int) entityMod), true);
        
        for (ItemEntity itemEntity : entity.captureDrops(null)) {
            ItemStack drop = itemEntity.getItem();
            if (addItemToStorage(drop))
                sendParticlePacket(level, pos, drop);
        }
    }
    
    private boolean addItemToStorage(ItemStack stack) {
        int slot = hasFreeSlots(stack);
        if (slot == -1) return false;
        
        ItemStack existing = getItemHandler().getStackInSlot(slot);
        ItemStack combined = ItemHelper.withSize(stack,
            stack.getCount() + existing.getCount(), false);
        getItemHandler().setStackInSlot(slot, combined);
        return true;
    }
    
    private void sendParticlePacket(Level level, BlockPos pos, ItemStack stack) {
        BBHandler.sendToTracking(level, pos.above(),
            new BBParticlePacket(pos.above().getCenter().add(0, -0.45, 0), stack));
    }
    
    public void updateBBLData() {
        if (level == null) return;
        
        BlockPos[] checkPositions = {
            getBlockPos().offset(1, -1, 0),
            getBlockPos().offset(-1, -1, 0),
            getBlockPos().offset(0, -1, 1),
            getBlockPos().offset(0, -1, -1)
        };
        
        for (BlockPos checkPos : checkPositions) {
            BlockEntity entity = level.getBlockEntity(checkPos);
            if (entity instanceof BlatGeneratorBlockEntity generator) {
                cachedBBL = generator.getCurrentLayer();
                cachedBBLLevel = generator.getCurrentLevel();
                BlatBlock.LOGGER.debug("Updated BBL data: {} level {}",
                    cachedBBL != null ? cachedBBL.toString() : "null", cachedBBLLevel);
                return;
            }
        }
    }
    
    public float getCachedModifier(GeneratorUpgradeItem.Type type) {
        if (!modifiersCached)
            updateModifierCache();
        return cachedModifiers[type.ordinal()];
    }
    
    private void updateModifierCache() {
        Arrays.fill(cachedModifiers, 1.0f);
        
        for (ItemStack stack : getUpgrades())
            if (stack.getItem() instanceof GeneratorUpgradeItem upgrade) {
                int index = upgrade.getType().ordinal();
                cachedModifiers[index] += upgrade.getQuality();
            }
        
        modifiersCached = true;
    }
    
    public List<ItemStack> getUpgrades() {
        List<ItemStack> upgrades = new ArrayList<>();
        for (int i = UPGRADE_SLOTS_START; i < UPGRADE_SLOTS_END; i++) {
            ItemStack stack = getItemHandler().getStackInSlot(i);
            if (stack.getItem() instanceof GeneratorUpgradeItem) {
                upgrades.add(stack);
            }
        }
        return upgrades;
    }
    
    @Override
    public void setChanged() {
        super.setChanged();
        updateBBLData();
        invalidateModifierCache();
    }
    
    private void invalidateModifierCache() {
        modifiersCached = false;
    }
    
    private void resetProgress() {
        progress = 0;
        progressMax = 0;
    }
    
    public int hasFreeSlots(ItemStack stack) {
        for (int i = 0; i < STORAGE_SLOTS; i++) {
            ItemStack slot = getItemHandler().getStackInSlot(i);
            if (slot.isEmpty() ||
                (ItemHelper.areStacksEqual(slot, stack) && slot.getCount() + stack.getCount() <= slot.getMaxStackSize()))
                return i;
        }
        return -1;
    }
    
    public int hasFreeSlots() {
        for (int i = 0; i < STORAGE_SLOTS; i++)
            if (getItemHandler().getStackInSlot(i).isEmpty())
                return i;
        return -1;
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("progress", progress);
        if (cachedBBL != null) {
            tag.putString("cachedBBL", cachedBBL.toString());
            tag.putInt("cachedBBLLevel", cachedBBLLevel);
        }
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        progress = tag.getInt("progress");
        if (tag.contains("cachedBBL")) {
            cachedBBL = ResourceLocation.tryParse(tag.getString("cachedBBL"));
            cachedBBLLevel = tag.getInt("cachedBBLLevel");
        }
        invalidateModifierCache();
    }
    
    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> progressMax;
                default -> 0;
            };
        }
        
        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> progressMax = value;
            }
        }
        
        @Override
        public int getCount() {
            return 2;
        }
    };
    
    public ContainerData getData() {
        return dataAccess;
    }
}
