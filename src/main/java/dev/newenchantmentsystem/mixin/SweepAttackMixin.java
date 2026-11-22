package dev.newenchantmentsystem.mixin;

import dev.newenchantmentsystem.api.Config;
import dev.newenchantmentsystem.api.SweepTargetTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * 横扫攻击伤害重做。
 * Forge 版原公式（Player.attack）：横扫伤害 f3 = 1.0F + getSweepingDamageRatio(等级) * 主伤害，
 * 其中 getSweepingDamageRatio(等级) = 1 - 1/(等级+1)（1/2/3 级 ≈ 50%/67%/75%），
 * 无附魔时为 0，横扫仅造成 1 点固定伤害。
 * 本模组：横扫伤害 = 横扫比例 × 主伤害，横扫比例 = 35%（基础）+ 10% × 横扫之刃等级。
 * 等级上限由 TierOverrideMaxLevelMixin（mediumMaxLevel）控制。
 */
@Mixin(Player.class)
public abstract class SweepAttackMixin {

    /** 去掉原公式中固定的 +1.0F，使横扫伤害 = 横扫比例 × 主伤害。 */
    @ModifyConstant(
            method = "attack",
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getSweepingDamageRatio(Lnet/minecraft/world/entity/LivingEntity;)F")),
            constant = @Constant(floatValue = 1.0F)
    )
    private float nes$dropSweepBaseOne(float pConstant) {
        return 0.0F;
    }

    /** 横扫比例 = 35% + 10% × 横扫之刃等级（无附魔时 = 35%）。 */
    @Redirect(
            method = "attack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getSweepingDamageRatio(Lnet/minecraft/world/entity/LivingEntity;)F")
    )
    private float nes$sweepRatio(LivingEntity pEntity) {
        float base = Config.SWEEPING_BASE_RATIO.get().floatValue();
        float perLevel = Config.SWEEPING_EDGE_PER_LEVEL.get().floatValue();
        return base + perLevel * EnchantmentHelper.getEnchantmentLevel(Enchantments.SWEEPING_EDGE, pEntity);
    }

    /**
     * 横扫副目标标记（"主目标/副目标"标签机制的核心注入点）。
     * 主目标伤害调用的是 {@code Entity.hurt}（{@code pTarget} 静态类型为 Entity），
     * 横扫副目标调用的是 {@code LivingEntity.hurt}（{@code livingentity} 静态类型为
     * LivingEntity，且 LivingEntity 覆写了 hurt）——二者是不同方法引用，
     * 故可在此精确拦截"仅横扫副目标"的伤害结算：
     * 结算前打上瞬时标签（{@link SweepTargetTracker#markSweepTarget}），
     * 结算后（{@code finally}）清除，使生命窃取等"仅主目标生效"的效果可精确排除副目标
     * （横扫比例可 &gt;100%，不能用伤害大小判断）。
     */
    @Redirect(
            method = "attack",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z")
    )
    private boolean nes$markSweepTarget(LivingEntity pTarget, DamageSource pSource, float pAmount) {
        SweepTargetTracker.markSweepTarget(pTarget);
        try {
            return pTarget.hurt(pSource, pAmount);
        } finally {
            SweepTargetTracker.unmarkSweepTarget(pTarget);
        }
    }
}
