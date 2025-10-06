package ru.blatfan.blatblock.common.block.autogenerator;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import ru.blatfan.blatblock.common.item.GeneratorUpgradeItem;

public class GeneratorUpgradeSlot extends SlotItemHandler {
    public GeneratorUpgradeSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }
    
    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return stack.getItem() instanceof GeneratorUpgradeItem;
    }
}
