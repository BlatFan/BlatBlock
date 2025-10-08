package ru.blatfan.blatblock.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccess {
    @Invoker("dropCustomDeathLoot")
    public void customDrop(DamageSource pDamageSource, int pLooting, boolean pHitByPlayer);
    @Invoker("dropFromLootTable")
    public void lootTableDrop(DamageSource pDamageSource, boolean pHitByPlayer);
}
