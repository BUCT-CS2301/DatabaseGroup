package com.platform.admin.modules.dashboard.service;

import java.util.List;
import java.util.Map;

public interface DashboardService {

    /**
     * 获取实时统计数据
     */
    Map<String, Object> getStats();

    /**
     * 获取访问量趋势
     */
    List<Map<String, Object>> getVisitTrend(String period);

    /**
     * 获取数据增长趋势
     */
    List<Map<String, Object>> getGrowthTrend(String period);

    /**
     * 获取待审核内容列表
     */
    List<Map<String, Object>> getPendingAudits();

    /**
     * 获取系统日志
     */
    List<Map<String, Object>> getSystemLogs();
}