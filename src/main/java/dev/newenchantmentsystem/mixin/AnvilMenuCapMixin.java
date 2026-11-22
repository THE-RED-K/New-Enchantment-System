package dev.newenchantmentsystem.mixin;

import dev.newenchantmentsystem.api.Config;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 铁砧"累积修理惩罚移除"与"过于昂贵上限配置化"——独立版。
 * <p>
 * 从 New-Armor-System 的 {@code AnvilMenuMixin} 中复制<b>与附魔合并成本相关</b>的两段逻辑：
 * <ul>
 *   <li>{@code ItemStack.getBaseRepairCost()} 一律返回 0：合并成本中的历史惩罚项
 *       {@code j} 恒为 0，末尾 {@code setRepairCost} 输入也恒为 0，惩罚不再累积；</li>
 *   <li>{@code DataSlot.get()} 拦截"过于昂贵"判断（原版 {@code cost >= 40} 两处）：
 *       规则开启时返回 0（上限恒不触发）；关闭时读取
 *       {@link Config#ANVIL_TOO_EXPENSIVE_THRESHOLD} 平移，使原判断等价于
 *       {@code 真实费用 >= 阈值}（默认 40，行为与原版一致）。</li>
 * </ul>
 * <p>
 * 配置入口见 {@link Config#ANVIL_REPAIR_COST_RULE} / {@link Config#ANVIL_TOO_EXPENSIVE_THRESHOLD}。
 * <p>
 * <b>与 New-Armor-System 的共存</b>：本类与护甲系统的 {@code AnvilMenuMixin} 命中同一调用点
 * （{@code getBaseRepairCost}、{@code DataSlot.get}）。Mixin 对同一调用点的多个 @Redirect
 * 会按优先级嵌套链接，行为不可控，因此由 {@link NewEnchantmentSystemMixinPlugin} 在护甲系统
 * 已装载时整类跳过本 Mixin——此时护甲系统的同名规则全权生效，本模组不注入。
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuCapMixin {

    /**
     * 忽略历史修理惩罚：{@code createResult} 内所有 {@code getBaseRepairCost()} 一律返回 0。
     */
    @Redirect(method = "createResult",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getBaseRepairCost()I"))
    private int nes$ignoreRepairPenalty(ItemStack stack) {
        if (Config.isLoaded() && Config.ANVIL_REPAIR_COST_RULE.get()) {
            return 0;
        }
        return stack.getBaseRepairCost();
    }

    /**
     * 可配置"过于昂贵"阈值：{@code createResult} 内 {@code DataSlot#get()} 的返回值
     * 仅用于 {@code cost >= 40} 的两处判断。规则开启时返回 0（恒不触发）；
     * 关闭时按 {@code 真实值 − 阈值 + 40} 平移，等价于 {@code 真实费用 >= 阈值}。
     * 真实费用仍由 {@code cost.set(j + i)} 写入 DataSlot，UI 显示与实际扣费不受影响。
     */
    @Redirect(method = "createResult",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;get()I"))
    private int nes$applyTooExpensiveCap(DataSlot cost) {
        if (Config.isLoaded()) {
            if (Config.ANVIL_REPAIR_COST_RULE.get()) {
                return 0; // 移除模式：上限恒不触发
            }
            int threshold = Config.ANVIL_TOO_EXPENSIVE_THRESHOLD.get();
            return cost.get() - threshold + 40; // 平移：>= 40 判断等价于真实费用 >= 阈值
        }
        return cost.get();
    }
}
