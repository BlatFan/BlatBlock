package ru.blatfan.blatblock.compat.kubejs.bbl;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.resources.ResourceLocation;
import oshi.util.tuples.Pair;
import ru.blatfan.blatblock.common.data.BBLayerManager;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class BBLEventJS extends EventJS {
    public BBLBuilder add(ResourceLocation id) {
        return new BBLBuilder(id);
    }
    public void addTo(ResourceLocation layerId, String blockIs, String blockChance) {
        BBLayerManager.modifBBL.add(new Pair<>(layerId, new Pair<>(blockIs, blockChance)));
    }
    public void remove(ResourceLocation id) {
        BBLayerManager.removalBBL.add(id);
    }
    public void setBaseId(ResourceLocation id){
        BBLayerManager.setBaseId(id);
    }
}