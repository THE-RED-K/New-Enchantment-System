package dev.newenchantmentsystem.api;

import dev.newenchantmentsystem.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 稳定立足（Stable Footing）实现（设计来自神化模组 Apotheosis）：悬空时不再有挖掘速度惩罚。
 * <p>
 * 原版挖矿速度实现 {@code Player.getDigSpeed(BlockState, BlockPos)} 中，玩家悬空
 * （{@code !onGround()}）时挖掘速度会除以 5.0F；随后该方法末尾触发
 * {@link PlayerEvent.BreakSpeed}（见反编译源码：{@code f /= 5.0F} 之后紧接
 * {@code ForgeEventFactory.getBreakSpeed(this, pState, f, pos)}）。
 * <p>
 * 因此本 Handler 在事件中把已含悬空惩罚的速度乘回 5.0F，即为精确补偿——与迁移前的
 * {@code BlockStateBase#getDestroyProgress} RETURN 乘回方案数值完全一致，但消除了
 * {@code BlockStateBase}（挖掘类 mod 热点）处的字节码注入。
 * <ul>
 *   <li>客户端 HUD（{@code MultiPlayerGameMode}）与服务端破块（{@code ServerPlayerGameMode}）
 *       都经由 {@code Player.getDigSpeed} 计算挖掘速度，均会触发本事件，双端一致。</li>
 *   <li>不依赖 {@code Player.getDigSpeed} 的 MCP→SRG 映射条目（该条目在本工具链中缺失，
 *       直接 mixin 注入会因 refmap 缺失而静默失效），事件方案天然绕开此映射缺口。</li>
 * </ul>
 */
public class StableFootingHandler {

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!Config.isLoaded()) {
            return;
        }
        Player player = event.getEntity();
        // 已落地则原版没有悬空惩罚，无需补偿
        if (player.onGround()) {
            return;
        }
        if (player.getItemBySlot(EquipmentSlot.FEET)
                .getEnchantmentLevel(ModEnchantments.STABLE_FOOTING.get()) <= 0) {
            return;
        }
        // 事件在 getDigSpeed 的悬空 f /= 5.0F 之后触发，乘回 5.0F 精确抵消惩罚
        event.setNewSpeed(event.getNewSpeed() * 5.0F);
    }
}
