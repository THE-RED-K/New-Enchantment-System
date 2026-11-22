package dev.newenchantmentsystem.mixin;

import dev.newenchantmentsystem.api.EnchantmentBanRegister;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 铁砧 ban 过滤：被 ban 的附魔书（或任何携带 ban 附魔的附加物）无法将 ban 附魔
 * 敲上目标物品。
 * <p>
 * 原版铁砧合并路径（{@code AnvilMenu.createResult}）遍历附加物上的附魔
 * （{@code map1 = EnchantmentHelper.getEnchantments(itemstack2)}）并调用
 * {@code Enchantment.canEnchant}，全程不检查 {@code isDiscoverable()} /
 * {@code isTradeable()}——附魔台/战利品/村民交易的过滤对铁砧无效，必须在此精确拦截。
 * <p>
 * 实现：{@code createResult} 中 {@code getEnchantments} 恰有两处调用——123 行第一格
 * （{@code map = getEnchantments(itemstack1)}）与 171 行第二格
 * （{@code map1 = getEnchantments(itemstack2)}）。用 {@code @Redirect} + {@code ordinal = 1}
 * 仅命中第二格调用，直接剔除 ban 附魔；第一格（目标物品）不经过此 handler，原样合并，
 * 不会洗掉物品上已存在的附魔。
 * 纯 ban 书过滤后 {@code map1} 为空 → 合并循环不执行 → {@code i <= 0} → 输出为空，
 * 表现即"无法敲上"；混合书则仅合并合法附魔。创造模式下 {@code flag1 = true} 的强制
 * 放行只作用于仍在 {@code map1} 中的附魔，故创造模式同样无法敲上 ban 附魔。
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuBanMixin {

    @Redirect(
            method = "createResult",
            at = @At(value = "INVOKE", ordinal = 1,
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantments(Lnet/minecraft/world/item/ItemStack;)Ljava/util/Map;"))
    private Map<Enchantment, Integer> newenchantmentsystem$stripBannedFromAddition(ItemStack stack) {
        // 仅第二格（附加物）调用进入此 handler（ordinal = 1），无条件过滤 ban 附魔
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        boolean removed = false;
        Map<Enchantment, Integer> filtered = new HashMap<>(enchantments);
        Iterator<Map.Entry<Enchantment, Integer>> iterator = filtered.entrySet().iterator();
        while (iterator.hasNext()) {
            if (EnchantmentBanRegister.isBanned(iterator.next().getKey())) {
                iterator.remove();
                removed = true;
            }
        }
        return removed ? filtered : enchantments;
    }
}
