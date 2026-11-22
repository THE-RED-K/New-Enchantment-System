package dev.newenchantmentsystem.api;

import dev.newenchantmentsystem.init.ModEnchantments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * 附魔规则统一门面（facade）——将禁用（ban）与互斥（exclusion）的<b>注册</b>与<b>查询</b>
 * 收敛到单一入口。外部代码（模组主类、mixin、事件处理器）只需依赖本类，无需关心底层
 * {@link EnchantmentBanRegister} / {@link EnchantmentExclusionRegister} 的差异。
 * <p>
 * 两个内置附魔（暴击之刃、魔法保护）及全部内置规则的注册集中在
 * {@link #registerAllDefaults()}，一次调用即可完成"注册所有能注册的内容"。
 * <p>
 * 覆盖的检查来源（注入点见 {@code EnchantmentMixin} / {@code AnvilMenuBanMixin}）：
 * <ul>
 *   <li><b>禁用</b>：附魔台、战利品、村民交易（{@code isDiscoverable} /
 *       {@code isTradeable}）与铁砧合并（{@code AnvilMenuBanMixin}）；</li>
 *   <li><b>互斥</b>：铁砧、附魔台多附魔、{@code /enchant} 指令
 *       （{@code checkCompatibility}，一处拦截全部来源）。</li>
 * </ul>
 */
public final class EnchantmentRules {

    private EnchantmentRules() {
    }

    // ==================== 一键注册 ====================

    /**
     * 一键注册所有内置默认规则（幂等——互斥对按注册名规范化去重，重复调用安全）。
     * <p>
     * 应在配置加载后（Mod 构造器或 {@code FMLCommonSetupEvent}）调用一次。当前内置规则：
     * <ul>
     *   <li>自动冶炼 ↔ 精准采集（烧炼会破坏"保留原方块"语义）；</li>
     *   <li>暴击之刃 ↔ 横扫之刃（同为武器附魔，互斥）；</li>
     *   <li>魔法保护 ↔ 保护 / 火焰保护 / 爆炸保护 / 弹射物保护
     *       （摔落保护除外，可与魔法保护共存）。</li>
     * </ul>
     * 注：经验修补 / 火焰保护的默认 ban 由配置 {@code enableMending} /
     * {@code enableFireProtection} 驱动（查询时动态判定），无需在此注册。
     */
    public static void registerAllDefaults() {
        // 自动冶炼 ↔ 精准采集（烧炼会破坏"保留原方块"语义）
        registerExclusive(ModEnchantments.AUTO_SMELTING.get(), Enchantments.SILK_TOUCH);
        // 暴击之刃 ↔ 横扫之刃
        registerExclusive(ModEnchantments.CRITICAL_BLADE.get(), Enchantments.SWEEPING_EDGE);
        // 魔法保护 ↔ 四系保护（摔落保护除外）
        registerExclusive(ModEnchantments.MAGIC_PROTECTION.get(), Enchantments.ALL_DAMAGE_PROTECTION);
        registerExclusive(ModEnchantments.MAGIC_PROTECTION.get(), Enchantments.FIRE_PROTECTION);
        registerExclusive(ModEnchantments.MAGIC_PROTECTION.get(), Enchantments.BLAST_PROTECTION);
        registerExclusive(ModEnchantments.MAGIC_PROTECTION.get(), Enchantments.PROJECTILE_PROTECTION);
    }

    // ==================== 注册转发 ====================

    /** 按附魔注册名（如 "minecraft:sharpness"）注册 ban。 */
    public static void registerBanned(String enchantmentId) {
        EnchantmentBanRegister.registerBanned(enchantmentId);
    }

    /** 按附魔注册名注册 ban。 */
    public static void registerBanned(ResourceLocation enchantmentId) {
        EnchantmentBanRegister.registerBanned(enchantmentId);
    }

    /** 按附魔实例注册 ban（内部取注册名）。 */
    public static void registerBanned(Enchantment enchantment) {
        EnchantmentBanRegister.registerBanned(enchantment);
    }

    /** 按附魔注册名注册互斥对（无序）。 */
    public static void registerExclusive(String enchantmentA, String enchantmentB) {
        EnchantmentExclusionRegister.registerExclusive(enchantmentA, enchantmentB);
    }

    /** 按附魔注册名注册互斥对（无序）。 */
    public static void registerExclusive(ResourceLocation enchantmentA, ResourceLocation enchantmentB) {
        EnchantmentExclusionRegister.registerExclusive(enchantmentA, enchantmentB);
    }

    /** 按附魔实例注册互斥对（无序）。 */
    public static void registerExclusive(Enchantment enchantmentA, Enchantment enchantmentB) {
        EnchantmentExclusionRegister.registerExclusive(enchantmentA, enchantmentB);
    }

    // ==================== 统一查询 ====================

    /** 该附魔是否被 ban（含配置驱动的默认 ban）。 */
    public static boolean isBanned(Enchantment enchantment) {
        return EnchantmentBanRegister.isBanned(enchantment);
    }

    /** 该附魔注册名是否被 ban（含配置驱动的默认 ban）。 */
    public static boolean isBanned(ResourceLocation enchantmentId) {
        return EnchantmentBanRegister.isBanned(enchantmentId);
    }

    /** 该附魔注册名是否被 ban（非法 ID 返回 false）。 */
    public static boolean isBanned(String enchantmentId) {
        ResourceLocation id = ResourceLocation.tryParse(enchantmentId);
        return id != null && EnchantmentBanRegister.isBanned(id);
    }

    /** 两个附魔是否互斥（无序，方向无关）。 */
    public static boolean isExclusive(Enchantment enchantmentA, Enchantment enchantmentB) {
        return EnchantmentExclusionRegister.isExclusive(enchantmentA, enchantmentB);
    }

    /** 两个附魔注册名是否互斥（无序，方向无关）。 */
    public static boolean isExclusive(ResourceLocation enchantmentA, ResourceLocation enchantmentB) {
        return EnchantmentExclusionRegister.isExclusive(enchantmentA, enchantmentB);
    }

    /**
     * 组合查询：目标物品能否接受附加物上的该附魔（ban + 互斥一次判定）。
     * 任一条件不满足即返回 {@code false}：
     * <ul>
     *   <li>目标与附加物为空、或指向同一附魔；</li>
     *   <li>附加物上的附魔被 ban；</li>
     *   <li>目标物品已有附魔与附加物附魔互斥。</li>
     * </ul>
     */
    public static boolean canApply(Enchantment target, Enchantment addition) {
        if (target == null || addition == null || target == addition) {
            return false;
        }
        return !isBanned(addition) && !isExclusive(target, addition);
    }

    /** 是否存在任何规则（ban 或互斥）。用于快速跳过查询，零规则时零开销。 */
    public static boolean hasRules() {
        return EnchantmentBanRegister.hasBannedEnchantments()
                || EnchantmentExclusionRegister.hasExclusions();
    }
}
