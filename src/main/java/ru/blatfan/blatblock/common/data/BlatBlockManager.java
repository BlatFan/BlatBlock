package ru.blatfan.blatblock.common.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import ru.blatfan.blatblock.BlatBlock;

import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlatBlockManager extends SimpleJsonResourceReloadListener {
    public static final BlatBlockLevel NULL_BBL = new BlatBlockLevel(Component.literal("NULL"), Color.RED, 0, new ArrayList<>(), new ArrayList<>(), "0", BlatBlock.loc("textures/gui/base.png"), null, 0);
    private static final Map<ResourceLocation, BlatBlockLevel> data = new ConcurrentHashMap<>();
    private static ResourceLocation baseId;
    
    private static final Object reloadLock = new Object();
    
    private static final Gson GSON = new Gson();
    
    public BlatBlockManager() {
        super(GSON, "blatblock");
    }
    
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        synchronized (reloadLock) {
            Map<ResourceLocation, BlatBlockLevel> oldData = new HashMap<>(data);
            ResourceLocation oldBaseId = baseId;
            
            data.clear();
            ResourceLocation newBaseId = null;
            int loadedCount = 0;
            int errorCount = 0;
            
            for (Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
                try {
                    if (entry.getValue() == null || !entry.getValue().isJsonObject())
                        throw new IllegalArgumentException("Invalid JSON structure");
                    
                    BlatBlockLevel bbl = BlatBlockLevel.fromJson(entry.getValue());
                    
                    if (bbl == null)
                        throw new IllegalStateException("BlatBlockLevel.fromJson returned null");
                    
                    
                    data.put(entry.getKey(), bbl);
                    loadedCount++;
                    
                    if (bbl.getBlockCost() == -1 && newBaseId == null)
                        newBaseId = entry.getKey();
                    
                    BlatBlock.LOGGER.debug("Successfully loaded BlatBlockLevel: {}", entry.getKey());
                    
                } catch (Exception e) {
                    errorCount++;
                    BlatBlock.LOGGER.error("Failed to load BlatBlockLevel {}: {} - {}",
                        entry.getKey(), e.getClass().getSimpleName(), e.getMessage());
                    
                    if (BlatBlock.DEBUG_MODE)
                        e.printStackTrace();
                }
            }
            
            if (loadedCount == 0) {
                BlatBlock.LOGGER.error("Failed to load any BlatBlockLevels! Restoring previous data...");
                data.putAll(oldData);
                baseId = oldBaseId;
            } else {
                if (newBaseId != null) {
                    baseId = newBaseId;
                    BlatBlock.LOGGER.info("Set base BlatBlockLevel: {}", baseId);
                } else if (!data.isEmpty()) {
                    baseId = data.keySet().iterator().next();
                    BlatBlock.LOGGER.warn("No explicit base level found, using first available: {}", baseId);
                } else {
                    baseId = BlatBlock.loc("null");
                    BlatBlock.LOGGER.error("No valid base level found!");
                }
            }
            
            if (errorCount > 0)
                BlatBlock.LOGGER.warn("Loaded {} BlatBlockLevels with {} errors", loadedCount, errorCount);
            else
                BlatBlock.LOGGER.info("Successfully loaded {} BlatBlockLevels", loadedCount);
        }
    }
    
    public static Map<ResourceLocation, BlatBlockLevel> getData() {
        return Collections.unmodifiableMap(BlatBlockManager.data);
    }
    
    public static BlatBlockLevel get(ResourceLocation id){
        return data.getOrDefault(id, NULL_BBL);
    }
    
    public static boolean isInitialized() {
        return !data.isEmpty() && baseId != null;
    }
    
    public static ResourceLocation getBaseId() {
        ResourceLocation current = baseId;
        return current != null ? current : BlatBlock.loc("null");
    }
    
    public static Set<ResourceLocation> getAvailableIds() {
        return Collections.unmodifiableSet(data.keySet());
    }
    
    public static boolean hasLevel(ResourceLocation id) {
        return data.containsKey(id);
    }
    
    public static int getLevelCount() {
        return data.size();
    }
}