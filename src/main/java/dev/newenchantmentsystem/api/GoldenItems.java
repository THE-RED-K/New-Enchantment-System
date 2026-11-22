package dev.newenchantmentsystem.api;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 金质物品注册表（Golden Item Registry）。
 * <p>
 * 用于金质自动修补（Golden Mending）的候选判定。刻意不使用原版
 * {@code piglin_loved} 标签——该标签可能被其他模组复用（如猪灵交易、
 * 掉落物等），会造成误判。改为维护一份显式物品列表，默认包含原版
 * 全部金质装备/工具/武器；兼容性附属可通过 {@link #add(Item...)}
 * 注册自己的金质物品。
 */
public final class GoldenItems {

    private GoldenItems() {
    }

    private static final Set<Item> ITEMS = new LinkedHashSet<>();

    static {
        // 原版金质工具
        ITEMS.add(Items.GOLDEN_SWORD);
        ITEMS.add(Items.GOLDEN_SHOVEL);
        ITEMS.add(Items.GOLDEN_PICKAXE);
        ITEMS.add(Items.GOLDEN_AXE);
        ITEMS.add(Items.GOLDEN_HOE);
        // 原版金质盔甲
        ITEMS.add(Items.GOLDEN_HELMET);
        ITEMS.add(Items.GOLDEN_CHESTPLATE);
        ITEMS.add(Items.GOLDEN_LEGGINGS);
        ITEMS.add(Items.GOLDEN_BOOTS);
        // 原版金质马铠（无耐久，会被 isDamageableItem 过滤，仍保留以覆盖完整列表）
        ITEMS.add(Items.GOLDEN_HORSE_ARMOR);
    }

    /**
     * 注册额外的金质物品（供兼容性附属调用，通常在其初始化阶段执行）。
     *
     * @param items 要加入金质列表的物品，忽略 null
     */
    public static void add(Item... items) {
        Collections.addAll(ITEMS, items);
    }

    /**
     * 判断一个物品堆是否属于金质注册表（未修改列表本身，仅查表）。
     *
     * @param stack 物品堆，可为空
     * @return 是金质注册物品返回 true
     */
    public static boolean isGolden(ItemStack stack) {
        return !stack.isEmpty() && ITEMS.contains(stack.getItem());
    }
}
