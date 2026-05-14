package com.platform.admin.modules.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.admin.modules.audit.entity.AuditQueueEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Map;

public interface AuditQueueMapper extends BaseMapper<AuditQueueEntity> {

    @Select("SELECT " +
            "SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) as pendingCount, " +
            "SUM(CASE WHEN status = 'APPROVED' THEN 1 ELSE 0 END) as approvedCount, " +
            "SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) as rejectedCount, " +
            "SUM(CASE WHEN auto_audit_result = 'PASS' THEN 1 ELSE 0 END) as autoApproved, " +
            "SUM(CASE WHEN auto_audit_result = 'REJECT' THEN 1 ELSE 0 END) as autoRejected " +
            "FROM audit_queue " +
            "WHERE submit_time >= #{startDate} AND submit_time <= #{endDate}")
    Map<String, Object> getStatistics(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
}