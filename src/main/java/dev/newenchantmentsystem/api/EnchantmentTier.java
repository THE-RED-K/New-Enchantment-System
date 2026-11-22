package dev.newenchantmentsystem.api;

import dev.newenchantmentsystem.enchantment.AutoSmeltingEnchantment;
import dev.newenchantmentsystem.enchantment.BulwarkEnchantment;
import dev.newenchantmentsystem.enchantment.CriticalBladeEnchantment;
import dev.newenchantmentsystem.enchantment.EndVeilEnchantment;
import dev.newenchantmentsystem.enchantment.EverlastingEnchantment;
import dev.newenchantmentsystem.enchantment.GoldenGlitterEnchantment;
import dev.newenchantmentsystem.enchantment.HighJumpEnchantment;
import dev.newenchantmentsystem.enchantment.LifestealEnchantment;
import dev.newenchantmentsystem.enchantment.MagicProtectionEnchantment;
import dev.newenchantmentsystem.enchantment.NightVisionEnchantment;
import dev.newenchantmentsystem.enchantment.PoisonEnchantment;
import dev.newenchantmentsystem.enchantment.StableFootingEnchantment;
import dev.newenchantmentsystem.enchantment.WitherEnchantment;
import dev.newenchantmentsystem.enchantment.WorkerExploitationEnchantment;
import net.minecraft.world.item.enchantment.ArrowDamageEnchantment;
import net.minecraft.world.item.enchantment.ArrowFireEnchantment;
import net.minecraft.world.item.enchantment.ArrowInfiniteEnchantment;
import net.minecraft.world.item.enchantment.ArrowKnockbackEnchantment;
import net.minecraft.world.item.enchantment.ArrowPiercingEnchantment;
import net.minecraft.world.item.enchantment.BindingCurseEnchantment;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import net.minecraft.world.item.enchantment.DiggingEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.FireAspectEnchantment;
import net.minecraft.world.item.enchantment.FishingSpeedEnchantment;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.item.enchantment.KnockbackEnchantment;
import net.minecraft.world.item.enchantment.LootBonusEnchantment;
import net.minecraft.world.item.enchantment.MendingEnchantment;
import net.minecraft.world.item.enchantment.MultiShotEnchantment;
import net.minecraft.world.item.enchantment.OxygenEnchantment;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.item.enchantment.QuickChargeEnchantment;
import net.minecraft.world.item.enchantment.SoulSpeedEnchantment;
import net.minecraft.world.item.enchantment.SweepingEdgeEnchantment;
import net.minecraft.world.item.enchantment.SwiftSneakEnchantment;
import net.minecraft.world.item.enchantment.ThornsEnchantment;
import net.minecraft.world.item.enchantment.TridentChannelingEnchantment;
import net.minecraft.world.item.enchantment.TridentImpalerEnchantment;
import net.minecraft.world.item.enchantment.TridentLoyaltyEnchantment;
import net.minecraft.world.item.enchantment.TridentRiptideEnchantment;
import net.minecraft.world.item.enchantment.UntouchingEnchantment;
import net.minecraft.world.item.enchantment.VanishingCurseEnchantment;
import net.minecraft.world.item.enchantment.WaterWalkerEnchantment;
import net.minecraft.world.item.enchantment.WaterWorkerEnchantment;

/**
 * 附魔档位（tier）判定工具，是 {@code TierMaxLevelMixin}（低档）与
 * {@code TierOverrideMaxLevelMixin}（中/高档）判定逻辑的统一出口。
 * 供铁砧成本（{@code AnvilMenuCostMixin}）等需要按档位取权重的模块复用，
 * 避免各 Mixin 各自硬编码一份档位列表。
 * <p>
 * 判定结果与两处 maxLevel 覆写严格一致：
 * <ul>
 *   <li>低档（{@link Config#LOW_MAX_LEVEL}，默认 1）：消失诅咒、精准采集、绑定诅咒、
 *       水下速掘、无限、火矢、引雷、多重射击、暴击之刃、经验修补、稳定立足、剥削、
 *       金光闪闪、自动冶炼、夜视、末地面纱、生命窃取、永恒、中毒、凋零、堡垒</li>
 *   <li>中档（{@link Config#MEDIUM_MAX_LEVEL}，默认 4）：时运、抢夺、饵钓、冰霜行者、
 *       灵魂疾行、迅捷潜行、水下呼吸、深海探索者、忠诚、击退、激流、冲击、穿透、
 *       快速装填、火焰附加、横扫之刃、荆棘</li>
 *   <li>高档（{@link Config#HIGH_MAX_LEVEL}，默认 10）：海之眷顾、力量、锋利、亡灵杀手、
 *       节肢杀手、穿刺、保护、摔落缓冲、弹射物保护、爆炸保护、魔法保护、耐久</li>
 *   <li>效率（Efficiency）档位由配置 {@link Config#EFFICIENCY_MAX_LEVEL_TIER} 单独决定</li>
 * </ul>
 * 耐久与荆棘的<b>机制</b>（耐久减免、反伤）已由 New-Armor-System 实现，本模组不干预；
 * 经验修补归入低档后，原版附魔已<b>全部</b>纳入三档体系。纳入档位仅影响铁砧成本等
 * 档位消费，不改动各附魔的 maxLevel 等机制。对第三方或新增未纳入的附魔返回
 * {@code null}，调用方应回退原版行为（如保持稀有度成本），与本模组"仅覆盖已分档附魔"
 * 的语义一致。
 */
public enum EnchantmentTier {

    LOW, MEDIUM, HIGH;

    /**
     * 解析附魔所属档位；未纳入档位体系的附魔返回 {@code null}。
     * 需在配置加载后调用（调用前先 {@link Config#isLoaded()} 判断）。
     * <p>
     * 优先级：{@link EnchantmentTierRegister} 注册表（外部扩展点）→ 内置 instanceof 链。
     */
    public static EnchantmentTier resolve(Enchantment enchantment) {
        // 注册表优先：第三方 / 本模组通过 EnchantmentTierRegister.registerTier 注册的档位直接命中
        EnchantmentTier registered = EnchantmentTierRegister.getTier(enchantment);
        if (registered != null) {
            return registered;
        }

        // 低档（与 TierMaxLevelMixin 列表一致，先于高档判断避免子类 instanceof 干扰）
        if (enchantment instanceof VanishingCurseEnchantment
                || enchantment instanceof UntouchingEnchantment
                || enchantment instanceof BindingCurseEnchantment
                || enchantment instanceof WaterWorkerEnchantment
                || enchantment instanceof ArrowInfiniteEnchantment
                || enchantment instanceof ArrowFireEnchantment
                || enchantment instanceof TridentChannelingEnchantment
                || enchantment instanceof MultiShotEnchantment
                || enchantment instanceof CriticalBladeEnchantment
                || enchantment instanceof MendingEnchantment
                || enchantment instanceof StableFootingEnchantment
                || enchantment instanceof WorkerExploitationEnchantment
                || enchantment instanceof GoldenGlitterEnchantment
                || enchantment instanceof AutoSmeltingEnchantment
                || enchantment instanceof NightVisionEnchantment
                || enchantment instanceof EndVeilEnchantment
                || enchantment instanceof LifestealEnchantment
                || enchantment instanceof EverlastingEnchantment
                || enchantment instanceof PoisonEnchantment
                || enchantment instanceof WitherEnchantment
                || enchantment instanceof BulwarkEnchantment) {
            return LOW;
        }

        // 耐久（DigDurabilityEnchantment，与效率 DiggingEnchantment 互不继承）：高档，
        // 仅影响成本等档位消费；机制（耐久减免）由 New-Armor-System 接管，不改动。
        if (enchantment instanceof DigDurabilityEnchantment) {
            return HIGH;
        }

        // 效率（DiggingEnchantment）：档位由 efficiencyMaxLevelTier 配置单独决定；
        // 配置未加载时回退默认档 high（与 Config.getEfficiencyMaxLevel 默认分支一致）
        if (enchantment instanceof DiggingEnchantment) {
            String tier = Config.isLoaded() ? Config.EFFICIENCY_MAX_LEVEL_TIER.get() : "high";
            if ("low".equals(tier)) {
                return LOW;
            }
            return "medium".equals(tier) ? MEDIUM : HIGH;
        }

        // 高档（与 TierOverrideMaxLevelMixin 高档分支一致）
        if (enchantment instanceof DamageEnchantment
                || enchantment instanceof ArrowDamageEnchantment
                || enchantment instanceof TridentImpalerEnchantment
                || enchantment instanceof ProtectionEnchantment
                || enchantment instanceof MagicProtectionEnchantment) {
            return HIGH;
        }

        // 海之眷顾（Luck of the Sea）= 高档；时运 / 抢夺 = 中档（与 TierOverrideMaxLevelMixin 一致）
        if (enchantment instanceof LootBonusEnchantment) {
            return enchantment == Enchantments.FISHING_LUCK ? HIGH : MEDIUM;
        }

        // 中档（与 TierOverrideMaxLevelMixin 中档列表一致）
        if (enchantment instanceof FishingSpeedEnchantment
                || enchantment instanceof FrostWalkerEnchantment
                || enchantment instanceof SoulSpeedEnchantment
                || enchantment instanceof SwiftSneakEnchantment
                || enchantment instanceof OxygenEnchantment
                || enchantment instanceof WaterWalkerEnchantment
                || enchantment instanceof TridentLoyaltyEnchantment
                || enchantment instanceof KnockbackEnchantment
                || enchantment instanceof TridentRiptideEnchantment
                || enchantment instanceof ArrowKnockbackEnchantment
                || enchantment instanceof ArrowPiercingEnchantment
                || enchantment instanceof QuickChargeEnchantment
                || enchantment instanceof FireAspectEnchantment
                || enchantment instanceof SweepingEdgeEnchantment
                || enchantment instanceof ThornsEnchantment
                || enchantment instanceof HighJumpEnchantment) {
            return MEDIUM;
        }

        // 未纳入档位体系的其余原版附魔：不干预
        return null;
    }
}
