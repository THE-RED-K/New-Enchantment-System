package dev.newenchantmentsystem.mixin;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 放宽保护类附魔的兼容性：允许不同保护类型共存。
 * <p>
 * 原版 {@link ProtectionEnchantment#checkCompatibility} 规定同类型互斥，且除摔落缓冲外
 * 不同保护类型（保护/火焰/爆炸/弹射物）也互斥——那是为点数（EPF）系统做的平衡。
 * 本模组已弃用点数、改为百分比加算（保护 + 弹射物 + 爆炸 + 魔法保护加算可超 100% 免疫），
 * 因此放开该限制：保护类附魔之间均可共存（相同附魔的重复仍由
 * {@link Enchantment#isCompatibleWith} 的自身判断阻止）。
 */
@Mixin(ProtectionEnchantment.class)
public abstract class ProtectionCompatibilityMixin {

    @Inject(method = "checkCompatibility", at = @At("HEAD"), cancellable = true)
    private void nes$allowAllProtections(Enchantment pOther, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
