package dev.newenchantmentsystem.api;

import dev.newenchantmentsystem.init.ModEnchantments;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 金光闪闪（Golden Glitter）附魔的效果处理（非 Mixin 部分）。
 * <p>
 * 行为分两条：
 * <ul>
 *   <li><b>视为金质</b>：携带本附魔物品的玩家被猪灵视为"穿金"。该判定由
 *       {@code PiglinAiMixin} 钩住 {@code PiglinAi.isWearingGold} 实现，本类只提供
 *       判定辅助方法 {@link #hasGoldenGlitter(Player)}。</li>
 *   <li><b>亮出真金即敌意</b>：携带者主/副手亮出真正的金质物品时，调用原版公开 API
 *       {@link PiglinAi#angerNearbyPiglins(Player, boolean)}，使 16 格内能看到携带者的
 *       猪灵立即进入 600 tick 的愤怒状态（立即对携带者产生敌意）。</li>
 * </ul>
 * <p>
 * 注意"真正的金质物品"判定使用原版 {@link ItemTags#PIGLIN_LOVED}（{@code #minecraft:piglin_loved}）
 * 标签——该标签在这里仅用于<b>识别玩家手中的原版金质物品</b>（金锭/金粒/金块等），
 * 而"附魔物品被视为金质"完全由附魔检测逻辑实现，未把任何物品写入或复用该标签，符合
 * "不通过复用原版猪灵喜欢标签来实现附魔效果"的约束。
 */
public class GoldenGlitterHandler {

    private GoldenGlitterHandler() {
    }

    /**
     * 玩家身上任一装备/手持槽位（含主手、副手）是否带有金光闪闪附魔。
     */
    public static boolean hasGoldenGlitter(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.getEnchantmentLevel(ModEnchantments.GOLDEN_GLITTER.get()) > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 玩家主手或副手是否持有"真正的金质物品"（原版猪灵视为金质的物品集合）。
     */
    public static boolean isHoldingRealGold(Player player) {
        return player.getMainHandItem().is(ItemTags.PIGLIN_LOVED)
                || player.getOffhandItem().is(ItemTags.PIGLIN_LOVED);
    }

    /**
     * 每 tick 检查：携带金光闪闪的玩家若在猪灵视野内亮出真正的金质物品，
     * 立即激起周围猪灵的敌意。仅在服务端处理。
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (player.level().isClientSide || !Config.isLoaded()) {
            return;
        }
        if (hasGoldenGlitter(player) && isHoldingRealGold(player)) {
            PiglinAi.angerNearbyPiglins(player, true);
        }
    }
}
