package com.platform.admin.modules.artifact.support;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 文物列表热度（P0：基于创建时间的基线算法，P1 可替换为真实统计）。
 */
public final class RelicPopularitySupport {

    private static final int POPULARITY_BASELINE = 1000;
    private static final int MAX_POPULARITY = 999_999;

    private RelicPopularitySupport() {
    }

    /**
     * popularity = max(0, 1000 - daysSinceCreate)，其中 daysSinceCreate 为创建日至当前日期的整天数差。
     *
     * @param createTime 文物创建时间
     * @return 0～999999 的非负整数
     */
    public static int compute(LocalDateTime createTime) {
        if (createTime == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(createTime.toLocalDate(), LocalDate.now());
        if (days < 0) {
            days = 0;
        }
        int value = POPULARITY_BASELINE - (int) days;
        if (value < 0) {
            value = 0;
        }
        return Math.min(value, MAX_POPULARITY);
    }
}
