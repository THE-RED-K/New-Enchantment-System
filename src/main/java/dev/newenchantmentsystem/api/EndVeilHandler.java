package dev.newenchantmentsystem.api;

import dev.newenchantmentsystem.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.EnderManAngerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/**
 * 末地面纱（End Veil）附魔的效果处理（纯 Forge 事件，无 Mixin）。
 * <p>
 * 行为分两条：
 * <ul>
 *   <li><b>防敌意</b>：携带本附魔物品的玩家看向末影人时，末影人不会对携带者产生敌意。
 *       原版 {@code EnderMan.isLookingAtMe} 首行调用
 *       {@code ForgeHooks.shouldSuppressEnderManAnger}，该钩子会在检测到注视前 post
 *       {@link EnderManAngerEvent}（{@code @Cancelable}）——取消事件即令
 *       {@code isLookingAtMe} 返回 false（等价于玩家戴着南瓜头）。</li>
 *   <li><b>挑衅破绽</b>：携带者在末影人 16 格范围内使用末影珍珠 / 末影之眼时，
 *       范围内所有末影人立即对携带者产生敌意（{@code EnderMan.setTarget}），无视面纱。
 *       实现为监听 {@link PlayerInteractEvent.RightClickItem}（服务端，物品实际使用前
 *       触发）并识别手持的 {@link Items#ENDER_PEARL} / {@link Items#ENDER_EYE}。</li>
 * </ul>
 */
public class EndVeilHandler {

    /** 挑衅生效半径：16 格。 */
    private static final double PROVOKE_RANGE = 16.0D;

    private EndVeilHandler() {
    }

    /**
     * 玩家身上任一装备/手持槽位（含主手、副手）是否带有末地面纱附魔。
     */
    public static boolean hasEndVeil(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.getEnchantmentLevel(ModEnchantments.END_VEIL.get()) > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 防敌意：带末地面纱的玩家看向末影人眼睛时不会触发末影人敌意。
     */
    @SubscribeEvent
    public static void onEnderManAnger(EnderManAngerEvent event) {
        if (hasEndVeil(event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    /**
     * 挑衅：带末地面纱的玩家在末影人 16 格范围内使用末影珍珠 / 末影之眼时，
     * 范围内末影人立即对玩家产生敌意（无视面纱）。仅在服务端结算。
     */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (!stack.is(Items.ENDER_PEARL) && !stack.is(Items.ENDER_EYE)) {
            return;
        }
        if (!hasEndVeil(player)) {
            return;
        }
        AABB box = AABB.ofSize(player.position(), PROVOKE_RANGE * 2, PROVOKE_RANGE * 2, PROVOKE_RANGE * 2);
        List<EnderMan> endermen = player.level().getEntitiesOfClass(EnderMan.class, box,
                e -> e.distanceToSqr(player) <= PROVOKE_RANGE * PROVOKE_RANGE);
        for (EnderMan enderman : endermen) {
            enderman.setTarget(player);
        }
    }
}
