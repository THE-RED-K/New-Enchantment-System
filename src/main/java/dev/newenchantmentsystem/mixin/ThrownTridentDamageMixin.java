package dev.newenchantmentsystem.mixin;

import dev.newenchantmentsystem.api.Config;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 穿刺重做（投掷三叉戟）：穿刺 +20% × 等级 伤害（对水生生物）。
 * 原版穿刺在 ThrownTrident.onHitEntity 中附加固定伤害（getDamageBonus flat），
 * 这里禁用 flat，改为最终伤害乘算。与 {@link PlayerAttackDamageMixin}（近战）保持一致。
 */
@Mixin(ThrownTrident.class)
public abstract class ThrownTridentDamageMixin {

    @Shadow
    private ItemStack tridentItem;

    /**
     * 禁用原版固定附加伤害（ThrownTrident.onHitEntity 中的 getDamageBonus）。
     */
    @Redirect(
            method = "onHitEntity",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getDamageBonus(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/MobType;)F")
    )
    private float nes$noFlatDamageBonus(ItemStack pStack, MobType pMobType) {
        return 0.0F;
    }

    /**
     * 最终伤害乘算：目标为水生生物时，穿刺 +20% × 等级。
     */
    @Redirect(
            method = "onHitEntity",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z")
    )
    private boolean nes$impalingDamageMultiplier(Entity pTarget, DamageSource pSource, float pAmount) {
        float multiplier = 1.0F;
        if (pTarget instanceof LivingEntity livingEntity && livingEntity.getMobType() == MobType.WATER) {
            multiplier = 1.0F + Config.IMPALING_DAMAGE_MULTIPLIER.get().floatValue()
                    * this.tridentItem.getEnchantmentLevel(Enchantments.IMPALING);
        }
        return pTarget.hurt(pSource, pAmount * multiplier);
    }
}
