<template>
  <div class="dashboard-container">
    <!-- 统计卡片区域 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #409EFF;">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">在线用户</div>
              <div class="stat-value">{{ stats.onlineUsers }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #67C23A;">
              <el-icon><Plus /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">今日新增用户</div>
              <div class="stat-value">{{ stats.todayNewUsers }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #E6A23C;">
              <el-icon><DocumentAdd /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">今日内容提交</div>
              <div class="stat-value">{{ stats.todayContentSubmit }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #F56C6C;">
              <el-icon><Monitor /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">审核队列积压</div>
              <div class="stat-value">{{ stats.pendingAudit }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #909399;">
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
            <div class="stat-icon" style="background-color: #B37FEB;">
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
            <div class="stat-icon" style="background-color: #5B8FF9;">
              <el-icon><View /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">今日访问</div>
              <div class="stat-value">{{ stats.todayVisit }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #51A051;">
              <el-icon><DocumentChecked /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">内容总数</div>
              <div class="stat-value">{{ stats.contentCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 访问量统计图表 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>访问量统计</span>
              <el-radio-group v-model="visitPeriod" size="small">
                <el-radio-button label="day">今日</el-radio-button>
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="visitChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据增长趋势图表 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>数据增长趋势</span>
              <el-radio-group v-model="growthPeriod" size="small">
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
                <el-radio-button label="year">本年</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="growthChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待审核内容和系统日志 -->
    <el-row :gutter="20" class="recent-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>待审核内容</span>
              <el-button link type="primary" @click="$router.push('/audit')">查看更多</el-button>
            </div>
          </template>
          <el-table :data="pendingAudits" style="width: 100%">
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
              v-for="(log, index) in systemLogs"
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
import { ref, onMounted, watch, onUnmounted } from 'vue'
import {
  UserFilled,
  Plus,
  DocumentAdd,
  Monitor,
  Collection,
  User,
  View,
  DocumentChecked
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import {
  type DashboardStats,
  type VisitTrend,
  type GrowthTrend,
  type PendingAudit,
  type SystemLog,
  getStats,
  getVisitTrend,
  getGrowthTrend,
  getPendingAudits,
  getSystemLogs
} from '@/api/dashboard'

// 统计数据
const stats = ref<DashboardStats>({
  onlineUsers: 45,
  todayNewUsers: 12,
  todayContentSubmit: 28,
  pendingAudit: 15,
  relicCount: 1234,
  userCount: 567,
  todayVisit: 890,
  contentCount: 2340
})

// 图表时间范围
const visitPeriod = ref<'day' | 'week' | 'month'>('week')
const growthPeriod = ref<'week' | 'month' | 'year'>('month')

// 图表引用
const visitChartRef = ref<HTMLDivElement | null>(null)
const growthChartRef = ref<HTMLDivElement | null>(null)
let visitChart: echarts.ECharts | null = null
let growthChart: echarts.ECharts | null = null

// 待审核内容
const pendingAudits = ref<PendingAudit[]>([])

// 系统日志
const systemLogs = ref<SystemLog[]>([])

// 模拟数据生成函数
const generateVisitTrendMock = (period: 'day' | 'week' | 'month'): VisitTrend[] => {
  const data: VisitTrend[] = []
  const now = new Date()
  
  if (period === 'day') {
    for (let i = 0; i < 24; i++) {
      data.push({
        date: `${i.toString().padStart(2, '0')}:00`,
        value: Math.floor(Math.random() * 500) + 200
      })
    }
  } else if (period === 'week') {
    const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    for (let i = 6; i >= 0; i--) {
      const date = new Date(now)
      date.setDate(date.getDate() - i)
      data.push({
        date: days[date.getDay()] || `${date.getMonth() + 1}/${date.getDate()}`,
        value: Math.floor(Math.random() * 500) + 400
      })
    }
  } else {
    for (let i = 30; i >= 0; i--) {
      const date = new Date(now)
      date.setDate(date.getDate() - i)
      data.push({
        date: `${date.getMonth() + 1}/${date.getDate()}`,
        value: Math.floor(Math.random() * 800) + 500
      })
    }
  }
  return data
}

const generateGrowthTrendMock = (period: 'week' | 'month' | 'year'): GrowthTrend[] => {
  const data: GrowthTrend[] = []
  const now = new Date()
  
  let days = 7
  if (period === 'month') days = 30
  else if (period === 'year') days = 365
  
  let relicCount = 1000
  let userCount = 400
  let contentCount = 2000
  
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date(now)
    date.setDate(date.getDate() - i)
    
    relicCount += Math.floor(Math.random() * 20)
    userCount += Math.floor(Math.random() * 15)
    contentCount += Math.floor(Math.random() * 30)
    
    let dateStr = ''
    if (period === 'week') {
      const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      dateStr = weekDays[date.getDay()]
    } else if (period === 'month') {
      dateStr = `${date.getDate()}日`
    } else {
      dateStr = `${date.getMonth() + 1}月`
    }
    
    data.push({
      date: dateStr,
      relicCount,
      userCount,
      contentCount
    })
  }
  return data
}

const generatePendingAuditsMock = (): PendingAudit[] => [
  { objectId: '1', contentType: '图文', content: '博物馆新展览预告文章内容...', submitTime: '2026-05-29 10:30' },
  { objectId: '2', contentType: '评论', content: '用户对青铜器的评论内容...', submitTime: '2026-05-29 09:15' },
  { objectId: '3', contentType: '图文', content: '文物修复过程记录文章...', submitTime: '2026-05-29 08:45' },
  { objectId: '4', contentType: '视频', content: '文物展示视频介绍...', submitTime: '2026-05-29 08:00' },
  { objectId: '5', contentType: '图文', content: '考古发现新成果报道...', submitTime: '2026-05-28 16:30' }
]

const generateSystemLogsMock = (): SystemLog[] => [
  { timestamp: '10:35', type: 'primary', content: '用户 admin 登录系统' },
  { timestamp: '10:20', type: 'success', content: '数据备份任务完成' },
  { timestamp: '10:05', type: 'warning', content: '文物图片上传成功' },
  { timestamp: '09:50', type: 'info', content: 'CSV导入任务完成，导入23条记录' },
  { timestamp: '09:30', type: 'primary', content: '用户 test 登录系统' }
]

// 初始化访问量图表
const initVisitChart = () => {
  if (!visitChartRef.value) return
  
  visitChart = echarts.init(visitChartRef.value)
  updateVisitChart()
}

// 更新访问量图表
const updateVisitChart = () => {
  if (!visitChart) return
  
  const mockData = generateVisitTrendMock(visitPeriod.value)
  
  visitChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#EBEEF5',
      borderWidth: 1,
      textStyle: {
        color: '#606266'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: mockData.map(item => item.date),
      axisLine: {
        lineStyle: {
          color: '#E4E7ED'
        }
      },
      axisLabel: {
        color: '#909399',
        fontSize: 12
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      splitLine: {
        lineStyle: {
          color: '#EBEEF5',
          type: 'dashed'
        }
      },
      axisLabel: {
        color: '#909399',
        fontSize: 12
      }
    },
    series: [{
      name: '访问量',
      type: 'line',
      smooth: true,
      data: mockData.map(item => item.value),
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ]
        }
      },
      itemStyle: {
        color: '#409EFF'
      },
      lineStyle: {
        width: 3
      },
      symbol: 'circle',
      symbolSize: 6,
      emphasis: {
        itemStyle: {
          color: '#409EFF',
          borderColor: '#fff',
          borderWidth: 2
        }
      }
    }]
  })
}

// 初始化数据增长图表
const initGrowthChart = () => {
  if (!growthChartRef.value) return
  
  growthChart = echarts.init(growthChartRef.value)
  updateGrowthChart()
}

// 更新数据增长图表
const updateGrowthChart = () => {
  if (!growthChart) return
  
  const mockData = generateGrowthTrendMock(growthPeriod.value)
  
  growthChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#EBEEF5',
      borderWidth: 1,
      textStyle: {
        color: '#606266'
      }
    },
    legend: {
      data: ['文物数量', '用户数量', '内容数量'],
      bottom: 0,
      textStyle: {
        color: '#909399',
        fontSize: 12
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: mockData.map(item => item.date),
      axisLine: {
        lineStyle: {
          color: '#E4E7ED'
        }
      },
      axisLabel: {
        color: '#909399',
        fontSize: 12
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      splitLine: {
        lineStyle: {
          color: '#EBEEF5',
          type: 'dashed'
        }
      },
      axisLabel: {
        color: '#909399',
        fontSize: 12
      }
    },
    series: [
      {
        name: '文物数量',
        type: 'line',
        smooth: true,
        data: mockData.map(item => item.relicCount),
        itemStyle: {
          color: '#409EFF'
        },
        lineStyle: {
          width: 2
        },
        symbol: 'circle',
        symbolSize: 4
      },
      {
        name: '用户数量',
        type: 'line',
        smooth: true,
        data: mockData.map(item => item.userCount),
        itemStyle: {
          color: '#67C23A'
        },
        lineStyle: {
          width: 2
        },
        symbol: 'circle',
        symbolSize: 4
      },
      {
        name: '内容数量',
        type: 'line',
        smooth: true,
        data: mockData.map(item => item.contentCount),
        itemStyle: {
          color: '#E6A23C'
        },
        lineStyle: {
          width: 2
        },
        symbol: 'circle',
        symbolSize: 4
      }
    ]
  })
}

// 加载数据
const loadData = async () => {
  try {
    const [statsData, auditsData, logsData] = await Promise.all([
      getStats(),
      getPendingAudits(),
      getSystemLogs()
    ])
    stats.value = { ...stats.value, ...statsData }
    pendingAudits.value = auditsData.length > 0 ? auditsData : generatePendingAuditsMock()
    systemLogs.value = logsData.length > 0 ? logsData : generateSystemLogsMock()
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
    // 使用模拟数据
    pendingAudits.value = generatePendingAuditsMock()
    systemLogs.value = generateSystemLogsMock()
  }
}

// 响应式调整图表大小
const handleResize = () => {
  visitChart?.resize()
  growthChart?.resize()
}

onMounted(() => {
  loadData()
  initVisitChart()
  initGrowthChart()
  window.addEventListener('resize', handleResize)
})

watch(visitPeriod, () => {
  updateVisitChart()
})

watch(growthPeriod, () => {
  updateGrowthChart()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  visitChart?.dispose()
  growthChart?.dispose()
})
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

.chart-container {
  height: 300px;
}

/* 最近动态区域样式 */
.recent-row {
  margin-bottom: 20px;
}

.el-timeline-item__timestamp {
  font-size: 12px;
  color: #909399;
}
</style>