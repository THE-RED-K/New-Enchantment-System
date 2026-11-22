package dev.newenchantmentsystem.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * 永恒（Everlasting）：带有此附魔的物品实体不会自然消失。
 * <p>
 * <ul>
 *   <li>最高等级：I（固定，开关型附魔——多级无额外效果）。</li>
 *   <li>适用物品：{@link EnchantmentCategory#VANISHABLE}（任意可附魔物品——可损耗的工具、
 *       武器、盔甲等；附魔台 / 铁砧 / 村民在生存模式下即可获取，非宝藏附魔）。</li>
 *   <li>效果：
 *       <ul>
 *         <li><b>不自然消失</b>：带附魔的物品掉落为实体后，不会因 5 分钟寿命超时
 *             （{@code ItemEntity.age >= lifespan}）而消失——效果由
 *             {@code EverlastingHandler} 监听 Forge {@code ItemExpireEvent} 实现：
 *             取消该事件后 Forge 以 {@code extraLife}（默认物品寿命 6000 tick）为物品
 *             续命，每次到期自动续期，故永不因超时消失；</li>
 *         <li><b>销毁方式除外</b>：岩浆、火、虚空、爆炸、仙人掌等销毁走
 *             {@code ItemEntity.hurt} / {@code outOfWorld} 路径，不经过
 *             {@code ItemExpireEvent}，因此带附魔的物品在这些方式下仍会正常销毁。</li>
 *       </ul>
 *   </li>
 * </ul>
 * 已纳入档位体系低档（{@code EnchantmentTier.LOW}）——开关型附魔归入低档（默认最高 1 级），
 * 铁砧成本按低档权重（{@code Config.ANVIL_COST_LOW_TIER}，默认 4）计算。
 */
public class EverlastingEnchantment extends Enchantment {

    public EverlastingEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.VANISHABLE, EquipmentSlot.values());
    }

    @Override
    public int getMaxLevel() {
        // 开关型附魔固定 I 级；覆写后调用不派发到基类，不受档位 maxLevel 体系影响
        return 1;
    }
}
