package ru.blatfan.blatblock.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import ru.blatfan.blatblock.BlatBlock;

import java.util.List;

import static ru.blatfan.blatblock.client.gui.BlatBlockScreen.getItemStack;

// JER
public abstract class BlankJEIRecipeCategory<T extends BBLRecipe> implements IRecipeCategory<T> {
    private final IDrawable icon;
    private final IDrawable bg;

    protected BlankJEIRecipeCategory(IGuiHelper guiHelper, ItemStack icon) {
        this.icon = guiHelper.createDrawableItemStack(icon);
        this.bg = guiHelper.createDrawable(BlatBlock.loc("textures/gui/jei.png"), 0, 0, 129, 132);
    }
    
    @Override
    public IDrawable getBackground() {
        return bg;
    }
    
    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }
    
    @Override
    public void draw(T recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        try {
            recipe.drawBg(guiGraphics);
            recipe.drawInfo(getBackground().getWidth(), getBackground().getHeight(), guiGraphics, mouseX, mouseY);
        } catch (Exception e) {
            BlatBlock.LOGGER.error("Error drawing JEI recipe: {}", e.getMessage());
        }
    }
    
    @Override
    public @NotNull List<Component> getTooltipStrings(T recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        try {
            return recipe.getTooltipStrings(mouseX, mouseY);
        } catch (Exception e) {
            BlatBlock.LOGGER.error("Error getting JEI tooltip: {}", e.getMessage());
            return List.of();
        }
    }
    
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, -1000, -1000).addItemStack(ItemStack.EMPTY);
        recipe.addResultItems(builder);
    }
}
