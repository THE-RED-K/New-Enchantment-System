package dev.newenchantmentsystem.mixin;

import dev.newenchantmentsystem.api.Config;
import dev.newenchantmentsystem.api.EnchantmentTier;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 铁砧合并成本按档位重设计（原版稀有度权重 → 档位权重）。
 * <p>
 * 原版 {@code AnvilMenu.createResult} 中合并附魔成本为：
 * <pre>
 *   switch (enchantment1.getRarity()) {
 *     case COMMON: k3 = 1; ... case VERY_RARE: k3 = 8;
 *   }
 *   if (flag) { k3 = Math.max(1, k3 / 2); }   // 附魔书半价
 *   i += k3 * j2;                              // 总成本 += 权重 * 合并后等级
 * </pre>
 * 本 Mixin 将"稀有度 → 权重"的映射替换为"档位 → 权重"：
 * {@code cost += 档位权重 * 合并后等级}，权重由配置
 * {@code anvilCostHighTier / anvilCostMediumTier / anvilCostLowTier} 控制（默认 1 / 2 / 4）。
 * 附魔书半价与成本 ≥40"太昂贵"等原版逻辑保持不变。
 * <p>
 * 实现方式：{@code createResult} 中 {@code getRarity()} 是唯一调用点，且 k3 是方法内
 * 局部变量、无独立方法可注入，因此用 {@link Redirect} 替换该调用：把档位权重映射回
 * {@link Enchantment.Rarity} 枚举（1→COMMON、2→UNCOMMON、4→RARE、8→VERY_RARE），
 * 原版 switch 分支即可产出目标权重，无需触碰局部变量。未纳入档位体系的附魔
 * （{@link EnchantmentTier#resolve} 返回 null）回退原稀有度成本。
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuCostMixin {

    @Redirect(method = "createResult",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/Enchantment;getRarity()Lnet/minecraft/world/item/enchantment/Enchantment$Rarity;"))
    private Enchantment.Rarity nes$tierAnvilMergeCost(Enchantment enchantment) {
        // 配置未加载或开关关闭时按原版稀有度计费
        if (!Config.isLoaded() || !Config.ANVIL_TIER_COST.get()) {
            return enchantment.getRarity();
        }
        EnchantmentTier tier = EnchantmentTier.resolve(enchantment);
        if (tier == null) {
            return enchantment.getRarity();
        }
        int weight;
        switch (tier) {
            case HIGH:
                weight = Config.ANVIL_COST_HIGH_TIER.get();
                break;
            case MEDIUM:
                weight = Config.ANVIL_COST_MEDIUM_TIER.get();
                break;
            default:
                weight = Config.ANVIL_COST_LOW_TIER.get();
        }
        Enchantment.Rarity rarity = rarityForWeight(weight);
        return rarity != null ? rarity : enchantment.getRarity();
    }

    /**
     * 档位权重 → 稀有度枚举，复用原版 switch 的 COMMON=1 / UNCOMMON=2 / RARE=4 / VERY_RARE=8。
     */
    private static Enchantment.Rarity rarityForWeight(int weight) {
        switch (weight) {
            case 1:
                return Enchantment.Rarity.COMMON;
            case 2:
                return Enchantment.Rarity.UNCOMMON;
            case 4:
                return Enchantment.Rarity.RARE;
            case 8:
                return Enchantment.Rarity.VERY_RARE;
            default:
                return null;
        }
    }
}
