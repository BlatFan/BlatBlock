package ru.blatfan.blatblock.common.block.autogenerator;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import ru.blatfan.blatapi.fluffy_fur.common.block.BlatEntityBlock;

public class AutoGeneratorBlock extends BlatEntityBlock {
    private final Type type;
    public AutoGeneratorBlock(Type type) {
        super(createProperties(type), AutoGeneratorBlockEntity::new);
        this.type = type;
    }
    
    private static Properties createProperties(Type type) {
        return Properties.of()
            .mapColor(getMapColor(type))
            .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
            .requiresCorrectToolForDrops()
            .strength(getHardness(type), getResistance(type))
            .sound(SoundType.METAL)
            .noOcclusion();
    }
    
    private static MapColor getMapColor(Type type) {
        return switch (type) {
            case BASIC, PERFECT -> MapColor.METAL;
            case IMPROVED -> MapColor.DIAMOND;
        };
    }
    
    private static float getHardness(Type type) {
        return switch (type) {
            case BASIC -> 5.0F;
            case IMPROVED -> 7.0F;
            case PERFECT -> 10.0F;
        };
    }
    
    private static float getResistance(Type type) {
        return switch (type) {
            case BASIC -> 6.0F;
            case IMPROVED -> 8.0F;
            case PERFECT -> 12.0F;
        };
    }
    
    public static AutoGeneratorBlock basic(){
        return new AutoGeneratorBlock(Type.BASIC);
    }
    public static AutoGeneratorBlock improved(){
        return new AutoGeneratorBlock(Type.IMPROVED);
    }
    public static AutoGeneratorBlock perfect(){
        return new AutoGeneratorBlock(Type.PERFECT);
    }
    
    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(!pLevel.isClientSide && !pPlayer.isShiftKeyDown())
            NetworkHooks.openScreen(((ServerPlayer) pPlayer), menuProvider(pPos), data -> {
                data.writeBlockPos(pPos);
                data.writeEnum(type);
            });
        return InteractionResult.CONSUME;
    }
    
    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return (level, pos, state, be) -> ((AutoGeneratorBlockEntity)be).tick(level, pos, type);
    }
    
    public MenuProvider menuProvider(BlockPos pos){
        return new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
                return new AutoGeneratorMenu(pContainerId, pos, pPlayerInventory, type);
            }
            
            @Override
            public Component getDisplayName() {
                return getName();
            }
        };
    }
    
    public enum Type { BASIC, IMPROVED, PERFECT}
}