package ru.blatfan.blatblock.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import ru.blatfan.blatblock.common.BBRegistry;

public class PlayerSettings {
    public static boolean dropToInv(Player player){
        if(player==null) return false;
        return player.getInventory().contains(new ItemStack(BBRegistry.ITEMS.DROP_RING.get()));
    }
    public static boolean spawnEntity(Player player){
        if(player==null) return true;
        return !player.getInventory().contains(new ItemStack(BBRegistry.ITEMS.ENTITY_RING.get()));
    }
    public static boolean tagItem(Player player){
        if(player==null) return true;
        return !player.getInventory().contains(new ItemStack(BBRegistry.ITEMS.TAG_RING.get()));
    }
    public static boolean setLiquid(Player player){
        if(player==null) return true;
        return !player.getInventory().contains(new ItemStack(BBRegistry.ITEMS.LIQUID_RING.get()));
    }
}