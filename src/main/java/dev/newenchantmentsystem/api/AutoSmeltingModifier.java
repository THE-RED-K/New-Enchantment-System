package dev.newenchantmentsystem.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.newenchantmentsystem.init.ModEnchantments;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

/**
 * 自动冶炼（Auto Smelting）战利品修改器：将工具附有自动冶炼的玩家破坏方块所得掉落物，
 * 替换为其熔炼产物。
 * <p>
 * 基于 Forge 官方战利品修改器机制（{@link IGlobalLootModifier} / {@link LootModifier}，
 * 1.20.1 为 Codec 注册体系），由 {@code data/.../loot_modifiers/auto_smelting.json}
 * 与 {@code data/forge/loot_modifiers/global_loot_modifiers.json} 启用，纯 Forge 机制、
 * 非 Mixin。仅对"经由战利品表的掉落"生效——原版所有会掉落可烧炼物的方块均有战利品表，
 * 故覆盖"任何可被烧炼的方块"。
 * <ul>
 *   <li>每个掉落物查询熔炼配方（{@link RecipeType#SMELTING}），有配方则替换为烧炼产物
 *       （数量按"1 输入 → 配方输出数量"缩放，原版熔炼配方均为 1:1）；</li>
 *   <li>无熔炼配方的掉落物（如钻石、木板、树苗）原样保留；</li>
 *   <li>烧炼经验由 {@link AutoSmeltingHandler}（{@code BlockEvent.BreakEvent}）另行结算。</li>
 * </ul>
 */
public class AutoSmeltingModifier extends LootModifier {

    /** 注册表中的 codec（对应 JSON：{"type": "new_enchantment_system:auto_smelting", "conditions": []}）。 */
    public static final Codec<AutoSmeltingModifier> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(IGlobalLootModifier.LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(lm -> lm.conditions))
                    .apply(inst, AutoSmeltingModifier::new));

    /** 熔炼配方快速检查器（缓存配方解析结果，避免每块方块全表扫描）。 */
    private static final RecipeManager.CachedCheck<Container, SmeltingRecipe> SMELTING_CHECK =
            RecipeManager.createCheck(RecipeType.SMELTING);

    public AutoSmeltingModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (tool == null || tool.getEnchantmentLevel(ModEnchantments.AUTO_SMELTING.get()) <= 0) {
            return generatedLoot;
        }
        for (int i = 0; i < generatedLoot.size(); i++) {
            ItemStack smelted = smelted(generatedLoot.get(i), context.getLevel());
            if (smelted != null) {
                generatedLoot.set(i, smelted);
            }
        }
        return generatedLoot;
    }

    /** 查询输入的熔炼配方；无配方返回 {@code null}。 */
    @Nullable
    public static SmeltingRecipe findRecipe(ItemStack input, Level level) {
        if (input.isEmpty()) {
            return null;
        }
        return SMELTING_CHECK.getRecipeFor(new SimpleContainer(input), level).orElse(null);
    }

    /** 将输入替换为烧炼产物；无熔炼配方返回 {@code null}（原样保留）。 */
    @Nullable
    public static ItemStack smelted(ItemStack input, Level level) {
        SmeltingRecipe recipe = findRecipe(input, level);
        if (recipe == null) {
            return null;
        }
        ItemStack result = recipe.getResultItem(level.registryAccess()).copy();
        // 1 输入 → 配方输出数量；n 个掉落物等比缩放（原版熔炼配方均为 1:1）
        result.setCount(input.getCount() * result.getCount());
        return result;
    }
}
