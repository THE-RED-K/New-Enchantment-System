package dev.newenchantmentsystem.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * 中毒（Poison）：近战攻击造成伤害后，为目标附加中毒效果。
 * <p>
 * <ul>
 *   <li><b>效果</b>：每次近战命中（含横扫副目标）为目标附加
 *       {@code 10s × 附魔等级} 的 <b>I 级</b> 中毒（{@link net.minecraft.world.effect.MobEffects#POISON}），
 *       由 {@code PoisonWitherHandler} 实现。</li>
 *   <li><b>适用物品</b>：{@link EnchantmentCategory#WEAPON}（剑、斧），主手。</li>
 *   <li><b>档位</b>：低档（{@code EnchantmentTier.LOW}）——不覆写 {@code getMaxLevel}，
 *       最大等级由 {@code TierMaxLevelMixin} 按 {@code Config.LOW_MAX_LEVEL}（默认 1）
 *       控制，可配置多级；铁砧成本按低档权重（{@code Config.ANVIL_COST_LOW_TIER}，
 *       默认 4）计算。</li>
 *   <li>稀有度 RARE（附魔台权重 2，名称青色）：攻击附加 DoT，对标原版火焰附加
 *       （{@link Rarity#RARE}）与火矢——同为命中附加持续伤害的效果；非宝藏、非诅咒。</li>
 * </ul>
 */
public class PoisonEnchantment extends Enchantment {

    public PoisonEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }
}
