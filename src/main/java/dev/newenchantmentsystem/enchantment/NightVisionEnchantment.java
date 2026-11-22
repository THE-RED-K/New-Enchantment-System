package dev.newenchantmentsystem.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * 夜视（Night Vision）：戴上附魔头盔持续获得夜视效果。
 * <p>
 * <ul>
 *   <li>最高等级：I（固定，开关型附魔——多级无额外效果）。</li>
 *   <li>适用物品：仅头盔（{@link EnchantmentCategory#ARMOR_HEAD}），附魔台 / 铁砧
 *       在生存模式下即可获取，非宝藏附魔。</li>
 *   <li>效果：佩戴带本附魔的头盔时，每 0.5s（10 ticks）将夜视效果刷新为 15s
 *       （300 ticks），效果持续期间保持视野明亮。由 {@code NightVisionHandler}
 *       （Forge {@code PlayerTickEvent}，非 Mixin）实现。</li>
 * </ul>
 * 已纳入档位体系低档（{@code EnchantmentTier.LOW}）——开关型附魔归入低档（默认最高 1 级），
 * 铁砧成本按低档权重（{@code Config.ANVIL_COST_LOW_TIER}，默认 4）计算。
 */
public class NightVisionEnchantment extends Enchantment {

    public NightVisionEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_HEAD, new EquipmentSlot[]{EquipmentSlot.HEAD});
    }

    @Override
    public int getMaxLevel() {
        // 开关型附魔固定 I 级；覆写后调用不派发到基类，不受档位 maxLevel 体系影响
        return 1;
    }
}
