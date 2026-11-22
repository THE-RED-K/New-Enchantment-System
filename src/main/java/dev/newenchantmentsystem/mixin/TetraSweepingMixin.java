package dev.newenchantmentsystem.mixin;

import dev.newenchantmentsystem.api.SweepTargetTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Tetra 兼容混入：Tetra 的实体横扫由 {@code se.mickelus.tetra.effect.SweepingEffect}
 * 自行调用 {@code LivingEntity.hurt}（不经 {@code Player.attack} 的横扫循环），
 * 故 {@link SweepAttackMixin} 的副目标瞬时标签不会覆盖 Tetra 的横扫副目标。
 * <p>
 * 本混入仅在 Tetra 模组（modid {@code tetra}）已装载时应用（由
 * {@link NewEnchantmentSystemMixinPlugin#shouldApplyMixin} 判定），Tetra 未装时整体跳过，
 * 不影响游戏启动。拦截 Tetra 横扫副目标的 {@code hurt} 结算，打上与
 * {@link SweepAttackMixin} 相同的瞬时标签（{@link SweepTargetTracker}），
 * 使生命窃取等"仅主目标生效"的效果对 Tetra 横扫同样只对主目标生效。
 * <p>
 * 注入点依据 Tetra 6.17.0（1.20.1）字节码实测确定，覆盖两处副目标伤害调用：
 * <ul>
 *   <li>{@code lambda$sweepAttack$4}：{@code sweepAttack} 的 forEach 消费 lambda，
 *       其非真横扫分支（{@code isTrueSweep == false}）直接调用 {@code target.hurt}；</li>
 *   <li>{@code causeTruesweepDamage}：{@code sweepAttack} 的真横扫分支与
 *       {@code truesweep}（空挥横扫）都汇聚到它，其中直接调用 {@code target.hurt}。</li>
 * </ul>
 * 注意：lambda 编号（{@code $4}）是编译器生成顺序号，未来 Tetra 版本可能变化，
 * 故两处均以 {@code require = 0} 声明——Tetra 结构变化时本混入静默跳过（最坏情况仅是
 * Tetra 横扫副目标恢复为可吸血，不会导致游戏崩溃）；Tetra 未装时由混入插件整体跳过。
 */
@Pseudo
@Mixin(targets = "se.mickelus.tetra.effect.SweepingEffect")
public abstract class TetraSweepingMixin {

    @Redirect(
            method = "lambda$sweepAttack$4",
            require = 0,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z")
    )
    private static boolean nes$markTetraSweepTarget(LivingEntity pTarget, DamageSource pSource, float pAmount) {
        SweepTargetTracker.markSweepTarget(pTarget);
        try {
            return pTarget.hurt(pSource, pAmount);
        } finally {
            SweepTargetTracker.unmarkSweepTarget(pTarget);
        }
    }

    @Redirect(
            method = "causeTruesweepDamage",
            require = 0,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z")
    )
    private static boolean nes$markTetraTrueSweepTarget(LivingEntity pTarget, DamageSource pSource, float pAmount) {
        SweepTargetTracker.markSweepTarget(pTarget);
        try {
            return pTarget.hurt(pSource, pAmount);
        } finally {
            SweepTargetTracker.unmarkSweepTarget(pTarget);
        }
    }
}
