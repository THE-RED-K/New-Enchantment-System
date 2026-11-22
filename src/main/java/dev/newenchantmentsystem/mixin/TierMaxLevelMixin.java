package dev.newenchantmentsystem.mixin;

import dev.newenchantmentsystem.api.Config;
import dev.newenchantmentsystem.api.EnchantmentTierRegister;
import dev.newenchantmentsystem.enchantment.CriticalBladeEnchantment;
import dev.newenchantmentsystem.enchantment.LifestealEnchantment;
import net.minecraft.world.item.enchantment.ArrowFireEnchantment;
import net.minecraft.world.item.enchantment.ArrowInfiniteEnchantment;
import net.minecraft.world.item.enchantment.BindingCurseEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.MendingEnchantment;
import net.minecraft.world.item.enchantment.MultiShotEnchantment;
import net.minecraft.world.item.enchantment.TridentChannelingEnchantment;
import net.minecraft.world.item.enchantment.UntouchingEnchantment;
import net.minecraft.world.item.enchantment.VanishingCurseEnchantment;
import net.minecraft.world.item.enchantment.WaterWorkerEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 低档（lowMaxLevel）附魔最大等级。
 * 这些附魔原版 maxLevel 均为 1，未覆写 getMaxLevel()，调用会派发到基类
 * {@link Enchantment#getMaxLevel()}（固定返回 1），因此在此统一拦截：
 * 消失诅咒、精准采集、绑定诅咒、水下速掘、无限、火矢、引雷、多重射击、暴击之刃、经验修补、
 * 生命窃取。
 */
@Mixin(Enchantment.class)
public abstract class TierMaxLevelMixin {

    @Inject(method = "getMaxLevel", at = @At("HEAD"), cancellable = true)
    private void nes$lowTierMaxLevel(CallbackInfoReturnable<Integer> cir) {
        if (!Config.isLoaded()) {
            return;
        }
        // 自定义档位（EnchantmentTierRegister.registerCustomTier）优先于任何档位配置
        Enchantment enchantment = (Enchantment) (Object) this;
        if (EnchantmentTierRegister.hasCustomMaxLevel(enchantment)) {
            cir.setReturnValue(EnchantmentTierRegister.getCustomMaxLevel(enchantment));
            return;
        }
        if (enchantment instanceof VanishingCurseEnchantment
                || enchantment instanceof UntouchingEnchantment
                || enchantment instanceof BindingCurseEnchantment
                || enchantment instanceof WaterWorkerEnchantment
                || enchantment instanceof ArrowInfiniteEnchantment
                || enchantment instanceof ArrowFireEnchantment
                || enchantment instanceof TridentChannelingEnchantment
                || enchantment instanceof MultiShotEnchantment
                || enchantment instanceof CriticalBladeEnchantment
                || enchantment instanceof LifestealEnchantment
                || enchantment instanceof MendingEnchantment) {
            cir.setReturnValue(Config.LOW_MAX_LEVEL.get());
        }
    }
}
