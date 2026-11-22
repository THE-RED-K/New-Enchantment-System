package dev.newenchantmentsystem.mixin;

import dev.newenchantmentsystem.api.Config;
import net.minecraft.world.item.enchantment.DiggingEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 效率附魔（Efficiency）最大等级：由配置 efficiencyMaxLevelTier 决定
 * （low / medium / high，默认 high，即使用 highMaxLevel = 10）。
 * 原版固定为 5 级。
 */
@Mixin(DiggingEnchantment.class)
public class DiggingEnchantmentMixin {

    @Inject(method = "getMaxLevel", at = @At("HEAD"), cancellable = true)
    private void nes$efficiencyMaxLevel(CallbackInfoReturnable<Integer> cir) {
        if (!Config.isLoaded()) {
            return;
        }
        cir.setReturnValue(Config.getEfficiencyMaxLevel());
    }
}
