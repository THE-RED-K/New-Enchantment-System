package dev.newenchantmentsystem.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

/**
 * 附魔档位 / 自定义 maxLevel 注册器。
 * <p>
 * 将"附魔 ID → 档位"与"附魔 ID → 自定义 maxLevel"的映射从
 * {@link EnchantmentTier#resolve} 的 instanceof 硬编码链中解耦出来，
 * 供本模组及第三方以数据驱动方式注册。两种注册方法：
 * <ul>
 *   <li>{@link #registerTier}：注册<b>标准档位</b>（{@link EnchantmentTier#LOW} /
 *       {@link EnchantmentTier#MEDIUM} / {@link EnchantmentTier#HIGH}），
 *       同时影响 maxLevel（档位配置 {@code lowMaxLevel / mediumMaxLevel / highMaxLevel}）
 *       与铁砧成本权重。</li>
 *   <li>{@link #registerCustomTier}：注册<b>自定义档位</b>——为附魔指定独立的
 *       maxLevel（自定义档位方法），不归属三档配置，优先于任何档位配置返回，
 *       适用于需要单独等级曲线的附魔。</li>
 * </ul>
 * <p>
 * 注册应在配置加载后、附魔生效前完成（Mod 构造器或 {@code FMLCommonSetupEvent}）；
 * 查询方（{@code TierMaxLevelMixin} / {@code TierOverrideMaxLevelMixin} /
 * {@link EnchantmentTier#resolve}）始终优先命中注册表，未注册的附魔回退原有
 * instanceof 判定。自定义档位仅影响 maxLevel，不改变铁砧成本档位；如需同时
 * 指定成本档位，可额外调用 {@link #registerTier}。
 */
public final class EnchantmentTierRegister {

    /** 附魔注册名 → 标准档位。 */
    private static final Map<ResourceLocation, EnchantmentTier> TIERS = new HashMap<>();

    /** 附魔注册名 → 自定义 maxLevel（自定义档位）。 */
    private static final Map<ResourceLocation, Integer> CUSTOM_MAX_LEVELS = new HashMap<>();

    private EnchantmentTierRegister() {
    }

    // ==================== 注册：标准档位 ====================

    /** 按附魔注册名（如 "minecraft:sharpness"）注册标准档位。 */
    public static void registerTier(String enchantmentId, EnchantmentTier tier) {
        TIERS.put(requireId(enchantmentId), requireTier(tier));
    }

    /** 按附魔实例注册标准档位（内部取 {@link ForgeRegistries#ENCHANTMENTS} 注册名）。 */
    public static void registerTier(Enchantment enchantment, EnchantmentTier tier) {
        TIERS.put(requireKey(enchantment), requireTier(tier));
    }

    // ==================== 注册：自定义档位（自定义 maxLevel） ====================

    /**
     * 按附魔注册名注册自定义档位：为附魔指定独立于三档配置的 maxLevel。
     * 命中后 {@code getMaxLevel()} 优先返回该值，配置调整不影响此附魔。
     */
    public static void registerCustomTier(String enchantmentId, int maxLevel) {
        CUSTOM_MAX_LEVELS.put(requireId(enchantmentId), requireLevel(maxLevel));
    }

    /** 按附魔实例注册自定义档位（自定义 maxLevel）。 */
    public static void registerCustomTier(Enchantment enchantment, int maxLevel) {
        CUSTOM_MAX_LEVELS.put(requireKey(enchantment), requireLevel(maxLevel));
    }

    // ==================== 查询 ====================

    /** 注册表查询：命中返回档位，未命中返回 {@code null}（调用方回退原判定）。 */
    public static EnchantmentTier getTier(Enchantment enchantment) {
        ResourceLocation key = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        return key == null ? null : TIERS.get(key);
    }

    /** 该附魔是否注册了自定义 maxLevel（自定义档位）。 */
    public static boolean hasCustomMaxLevel(Enchantment enchantment) {
        ResourceLocation key = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        return key != null && CUSTOM_MAX_LEVELS.containsKey(key);
    }

    /** 返回注册的自定义 maxLevel；未注册返回 -1（应配合 {@link #hasCustomMaxLevel} 使用）。 */
    public static int getCustomMaxLevel(Enchantment enchantment) {
        ResourceLocation key = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        return key == null ? -1 : CUSTOM_MAX_LEVELS.getOrDefault(key, -1);
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

    private static EnchantmentTier requireTier(EnchantmentTier tier) {
        if (tier == null) {
            throw new NullPointerException("Tier must not be null");
        }
        return tier;
    }

    private static int requireLevel(int maxLevel) {
        if (maxLevel < 1) {
            throw new IllegalArgumentException("Custom maxLevel must be >= 1, got " + maxLevel);
        }
        return maxLevel;
    }
}
