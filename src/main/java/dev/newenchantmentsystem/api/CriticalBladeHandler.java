package dev.newenchantmentsystem.api;

import dev.newenchantmentsystem.init.ModEnchantments;
import dev.newenchantmentsystem.api.Config;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 暴击之刃（Critical Blade）事件处理。
 * 通过 Forge 官方事件 {@link CriticalHitEvent} 修改暴击伤害倍率，无需 Mixin，兼容性最佳。
 * <p>
 * 仅对原版判定成立的暴击（{@link CriticalHitEvent#isVanillaCritical()}）生效：
 * <ul>
 *   <li>原版 {@code Player.attack} 只在暴击成立时广播该事件并执行
 *       {@code f *= hitResult.getDamageModifier()}，非暴击根本没有事件；</li>
 *   <li>但 Tetra 的 {@code causeTruesweepDamage}（真实横扫副目标结算）与
 *       {@code hitEntity}（jab 突刺结算）会<b>无条件</b>调用
 *       {@code ForgeHooks.getCriticalHit(player, target, false, 1.5F)}
 *       广播同一事件并乘算 {@code getDamageModifier()}（vanillaCritical=false）。
 *       若不排除，暴击之刃会错误放大横扫副目标 / 突刺伤害（它们并非原版暴击）。</li>
 * </ul>
 * 乘算写法（而非覆盖为固定值），可与其它 mod 的暴击修正友好叠加。
 */
public class CriticalBladeHandler {

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        if (!Config.isLoaded()) {
            return;
        }
        // 排除 Tetra 横扫/突刺等非原版暴击事件（见类注释），仅对原版真暴击生效
        if (!event.isVanillaCritical()) {
            return;
        }
        Player player = event.getEntity();
        ItemStack stack = player.getMainHandItem();
        int level = stack.getEnchantmentLevel(ModEnchantments.CRITICAL_BLADE.get());
        if (level <= 0) {
            return;
        }
        float perLevel = Config.CRITICAL_BLADE_MULTIPLIER.get().floatValue();
        event.setDamageModifier(event.getDamageModifier() * (1.0F + perLevel * level));
    }
}
