package dev.newenchantmentsystem.mixin;

import dev.newenchantmentsystem.api.Config;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 力量重做（弓）：每级 +15% 箭矢伤害。
 * 原版在 BowItem.releaseUsing 中为箭附加固定伤害（baseDamage + level * 0.5 + 0.5），
 * 这里改为乘算：baseDamage × (1 + 0.15 × 等级)。
 * 箭的发射者即射箭玩家（主手为弓），据此读取力量等级。
 */
@Mixin(BowItem.class)
public abstract class BowItemPowerMixin {

    @Redirect(
            method = "releaseUsing",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;setBaseDamage(D)V")
    )
    private void nes$powerArrowDamage(AbstractArrow pArrow, double pBaseDamage) {
        double multiplier = 1.0D;
        if (pArrow.getOwner() instanceof LivingEntity shooter) {
            int level = shooter.getMainHandItem().getEnchantmentLevel(Enchantments.POWER_ARROWS);
            if (level > 0) {
                multiplier = 1.0D + Config.POWER_DAMAGE_MULTIPLIER.get() * level;
            }
        }
        pArrow.setBaseDamage(pBaseDamage * multiplier);
    }
}
