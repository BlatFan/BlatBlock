package ru.blatfan.blatblock.client.block_render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import ru.blatfan.blatblock.BlatBlock;
import ru.blatfan.blatblock.common.block.blatgenerator.BlatGeneratorBlockEntity;

import java.util.List;


public class BlatGeneratorRenderer implements BlockEntityRenderer<BlatGeneratorBlockEntity> {
    public BlatGeneratorRenderer(BlockEntityRendererProvider.Context pContext){}
    @Override
    public void render(BlatGeneratorBlockEntity entity, float partialTick, PoseStack pose, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (entity == null || pose == null || buffer == null || !BlatBlock.ConfigClient.GENERATOR_HOLOGRAM.get()) return;
        List<Component> renderTexts = entity.getRenderText();
        if (renderTexts == null || renderTexts.isEmpty()) return;
        Font font = Minecraft.getInstance().font;
        if (font == null) return;
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (entityRenderDispatcher == null) return;
        
        int size = renderTexts.size();
        int maxLines = Math.min(size, 10);
        
        for (int i = 0; i < maxLines; i++) {
            Component text = renderTexts.get(i);
            if (text == null) continue;
            
            renderTextLine(text, i, maxLines, pose, buffer, font, entityRenderDispatcher, packedLight);
        }
    }
    
    private void renderTextLine(Component text, int lineIndex, int totalLines, PoseStack pose, MultiBufferSource buffer, Font font, EntityRenderDispatcher entityRenderDispatcher, int packedLight) {
        pose.pushPose();
        try {
            float textWidth = font.width(text);
            float offsetX = -textWidth / 2.0f;
            float offsetY = 3.0f + (totalLines - lineIndex) * 0.5f;
            
            pose.translate(0.5, offsetY, 0.5);
            pose.mulPose(entityRenderDispatcher.cameraOrientation());
            pose.scale(-0.025f, -0.025f, 0.025f);
            
            Matrix4f matrix4f = pose.last().pose();
            
            font.drawInBatch(text, offsetX, 0,
                getTextColor(lineIndex),
                false, matrix4f, buffer,
                Font.DisplayMode.NORMAL,
                0,
                packedLight);
            
        } catch (Exception e) {
            BlatBlock.LOGGER.warn("Failed to render text line {}: {}", lineIndex, e.getMessage());
        } finally {
            pose.popPose();
        }
    }
    
    private int getTextColor(int lineIndex) {
        return switch (lineIndex) {
            case 0 -> 0xFFFFFF;
            case 1 -> 0x80FF00;
            default -> 0xCCCCCC;
        };
    }
    
}
