package dev.newenchantmentsystem.enchantment;

import dev.newenchantmentsystem.api.Config;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * 魔法保护（Magic Protection）：魔法/元素伤害每级 -3%（乘算）。
 * - 高档附魔，最大等级直接读取 {@link Config#HIGH_MAX_LEVEL}（默认 10）
 * - 适用于全部盔甲（{@link EnchantmentCategory#ARMOR}）
 * - 魔法伤害集合 = #minecraft:witch_resistant_to（魔法/尖牙/荆棘） ∪ #minecraft:is_fire（元素伤害归入魔法大类）
 * - 减伤百分比与保护/弹射物/爆炸保护加算，可超过 100% 形成免疫（刻意设计）
 * - 不覆写 getDamageProtection：本模组已完全弃用原版附魔点数（EPF）系统，
 *   效果由 {@code LivingEntityProtectionMixin} 直接按等级百分比计算
 */
public class MagicProtectionEnchantment extends Enchantment {

    public MagicProtectionEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.ARMOR,
                new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET});
    }

    @Override
    public int getMaxLevel() {
        // 配置未加载（注册阶段）时回退固定值 10（= 配置 highMaxLevel 默认值的固定写死值，
        // 不读取配置）；配置加载后返回可改的高档最大等级。
        return Config.isLoaded() ? Config.HIGH_MAX_LEVEL.get() : 10;
    }
}
