package dev.newenchantmentsystem.api;

import dev.newenchantmentsystem.mixin.SweepAttackMixin;
import net.minecraft.world.entity.LivingEntity;

import java.util.IdentityHashMap;

/**
 * 横扫目标瞬时标记器（"主目标 / 副目标"标签机制的实现）。
 * <p>
 * 模组重做了横扫机制（{@code SweepAttackMixin}）：横扫对所有剑生效（基础 35% +
 * 10% × 横扫之刃等级），且横扫比例可能超过 100%——因此任何"仅对攻击主目标生效"
 * 的效果都不能用"伤害大小"区分主/副目标。
 * <p>
 * 本类通过 {@link SweepAttackMixin} 的 {@code @Redirect} 在横扫副目标
 * {@code hurt} 结算期间打上瞬时标签（{@link #markSweepTarget}），结算结束
 * （{@code finally}）即清除（{@link #unmarkSweepTarget}）。服务端主线程内
 * {@code Player.attack} 同步执行，标签生命周期严格限制在单次横扫循环内；
 * {@link IdentityHashMap} 保证同一实体实例语义（不同实例即使 equals 相同也不混淆）。
 * <p>
 * 现有使用者：生命窃取（{@link LifestealHandler}）——横扫副目标不吸血，
 * 仅主目标吸血。
 */
public final class SweepTargetTracker {

    /** 当前正在结算横扫伤害的实体集合（瞬时，结算后即清空）。 */
    private static final IdentityHashMap<LivingEntity, Boolean> SWEEP_TARGETS = new IdentityHashMap<>();

    private SweepTargetTracker() {
    }

    /** 标记某实体为"正在结算横扫伤害"。 */
    public static void markSweepTarget(LivingEntity entity) {
        SWEEP_TARGETS.put(entity, Boolean.TRUE);
    }

    /** 结算结束，移除横扫标记。 */
    public static void unmarkSweepTarget(LivingEntity entity) {
        SWEEP_TARGETS.remove(entity);
    }

    /** 该实体当前是否正处于横扫伤害结算中（即副目标）。 */
    public static boolean isSweepTarget(LivingEntity entity) {
        return SWEEP_TARGETS.containsKey(entity);
    }
}
