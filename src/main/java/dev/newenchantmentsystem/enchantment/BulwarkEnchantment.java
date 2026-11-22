package dev.newenchantmentsystem.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * 堡垒（Bulwark）：举盾格挡时免疫击退。
 * <p>
 * 设计来自万象模组（Apotheosis）。由于本模组的配套护甲系统与万象不兼容，
 * 故在本模组内独立实现该附魔。
 * <ul>
 *   <li><b>最高等级</b>：I（固定，开关型附魔——多级无额外效果）。</li>
 *   <li><b>适用物品</b>：盾牌。原版附魔分类没有"盾牌"类别，故覆写
 *       {@link #canEnchant} 与 {@link #canApplyAtEnchantingTable} 限定
 *       {@link ShieldItem}；1.20.1 盾牌本身满足 {@code Item.isEnchantable}
 *       （可损耗），附魔台 / 铁砧可正常应用。</li>
 *   <li><b>效果</b>：携带者举盾格挡（{@code isBlocking}，即使用中的物品为盾牌）时，
 *       免疫一切击退——由 {@code BulwarkHandler} 取消 Forge {@code LivingKnockBackEvent}
 *       实现（原版所有伤害击退与击退附魔/药水均经 {@code LivingEntity.knockback}
 *       结算，取消事件即完全免疫；爆炸推离走原版独立 push 路径，不在范围内）。</li>
 *   <li><b>稀有度</b>：RARE（附魔台权重 2，名称青色）——对标原版爆炸保护
 *       （{@link Rarity#RARE}）等防护强效档：免疫击退属综合防护效果，但需主动举盾
 *       才生效（格挡期间无法攻击），强度与爆炸保护相当。</li>
 *   <li>非宝藏、非诅咒。效果同时覆盖主手 / 副手持盾。</li>
 * </ul>
 * 已纳入档位体系低档（{@code EnchantmentTier.LOW}），铁砧成本按低档权重
 * （{@code Config.ANVIL_COST_LOW_TIER}，默认 4）计算；maxLevel 固定 I 级，
 * 与低档默认 maxLevel（1）一致。
 */
public class BulwarkEnchantment extends Enchantment {

    public BulwarkEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.BREAKABLE, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    @Override
    public int getMaxLevel() {
        // 固定 I 级；覆写后调用不派发到基类，不受档位 maxLevel 体系影响
        return 1;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof ShieldItem;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return stack.getItem() instanceof ShieldItem;
    }
}
