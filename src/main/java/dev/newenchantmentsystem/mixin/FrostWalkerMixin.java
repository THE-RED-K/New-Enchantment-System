package dev.newenchantmentsystem.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 冰霜行者（Frost Walker）冻结半径重设。
 * 原版公式：min(16, 2 + 等级)；本模组改为 min(16, 2 + 0.5 * 等级)。
 * 等级上限由 TierOverrideMaxLevelMixin（mediumMaxLevel）控制。
 */
@Mixin(FrostWalkerEnchantment.class)
public abstract class FrostWalkerMixin {

    @Redirect(
            method = "onEntityMoved",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I")
    )
    private static int nes$frostWalkerRadius(int pMax, int pUnused,
                                             LivingEntity pLiving, Level pLevel, BlockPos pPos, int pLevelConflicting) {
        return Math.min(pMax, (int) (2.0F + 0.5F * pLevelConflicting));
    }
}
