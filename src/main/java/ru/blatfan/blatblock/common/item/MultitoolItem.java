package ru.blatfan.blatblock.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.ToolActions;

import java.util.Optional;

public class MultitoolItem extends DiggerItem {
    public static final TagKey<Block> MINEABLE_WITH_MULTITOOL = BlockTags.create(new ResourceLocation("mineable/multitool"));
    @Getter@Setter
    private float attackDamage, attackSpeed;
    
    public MultitoolItem(float attackDamage, float attackSpeed, Tier pTier, Properties pProperties) {
        super(0, 0, pTier, MINEABLE_WITH_MULTITOOL, pProperties);
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
    }
    
    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot pEquipmentSlot) {
        Multimap<Attribute, AttributeModifier> defaultModifiers;
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", attackDamage-1, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", attackSpeed - 4, AttributeModifier.Operation.ADDITION));
        defaultModifiers = builder.build();
        return pEquipmentSlot == EquipmentSlot.MAINHAND ? defaultModifiers : super.getDefaultAttributeModifiers(pEquipmentSlot);
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        Player player = context.getPlayer();
        if(player==null) return InteractionResult.PASS;
        Optional<BlockState> strippedState = getStripped(blockstate);
        if (strippedState.isPresent()) {
            level.playSound(player, blockpos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!level.isClientSide) {
                level.setBlock(blockpos, strippedState.get(), 11);
                context.getItemInHand().hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(context.getHand()));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        
        Optional<BlockState> scrapedState = getScraped(blockstate);
        if (scrapedState.isPresent()) {
            level.playSound(player, blockpos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.levelEvent(player, 3005, blockpos, 0);
            if (!level.isClientSide) {
                level.setBlock(blockpos, scrapedState.get(), 11);
                context.getItemInHand().hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(context.getHand()));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        
        Optional<BlockState> waxOffState = getWaxOff(blockstate);
        if (waxOffState.isPresent()) {
            level.playSound(player, blockpos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.levelEvent(player, 3004, blockpos, 0);
            if (!level.isClientSide) {
                level.setBlock(blockpos, waxOffState.get(), 11);
                context.getItemInHand().hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(context.getHand()));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        
        if (blockstate.hasProperty(BlockStateProperties.LIT) && blockstate.getValue(BlockStateProperties.LIT)) {
            if (blockstate.is(Blocks.CAMPFIRE) || blockstate.is(Blocks.SOUL_CAMPFIRE)) {
                if (!level.isClientSide) {
                    level.levelEvent(player, 1009, blockpos, 0);
                    CampfireBlock.dowse(player, level, blockpos, blockstate);
                    level.setBlock(blockpos, blockstate.setValue(BlockStateProperties.LIT, false), 11);
                    context.getItemInHand().hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(context.getHand()));
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        
        if (context.getClickedFace() != Direction.DOWN && level.isEmptyBlock(blockpos.above())) {
            BlockState tilledState = level.getBlockState(blockpos).getToolModifiedState(context, ToolActions.HOE_TILL, false);
            if (tilledState != null) {
                level.playSound(player, blockpos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                
                if (!level.isClientSide) {
                    level.setBlock(blockpos, tilledState, 11);
                    context.getItemInHand().hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(context.getHand()));
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        
        return InteractionResult.PASS;
    }
    
    private static Optional<BlockState> getStripped(BlockState state) {
        return Optional.ofNullable(AxeItem.STRIPPABLES.get(state.getBlock()))
            .map(block -> block.defaultBlockState().setValue(RotatedPillarBlock.AXIS,
                state.getValue(RotatedPillarBlock.AXIS)));
    }
    
    private static Optional<BlockState> getScraped(BlockState state) {
        return Optional.ofNullable(HoneycombItem.WAX_OFF_BY_BLOCK.get().get(state.getBlock()))
            .map(Block::defaultBlockState);
    }
    
    private static Optional<BlockState> getWaxOff(BlockState state) {
        return Optional.ofNullable(HoneycombItem.WAXABLES.get().get(state.getBlock()))
            .map(Block::defaultBlockState);
    }
    
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.category == EnchantmentCategory.DIGGER ||
            enchantment.category == EnchantmentCategory.BREAKABLE ||
            super.canApplyAtEnchantingTable(stack, enchantment);
    }
    
    @Override
    public int getEnchantmentValue() {
        return Math.max(this.getTier().getEnchantmentValue(), 10);
    }
    
    @Override
    public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
        return ToolActions.DEFAULT_PICKAXE_ACTIONS.contains(toolAction) ||
            ToolActions.DEFAULT_AXE_ACTIONS.contains(toolAction) ||
            ToolActions.DEFAULT_HOE_ACTIONS.contains(toolAction) ||
            ToolActions.SHOVEL_DIG == toolAction ||
            ToolActions.DEFAULT_SWORD_ACTIONS.contains(toolAction);
    }
    
    public static MultitoolItem wooden() {
        return new MultitoolItem(5, 1.2f, Tiers.WOOD,
            new Properties().stacksTo(1).defaultDurability(120));
    }
    
    public static MultitoolItem stone() {
        return new MultitoolItem(6, 1.2f, Tiers.STONE,
            new Properties().stacksTo(1).defaultDurability(260));
    }
    
    public static MultitoolItem iron() {
        return new MultitoolItem(7, 1.2f, Tiers.IRON,
            new Properties().stacksTo(1).defaultDurability(500));
    }
    
    public static MultitoolItem gold() {
        return new MultitoolItem(5, 1.6f, Tiers.GOLD,
            new Properties().stacksTo(1).defaultDurability(64));
    }
    
    public static MultitoolItem diamond() {
        return new MultitoolItem(8, 1.2f, Tiers.DIAMOND,
            new Properties().stacksTo(1).defaultDurability(3120));
    }
    
    public static MultitoolItem netherite() {
        return new MultitoolItem(9, 1.2f, Tiers.NETHERITE,
            new Properties().stacksTo(1).defaultDurability(4060).fireResistant());
    }
}