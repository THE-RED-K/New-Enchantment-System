package dev.newenchantmentsystem.api;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * /sep X 指令 —— 查询从当前等级升到 X 级所需的经验值。
 * 算法：needed = SEP(targetLevel) - SEP(currentLevel)
 * （SEP 为从 0 级升到某级的等差数列总消耗）。
 */
public class LevelXpCommand {

    /** 目标等级上限：防止极端输入与配置组合导致 long 溢出，100 万级已覆盖所有实际需求。 */
    private static final int MAX_QUERY_LEVEL = 1_000_000;

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("sep")
                        .requires(source -> source.hasPermission(0))
                        .then(Commands.argument("level", IntegerArgumentType.integer(0, MAX_QUERY_LEVEL))
                                .executes(LevelXpCommand::query)));
    }

    private static int query(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        // 配置未加载或系统禁用时静默返回（提示无实际意义，不打扰玩家）
        if (!Config.isLoaded() || !Config.ENABLE_LEVEL_SYSTEM.get()) {
            return 0;
        }

        int targetLevel = IntegerArgumentType.getInteger(context, "level");
        ServerPlayer player = source.getPlayerOrException();
        int currentLevel = player.experienceLevel;

        // 先求总和：0 级升到目标等级的总经验
        long totalToTarget = LevelXp.totalXpToLevel(targetLevel);
        // 再求当前经验值：0 级升到当前等级已消耗的总经验
        long spentToCurrent = LevelXp.totalXpToLevel(currentLevel);
        // 最后相减：从当前等级升到目标等级所需经验
        long needed = totalToTarget - spentToCurrent;

        if (targetLevel <= currentLevel) {
            source.sendSuccess(() -> Component.translatable("command.new_enchantment_system.sep.reached", currentLevel)
                    .withStyle(ChatFormatting.GOLD), false);
            return 1;
        }

        Component message = Component.literal("[")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.translatable("command.new_enchantment_system.mod_name")
                        .withStyle(ChatFormatting.YELLOW))
                .append(Component.literal("] ").withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("command.new_enchantment_system.sep.result",
                                currentLevel, targetLevel, needed)
                        .withStyle(ChatFormatting.GREEN))
                .append(Component.literal("\n").withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("command.new_enchantment_system.sep.detail",
                                targetLevel, totalToTarget, currentLevel, spentToCurrent)
                        .withStyle(ChatFormatting.GRAY));
        source.sendSuccess(() -> message, false);
        return (int) needed;
    }
}
