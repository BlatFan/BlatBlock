package ru.blatfan.blatblock.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import ru.blatfan.blatblock.BlatBlock;

import java.util.HashMap;
import java.util.Map;

public class BlatBlockManager extends SimpleJsonResourceReloadListener {
    private static final Map<ResourceLocation, BlatBlockLevel> data = new HashMap<>();
    
    private static final Gson GSON = new Gson();
    public BlatBlockManager() {
        super(GSON, "blatblock");
    }
    
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        data.clear();
        for(Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
            data.put(entry.getKey(), BlatBlockLevel.fromJson(entry.getValue()));
            BlatBlock.LOGGER.info("{} loaded", entry.getKey());
        }
    }
}
