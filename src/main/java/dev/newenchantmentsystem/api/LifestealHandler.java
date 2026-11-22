package dev.newenchantmentsystem.api;

import dev.newenchantmentsystem.init.ModEnchantments;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 生命窃取（Lifesteal）附魔的效果处理。
 * <p>
 * 监听 {@link LivingDamageEvent}（护甲减免后的最终伤害，仅在服务端结算）：
 * 玩家用主手带生命窃取附魔的武器造成近战伤害时，回复
 * {@code 5% × 附魔等级 × 实际伤害} 点生命。
 * <ul>
 *   <li><b>仅限近战</b>：要求 {@link DamageSource#getDirectEntity()} 与
 *       {@link DamageSource#getEntity()} 相同（即伤害直接来源于玩家本身，
 *       弓箭 / 火球等间接伤害源被排除）；</li>
 *   <li><b>仅主目标</b>：横扫副目标（{@link SweepTargetTracker#isSweepTarget}）不吸血。
 *       横扫比例可 &gt;100%，故不以伤害大小判断主/副目标，而以横扫结算期间的
 *       瞬时标签精确区分；</li>
 *   <li>回复直接调用 {@link Player#heal}，不受饱食度影响。</li>
 * </ul>
 */
public class LifestealHandler {

    /** 吸血比例：每级 5%（回复量 = 5% × 等级 × 实际伤害）。 */
    private static final float HEAL_RATIO_PER_LEVEL = 0.05F;

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
        // 横扫副目标不吸血（横扫比例可 >100%，不能按伤害大小判断）
        if (SweepTargetTracker.isSweepTarget(event.getEntity())) {
            return;
        }
        ItemStack weapon = player.getMainHandItem();
        int level = weapon.getEnchantmentLevel(ModEnchantments.LIFESTEAL.get());
        if (level <= 0) {
            return;
        }
        float heal = event.getAmount() * HEAL_RATIO_PER_LEVEL * level;
        player.heal(heal);
    }
}
