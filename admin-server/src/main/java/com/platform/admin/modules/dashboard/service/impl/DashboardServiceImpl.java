package com.platform.admin.modules.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.admin.modules.audit.entity.ContentAudit;
import com.platform.admin.modules.audit.mapper.ContentAuditMapper;
import com.platform.admin.modules.artifact.entity.ArtifactEntity;
import com.platform.admin.modules.artifact.mapper.ArtifactMapper;
import com.platform.admin.modules.dashboard.service.DashboardService;
import com.platform.admin.modules.user.entity.User;
import com.platform.admin.modules.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final UserMapper userMapper;
    private final ArtifactMapper artifactMapper;
    private final ContentAuditMapper contentAuditMapper;

    public DashboardServiceImpl(UserMapper userMapper, ArtifactMapper artifactMapper,
                               ContentAuditMapper contentAuditMapper) {
        this.userMapper = userMapper;
        this.artifactMapper = artifactMapper;
        this.contentAuditMapper = contentAuditMapper;
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 在线用户数（模拟）
        stats.put("onlineUsers", 45);
        
        // 今日新增用户数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        long todayNewUsers = userMapper.selectCount(new LambdaQueryWrapper<User>().ge(User::getCreateTime, todayStart));
        stats.put("todayNewUsers", todayNewUsers);
        
        // 今日内容提交量
        long todayContentSubmit = contentAuditMapper.selectCount(new LambdaQueryWrapper<ContentAudit>().ge(ContentAudit::getSubmitTime, todayStart));
        stats.put("todayContentSubmit", todayContentSubmit);
        
        // 审核队列积压量（待审核状态）
        long pendingAudit = contentAuditMapper.selectCount(new LambdaQueryWrapper<ContentAudit>().eq(ContentAudit::getStatus, "PENDING"));
        stats.put("pendingAudit", pendingAudit);
        
        // 文物总数
        long relicCount = artifactMapper.selectCount(null);
        stats.put("relicCount", relicCount);
        
        // 用户总数
        long userCount = userMapper.selectCount(null);
        stats.put("userCount", userCount);
        
        // 今日访问量（模拟）
        stats.put("todayVisit", 890);
        
        // 内容总数（模拟）
        stats.put("contentCount", 2340);
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getVisitTrend(String period) {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate now = LocalDate.now();
        
        int days = switch (period) {
            case "day" -> 24;
            case "month" -> 30;
            default -> 7; // week
        };
        
        for (int i = days - 1; i >= 0; i--) {
            Map<String, Object> item = new HashMap<>();
            if ("day".equals(period)) {
                int hour = (LocalTime.now().getHour() - i + 24) % 24;
                item.put("date", String.format("%02d:00", hour));
            } else {
                LocalDate date = now.minusDays(i);
                item.put("date", String.format("%d/%d", date.getMonthValue(), date.getDayOfMonth()));
            }
            // 模拟访问量数据
            item.put("value", (int) (Math.random() * 500) + 200);
            trend.add(item);
        }
        
        return trend;
    }

    @Override
    public List<Map<String, Object>> getGrowthTrend(String period) {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate now = LocalDate.now();
        
        int days = switch (period) {
            case "year" -> 12;
            default -> period.equals("week") ? 7 : 30;
        };
        
        long relicCount = artifactMapper.selectCount(null);
        long userCount = userMapper.selectCount(null);
        long baseRelics = Math.max(relicCount, days * 5L);
        long baseUsers = Math.max(userCount, days * 2L);
        long baseContents = 2000L;
        
        for (int i = days - 1; i >= 0; i--) {
            Map<String, Object> item = new HashMap<>();
            if ("year".equals(period)) {
                int month = (now.getMonthValue() - i + 12) % 12;
                if (month == 0) month = 12;
                item.put("date", month + "月");
            } else {
                LocalDate date = now.minusDays(i);
                item.put("date", String.format("%d/%d", date.getMonthValue(), date.getDayOfMonth()));
            }
            
            item.put("relics", baseRelics + i * 10 + (long) (Math.random() * 5));
            item.put("users", baseUsers + i * 3 + (long) (Math.random() * 2));
            item.put("contents", baseContents + i * 15 + (long) (Math.random() * 10));
            
            trend.add(item);
        }
        
        return trend;
    }

    @Override
    public List<Map<String, Object>> getPendingAudits() {
        List<ContentAudit> audits = contentAuditMapper.selectList(
            new LambdaQueryWrapper<ContentAudit>()
                .eq(ContentAudit::getStatus, "PENDING")
                .orderByDesc(ContentAudit::getSubmitTime)
                .last("LIMIT 5")
        );
        List<Map<String, Object>> result = new ArrayList<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (ContentAudit audit : audits) {
            Map<String, Object> item = new HashMap<>();
            item.put("objectId", audit.getObjectId());
            item.put("contentType", audit.getContentType());
            item.put("content", audit.getContentText());
            item.put("submitTime", audit.getSubmitTime() != null ? audit.getSubmitTime().format(formatter) : "");
            result.add(item);
        }
        
        // 如果数据库中没有数据，返回模拟数据
        if (result.isEmpty()) {
            String[] types = {"图文", "评论", "音频", "视频"};
            String[] contents = {
                "博物馆新展览预告文章内容...",
                "用户对青铜器的评论内容...",
                "文物修复过程记录文章...",
                "文物讲解音频内容...",
                "考古发现新进展报道..."
            };
            LocalDateTime now = LocalDateTime.now();
            for (int i = 0; i < 5; i++) {
                Map<String, Object> item = new HashMap<>();
                item.put("objectId", String.valueOf(i + 1));
                item.put("contentType", types[i % types.length]);
                item.put("content", contents[i]);
                item.put("submitTime", now.minusMinutes(i * 15).format(formatter));
                result.add(item);
            }
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getSystemLogs() {
        List<Map<String, Object>> logs = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        // 模拟系统日志
        String[][] logData = {
            {"primary", "用户 admin 登录系统"},
            {"success", "数据备份任务完成"},
            {"warning", "文物图片上传成功"},
            {"info", "CSV导入任务完成，导入23条记录"},
            {"danger", "检测到敏感词内容，已拦截"}
        };
        
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < logData.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("timestamp", now.minusMinutes(i * 15).format(formatter));
            item.put("type", logData[i][0]);
            item.put("content", logData[i][1]);
            logs.add(item);
        }
        
        return logs;
    }
}