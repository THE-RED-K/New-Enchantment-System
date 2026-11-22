package dev.newenchantmentsystem.mixin;

import net.minecraftforge.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * 条件混入插件：
 * <ul>
 *   <li>当 New-Armor-System（modid {@code new_armor_system}）已装载时，跳过
 *       {@link AnvilMenuCapMixin}，避免与护甲系统的 {@code AnvilMenuMixin} 在同一调用点
 *       （{@code ItemStack.getBaseRepairCost} / {@code DataSlot.get}）产生双重 @Redirect；</li>
 *   <li>仅当 Tetra（modid {@code tetra}）已装载时，应用 {@link TetraSweepingMixin}
 *       （Tetra 的实体横扫不经 {@code Player.attack} 横扫循环，需单独打副目标标签）与
 *       {@link TetraSweepFormulaMixin}（Tetra 横扫基础伤害公式重写，兼容本模组的
 *       横扫之刃改动），Tetra 未装时跳过，避免引用不存在的目标类。</li>
 * </ul>
 * 其余混入类不受影响，照常应用。
 * <p>
 * <b>时序约束</b>：本插件的 {@link #shouldApplyMixin(String, String)} 在 ModLauncher
 * 类变换阶段执行，远早于 FML 填充运行时 {@code net.minecraftforge.fml.ModList}。
 * 若在此处调用 {@code ModList.get().isLoaded(...)}，返回的列表为空/未初始化，
 * isLoaded 恒为 false，条件跳过将静默失效。必须使用早期 mod 列表
 * {@code FMLLoader.getLoadingModList()}。
 */
public class NewEnchantmentSystemMixinPlugin implements IMixinConfigPlugin {

    private static final String ANVIL_CAP_MIXIN = "dev.newenchantmentsystem.mixin.AnvilMenuCapMixin";
    private static final String ARMOR_MOD_ID = "new_armor_system";
    private static final String TETRA_SWEEPING_MIXIN = "dev.newenchantmentsystem.mixin.TetraSweepingMixin";
    private static final String TETRA_SWEEP_FORMULA_MIXIN = "dev.newenchantmentsystem.mixin.TetraSweepFormulaMixin";
    private static final String TETRA_MOD_ID = "tetra";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    /**
     * 在 Mixin 插件阶段检测 mod 是否已装载。
     * 不能用 {@code ModList.get()}（运行时列表此时未填充），必须用 FMLLoader 的早期加载列表。
     */
    private static boolean isModLoaded(String modid) {
        try {
            return FMLLoader.getLoadingModList().getModFileById(modid) != null;
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (ANVIL_CAP_MIXIN.equals(mixinClassName)) {
            return !isModLoaded(ARMOR_MOD_ID);
        }
        if (TETRA_SWEEPING_MIXIN.equals(mixinClassName) || TETRA_SWEEP_FORMULA_MIXIN.equals(mixinClassName)) {
            return isModLoaded(TETRA_MOD_ID);
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
