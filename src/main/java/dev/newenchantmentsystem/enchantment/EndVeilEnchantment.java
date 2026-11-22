package dev.newenchantmentsystem.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * 末地面纱（End Veil）：防止末影人因玩家注视其眼睛而敌视玩家。
 * <p>
 * <ul>
 *   <li>最高等级：I（固定，开关型附魔——多级无额外效果）。</li>
 *   <li>适用物品：{@link EnchantmentCategory#VANISHABLE}（任意可附魔物品，佩戴 / 手持
 *       均生效；附魔台 / 铁砧在生存模式下即可获取，非宝藏附魔）。</li>
 *   <li>效果：
 *       <ul>
 *         <li><b>防敌意</b>：携带者看向末影人眼睛时，末影人不会对携带者产生敌意
 *             （效果由 {@code EndVeilHandler} 监听 Forge {@code EnderManAngerEvent}
 *             实现——该事件由 {@code EnderMan.isLookingAtMe} 首行调用的
 *             {@code ForgeHooks.shouldSuppressEnderManAnger} 派发，无需 Mixin）；</li>
 *         <li><b>挑衅破绽</b>：携带者在末影人 16 格范围内使用末影珍珠 / 末影之眼时，
 *             范围内末影人立即对携带者产生敌意，无视面纱。</li>
 *       </ul>
 *   </li>
 * </ul>
 * 设计来自更好的末地模组（BetterEnd），本模组新增"使用末影珍珠 / 末影之眼挑衅"机制。
 * 已纳入档位体系低档（{@code EnchantmentTier.LOW}）——开关型附魔归入低档（默认最高 1 级），
 * 铁砧成本按低档权重（{@code Config.ANVIL_COST_LOW_TIER}，默认 4）计算。
 */
public class EndVeilEnchantment extends Enchantment {

    public EndVeilEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.VANISHABLE, EquipmentSlot.values());
    }

    @Override
    public int getMaxLevel() {
        // 开关型附魔固定 I 级；覆写后调用不派发到基类，不受档位 maxLevel 体系影响
        return 1;
    }
}
