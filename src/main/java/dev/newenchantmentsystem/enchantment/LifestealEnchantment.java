package dev.newenchantmentsystem.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * 生命窃取（Lifesteal）：近战攻击造成伤害时回复生命。
 * <p>
 * <ul>
 *   <li><b>吸血公式</b>：回复量 = 5% × 附魔等级 × 实际造成伤害
 *       （{@code LivingDamageEvent.getAmount()}，护甲减免后的最终伤害），
 *       由 {@code LifestealHandler} 实现。</li>
 *   <li><b>横扫限制</b>：仅对攻击主目标生效，横扫副目标不吸血——模组重做了横扫机制
 *       （横扫对所有剑生效、比例可 &gt;100%），故不能用伤害大小判断主/副目标，
 *       而是通过 {@code SweepTargetTracker} 的瞬时标签（横扫 {@code hurt} 结算期间标记）
 *       精确排除副目标。</li>
 *   <li><b>适用物品</b>：{@link EnchantmentCategory#WEAPON}（剑、斧），主手。</li>
 *   <li><b>档位</b>：低档（{@code EnchantmentTier.LOW}）——不覆写 {@code getMaxLevel}，
 *       最大等级由 {@code TierMaxLevelMixin} 按 {@code Config.LOW_MAX_LEVEL}（默认 1）
 *       控制，可配置多级；铁砧成本按低档权重（{@code Config.ANVIL_COST_LOW_TIER}，
 *       默认 4）计算。</li>
 *   <li>稀有度 RARE：附魔台权重 2，与横扫之刃一致；非宝藏、非诅咒。</li>
 * </ul>
 */
public class LifestealEnchantment extends Enchantment {

    public LifestealEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }
}
