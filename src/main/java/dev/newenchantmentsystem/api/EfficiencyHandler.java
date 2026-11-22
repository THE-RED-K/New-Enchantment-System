package dev.newenchantmentsystem.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 效率附魔（Efficiency）自定义挖掘速度加成。
 * 原版公式写死在 Player.getDigSpeed 中：f += level^2 + 1。
 * 新公式：提升效率 E+ = EfficiencyCoefficient * level + EfficiencyConstant（默认 2.5 * L + 1）。
 *
 * 实现：Player.getDigSpeed 末尾会触发 PlayerEvent.BreakSpeed，
 * 其 getOriginalSpeed() 已包含原版效率加成，把该加成替换为自定义线性加成即可，其余逻辑不动。
 */
public class EfficiencyHandler {

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!Config.isLoaded()) {
            return;
        }
        Player player = event.getEntity();
        int level = EnchantmentHelper.getBlockEfficiency(player);
        if (level <= 0) {
            return;
        }
        // 与原版一致：仅当主手基础挖掘速度 > 1.0F 时效率加成才生效
        BlockState state = event.getState();
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty() || mainHand.getDestroySpeed(state) <= 1.0F) {
            return;
        }
        // 去掉原版 level^2 + 1，换上自定义线性加成 E+ = coefficient * level + constant
        float customBonus = (float) (Config.EFFICIENCY_COEFFICIENT.get() * level + Config.EFFICIENCY_CONSTANT.get());
        float adjusted = event.getOriginalSpeed() - (level * level + 1) + customBonus;
        event.setNewSpeed(adjusted);
    }
}
