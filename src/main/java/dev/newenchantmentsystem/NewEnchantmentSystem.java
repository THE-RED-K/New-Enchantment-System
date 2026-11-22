package dev.newenchantmentsystem;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import dev.newenchantmentsystem.api.AutoSmeltingHandler;
import dev.newenchantmentsystem.api.AutoSmeltingModifier;
import dev.newenchantmentsystem.api.BaneOfArthropodsHandler;
import dev.newenchantmentsystem.api.Config;
import dev.newenchantmentsystem.api.BulwarkHandler;
import dev.newenchantmentsystem.api.EndVeilHandler;
import dev.newenchantmentsystem.api.EverlastingHandler;
import dev.newenchantmentsystem.api.CriticalBladeHandler;
import dev.newenchantmentsystem.api.EfficiencyHandler;
import dev.newenchantmentsystem.api.LevelXpCommand;
import dev.newenchantmentsystem.api.EnchantmentBanHandler;
import dev.newenchantmentsystem.api.EnchantmentRules;
import dev.newenchantmentsystem.api.GoldenGlitterHandler;
import dev.newenchantmentsystem.api.HighJumpHandler;
import dev.newenchantmentsystem.api.LifestealHandler;
import dev.newenchantmentsystem.api.NightVisionHandler;
import dev.newenchantmentsystem.api.PoisonWitherHandler;
import dev.newenchantmentsystem.api.StableFootingHandler;
import dev.newenchantmentsystem.init.ModEnchantments;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

// 此值须与 gradle.properties 中的 mod_id 及 META-INF/mods.toml 中的 modId 一致
@Mod(NewEnchantmentSystem.MOD_ID)
public class NewEnchantmentSystem {
    // 在公共位置定义 mod id 供全项目引用
    public static final String MOD_ID = "new_enchantment_system";

    /** 战利品修改器注册表（自动冶炼等，1.20.1 为 Codec 体系）。
     *  必须用 Keys（ResourceKey）形式：GLOBAL_LOOT_MODIFIER_SERIALIZERS 是 Supplier，
     *  在 NewRegistryEvent 触发前调用 get() 会返回 null（类加载期必然触发），导致 DeferredRegister NPE。 */
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLM =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MOD_ID);

    private static final Logger LOGGER = LogUtils.getLogger();

    @SuppressWarnings({"deprecation", "removal"}) // ModLoadingContext.get() 在 1.20.1 为唯一标准用法（新版本才标记弃用/移除）
    public NewEnchantmentSystem(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // 注册运行时配置文件（首次启动自动生成 config/newenchantmentsystem-common.toml）
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // 注册自定义附魔（暴击之刃等）
        ModEnchantments.ENCHANTMENTS.register(modEventBus);

        // 战利品修改器：自动冶炼（破坏方块掉落熔炼产物，data JSON 启用）
        GLM.register("auto_smelting", () -> AutoSmeltingModifier.CODEC);
        GLM.register(modEventBus);

        // 在此注册你的 DeferredRegister，例如：
        // DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
        // ITEMS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        // 注册服务端/游戏事件监听
        MinecraftForge.EVENT_BUS.register(this);
        // 被禁用原版附魔清除器（enableMending / enableFireProtection=false 时生效）
        MinecraftForge.EVENT_BUS.register(EnchantmentBanHandler.class);
        // 等级经验系统指令（/sep X）
        MinecraftForge.EVENT_BUS.register(LevelXpCommand.class);
        // 效率附魔自定义挖掘速度加成（E+ = coefficient * level + constant）
        MinecraftForge.EVENT_BUS.register(EfficiencyHandler.class);
        // 暴击之刃暴击伤害加成（每级 +25%，基于 Forge 官方 CriticalHitEvent）
        MinecraftForge.EVENT_BUS.register(CriticalBladeHandler.class);
        // 金光闪闪：携带者亮出真金质物品时激起周围猪灵敌意
        MinecraftForge.EVENT_BUS.register(GoldenGlitterHandler.class);
        // 自动冶炼：破坏方块时掉落物替换为熔炼产物（战利品修改器替换掉落 + BreakEvent 结算经验）
        MinecraftForge.EVENT_BUS.register(AutoSmeltingHandler.class);
        // 夜视：戴上附魔头盔每 0.5s 刷新 15s 夜视效果
        MinecraftForge.EVENT_BUS.register(NightVisionHandler.class);
        // 节肢杀手：剑挖掘蜘蛛网时每级 +10% 速度
        MinecraftForge.EVENT_BUS.register(BaneOfArthropodsHandler.class);
        // 末地面纱：防末影人注视敌意（EnderManAngerEvent）+ 用末影珍珠/末影之眼挑衅招致敌意
        MinecraftForge.EVENT_BUS.register(EndVeilHandler.class);
        // 生命窃取：近战攻击吸血（仅主目标，横扫副目标由 SweepTargetTracker 标记排除）
        MinecraftForge.EVENT_BUS.register(LifestealHandler.class);
        // 永恒：带附魔的物品实体不因寿命超时自然消失（取消 ItemExpireEvent 续命）
        MinecraftForge.EVENT_BUS.register(EverlastingHandler.class);
        // 中毒/凋零：近战命中附加 10s×等级 的 I 级效果（含横扫副目标）
        MinecraftForge.EVENT_BUS.register(PoisonWitherHandler.class);
        // 堡垒：举盾格挡时免疫击退（取消 LivingKnockBackEvent）
        MinecraftForge.EVENT_BUS.register(BulwarkHandler.class);
        // 高跳：跳跃高度 +10%/级（LivingJumpEvent 改 y 初速，h = v²/2g 换算）
        MinecraftForge.EVENT_BUS.register(HighJumpHandler.class);
        // 稳定立足：悬空时消除原版 f /= 5.0F 挖掘惩罚（PlayerEvent.BreakSpeed）
        MinecraftForge.EVENT_BUS.register(StableFootingHandler.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // 在此编写需要 client + server 双方环境共同执行的初始化逻辑
        //
        // 档位 / 自定义 maxLevel 注册（EnchantmentTierRegister）示例：
        //   EnchantmentTierRegister.registerTier("minecraft:sharpness", EnchantmentTier.HIGH);
        //   EnchantmentTierRegister.registerTier(ModEnchantments.CRITICAL_BLADE.get(), EnchantmentTier.LOW);
        // 自定义档位（独立的 maxLevel，优先于档位配置，如给某附魔单独定 6 级曲线）：
        //   EnchantmentTierRegister.registerCustomTier("modid:my_enchant", 6);
        //   EnchantmentTierRegister.registerCustomTier(myEnchantment, 6);
        // 注册后 getMaxLevel() 与铁砧成本会立即按注册值生效，无需改动其余逻辑。
        //
        // 附魔禁用（EnchantmentBanRegistry）示例——ban 的附魔在所有来源消失：
        //   EnchantmentBanRegistry.registerBanned("minecraft:sharpness");
        //   EnchantmentBanRegistry.registerBanned(ModEnchantments.CRITICAL_BLADE.get());
        // 覆盖：附魔台、战利品、村民交易、铁砧（书无法敲上）、创造模式附魔书展示，
        // 且玩家背包中已存在的 ban 附魔会被自动清扫。
        // 内置经验修补/火焰保护由配置 enableMending / enableFireProtection 驱动（默认 ban）。
        //
        // 统一入口（EnchantmentRules）——注册 / 查询全部走此门面：
        //   EnchantmentRules.registerBanned("minecraft:sharpness");      // ban 附魔
        //   EnchantmentRules.registerExclusive("minecraft:a", "modid:b"); // 互斥对
        //   EnchantmentRules.isBanned(...) / isExclusive(...) / canApply(...)
        // 一键注册所有内置规则（互斥：暴击之刃↔横扫之刃、魔法保护↔四系保护<摔落除外>），幂等可重复调用
        EnchantmentRules.registerAllDefaults();
        LOGGER.info("{} config loaded.", MOD_ID);
    }
}
