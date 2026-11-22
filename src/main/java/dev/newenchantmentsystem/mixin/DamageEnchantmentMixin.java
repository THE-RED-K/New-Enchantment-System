package dev.newenchantmentsystem.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 移除节肢杀手（Bane of Arthropods）的原版缓慢效果。
 * 节肢杀手 = DamageEnchantment type 2；其 doPostAttack 会给节肢目标施加
 * MOVEMENT_SLOWDOWN，现按需求直接取消该效果（伤害公式已改为 +30% × 等级 乘算，
 * 由 {@link PlayerAttackDamageMixin} 处理）。
 */
@Mixin(DamageEnchantment.class)
public abstract class DamageEnchantmentMixin {

    @Inject(method = "doPostAttack", at = @At("HEAD"), cancellable = true)
    private void nes$removeBaneSlowdown(LivingEntity pUser, Entity pTarget, int pLevel, CallbackInfo ci) {
        if (((DamageEnchantment) (Object) this).type == DamageEnchantment.ARTHROPODS) {
            ci.cancel();
        }
    }
}
