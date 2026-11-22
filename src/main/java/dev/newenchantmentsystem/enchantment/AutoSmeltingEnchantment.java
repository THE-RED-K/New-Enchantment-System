package dev.newenchantmentsystem.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.UntouchingEnchantment;

/**
 * 自动冶炼（Auto Smelting）：破坏方块时掉落熔炼后的产物，对任何拥有熔炼配方的掉落物生效。
 * <p>
 * 设计来自神化模组（Apotheosis）。经典行为：矿石直接掉落金属锭、圆石掉落石头、
 * 原木掉落木炭等；无熔炼配方的掉落物（如钻石、木板、树苗）原样保留。
 * <ul>
 *   <li>最高等级：I（固定，开关型附魔——多级无额外效果）</li>
 *   <li>适用物品：镐 / 斧 / 铲 / 锄（{@link EnchantmentCategory#DIGGER}）</li>
 *   <li>与精准采集（{@link UntouchingEnchantment}）互斥：烧炼会破坏"保留原方块"的语义；
 *       原版路径靠本类 {@link #checkCompatibility} 覆写，{@code /enchant} 指令等额外来源
 *       由 {@code EnchantmentRules} 互斥注册表兜底。</li>
 *   <li>效果由 {@code AutoSmeltingHandler} 实现（Forge {@code BlockBreakEvent}，非 Mixin）：
 *       修改掉落列表，将每个有熔炼配方的掉落物替换为其烧炼产物，并按熔炼配方补充经验。</li>
 * </ul>
 * 已纳入档位体系低档（{@code EnchantmentTier.LOW}）——开关型附魔归入低档（默认最高 1 级），
 * 铁砧成本按低档权重（{@code Config.ANVIL_COST_LOW_TIER}，默认 4）计算。
 * <p>
 * 稀有度 VERY_RARE（附魔台权重 1，名称深紫）：对标原版精准采集（{@code UntouchingEnchantment}，
 * 同为 {@link Rarity#VERY_RARE}）——两者同为"掉落转换"类且互斥，自动冶炼（矿石直接变锭、
 * 掉落物就地熔炼）强度与之相当，故按原版最稀有档位设定。
 */
public class AutoSmeltingEnchantment extends Enchantment {

    public AutoSmeltingEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        // 开关型附魔固定 I 级；覆写后调用不派发到基类，不受档位 maxLevel 体系影响
        return 1;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        // 烧炼（转换掉落物）与精准采集（保留原方块）语义冲突
        return !(other instanceof UntouchingEnchantment) && super.checkCompatibility(other);
    }
}
