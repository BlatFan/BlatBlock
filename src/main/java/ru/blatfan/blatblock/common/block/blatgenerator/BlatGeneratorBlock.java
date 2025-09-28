package ru.blatfan.blatblock.common.block.blatgenerator;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import ru.blatfan.blatapi.fluffy_fur.common.block.BlatEntityBlock;
import ru.blatfan.blatblock.client.gui.BlatBlockScreen;

public class BlatGeneratorBlock extends BlatEntityBlock {
    public BlatGeneratorBlock() {
        super(Properties.copy(Blocks.BEDROCK).noOcclusion(), BlatGeneratorBlockEntity::new);
    }
    
    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(pLevel.isClientSide && pLevel.getBlockEntity(pPos) instanceof BlatGeneratorBlockEntity e && !pPlayer.isShiftKeyDown())
            Minecraft.getInstance().setScreen(new BlatBlockScreen(e));
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }
    
    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return (level, pos, state, t) -> ((BlatGeneratorBlockEntity)t).tick(level, pos);
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}