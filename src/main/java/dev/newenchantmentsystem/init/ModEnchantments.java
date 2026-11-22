package dev.newenchantmentsystem.init;

import dev.newenchantmentsystem.NewEnchantmentSystem;
import dev.newenchantmentsystem.enchantment.AutoSmeltingEnchantment;
import dev.newenchantmentsystem.enchantment.BulwarkEnchantment;
import dev.newenchantmentsystem.enchantment.CriticalBladeEnchantment;
import dev.newenchantmentsystem.enchantment.EndVeilEnchantment;
import dev.newenchantmentsystem.enchantment.EverlastingEnchantment;
import dev.newenchantmentsystem.enchantment.GoldenGlitterEnchantment;
import dev.newenchantmentsystem.enchantment.HighJumpEnchantment;
import dev.newenchantmentsystem.enchantment.LifestealEnchantment;
import dev.newenchantmentsystem.enchantment.MagicProtectionEnchantment;
import dev.newenchantmentsystem.enchantment.NightVisionEnchantment;
import dev.newenchantmentsystem.enchantment.PoisonEnchantment;
import dev.newenchantmentsystem.enchantment.StableFootingEnchantment;
import dev.newenchantmentsystem.enchantment.WitherEnchantment;
import dev.newenchantmentsystem.enchantment.WorkerExploitationEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 本模组的自定义附魔注册表。
 */
public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, NewEnchantmentSystem.MOD_ID);

    /** 暴击之刃：暴击伤害每级 +25%（乘算）。 */
    public static final RegistryObject<CriticalBladeEnchantment> CRITICAL_BLADE =
            ENCHANTMENTS.register("critical_blade", CriticalBladeEnchantment::new);

    /** 魔法保护：魔法/元素伤害每级 -3%（乘算，与其它保护加算）。 */
    public static final RegistryObject<MagicProtectionEnchantment> MAGIC_PROTECTION =
            ENCHANTMENTS.register("magic_protection", MagicProtectionEnchantment::new);

    /** 金光闪闪：猪灵将携带者视为金质；亮出真金质物品时立即对携带者产生敌意。固定 I 级，任意可附魔物品。 */
    public static final RegistryObject<GoldenGlitterEnchantment> GOLDEN_GLITTER =
            ENCHANTMENTS.register("golden_glitter", GoldenGlitterEnchantment::new);

    /** 末地面纱：防止末影人因玩家注视其眼睛而敌视；但在末影人 16 格内使用末影珍珠/末影之眼会立即招致敌意。固定 I 级。 */
    public static final RegistryObject<EndVeilEnchantment> END_VEIL =
            ENCHANTMENTS.register("end_veil", EndVeilEnchantment::new);

    /** 稳定立足：消除悬空挖掘减速（设计来自神化模组）。固定 I 级，仅靴子。 */
    public static final RegistryObject<StableFootingEnchantment> STABLE_FOOTING =
            ENCHANTMENTS.register("stable_footing", StableFootingEnchantment::new);

    /** 剥削：剪羊毛双倍产出，但每次对绵羊造成 2 点伤害（设计来自神化模组）。固定 I 级，仅剪刀。 */
    public static final RegistryObject<WorkerExploitationEnchantment> WORKER_EXPLOITATION =
            ENCHANTMENTS.register("worker_exploitation", WorkerExploitationEnchantment::new);

    /** 自动冶炼：破坏方块时掉落熔炼后的产物（设计来自神化模组）。固定 I 级，仅挖掘类工具。 */
    public static final RegistryObject<AutoSmeltingEnchantment> AUTO_SMELTING =
            ENCHANTMENTS.register("auto_smelting", AutoSmeltingEnchantment::new);

    /** 夜视：戴上附魔头盔持续获得夜视效果。固定 I 级，仅头盔。 */
    public static final RegistryObject<NightVisionEnchantment> NIGHT_VISION =
            ENCHANTMENTS.register("night_vision", NightVisionEnchantment::new);

    /** 永恒：带附魔的物品实体不会自然消失（5 分钟寿命超时除外），岩浆等销毁方式照常。固定 I 级，任意可附魔物品。 */
    public static final RegistryObject<EverlastingEnchantment> EVERLASTING =
            ENCHANTMENTS.register("everlasting", EverlastingEnchantment::new);

    /** 生命窃取：近战攻击吸血（5%×等级×实际伤害），仅主目标生效。低档，maxLevel 由 LOW_MAX_LEVEL 控制。 */
    public static final RegistryObject<LifestealEnchantment> LIFESTEAL =
            ENCHANTMENTS.register("lifesteal", LifestealEnchantment::new);

    /** 中毒：近战命中附加 10s×等级 的 I 级中毒效果（含横扫副目标）。低档，maxLevel 由 LOW_MAX_LEVEL 控制。 */
    public static final RegistryObject<PoisonEnchantment> POISON =
            ENCHANTMENTS.register("poison", PoisonEnchantment::new);

    /** 凋零：近战命中附加 10s×等级 的 I 级凋零效果（含横扫副目标）。低档，maxLevel 由 LOW_MAX_LEVEL 控制。 */
    public static final RegistryObject<WitherEnchantment> WITHER =
            ENCHANTMENTS.register("wither", WitherEnchantment::new);

    /** 堡垒：举盾格挡时免疫击退（设计来自万象模组）。固定 I 级，仅盾牌。 */
    public static final RegistryObject<BulwarkEnchantment> BULWARK =
            ENCHANTMENTS.register("bulwark", BulwarkEnchantment::new);

    /** 高跳：每级跳跃高度 +10%、摔落安全距离 +10%（安全距离在 calculateFallDamage 源头放大，
     * 原版与新护甲系统两条摔落伤害路径均生效）。中档，maxLevel 由 MEDIUM_MAX_LEVEL 控制，仅靴子。 */
    public static final RegistryObject<HighJumpEnchantment> HIGH_JUMP =
            ENCHANTMENTS.register("high_jump", HighJumpEnchantment::new);

}
