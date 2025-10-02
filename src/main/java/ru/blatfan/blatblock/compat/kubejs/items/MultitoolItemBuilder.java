package ru.blatfan.blatblock.compat.kubejs.items;

import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import ru.blatfan.blatblock.common.item.MultitoolItem;

public class MultitoolItemBuilder extends ItemBuilder {
    private int attackDamage;
    private int attackSpeed;
    private Tier tier;
    
    public MultitoolItemBuilder(ResourceLocation i) {
        super(i);
        this.attackDamage = 2;
        this.attackSpeed = 1;
        this.tier = Tiers.WOOD;
    }
    
    @Override
    public Item createObject() {
        return new MultitoolItem(attackDamage, attackSpeed, tier, createItemProperties());
    }
    
    @Info("Sets the item's attack damage.")
    public ItemBuilder attackDamage(int v) {
        attackDamage = v;
        return this;
    }
    @Info("Sets the item's attack speed.")
    public ItemBuilder attackSpeed(int v) {
        attackSpeed = v;
        return this;
    }
    @Info("Sets the item's tier.")
    public ItemBuilder tier(Object v) {
        tier = toToolTier(v);
        return this;
    }
}