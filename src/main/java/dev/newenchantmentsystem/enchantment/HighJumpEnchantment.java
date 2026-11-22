package dev.newenchantmentsystem.enchantment;

import dev.newenchantmentsystem.api.Config;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * 高跳（High Jump）：每级跳跃高度 +10%、摔落安全距离 +10%。
 * <ul>
 *   <li>中档附魔，最大等级读取 {@link Config#MEDIUM_MAX_LEVEL}（默认 4）</li>
 *   <li>适用物品：靴子（{@link EnchantmentCategory#ARMOR_FEET}）</li>
 *   <li>跳跃高度由 {@code HighJumpHandler}（{@code LivingJumpEvent}）实现：
 *       初速 ×√(1+b)（h = v²/2g，高度恰 ×(1+b)，b = 每级百分比 × 等级）</li>
 *   <li>摔落安全距离由 {@code HighJumpHandler}（{@code LivingFallEvent}）实现：在
 *       {@code causeFallDamage} 开头的事件中把摔落距离减去 {@code 3.0×b}（等价于把安全距离
 *       3.0F 放大为 3.0F×(1+b)）；Forge 会把事件修改后的距离传入 {@code calculateFallDamage}，
 *       对原版公式与新护甲系统的质量公式同时叠加生效</li>
 * </ul>
 * 已纳入档位体系中档（{@code EnchantmentTier.MEDIUM}），铁砧成本按中档权重
 * （{@code Config.ANVIL_COST_MEDIUM_TIER}，默认 2）计算。
 */
public class HighJumpEnchantment extends Enchantment {

    public HighJumpEnchantment() {
        super(Rarity.COMMON, EnchantmentCategory.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET});
    }

    @Override
    public int getMaxLevel() {
        // 中档：配置未加载（注册阶段）时回退固定值 4（= mediumMaxLevel 默认值），
        // 配置加载后返回可改的中档最大等级。
        return Config.isLoaded() ? Config.MEDIUM_MAX_LEVEL.get() : 4;
    }
}
