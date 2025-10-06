package ru.blatfan.blatblock.common;

import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import ru.blatfan.blatapi.common.registry.BlatRegister;
import ru.blatfan.blatblock.BlatBlock;
import ru.blatfan.blatblock.common.block.autogenerator.*;
import ru.blatfan.blatblock.common.block.blatgenerator.*;
import ru.blatfan.blatblock.common.item.GeneratorRing;
import ru.blatfan.blatblock.common.item.GeneratorUpgradeItem;
import ru.blatfan.blatblock.common.item.MultitoolItem;

import java.util.function.Supplier;

public class BBRegistry {
    private static final BlatRegister R = new BlatRegister(BlatBlock.MOD_ID);
    
    public static class BLOCKS {
        public static final RegistryObject<Block> BLAT_GENERATOR = R.block("blat_generator", BlatGeneratorBlock::new);
        public static final RegistryObject<Block> BASIC_AUTO_GENERATOR = register("basic_auto_generator", AutoGeneratorBlock::basic);
        public static final RegistryObject<Block> IMPROVED_AUTO_GENERATOR = register("improved_auto_generator", AutoGeneratorBlock::improved);
        public static final RegistryObject<Block> PERFECT_AUTO_GENERATOR = register("perfect_auto_generator", AutoGeneratorBlock::perfect);
        
        public static RegistryObject<Block> register(String id, Supplier<Block> supplier){
            RegistryObject<Block> blockRegistryObject = R.block(id, supplier);
            R.item(id, ()-> new BlockItem(blockRegistryObject.get(), new Item.Properties()));
            return blockRegistryObject;
        }
        public static void init(){}
    }
    public static class BLOCK_ENTITIES {
        public static final RegistryObject<BlockEntityType<BlatGeneratorBlockEntity>> BLAT_GENERATOR =
            R.block_entity_type("blat_generator",
                () -> BlockEntityType.Builder.of(BlatGeneratorBlockEntity::new, BLOCKS.BLAT_GENERATOR.get()).build(null));
        public static final RegistryObject<BlockEntityType<AutoGeneratorBlockEntity>> AUTO_GENERATOR =
            R.block_entity_type("auto_generator",
                () -> BlockEntityType.Builder.of(AutoGeneratorBlockEntity::new, BLOCKS.BASIC_AUTO_GENERATOR.get(), BLOCKS.IMPROVED_AUTO_GENERATOR.get(), BLOCKS.PERFECT_AUTO_GENERATOR.get()).build(null));
        
        public static void init(){}
    }
    public static class ITEMS {
        public static final RegistryObject<Item> DROP_RING = R.item("drop_ring", GeneratorRing::new);
        public static final RegistryObject<Item> ENTITY_RING = R.item("entity_ring", GeneratorRing::new);
        public static final RegistryObject<Item> TAG_RING = R.item("tag_ring", GeneratorRing::new);
        public static final RegistryObject<Item> LIQUID_RING = R.item("liquid_ring", GeneratorRing::new);
        
        public static final RegistryObject<Item> WOODEN_MULTITOOL = R.item("wooden_multitool", MultitoolItem::wooden);
        public static final RegistryObject<Item> STONE_MULTITOOL = R.item("stone_multitool", MultitoolItem::stone);
        public static final RegistryObject<Item> IRON_MULTITOOL = R.item("iron_multitool", MultitoolItem::iron);
        public static final RegistryObject<Item> GOLDEN_MULTITOOL = R.item("golden_multitool", MultitoolItem::gold);
        public static final RegistryObject<Item> DIAMOND_MULTITOOL = R.item("diamond_multitool", MultitoolItem::diamond);
        public static final RegistryObject<Item> NETHERITE_MULTITOOL = R.item("netherite_multitool", MultitoolItem::netherite);
        
        public static final RegistryObject<Item> BASIC_SPEED_GENERATOR_UPGRADE = R.item("basic_speed_generator_upgrade", ()-> new GeneratorUpgradeItem(GeneratorUpgradeItem.Type.SPEED, 1.25f));
        public static final RegistryObject<Item> BASIC_FORTUNE_GENERATOR_UPGRADE = R.item("basic_fortune_generator_upgrade", ()-> new GeneratorUpgradeItem(GeneratorUpgradeItem.Type.FORTUNE, 2f));
        public static final RegistryObject<Item> BASIC_TAG_GENERATOR_UPGRADE = R.item("basic_tag_generator_upgrade", ()-> new GeneratorUpgradeItem(GeneratorUpgradeItem.Type.TAG, 10f));
        public static final RegistryObject<Item> BASIC_ENTITY_GENERATOR_UPGRADE = R.item("basic_entity_generator_upgrade", ()-> new GeneratorUpgradeItem(GeneratorUpgradeItem.Type.ENTITY, 1));
        public static final RegistryObject<Item> IMPROVED_SPEED_GENERATOR_UPGRADE = R.item("improved_speed_generator_upgrade", ()-> new GeneratorUpgradeItem(GeneratorUpgradeItem.Type.SPEED, 1.5f));
        public static final RegistryObject<Item> IMPROVED_FORTUNE_GENERATOR_UPGRADE = R.item("improved_fortune_generator_upgrade", ()-> new GeneratorUpgradeItem(GeneratorUpgradeItem.Type.FORTUNE, 3f));
        public static final RegistryObject<Item> IMPROVED_ENTITY_GENERATOR_UPGRADE = R.item("improved_entity_generator_upgrade", ()-> new GeneratorUpgradeItem(GeneratorUpgradeItem.Type.ENTITY, 2));
        public static final RegistryObject<Item> PERFECT_SPEED_GENERATOR_UPGRADE = R.item("perfect_speed_generator_upgrade", ()-> new GeneratorUpgradeItem(GeneratorUpgradeItem.Type.SPEED, 2f));
        public static final RegistryObject<Item> PERFECT_FORTUNE_GENERATOR_UPGRADE = R.item("perfect_fortune_generator_upgrade", ()-> new GeneratorUpgradeItem(GeneratorUpgradeItem.Type.FORTUNE, 4f));
        public static final RegistryObject<Item> PERFECT_ENTITY_GENERATOR_UPGRADE = R.item("perfect_entity_generator_upgrade", ()-> new GeneratorUpgradeItem(GeneratorUpgradeItem.Type.ENTITY, 3));
        
        public static void init(){}
    }
    
    public static final RegistryObject<CreativeModeTab> TAB = R.creative_mode_tab("tab", ()-> CreativeModeTab.builder()
        .title(Component.literal("BlatBlock"))
        .icon(()-> new ItemStack(ITEMS.DIAMOND_MULTITOOL.get()))
        .displayItems((pParameters, out) -> {
            out.accept(ITEMS.ENTITY_RING.get());
            out.accept(ITEMS.TAG_RING.get());
            out.accept(ITEMS.DROP_RING.get());
            out.accept(ITEMS.LIQUID_RING.get());
            
            out.accept(ITEMS.WOODEN_MULTITOOL.get());
            out.accept(ITEMS.STONE_MULTITOOL.get());
            out.accept(ITEMS.IRON_MULTITOOL.get());
            out.accept(ITEMS.GOLDEN_MULTITOOL.get());
            out.accept(ITEMS.DIAMOND_MULTITOOL.get());
            out.accept(ITEMS.NETHERITE_MULTITOOL.get());
            
            out.accept(BLOCKS.BASIC_AUTO_GENERATOR.get());
            out.accept(BLOCKS.IMPROVED_AUTO_GENERATOR.get());
            out.accept(BLOCKS.PERFECT_AUTO_GENERATOR.get());
            
            out.accept(ITEMS.BASIC_SPEED_GENERATOR_UPGRADE.get());
            out.accept(ITEMS.BASIC_FORTUNE_GENERATOR_UPGRADE.get());
            out.accept(ITEMS.BASIC_TAG_GENERATOR_UPGRADE.get());
            out.accept(ITEMS.BASIC_ENTITY_GENERATOR_UPGRADE.get());
            out.accept(ITEMS.IMPROVED_SPEED_GENERATOR_UPGRADE.get());
            out.accept(ITEMS.IMPROVED_FORTUNE_GENERATOR_UPGRADE.get());
            out.accept(ITEMS.IMPROVED_ENTITY_GENERATOR_UPGRADE.get());
            out.accept(ITEMS.PERFECT_SPEED_GENERATOR_UPGRADE.get());
            out.accept(ITEMS.PERFECT_FORTUNE_GENERATOR_UPGRADE.get());
            out.accept(ITEMS.PERFECT_ENTITY_GENERATOR_UPGRADE.get());
        })
        .build());
    
    public static final RegistryObject<MenuType<AutoGeneratorMenu>> AUTO_GENERATOR_MENU = R.menu_type("auto_generator", ()-> IForgeMenuType
        .create((windowId, inv, data) -> new AutoGeneratorMenu(windowId, data.readBlockPos(), inv, data.readEnum(AutoGeneratorBlock.Type.class))));
    
    public static void init(IEventBus bus){
        ITEMS.init();
        BLOCKS.init();
        BLOCK_ENTITIES.init();
        R.register(bus);
    }
}