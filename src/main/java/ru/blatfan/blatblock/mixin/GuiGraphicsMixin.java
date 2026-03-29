package ru.blatfan.blatblock.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ItemDecoratorHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.blatfan.blatapi.common.guide_book.pages.TextPage;
import ru.blatfan.blatapi.utils.GuiUtil;
import ru.blatfan.blatapi.utils.collection.Text;
import ru.blatfan.blatblock.BlatBlock;
import ru.blatfan.blatblock.client.gui.AutoGeneratorScreen;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @Final @Shadow
    private Minecraft minecraft;
    @Final @Shadow
    private PoseStack pose;
    @Shadow
    public abstract void fill(RenderType pRenderType, int pMinX, int pMinY, int pMaxX, int pMaxY, int pColor);
    
    @Inject(at = @At("HEAD"), method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", cancellable = true)
    public void renderItemDecorations(Font pFont, ItemStack pStack, int pX, int pY, String pText, CallbackInfo ci) {
        if(!(minecraft.screen instanceof AutoGeneratorScreen) || pStack.getCount()<=pStack.getMaxStackSize()) return;
        ci.cancel();
        GuiGraphics gui = (GuiGraphics) ((Object)this);
        if (!pStack.isEmpty()) {
            pose.pushPose();
            if (pStack.getCount() != 1 || pText != null) {
                String s = pText == null ? String.valueOf(pStack.getCount()) : pText;
                pose.translate(0, 0, 200);
                float scale = Math.min(1, GuiUtil.findWidthScale(s, 14));
                Color color = BlatBlock.ConfigClient.ITEM_COUNT_COLOR.get();
                GuiUtil.drawScaledString(gui, s, (int) (pX + 17 - pFont.width(s)*scale), (int) (pY + 8*(2-scale)), color, scale);
            }
            
            if (pStack.isBarVisible()) {
                int l = pStack.getBarWidth();
                int i = pStack.getBarColor();
                int j = pX + 2;
                int k = pY + 13;
                fill(RenderType.guiOverlay(), j, k, j + 13, k + 2, -16777216);
                fill(RenderType.guiOverlay(), j, k, j + l, k + 1, i | -16777216);
            }
            
            LocalPlayer localplayer = minecraft.player;
            float f = localplayer == null ? 0.0F : localplayer.getCooldowns().getCooldownPercent(pStack.getItem(), minecraft.getFrameTime());
            if (f > 0.0F) {
                int i1 = pY + Mth.floor(16.0F * (1.0F - f));
                int j1 = i1 + Mth.ceil(16.0F * f);
                fill(RenderType.guiOverlay(), pX, i1, pX + 16, j1, Integer.MAX_VALUE);
            }
            
            pose.popPose();
            ItemDecoratorHandler.of(pStack).render(gui, pFont, pStack, pX, pY);
        }
    }
}