package ru.blatfan.blatblock.compat.kubejs.bbl;

import com.google.gson.JsonElement;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.blatfan.blatapi.utils.ColorHelper;
import ru.blatfan.blatblock.BlatBlock;
import ru.blatfan.blatblock.common.data.BlatBlockLayer;
import ru.blatfan.blatblock.common.data.BlatBlockManager;

import java.awt.*;
import java.util.*;
import java.util.List;

public class BBLBuilder {
    @Getter
    private final ResourceLocation id;
    private Component displayName;
    private Color titleColor;
    private int blockCost;
    private final List<BlatBlockLayer.Entry> blocks;
    private final List<BlatBlockLayer.Entry> entities;
    private String blockcalc;
    private ResourceLocation texture;
    private ResourceLocation bg;
    private int sort;
    
    public BBLBuilder(ResourceLocation id) {
        this.id = id;
        this.displayName = Component.literal("Unnamed Layer");
        this.titleColor = Color.WHITE;
        this.blockCost = 0;
        this.blocks = new ArrayList<>();
        this.entities = new ArrayList<>();
        this.blockcalc = "1";
        this.texture = BlatBlock.loc("textures/gui/base.png");
        this.bg = null;
        this.sort = 0;
    }
    
    public BlatBlockLayer create() {
        return new BlatBlockLayer(
            this.displayName,
            this.titleColor,
            this.blockCost,
            this.blocks,
            this.entities,
            this.blockcalc,
            this.texture,
            this.bg,
            this.sort
        );
    }
    
    public void register(){
        BlatBlockManager.add(this.id, create());
    }
    
    public BBLBuilder title(String title) {
        this.displayName = Component.translatable(title);
        return this;
    }
    
    public BBLBuilder titleColor(String hexColor) {
        this.titleColor = ColorHelper.getColor(hexColor);
        return this;
    }
    
    public BBLBuilder blockCost(int cost) {
        this.blockCost = cost;
        return this;
    }
    
    public BBLBuilder blockcalc(String formula) {
        this.blockcalc = formula;
        return this;
    }
    
    public BBLBuilder texture(String texture) {
        this.texture = new ResourceLocation(texture);
        return this;
    }
    
    public BBLBuilder background(String bg) {
        this.bg = new ResourceLocation(bg);
        return this;
    }
    
    public BBLBuilder sort(int sort) {
        this.sort = sort;
        return this;
    }
    
    public BBLBuilder block(String blockId, String chance) {
        return block(blockId, chance, 0);
    }
    
    public BBLBuilder block(String blockId, String chance, int level) {
        this.blocks.add(new BlatBlockLayer.Entry(
            new ResourceLocation(blockId),
            chance,
            level
        ));
        return this;
    }
    
    public BBLBuilder blocks(Map<String, Object> blockMap) {
        blockMap.forEach((blockId, data) -> {
            if (data instanceof String chance) {
                block(blockId, chance, 0);
            } else if (data instanceof Map<?, ?> blockData) {
                Map<String, Object> bd = (Map<String, Object>) blockData;
                String chance = bd.getOrDefault("chance", "1.0").toString();
                int level = Integer.parseInt(bd.getOrDefault("level", 0).toString());
                block(blockId, chance, level);
            }
        });
        return this;
    }
    
    public BBLBuilder entity(String entityId, String chance) {
        return entity(entityId, chance, 0);
    }
    
    public BBLBuilder entity(String entityId, String chance, int level) {
        this.entities.add(new BlatBlockLayer.Entry(
            new ResourceLocation(entityId),
            chance,
            level
        ));
        return this;
    }
    
    public BBLBuilder entities(Map<String, Object> entityMap) {
        entityMap.forEach((entityId, data) -> {
            if (data instanceof String chance) {
                entity(entityId, chance, 0);
            } else if (data instanceof Map<?, ?> entityData) {
                Map<String, Object> ed = (Map<String, Object>) entityData;
                String chance = ed.getOrDefault("chance", "0.1").toString();
                int level = Integer.parseInt(ed.getOrDefault("level", 0).toString());
                entity(entityId, chance, level);
            }
        });
        return this;
    }
}
