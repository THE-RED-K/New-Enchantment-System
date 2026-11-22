package dev.newenchantmentsystem.mixin;

import dev.newenchantmentsystem.api.Config;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 击退附魔（Knockback）击退距离公式：击退 = 2 + 附魔等级。
 * 原版 {@code EnchantmentHelper.getKnockbackBonus} 返回击退附魔等级，
 * {@code Player.attack} 将其累加到击退强度 i（默认 0）上，推击强度为 i * 0.5。
 * 这里返回 level + 2，使击退附魔贡献的距离变为 2 + 等级。
 */
@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {

    @Inject(method = "getKnockbackBonus", at = @At("HEAD"), cancellable = true)
    private static void nes$knockbackBonus(LivingEntity pPlayer, CallbackInfoReturnable<Integer> cir) {
        if (!Config.isLoaded()) {
            return;
        }
        int level = EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, pPlayer);
        cir.setReturnValue(level + Config.KNOCKBACK_BONUS.get());
    }
}
