package com.platform.admin.modules.backup.support;

import com.platform.admin.common.BusinessException;
import com.platform.admin.common.ErrorCode;
import org.springframework.scheduling.support.CronExpression;

import java.time.LocalDateTime;
import java.time.ZoneId;

public final class BackupCronUtil {

    private BackupCronUtil() {
    }

    /**
     * 校验 Cron 表达式并计算下一次执行时间。
     *
     * @param cronExpression 标准 6 位 Cron（秒 分 时 日 月 周）
     * @param from           起始时间
     * @return 下一次执行时间
     */
    public static LocalDateTime nextExecutionTime(String cronExpression, LocalDateTime from) {
        try {
            CronExpression expression = CronExpression.parse(cronExpression);
            LocalDateTime next = expression.next(from);
            if (next == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Cron 表达式无法计算下次执行时间");
            }
            return next;
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Cron 表达式非法");
        }
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
    }
}
