package dev.newenchantmentsystem.api;

/**
 * 等级经验计算工具。
 * 升级消耗为线性公式：EP(LV) = LevelCoefficient * (LV + LevelConstant)
 * EP 序列为公差 = LevelCoefficient 的等差数列，
 * 从 0 级升到某级的总消耗（SEP）用等差数列求和公式计算。
 */
public final class LevelXp {

    private LevelXp() {
    }

    /**
     * 从等级 level 升到 level + 1 所需经验（EP）。
     * EP(LV) = LevelCoefficient * (LV + LevelConstant)，LV 从 0 开始。
     */
    public static long epForLevel(int level) {
        long coefficient = Config.LEVEL_COEFFICIENT.get();
        long constant = Config.LEVEL_CONSTANT.get();
        return coefficient * ((long) level + constant);
    }

    /**
     * 从 0 级升到 targetLevel 级的总经验消耗（SEP），等差数列求和。
     * 首项 a1 = EP(0) = coefficient * constant，公差 d = coefficient：
     * SEP(n) = n * (a1 + a_n) / 2 = n * coefficient * (2 * constant + n - 1) / 2
     * 先做除法再乘，避免中间结果溢出 long。
     */
    public static long totalXpToLevel(int targetLevel) {
        if (targetLevel <= 0) {
            return 0;
        }
        long n = targetLevel;
        long coefficient = Config.LEVEL_COEFFICIENT.get();
        long constant = Config.LEVEL_CONSTANT.get();
        long sumPair = 2L * coefficient * constant + (n - 1) * coefficient; // a1 + a_n
        if ((n & 1L) == 0L) {
            return (n / 2L) * sumPair;
        }
        // n 为奇数时 a1 + a_n 必为偶数，除以 2 不会丢精度
        return n * (sumPair / 2L);
    }

    /**
     * 从 currentLevel 级升到 targetLevel 级所需经验。
     * 算法：先求总和 SEP(targetLevel)，再求当前已消耗 SEP(currentLevel)，最后相减。
     */
    public static long xpFromTo(int currentLevel, int targetLevel) {
        if (targetLevel <= currentLevel) {
            return 0;
        }
        return totalXpToLevel(targetLevel) - totalXpToLevel(currentLevel);
    }
}
