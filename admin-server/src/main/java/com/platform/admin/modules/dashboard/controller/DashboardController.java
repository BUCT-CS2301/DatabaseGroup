package com.platform.admin.modules.dashboard.controller;

import com.platform.admin.common.Result;
import com.platform.admin.modules.dashboard.service.DashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * 获取实时统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.success(dashboardService.getStats());
    }

    /**
     * 获取访问量趋势
     */
    @GetMapping("/visit-trend")
    public Result<List<Map<String, Object>>> getVisitTrend(@RequestParam(defaultValue = "week") String period) {
        return Result.success(dashboardService.getVisitTrend(period));
    }

    /**
     * 获取数据增长趋势
     */
    @GetMapping("/growth-trend")
    public Result<List<Map<String, Object>>> getGrowthTrend(@RequestParam(defaultValue = "month") String period) {
        return Result.success(dashboardService.getGrowthTrend(period));
    }

    /**
     * 获取待审核内容列表
     */
    @GetMapping("/pending-audits")
    public Result<List<Map<String, Object>>> getPendingAudits() {
        return Result.success(dashboardService.getPendingAudits());
    }

    /**
     * 获取系统日志
     */
    @GetMapping("/system-logs")
    public Result<List<Map<String, Object>>> getSystemLogs() {
        return Result.success(dashboardService.getSystemLogs());
    }
}