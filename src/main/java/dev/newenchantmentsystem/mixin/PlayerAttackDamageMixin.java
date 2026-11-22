package dev.newenchantmentsystem.mixin;

import dev.newenchantmentsystem.api.Config;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 近战伤害类附魔重做（Player.attack）：
 * 原版锋利/亡灵杀手/节肢杀手/穿刺均为固定附加伤害（EnchantmentHelper.getDamageBonus 的 flat 值），
 * 现改为按最终伤害乘算：
 * <ul>
 *   <li>锋利（Sharpness）：+10% × 等级</li>
 *   <li>亡灵杀手（Smite）：+20% × 等级（对亡灵）</li>
 *   <li>节肢杀手（Bane of Arthropods）：+30% × 等级（对节肢动物）</li>
 *   <li>穿刺（Impaling）：+20% × 等级（对水生生物）</li>
 * </ul>
 * 实现：禁用原版 flat 附加伤害（getDamageBonus 返回 0），
 * 在最终伤害结算处（Entity.hurt）按目标类型乘算倍率。
 * 三叉戟近战同样经过 Player.attack，穿刺加成一并生效。
 */
@Mixin(Player.class)
public abstract class PlayerAttackDamageMixin {

    /**
     * 禁用原版固定附加伤害：Player.attack 中 getDamageBonus 的返回值改为 0，
     * 伤害加成统一由 {@link #nes$attackDamageMultiplier} 乘算。
     */
    @Redirect(
            method = "attack",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getDamageBonus(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/MobType;)F")
    )
    private float nes$noFlatDamageBonus(ItemStack pStack, MobType pMobType) {
        return 0.0F;
    }

    /**
     * 最终伤害乘算：按主手武器附魔等级与目标类型计算倍率。
     */
    @Redirect(
            method = "attack",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z")
    )
    private boolean nes$attackDamageMultiplier(Entity pTarget, DamageSource pSource, float pAmount) {
        ItemStack stack = ((Player) (Object) this).getMainHandItem();
        float multiplier = 1.0F + Config.SHARPNESS_DAMAGE_MULTIPLIER.get().floatValue()
                * stack.getEnchantmentLevel(Enchantments.SHARPNESS);
        if (pTarget instanceof LivingEntity livingEntity) {
            MobType mobType = livingEntity.getMobType();
            if (mobType == MobType.UNDEAD) {
                multiplier *= 1.0F + Config.SMITE_DAMAGE_MULTIPLIER.get().floatValue()
                        * stack.getEnchantmentLevel(Enchantments.SMITE);
            } else if (mobType == MobType.ARTHROPOD) {
                multiplier *= 1.0F + Config.BANE_OF_ARTHROPODS_DAMAGE_MULTIPLIER.get().floatValue()
                        * stack.getEnchantmentLevel(Enchantments.BANE_OF_ARTHROPODS);
            } else if (mobType == MobType.WATER) {
                multiplier *= 1.0F + Config.IMPALING_DAMAGE_MULTIPLIER.get().floatValue()
                        * stack.getEnchantmentLevel(Enchantments.IMPALING);
            }
        }
        return pTarget.hurt(pSource, pAmount * multiplier);
    }
}
