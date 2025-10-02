package ru.blatfan.blatblock.compat.kubejs.bbl;

import net.minecraft.resources.ResourceLocation;
import ru.blatfan.blatblock.common.data.BlatBlockManager;

public class BBLRegistryJS {
    private BBLRegistryJS(){}
    public static final BBLRegistryJS INSTANCE = new BBLRegistryJS();
    
    public BBLBuilder add(ResourceLocation id) {
        return new BBLBuilder(id);
    }
    public void remove(ResourceLocation id) {
        BlatBlockManager.remove(id);
    }
    public void removeAll() {
        BlatBlockManager.removeAll();
    }
}