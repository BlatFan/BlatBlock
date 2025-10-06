package ru.blatfan.blatblock.common.block.autogenerator;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import ru.blatfan.blatapi.fluffy_fur.client.gui.screen.ContainerMenuBase;
import ru.blatfan.blatapi.fluffy_fur.client.gui.screen.ResultSlot;
import ru.blatfan.blatblock.BlatBlock;
import ru.blatfan.blatblock.common.BBRegistry;

import java.util.Optional;

@Getter
public class AutoGeneratorMenu extends ContainerMenuBase {
    private final BlockEntity blockEntity;
    private final AutoGeneratorBlock.Type genType;
    private final ContainerData data;
    
    public AutoGeneratorMenu(int pContainerId, BlockPos pos, Inventory inventory, AutoGeneratorBlock.Type genType) {
        super(BBRegistry.AUTO_GENERATOR_MENU.get(), pContainerId);
        this.playerInventory = new InvWrapper(inventory);
        this.blockEntity = inventory.player.level().getBlockEntity(pos);
        this.genType = genType;
        if (blockEntity instanceof AutoGeneratorBlockEntity be) {
            this.data=be.getData();
            Optional<ItemStackHandler> itemHandler = getItemHandlerSafe();
            if (itemHandler.isPresent()) {
                ItemStackHandler handler = itemHandler.get();
                BlatBlock.LOGGER.debug("ItemHandler has {} slots", handler.getSlots());
                if (handler.getSlots() >= 31) {
                    for(int i=0; i<27; i++)
                        addSlot(new ResultSlot(handler, i, 8+(i%9)*18, 18+(i/9)*18));
                    addSlot(new GeneratorUpgradeSlot(handler,27, 178,8));
                    addSlot(new GeneratorUpgradeSlot(handler,28, 178,26));
                    addSlot(new GeneratorUpgradeSlot(handler,29, 178,44));
                    addSlot(new GeneratorUpgradeSlot(handler,30, 178,62));
                } else
                    BlatBlock.LOGGER.error("ItemHandler has only {} slots, but 31 are required", handler.getSlots());
            } else
                BlatBlock.LOGGER.error("BlockEntity does not have an ItemHandler capability");
        } else {
            BlatBlock.LOGGER.error("BlockEntity at {} is not a AutoGeneratorBlockEntity", pos);
            this.data=new SimpleContainerData(2);
            this.data.set(0, 0);
            this.data.set(1, 1);
        }
        
        addDataSlots(data);
        this.layoutPlayerInventorySlots(8, 86);
    }
    
    public int getProgress(){
        return data.get(0);
    }
    public int getProgressMax(){
        return data.get(1);
    }
    
    public Optional<ItemStackHandler> getItemHandlerSafe() {
        if (blockEntity == null)
            return Optional.empty();
        return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER)
            .resolve()
            .map(cap -> {
                if (cap instanceof ItemStackHandler) {
                    return (ItemStackHandler) cap;
                }
                BlatBlock.LOGGER.warn("ItemHandler is not an instance of ItemStackHandler: {}", cap.getClass());
                return new ItemStackHandler(31);
            });
    }
    
    @Override
    public int getInventorySize() {
        if (blockEntity == null) return 0;
        return getItemHandlerSafe().get().getSlots();
    }
    
    @Override
    public boolean stillValid(Player player) {
        if (blockEntity == null || blockEntity.isRemoved()) return false;
        BlockState state = blockEntity.getBlockState();
        double distance = player.position().distanceTo(blockEntity.getBlockPos().getCenter());
        boolean isValidState = BBRegistry.BLOCK_ENTITIES.AUTO_GENERATOR.get().isValid(state);
        boolean isCloseEnough = distance < 9;
        return isValidState && isCloseEnough;
    }
}
