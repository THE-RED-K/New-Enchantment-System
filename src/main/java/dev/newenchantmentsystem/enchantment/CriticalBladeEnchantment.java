package dev.newenchantmentsystem.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * 暴击之刃（Critical Blade）：暴击伤害每级 +25%（乘算）。
 * - 低档附魔，最大等级由 {@code TierMaxLevelMixin}（lowMaxLevel）控制
 * - 适用于剑和斧（{@link EnchantmentCategory#WEAPON}）
 * - 稀有度 RARE：附魔台权重 2，与横扫之刃一致；非宝藏、非诅咒，
 *   附魔台/村民交易/战利品刷新规则均与原版横扫之刃一致
 */
public class CriticalBladeEnchantment extends Enchantment {

    public CriticalBladeEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }
}
