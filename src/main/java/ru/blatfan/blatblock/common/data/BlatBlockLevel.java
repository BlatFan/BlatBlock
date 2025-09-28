package ru.blatfan.blatblock.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class BlatBlockLevel {
    @Getter
    private final int blockCost;
    private final List<ResourceLocation> blocks;
    private final List<ResourceLocation> entities;
    
    public static BlatBlockLevel fromJson(JsonElement element){
        JsonObject json = element.getAsJsonObject();
        int blockCost = json.has("block_cost") ? json.get("block_cost").getAsInt() : 0;
        List<ResourceLocation> blocks = new ArrayList<>();
        List<ResourceLocation> entities = new ArrayList<>();
        for(JsonElement blockEl : json.get("blocks").getAsJsonArray())
            blocks.add(new ResourceLocation(blockEl.getAsString()));
        if(json.has("entities"))
            for(JsonElement entityEl : json.get("entities").getAsJsonArray())
                entities.add(new ResourceLocation(entityEl.getAsString()));
        return new BlatBlockLevel(blockCost, blocks, entities);
    }
    
    public List<Block> getBlocks(){
        List<Block> blocks = new ArrayList<>();
        for(ResourceLocation id : this.blocks)
            if(BuiltInRegistries.BLOCK.containsKey(id))
                blocks.add(BuiltInRegistries.BLOCK.get(id));
        return blocks;
    }
    
    public List<EntityType<?>> getEntities(){
        List<EntityType<?>> entityTypes = new ArrayList<>();
        for(ResourceLocation id : this.entities)
            if(BuiltInRegistries.ENTITY_TYPE.containsKey(id))
                entityTypes.add(BuiltInRegistries.ENTITY_TYPE.get(id));
        return entityTypes;
    }
}