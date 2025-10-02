package ru.blatfan.blatblock.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import ru.blatfan.blatblock.BlatBlock;
import ru.blatfan.blatblock.common.data.BlatBlockLayer;
import ru.blatfan.blatblock.common.data.BlatBlockManager;
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
        Map<ResourceLocation, BlatBlockLayer> data = BlatBlockManager.getData();
        List<BBLBlocksWrapper> recipesB = new ArrayList<>();
        List<BBLEntitiesWrapper> recipesE = new ArrayList<>();
        
        for (Map.Entry<ResourceLocation, BlatBlockLayer> entry : data.entrySet()) {
            ResourceLocation id = entry.getKey();
            BlatBlockLayer bbl = entry.getValue();
            
            Set<Integer> uniqueLevels = new HashSet<>();
            bbl.getBlocks().forEach(blockEntry -> uniqueLevels.add(blockEntry.level()));
            
            for (Integer level : uniqueLevels)
                if (!bbl.getBlocks(level).isEmpty())
                    recipesB.add(new BBLBlocksWrapper(id, level));
        }
        
        for (Map.Entry<ResourceLocation, BlatBlockLayer> entry : data.entrySet()) {
            ResourceLocation id = entry.getKey();
            BlatBlockLayer bbl = entry.getValue();
            
            Set<Integer> uniqueLevels = new HashSet<>();
            bbl.getEntities().forEach(entityEntry -> uniqueLevels.add(entityEntry.level()));
            
            for (Integer level : uniqueLevels)
                if (!bbl.getEntities(level).isEmpty())
                    recipesE.add(new BBLEntitiesWrapper(id, level));
        }
        
        registration.addRecipes(BBLBlocksCategory.TYPE, recipesB);
        registration.addRecipes(BBLEntitiesCategory.TYPE, recipesE);
    }
}