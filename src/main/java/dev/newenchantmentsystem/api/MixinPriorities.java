package dev.newenchantmentsystem.api;

/**
 * 本模组 Mixin 的优先级常量（单一事实来源）。
 *
 * <p><b>为什么"接管型"mixin 要用低优先级</b>：
 * <ol>
 *   <li>Mixin 按优先级<b>升序</b>应用 —— {@code MixinInfo#compareTo} 的字节码为
 *       {@code return this.priority - other.priority}（同级再按注册顺序 {@code order}），
 *       故优先级越低越早写入目标方法。</li>
 *   <li>一个方法被某个 mixin {@code @Overwrite} 之后，其它模组的注入
 *       （{@code @Inject} / {@code @ModifyVariable} / MixinExtras 的 {@code @WrapOperation} 等）
 *       能否落入该方法体，取决于 {@code InjectionPoint#checkPriority(mergedPriority, myPriority)}：
 *       其字节码等价于 {@code mergedPriority < myPriority} —— <b>注入方的优先级必须严格高于接管方</b>，
 *       否则在 PREINJECT 阶段抛出
 *       {@code InvalidInjectionException: ... cannot inject into ... merged by ... with priority ...}；
 *       由于 {@code injectors.defaultRequire = 1}，这会直接导致游戏启动崩溃。</li>
 *   <li>反过来，{@code @Overwrite} 之间的冲突是"<b>先写入者胜出</b>"：后写入者被
 *       {@code Method overwrite conflict ... Skipping method} 跳过。因此低优先级同时保证
 *       本模组的接管不会被别的 {@code @Overwrite} 取代 —— 两头都稳。</li>
 * </ol>
 *
 * <p><b>实测教训</b>（来自同作者 New-Armor-System 1.20.1 测试实例）：Apotheosis 7.4.8 的
 * {@code ItemStackMixin} 声明 {@code priority=500}，与旧 {@link #TAKE_OVER}(500) <b>平级</b> ⇒
 * {@code 500 < 500} 不成立 ⇒ 注入失败、启动崩溃（异常文本：{@code @At("INVOKE") on
 * ItemStack::swapDura ... cannot inject into ItemStack::m_220157_ ... merged by
 * dev.newarmorsystem.mixin.ItemStackMixin with priority 500}）。</p>
 *
 * <p><b>本模组现状（2026-11-22 起）</b>：本模组<b>没有任何 {@code @Overwrite}</b> ——
 * 目标方法体均未被整体接管，全部为注入式 mixin
 * （{@code @Inject} / {@code @Redirect} / {@code @ModifyArg}），原版锚点完整保留，
 * 因此<b>一律保持默认优先级 1000</b>，不消费本文件的常量。
 * 本文件随 New-Armor-System 移植而来，常量阶梯预留给将来可能出现的接管型 mixin。
 * （把只注入/重定向的 mixin 压低会让本模组自己的注入变成"底层"，反而可能被模组后写入的
 * {@code @Overwrite} 冲掉，故不作为默认做法。）
 *
 * <p><b>将来若新增 {@code @Overwrite}</b>：推荐取 {@link #TAKE_OVER_FINAL}
 * （{@code -Integer.MAX_VALUE}，最早写入，任何模组都不能取代或早于它），
 * 其它模组的注入仍可叠加其上（注入方优先级只需严格大于该值，恒成立）。
 * 升级到 {@link #TAKE_OVER_OVER}（250）以下各级仅在与某特定模组平级冲突时按需下调
 * （低于默认值即可让外部注入叠加）。</p>
 *
 * <p>Mixin 默认优先级是 1000，生态中绝大多数 mixin 配置都使用默认值（实测某大型整合包
 * 793 个配置：792 个 1000、1 个 {@code Integer.MAX_VALUE}，无一低于 1000）—— 这是
 * "低于默认值即可让外部注入叠加"的实测依据。</p>
 *
 * @author THEREDK
 */
public final class MixinPriorities {

    /**
     * 接管型（{@code @Overwrite}）mixin 的优先级：低于 Mixin 默认值 1000，
     * 确保其它模组的注入仍可落入本模组接管后的方法体，同时本模组的接管不被取代。
     */
    public static final int TAKE_OVER = 500;

    public static final int TAKE_OVER_OVER = 250;

    public static final int TAKE_OVER_OVER_OVER = 0;

    public static final int TAKE_OVER_OVER_OVER_OVER = -250;

    public static final int TAKE_OVER_OVER_OVER_OVER_OVER = -500;

    public static final int TAKE_OVER_OVER_OVER_OVER_OVER_OVER = -750;

    public static final int TAKE_OVER_OVER_OVER_OVER_OVER_OVER_OVER = -1000;

    public static final int TAKE_OVER_FINAL = -Integer.MAX_VALUE;

    public static final int WRAPPER = 750;

    /**
     * 最高优先级（最后应用）：供"必须最后定案"的守卫类使用。
     *
     * <p>本模组暂无使用者；预留给将来可能出现的守卫 mixin。
     * （同作者的 New-Armor-System 中由其 {@code ItemStackZeroLossGuardMixin} 使用。）</p>
     */
    public static final int FINAL = Integer.MAX_VALUE;

    private MixinPriorities() {
    }
}