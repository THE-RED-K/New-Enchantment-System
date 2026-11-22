package dev.newenchantmentsystem.api;

import dev.newenchantmentsystem.init.ModEnchantments;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 永恒（Everlasting）附魔效果：拦截物品实体自然消失。
 * <p>
 * 原版 / Forge 的物品自然消失逻辑在 {@code ItemEntity.tick()}：
 * <pre>{@code
 * if (!this.level().isClientSide && this.age >= lifespan) {
 *     int hook = ForgeEventFactory.onItemExpire(this, item);
 *     if (hook < 0) {
 *         this.discard();           // 事件被取消（post 返回 true）时不会走到这里
 *     } else {
 *         this.lifespan += hook;    // 取消事件 → 返回 getExtraLife()（默认物品寿命 6000）→ 续命
 *     }
 * }
 * }</pre>
 * 因此本处理器取消 {@link ItemExpireEvent} 后，物品每次到期都会被续上 6000 tick 寿命，
 * 永不因超时自然消失；且 {@code ItemEntity} 的岩浆 / 火 / 虚空等销毁路径
 * （{@code hurt} / {@code outOfWorld}）完全不经由此事件，天然满足"岩浆等方式除外"。
 */
public class EverlastingHandler {

    @SubscribeEvent
    public static void onItemExpire(ItemExpireEvent event) {
        // 物品实体携带永恒附魔时取消过期事件，使其每次到期自动续命
        if (event.getEntity().getItem().getEnchantmentLevel(ModEnchantments.EVERLASTING.get()) > 0) {
            event.setCanceled(true);
        }
    }
}
