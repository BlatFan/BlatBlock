package ru.blatfan.blatblock.compat.jei;

import lombok.Getter;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import ru.blatfan.blatblock.common.data.BlatBlockLayer;
import ru.blatfan.blatblock.common.data.BBLayerManager;

@Getter
public abstract class BBLRecipe implements IRecipeCategoryExtension, IRecipeSlotTooltipCallback {
    private final ResourceLocation bbl;
    private final int level;
    
    protected BBLRecipe(ResourceLocation bbl, int level) {
        this.bbl = bbl;
        this.level = level;
    }
    
    public BlatBlockLayer get(){
        return BBLayerManager.get(bbl);
    }
    
    public void drawBg(GuiGraphics gui){
        if(get().getBg()!=null) {
            gui.blit(get().getBg(), 0, 0, 129, 66, 0, 0, 129, 66, 129, 66);
            gui.blit(get().getBg(), 0, 66, 129, 66, 0, 0, 129, 66, 129, 66);
        }
    }
    
    public abstract void addResultItems(IRecipeLayoutBuilder builder);
}