package dev.newenchantmentsystem.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 节肢杀手（Bane of Arthropods）对蜘蛛网的挖掘加速。
 * <p>
 * 带节肢杀手附魔的剑破坏蜘蛛网时，每级挖掘速度 +10%（乘算，与其它速度加成友好叠加）。
 * <ul>
 *   <li>仅当主手为剑（{@link SwordItem}）、目标方块为蜘蛛网（{@link Blocks#COBWEB}）、
 *       且剑上带节肢杀手附魔时生效；</li>
 *   <li>实现：监听 {@link PlayerEvent.BreakSpeed}（在 {@code Player.getDigSpeed} 末尾
 *       触发），基于事件当前速度 {@link PlayerEvent.BreakSpeed#getNewSpeed()} 乘算放大，
 *       与效率自定义公式等其它 BreakSpeed 监听器可叠加；</li>
 *   <li>乘算写法（而非覆盖为固定值），避免破坏其它模组的速度修正。</li>
 * </ul>
 */
public class BaneOfArthropodsHandler {

    /** 每级挖掘速度加成：+10%。 */
    private static final float SPEED_BONUS_PER_LEVEL = 0.1F;

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.isCanceled()) {
            return;
        }
        BlockState state = event.getState();
        if (!state.is(Blocks.COBWEB)) {
            return;
        }
        Player player = event.getEntity();
        ItemStack tool = player.getMainHandItem();
        // 仅限剑（节肢杀手可附于剑/斧，加速仅对剑生效）
        if (!(tool.getItem() instanceof SwordItem)) {
            return;
        }
        int level = tool.getEnchantmentLevel(Enchantments.BANE_OF_ARTHROPODS);
        if (level <= 0) {
            return;
        }
        // 每级 +10%，乘算叠加
        event.setNewSpeed(event.getNewSpeed() * (1.0F + SPEED_BONUS_PER_LEVEL * level));
    }
}
