package com.platform.admin.modules.log.service;

import com.platform.admin.common.PageResult;
import com.platform.admin.modules.log.dto.LogExportRequest;
import com.platform.admin.modules.log.vo.LogExportVO;
import com.platform.admin.modules.log.vo.LogStatsVO;
import com.platform.admin.modules.log.vo.OperationLogDetailVO;
import com.platform.admin.modules.log.vo.OperationLogVO;
import com.platform.admin.modules.log.vo.SecurityLogVO;
import com.platform.admin.modules.log.vo.SystemLogVO;

import java.nio.file.Path;
import java.time.LocalDateTime;

public interface LogService {

    PageResult<OperationLogVO> pageOperationLogs(
            long page,
            long pageSize,
            String userId,
            String module,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    OperationLogDetailVO getOperationLogDetail(String objectId);

    PageResult<SystemLogVO> pageSystemLogs(long page, long pageSize, String level);

    PageResult<SecurityLogVO> pageSecurityLogs(long page, long pageSize);

    LogExportVO exportLogs(LogExportRequest request);

    /**
     * 获取日志统计数据。
     *
     * @return 日志统计信息
     */
    LogStatsVO getStats();

    /**
     * 根据 fileId 解析导出文件路径。
     *
     * @param fileId 导出文件标识
     * @return 文件路径
     */
    Path resolveExportFile(String fileId);
}
