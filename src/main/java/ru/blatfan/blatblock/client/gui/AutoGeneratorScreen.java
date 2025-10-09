package ru.blatfan.blatblock.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import ru.blatfan.blatapi.common.guide_book.pages.TextPage;
import ru.blatfan.blatapi.utils.GuiUtil;
import ru.blatfan.blatblock.BlatBlock;
import ru.blatfan.blatblock.common.block.autogenerator.AutoGeneratorMenu;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AutoGeneratorScreen extends AbstractContainerScreen<AutoGeneratorMenu> {
    public AutoGeneratorScreen(AutoGeneratorMenu pMenu, Inventory inv, Component component) {
        super(pMenu, inv, component);
        this.imageWidth=197;
        this.imageHeight=168;
        this.inventoryLabelY+=5;
    }
    
    @Override
    public void render(GuiGraphics gui, int mX, int mY, float partialTick) {
        super.render(gui, mX, mY, partialTick);
        renderTooltip(gui, mX, mY);
    }
    
    public ResourceLocation getTexture(){
        return switch (menu.getGenType()){
            case BASIC -> BlatBlock.loc("textures/gui/basic_auto_generator.png");
            case IMPROVED -> BlatBlock.loc("textures/gui/improved_auto_generator.png");
            case PERFECT -> BlatBlock.loc("textures/gui/perfect_auto_generator.png");
        };
    }
    
    @Override
    protected void renderBg(GuiGraphics gui, float pPartialTick, int pMouseX, int pMouseY) {
        renderBackground(gui);
        gui.blit(getTexture(), leftPos, topPos, 0, 0, imageWidth, imageHeight);
        gui.blit(getTexture(), leftPos+7, topPos+72, 0, 168, 162, 3);
        gui.blit(getTexture(), leftPos+7, topPos+72, 0, 171, (int) (162*(menu.getProgress()/(float)menu.getProgressMax())), 3);
    }
    
    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        GuiUtil.drawScaledCentreString(pGuiGraphics, this.title, 100, 2, Color.WHITE,
            TextPage.findOptimalScale(new ArrayList<>(List.of(this.title)), 143, 10));
        pGuiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }
    
    @Override
    protected void renderTooltip(GuiGraphics gui, int x, int y) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            ItemStack itemstack = this.hoveredSlot.getItem();
            renderTooltip(gui, this.getTooltipFromContainerItem(itemstack), x, y);
        }
        if(BlatBlockScreen.isMouseOver(x,y, leftPos+7, topPos+72, 162, 3))
            renderTooltip(gui, new ArrayList<>(List.of(Component.literal((int)((menu.getProgress()/(float)menu.getProgressMax())*100)+"%"))), x, y);
    }
    
    private void renderTooltip(GuiGraphics gui, List<Component> tooltips, int rawMX, int rawMY) {
        int mX = rawMX+8;
        int mY = rawMY-8;
        AtomicInteger maxWidth = new AtomicInteger();
        tooltips.forEach(tooltip -> {
            if(maxWidth.get() <font.width(tooltip)+2) maxWidth.set(font.width(tooltip) + 2);
        });
        gui.pose().pushPose();
        gui.pose().translate(0, 0, 500);
        
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1,  1,1, 0.7f);
        gui.blitNineSlicedSized(getTexture(), mX, mY-1, maxWidth.get(), tooltips.size()*font.lineHeight+2, 11, 33, 33, 0, 176, 256, 256);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        
        for(int i=0; i<tooltips.size(); i++)
            gui.drawString(font, tooltips.get(i), mX+1, mY+1+i*font.lineHeight, Color.WHITE.getRGB());
        
        gui.pose().popPose();
    }
}