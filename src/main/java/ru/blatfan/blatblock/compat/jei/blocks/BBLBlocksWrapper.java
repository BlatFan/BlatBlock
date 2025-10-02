package ru.blatfan.blatblock.compat.jei.blocks;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import ru.blatfan.blatapi.client.guide_book.GuideClient;
import ru.blatfan.blatapi.client.render.FluidRenderMap;
import ru.blatfan.blatapi.utils.Text;
import ru.blatfan.blatblock.compat.jei.BBLRecipe;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static ru.blatfan.blatblock.client.gui.BlatBlockScreen.getItemStack;
import static ru.blatfan.blatblock.client.gui.BlatBlockScreen.isMouseOver;

public class BBLBlocksWrapper extends BBLRecipe {
    public BBLBlocksWrapper(ResourceLocation bbl, int level) {
        super(bbl, level);
    }
    
    @Override
    public void onTooltip(IRecipeSlotView recipeSlotView, List<Component> tooltip) {}
    
    @Override
    public List<Component> getTooltipStrings(double mX, double mY) {
        List<Block> levelBlocks = get().getBlocks(getLevel());
        for (int i = 0; i < levelBlocks.size(); i++) {
            int x = i % 7;
            int y = i / 7;
            
            int blockX = 2 + x * 18;
            int blockY = 11 + y * 18;
            
            if (isMouseOver(mX, mY, blockX, blockY, 16, 16)) {
                Block block = levelBlocks.get(i);
                ItemStack item = getItemStack(block);
                float chance = get().get(block).chance(getLevel());
                
                if (item!=null) {
                    List<Component> tooltips = item.getTooltipLines(GuideClient.player, GuideClient.tooltipFlag);
                    if (item.getItem() instanceof BucketItem bucketItem) {
                        tooltips.set(0, bucketItem.getFluid().getFluidType().getDescription());
                    }
                    tooltips.add(Text.create("tooltip.blatblock.chance").add(String.format("%.1f%%", chance * 100)));
                    return tooltips;
                } else if (block instanceof LiquidBlock liquidBlock) {
                    List<Component> tooltips = new ArrayList<>();
                    tooltips.add(liquidBlock.getFluid().getFluidType().getDescription());
                    tooltips.add(Text.create(BuiltInRegistries.FLUID.getKey(liquidBlock.getFluid()).toString()).withColor(Color.DARK_GRAY));
                    tooltips.add(Text.create("tooltip.blatblock.chance").add(String.format("%.1f%%", chance * 100)));
                    return tooltips;
                }
            }
        }
        return new ArrayList<>();
    }
    
    @Override
    public void drawInfo(int recipeWidth, int recipeHeight, GuiGraphics gui, double mouseX, double mouseY) {
        ResourceLocation texture = get().getTexture();
        List<Block> levelBlocks = get().getBlocks(getLevel());
        
        boolean isMaxLevel = getLevel() >= 99;
        String levelText = isMaxLevel ? Component.translatable("tooltip.blatblock.max").getString() : String.valueOf(getLevel()+1);
        gui.drawString(GuideClient.font,
            Text.create("tooltip.blatblock.level", get().getTitle().getString(), levelText)
                .withColor(get().getTitleColor())
                .asComponent(), 0, 0, Color.WHITE.getRGB());
        
        for (int i = 0; i < levelBlocks.size(); i++) {
            int x = i % 7;
            int y = i / 7;
            gui.blit(texture, 1 + x * 18, 10 + y * 18, 0, 30, 18, 18, 64, 64);
        }
        
        for (int i = 0; i < levelBlocks.size(); i++) {
            int x = i % 7;
            int y = i / 7;
            Block block = levelBlocks.get(i);
            ItemStack stack = getItemStack(block);
            
            if (stack!=null) {
                gui.renderItem(stack, 2 + x * 18, 11 + y * 18);
                gui.renderItemDecorations(GuideClient.font, stack, 2 + x * 18, 2 + y * 18);
            } else if (block instanceof LiquidBlock liquidBlock) {
                try {
                    TextureAtlasSprite sprite = FluidRenderMap.getCachedFluidTexture(
                        new FluidStack(liquidBlock.getFluid().getSource(), 1000),
                        FluidRenderMap.FluidFlow.STILL
                    );
                    
                    if (liquidBlock.getFluid().getSource() == Fluids.WATER) {
                        RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
                    }
                    
                    gui.blit(2 + x * 18, 11 + y * 18, 0, 16, 16, sprite);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                } catch (Exception e) {
                    gui.fill(2 + x * 18, 11 + y * 18, 2 + x * 18 + 16, 2 + y * 18 + 16, 0xFFFFFFFF);
                }
            }
        }
    }
}