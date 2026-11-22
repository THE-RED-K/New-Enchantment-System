package dev.newenchantmentsystem.mixin;

import dev.newenchantmentsystem.api.Config;
import dev.newenchantmentsystem.api.EnchantmentTierRegister;
import net.minecraft.world.item.enchantment.ArrowDamageEnchantment;
import net.minecraft.world.item.enchantment.ArrowKnockbackEnchantment;
import net.minecraft.world.item.enchantment.ArrowPiercingEnchantment;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.FireAspectEnchantment;
import net.minecraft.world.item.enchantment.FishingSpeedEnchantment;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.item.enchantment.KnockbackEnchantment;
import net.minecraft.world.item.enchantment.LootBonusEnchantment;
import net.minecraft.world.item.enchantment.OxygenEnchantment;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.item.enchantment.QuickChargeEnchantment;
import net.minecraft.world.item.enchantment.SoulSpeedEnchantment;
import net.minecraft.world.item.enchantment.SweepingEdgeEnchantment;
import net.minecraft.world.item.enchantment.SwiftSneakEnchantment;
import net.minecraft.world.item.enchantment.ThornsEnchantment;
import net.minecraft.world.item.enchantment.TridentImpalerEnchantment;
import net.minecraft.world.item.enchantment.TridentLoyaltyEnchantment;
import net.minecraft.world.item.enchantment.TridentRiptideEnchantment;
import net.minecraft.world.item.enchantment.WaterWalkerEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 中档/高档附魔最大等级。这些附魔类均覆写了 getMaxLevel()，运行时派发到各自方法，
 * 因此把全部目标类列为多目标 Mixin，统一拦截：
 * <ul>
 *   <li>中档（mediumMaxLevel）：时运、抢夺、饵钓、冰霜行者、灵魂疾行、迅捷潜行、
 *       水下呼吸、深海探索者、忠诚、击退、激流、冲击、穿透、快速装填、火焰附加、
 *       横扫之刃、荆棘</li>
 *   <li>高档（highMaxLevel）：海之眷顾、力量、锋利、亡灵杀手、节肢杀手、穿刺、
 *       保护、摔落缓冲、弹射物保护、爆炸保护、耐久</li>
 * </ul>
 * 注意：时运 / 抢夺 / 海之眷顾共用 {@link LootBonusEnchantment}，按原附魔名称区分：
 * 海之眷顾（{@link Enchantments#FISHING_LUCK}）= 高档；
 * 时运（{@link Enchantments#BLOCK_FORTUNE}）、抢夺（{@link Enchantments#MOB_LOOTING}）= 中档。
 * 锋利 / 亡灵杀手 / 节肢杀手共用 {@link DamageEnchantment}（type 0/1/2），
 * 力量 = {@link ArrowDamageEnchantment}、穿刺 = {@link TridentImpalerEnchantment}，均为高档。
 * 保护 / 摔落缓冲 / 弹射物保护 / 爆炸保护共用 {@link ProtectionEnchantment}（type 区分），
 * 均为高档；魔法保护（{@code MagicProtectionEnchantment}）在本类中处理会因多目标
 * descriptor 无法解析（本模组类未覆写 getMaxLevel），故在其自身覆写读取高档配置。
 */
@Mixin({
        LootBonusEnchantment.class,
        FishingSpeedEnchantment.class,
        FrostWalkerEnchantment.class,
        SoulSpeedEnchantment.class,
        SwiftSneakEnchantment.class,
        OxygenEnchantment.class,
        WaterWalkerEnchantment.class,
        TridentLoyaltyEnchantment.class,
        KnockbackEnchantment.class,
        TridentRiptideEnchantment.class,
        ArrowKnockbackEnchantment.class,
        ArrowPiercingEnchantment.class,
        QuickChargeEnchantment.class,
        FireAspectEnchantment.class,
        SweepingEdgeEnchantment.class,
        DamageEnchantment.class,
        ArrowDamageEnchantment.class,
        TridentImpalerEnchantment.class,
        ProtectionEnchantment.class,
        DigDurabilityEnchantment.class,
        ThornsEnchantment.class
})
public abstract class TierOverrideMaxLevelMixin {

    @Inject(method = "getMaxLevel", at = @At("HEAD"), cancellable = true)
    private void nes$tierMaxLevel(CallbackInfoReturnable<Integer> cir) {
        if (!Config.isLoaded()) {
            return;
        }
        // 自定义档位（EnchantmentTierRegister.registerCustomTier）优先于中/高档配置
        Enchantment enchantment = (Enchantment) (Object) this;
        if (EnchantmentTierRegister.hasCustomMaxLevel(enchantment)) {
            cir.setReturnValue(EnchantmentTierRegister.getCustomMaxLevel(enchantment));
            return;
        }
        if (enchantment instanceof LootBonusEnchantment) {
            if (enchantment == Enchantments.FISHING_LUCK) {
                // 海之眷顾（Luck of the Sea）——高档
                cir.setReturnValue(Config.HIGH_MAX_LEVEL.get());
            } else if (enchantment == Enchantments.BLOCK_FORTUNE || enchantment == Enchantments.MOB_LOOTING) {
                // 时运（Fortune）/ 抢夺（Looting）——中档
                cir.setReturnValue(Config.MEDIUM_MAX_LEVEL.get());
            } else {
                // 未列出的新附魔，默认中档兜底
                cir.setReturnValue(Config.MEDIUM_MAX_LEVEL.get());
            }
            return;
        }
        if (enchantment instanceof DamageEnchantment
                || enchantment instanceof ArrowDamageEnchantment
                || enchantment instanceof TridentImpalerEnchantment) {
            // 力量 / 锋利 / 亡灵杀手 / 节肢杀手 / 穿刺（高档）
            cir.setReturnValue(Config.HIGH_MAX_LEVEL.get());
            return;
        }
        if (enchantment instanceof ProtectionEnchantment) {
            // 保护 / 摔落缓冲 / 弹射物保护 / 爆炸保护（高档）
            cir.setReturnValue(Config.HIGH_MAX_LEVEL.get());
            return;
        }
        if (enchantment instanceof DigDurabilityEnchantment) {
            // 耐久（Unbreaking）——高档
            cir.setReturnValue(Config.HIGH_MAX_LEVEL.get());
            return;
        }
        if (enchantment instanceof ThornsEnchantment) {
            // 荆棘（Thorns）——中档
            cir.setReturnValue(Config.MEDIUM_MAX_LEVEL.get());
            return;
        }
        cir.setReturnValue(Config.MEDIUM_MAX_LEVEL.get());
    }
}
