package dev.newenchantmentsystem.mixin;

import dev.newenchantmentsystem.api.Config;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 冲击附魔（Punch）击退距离公式：击退 = 2 + 附魔等级。
 * 原版 {@code AbstractArrow.setKnockback} 仅被 {@code setEnchantmentEffectsFromEntity}
 * 以冲击等级调用，命中时按 knockback * 0.6 推击。这里把存入值改为 level + 2，
 * 使冲击附魔贡献的距离变为 2 + 等级。
 */
@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {

    @Shadow
    private int knockback;

    @Inject(method = "setKnockback", at = @At("HEAD"), cancellable = true)
    private void nes$punchKnockback(int pKnockback, CallbackInfo ci) {
        if (!Config.isLoaded()) {
            return;
        }
        this.knockback = pKnockback + 2;
        ci.cancel();
    }
}
