package dev.newenchantmentsystem.api;

import dev.newenchantmentsystem.init.ModEnchantments;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 夜视（Night Vision）附魔的效果处理。
 * <p>
 * 监听 {@link TickEvent.PlayerTickEvent}（服务端）：每 0.5s（10 ticks）无条件检查
 * 一次玩家头盔槽位，若头盔带夜视附魔，则将夜视效果刷新为 15s（300 ticks）。
 * <p>
 * <b>必须无条件刷新而非"快耗尽才刷新"</b>，原因有二：
 * <ul>
 *   <li>其他模组可能主动清除 / 覆盖效果，条件式刷新会让效果在两次刷新之间
 *       长时间缺失；</li>
 *   <li>原版夜视效果在剩余时长低于 200 ticks（10s）时进入明暗闪烁动画，
 *       条件式刷新（剩余 &lt; 0.5s 才补）会让效果长期处于闪烁区间、视野一暗一明，
 *       而每 0.5s 刷新到 300 ticks 可让剩余时长始终 ≥ 290 ticks，永不进入闪烁区间。</li>
 * </ul>
 * 效果实例与原版夜视药水一致（0 级、显示图标、无环境粒子），卸下头盔后效果自然耗尽。
 */
public class NightVisionHandler {

    /** 夜视效果时长：15 秒 = 300 ticks。 */
    private static final int NIGHT_VISION_DURATION = 300;
    /** 刷新检查间隔：0.5 秒 = 10 ticks。 */
    private static final int CHECK_INTERVAL = 10;

    private NightVisionHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (player.level().isClientSide) {
            return;
        }
        // 每 0.5s（10 ticks）检查一次
        if (player.tickCount % CHECK_INTERVAL != 0) {
            return;
        }
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getEnchantmentLevel(ModEnchantments.NIGHT_VISION.get()) <= 0) {
            return;
        }
        // 无条件刷新：效果始终维持在 290~300 ticks，高于闪烁阈值（200 ticks）
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, NIGHT_VISION_DURATION));
    }
}
