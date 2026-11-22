package dev.newenchantmentsystem.mixin;

import dev.newenchantmentsystem.api.Config;
import dev.newenchantmentsystem.api.GoldenGlitterHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 金光闪闪（Golden Glitter）附魔的猪灵 AI 钩子。
 * <p>
 * 仅在原版判定不满足/需要反转时介入，纯静态注入（{@link PiglinAi} 为静态工具类）：
 * <ul>
 *   <li>{@code isWearingGold}：携带金光闪闪附魔的玩家被视为"穿金"——猪灵传感器
 *       （{@code PiglinSpecificSensor}）填充 {@code NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD}
 *       时直接调用本方法，因此携带者不会被列为可攻击目标（猪灵保持中立）。</li>
 *   <li>{@code isPlayerHoldingLovedItem}：携带者亮出真金时取消"手持想要物品"标记——
 *       避免猪灵同时进入"想要/交易"状态，确保敌意（愤怒）行为纯粹。</li>
 * </ul>
 * 敌意的主动触发（{@code PiglinAi.angerNearbyPiglins}）由非 Mixin 的
 * {@link GoldenGlitterHandler} 通过 Forge 事件完成。
 */
@Mixin(PiglinAi.class)
public abstract class PiglinAiMixin {

    @Inject(method = "isWearingGold", at = @At("RETURN"), cancellable = true)
    private static void nes$goldenGlitterIsWearingGold(LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.isLoaded() || cir.getReturnValueZ() || !(livingEntity instanceof Player player)) {
            return;
        }
        if (GoldenGlitterHandler.hasGoldenGlitter(player)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isPlayerHoldingLovedItem", at = @At("RETURN"), cancellable = true)
    private static void nes$goldenGlitterNotHoldingLovedItem(LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.isLoaded() || !cir.getReturnValueZ() || !(livingEntity instanceof Player player)) {
            return;
        }
        if (GoldenGlitterHandler.hasGoldenGlitter(player)) {
            cir.setReturnValue(false);
        }
    }
}
