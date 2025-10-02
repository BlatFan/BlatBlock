package ru.blatfan.blatblock.compat.jei.entities;

import com.mojang.math.Axis;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import ru.blatfan.blatapi.client.guide_book.GuideClient;
import ru.blatfan.blatapi.utils.ClientTicks;
import ru.blatfan.blatapi.utils.GuiUtil;
import ru.blatfan.blatapi.utils.Text;
import ru.blatfan.blatblock.BlatBlock;
import ru.blatfan.blatblock.compat.jei.BBLRecipe;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static ru.blatfan.blatblock.client.gui.BlatBlockScreen.isMouseOver;

public class BBLEntitiesWrapper extends BBLRecipe {
    public BBLEntitiesWrapper(ResourceLocation bbl, int level) {
        super(bbl, level);
    }
    
    @Override
    public void onTooltip(IRecipeSlotView recipeSlotView, List<Component> tooltip) {}
    
    @Override
    public List<Component> getTooltipStrings(double mX, double mY) {
        List<EntityType<?>> levelEntities = get().getEntities(getLevel());
        for (int i = 0; i < levelEntities.size(); i++) {
            int x = i % 4;
            int y = i / 4;
            
            int blockX = 2 + x * 32;
            int blockY = 11 + y * 48;
            
            if (isMouseOver(mX, mY, blockX, blockY, 16, 16)) {
                List<Component> tooltips = new ArrayList<>();
                EntityType<?> entityType = levelEntities.get(i);
                Entity fakeEntity = null;
                if(Minecraft.getInstance().level!=null)
                    fakeEntity = entityType.create(Minecraft.getInstance().level);
                float chance = get().get(entityType).chance(getLevel());
                if(fakeEntity!=null)
                    tooltips.add(fakeEntity.getDisplayName());
                tooltips.add(Text.create(BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString()).withColor(Color.DARK_GRAY));
                tooltips.add(Text.create("tooltip.blatblock.chance").add(String.format("%.1f%%", chance * 100)));
                return tooltips;
            }
        }
        return new ArrayList<>();
    }
    
    @Override
    public void drawInfo(int recipeWidth, int recipeHeight, GuiGraphics gui, double mouseX, double mouseY) {
        ResourceLocation texture = get().getTexture();
        
        boolean isMaxLevel = getLevel() >= 99;
        String levelText = isMaxLevel ? Component.translatable("tooltip.blatblock.max").getString() : String.valueOf(getLevel()+1);
        gui.drawString(GuideClient.font,
            Text.create("tooltip.blatblock.level", get().getTitle().getString(), levelText)
                .withColor(get().getTitleColor())
                .asComponent(), 0, 0, Color.WHITE.getRGB());
        
        List<EntityType<?>> levelEntities = get().getEntities(getLevel());
        for (int i = 0; i < levelEntities.size(); i++) {
            int x = i % 4;
            int y = i / 4;
            gui.blit(texture, 1 + x * 32, 10 + y * 48, 32, 0, 32, 48, 64, 64);
        }
        
        for (int i = 0; i < levelEntities.size(); i++) {
            EntityType<?> entityType = levelEntities.get(i);
            if(Minecraft.getInstance().level==null) return;
            Entity entity = entityType.create(Minecraft.getInstance().level);
            
            if (entity instanceof LivingEntity living) {
                int x = i % 4;
                int y = i / 4;
                
                living.yHeadRot = 0;
                living.yHeadRotO = 0;
                
                float rotation = (float) ClientTicks.ticks + (i * 10);
                
                try {
                    GuiUtil.renderEntityQuaternionf(gui,
                        16 + x * 32,
                        48 +(living.getEyeHeight() > 2 ? 7 : 0) - (living.getEyeHeight() < 1 ? 7 : 0) + y * 48,
                        16,
                        Axis.YP.rotationDegrees(rotation),
                        Axis.XP.rotationDegrees(180),
                        living);
                } catch (Exception e) {
                    BlatBlock.LOGGER.warn("Failed to render entity {}: {}",
                        entityType.getDescriptionId(), e.getMessage());
                }
            }
        }
    }
}