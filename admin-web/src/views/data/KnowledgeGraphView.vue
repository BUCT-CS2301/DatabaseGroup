<template>
  <div class="knowledge-graph-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>知识图谱</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleRefresh" :loading="loading">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <!-- 图谱展示区域 -->
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="hover" class="graph-card">
          <template #header>
            <span>文物关系图谱</span>
          </template>
          <div ref="graphRef" class="graph-container"></div>
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
            <div class="stat-item">
              <span class="stat-label">时期节点</span>
              <span class="stat-value">{{ graphStats.periodCount }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">材质节点</span>
              <span class="stat-value">{{ graphStats.materialCount }}</span>
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
              <span class="legend-dot" style="background-color: #F56C6C;"></span>
              <span class="legend-text">时期节点</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot" style="background-color: #909399;"></span>
              <span class="legend-text">材质节点</span>
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
      <el-table :data="nodeDetails" style="width: 100%" max-height="400">
        <el-table-column prop="name" label="节点名称" width="200" />
        <el-table-column prop="type" label="节点类型" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="getTypeTagType(row.type)">{{ getTypeLabel(row.type) }}</el-tag>
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
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getGraphData, type GraphNode, type GraphEdge } from '@/api/knowledge-graph'

// 图谱容器
const graphRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null

// 加载状态
const loading = ref(false)

// 图谱数据
const graphData = reactive({
  nodes: [] as GraphNode[],
  edges: [] as GraphEdge[]
})

// 图谱统计
const graphStats = reactive({
  nodeCount: 0,
  edgeCount: 0,
  museumCount: 0,
  relicCount: 0,
  typeCount: 0,
  periodCount: 0,
  materialCount: 0
})

// 节点详情列表
const nodeDetails = ref<Array<{
  name: string
  type: string
  relatedCount: number
  description: string
}>>([])

// 获取类型标签颜色
const getTypeTagType = (type: string) => {
  const map: Record<string, any> = {
    '博物馆': 'primary',
    '文物': 'success',
    '类型': 'warning',
    '时期': 'danger',
    '材质': 'info'
  }
  return map[type] || 'info'
}

// 获取类型标签文本
const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    'museum': '博物馆',
    'relic': '文物',
    'type': '类型',
    'period': '时期',
    'material': '材质'
  }
  return map[type] || type
}

// 初始化图表
const initChart = () => {
  if (!graphRef.value) return

  chartInstance = echarts.init(graphRef.value)
  updateChart()

  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
}

// 更新图表
const updateChart = () => {
  if (!chartInstance) return

  const option: any = {
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => {
        if (params.dataType === 'node') {
          return `<div style="padding: 8px;">
            <div style="font-weight: bold; margin-bottom: 4px;">${params.data.name}</div>
            <div style="color: #909399; font-size: 12px;">类型: ${getTypeLabel(params.data.type)}</div>
            <div style="color: #909399; font-size: 12px;">关联数: ${params.data.value}</div>
          </div>`
        } else if (params.dataType === 'edge') {
          return `<div style="padding: 8px;">
            <div style="font-weight: bold;">${params.data.name}</div>
          </div>`
        }
        return ''
      }
    },
    legend: {
      show: false
    },
    series: [
      {
        type: 'graph',
        layout: 'force',
        data: JSON.parse(JSON.stringify(graphData.nodes)),
        links: JSON.parse(JSON.stringify(graphData.edges)),
        categories: [
          { name: '博物馆' },
          { name: '文物' },
          { name: '类型' },
          { name: '时期' },
          { name: '材质' }
        ],
        roam: true,
        draggable: true,
        label: {
          show: true,
          position: 'right',
          formatter: '{b}'
        },
        lineStyle: {
          color: '#ccc',
          curveness: 0.2
        },
        emphasis: {
          focus: 'adjacency',
          lineStyle: {
            width: 4
          }
        },
        force: {
          repulsion: 300,
          edgeLength: [50, 100]
        }
      }
    ]
  }

  chartInstance.clear()
  chartInstance.setOption(option, true)
}

// 加载图谱数据
const loadGraphData = async () => {
  console.log('loadGraphData called')
  loading.value = true
  try {
    console.log('Calling getGraphData...')
    const res = await getGraphData()
    console.log('getGraphData response:', res)
    const result = (res as any).data as { nodes?: GraphNode[], edges?: GraphEdge[], stats?: any }

    graphData.nodes = result.nodes || []
    graphData.edges = result.edges || []

    // 更新统计数据
    if (result.stats) {
      Object.assign(graphStats, result.stats)
    } else {
      // 计算统计数据
      graphStats.nodeCount = graphData.nodes.length
      graphStats.edgeCount = graphData.edges.length
      graphStats.museumCount = graphData.nodes.filter(n => n.type === 'museum').length
      graphStats.relicCount = graphData.nodes.filter(n => n.type === 'relic').length
      graphStats.typeCount = graphData.nodes.filter(n => n.type === 'type').length
      graphStats.periodCount = graphData.nodes.filter(n => n.type === 'period').length
      graphStats.materialCount = graphData.nodes.filter(n => n.type === 'material').length
    }

    // 更新节点详情列表
    updateNodeDetails()

    // 更新图表
    updateChart()

    ElMessage.success('图谱数据加载成功')
  } catch (error) {
    console.error('Failed to load graph data:', error)
    ElMessage.error('图谱数据加载失败，使用模拟数据')

    // 使用模拟数据
    loadMockData()
  } finally {
    loading.value = false
  }
}

// 加载模拟数据
const loadMockData = () => {
  // 模拟节点数据
  graphData.nodes = [
    { id: 'm1', name: '故宫博物院', type: 'museum', category: 0, symbolSize: 40, value: 256, itemStyle: { color: '#409EFF' } },
    { id: 'm2', name: '大都会艺术博物馆', type: 'museum', category: 0, symbolSize: 40, value: 198, itemStyle: { color: '#409EFF' } },
    { id: 'm3', name: '卢浮宫博物馆', type: 'museum', category: 0, symbolSize: 40, value: 312, itemStyle: { color: '#409EFF' } },
    { id: 't1', name: '青铜器', type: 'type', category: 2, symbolSize: 30, value: 432, itemStyle: { color: '#E6A23C' } },
    { id: 't2', name: '陶瓷器', type: 'type', category: 2, symbolSize: 30, value: 356, itemStyle: { color: '#E6A23C' } },
    { id: 't3', name: '玉器', type: 'type', category: 2, symbolSize: 30, value: 289, itemStyle: { color: '#E6A23C' } },
    { id: 'p1', name: '商代', type: 'period', category: 3, symbolSize: 25, value: 156, itemStyle: { color: '#F56C6C' } },
    { id: 'p2', name: '周代', type: 'period', category: 3, symbolSize: 25, value: 203, itemStyle: { color: '#F56C6C' } },
    { id: 'p3', name: '唐代', type: 'period', category: 3, symbolSize: 25, value: 178, itemStyle: { color: '#F56C6C' } },
    { id: 'mt1', name: '青铜', type: 'material', category: 4, symbolSize: 20, value: 234, itemStyle: { color: '#909399' } },
    { id: 'mt2', name: '陶瓷', type: 'material', category: 4, symbolSize: 20, value: 267, itemStyle: { color: '#909399' } },
    { id: 'r1', name: '商代青铜鼎', type: 'relic', category: 1, symbolSize: 35, value: 45, itemStyle: { color: '#67C23A' } },
    { id: 'r2', name: '周代青铜簋', type: 'relic', category: 1, symbolSize: 35, value: 38, itemStyle: { color: '#67C23A' } },
    { id: 'r3', name: '唐代三彩马', type: 'relic', category: 1, symbolSize: 35, value: 52, itemStyle: { color: '#67C23A' } },
    { id: 'r4', name: '宋代青花瓷', type: 'relic', category: 1, symbolSize: 35, value: 41, itemStyle: { color: '#67C23A' } },
    { id: 'r5', name: '商代玉璧', type: 'relic', category: 1, symbolSize: 35, value: 33, itemStyle: { color: '#67C23A' } }
  ]

  // 模拟关系数据
  graphData.edges = [
    { source: 'r1', target: 'm1', name: '收藏', lineStyle: { color: '#409EFF', width: 2, type: 'solid' } },
    { source: 'r2', target: 'm1', name: '收藏', lineStyle: { color: '#409EFF', width: 2, type: 'solid' } },
    { source: 'r3', target: 'm2', name: '收藏', lineStyle: { color: '#409EFF', width: 2, type: 'solid' } },
    { source: 'r4', target: 'm3', name: '收藏', lineStyle: { color: '#409EFF', width: 2, type: 'solid' } },
    { source: 'r5', target: 'm1', name: '收藏', lineStyle: { color: '#409EFF', width: 2, type: 'solid' } },
    { source: 'r1', target: 't1', name: '类型', lineStyle: { color: '#E6A23C', width: 1, type: 'dashed' } },
    { source: 'r2', target: 't1', name: '类型', lineStyle: { color: '#E6A23C', width: 1, type: 'dashed' } },
    { source: 'r3', target: 't2', name: '类型', lineStyle: { color: '#E6A23C', width: 1, type: 'dashed' } },
    { source: 'r4', target: 't2', name: '类型', lineStyle: { color: '#E6A23C', width: 1, type: 'dashed' } },
    { source: 'r5', target: 't3', name: '类型', lineStyle: { color: '#E6A23C', width: 1, type: 'dashed' } },
    { source: 'r1', target: 'p1', name: '时期', lineStyle: { color: '#F56C6C', width: 1, type: 'dashed' } },
    { source: 'r2', target: 'p2', name: '时期', lineStyle: { color: '#F56C6C', width: 1, type: 'dashed' } },
    { source: 'r3', target: 'p3', name: '时期', lineStyle: { color: '#F56C6C', width: 1, type: 'dashed' } },
    { source: 'r1', target: 'mt1', name: '材质', lineStyle: { color: '#909399', width: 1, type: 'dashed' } },
    { source: 'r2', target: 'mt1', name: '材质', lineStyle: { color: '#909399', width: 1, type: 'dashed' } },
    { source: 'r3', target: 'mt2', name: '材质', lineStyle: { color: '#909399', width: 1, type: 'dashed' } },
    { source: 'r4', target: 'mt2', name: '材质', lineStyle: { color: '#909399', width: 1, type: 'dashed' } },
    { source: 't1', target: 'mt1', name: '关联', lineStyle: { color: '#909399', width: 1, type: 'dashed' } },
    { source: 't2', target: 'mt2', name: '关联', lineStyle: { color: '#909399', width: 1, type: 'dashed' } }
  ]

  // 更新统计数据
  graphStats.nodeCount = graphData.nodes.length
  graphStats.edgeCount = graphData.edges.length
  graphStats.museumCount = graphData.nodes.filter(n => n.type === 'museum').length
  graphStats.relicCount = graphData.nodes.filter(n => n.type === 'relic').length
  graphStats.typeCount = graphData.nodes.filter(n => n.type === 'type').length
  graphStats.periodCount = graphData.nodes.filter(n => n.type === 'period').length
  graphStats.materialCount = graphData.nodes.filter(n => n.type === 'material').length

  // 更新节点详情列表
  updateNodeDetails()

  // 更新图表
  updateChart()
}

// 更新节点详情列表
const updateNodeDetails = () => {
  nodeDetails.value = graphData.nodes.map(node => {
    const relatedCount = graphData.edges.filter(
      edge => edge.source === node.id || edge.target === node.id
    ).length

    const descriptions: Record<string, string> = {
      museum: '收藏和展示珍贵文物的场所',
      relic: '具有历史、艺术、科学价值的文物',
      type: '文物的分类类型',
      period: '文物的历史时期',
      material: '文物的制作材质'
    }

    return {
      name: node.name,
      type: node.type,
      relatedCount,
      description: descriptions[node.type] || '知识图谱节点'
    }
  })
}

// 刷新
const handleRefresh = () => {
  loadGraphData()
}

// 查看详情
const handleViewDetail = (row: any) => {
  ElMessage.info(`查看节点详情: ${row.name}`)
}

// 高亮节点
const handleHighlight = (row: any) => {
  if (!chartInstance) return

  const node = graphData.nodes.find(n => n.name === row.name)
  if (node) {
    chartInstance.dispatchAction({
      type: 'highlight',
      dataType: 'node',
      name: node.name
    })
    ElMessage.success(`已高亮节点: ${row.name}`)
  }
}

// 窗口大小变化处理
const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

// 组件挂载
onMounted(() => {
  initChart()
  loadGraphData()
})

// 组件卸载
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
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

.graph-container {
  height: 500px;
  width: 100%;
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
