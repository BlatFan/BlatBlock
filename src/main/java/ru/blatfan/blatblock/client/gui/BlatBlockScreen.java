package ru.blatfan.blatblock.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.glfw.GLFW;
import ru.blatfan.blatapi.client.guide_book.GuideClient;
import ru.blatfan.blatapi.client.render.FluidRenderMap;
import ru.blatfan.blatapi.utils.*;
import ru.blatfan.blatblock.BlatBlock;
import ru.blatfan.blatblock.common.block.blatgenerator.BlatGeneratorBlockEntity;
import ru.blatfan.blatblock.common.data.BlatBlockLevel;
import ru.blatfan.blatblock.common.data.BlatBlockManager;
import ru.blatfan.blatblock.common.network.BBHandler;
import ru.blatfan.blatblock.common.network.BlatGeneratorPacket;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BlatBlockScreen extends Screen {
    private float page = 0, tPage = 0;
    private int pageMax;
    protected int leftPos;
    protected int topPos;
    
    private ResourceLocation current;
    
    private float entityOffset, tEntityOffset;
    private float blocksOffset, tBlocksOffset;
    
    private final BlatGeneratorBlockEntity entity;
    
    private final Map<Integer, List<Block>> blockCache = new HashMap<>();
    private final Map<Integer, List<EntityType<?>>> entityCache = new HashMap<>();
    private final Map<EntityType<?>, Entity> entityPool = new HashMap<>();
    private int lastCacheUpdate = -1;
    
    private static final int MAX_VISIBLE_BLOCKS = 49;
    private static final int MAX_VISIBLE_ENTITIES = 16;
    private static final int CACHE_UPDATE_INTERVAL = 30;
    
    public BlatBlockScreen(BlatGeneratorBlockEntity entity) {
        super(Component.empty());
        this.entity = entity;
    }
    
    @Override
    protected void init() {
        this.leftPos = (this.width - 260) / 2;
        this.topPos = (this.height - 134) / 2;
        
        this.entityOffset = 0;
        this.tEntityOffset = 0;
        this.blocksOffset = 0;
        this.tBlocksOffset = 0;
        
        super.init();
    }
    
    private BlatBlockLevel getBBLevel() {
        return getBBLevel(current);
    }
    
    private BlatBlockLevel getBBLevel(ResourceLocation id) {
        BlatBlockLevel level = BlatBlockManager.get(id);
        return level != null ? level : BlatBlockManager.NULL_BBL;
    }
    
    @Override
    public void render(GuiGraphics gui, int mX, int mY, float partialTick) {
        if(current==null) current=entity.getCurrentBBLevel();
        pageMax = 0;
        for(int i=0; i<entity.getMinedBlocks().size(); i++){
            ResourceLocation blockLevel = entity.getMinedBlocks().entrySet().stream().toList().get(i).getKey();
            int s = font.width(getBBLevel(blockLevel).getTitle())+2;
            pageMax+=s;
        }
        if(pageMax<=260) tPage=0;
        
        page = MathUtils.lerp(page, tPage, 0.1f);
        entityOffset = MathUtils.lerp(entityOffset, tEntityOffset, 0.1f);
        blocksOffset = MathUtils.lerp(blocksOffset, tBlocksOffset, 0.1f);
        
        renderBackground(gui);
        renderForLevel(gui, leftPos, topPos, mX, mY, entity.getCurrentLevel(current));
        if (entity.getCurrentLevel(current) + 1 < 99)
            renderForLevel(gui, leftPos + 130, topPos, mX, mY, entity.getCurrentLevel(current) + 1);
        renderButtons(gui, mX, mY, partialTick);
        
        renderSetOrBuy(gui, mX, mY, partialTick);
    }
    private void renderSetOrBuy(GuiGraphics gui, int mX, int mY, float partialTick) {
        ResourceLocation t = BlatBlock.loc("textures/gui/tooltips.png");
        if(entity.getCurrentBBLevel().equals(current)) return;
        Component text = entity.getMinedBlock(current)>0 ? Component.translatable("tooltip.blatblock.set") : Component.translatable("tooltip.blatblock.buy");
        gui.blitNineSlicedSized(t, leftPos+130-font.width(text)/2, topPos+150, font.width(text)+4, 16, 11, 33, 33, 0, 0,  33, 33);
        if(!canChange(current)) {
            text = text.copy().withStyle(ChatFormatting.STRIKETHROUGH);
            if(isMouseOver(mX, mY, leftPos+130-font.width(text)/2f, topPos+150, font.width(text)+4, 16))
                renderTooltip(gui, new ArrayList<>(List.of(Text.create("tooltip.blatblock.mined_with_next", entity.getAllMinedBlock(), getBBLevel().getBlockCost()))), mX, mY);
        }
        gui.drawString(font, text, leftPos+131-font.width(text)/2, topPos+152, Color.WHITE.getRGB());
    }
    
    private void renderButtons(GuiGraphics gui, int mX, int mY, float partialTick) {
        ResourceLocation b = BlatBlock.loc("textures/gui/buttons.png");
        
        gui.enableScissor(leftPos, topPos+133, leftPos+260, topPos+133+16);
        int x = leftPos;
        for(int i=0; i<entity.getSorted().size(); i++){
            ResourceLocation blockLevel = entity.getSorted().get(i);
            int s = font.width(getBBLevel(blockLevel).getTitle())+2;
            gui.blitNineSlicedSized(b, (int) (x-page), topPos+133, s, 16, 6, 32, 16, 0, blockLevel.equals(entity.getCurrentBBLevel()) ? 16 : 0, 32, 32);
            gui.drawString(font, getBBLevel(blockLevel).getTitle(), (int) (x+1-page), topPos+133+2, getBBLevel(blockLevel).getTitleColor().getRGB());
            x+=s+2;
        }
        gui.disableScissor();
    }
    
    @Override
    public void renderBackground(GuiGraphics gui) {
        gui.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        ResourceLocation texture = getBBLevel().getTexture();
        gui.blitNineSlicedSized(texture, leftPos - 1, topPos - 1, 260, 134, 10, 30, 30, 0, 0, 64, 64);
        if(getBBLevel().getBg()!=null)
            gui.blit(getBBLevel().getBg(), leftPos, topPos, 258, 132, 0, 0, 258, 132, 258, 132);
        GuiUtil.drawScaledCentreString(gui, getBBLevel().getTitle(), leftPos + 130, topPos - 18, getBBLevel().getTitleColor(), 2);
        if(!canChange(current)) {
            gui.pose().pushPose();
            gui.pose().translate(0, 0, 1000);
            gui.fillGradient(leftPos - 1, topPos - 1, leftPos - 1+260, topPos - 1+134, -1072689136, -804253680);
            gui.pose().popPose();
        }
    }
    
    private void renderForLevel(GuiGraphics gui, int leftPos, int topPos, int mX, int mY, int blockLevel) {
        gui.drawString(font, String.valueOf(blockLevel + 1), leftPos, topPos, Color.WHITE.getRGB());
        ResourceLocation texture = getBBLevel().getTexture();
        
        List<Block> levelBlocks = getCachedBlocks(blockLevel);
        List<EntityType<?>> levelEntities = getCachedEntities(blockLevel);
        List<Entity> cachedEntities = new ArrayList<>();
        
        renderBlocks(gui, leftPos, topPos, texture, levelBlocks);
        renderEntities(gui, leftPos, topPos, texture, levelEntities, cachedEntities);
        handleTooltips(gui, leftPos, topPos, mX, mY, blockLevel, levelBlocks, levelEntities, cachedEntities);
    }
    
    private List<Block> getCachedBlocks(int level) {
        updateCacheIfNeeded();
        return blockCache.computeIfAbsent(level, l -> {
            List<Block> blocks = getBBLevel().getBlocks(l);
            return blocks.size() > MAX_VISIBLE_BLOCKS ?
                blocks.subList(0, MAX_VISIBLE_BLOCKS) : blocks;
        });
    }
    
    private List<EntityType<?>> getCachedEntities(int level) {
        updateCacheIfNeeded();
        return entityCache.computeIfAbsent(level, l -> {
            List<EntityType<?>> entities = getBBLevel().getEntities(l);
            return entities.size() > MAX_VISIBLE_ENTITIES ?
                entities.subList(0, MAX_VISIBLE_ENTITIES) : entities;
        });
    }
    
    private void updateCacheIfNeeded() {
        int currentTime = (int) (System.currentTimeMillis() / 1000);
        if (currentTime - lastCacheUpdate > CACHE_UPDATE_INTERVAL) {
            updateCache();
        }
    }
    private void updateCache() {
        int currentTime = (int) (System.currentTimeMillis() / 1000);
        blockCache.clear();
        entityCache.clear();
        lastCacheUpdate = currentTime;
    }
    
    private Entity getPooledEntity(EntityType<?> type) {
        return entityPool.computeIfAbsent(type, t -> {
            try {
                if (minecraft != null && minecraft.level != null)
                    return t.create(minecraft.level);
                return null;
            } catch (Exception e) {
                BlatBlock.LOGGER.warn("Failed to create pooled entity {}: {}",
                    t.getDescriptionId(), e.getMessage());
                return null;
            }
        });
    }
    
    private void renderBlocks(GuiGraphics gui, int leftPos, int topPos, ResourceLocation texture, List<Block> levelBlocks) {
        if (levelBlocks.isEmpty()) return;
        
        PoseStack pose = gui.pose();
        
        gui.enableScissor(leftPos + 1, topPos + 10, leftPos + 129, topPos + 44);
        pose.pushPose();
        pose.translate(0, -blocksOffset, 0);
        
        for (int i = 0; i < levelBlocks.size(); i++) {
            int x = i % 7;
            int y = i / 7;
            gui.blit(texture, leftPos + 1 + x * 18, topPos + 10 + y * 18, 0, 30, 18, 18, 64, 64);
        }
        
        for (int i = 0; i < levelBlocks.size(); i++) {
            int x = i % 7;
            int y = i / 7;
            ItemStack stack = getItemStack(levelBlocks.get(i));
            if(stack!=null) {
                gui.renderItem(stack, leftPos + 2 + x * 18, topPos + 11 + y * 18);
                gui.renderItemDecorations(font, stack, leftPos + 2 + x * 18, topPos + 11 + y * 18);
            } else if(levelBlocks.get(i) instanceof LiquidBlock l) {
                TextureAtlasSprite sprite = FluidRenderMap.getCachedFluidTexture(new FluidStack(l.getFluid().getSource(), 1000), FluidRenderMap.FluidFlow.STILL);
                if (l.getFluid().getSource() == Fluids.WATER)
                    RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
                gui.blit(leftPos + 2 + x * 18, topPos + 11 + y * 18, 0, 16, 16, sprite);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
        pose.popPose();
        gui.disableScissor();
    }
    
    private ItemStack getItemStack(Block block) {
        if (block instanceof LiquidBlock) return null;
        Item blockItem = BlockItem.BY_BLOCK.get(block);
        if (blockItem != null && blockItem != Items.AIR) {
            return new ItemStack(blockItem);
        }
        Item asItem = block.asItem();
        if (asItem != Items.AIR) {
            return new ItemStack(asItem);
        }
        return ItemStack.EMPTY;
    }
    
    private void renderEntities(GuiGraphics gui, int leftPos, int topPos, ResourceLocation texture, List<EntityType<?>> levelEntities, List<Entity> cachedEntities) {
        if (levelEntities.isEmpty() || minecraft == null || minecraft.level == null) return;
        
        PoseStack pose = gui.pose();
        
        gui.enableScissor(leftPos + 1, topPos + 45, leftPos + 129, topPos + 132);
        pose.pushPose();
        pose.translate(0, -entityOffset, 0);
        
        for (int i = 0; i < levelEntities.size(); i++) {
            int x = i % 4;
            int y = i / 4;
            gui.blit(texture, leftPos + 1 + x * 32, topPos + 45 + y * 48, 32, 0, 32, 48, 64, 64);
        }
        
        for (int i = 0; i < levelEntities.size(); i++) {
            EntityType<?> entityType = levelEntities.get(i);
            Entity entity = getPooledEntity(entityType);
            cachedEntities.add(entity);
            
            if (entity instanceof LivingEntity living) {
                int x = i % 4;
                int y = i / 4;
                
                living.yHeadRot = 0;
                living.yHeadRotO = 0;
                
                float rotation = (float) ClientTicks.ticks + (i * 10);
                
                try {
                    GuiUtil.renderEntityQuaternionf(gui,
                        leftPos + 16 + x * 32,
                        topPos + 83 +(living.getEyeHeight() > 2 ? 7 : 0) - (living.getEyeHeight() < 1 ? 7 : 0) + y * 48,
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
        
        pose.popPose();
        gui.disableScissor();
    }
    
    private void handleTooltips(GuiGraphics gui, int leftPos, int topPos, int mX, int mY, int blockLevel, List<Block> levelBlocks, List<EntityType<?>> levelEntities, List<Entity> cachedEntities) {
        for (int i = 0; i < levelBlocks.size(); i++) {
            int x = i % 7;
            int y = i / 7;
            
            int blockX = leftPos + 2 + x * 18;
            int blockY = topPos + 11 + y * 18 - (int)blocksOffset;
            
            if (isMouseOver(mX, mY, blockX, blockY, 16, 16) && isMouseOver(mX, mY, leftPos+1, topPos+10,  128, 34)) {
                ItemStack item = getItemStack(levelBlocks.get(i));
                float chance = getBBLevel().get(levelBlocks.get(i)).chance(blockLevel);
                if(item!=null)renderTooltip(gui, item, chance, mX, mY);
                else if(levelBlocks.get(i) instanceof LiquidBlock l) renderTooltip(gui, l.getFluid(), chance, mX, mY);
                return;
            }
        }
        
        for (int i = 0; i < Math.min(levelEntities.size(), cachedEntities.size()); i++) {
            Entity entity = cachedEntities.get(i);
            if (entity == null) continue;
            
            int x = i % 4;
            int y = i / 4;
            
            int entityX = leftPos + 1 + x * 32;
            int entityY = topPos + 45 + y * 48 - (int)entityOffset;
            
            if (isMouseOver(mX, mY, entityX, entityY, 32, 48) && isMouseOver(mX, mY, leftPos+1, topPos+45,  128, 87)) {
                float chance = getBBLevel().get(levelEntities.get(i)).chance(blockLevel);
                renderTooltip(gui, entity, chance, mX, mY);
                return;
            }
        }
    }
    
    public static boolean isMouseOver(double mouseX, double mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
    
    private void renderTooltip(GuiGraphics gui, List<Component> tooltips, int rawMX, int rawMY) {
        ResourceLocation t = BlatBlock.loc("textures/gui/tooltips.png");
        int mX = rawMX+8;
        int mY = rawMY-8;
        Color color = getBBLevel().getTitleColor();
        AtomicInteger maxWidth = new AtomicInteger();
        tooltips.set(0, Text.create("  ").add(tooltips.get(0)));
        tooltips.forEach(tooltip -> {
            if(maxWidth.get() <font.width(tooltip)+2) {
                maxWidth.set(font.width(tooltip) + 2);
                if (tooltips.indexOf(tooltip) == tooltips.size() - 1) maxWidth.set(maxWidth.get()+2);
            }
        });
        gui.pose().pushPose();
        gui.pose().translate(0, 0, 500);
        
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, 0.7f);
        gui.blitNineSlicedSized(t, mX, mY-1, maxWidth.get(), tooltips.size()*font.lineHeight+2, 11, 33, 33, 0, 0,  33, 33);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        
        for(int i=0; i<tooltips.size(); i++)
            gui.drawString(font, tooltips.get(i), mX+1, mY+1+i*font.lineHeight, Color.WHITE.getRGB());
        
        gui.pose().popPose();
    }
    
    private void renderTooltip(GuiGraphics gui, ItemStack item, float chance, int mX, int mY) {
        List<Component> tooltips = item.getTooltipLines(GuideClient.player, GuideClient.tooltipFlag);
        if(item.getItem() instanceof BucketItem bucketItem)
            tooltips.set(0, bucketItem.getFluid().getFluidType().getDescription());
        tooltips.add(Text.create("tooltip.blatblock.chance").add(String.format("%.1f%%", chance * 100)));
        renderTooltip(gui, tooltips, mX, mY);
    }
    
    private void renderTooltip(GuiGraphics gui, Fluid fluid, float chance, int mX, int mY) {
        List<Component> tooltips = new ArrayList<>();
        tooltips.add(fluid.getFluidType().getDescription());
        tooltips.add(Text.create(BuiltInRegistries.FLUID.getKey(fluid).toString()).withColor(Color.DARK_GRAY));
        tooltips.add(Text.create("tooltip.blatblock.chance").add(String.format("%.1f%%", chance * 100)));
        renderTooltip(gui, tooltips, mX, mY);
    }
    
    private void renderTooltip(GuiGraphics gui, Entity entity, float chance, int mX, int mY) {
        List<Component> tooltips = new ArrayList<>();
        tooltips.add(entity.getDisplayName());
        tooltips.add(Text.create(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString()).withColor(Color.DARK_GRAY));
        tooltips.add(Text.create("tooltip.blatblock.chance").add(String.format("%.1f%%", chance * 100)));
        renderTooltip(gui, tooltips, mX, mY);
    }
    
    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        int x = leftPos;
        for(int i=0; i<entity.getSorted().size(); i++){
            ResourceLocation blockLevel = entity.getSorted().get(i);
            int s = font.width(getBBLevel(blockLevel).getTitle())+2;
            if(pButton==0 && isMouseOver(pMouseX, pMouseY, x-page, topPos+133, s, 16)){
                current=blockLevel;
                updateCache();
                tBlocksOffset=0;
                tEntityOffset=0;
                return true;
            }
            x+=s+2;
        }
        if(!entity.getCurrentBBLevel().equals(current) && pButton==0 && canChange(current)) {
            boolean isSet = entity.getMinedBlock(current) > 0;
            Component text = isSet ? Component.translatable("tooltip.blatblock.set") : Component.translatable("tooltip.blatblock.buy");
            if(isMouseOver(pMouseX, pMouseY, leftPos+130-font.width(text)/2d, topPos+150, font.width(text)/2d+4, 16)) {
                BBHandler.sendToServer(new BlatGeneratorPacket(entity.getBlockPos(), current, isSet ? BlatGeneratorPacket.Type.SET : BlatGeneratorPacket.Type.BUY));
                onClose();
                return true;
            }
        }
        return false;
    }
    
    private boolean canChange(ResourceLocation bbl){
        return entity.getAllMinedBlock()>=BlatBlockManager.get(bbl).getBlockCost();
    }
    
    @Override
    public boolean mouseScrolled(double mX, double mY, double delta) {
        if (isMouseOver(mX, mY, leftPos+1, topPos+10, 258, 34)) {
            List<Block> allBlocks = getCachedBlocks(entity.getCurrentLevel());
            int maxRows = (allBlocks.size() + 6) / 7;
            float visibleRows = 1.8f;
            float maxOffset = Math.max(0, (maxRows - visibleRows) * 18);
            
            tBlocksOffset = Math.max(0, Math.min(maxOffset, tBlocksOffset - (float) delta * 18));
            return true;
        }
        if (isMouseOver(mX, mY, leftPos+1, topPos+45, 258, 87)) {
            List<EntityType<?>> allEntities = getCachedEntities(entity.getCurrentLevel());
            int maxRows = (allEntities.size() + 3) / 4;
            float visibleRows = 1.8f;
            float maxOffset = Math.max(0, (maxRows - visibleRows) * 48);
            
            tEntityOffset = Math.max(0, Math.min(maxOffset, tEntityOffset - (float) delta * 48));
            return true;
        }
        if (isMouseOver(mX, mY, leftPos, topPos+133, 260, 16)) {
            tPage = Math.max(0, Math.min(pageMax, tPage - (float) delta * 16));
            return true;
        }
        return false;
    }
    
    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == GLFW.GLFW_KEY_ESCAPE || pKeyCode == GLFW.GLFW_KEY_E) {
            onClose();
            return true;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }
    
    @Override
    public void onClose() {
        blockCache.clear();
        entityCache.clear();
        
        entityPool.values().forEach(entity -> {
            if (entity != null) entity.discard();
        });
        entityPool.clear();
        
        super.onClose();
    }
}