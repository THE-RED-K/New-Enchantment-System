package dev.newenchantmentsystem.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * 稳定立足（Stable Footing）：悬空时不再有挖掘速度惩罚。
 * <p>
 * 设计来自神化模组（Apotheosis）。由于本模组的配套护甲系统与神化不兼容，
 * 故在本模组内独立实现该附魔；神化的其他附魔因过于超模不考虑加入。
 * <ul>
 *   <li>最高等级：I（固定）</li>
 *   <li>适用物品：靴子（{@link EnchantmentCategory#ARMOR_FEET}）</li>
 *   <li>效果由 {@code StableFootingHandler} 实现：{@code Player.getDigSpeed}
 *       末尾触发 {@code PlayerEvent.BreakSpeed}（在悬空 {@code f /= 5.0F} 惩罚之后），
 *       事件内把已含惩罚的挖掘速度乘回 5.0F，精确消除悬空挖掘减速。</li>
 * </ul>
 * 已纳入档位体系低档（{@code EnchantmentTier.LOW}），铁砧成本按低档权重
 * （{@code Config.ANVIL_COST_LOW_TIER}，默认 4）计算；maxLevel 固定 I 级，
 * 与低档默认 maxLevel（1）一致。
 */
public class StableFootingEnchantment extends Enchantment {

    public StableFootingEnchantment() {
        super(Rarity.COMMON, EnchantmentCategory.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET});
    }

    @Override
    public int getMaxLevel() {
        // 固定 I 级；覆写后调用不派发到基类，不受档位 maxLevel 体系影响
        return 1;
    }
}
