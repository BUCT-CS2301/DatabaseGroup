<template>
  <div class="backup-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>备份与恢复</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreateBackup">
          <el-icon><Plus /></el-icon>创建备份
        </el-button>
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <!-- 筛选区域 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="备份类型">
          <el-select v-model="filterForm.type" placeholder="全部类型" clearable style="width: 150px;">
            <el-option label="全量备份" value="full" />
            <el-option label="增量备份" value="incremental" />
            <el-option label="配置备份" value="config" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 150px;">
            <el-option label="成功" value="success" />
            <el-option label="进行中" value="running" />
            <el-option label="失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 备份列表 -->
    <el-card shadow="hover" class="backup-list">
      <el-table :data="backupList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="备份ID" width="100" />
        <el-table-column prop="name" label="备份名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="备份类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="size" label="备份大小" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDownload(row)" :disabled="row.status !== 'success'">
              下载
            </el-button>
            <el-button link type="success" @click="handleRestore(row)" :disabled="row.status !== 'success'">
              恢复
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              删除
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

    <!-- 创建备份对话框 -->
    <el-dialog v-model="createDialogVisible" title="创建备份" width="500px">
      <el-form :model="backupForm" label-width="100px">
        <el-form-item label="备份名称">
          <el-input v-model="backupForm.name" placeholder="请输入备份名称" />
        </el-form-item>
        <el-form-item label="备份类型">
          <el-select v-model="backupForm.type" placeholder="请选择备份类型" style="width: 100%;">
            <el-option label="全量备份" value="full" />
            <el-option label="增量备份" value="incremental" />
            <el-option label="配置备份" value="config" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="backupForm.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmCreate" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'

// 筛选表单
const filterForm = reactive({
  type: '',
  status: ''
})

// 分页配置
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 备份列表数据
const backupList = ref([
  {
    id: 'BK001',
    name: '全量备份-20260524-103000',
    type: 'full',
    size: '1.2GB',
    status: 'success',
    createTime: '2026-05-24 10:30:00'
  },
  {
    id: 'BK002',
    name: '增量备份-20260524-090000',
    type: 'incremental',
    size: '256MB',
    status: 'success',
    createTime: '2026-05-24 09:00:00'
  },
  {
    id: 'BK003',
    name: '配置备份-20260523-180000',
    type: 'config',
    size: '15MB',
    status: 'success',
    createTime: '2026-05-23 18:00:00'
  }
])

// 加载状态
const loading = ref(false)
const submitting = ref(false)

// 创建备份对话框
const createDialogVisible = ref(false)
const backupForm = reactive({
  name: '',
  type: 'full',
  remark: ''
})

// 获取类型标签颜色
const getTypeTagType = (type: string) => {
  const map: Record<string, string> = {
    full: 'primary',
    incremental: 'warning',
    config: 'info'
  }
  return map[type] || 'info'
}

// 获取类型标签文本
const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    full: '全量备份',
    incremental: '增量备份',
    config: '配置备份'
  }
  return map[type] || type
}

// 获取状态标签颜色
const getStatusTagType = (status: string) => {
  const map: Record<string, string> = {
    success: 'success',
    running: 'warning',
    failed: 'danger'
  }
  return map[status] || 'info'
}

// 获取状态标签文本
const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    success: '成功',
    running: '进行中',
    failed: '失败'
  }
  return map[status] || status
}

// 筛选
const handleFilter = () => {
  pagination.page = 1
  ElMessage.success('筛选成功')
}

// 重置筛选
const handleReset = () => {
  filterForm.type = ''
  filterForm.status = ''
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

// 创建备份
const handleCreateBackup = () => {
  backupForm.name = `备份-${new Date().toISOString().slice(0, 19).replace(/[:-]/g, '')}`
  backupForm.type = 'full'
  backupForm.remark = ''
  createDialogVisible.value = true
}

// 确认创建
const handleConfirmCreate = async () => {
  if (!backupForm.name) {
    ElMessage.warning('请输入备份名称')
    return
  }
  submitting.value = true
  setTimeout(() => {
    submitting.value = false
    createDialogVisible.value = false
    ElMessage.success('备份任务已创建')
    handleRefresh()
  }, 1000)
}

// 下载备份
const handleDownload = (row: any) => {
  ElMessage.success(`开始下载: ${row.name}`)
}

// 恢复备份
const handleRestore = (row: any) => {
  ElMessageBox.confirm(
    `确定要恢复备份 "${row.name}" 吗？恢复操作将覆盖当前数据。`,
    '恢复确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success('恢复任务已启动')
  }).catch(() => {})
}

// 删除备份
const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除备份 "${row.name}" 吗？此操作不可恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success('删除成功')
    handleRefresh()
  }).catch(() => {})
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
.backup-container {
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

.backup-list {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
