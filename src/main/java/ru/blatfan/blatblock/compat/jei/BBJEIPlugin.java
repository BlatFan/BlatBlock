package ru.blatfan.blatblock.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import ru.blatfan.blatblock.BlatBlock;
import ru.blatfan.blatblock.common.data.BlatBlockLayer;
import ru.blatfan.blatblock.common.data.BBLayerManager;
import ru.blatfan.blatblock.compat.jei.blocks.BBLBlocksCategory;
import ru.blatfan.blatblock.compat.jei.blocks.BBLBlocksWrapper;
import ru.blatfan.blatblock.compat.jei.entities.BBLEntitiesCategory;
import ru.blatfan.blatblock.compat.jei.entities.BBLEntitiesWrapper;

import java.util.*;

@JeiPlugin
public class BBJEIPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return BlatBlock.loc("jei");
    }
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new BBLEntitiesCategory(guiHelper));
        registration.addRecipeCategories(new BBLBlocksCategory(guiHelper));
    }
    
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<BBLBlocksWrapper> recipesB = new ArrayList<>();
        List<BBLEntitiesWrapper> recipesE = new ArrayList<>();

        List<ResourceLocation> sortedIds = new ArrayList<>(BBLayerManager.getData().keySet());
        sortedIds.sort(Comparator.comparing(ResourceLocation::toString));
        
        for (ResourceLocation id : sortedIds) {
            BlatBlockLayer layer = BBLayerManager.get(id);
            
            Set<Integer> blockLevels = new TreeSet<>();
            layer.getBlocks().forEach(entry -> blockLevels.add(entry.level()));
            for (Integer lvl : blockLevels) if (!layer.getBlocks(lvl).isEmpty())
                recipesB.add(new BBLBlocksWrapper(id, lvl));
            
            Set<Integer> entityLevels = new TreeSet<>();
            layer.getEntities().forEach(entry -> entityLevels.add(entry.level()));
            for (Integer lvl : entityLevels) if (!layer.getEntities(lvl).isEmpty())
                recipesE.add(new BBLEntitiesWrapper(id, lvl));
        }

        registration.addRecipes(BBLBlocksCategory.TYPE, recipesB);
        registration.addRecipes(BBLEntitiesCategory.TYPE, recipesE);
        
        
        registration.addRecipes(BBLBlocksCategory.TYPE, recipesB);
        registration.addRecipes(BBLEntitiesCategory.TYPE, recipesE);
    }
}