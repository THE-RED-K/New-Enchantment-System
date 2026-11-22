package dev.newenchantmentsystem.mixin;

import dev.newenchantmentsystem.api.Config;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/// Tetra 横扫基础伤害公式重写。
///
/// Tetra 6.17.0（1.20.1）在 `SweepingEffect.sweepAttack`（左键横扫，真/非真横扫共用）
/// 与 `SweepingEffect.truesweep`（右键真横扫）中都以
/// `f = Math.max(1.0D, atk * level * 0.125D)` 计算横扫基础伤害
/// （`atk` = `attacker.getAttributeValue(Attributes.ATTACK_DAMAGE)`，
/// `level` = `SweepingEffect.getSweepingLevel(stack)`，即 Tetra sweeping 效果
/// + 原版横扫之刃等级）。
///
/// 本模组的横扫之刃改动（[SweepAttackMixin]）把原版比例公式
/// `1 + getSweepingDamageRatio(level) × 主伤害` 改为
/// `(SWEEPING_BASE_RATIO + SWEEPING_EDGE_PER_LEVEL × 等级) × 主伤害`
/// （默认 35% + 10% × 等级）。为使 Tetra 武器上的横扫之刃附魔保持同一套数值语义，
/// 本混入把 Tetra 的基础伤害公式同步重写为
/// `Math.max(1.0D, atk × (SWEEPING_BASE_RATIO + SWEEPING_EDGE_PER_LEVEL × level))`，
/// 直接沿用 [Config#SWEEPING_BASE_RATIO] 与 [Config#SWEEPING_EDGE_PER_LEVEL]
/// 两个配置项（与 Tetra 6.17.0 原版横扫之刃公式保持一致）。
///
/// 注入点依据 Tetra 6.17.0 字节码实测确定：两个方法内的 `Math.max(DD)D` 调用
/// 各出现且仅出现一次（`sweepAttack` 偏移 32、`truesweep` 偏移 20），
/// 均为基础伤害公式，可直接整点重定向。
///
/// 两处 handler 的附加外层方法参数（Mixin 官方支持：重定向调用参数之后按顺序追加
/// 被注入方法的参数）：
///
///   - `sweepAttack(ItemStack, LivingEntity, LivingEntity, int)` 的
///     `level` 是方法参数，可直接精确使用；
///   - `truesweep(ItemStack, LivingEntity, boolean)` 的 `level`
///     是局部变量（`getSweepingLevel` 的结果），handler 无法捕获，
///     只能由旧公式结果反推：`level = b / (atk × 0.125)`。
///     当 `b > 1.0`（未被 `max` 钳制）时反推精确；当 `b == 1.0`
///     （钳制区，即 `atk × level × 0.125 ≤ 1`）时反推值为上界
///     `8 / atk`，新公式高估至多 `SWEEPING_EDGE_PER_LEVEL × 8`
///     （默认 0.1 × 8 = 0.8 点伤害），属可接受的近似。
///
/// 与 [TetraSweepingMixin] 相同的容错策略：两处均以 `require = 0`
/// 声明，Tetra 结构变化时本混入静默跳过；Tetra 未装时由混入插件整体跳过。
@Pseudo
@Mixin(targets = "se.mickelus.tetra.effect.SweepingEffect")
public abstract class TetraSweepFormulaMixin {

    /**
     * 左键横扫（sweepAttack）的基础伤害公式：
     * {@code max(1.0, atk × level × 0.125)} → {@code max(1.0, atk × (base + per × level))}。
     * {@code level} 为方法第 4 个参数，精确可用。
     */
    @Redirect(
            method = "sweepAttack",
            require = 0,
            at = @At(value = "INVOKE",
                    target = "Ljava/lang/Math;max(DD)D")
    )
    private static double nes$sweepAttackBase(double pConst, double pOld,
            ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker, int pLevel) {
        return nes$rewriteBase(pConst, pOld, pAttacker, pLevel);
    }

    /**
     * 右键真横扫（truesweep）的基础伤害公式，注入点与 {@code sweepAttack} 相同。
     * {@code level} 是方法内局部变量（{@code getSweepingLevel(stack)} 的结果），
     * 由旧公式结果 {@code pOld = max(1.0, atk × level × 0.125)} 反推（钳制区近似，见类注释）。
     */
    @Redirect(
            method = "truesweep",
            require = 0,
            at = @At(value = "INVOKE",
                    target = "Ljava/lang/Math;max(DD)D")
    )
    private static double nes$truesweepBase(double pConst, double pOld,
            ItemStack pStack, LivingEntity pAttacker, boolean pHasSound) {
        double atk = pAttacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float level = atk > 0.0D ? (float) (pOld / (atk * 0.125D)) : 0.0F;
        return nes$rewriteBase(pConst, pOld, pAttacker, level);
    }

    /**
     * 共享公式：{@code max(1.0, atk × (SWEEPING_BASE_RATIO + SWEEPING_EDGE_PER_LEVEL × level))}。
     * 配置未加载（mixin 早于配置读取）时退回旧公式，避免 {@link IllegalStateException}。
     */
    private static double nes$rewriteBase(double pConst, double pOld, LivingEntity pAttacker, float pLevel) {
        if (!Config.isLoaded()) {
            return Math.max(pConst, pOld);
        }
        double atk = pAttacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float base = Config.SWEEPING_BASE_RATIO.get().floatValue();
        float perLevel = Config.SWEEPING_EDGE_PER_LEVEL.get().floatValue();
        return Math.max(pConst, atk * (base + perLevel * pLevel));
    }
}
