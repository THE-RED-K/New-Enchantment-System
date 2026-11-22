package dev.newenchantmentsystem.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 附魔禁用（ban）注册表——统一管理"被禁用的附魔"，并作为所有来源过滤的单一查询入口。
 * <p>
 * 一个附魔被 ban 后，在以下所有来源中均不再出现：
 * <ul>
 *   <li><b>附魔台</b>：{@code isDiscoverable()} 返回 {@code false}（{@code EnchantmentMixin}）；</li>
 *   <li><b>战利品随机附魔</b>：{@code EnchantmentHelper.getAvailableEnchantmentResults} 依赖
 *       {@code isDiscoverable()}，故随战利品生成的附魔书/物品同样被过滤；</li>
 *   <li><b>村民交易</b>：图书管理员附魔书交易（{@code VillagerTrades.EnchantBookForEmeralds}）
 *       依赖 {@code isTradeable()}，本注册表同样使其返回 {@code false}（{@code EnchantmentMixin}）；</li>
 *   <li><b>铁砧</b>：ban 附魔书无法将 ban 附魔敲上物品（{@code AnvilMenuBanMixin}，
 *       isDiscoverable/isTradeable 均不影响铁砧合并路径，必须在此精确拦截）；</li>
 *   <li><b>背包清扫</b>：已存在的 ban 附魔周期性从玩家背包移除（{@code EnchantmentBanHandler}）。</li>
 * </ul>
 * <p>
 * 内置两个由配置驱动的默认 ban（{@code enableMending} / {@code enableFireProtection}），
 * 配置开启即自动解除；外部附魔（含本模组新附魔与第三方附魔）通过
 * {@link #registerBanned} 系列方法注册，注册即生效（不受配置影响）。
 * 注册应在配置加载后完成（Mod 构造器或 {@code FMLCommonSetupEvent}）。
 */
public final class EnchantmentBanRegister {

    /** 注册表中所有被 ban 的附魔注册名。 */
    private static final Set<ResourceLocation> BANNED = new HashSet<>();

    private static final ResourceLocation MENDING_ID = ResourceLocation.tryParse("minecraft:mending");
    private static final ResourceLocation FIRE_PROTECTION_ID = ResourceLocation.tryParse("minecraft:fire_protection");

    private EnchantmentBanRegister() {
    }

    // ==================== 注册 ====================

    /** 按附魔注册名（如 "minecraft:sharpness"）注册 ban。 */
    public static void registerBanned(String enchantmentId) {
        BANNED.add(requireId(enchantmentId));
    }

    /** 按附魔注册名注册 ban。 */
    public static void registerBanned(ResourceLocation enchantmentId) {
        BANNED.add(requireId(enchantmentId));
    }

    /** 按附魔实例注册 ban（内部取 {@link ForgeRegistries#ENCHANTMENTS} 注册名）。 */
    public static void registerBanned(Enchantment enchantment) {
        BANNED.add(requireKey(enchantment));
    }

    // ==================== 查询 ====================

    /**
     * 该附魔是否被 ban。配置未加载时恒返回 {@code false}（注册阶段按原版放行），
     * 配置加载后自动开始生效，无需重启。
     */
    public static boolean isBanned(Enchantment enchantment) {
        if (!Config.isLoaded()) {
            return false;
        }
        ResourceLocation key = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        return key != null && isBanned(key);
    }

    /** 该附魔注册名是否被 ban（含配置驱动的内置默认 ban）。 */
    public static boolean isBanned(ResourceLocation enchantmentId) {
        if (!Config.isLoaded()) {
            return false;
        }
        if (BANNED.contains(enchantmentId)) {
            return true;
        }
        // 配置驱动的默认 ban：enableMending / enableFireProtection = false 时生效，开启即解除
        if (!Config.ENABLE_MENDING.get() && MENDING_ID.equals(enchantmentId)) {
            return true;
        }
        return !Config.ENABLE_FIRE_PROTECTION.get() && FIRE_PROTECTION_ID.equals(enchantmentId);
    }

    /** 当前是否存在任何被 ban 的附魔（含配置驱动的默认 ban）。用于快速跳过清扫等逻辑。 */
    public static boolean hasBannedEnchantments() {
        if (!Config.isLoaded()) {
            return false;
        }
        if (!BANNED.isEmpty()) {
            return true;
        }
        return !Config.ENABLE_MENDING.get() || !Config.ENABLE_FIRE_PROTECTION.get();
    }

    /** 返回注册表中所有被 ban 的附魔注册名（不可变视图，不含配置驱动的默认 ban）。 */
    public static Set<ResourceLocation> getBannedIds() {
        return Collections.unmodifiableSet(BANNED);
    }

    // ==================== 校验辅助 ====================

    private static ResourceLocation requireId(String enchantmentId) {
        if (enchantmentId == null || enchantmentId.isEmpty()) {
            throw new IllegalArgumentException("Enchantment id must not be null or empty");
        }
        ResourceLocation id = ResourceLocation.tryParse(enchantmentId);
        if (id == null) {
            throw new IllegalArgumentException("Invalid enchantment id: " + enchantmentId);
        }
        return id;
    }

    private static ResourceLocation requireId(ResourceLocation enchantmentId) {
        if (enchantmentId == null) {
            throw new NullPointerException("Enchantment id must not be null");
        }
        return enchantmentId;
    }

    private static ResourceLocation requireKey(Enchantment enchantment) {
        if (enchantment == null) {
            throw new NullPointerException("Enchantment must not be null");
        }
        ResourceLocation key = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        if (key == null) {
            throw new IllegalArgumentException("Enchantment is not registered: " + enchantment);
        }
        return key;
    }
}
