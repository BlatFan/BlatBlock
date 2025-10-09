package ru.blatfan.blatblock.common.item;

import lombok.Getter;
import net.minecraft.world.item.Item;

@Getter
public class GeneratorUpgradeItem extends Item {
    private final Type type;
    private final float quality;
    public GeneratorUpgradeItem(Type type, float quality) {
        this(type, quality, new Properties().stacksTo(1));
    }
    public GeneratorUpgradeItem(Type type, float quality, Properties properties) {
        super(properties);
        this.type = type;
        this.quality = quality;
    }
    
    public enum Type { SPEED, FORTUNE, TAG, ENTITY, STACK}
}