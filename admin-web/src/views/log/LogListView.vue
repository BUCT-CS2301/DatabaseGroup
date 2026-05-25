<template>
  <div class="log-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>日志管理</h2>
      <div class="header-actions">
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>导出日志
        </el-button>
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <!-- 筛选区域 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="日志类型">
          <el-select v-model="filterForm.type" placeholder="全部类型" clearable style="width: 150px;">
            <el-option label="操作日志" value="operation" />
            <el-option label="登录日志" value="login" />
            <el-option label="错误日志" value="error" />
            <el-option label="审计日志" value="audit" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作用户">
          <el-input v-model="filterForm.username" placeholder="请输入用户名" clearable style="width: 150px;" />
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
        <el-table-column prop="id" label="日志ID" width="80" />
        <el-table-column prop="type" label="日志类型" width="100">
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
    <el-dialog v-model="detailDialogVisible" title="日志详情" width="600px">
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
        <el-descriptions-item label="IP地址">{{ currentLog?.ip }}</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ currentLog?.createTime }}</el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">{{ currentLog?.description }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre class="json-content">{{ currentLog?.params }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="响应结果" :span="2">
          <pre class="json-content">{{ currentLog?.result }}</pre>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Refresh } from '@element-plus/icons-vue'

// 筛选表单
const filterForm = reactive({
  type: '',
  username: '',
  dateRange: [] as string[]
})

// 分页配置
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 日志列表数据
const logList = ref([
  {
    id: 'LOG001',
    type: 'operation',
    username: 'admin',
    module: '文物管理',
    action: '创建文物',
    description: '创建了新文物记录：青铜鼎',
    ip: '192.168.1.100',
    createTime: '2026-05-24 10:30:00',
    params: '{"title":"青铜鼎","period":"商代","type":"青铜器"}',
    result: '{"code":200,"message":"创建成功"}'
  },
  {
    id: 'LOG002',
    type: 'login',
    username: 'admin',
    module: '系统',
    action: '用户登录',
    description: '用户 admin 登录系统',
    ip: '192.168.1.100',
    createTime: '2026-05-24 10:25:00',
    params: '{"username":"admin"}',
    result: '{"code":200,"message":"登录成功"}'
  },
  {
    id: 'LOG003',
    type: 'error',
    username: 'system',
    module: '数据导入',
    action: 'CSV导入',
    description: 'CSV导入失败：数据格式错误',
    ip: '127.0.0.1',
    createTime: '2026-05-24 10:20:00',
    params: '{"filename":"relics.csv","rows":100}',
    result: '{"code":500,"message":"数据格式错误：第5行缺少必填字段"}'
  },
  {
    id: 'LOG004',
    type: 'audit',
    username: 'admin',
    module: '内容审核',
    action: '审核通过',
    description: '审核通过图文内容',
    ip: '192.168.1.100',
    createTime: '2026-05-24 10:15:00',
    params: '{"contentId":"CONT001","result":"PASS"}',
    result: '{"code":200,"message":"审核成功"}'
  },
  {
    id: 'LOG005',
    type: 'operation',
    username: 'admin',
    module: '用户管理',
    action: '修改密码',
    description: '修改用户密码',
    ip: '192.168.1.100',
    createTime: '2026-05-24 10:10:00',
    params: '{"userId":"USER001"}',
    result: '{"code":200,"message":"密码修改成功"}'
  }
])

// 加载状态
const loading = ref(false)

// 详情对话框
const detailDialogVisible = ref(false)
const currentLog = ref<any>(null)

// 获取类型标签颜色
const getTypeTagType = (type: string) => {
  const map: Record<string, string> = {
    operation: 'primary',
    login: 'success',
    error: 'danger',
    audit: 'warning'
  }
  return map[type] || 'info'
}

// 获取类型标签文本
const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    operation: '操作日志',
    login: '登录日志',
    error: '错误日志',
    audit: '审计日志'
  }
  return map[type] || type
}

// 筛选
const handleFilter = () => {
  pagination.page = 1
  ElMessage.success('筛选成功')
}

// 重置筛选
const handleReset = () => {
  filterForm.type = ''
  filterForm.username = ''
  filterForm.dateRange = []
  handleFilter()
}

// 刷新
const handleRefresh = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('刷新成功')
  }, 500)
}

// 导出日志
const handleExport = () => {
  ElMessage.success('开始导出日志')
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
</style>
