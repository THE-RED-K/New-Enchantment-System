package dev.newenchantmentsystem.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * 火焰附加（Fire Aspect）着火持续时间重设。
 * 原版：造成伤害后 {@code setSecondsOnFire(等级 * 4)} 秒；本模组改为
 * {@code setSecondsOnFire(2.5 * 等级)} 秒。
 * 仅拦截 {@code Player.attack} 中第二处 {@code setSecondsOnFire} 调用
 * （ordinal=1，造成伤害后的持续燃烧；第一处 ordinal=0 是攻击瞬间先点燃 1 秒的
 * 视觉动画，保持原版不变）。
 * 等级上限由 TierOverrideMaxLevelMixin（mediumMaxLevel）控制。
 */
@Mixin(Player.class)
public abstract class FireAspectMixin {

    @ModifyArg(
            method = "attack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setSecondsOnFire(I)V", ordinal = 1),
            index = 0
    )
    private int nes$fireAspectDuration(int pSeconds) {
        // 原版参数 = 等级 * 4，故等级 = pSeconds / 4，新持续时间 = 2.5 * 等级（四舍五入）
        return Math.round(2.5F * (pSeconds / 4.0F));
    }
}
