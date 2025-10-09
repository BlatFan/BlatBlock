package ru.blatfan.blatblock.common.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.fml.ModList;
import oshi.util.tuples.Pair;
import ru.blatfan.blatblock.BlatBlock;
import ru.blatfan.blatblock.compat.kubejs.BBKubeJSPlugin;
import ru.blatfan.blatblock.compat.kubejs.bbl.BBLBuilder;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class BBLayerManager extends SimpleJsonResourceReloadListener {
    public static final BlatBlockLayer NULL_BBL =
        new BlatBlockLayer(Component.literal("NULL"), Color.RED, Integer.MAX_VALUE,
            new ArrayList<>(), new ArrayList<>(), "0", BlatBlock.loc("textures/gui/base.png"), null, 0);
    
    private static final Map<ResourceLocation, BlatBlockLayer> LAYERS = new ConcurrentHashMap<>();
    public static final List<Supplier<BBLBuilder>> jsLayers = new ArrayList<>();
    public static final List<ResourceLocation> removalBBL = new ArrayList<>();
    public static final List<Pair<ResourceLocation, Pair<String, String>>> modifBBL = new ArrayList<>();
    
    @Setter
    private static volatile ResourceLocation baseId;
    
    private static final Gson GSON = new Gson();
    
    public BBLayerManager() {
        super(GSON, "blatblock");
    }
    
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonLayers, ResourceManager resourceManager, ProfilerFiller profiler) {
        LAYERS.clear();
        ResourceLocation newBase = null;
        if(ModList.get().isLoaded("kubejs")) BBKubeJSPlugin.postBBL();
        
        int loaded = 0, errors = 0;
        for (var kv : jsonLayers.entrySet()) {
            ResourceLocation id = kv.getKey();
            if(removalBBL.contains(id)) continue;
            try {
                BlatBlockLayer layer = BlatBlockLayer.fromJson(kv.getValue().getAsJsonObject());
                boolean c = false;
                if(ModList.get().isLoaded("kubejs"))
                    for(Pair<ResourceLocation, Pair<String, String>> modif : modifBBL) if(modif.getA().equals(id)){
                        BBLBuilder builder = new BBLBuilder(layer, id);
                        builder.block(modif.getB().getA(), modif.getB().getB());
                        jsLayers.add(()-> builder);
                        c=true;
                    }
                if(c) continue;
                LAYERS.put(id, layer);
                loaded++;
                if (layer.getBlockCost() == -1 && newBase == null) newBase = id;
            } catch (Exception e) {
                errors++;
                BlatBlock.LOGGER.error("Failed loading layer {}: {}", id, e.getMessage());
            }
        }
        if(ModList.get().isLoaded("kubejs"))
            for (Supplier<BBLBuilder> bblbS : jsLayers) {
                BBLBuilder bblb = bblbS.get();
                ResourceLocation id = bblb.getId();
                if(removalBBL.contains(id)) continue;
                try {
                    if(ModList.get().isLoaded("kubejs"))
                        for(Pair<ResourceLocation, Pair<String, String>> modif : modifBBL)
                            if(modif.getA().equals(id))
                                bblb.block(modif.getB().getA(), modif.getB().getB());
                    LAYERS.put(id, bblb.create());
                    loaded++;
                    if (bblb.create().getBlockCost() == -1 && newBase == null) newBase = id;
                } catch (Exception e) {
                    errors++;
                    BlatBlock.LOGGER.error("Failed loading js layer {}: {}", id, e.getMessage());
                }
            }
        
        if (loaded == 0) {
            BlatBlock.LOGGER.error("No layers loaded from JSON, skipping baseId update");
        } else if (newBase != null) {
            baseId = newBase;
            BlatBlock.LOGGER.info("Base layer set: {}", newBase);
        } else {
            baseId = LAYERS.keySet().iterator().next();
            BlatBlock.LOGGER.warn("No explicit base, using first: {}", baseId);
        }
        
        BlatBlock.LOGGER.info("JSON reload complete: {} loaded, {} errors, {} total", loaded, errors, LAYERS.size());
    }
    
    public static Map<ResourceLocation, BlatBlockLayer> getData() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(LAYERS));
    }
    
    public static BlatBlockLayer get(ResourceLocation id) {
        return LAYERS.getOrDefault(id, NULL_BBL);
    }
    
    public static ResourceLocation getBaseId() {
        var id = baseId;
        return id != null ? id : BlatBlock.loc("null");
    }
    
    public static Set<ResourceLocation> getAvailableIds() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(LAYERS.keySet()));
    }
}