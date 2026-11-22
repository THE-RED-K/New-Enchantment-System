package dev.newenchantmentsystem.api;

import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 迅捷潜行（Swift Sneak）等级突破实现（客户端专用）。
 * <p>
 * 本模组档位系统允许迅捷潜行超过原版 3 级上限；原版潜行速度倍率公式
 * {@code f = Mth.clamp(0.3F + getSneakingSpeedBonus(entity), 0.0F, 1.0F)}
 * 在 {@code LocalPlayer.aiStep} 中计算（bonus = 等级 × 0.15F），当等级 ≥ 5 时
 * {@code f} 被钳死在 1.0，更高等级不再有收益。
 * <p>
 * 迁移前由 {@code SwiftSneakMixin} 移除 {@code LocalPlayer.aiStep} 中的钳制上限；
 * 现改为事件实现（消除 {@code LocalPlayer.aiStep} 客户端移动热点的注入）：
 * <ul>
 *   <li>{@code aiStep} 中 {@code input.tick(isMovingSlowly(), f)} 之后紧接
 *       {@code ForgeHooksClient.onMovementInputUpdate}，触发
 *       {@link MovementInputUpdateEvent}（反编译源码实证），且事件内可改写已缩放后的
 *       {@link Input#forwardImpulse}/{@link Input#leftImpulse}。</li>
 *   <li>等级 ≤ 4：{@code f = 0.3 + 0.15×等级 < 1.0}，钳制前后相同，无需补偿，与 mixin
 *       （不改）逐位一致。</li>
 *   <li>等级 ≥ 5：把钳制前的真实倍率 {@code mult = 0.3 + 0.15×等级} 乘回 impulse，
 *       与 mixin 移除钳制上限后的结果逐位一致。</li>
 * </ul>
 * 服务端不加载本类（{@code @Mod.EventBusSubscriber(value = Dist.CLIENT)}），避免引用
 * {@code LocalPlayer}/{@code Input} 等客户端类导致服务端启动失败。
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class SwiftSneakHandler {

    @SubscribeEvent
    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        if (!Config.isLoaded()) {
            return;
        }
        if (!(event.getEntity() instanceof LocalPlayer player)) {
            return;
        }
        // input.tick 仅当 isMovingSlowly() 为 true 时才用 f 缩放 impulse，条件必须一致
        if (!player.isMovingSlowly()) {
            return;
        }
        // getSneakingSpeedBonus = 等级 × 0.15F；0.3 + 0.15×等级 > 1.0 即等级 ≥ 5
        float bonus = EnchantmentHelper.getSneakingSpeedBonus(player);
        if (bonus <= 0.7F) {
            return;
        }
        float mult = 0.3F + bonus;
        Input input = event.getInput();
        input.forwardImpulse *= mult;
        input.leftImpulse *= mult;
    }
}
