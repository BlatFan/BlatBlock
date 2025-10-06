package ru.blatfan.blatblock.compat.kubejs.items;

import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import ru.blatfan.blatblock.common.item.GeneratorUpgradeItem;

public class GeneratorUpgradeBuilder extends ItemBuilder {
    private GeneratorUpgradeItem.Type type;
    private float quality;
    
    public GeneratorUpgradeBuilder(ResourceLocation i) {
        super(i);
        this.type = GeneratorUpgradeItem.Type.SPEED;
        this.quality = 1;
        this.maxStackSize=1;
    }
    
    @Override
    public Item createObject() {
        return new GeneratorUpgradeItem(type, quality, createItemProperties());
    }
    
    @Info("Sets the item's quality.")
    public ItemBuilder quality(int v) {
        quality = v;
        return this;
    }
    @Info("Sets the item's type.")
    public ItemBuilder type(Object v) {
        type = toUpgradeType(v);
        return this;
    }
    public static GeneratorUpgradeItem.Type toUpgradeType(Object o) {
        if (o instanceof GeneratorUpgradeItem.Type type) return type;
        return GeneratorUpgradeItem.Type.valueOf(String.valueOf(o).toUpperCase());
    }
}