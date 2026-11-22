package dev.newenchantmentsystem.api;

import dev.newenchantmentsystem.init.ModEnchantments;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 高跳（High Jump）的效果实现。
 *
 * <p><b>跳跃高度</b>：监听 {@link LivingJumpEvent}（在 {@code LivingEntity.jumpFromGround} 设置
 * 初速度之后、由 {@code ForgeHooks.onLivingJump} 触发）：把 y 初速度放大为 {@code ×√(1+b)}，
 * b = 每级百分比 × 等级。换算依据：跳跃高度 h = v²/2g（重力恒定），初速 ×√(1+b) 恰好使高度
 * ×(1+b)，即配置的每级 +10% 是<b>高度</b> +10% 而非初速度 +10%（初速 +10% 会带来 +21% 高度）。
 *
 * <p><b>摔落安全距离</b>：监听 {@link LivingFallEvent}。Forge 的
 * {@code LivingEntity.causeFallDamage} 开头先执行 {@code ForgeHooks.onLivingFall}，并把事件
 * 修改后的 distance/damageMultiplier 写回方法参数再传入 {@code calculateFallDamage}，因此在
 * 事件里把摔落距离减去 {@code 3.0 × b}（等价于把安全距离从 3.0 放大到 {@code 3.0 × (1 + b)}）。
 * 该修改发生在两条摔落伤害路径<b>分叉之前</b>：
 * <ul>
 *   <li>原版公式：{@code ceil((d' − 3.0 − 跳跃提升) × 倍率)}，d' = d − 3.0×b，与
 *       {@code ceil((d − 3.0×(1+b) − 跳跃提升) × 倍率)} 恒等；</li>
 *   <li>新护甲系统的质量公式：其 {@code Mth.ceil @Redirect} 与原版共用同一个传入距离
 *       （其 javadoc 注明 "已含 LivingFallEvent 的修改"），安全区随之拓宽 3.0×b 格，
 *       质量公式其余部分完整保留。</li>
 * </ul>
 * 两条路径同时生效、互相叠加，而非由本模组接管/替换其中一条。
 *
 * <p>客户端与服务端均处理跳跃：两端修改一致，客户端本地跳跃预测不闪回。
 * 对任意穿附魔靴子的生物生效（mob 穿附魔靴也会跳得更高）。
 */
public class HighJumpHandler {

    private HighJumpHandler() {
    }

    @SubscribeEvent
    public static void onLivingJump(LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        int level = entity.getItemBySlot(EquipmentSlot.FEET).getEnchantmentLevel(ModEnchantments.HIGH_JUMP.get());
        if (level <= 0) {
            return;
        }
        double bonus = Config.HIGH_JUMP_JUMP_HEIGHT_BONUS.get() * level;
        if (bonus <= 0.0D) {
            return;
        }
        Vec3 motion = entity.getDeltaMovement();
        entity.setDeltaMovement(motion.x, motion.y * Math.sqrt(1.0D + bonus), motion.z);
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!Config.isLoaded()) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (entity.getType().is(EntityTypeTags.FALL_DAMAGE_IMMUNE)) {
            return;
        }
        int level = entity.getItemBySlot(EquipmentSlot.FEET).getEnchantmentLevel(ModEnchantments.HIGH_JUMP.get());
        if (level <= 0) {
            return;
        }
        double bonus = Config.HIGH_JUMP_FALL_SAFE_DISTANCE_BONUS.get() * level;
        if (bonus <= 0.0D) {
            return;
        }
        // 安全距离 3.0 → 3.0 × (1 + bonus)，等价于把摔落距离减去 3.0 × bonus。
        // 仅当距离大于原版安全距离 3.0 时才改写：d ≤ 3.0 时原版本就无伤，改写会破坏
        // 与原版公式在 3.0 < d ≤ 3.0×(1+bonus) 区间的等价性（必须改写才能保证该区间无伤）。
        float distance = event.getDistance();
        if (distance > 3.0F) {
            event.setDistance(distance - 3.0F * (float) bonus);
        }
    }
}
