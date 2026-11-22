package dev.newenchantmentsystem.api;

import dev.newenchantmentsystem.init.ModEnchantments;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 中毒（Poison）与凋零（Wither）附魔的效果处理。
 * <p>
 * 监听 {@link LivingDamageEvent}（护甲减免后的最终伤害，仅在服务端结算）：
 * 玩家用主手带中毒 / 凋零附魔的武器造成近战伤害时，为目标附加
 * {@code 10s × 附魔等级} 的 <b>I 级</b> 对应效果。
 * <ul>
 *   <li><b>仅限近战</b>：要求 {@link DamageSource#getDirectEntity()} 与
 *       {@link DamageSource#getEntity()} 相同（即伤害直接来源于玩家本身，
 *       弓箭 / 火球等间接伤害源被排除）；</li>
 *   <li><b>横扫副目标同样生效</b>：横扫命中即造成伤害，故副目标也会被附加效果
 *       （与"造成伤害后附加"语义一致；若需仅主目标，仿 {@link LifestealHandler}
 *       用 {@link SweepTargetTracker#isSweepTarget} 排除即可）；</li>
 *   <li>免疫（亡灵免毒、凋灵免凋零等）由原版 {@code canBeAffected} 机制自然处理；
 *       已有同效果等级更高时 {@code addEffect} 不会降级覆盖。</li>
 * </ul>
 */
public class PoisonWitherHandler {

    /** 每级效果时长：10 秒（20 tick/s）。 */
    private static final int DURATION_TICKS_PER_LEVEL = 10 * 20;

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        DamageSource source = event.getSource();
        // 仅近战：直接伤害源必须与间接伤害源相同（即玩家本体造成，排除弓箭/火球等）
        if (source.getDirectEntity() != source.getEntity()) {
            return;
        }
        if (!(source.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide) {
            return;
        }
        ItemStack weapon = player.getMainHandItem();
        int poisonLevel = weapon.getEnchantmentLevel(ModEnchantments.POISON.get());
        int witherLevel = weapon.getEnchantmentLevel(ModEnchantments.WITHER.get());
        if (poisonLevel <= 0 && witherLevel <= 0) {
            return;
        }
        LivingEntity target = event.getEntity();
        if (poisonLevel > 0) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, DURATION_TICKS_PER_LEVEL * poisonLevel, 0));
        }
        if (witherLevel > 0) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, DURATION_TICKS_PER_LEVEL * witherLevel, 0));
        }
    }
}
