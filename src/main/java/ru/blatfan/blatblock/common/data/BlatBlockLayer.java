package ru.blatfan.blatblock.common.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import ru.blatfan.blatapi.utils.ColorHelper;
import ru.blatfan.blatapi.utils.Text;
import ru.blatfan.blatblock.BlatBlock;
import ru.blatfan.blatblock.util.MathEvaluator;
import ru.blatfan.blatblock.util.PlayerSettings;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
@Getter
public class BlatBlockLayer {
    private final Component title;
    private final Color titleColor;
    private final int blockCost;
    private final List<Entry> blocks;
    private final List<Entry> entities;
    private final String blockcalc;
    private final ResourceLocation texture;
    private final ResourceLocation bg;
    private final int sort;
    
    public static BlatBlockLayer fromJson(JsonElement element) {
        if (element == null || !element.isJsonObject())
            throw new IllegalArgumentException("Invalid JSON element for BlatBlockLayer");
        JsonObject json = element.getAsJsonObject();
        List<Entry> blocks = new ArrayList<>();
        List<Entry> entities = new ArrayList<>();
        
        if (json.has("blocks") && json.get("blocks").isJsonArray()) {
            for (JsonElement blockEl : json.get("blocks").getAsJsonArray()) {
                try {
                    Entry entry = parseBlockEntry(blockEl);
                    if (entry != null)
                        blocks.add(entry);
                } catch (Exception e) {
                    BlatBlock.LOGGER.warn("Failed to parse block entry: {}", e.getMessage());
                }
            }
        }
        
        if (json.has("entities") && json.get("entities").isJsonArray()) {
            for (JsonElement entityEl : json.get("entities").getAsJsonArray()) {
                try {
                    Entry entry = parseEntityEntry(entityEl);
                    if (entry != null) entities.add(entry);
                } catch (Exception e) {
                    BlatBlock.LOGGER.warn("Failed to parse entity entry: {}", e.getMessage());
                }
            }
        }
        
        int blockCost = json.has("block_cost") ? json.get("block_cost").getAsInt() : 0;
        String levelcalc = json.has("block_calc") ? json.get("block_calc").getAsString() : "level * 300";
        ResourceLocation texture = json.has("texture") ? ResourceLocation.tryParse(json.get("texture").getAsString()) :
            BlatBlock.loc("textures/gui/base.png");
        ResourceLocation bg = json.has("background") ? ResourceLocation.tryParse(json.get("background").getAsString()) : null;
        Component title = Text.create(json.get("title").getAsString()).asComponent();
        Color color = json.has("title_color") ? ColorHelper.getColor(json.get("title_color").getAsString()) : Color.WHITE;
        int sort = json.has("sort") ? json.get("sort").getAsInt() : 1;
        return new BlatBlockLayer(title, color, blockCost, blocks, entities, levelcalc, texture, bg, sort);
    }
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        JsonArray blocks = new JsonArray();
        JsonArray entities = new JsonArray();
        
        for (Entry block : this.blocks)
            try {
                blocks.add(block.toBlockJson());
            } catch (Exception e) {
                BlatBlock.LOGGER.warn("Failed to serialize block entry: {}", e.getMessage());
            }
        
        for (Entry entity : this.entities)
            try {
                entities.add(entity.toEntityJson());
            } catch (Exception e) {
                BlatBlock.LOGGER.warn("Failed to serialize entity entry: {}", e.getMessage());
            }
        
        json.addProperty("block_cost", blockCost);
        json.addProperty("block_calc", blockcalc);
        json.addProperty("texture", texture.toString());
        if(bg!=null)json.addProperty("background", bg.toString());
        json.addProperty("title", title.toString());
        json.addProperty("title_color", String.format("#%08X", titleColor.getRGB()));
        json.addProperty("sort", sort);
        return json;
    }
    
    public float calcBlocks(int level) {
        try {
            String expr = getBlockcalc();
            return (int) new MathEvaluator(expr)
                .setVariable("level", level + 1)
                .evaluate();
        } catch (Exception e) {
            BlatBlock.LOGGER.error("Error evaluating level formula '{}' : {}", getBlockcalc(), e.getMessage());
            return 0;
        }
    }
    
    public Entry get(Block block) {
        for (Entry entry : blocks)
            if (entry.id().equals(BuiltInRegistries.BLOCK.getKey(block)))
                return entry;
        return null;
    }
    
    public Entry get(EntityType<?> entityType) {
        for (Entry entry : entities)
            if (entry.id().equals(BuiltInRegistries.ENTITY_TYPE.getKey(entityType)))
                return entry;
        return null;
    }
    
    private static Entry parseBlockEntry(JsonElement element) {
        if (!element.isJsonObject()) return null;
        
        JsonObject obj = element.getAsJsonObject();
        if (!obj.has("block") || !obj.has("chance"))
            return null;
        
        String blockId = obj.get("block").getAsString();
        String chance = obj.get("chance").getAsString();
        int level = obj.has("level") ? obj.get("level").getAsInt() : 0;
        
        if (blockId.isEmpty() || chance.isEmpty() || level < 0)
            return null;
        
        return new Entry(new ResourceLocation(blockId), chance, level);
    }
    
    private static Entry parseEntityEntry(JsonElement element) {
        if (!element.isJsonObject()) return null;
        
        JsonObject obj = element.getAsJsonObject();
        if (!obj.has("entity") || !obj.has("chance"))
            return null;
        
        String entityId = obj.get("entity").getAsString();
        String chance = obj.get("chance").getAsString();
        int level = obj.has("level") ? obj.get("level").getAsInt() : 0;
        
        if (entityId.isEmpty() || chance.isEmpty() || level < 0)
            return null;
        
        return new Entry(new ResourceLocation(entityId), chance, level);
    }
    
    public void rand(Player player, Level level, BlockPos pos, Random random, int blockLevel) {
        try {
            BlockState blockState = getRandBlock(random, blockLevel);
            if (blockState != null && blockState.getBlock() instanceof LiquidBlock && !PlayerSettings.setLiquid(player))
                blockState = Blocks.AIR.defaultBlockState();
            if (blockState != null) level.setBlock(pos, blockState, 3);
            
        } catch (Exception e) {
            BlatBlock.LOGGER.error("Failed to set block at {}: {}", pos, e.getMessage());
        }
        
        EntityType<?> entityType = getRandEntity(random, blockLevel);
        if (entityType != null && PlayerSettings.spawnEntity(player))
            try {
                Entity entity = entityType.create(level);
                if (entity != null) {
                    Vec3 centerPos = pos.above().getCenter();
                    entity.setPos(centerPos.x, centerPos.y, centerPos.z);
                    level.addFreshEntity(entity);
                }
            } catch (Exception e) {
                BlatBlock.LOGGER.error("Failed to spawn entity {} at {}: {}",
                    entityType.getDescriptionId(), pos, e.getMessage());
            }
    }
    
    public BlockState getRandBlock(Random random, int blockLevel) {
        List<Entry> availableEntries = blocks.stream()
            .filter(entry -> blockLevel >= entry.level)
            .filter(entry -> BuiltInRegistries.BLOCK.containsKey(entry.id))
            .toList();
        
        if (availableEntries.isEmpty())
            return Blocks.AIR.defaultBlockState();
        
        float totalWeight = (float) availableEntries.stream()
            .mapToDouble(e -> e.chance(blockLevel))
            .sum();
        
        if (totalWeight <= 0)
            return Blocks.AIR.defaultBlockState();
        
        float randomValue = random.nextFloat() * totalWeight;
        float currentWeight = 0;
        
        for (Entry entry : availableEntries) {
            currentWeight += entry.chance(blockLevel);
            if (randomValue <= currentWeight) {
                Block block = BuiltInRegistries.BLOCK.get(entry.id);
                return block.defaultBlockState();
            }
        }
        
        Entry lastEntry = availableEntries.get(availableEntries.size() - 1);
        return BuiltInRegistries.BLOCK.get(lastEntry.id).defaultBlockState();
    }
    
    public EntityType<?> getRandEntity(Random random, int blockLevel) {
        List<Entry> availableEntries = entities.stream()
            .filter(entry -> blockLevel >= entry.level)
            .filter(entry -> BuiltInRegistries.ENTITY_TYPE.containsKey(entry.id))
            .toList();
        
        if (availableEntries.isEmpty())
            return null;
        
        float totalWeight = (float) availableEntries.stream()
            .mapToDouble(e -> e.chance(blockLevel))
            .sum();
        
        float emptyWeight = totalWeight * 0.9f;
        float totalWeightWithEmpty = totalWeight + emptyWeight;
        
        if (totalWeightWithEmpty <= 0)
            return null;
        
        float randomValue = random.nextFloat() * totalWeightWithEmpty;
        
        if (randomValue <= emptyWeight) return null;
        
        
        randomValue -= emptyWeight;
        float currentWeight = 0;
        
        for (Entry entry : availableEntries) {
            currentWeight += entry.chance(blockLevel);
            if (randomValue <= currentWeight)
                return BuiltInRegistries.ENTITY_TYPE.get(entry.id);
        }
        
        Entry lastEntry = availableEntries.get(availableEntries.size() - 1);
        return BuiltInRegistries.ENTITY_TYPE.get(lastEntry.id);
    }
    
    public List<Block> getBlocks(int blockLevel) {
        List<Block> blocks = new ArrayList<>();
        for (Entry entry : this.blocks)
            if (BuiltInRegistries.BLOCK.containsKey(entry.id) && entry.level <= blockLevel && entry.chance(blockLevel) > 0)
                blocks.add(BuiltInRegistries.BLOCK.get(entry.id));
        return blocks;
    }
    
    public List<EntityType<?>> getEntities(int blockLevel) {
        List<EntityType<?>> entityTypes = new ArrayList<>();
        for (Entry entry : this.entities)
            if (BuiltInRegistries.ENTITY_TYPE.containsKey(entry.id) && entry.level <= blockLevel && entry.chance(blockLevel) > 0)
                entityTypes.add(BuiltInRegistries.ENTITY_TYPE.get(entry.id));
        return entityTypes;
    }
    
    public record Entry(ResourceLocation id, String chanceFormul, int level) {
        public float chance(int level) {
            MathEvaluator evaluator = new MathEvaluator(chanceFormul)
                .setVariable("level", level);
            return (float) Math.max(0, evaluator.evaluate());
        }
        
        public JsonObject toBlockJson() {
            JsonObject json = new JsonObject();
            json.addProperty("block", id.toString());
            json.addProperty("chance", chanceFormul);
            json.addProperty("level", level);
            return json;
        }
        public JsonObject toEntityJson() {
            JsonObject json = new JsonObject();
            json.addProperty("entity", id.toString());
            json.addProperty("chance", chanceFormul);
            json.addProperty("level", level);
            return json;
        }
    }
}