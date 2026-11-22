package dev.newenchantmentsystem.mixin;

import dev.newenchantmentsystem.api.EnchantmentRules;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 拦截 Enchantment 的禁用（ban）与互斥（exclusion）行为，统一查询
 * {@link EnchantmentRules} 门面。
 * <p>
 * <b>禁用</b>：
 * <ul>
 *   <li>{@code isDiscoverable}：覆盖附魔台、战利品随机附魔
 *       （{@code EnchantmentHelper.getAvailableEnchantmentResults}）与创造模式
 *       附魔书展示（{@code CreativeModeTabs.addEnchantedBooks}）；</li>
 *   <li>{@code isTradeable}：覆盖村民图书管理员附魔书交易
 *       （{@code VillagerTrades.EnchantBookForEmeralds} 依赖 isTradeable 而非
 *       isDiscoverable，须单独拦截）。</li>
 * </ul>
 * 铁砧合并路径不查以上两方法，由 {@code AnvilMenuBanMixin} 单独拦截。
 * <p>
 * <b>互斥</b>：{@code isCompatibleWith} 为 final 方法，内部双向调用虚方法
 * {@code checkCompatibility}——铁砧（{@code isEnchantmentCompatible}）、附魔台多附魔
 * （{@code filterCompatibleEnchantments}）与 {@code /enchant} 指令全部汇聚于此，
 * 在 {@code checkCompatibility} 头注入一次查询即可让互斥规则在全部来源生效。
 * <p>
 * 另拦截 Enchantment#canApplyAtEnchantingTable，使锋利（Sharpness）可附在斧子上。
 * Forge 附魔台检查链：canApplyAtEnchantingTable → Item.canApplyAtEnchantingTable
 * → category.canEnchant（锋利属 WEAPON，仅放行剑），故原版附魔台不给斧子出锋利；
 * 铁砧走 DamageEnchantment.canEnchant 覆写（AxeItem 直接放行），本就不受限。
 */
@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {

    // 附魔台 / 战利品 / 创造模式附魔书展示：isDiscoverable
    @Inject(method = "isDiscoverable", at = @At("HEAD"), cancellable = true)
    private void newenchantmentsystem$banEnchantments(CallbackInfoReturnable<Boolean> cir) {
        // 配置未加载时（如注册阶段）门面按原版放行，加载后自动开始拦截，无需重启
        if (EnchantmentRules.isBanned((Enchantment) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    // 村民交易附魔书：isTradeable
    @Inject(method = "isTradeable", at = @At("HEAD"), cancellable = true)
    private void newenchantmentsystem$banFromVillagerTrades(CallbackInfoReturnable<Boolean> cir) {
        if (EnchantmentRules.isBanned((Enchantment) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    // 互斥规则：isCompatibleWith（final）内部双向调用 checkCompatibility，
    // 铁砧 / 附魔台多附魔 / /enchant 指令全部汇聚于此，一处拦截即覆盖全部来源
    @Inject(method = "checkCompatibility", at = @At("HEAD"), cancellable = true)
    private void newenchantmentsystem$applyExclusions(Enchantment other, CallbackInfoReturnable<Boolean> cir) {
        // 无任何规则（ban/互斥均为空）时快速跳过，零开销
        if (!EnchantmentRules.hasRules()) {
            return;
        }
        if (EnchantmentRules.isExclusive((Enchantment) (Object) this, other)) {
            cir.setReturnValue(false);
        }
    }

    // canApplyAtEnchantingTable 是 Forge patch 方法（无混淆映射），须 remap = false
    @Inject(method = "canApplyAtEnchantingTable", at = @At("HEAD"), cancellable = true, remap = false)
    private void newenchantmentsystem$axeSharpness(ItemStack pStack, CallbackInfoReturnable<Boolean> cir) {
        // 锋利允许附在斧子上（附魔台路径）。其它附魔/物品走原逻辑。
        if ((Object) this == Enchantments.SHARPNESS && pStack.getItem() instanceof AxeItem) {
            cir.setReturnValue(true);
        }
    }
}
