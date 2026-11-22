package dev.newenchantmentsystem.api;

import dev.newenchantmentsystem.init.ModEnchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 自动冶炼（Auto Smelting）烧炼经验结算。
 * <p>
 * 掉落物替换由 {@link AutoSmeltingModifier}（Forge 战利品修改器）完成；本类监听
 * {@link BlockEvent.BreakEvent} 补充烧炼经验。事件触发于方块破坏流程中、方块实体
 * 尚未被移除时，在此按与原版 {@code Block.dropResources} 相同的战利品参数
 * （ORIGIN / TOOL / THIS_ENTITY / luck / BLOCK_ENTITY）重算一次掉落，对每个有
 * 熔炼配方的掉落物累计配方经验（{@code recipe.getExperience()} × 数量），写入
 * {@link BlockEvent.BreakEvent#setExpToDrop(int)}，随方块破坏经验一并给玩家。
 * <ul>
 *   <li>仅当主手工具带自动冶炼附魔时生效；</li>
 *   <li>重算结果与真实掉落同源（同一战利品表），仅数量随机性略有差异，作为经验近似足够。</li>
 * </ul>
 */
public class AutoSmeltingHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled()) {
            return;
        }
        LevelAccessor levelAccessor = event.getLevel();
        if (levelAccessor.isClientSide()) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack tool = player.getMainHandItem();
        if (tool.getEnchantmentLevel(ModEnchantments.AUTO_SMELTING.get()) <= 0) {
            return;
        }
        if (!(levelAccessor instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
        LootParams.Builder builder = (new LootParams.Builder(serverLevel))
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, tool)
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withLuck(player.getLuck());
        if (blockEntity != null) {
            builder.withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);
        }
        float bonusExp = 0.0F;
        for (ItemStack drop : state.getDrops(builder)) {
            SmeltingRecipe recipe = AutoSmeltingModifier.findRecipe(drop, serverLevel);
            if (recipe != null) {
                bonusExp += recipe.getExperience() * drop.getCount();
            }
        }
        if (bonusExp > 0.0F) {
            event.setExpToDrop(event.getExpToDrop() + (int) Math.floor(bonusExp));
        }
    }
}
