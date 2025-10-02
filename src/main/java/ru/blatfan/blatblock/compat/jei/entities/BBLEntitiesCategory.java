package ru.blatfan.blatblock.compat.jei.entities;

import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import ru.blatfan.blatblock.BlatBlock;
import ru.blatfan.blatblock.compat.jei.BlankJEIRecipeCategory;

public class BBLEntitiesCategory extends BlankJEIRecipeCategory<BBLEntitiesWrapper> {
    public static final ResourceLocation ID = BlatBlock.loc("entities");
    public static final RecipeType<BBLEntitiesWrapper> TYPE = new RecipeType<>(ID, BBLEntitiesWrapper.class);
    
    public BBLEntitiesCategory(IGuiHelper guiHelper) {
        super(guiHelper, new ItemStack(Items.STONE));
    }
    
    @Override
    public RecipeType<BBLEntitiesWrapper> getRecipeType() {
        return TYPE;
    }
    
    @Override
    public Component getTitle() {
        return Component.translatable("jei.blatblock.entities");
    }
}