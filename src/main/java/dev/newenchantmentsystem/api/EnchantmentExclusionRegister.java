package dev.newenchantmentsystem.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 附魔互斥（exclusion）注册表——统一管理"不可共存于同一物品"的附魔对。
 * <p>
 * 一个互斥对注册后，两个附魔无法同时出现在同一物品上，覆盖所有原版检查路径：
 * <ul>
 *   <li><b>铁砧</b>：{@code AnvilMenu.createResult} 通过
 *       {@code EnchantmentHelper.isEnchantmentCompatible} 检查（其内部遍历调用
 *       {@code Enchantment.isCompatibleWith}）；</li>
 *   <li><b>附魔台多附魔</b>：{@code EnchantmentHelper.filterCompatibleEnchantments}
 *       同样调用 {@code isCompatibleWith} 过滤候选列表；</li>
 *   <li><b>{@code /enchant} 指令</b>：{@code EnchantCommand} 亦经
 *       {@code isCompatibleWith} 校验。</li>
 * </ul>
 * 而 {@code isCompatibleWith} 为 {@code final} 方法，内部双向调用
 * {@code Enchantment.checkCompatibility}（唯一非 final 虚方法）。{@code EnchantmentMixin}
 * 在 {@code checkCompatibility} 头注入本注册表查询，一处拦截即覆盖全部来源。
 * <p>
 * 互斥为<b>无序对</b>：注册 (A, B) 与 (B, A) 等价，任一方向命中即互斥。
 * 注册应在配置加载后完成（Mod 构造器或 {@code FMLCommonSetupEvent}）。
 */
public final class EnchantmentExclusionRegister {

    /** 注册表中所有互斥附魔对（规范化无序对）。 */
    private static final Set<Exclusion> EXCLUSIONS = new HashSet<>();

    private EnchantmentExclusionRegister() {
    }

    // ==================== 注册 ====================

    /** 按附魔注册名（如 "minecraft:sharpness"）注册互斥对。 */
    public static void registerExclusive(String enchantmentA, String enchantmentB) {
        EXCLUSIONS.add(new Exclusion(requireId(enchantmentA), requireId(enchantmentB)));
    }

    /** 按附魔注册名注册互斥对。 */
    public static void registerExclusive(ResourceLocation enchantmentA, ResourceLocation enchantmentB) {
        EXCLUSIONS.add(new Exclusion(requireId(enchantmentA), requireId(enchantmentB)));
    }

    /** 按附魔实例注册互斥对（内部取 {@link ForgeRegistries#ENCHANTMENTS} 注册名）。 */
    public static void registerExclusive(Enchantment enchantmentA, Enchantment enchantmentB) {
        EXCLUSIONS.add(new Exclusion(requireKey(enchantmentA), requireKey(enchantmentB)));
    }

    // ==================== 查询 ====================

    /** 两个附魔是否互斥（无序，方向无关）。未注册/未注册完成的附魔返回 false。 */
    public static boolean isExclusive(Enchantment enchantmentA, Enchantment enchantmentB) {
        if (enchantmentA == null || enchantmentB == null || enchantmentA == enchantmentB) {
            return false;
        }
        return isExclusive(
                ForgeRegistries.ENCHANTMENTS.getKey(enchantmentA),
                ForgeRegistries.ENCHANTMENTS.getKey(enchantmentB));
    }

    /** 两个附魔注册名是否互斥（无序，方向无关）。 */
    public static boolean isExclusive(ResourceLocation enchantmentA, ResourceLocation enchantmentB) {
        if (enchantmentA == null || enchantmentB == null || enchantmentA.equals(enchantmentB)) {
            return false;
        }
        return EXCLUSIONS.contains(new Exclusion(enchantmentA, enchantmentB));
    }

    /** 当前是否存在任何互斥规则。用于快速跳过查询。 */
    public static boolean hasExclusions() {
        return !EXCLUSIONS.isEmpty();
    }

    /** 返回所有互斥对的不可变视图。 */
    public static Set<Exclusion> getExclusions() {
        return Collections.unmodifiableSet(EXCLUSIONS);
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

    /**
     * 规范化无序对：构造时按注册名字典序排序，保证 (A, B) 与 (B, A) 命中同一规则。
     */
    public static final class Exclusion {
        private final ResourceLocation first;
        private final ResourceLocation second;

        Exclusion(ResourceLocation enchantmentA, ResourceLocation enchantmentB) {
            if (enchantmentA.compareTo(enchantmentB) <= 0) {
                this.first = enchantmentA;
                this.second = enchantmentB;
            } else {
                this.first = enchantmentB;
                this.second = enchantmentA;
            }
        }

        public ResourceLocation first() {
            return first;
        }

        public ResourceLocation second() {
            return second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Exclusion that)) {
                return false;
            }
            return first.equals(that.first) && second.equals(that.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }

        @Override
        public String toString() {
            return first + " <-> " + second;
        }
    }
}
