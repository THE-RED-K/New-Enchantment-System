package dev.newenchantmentsystem.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * 凋零（Wither）：近战攻击造成伤害后，为目标附加凋零效果。
 * <p>
 * <ul>
 *   <li><b>效果</b>：每次近战命中（含横扫副目标）为目标附加
 *       {@code 10s × 附魔等级} 的 <b>I 级</b> 凋零（{@link net.minecraft.world.effect.MobEffects#WITHER}），
 *       由 {@code PoisonWitherHandler} 实现。</li>
 *   <li><b>适用物品</b>：{@link EnchantmentCategory#WEAPON}（剑、斧），主手。</li>
 *   <li><b>档位</b>：低档（{@code EnchantmentTier.LOW}）——不覆写 {@code getMaxLevel}，
 *       最大等级由 {@code TierMaxLevelMixin} 按 {@code Config.LOW_MAX_LEVEL}（默认 1）
 *       控制，可配置多级；铁砧成本按低档权重（{@code Config.ANVIL_COST_LOW_TIER}，
 *       默认 4）计算。</li>
 *   <li>稀有度 VERY_RARE（附魔台权重 1，名称深紫）：凋零无视护甲且困难模式伤害更高，
 *       强度高于同为攻击 DoT 的中毒（RARE，对标火焰附加），对标原版荆棘
 *       （{@link Rarity#VERY_RARE}）的强机制档；非宝藏、非诅咒。</li>
 * </ul>
 */
public class WitherEnchantment extends Enchantment {

    public WitherEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }
}
