package ru.blatfan.blatblock.common.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ru.blatfan.blatapi.common.player_stages.PlayerStages;
import ru.blatfan.blatapi.utils.LevelWorldUtil;
import ru.blatfan.blatapi.utils.NBTHelper;
import ru.blatfan.blatapi.utils.PlayerUtil;
import ru.blatfan.blatblock.common.BBRegistry;
import ru.blatfan.blatblock.common.block.blatgenerator.BlatGeneratorBlock;
import ru.blatfan.blatblock.common.data.BlatBlockLayer;
import ru.blatfan.blatblock.common.data.BBLayerManager;
import ru.blatfan.blatblock.util.PlayerSettings;

import java.util.Random;

public class GeneratorEvents {
    @SubscribeEvent
    public static void blockClick(PlayerInteractEvent.RightClickBlock event){
        BlockPos clickPos = event.getPos();
        Level level = event.getEntity().level();
        Player player = event.getEntity();
        if(level.getBlockState(clickPos.below()).getBlock() instanceof BlatGeneratorBlock generator && !player.isShiftKeyDown())
            generator.use(level.getBlockState(clickPos.below()), level, clickPos.below(), event.getEntity(), event.getHand(), event.getHitVec());
    }
    
    @SubscribeEvent
    public static void blockDrop(BlockEvent.BreakEvent event){
        BlockPos pos = event.getPos();
        Player player = event.getPlayer();
        Level level = player.level();
        BlockState gen = level.getBlockState(pos.below());
        BlockState state = event.getState();
        if (level.isClientSide) return;
        if(gen.getBlock() instanceof BlatGeneratorBlock && !player.isCreative()
            && (!state.requiresCorrectToolForDrops() || player.getMainHandItem().isCorrectToolForDrops(state))){
            event.setCanceled(true);
            player.getMainHandItem().mineBlock(level, state, pos, player);
            level.destroyBlock(pos, false);
            ItemStack stack = dropStack(state.getBlock(), PlayerSettings.tagItem(player));
            if(PlayerSettings.dropToInv(player)) PlayerUtil.addItem(player, stack);
            else LevelWorldUtil.dropItemStackInWorld(level, pos.offset(0, 1, 0), stack);
        }
    }
    
    public static ItemStack dropStack(Block block, boolean tag){
        Item item = BlockItem.BY_BLOCK.getOrDefault(block, Items.AIR);
        ItemStack stack = new ItemStack(item);
        if(tag)NBTHelper.setString(stack, "source", "generator");
        return stack;
    }
    
    @SubscribeEvent
    public static void genBlockTooltip(ItemTooltipEvent event){
        ItemStack stack = event.getItemStack();
        if(NBTHelper.hasKey(stack, "source"))
            event.getToolTip().add(Component.translatable("tooltip.blatblock.source",
                Component.translatable("tooltip.blatblock.source."+NBTHelper.getString(stack, "source")).getString()));
    }
    
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        ServerLevel level = player.serverLevel();
        
        if (isEmptyVoidWorld(level)) {
            BlockPos generatorPos = new BlockPos(0, 0, 0);
            if (level.getBlockState(generatorPos).isAir()) {
                level.setBlock(generatorPos, BBRegistry.BLOCKS.BLAT_GENERATOR.get().defaultBlockState(), 3);
                BlatBlockLayer bbl = BBLayerManager.get(BBLayerManager.getBaseId());
                BlockState state = bbl.getRandBlock(new Random(), 0);
                level.setBlock(generatorPos.offset(1, 0, 0), state, 3);
                level.setBlock(generatorPos.offset(-1, 0, 0), state, 3);
                level.setBlock(generatorPos.offset(0, 0, 1), state, 3);
                level.setBlock(generatorPos.offset(0, 0, -1), state, 3);
            }
            if (player.blockPosition().distSqr(Vec3i.ZERO) > 100 && !PlayerStages.get(player, "bb_first_join")) {
                player.teleportTo(0.5, 2, 0.5);
                PlayerStages.add(player, "bb_first_join");
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 60*20, 4, true, true, true));
            }
        }
    }
    
    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        
        if (isEmptyVoidWorld(level))
            level.setDefaultSpawnPos(new BlockPos(0, 2, 0), 0.0f);
    }
    
    private static boolean isEmptyVoidWorld(ServerLevel level) {
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        if (generator instanceof FlatLevelSource flatGen)
            return flatGen.settings().getLayersInfo().isEmpty();
        return false;
    }
}