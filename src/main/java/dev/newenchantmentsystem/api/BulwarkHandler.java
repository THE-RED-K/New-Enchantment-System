package dev.newenchantmentsystem.api;

import dev.newenchantmentsystem.init.ModEnchantments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 堡垒（Bulwark）附魔的效果处理。
 * <p>
 * 监听 Forge {@link LivingKnockBackEvent}（由 {@code LivingEntity.knockback}
 * 首行触发，可取消）：实体正在举盾格挡（{@link LivingEntity#isBlocking()}，
 * 即使用中的物品为可格挡的盾牌）且该盾牌带堡垒附魔时，取消事件——免疫击退。
 * <ul>
 *   <li>覆盖所有经 {@code knockback} 结算的击退源：玩家近战（含疾跑攻击）、
 *       横扫副目标、击退附魔 / 药水、末影龙冲撞等；原版爆炸推离走独立 push
 *       路径，不在范围内。</li>
 *   <li>仅检查 {@code isBlocking()} 时的使用物品（{@code getUseItem()}），
 *       主手 / 副手持盾均可，且自动适配其它模组中可 {@code SHIELD_BLOCK} 的盾牌。</li>
 *   <li>取消事件两端幂等，无副作用。</li>
 * </ul>
 */
public class BulwarkHandler {

    @SubscribeEvent
    public static void onLivingKnockBack(LivingKnockBackEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.isBlocking()
                && entity.getUseItem().getEnchantmentLevel(ModEnchantments.BULWARK.get()) > 0) {
            event.setCanceled(true);
        }
    }
}
