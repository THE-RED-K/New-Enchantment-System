package dev.newenchantmentsystem.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 被禁用附魔清除器（背包清扫）：周期性扫描玩家物品栏，将已存在的 ban 附魔移除
 * （"检测到就消失"）。判定统一走 {@link EnchantmentBanRegister#isBanned}，因此
 * 覆盖配置驱动的默认 ban（经验修补 Mending、火焰保护 Fire Protection）以及
 * 通过 {@link EnchantmentBanRegister#registerBanned} 注册的任意附魔。
 * 覆盖主背包、盔甲栏与副手，同时处理普通物品（Enchantments 标签）
 * 与附魔书（StoredEnchantments 标签）。
 */
public class EnchantmentBanHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // 配置未加载时不做任何处理，防止 get() 抛异常
        if (!Config.isLoaded()) {
            return;
        }
        if (!EnchantmentBanRegister.hasBannedEnchantments()) {
            return;
        }
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (player.level().isClientSide) {
            return;
        }
        // 按可配置的间隔扫描（默认 10 tick = 0.5 秒），机器卡可调大
        int scanInterval = Config.SCAN_INTERVAL.get();
        if (scanInterval <= 0 || player.tickCount % scanInterval != 0) {
            return;
        }

        Inventory inventory = player.getInventory();
        for (ItemStack stack : inventory.items) {
            purgeBanned(stack);
        }
        for (ItemStack stack : inventory.armor) {
            purgeBanned(stack);
        }
        for (ItemStack stack : inventory.offhand) {
            purgeBanned(stack);
        }
    }

    private static void purgeBanned(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        // 普通物品附魔（Enchantments 标签）。
        // getEnchantments 返回底层 Map，为安全起见拷贝后移除。
        Map<Enchantment, Integer> enchantments = new HashMap<>(EnchantmentHelper.getEnchantments(stack));
        boolean changed = false;
        Iterator<Map.Entry<Enchantment, Integer>> iterator = enchantments.entrySet().iterator();
        while (iterator.hasNext()) {
            if (EnchantmentBanRegister.isBanned(iterator.next().getKey())) {
                iterator.remove();
                changed = true;
            }
        }
        if (changed) {
            EnchantmentHelper.setEnchantments(enchantments, stack);
        }

        // 附魔书附魔（StoredEnchantments 标签）。
        // 1.20.1 中 EnchantedBookItem.getEnchantments 直接返回该标签的 ListTag，
        // 按其 id 移除对应项；getList 返回底层引用，修改后需写回。
        ListTag stored = EnchantedBookItem.getEnchantments(stack);
        boolean removedFromBook = false;
        for (int i = stored.size() - 1; i >= 0; i--) {
            CompoundTag entry = stored.getCompound(i);
            ResourceLocation id = ResourceLocation.tryParse(entry.getString("id"));
            if (id != null && EnchantmentBanRegister.isBanned(id)) {
                stored.remove(i);
                removedFromBook = true;
            }
        }
        if (removedFromBook) {
            stack.getOrCreateTag().put(EnchantedBookItem.TAG_STORED_ENCHANTMENTS, stored);
        }
    }
}
