package ru.blatfan.blatblock.common.block.blatgenerator;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import ru.blatfan.blatapi.fluffy_fur.common.block.entity.BlockEntityBase;
import ru.blatfan.blatapi.utils.Text;
import ru.blatfan.blatblock.BlatBlock;
import ru.blatfan.blatblock.common.BBRegistry;
import ru.blatfan.blatblock.common.block.autogenerator.AutoGeneratorBlockEntity;
import ru.blatfan.blatblock.common.data.BlatBlockLayer;
import ru.blatfan.blatblock.common.data.BBLayerManager;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BlatGeneratorBlockEntity extends BlockEntityBase {
    @Getter@Setter
    private ResourceLocation currentLayer = BlatBlock.loc("null");
    @Getter
    private final Map<ResourceLocation, Integer> minedBlocks = new HashMap<>();
    
    private int tickCounter = 0;
    
    public BlatGeneratorBlockEntity(BlockPos pos, BlockState blockState) {
        super(BBRegistry.BLOCK_ENTITIES.BLAT_GENERATOR.get(), pos, blockState);
        this.random = new Random();
    }
    
    public void tick(Level level, BlockPos pos) {
        tickCounter++;
        if(currentLayer.getPath().equals("null")) {
            currentLayer = BBLayerManager.getBaseId();
            setMinedBlock(currentLayer, 1);
        }
        
        if (level.isClientSide) {
            if (tickCounter % 40 == 0 && random.nextInt(5) == 0) {
                level.playLocalSound(pos, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.BLOCKS, 1, 1, true);
            }
        } else {
            if (level.getBlockState(pos.above()).isAir() && getMinedBlock()>0) {
                Player player = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 5, false);
                addMinedBlock(1);
                BlatBlockLayer lvlData = BBLayerManager.get(this.currentLayer);
                if(player!=null && player.blockPosition().distSqr(Vec3i.ZERO) <= 2)
                    player.setDeltaMovement(new Vec3(0, 1, 0));
                if (lvlData != null) {
                    lvlData.rand(player, level, pos.above(), random, getCurrentLevel());
                } else {
                    BlatBlock.LOGGER.warn("No BlatBlockLayer data for {}", this.currentLayer);
                    this.currentLayer = BBLayerManager.getBaseId();
                }
                setChanged();
            }
        }
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("block_level", currentLayer.toString());
        
        CompoundTag minedTag = new CompoundTag();
        for (Map.Entry<ResourceLocation, Integer> entry : minedBlocks.entrySet()) {
            minedTag.putInt(entry.getKey().toString(), entry.getValue());
        }
        tag.put("mined_block", minedTag);
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        try {
            String lvlStr = tag.getString("block_level");
            if (lvlStr.isEmpty()) {
                BlatBlock.LOGGER.warn("Empty block_level in NBT at {}", getBlockPos());
                currentLayer = BlatBlock.loc("null");
            } else {
                ResourceLocation parsed = ResourceLocation.tryParse(lvlStr);
                currentLayer = (parsed != null) ? parsed : BlatBlock.loc("null");
            }
            
            minedBlocks.clear();
            if (tag.contains("mined_block", CompoundTag.TAG_COMPOUND)) {
                CompoundTag minedTag = tag.getCompound("mined_block");
                for (ResourceLocation id : BBLayerManager.getAvailableIds()) {
                    int count = minedTag.getInt(id.toString());
                    minedBlocks.put(id, Math.max(0, count));
                }
            }
        } catch (Exception e) {
            BlatBlock.LOGGER.error("Error loading BlatGeneratorBlockEntity NBT at {}: {}", getBlockPos(), e.getMessage());
            resetToDefaults();
        }
    }
    
    private void resetToDefaults() {
        minedBlocks.clear();
        currentLayer = BBLayerManager.getBaseId();
    }
    
    public List<Component> getRenderText() {
        List<Component> list = new ArrayList<>();
        BlatBlockLayer bbl = BBLayerManager.NULL_BBL;
        
        try {
            bbl = BBLayerManager.get(currentLayer);
        } catch (Exception ignored) { }
        
        int currentLevel = getCurrentLevel();
        int minedBlocks = getMinedBlock();
        boolean isMaxLevel = currentLevel >= 99;
        
        String levelText = isMaxLevel ? Component.translatable("tooltip.blatblock.max").getString() : String.valueOf(currentLevel+1);
        list.add(Text.create("tooltip.blatblock.level", bbl.getTitle().getString(), levelText)
            .withColor(bbl.getTitleColor())
            .asComponent());
        
        if(!isMaxLevel)
            list.add(Text.create("tooltip.blatblock.mined_with_next", minedBlocks, (int) bbl.calcBlocks(currentLevel + 1))
                .withColor(bbl.getTitleColor())
                .asComponent());
        else
            list.add(Text.create("tooltip.blatblock.mined", minedBlocks)
                .withColor(bbl.getTitleColor())
                .asComponent());
        
        if (!isMaxLevel) {
            int levelBlocks = currentLevel>0 ? (int) bbl.calcBlocks(currentLevel) : 0;
            int nextLevelBlocks = (int) (bbl.calcBlocks(currentLevel + 1) - levelBlocks);
            if (nextLevelBlocks > 0) {
                int progress = Math.round((float) (minedBlocks - levelBlocks) / nextLevelBlocks * 100);
                list.add(Text.create("tooltip.blatblock.progress",String.valueOf(Math.min(progress, 100)))
                    .add("%").withColor(bbl.getTitleColor()).asComponent());
            }
        } else {
            list.add(Text.create("tooltip.blatblock.max_level")
                .withColor(bbl.getTitleColor()).asComponent());
        }
        
        return list;
    }
    
    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-2, 0, -2), worldPosition.offset(2, 5, 2));
    }
    
    public List<ResourceLocation> getSorted() {
        return minedBlocks.keySet().stream()
            .sorted(Comparator.comparingInt(id -> {
                BlatBlockLayer level = BBLayerManager.get(id);
                return level != null ? level.getSort() : Integer.MAX_VALUE;
            }))
            .collect(Collectors.toList());
    }
    
    public int getLevelBlocks() {
        return getLevelBlocks(currentLayer);
    }
    
    public int getLevelBlocks(ResourceLocation id) {
        BlatBlockLayer lvlData = BBLayerManager.get(id);
        if (lvlData == null) {
            BlatBlock.LOGGER.warn("No BlatBlockLayer for {} in getBlockLevel()", id);
            return 0;
        }
        return (int) lvlData.calcBlocks(getCurrentLevel());
    }
    
    public int getCurrentLevel() {
        return getCurrentLevel(currentLayer);
    }
    
    public int getCurrentLevel(ResourceLocation id) {
        BlatBlockLayer lvlData = BBLayerManager.get(id);
        if (lvlData == null) {
            BlatBlock.LOGGER.warn("No BlatBlockLayer for {} in getCurrentLevel()", id);
            return 0;
        }
        
        int minedCount = getMinedBlock(id);
        int level = 0;
        int maxLevel = 99;
        
        while (level < maxLevel) {
            int requiredBlocks = (int) lvlData.calcBlocks(level + 1);
            if (minedCount < requiredBlocks) break;
            level++;
        }
        
        return level;
    }
    
    @Override
    public void setChanged() {
        super.setChanged();
        List<BlockEntity> entities = new ArrayList<>();
        if(level==null) return;
        entities.add(level.getBlockEntity(getBlockPos().offset(1, 0, 0)));
        entities.add(level.getBlockEntity(getBlockPos().offset(-1, 0, 0)));
        entities.add(level.getBlockEntity(getBlockPos().offset(0, 0, 1)));
        entities.add(level.getBlockEntity(getBlockPos().offset(0, 0, -1)));
        for(BlockEntity entity : entities) if(entity instanceof AutoGeneratorBlockEntity) entity.setChanged();
    }
    
    public int getMinedBlock() {
        return getMinedBlock(currentLayer);
    }
    
    public int getAllMinedBlock() {
        AtomicInteger m = new AtomicInteger();
        minedBlocks.forEach((r, i) -> m.addAndGet(i));
        return m.get();
    }
    
    public void setMinedBlock(int count) {
        setMinedBlock(currentLayer, count);
    }
    
    public void addMinedBlock(int delta) {
        addMinedBlock(currentLayer, delta);
    }
    
    public int getMinedBlock(ResourceLocation id) {
        return minedBlocks.getOrDefault(id, 0);
    }
    
    public void setMinedBlock(ResourceLocation id, int count) {
        minedBlocks.put(id, Math.max(0, count));
        setChanged();
    }
    
    public void addMinedBlock(ResourceLocation id, int delta) {
        setMinedBlock(getMinedBlock(id) + delta);
    }
}