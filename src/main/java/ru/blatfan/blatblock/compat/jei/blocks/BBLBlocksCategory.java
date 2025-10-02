package ru.blatfan.blatblock.compat.jei.blocks;

import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import ru.blatfan.blatblock.BlatBlock;
import ru.blatfan.blatblock.compat.jei.BlankJEIRecipeCategory;

public class BBLBlocksCategory extends BlankJEIRecipeCategory<BBLBlocksWrapper> {
    public static final ResourceLocation ID = BlatBlock.loc("blocks");
    public static final RecipeType<BBLBlocksWrapper> TYPE = new RecipeType<>(ID, BBLBlocksWrapper.class);
    
    public BBLBlocksCategory(IGuiHelper guiHelper) {
        super(guiHelper, new ItemStack(Items.STONE));
    }
    
    @Override
    public RecipeType<BBLBlocksWrapper> getRecipeType() {
        return TYPE;
    }
    
    @Override
    public Component getTitle() {
        return Component.translatable("jei.blatblock.blocks");
    }
}