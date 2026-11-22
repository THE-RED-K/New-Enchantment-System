package dev.newenchantmentsystem.mixin;

import dev.newenchantmentsystem.api.Config;
import dev.newenchantmentsystem.api.GoldenItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 本 Mixin 在 {@code ExperienceOrb.repairPlayerItems}（原版"经验 → 耐久"修复入口）的
 * HEAD 处接管整条修复链路，实现两类行为：
 *
 * <p>1. 原版 Mending 速率调节（{@link Config#MENDING_XP_REPAIR_RATIO}，默认 2.0 = 原版）：
 * 存在带 Mending 附魔的损坏物品时，按配置的"每点经验修复量"执行与原版等价的递归逻辑
 * （每点经验修复 ratio 点耐久，修复多少耐久就消耗多少经验），使速率可配置。
 * 配置默认 2.0 时与原版逐位一致。</p>
 *
 * <p>2. 金质自动修补（Golden Mending）：当配置 goldenMending = true（默认）时，
 * 无 Mending 附魔物品时，金质装备/工具/武器无需附魔即自带经验修补效果：
 * 拾取经验球时按 1 点经验修复 1 点耐久的固定换算自动修复
 * （独立于 Mending 速率配置，刻意保持 1:1）。</p>
 *
 * <p>注入点：ExperienceOrb.repairPlayerItems 的 HEAD。原版附魔 Mending 仍优先处理。
 * 速率配置以模组配置为准，统一覆盖 Forge 物品级 getXpRepairRatio 覆写（全局可配置语义）。
 * 采用 HEAD 接管而非 {@code @Redirect} 指令重定向，规避对私有方法与接口默认方法的
 * 指令级 target 解析问题。</p>
 */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {

    /** 经验球携带的经验值（原版字段，@Shadow 混入访问）。 */
    @Shadow
    public int value;

    /**
     * 原版 Mending 修复入口的 HEAD 接管：
     * - 配置未加载或 repairAmount <= 0 时不干预，交还原版；
     * - 存在带 Mending 附魔的损坏物品：按配置速率执行修复（附魔优先，默认 2.0 与原版一致）；
     * - 否则若 goldenMending=true：金质自动修补按 1:1 接管。
     */
    @Inject(method = "repairPlayerItems", at = @At("HEAD"), cancellable = true)
    private void newenchantmentsystem$handleRepair(Player player, int repairAmount,
                                                   CallbackInfoReturnable<Integer> cir) {
        if (!Config.isLoaded() || repairAmount <= 0) {
            return;
        }
        // 仍有带 Mending 附魔的损坏物品：按配置速率接管原版修复（附魔优先）
        if (EnchantmentHelper.getRandomItemWith(Enchantments.MENDING, player, ItemStack::isDamaged) != null) {
            cir.setReturnValue(newenchantmentsystem$repairMending(player, repairAmount, this.value));
            return;
        }
        // 无 Mending 物品且开启金质自动修补：1:1 修复金质物品
        if (Config.GOLDEN_MENDING.get()) {
            cir.setReturnValue(newenchantmentsystem$repairGoldenItems(player, repairAmount));
        }
    }

    /**
     * 复制原版 repairPlayerItems 的递归逻辑，速率改为配置 mendingXpRepairRatio：
     * 每点经验修复 ratio 点耐久（原版为 getXpRepairRatio()，默认 2f），
     * 修复了 repaired 点耐久则消耗 repaired / ratio 点经验（原版 durabilityToXp）。
     * 配置默认 2.0 时浮点运算 + 截断结果与原版整型运算逐位一致。
     * orbValue 为经验球携带的经验值（原版 this.value；非 final 且需在静态上下文中使用，故以参数传入）。
     */
    @Unique
    private static int newenchantmentsystem$repairMending(Player player, int repairAmount, int orbValue) {
        Map.Entry<EquipmentSlot, ItemStack> entry =
                EnchantmentHelper.getRandomItemWith(Enchantments.MENDING, player, ItemStack::isDamaged);
        if (entry != null) {
            ItemStack stack = entry.getValue();
            float ratio = (float) newenchantmentsystem$mendingRatio();
            int repaired = Math.min((int) (orbValue * ratio), stack.getDamageValue());
            stack.setDamageValue(stack.getDamageValue() - repaired);
            int remaining = repairAmount - (int) (repaired / ratio);
            return remaining > 0 ? newenchantmentsystem$repairMending(player, remaining, orbValue) : 0;
        }
        return repairAmount;
    }

    /** 读取配置的每点经验修复量；配置未加载时回退原版值 2.0。 */
    @Unique
    private static double newenchantmentsystem$mendingRatio() {
        return Config.isLoaded() ? Config.MENDING_XP_REPAIR_RATIO.get() : 2.0D;
    }

    /** 参照原版 repairPlayerItems 的递归逻辑，候选改为金质损坏物品。 */
    @Unique
    private static int newenchantmentsystem$repairGoldenItems(Player player, int repairAmount) {
        Map.Entry<EquipmentSlot, ItemStack> entry = newenchantmentsystem$pickRandomGoldenItem(player);
        if (entry == null) {
            return repairAmount;
        }
        ItemStack stack = entry.getValue();
        // 本模组换算：1 点经验修复 1 点耐久（区别于原版 Mending 的 1:2）
        int repaired = Math.min(repairAmount, stack.getDamageValue());
        stack.setDamageValue(stack.getDamageValue() - repaired);
        int remaining = repairAmount - repaired;
        return remaining > 0 ? newenchantmentsystem$repairGoldenItems(player, remaining) : 0;
    }

    /**
     * 从玩家 6 个装备槽（主手、副手、头盔、胸甲、护腿、靴子）中随机选一个
     * 可修复的金质物品。金质判定使用 {@link GoldenItems} 注册表
     * （默认含原版全部金质装备/工具/武器，兼容附属可通过 add() 扩展），
     * 并过滤掉不可损坏的物品（金锭、金苹果等）。
     */
    @Unique
    private static Map.Entry<EquipmentSlot, ItemStack> newenchantmentsystem$pickRandomGoldenItem(Player player) {
        List<Map.Entry<EquipmentSlot, ItemStack>> candidates = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (GoldenItems.isGolden(stack)
                    && stack.isDamageableItem() && stack.isDamaged()) {
                candidates.add(Map.entry(slot, stack));
            }
        }
        return candidates.isEmpty() ? null : candidates.get(player.getRandom().nextInt(candidates.size()));
    }
}
