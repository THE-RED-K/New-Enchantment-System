package dev.newenchantmentsystem.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * 深海探索者（Depth Strider）3 级以上收益曲线修复。
 * <p>
 * 原版 {@code LivingEntity.travel} 将附魔等级 f6 硬钳制在 3.0（{@code f6 > 3.0F → 3.0F}），
 * 导致分档（mediumMaxLevel = 4）后 4 级与 3 级逐位相同，成为纯伪等级。
 * 仅移除钳制不可行：权重 f6/3 > 1 会使衰减系数 f4 越过 0.546 目标值继续下探，
 * 疾跑游泳时反而减速（过冲）。
 * <p>
 * 本 mixin 保留 1-3 级原版公式不变，对 3 级以上在水平加速 f5（{@code moveRelative} 入参）
 * 上乘 {@code 1 + (level - 3) / 3}。由于水下稳态速度 v = f5·f4/(1-f4) 与 f5 成正比，
 * 每级恰好提升 1/3 玩家游泳移速（乘算外推，4 级 = 4/3 × 3 级，5 级 = 5/3 × 3 级）。
 * 摩擦系数 f4 不参与外推，避免原版钳制被破坏后的过冲问题。
 */
@Mixin(LivingEntity.class)
public abstract class DepthStriderMixin {

    @ModifyArg(
            method = "travel",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V", ordinal = 0),
            index = 0
    )
    private float nes$depthStriderSwimBoost(float pSpeed) {
        LivingEntity entity = (LivingEntity) (Object) this;
        int level = EnchantmentHelper.getDepthStrider(entity);
        if (level > 3 && entity instanceof Player) {
            pSpeed *= 1.0F + (level - 3) / 3.0F;
        }
        return pSpeed;
    }
}
