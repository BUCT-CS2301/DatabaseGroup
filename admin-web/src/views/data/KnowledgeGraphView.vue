<template>
  <div class="knowledge-graph-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>知识图谱</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleRefresh">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <!-- 图谱展示区域 -->
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="hover" class="graph-card">
          <template #header>
            <div class="card-header">
              <span>文物关系图谱</span>
              <el-radio-group v-model="graphLayout" size="small">
                <el-radio-button label="force">力导向图</el-radio-button>
                <el-radio-button label="tree">树形图</el-radio-button>
                <el-radio-button label="radial">放射图</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="graph-placeholder">
            <el-icon class="graph-icon"><Connection /></el-icon>
            <div class="graph-text">
              <h3>知识图谱可视化区域</h3>
              <p>展示文物之间的关系网络，包括：</p>
              <ul>
                <li>同一时期文物的关联关系</li>
                <li>同一博物馆的馆藏关系</li>
                <li>文物类型分类关系</li>
                <li>文物材质关联分析</li>
              </ul>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover" class="stats-card">
          <template #header>
            <span>图谱统计</span>
          </template>
          <div class="stats-list">
            <div class="stat-item">
              <span class="stat-label">节点总数</span>
              <span class="stat-value">{{ graphStats.nodeCount }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">关系总数</span>
              <span class="stat-value">{{ graphStats.edgeCount }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">博物馆节点</span>
              <span class="stat-value">{{ graphStats.museumCount }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">文物节点</span>
              <span class="stat-value">{{ graphStats.relicCount }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">类型节点</span>
              <span class="stat-value">{{ graphStats.typeCount }}</span>
            </div>
          </div>
        </el-card>

        <el-card shadow="hover" class="info-card" style="margin-top: 20px;">
          <template #header>
            <span>图例说明</span>
          </template>
          <div class="legend-list">
            <div class="legend-item">
              <span class="legend-dot" style="background-color: #409EFF;"></span>
              <span class="legend-text">博物馆节点</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot" style="background-color: #67C23A;"></span>
              <span class="legend-text">文物节点</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot" style="background-color: #E6A23C;"></span>
              <span class="legend-text">类型节点</span>
            </div>
            <div class="legend-item">
              <span class="legend-line" style="border-top-style: solid;"></span>
              <span class="legend-text">收藏关系</span>
            </div>
            <div class="legend-item">
              <span class="legend-line" style="border-top-style: dashed;"></span>
              <span class="legend-text">类型关系</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 节点详情 -->
    <el-card shadow="hover" class="detail-card" style="margin-top: 20px;">
      <template #header>
        <span>节点详情</span>
      </template>
      <el-table :data="nodeDetails" style="width: 100%">
        <el-table-column prop="name" label="节点名称" width="200" />
        <el-table-column prop="type" label="节点类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="relatedCount" label="关联数量" width="120" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">查看详情</el-button>
            <el-button link type="primary" @click="handleHighlight(row)">高亮</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Connection } from '@element-plus/icons-vue'

// 图谱布局
const graphLayout = ref('force')

// 图谱统计
const graphStats = reactive({
  nodeCount: 1234,
  edgeCount: 5678,
  museumCount: 50,
  relicCount: 1000,
  typeCount: 184
})

// 节点详情列表
const nodeDetails = ref([
  {
    name: '故宫博物院',
    type: '博物馆',
    relatedCount: 256,
    description: '中国最大的古代文化艺术博物馆，馆藏文物丰富'
  },
  {
    name: '大都会艺术博物馆',
    type: '博物馆',
    relatedCount: 198,
    description: '美国最大的艺术博物馆，收藏世界各地珍贵文物'
  },
  {
    name: '青铜器',
    type: '类型',
    relatedCount: 432,
    description: '以青铜为主要材质制作的器物，常见于商周时期'
  },
  {
    name: '商代青铜鼎',
    type: '文物',
    relatedCount: 45,
    description: '商代时期的青铜礼器，具有重要的历史价值'
  },
  {
    name: '陶瓷器',
    type: '类型',
    relatedCount: 356,
    description: '以陶瓷为主要材质的器物，包括瓷器、陶器等'
  }
])

// 刷新
const handleRefresh = () => {
  ElMessage.success('图谱已刷新')
}

// 查看详情
const handleViewDetail = (row: any) => {
  ElMessage.info(`查看节点详情: ${row.name}`)
}

// 高亮节点
const handleHighlight = (row: any) => {
  ElMessage.success(`已高亮节点: ${row.name}`)
}
</script>

<style scoped>
.knowledge-graph-container {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.graph-placeholder {
  height: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
  border-radius: 4px;
}

.graph-icon {
  font-size: 64px;
  color: #409EFF;
  margin-bottom: 20px;
}

.graph-text {
  text-align: center;
  color: #606266;
}

.graph-text h3 {
  margin: 0 0 12px;
  font-size: 18px;
  color: #303133;
}

.graph-text p {
  margin: 0 0 8px;
}

.graph-text ul {
  margin: 0;
  padding-left: 20px;
  text-align: left;
}

.graph-text li {
  margin: 4px 0;
}

.stats-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.legend-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.legend-line {
  width: 24px;
  height: 0;
  border-top-width: 3px;
  border-top-color: #909399;
}

.legend-text {
  font-size: 14px;
  color: #606266;
}
</style>
