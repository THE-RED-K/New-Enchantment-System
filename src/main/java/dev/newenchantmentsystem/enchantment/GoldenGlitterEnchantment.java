package dev.newenchantmentsystem.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * 金光闪闪（Golden Glitter）：佩戴/手持本附魔物品的玩家被猪灵视为"金质"。
 * <p>
 * <ul>
 *   <li>最高等级：I（固定）。</li>
 *   <li>适用物品：{@link EnchantmentCategory#VANISHABLE}（1.20.1 无 ALL；该类别覆盖
 *       全部可附魔物品——工具、武器、盔甲、弓弩、三叉戟等，附魔台 / 铁砧在生存模式下
 *       即可获取，非宝藏附魔）。</li>
 *   <li>效果：猪灵对携带者保持中立（视为穿金）；但当携带者在猪灵视野范围内亮出
 *       真正的金质物品（{@code #minecraft:piglin_loved}）时，猪灵立即对其产生敌意。</li>
 *   <li>效果实现：
 *       <ul>
 *         <li>"视为金质"由 {@code PiglinAiMixin} 钩住 {@code PiglinAi.isWearingGold}
 *             判定实现（猪灵传感器不再把携带者列为可攻击目标）；</li>
 *         <li>"亮出真金即敌意"由 {@code GoldenGlitterHandler}（Forge 事件，非 Mixin）
 *             调用原版 public API {@code PiglinAi.angerNearbyPiglins} 实现。</li>
 *       </ul>
 *   </li>
 * </ul>
 * 已纳入档位体系低档（{@code EnchantmentTier.LOW}），铁砧成本按低档权重
 * （{@code Config.ANVIL_COST_LOW_TIER}，默认 4）计算；maxLevel 固定 I 级，
 * 与低档默认 maxLevel（1）一致。稀有度（RARE）保持不变。
 */
public class GoldenGlitterEnchantment extends Enchantment {

    public GoldenGlitterEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.VANISHABLE, EquipmentSlot.values());
    }

    @Override
    public int getMaxLevel() {
        // 固定 I 级；覆写后调用不派发到基类，不受档位 maxLevel 体系影响
        return 1;
    }
}
