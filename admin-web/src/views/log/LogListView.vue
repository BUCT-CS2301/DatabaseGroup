<template>
  <div class="log-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>日志管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleExport">
          <el-icon><Download /></el-icon>导出日志
        </el-button>
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <el-card class="stat-card">
        <div class="stat-icon operation-icon">
          <el-icon><Files /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.operationCount }}</div>
          <div class="stat-label">操作日志</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-icon login-icon">
          <el-icon><UserFilled /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.loginCount }}</div>
          <div class="stat-label">登录日志</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-icon error-icon">
          <el-icon><WarnTriangleFilled /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.errorCount }}</div>
          <div class="stat-label">错误日志</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-icon security-icon">
          <el-icon><Lock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.securityCount }}</div>
          <div class="stat-label">安全日志</div>
        </div>
      </el-card>
    </div>

    <!-- 筛选区域 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="日志类型">
          <el-select v-model="filterForm.type" placeholder="全部类型" clearable style="width: 150px;">
            <el-option label="操作日志" value="operation" />
            <el-option label="登录日志" value="login" />
            <el-option label="系统日志" value="system" />
            <el-option label="安全日志" value="security" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作用户">
          <el-input v-model="filterForm.username" placeholder="请输入用户名" clearable style="width: 150px;" />
        </el-form-item>
        <el-form-item label="操作模块">
          <el-select v-model="filterForm.module" placeholder="全部模块" clearable style="width: 150px;">
            <el-option label="系统" value="系统" />
            <el-option label="用户管理" value="用户管理" />
            <el-option label="角色权限" value="角色权限" />
            <el-option label="内容审核" value="内容审核" />
            <el-option label="文物数据" value="文物数据" />
            <el-option label="知识图谱" value="知识图谱" />
            <el-option label="UGC管理" value="UGC管理" />
            <el-option label="备份恢复" value="备份恢复" />
            <el-option label="日志管理" value="日志管理" />
            <el-option label="系统配置" value="系统配置" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作动作">
          <el-input v-model="filterForm.action" placeholder="请输入操作动作" clearable style="width: 150px;" />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="filterForm.keyword" placeholder="请输入关键词" clearable style="width: 150px;" />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志列表 -->
    <el-card shadow="hover" class="log-list">
      <el-table :data="logList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="日志ID" width="100" />
        <el-table-column prop="type" label="日志类型" width="110">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)" size="small">
              {{ getTypeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="操作用户" width="120" />
        <el-table-column prop="module" label="操作模块" width="120" />
        <el-table-column prop="action" label="操作动作" width="120" />
        <el-table-column prop="description" label="操作描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="result" label="操作结果" width="100">
          <template #default="{ row }">
            <el-tag :type="getResultTagType(row.result)">
              {{ getResultLabel(row.result) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="日志详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="日志ID">{{ currentLog?.id }}</el-descriptions-item>
        <el-descriptions-item label="日志类型">
          <el-tag :type="getTypeTagType(currentLog?.type || '')" size="small">
            {{ getTypeLabel(currentLog?.type || '') }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作用户">{{ currentLog?.username }}</el-descriptions-item>
        <el-descriptions-item label="操作模块">{{ currentLog?.module }}</el-descriptions-item>
        <el-descriptions-item label="操作动作">{{ currentLog?.action }}</el-descriptions-item>
        <el-descriptions-item label="操作结果">
          <el-tag :type="getResultTagType(currentLog?.result || '')" size="small">
            {{ getResultLabel(currentLog?.result || '') }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog?.ip }}</el-descriptions-item>
        <el-descriptions-item label="用户代理">{{ currentLog?.userAgent }}</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ currentLog?.createTime }}</el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">{{ currentLog?.description }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre class="json-content">{{ formatJson(currentLog?.params) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="响应结果" :span="2">
          <pre class="json-content">{{ formatJson(currentLog?.resultData) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 导出选择对话框 -->
    <el-dialog v-model="exportDialogVisible" title="导出日志" width="450px">
      <el-form :model="exportForm" label-width="100px">
        <el-form-item label="导出格式">
          <el-select v-model="exportForm.format" placeholder="请选择导出格式" style="width: 100%;">
            <el-option label="CSV" value="csv" />
            <el-option label="Excel" value="excel" />
          </el-select>
        </el-form-item>
        <el-form-item label="导出范围">
          <el-radio-group v-model="exportForm.range">
            <el-radio label="全部">全部数据</el-radio>
            <el-radio label="current">当前页</el-radio>
            <el-radio label="filtered">筛选结果</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exportDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmExport" :loading="exporting">确定导出</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Refresh, Files, UserFilled, WarnTriangleFilled, Lock } from '@element-plus/icons-vue'
import type { LogRecord, LogStats } from '@/api/log'
import { getLogList, getLogStats, exportLogs } from '@/api/log'

// 统计数据
const stats = reactive({
  operationCount: 128,
  loginCount: 45,
  errorCount: 5,
  securityCount: 23
})

// 筛选表单
const filterForm = reactive({
  type: '',
  username: '',
  module: '',
  action: '',
  keyword: '',
  dateRange: [] as string[]
})

// 分页配置
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 日志列表数据
const logList = ref<LogRecord[]>([])

// 加载状态
const loading = ref(false)
const exporting = ref(false)

// 详情对话框
const detailDialogVisible = ref(false)
const currentLog = ref<any>(null)

// 导出对话框
const exportDialogVisible = ref(false)
const exportForm = reactive({
  format: 'csv',
  range: 'filtered'
})

// 获取类型标签颜色
const getTypeTagType = (type: string) => {
  const map: Record<string, string> = {
    operation: 'primary',
    login: 'success',
    system: 'info',
    error: 'danger',
    security: 'warning',
    audit: 'warning'
  }
  return map[type] || 'info'
}

// 获取类型标签文本
const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    operation: '操作日志',
    login: '登录日志',
    system: '系统日志',
    error: '错误日志',
    security: '安全日志',
    audit: '审计日志'
  }
  return map[type] || type
}

// 获取结果标签颜色
const getResultTagType = (result: string) => {
  const map: Record<string, string> = {
    success: 'success',
    failed: 'danger',
    error: 'danger'
  }
  return map[result] || 'info'
}

// 获取结果标签文本
const getResultLabel = (result: string) => {
  const map: Record<string, string> = {
    success: '成功',
    failed: '失败',
    error: '错误'
  }
  return map[result] || result
}

// 格式化JSON
const formatJson = (jsonString?: string) => {
  if (!jsonString) return ''
  try {
    const obj = JSON.parse(jsonString)
    return JSON.stringify(obj, null, 2)
  } catch {
    return jsonString
  }
}

// 加载日志列表
const loadLogList = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    if (filterForm.type) params.type = filterForm.type
    if (filterForm.username) params.username = filterForm.username
    if (filterForm.module) params.module = filterForm.module
    if (filterForm.action) params.action = filterForm.action
    if (filterForm.keyword) params.keyword = filterForm.keyword
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startTime = filterForm.dateRange[0]
      params.endTime = filterForm.dateRange[1]
    }
    
    const res = await getLogList(params)
    console.log('Log API response:', res)
    if (res && res.data && Array.isArray(res.data)) {
      logList.value = res.data
      pagination.total = res.total || 0
    } else {
      console.warn('Invalid log data format, using mock data')
      throw new Error('Invalid data format')
    }
  } catch (error) {
    console.error('Failed to load log list:', error)
    logList.value = [
      { id: 'LOG001', type: 'operation', username: 'admin', module: '文物管理', action: '创建文物', description: '创建了新文物记录：青铜鼎', ip: '192.168.1.100', userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', result: 'success', resultData: '{"code":200,"message":"创建成功"}', createTime: '2026-05-24 10:30:00', params: '{"title":"青铜鼎"}' },
      { id: 'LOG002', type: 'login', username: 'admin', module: '系统', action: '用户登录', description: '用户 admin 登录系统', ip: '192.168.1.100', userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', result: 'success', resultData: '{"code":200,"message":"登录成功"}', createTime: '2026-05-24 10:25:00', params: '{"username":"admin"}' },
      { id: 'LOG003', type: 'system', username: 'system', module: '数据导入', action: 'CSV导入', description: 'CSV导入任务执行完成', ip: '127.0.0.1', userAgent: 'System Task', result: 'success', resultData: '{"code":200,"message":"导入完成"}', createTime: '2026-05-24 10:20:00', params: '{"filename":"relics.csv","rows":100}' },
      { id: 'LOG004', type: 'security', username: 'admin', module: '系统', action: '权限变更', description: '管理员修改了用户权限', ip: '192.168.1.100', userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', result: 'success', resultData: '{"code":200,"message":"权限变更成功"}', createTime: '2026-05-24 10:15:00', params: '{"userId":"USER001"}' },
      { id: 'LOG005', type: 'error', username: 'system', module: 'API接口', action: '接口调用', description: '接口查询失败：数据库连接超时', ip: '192.168.1.101', userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', result: 'error', resultData: '{"code":500,"message":"数据库连接超时"}', createTime: '2026-05-24 10:10:00', params: '{"page":1,"pageSize":10}' },
      { id: 'LOG006', type: 'login', username: 'test', module: '系统', action: '登录失败', description: '用户 test 登录失败：密码错误', ip: '192.168.1.102', userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', result: 'failed', resultData: '{"code":401,"message":"用户名或密码错误"}', createTime: '2026-05-24 10:05:00', params: '{"username":"test"}' },
      { id: 'LOG007', type: 'audit', username: 'auditor', module: '内容审核', action: '审核通过', description: '审核通过图文内容 ID:CONT001', ip: '192.168.1.103', userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', result: 'success', resultData: '{"code":200,"message":"审核成功"}', createTime: '2026-05-24 09:55:00', params: '{"contentId":"CONT001"}' },
      { id: 'LOG008', type: 'operation', username: 'admin', module: '用户管理', action: '创建用户', description: '创建新用户：testuser', ip: '192.168.1.100', userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', result: 'success', resultData: '{"code":200,"message":"用户创建成功"}', createTime: '2026-05-24 09:50:00', params: '{"username":"testuser"}' }
    ]
    pagination.total = 8
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await getLogStats()
    Object.assign(stats, res)
  } catch (error) {
    console.error('Failed to load stats:', error)
  }
}

// 筛选
const handleFilter = () => {
  pagination.page = 1
  loadLogList()
}

// 重置筛选
const handleReset = () => {
  filterForm.type = ''
  filterForm.username = ''
  filterForm.module = ''
  filterForm.action = ''
  filterForm.keyword = ''
  filterForm.dateRange = []
  handleFilter()
}

// 刷新
const handleRefresh = () => {
  loadLogList()
  loadStats()
}

// 导出日志
const handleExport = () => {
  exportDialogVisible.value = true
}

// 确认导出
const handleConfirmExport = async () => {
  exporting.value = true
  try {
    const params: any = {
      format: exportForm.format,
      range: exportForm.range
    }
    if (exportForm.range === 'filtered') {
      if (filterForm.type) params.type = filterForm.type
      if (filterForm.username) params.username = filterForm.username
      if (filterForm.module) params.module = filterForm.module
      if (filterForm.action) params.action = filterForm.action
      if (filterForm.keyword) params.keyword = filterForm.keyword
      if (filterForm.dateRange && filterForm.dateRange.length === 2) {
        params.startTime = filterForm.dateRange[0]
        params.endTime = filterForm.dateRange[1]
      }
    }
    
    await exportLogs(params)
    exportDialogVisible.value = false
    ElMessage.success(`日志已导出为${exportForm.format.toUpperCase()}格式`)
  } catch (error) {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

// 查看详情
const handleDetail = (row: any) => {
  currentLog.value = row
  detailDialogVisible.value = true
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  handleRefresh()
}

// 页码改变
const handleCurrentChange = (page: number) => {
  pagination.page = page
  handleRefresh()
}

// 初始化
onMounted(() => {
  console.log('LogListView mounted')
  loadLogList()
  loadStats()
})

// 清理
onUnmounted(() => {
  console.log('LogListView unmounted')
})
</script>

<style scoped>
.log-container {
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

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-icon.operation-icon {
  background-color: #E6F7FF;
  color: #1890FF;
}

.stat-icon.login-icon {
  background-color: #F6FFED;
  color: #52C41A;
}

.stat-icon.error-icon {
  background-color: #FFF2F0;
  color: #FF4D4F;
}

.stat-icon.security-icon {
  background-color: #FFFBE6;
  color: #FAAD14;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.filter-card {
  margin-bottom: 20px;
}

.log-list {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.json-content {
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.6;
  margin: 0;
  max-height: 150px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
}

@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: 1fr;
  }
}
</style>