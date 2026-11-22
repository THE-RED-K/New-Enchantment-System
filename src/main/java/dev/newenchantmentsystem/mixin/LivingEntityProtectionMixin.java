package dev.newenchantmentsystem.mixin;

import dev.newenchantmentsystem.api.Config;
import dev.newenchantmentsystem.init.ModEnchantments;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 百分比保护系统：完全弃用原版附魔点数（EPF）系统。
 * <p>
 * 原版 {@code LivingEntity.getDamageAfterMagicAbsorb} 用护甲上所有保护附魔的点数
 * （{@link EnchantmentHelper#getDamageProtection}）调 {@code CombatRules.getDamageAfterMagicAbsorb}
 * 计算减伤（点数 clamp 0~20，每点 4%，上限 80%）。本 Mixin 通过两个 @Redirect 接管：
 * <ol>
 *   <li>强制 {@code getDamageProtection} 返回 1——点数被弃用，仅保证穿过 {@code if (k > 0)}
 *       进入结算调用点（否则仅穿魔法保护等非点数附魔时该分支不会执行）；</li>
 *   <li>重定向 {@code CombatRules.getDamageAfterMagicAbsorb}，改为按附魔等级百分比乘算。</li>
 * </ol>
 * 公式（对某次伤害）：
 * <pre>
 *   total = 保护等级总和 × 1.5%                        // 所有伤害
 *         + 魔法保护等级总和 × 3%（若 witch_resistant_to 或 is_fire）// 元素归入魔法大类
 *         + 弹射物保护等级总和 × 3%（若 is_projectile）
 *         + 爆炸保护等级总和 × 3%（若 is_explosion）
 *   伤害 ×= max(0, 1 − total)                          // 加算，可超过 100% → 免疫（刻意设计）
 *   伤害 ×= max(0, 1 − 摔落缓冲等级总和 × 3%)（若 is_fall）// 摔落缓冲：先加算所有保护，再独立乘算
 * </pre>
 * 与 New-Armor-System 无冲突：该模组仅 @Overwrite 护甲减伤
 * {@code CombatRules.getDamageAfterAbsorb}，本 Mixin 不触碰该方法与类。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityProtectionMixin {

    @Redirect(method = "getDamageAfterMagicAbsorb",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getDamageProtection(Ljava/lang/Iterable;Lnet/minecraft/world/damagesource/DamageSource;)I"))
    private int nes$forceEnterProtection(Iterable<ItemStack> pStacks, DamageSource pSource) {
        // 原版点数已弃用；恒返回 1（>0），使结算调用点必然执行，由 nes$percentageProtection 接管。
        return 1;
    }

    @Redirect(method = "getDamageAfterMagicAbsorb",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterMagicAbsorb(FF)F"))
    private float nes$percentageProtection(float pDamage, float pEnchantModifiers,
                                           DamageSource pDamageSource, float pDamageAmount) {
        // 等价原版：不可被附魔减免的伤害（虚空、/kill、声波除外——声波已在 BYPASSES_ENCHANTMENTS 分支返回）
        if (pDamageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return pDamage;
        }
        LivingEntity self = (LivingEntity) (Object) this;

        boolean magicDamage = pDamageSource.is(DamageTypeTags.WITCH_RESISTANT_TO)
                || pDamageSource.is(DamageTypeTags.IS_FIRE); // 元素伤害归入魔法大类
        boolean projectileDamage = pDamageSource.is(DamageTypeTags.IS_PROJECTILE);
        boolean explosionDamage = pDamageSource.is(DamageTypeTags.IS_EXPLOSION);

        // 第一步：加算所有适用的保护百分比（可超过 100% → 免疫）。
        // 每级数值由配置控制：保护 protectionPerLevel，其余 specialProtectionPerLevel。
        float protectionPerLevel = Config.PROTECTION_PER_LEVEL.get().floatValue();
        float specialPerLevel = Config.SPECIAL_PROTECTION_PER_LEVEL.get().floatValue();
        float total = 0.0F;
        for (ItemStack stack : self.getArmorSlots()) {
            total += stack.getEnchantmentLevel(Enchantments.ALL_DAMAGE_PROTECTION) * protectionPerLevel;
            if (magicDamage) {
                total += stack.getEnchantmentLevel(ModEnchantments.MAGIC_PROTECTION.get()) * specialPerLevel;
            }
            if (projectileDamage) {
                total += stack.getEnchantmentLevel(Enchantments.PROJECTILE_PROTECTION) * specialPerLevel;
            }
            if (explosionDamage) {
                total += stack.getEnchantmentLevel(Enchantments.BLAST_PROTECTION) * specialPerLevel;
            }
        }
        float result = pDamage * Math.max(0.0F, 1.0F - total);

        // 第二步：摔落缓冲独立于加算，单独乘算（摔落缓冲对 is_fall 生效）
        if (pDamageSource.is(DamageTypeTags.IS_FALL)) {
            float fallReduction = 0.0F;
            for (ItemStack stack : self.getArmorSlots()) {
                fallReduction += stack.getEnchantmentLevel(Enchantments.FALL_PROTECTION) * specialPerLevel;
            }
            result *= Math.max(0.0F, 1.0F - fallReduction);
        }
        return result;
    }
}
