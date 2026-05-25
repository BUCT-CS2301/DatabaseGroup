<template>
  <div class="dashboard-container">
    <!-- 统计卡片区域 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #409EFF;">
              <el-icon><Collection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">文物总数</div>
              <div class="stat-value">{{ stats.relicCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #67C23A;">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">用户总数</div>
              <div class="stat-value">{{ stats.userCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #E6A23C;">
              <el-icon><DocumentChecked /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">待审核内容</div>
              <div class="stat-value">{{ stats.pendingAudit }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #F56C6C;">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">今日访问</div>
              <div class="stat-value">{{ stats.todayVisit }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>数据趋势</span>
              <el-radio-group v-model="chartPeriod" size="small">
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
                <el-radio-button label="year">本年</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-placeholder">
            <el-icon class="chart-icon"><TrendCharts /></el-icon>
            <span>数据趋势图表</span>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>快速操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/data/artifact')">
              <el-icon><Plus /></el-icon>添加文物
            </el-button>
            <el-button type="success" @click="$router.push('/audit')">
              <el-icon><DocumentChecked /></el-icon>审核内容
            </el-button>
            <el-button type="warning" @click="$router.push('/backup')">
              <el-icon><Coin /></el-icon>数据备份
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近动态区域 -->
    <el-row :gutter="20" class="recent-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>待审核内容</span>
              <el-button link type="primary" @click="$router.push('/audit')">查看更多</el-button>
            </div>
          </template>
          <el-table :data="recentAudits" style="width: 100%">
            <el-table-column prop="contentType" label="类型" width="100" />
            <el-table-column prop="content" label="内容" show-overflow-tooltip />
            <el-table-column prop="submitTime" label="提交时间" width="150" />
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>系统日志</span>
              <el-button link type="primary" @click="$router.push('/log')">查看更多</el-button>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="(log, index) in recentLogs"
              :key="index"
              :timestamp="log.timestamp"
              :type="log.type"
              :hollow="true"
            >
              {{ log.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import {
  Collection,
  User,
  DocumentChecked,
  Clock,
  TrendCharts,
  Plus,
  Coin
} from '@element-plus/icons-vue'

// 统计数据
const stats = ref({
  relicCount: 1234,
  userCount: 567,
  pendingAudit: 23,
  todayVisit: 890
})

// 图表时间范围
const chartPeriod = ref('week')

// 最近待审核内容
const recentAudits = ref([
  { contentType: '图文', content: '博物馆新展览预告文章内容...', submitTime: '2026-05-24 10:30' },
  { contentType: '评论', content: '用户对青铜器的评论内容...', submitTime: '2026-05-24 09:15' },
  { contentType: '图文', content: '文物修复过程记录文章...', submitTime: '2026-05-24 08:45' }
])

// 最近系统日志
const recentLogs = ref([
  { timestamp: '2026-05-24 10:35', type: 'primary', content: '用户 admin 登录系统' },
  { timestamp: '2026-05-24 10:20', type: 'success', content: '数据备份任务完成' },
  { timestamp: '2026-05-24 10:05', type: 'warning', content: '文物图片上传成功' },
  { timestamp: '2026-05-24 09:50', type: 'info', content: 'CSV导入任务完成，导入23条记录' }
])
</script>

<style scoped>
.dashboard-container {
  padding: 0;
}

/* 统计卡片样式 */
.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

/* 图表区域样式 */
.chart-row {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-placeholder {
  height: 250px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  background-color: #fafafa;
  border-radius: 4px;
}

.chart-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

/* 快速操作样式 */
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quick-actions .el-button {
  width: 100%;
  justify-content: flex-start;
  padding-left: 20px;
}

.quick-actions .el-button .el-icon {
  margin-right: 8px;
}

/* 最近动态区域样式 */
.recent-row {
  margin-bottom: 20px;
}
</style>
