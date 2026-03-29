package ru.blatfan.blatblock.common.events;

import dev.latvian.mods.kubejs.entity.forge.LivingEntityDropsEventJS;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.blatfan.blatapi.common.player_stages.PlayerStages;
import ru.blatfan.blatapi.utils.LevelWorldUtil;
import ru.blatfan.blatapi.utils.NBTHelper;
import ru.blatfan.blatapi.utils.PlayerUtil;
import ru.blatfan.blatblock.BlatBlock;
import ru.blatfan.blatblock.common.BBRegistry;
import ru.blatfan.blatblock.common.block.blatgenerator.BlatGeneratorBlock;
import ru.blatfan.blatblock.common.data.BlatBlockLayer;
import ru.blatfan.blatblock.common.data.BBLayerManager;
import ru.blatfan.blatblock.util.PlayerSettings;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = BlatBlock.MOD_ID)
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
            List<ItemStack> stacks = dropStack(PlayerSettings.tagItem(player), state, (ServerLevel) level, pos, null);
            stacks.forEach(stack -> {
                if(PlayerSettings.dropToInv(player)) PlayerUtil.addItem(player, stack);
                else {
                    ItemEntity entity = LevelWorldUtil.dropItemStackInWorld(level, pos.offset(0, 1, 0), stack);
                    entity.setDeltaMovement(0, 0, 0);
                }
            });
        }
    }
    
    @SubscribeEvent
    public static void entityDrop(LivingDropsEvent event){
        if(event.getEntity().getPersistentData().getString("source").equals("generator"))
            for (ItemEntity drop : event.getDrops()) {
                ItemStack stack = drop.getItem();
                NBTHelper.setString(stack, "source", "generator");
                drop.setItem(stack);
            }
    }
    
    public static List<ItemStack> dropStack(boolean tag, BlockState state, ServerLevel level, BlockPos pos, @Nullable BlockEntity blockEntity){
        List<ItemStack> drop = Block.getDrops(state, level, pos, blockEntity);
        if(tag) drop.forEach(stack -> NBTHelper.setString(stack, "source", "generator"));
        return drop;
    }
    
    @SubscribeEvent
    public static void genTooltip(ItemTooltipEvent event){
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
            if (player.blockPosition().distSqr(Vec3i.ZERO) > 100 && !PlayerStages.getBool(player, BlatBlock.loc("first_join"))) {
                player.teleportTo(0.5, 2, 0.5);
                PlayerStages.setBool(player, BlatBlock.loc("first_join"), true);
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