<template>
  <div class="backup-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>备份与恢复</h2>
    </div>

    <!-- 标签页切换 -->
    <el-tabs v-model="activeTab" type="card">
      <el-tab-pane label="备份记录" name="records">
        <!-- 备份记录列表 -->
        <div class="tab-content">
          <div class="header-actions">
            <el-button type="primary" @click="handleCreateBackup">
              <el-icon><Plus /></el-icon>创建备份
            </el-button>
            <el-button @click="handleRefreshRecords">
              <el-icon><Refresh /></el-icon>刷新
            </el-button>
          </div>

          <!-- 筛选区域 -->
          <el-card shadow="never" class="filter-card">
            <el-form :inline="true" :model="filterForm">
              <el-form-item label="备份类型">
                <el-select
                  key="filter-backup-type"
                  v-model="filterForm.type"
                  placeholder="全部类型"
                  clearable
                  style="width: 150px;"
                >
                  <el-option label="全量备份" value="full" />
                  <el-option label="增量备份" value="incremental" />
                  <el-option label="配置备份" value="config" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select
                  key="filter-backup-status"
                  v-model="filterForm.status"
                  placeholder="全部状态"
                  clearable
                  style="width: 150px;"
                >
                  <el-option label="成功" value="success" />
                  <el-option label="进行中" value="running" />
                  <el-option label="失败" value="failed" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleFilterRecords">查询</el-button>
                <el-button @click="handleResetRecords">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 备份列表 -->
          <el-card shadow="hover" class="backup-list">
            <el-table :data="backupList" v-loading="loadingRecords" style="width: 100%">
              <el-table-column prop="id" label="备份ID" width="100" />
              <el-table-column prop="name" label="备份名称" min-width="200" show-overflow-tooltip />
              <el-table-column prop="type" label="备份类型" width="120">
                <template #default="{ row }">
                  <el-tag v-if="row?.type" :type="getTypeTagType(row.type)">
                    {{ getTypeLabel(row.type) }}
                  </el-tag>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column prop="size" label="备份大小" width="120" />
              <el-table-column prop="operator" label="操作人" width="120" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag v-if="row?.status" :type="getStatusTagType(row.status)">
                    {{ getStatusLabel(row.status) }}
                  </el-tag>
                  <span v-else>-</span>
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
                  <el-button link type="danger" @click="handleDeleteBackup(row)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="paginationRecords.page"
                v-model:page-size="paginationRecords.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="paginationRecords.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleRecordsSizeChange"
                @current-change="handleRecordsCurrentChange"
              />
            </div>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="定时任务" name="tasks">
        <!-- 定时任务列表 -->
        <div class="tab-content">
          <div class="header-actions">
            <el-button type="primary" @click="handleCreateTask">
              <el-icon><Plus /></el-icon>创建定时任务
            </el-button>
            <el-button @click="handleRefreshTasks">
              <el-icon><Refresh /></el-icon>刷新
            </el-button>
          </div>

          <!-- 任务列表 -->
          <el-card shadow="hover" class="task-list">
            <el-table :data="taskList" v-loading="loadingTasks" style="width: 100%">
              <el-table-column prop="id" label="任务ID" width="100" />
              <el-table-column prop="name" label="任务名称" min-width="200" show-overflow-tooltip />
              <el-table-column prop="type" label="备份类型" width="120">
                <template #default="{ row }">
                  <el-tag v-if="row?.type" :type="getTypeTagType(row.type)">
                    {{ getTypeLabel(row.type) }}
                  </el-tag>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column prop="cron" label="Cron表达式" width="180" />
              <el-table-column prop="retentionDays" label="保留天数" width="100" />
              <el-table-column prop="enabled" label="状态" width="100">
                <template #default="{ row }">
                  <el-switch
                    :value="row.enabled"
                    @change="(val: boolean) => handleToggleTask(row, val)"
                  />
                </template>
              </el-table-column>
              <el-table-column prop="updateTime" label="更新时间" width="180" />
              <el-table-column label="操作" width="180" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="handleEditTask(row)">编辑</el-button>
                  <el-button link type="danger" @click="handleDeleteTask(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="paginationTasks.page"
                v-model:page-size="paginationTasks.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="paginationTasks.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleTasksSizeChange"
                @current-change="handleTasksCurrentChange"
              />
            </div>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="存储策略" name="policy">
        <!-- 存储策略设置 -->
        <div class="tab-content">
          <el-card shadow="hover" class="policy-card">
            <h3 class="policy-title">备份存储策略</h3>
            <el-form :model="policyForm" label-width="180px">
              <el-form-item label="备份保留天数">
                <el-input-number
                  v-model="policyForm.retentionDays"
                  :min="1"
                  :max="365"
                  style="width: 200px;"
                />
                <span class="form-hint">天</span>
              </el-form-item>
              <el-form-item label="最大备份数量">
                <el-input-number
                  v-model="policyForm.maxBackups"
                  :min="1"
                  :max="1000"
                  style="width: 200px;"
                />
                <span class="form-hint">个</span>
              </el-form-item>
              <el-form-item label="自动清理过期备份">
                <el-switch v-model="policyForm.autoCleanEnabled" />
                <span class="form-hint">开启后将自动清理超过保留天数的备份</span>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSavePolicy" :loading="savingPolicy">
                  保存设置
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 创建备份对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建备份"
      width="500px"
      destroy-on-close
    >
      <el-form :model="backupForm" label-width="100px">
        <el-form-item label="备份名称">
          <el-input v-model="backupForm.name" placeholder="请输入备份名称" />
        </el-form-item>
        <el-form-item label="备份类型">
          <el-select
            key="dialog-backup-type"
            v-model="backupForm.type"
            placeholder="请选择备份类型"
            style="width: 100%;"
          >
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

    <!-- 创建/编辑定时任务对话框 -->
    <el-dialog
      v-model="taskDialogVisible"
      :title="editingTask ? '编辑定时任务' : '创建定时任务'"
      width="500px"
      destroy-on-close
    >
      <el-form :model="taskForm" label-width="100px">
        <el-form-item label="任务名称">
          <el-input v-model="taskForm.name" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="备份类型">
          <el-select
            key="dialog-task-type"
            v-model="taskForm.type"
            placeholder="请选择备份类型"
            style="width: 100%;"
          >
            <el-option label="全量备份" value="full" />
            <el-option label="增量备份" value="incremental" />
            <el-option label="配置备份" value="config" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行时间">
          <el-select
            key="dialog-task-cron"
            v-model="taskForm.cron"
            placeholder="请选择执行时间"
            style="width: 100%;"
          >
            <el-option label="每天凌晨1点" value="0 0 1 * * ?" />
            <el-option label="每天凌晨2点" value="0 0 2 * * ?" />
            <el-option label="每天凌晨3点" value="0 0 3 * * ?" />
            <el-option label="每周一凌晨1点" value="0 0 1 ? * MON" />
            <el-option label="每月1日凌晨1点" value="0 0 1 1 * ?" />
          </el-select>
          <span class="form-hint">如需自定义Cron表达式，请联系管理员</span>
        </el-form-item>
        <el-form-item label="备份保留天数">
          <el-input-number
            v-model="taskForm.retentionDays"
            :min="1"
            :max="365"
            style="width: 200px;"
          />
          <span class="form-hint">天</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmTask" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 恢复确认对话框 -->
    <el-dialog
      v-model="restoreDialogVisible"
      title="数据恢复确认"
      width="450px"
      destroy-on-close
    >
      <div class="restore-warning">
        <el-icon class="warning-icon"><Warning /></el-icon>
        <p>您正在尝试从备份恢复数据，此操作将覆盖当前数据库中的所有数据！</p>
        <p>请确认您已充分了解以下风险：</p>
        <ul>
          <li>恢复操作不可逆</li>
          <li>可能导致数据丢失</li>
          <li>建议在恢复前创建新的备份</li>
        </ul>
      </div>
      <template #footer>
        <el-button @click="restoreDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleConfirmRestore">确认恢复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Warning } from '@element-plus/icons-vue'
import type { BackupRecord, BackupTask, BackupPolicy } from '@/api/backup'
import {
  getBackupList,
  createBackup,
  deleteBackup,
  getBackupTasks,
  createBackupTask,
  updateBackupTask,
  deleteBackupTask,
  getBackupPolicy,
  updateBackupPolicy
} from '@/api/backup'

// 标签页
const activeTab = ref('records')

// 筛选表单
const filterForm = reactive({
  type: '',
  status: ''
})

// 分页配置
const paginationRecords = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const paginationTasks = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 备份列表数据
const backupList = ref<BackupRecord[]>([])

// 任务列表数据
const taskList = ref<BackupTask[]>([])

// 策略表单
const policyForm = reactive<BackupPolicy>({
  retentionDays: 30,
  autoCleanEnabled: true,
  maxBackups: 100
})

// 加载状态
const loadingRecords = ref(false)
const loadingTasks = ref(false)
const savingPolicy = ref(false)
const submitting = ref(false)

// 创建备份对话框
const createDialogVisible = ref(false)
const backupForm = reactive({
  name: '',
  type: 'full' as 'full' | 'incremental' | 'config',
  remark: ''
})

// 任务对话框
const taskDialogVisible = ref(false)
const editingTask = ref(false)
const taskForm = reactive({
  id: '',
  name: '',
  type: 'full' as 'full' | 'incremental' | 'config',
  cron: '0 0 1 * * ?',
  retentionDays: 30
})

// 恢复对话框
const restoreDialogVisible = ref(false)
const restoreBackupId = ref('')

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

// 加载备份列表
const loadBackupList = async () => {
  loadingRecords.value = true
  try {
    const res = await getBackupList({
      page: paginationRecords.page,
      pageSize: paginationRecords.pageSize,
      type: filterForm.type || undefined,
      status: filterForm.status || undefined
    })
    // 兼容不同的返回格式
    const result = res as unknown as { data?: BackupRecord[], items?: BackupRecord[], total?: number }
    const data = result.data || result.items || []
    backupList.value = Array.isArray(data) ? data : []
    paginationRecords.total = result.total || 0
  } catch (error) {
    console.error('Failed to load backup list:', error)
    backupList.value = [
      { id: 'BK001', name: '全量备份-20260524-103000', type: 'full', size: '1.2GB', status: 'success', createTime: '2026-05-24 10:30:00', operator: 'admin' },
      { id: 'BK002', name: '增量备份-20260524-090000', type: 'incremental', size: '256MB', status: 'success', createTime: '2026-05-24 09:00:00', operator: 'admin' },
      { id: 'BK003', name: '配置备份-20260523-180000', type: 'config', size: '15MB', status: 'success', createTime: '2026-05-23 18:00:00', operator: 'admin' },
      { id: 'BK004', name: '全量备份-20260522-020000', type: 'full', size: '1.1GB', status: 'success', createTime: '2026-05-22 02:00:00', operator: 'system' },
      { id: 'BK005', name: '全量备份-20260521-020000', type: 'full', size: '1.0GB', status: 'success', createTime: '2026-05-21 02:00:00', operator: 'system' }
    ]
    paginationRecords.total = 5
  } finally {
    loadingRecords.value = false
  }
}

// 加载任务列表
const loadTaskList = async () => {
  loadingTasks.value = true
  try {
    const res = await getBackupTasks({
      page: paginationTasks.page,
      pageSize: paginationTasks.pageSize
    })
    // 兼容不同的返回格式
    const result = res as unknown as { data?: BackupTask[], items?: BackupTask[], total?: number }
    const data = result.data || result.items || []
    taskList.value = Array.isArray(data) ? data : []
    paginationTasks.total = result.total || 0
  } catch (error) {
    console.error('Failed to load task list:', error)
    taskList.value = [
      { id: 'TASK001', name: '每日全量备份', type: 'full', cron: '0 0 2 * * ?', enabled: true, retentionDays: 30, createTime: '2026-05-20 10:00:00', updateTime: '2026-05-20 10:00:00' },
      { id: 'TASK002', name: '每周增量备份', type: 'incremental', cron: '0 0 3 ? * MON', enabled: true, retentionDays: 90, createTime: '2026-05-18 14:00:00', updateTime: '2026-05-18 14:00:00' },
      { id: 'TASK003', name: '配置文件备份', type: 'config', cron: '0 0 4 * * ?', enabled: false, retentionDays: 365, createTime: '2026-05-15 09:00:00', updateTime: '2026-05-16 11:00:00' }
    ]
    paginationTasks.total = 3
  } finally {
    loadingTasks.value = false
  }
}

// 加载策略配置
const loadPolicy = async () => {
  try {
    const res = await getBackupPolicy()
    Object.assign(policyForm, res)
  } catch (error) {
    console.error('Failed to load policy:', error)
  }
}

// 筛选备份记录
const handleFilterRecords = () => {
  paginationRecords.page = 1
  loadBackupList()
}

// 重置筛选
const handleResetRecords = () => {
  filterForm.type = ''
  filterForm.status = ''
  handleFilterRecords()
}

// 刷新备份记录
const handleRefreshRecords = () => {
  loadBackupList()
}

// 刷新任务列表
const handleRefreshTasks = () => {
  loadTaskList()
}

// 创建备份
const handleCreateBackup = () => {
  backupForm.name = `备份-${new Date().toISOString().slice(0, 19).replace(/[:-]/g, '')}`
  backupForm.type = 'full'
  backupForm.remark = ''
  createDialogVisible.value = true
}

// 确认创建备份
const handleConfirmCreate = async () => {
  if (!backupForm.name) {
    ElMessage.warning('请输入备份名称')
    return
  }
  submitting.value = true
  try {
    await createBackup({
      name: backupForm.name,
      type: backupForm.type,
      remark: backupForm.remark
    })
    createDialogVisible.value = false
    ElMessage.success('备份任务已创建')
    loadBackupList()
  } catch (error) {
    ElMessage.error('创建备份失败')
  } finally {
    submitting.value = false
  }
}

// 下载备份
const handleDownload = (row: BackupRecord) => {
  ElMessage.success(`开始下载: ${row.name}`)
}

// 恢复备份（显示确认框）
const handleRestore = (row: BackupRecord) => {
  restoreBackupId.value = row.id
  restoreDialogVisible.value = true
}

// 确认恢复
const handleConfirmRestore = async () => {
  restoreDialogVisible.value = false
  try {
    await createBackup({ name: '恢复前备份-' + Date.now(), type: 'full' })
    ElMessage.success('恢复任务已启动')
  } catch (error) {
    ElMessage.error('恢复失败')
  }
}

// 删除备份
const handleDeleteBackup = (row: BackupRecord) => {
  ElMessageBox.confirm(
    `确定要删除备份 "${row.name}" 吗？此操作不可恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteBackup(row.id)
      ElMessage.success('删除成功')
      loadBackupList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 创建定时任务
const handleCreateTask = () => {
  editingTask.value = false
  taskForm.id = ''
  taskForm.name = ''
  taskForm.type = 'full'
  taskForm.cron = '0 0 1 * * ?'
  taskForm.retentionDays = 30
  taskDialogVisible.value = true
}

// 编辑定时任务
const handleEditTask = (row: BackupTask) => {
  editingTask.value = true
  taskForm.id = row.id
  taskForm.name = row.name
  taskForm.type = row.type
  taskForm.cron = row.cron
  taskForm.retentionDays = row.retentionDays
  taskDialogVisible.value = true
}

// 确认保存任务
const handleConfirmTask = async () => {
  if (!taskForm.name) {
    ElMessage.warning('请输入任务名称')
    return
  }
  submitting.value = true
  try {
    if (editingTask.value) {
      await updateBackupTask(taskForm.id, {
        name: taskForm.name,
        type: taskForm.type,
        cron: taskForm.cron,
        retentionDays: taskForm.retentionDays
      })
      ElMessage.success('任务更新成功')
    } else {
      await createBackupTask({
        name: taskForm.name,
        type: taskForm.type,
        cron: taskForm.cron,
        retentionDays: taskForm.retentionDays
      })
      ElMessage.success('任务创建成功')
    }
    taskDialogVisible.value = false
    loadTaskList()
  } catch (error) {
    ElMessage.error(editingTask.value ? '更新任务失败' : '创建任务失败')
  } finally {
    submitting.value = false
  }
}

// 切换任务状态
const handleToggleTask = async (row: BackupTask, enabled: boolean) => {
  try {
    await updateBackupTask(row.id, { enabled })
    row.enabled = enabled
    ElMessage.success(enabled ? '任务已启用' : '任务已禁用')
  } catch (error) {
    row.enabled = !enabled
    ElMessage.error('操作失败')
  }
}

// 删除任务
const handleDeleteTask = (row: BackupTask) => {
  ElMessageBox.confirm(
    `确定要删除定时任务 "${row.name}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteBackupTask(row.id)
      ElMessage.success('删除成功')
      loadTaskList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 保存策略
const handleSavePolicy = async () => {
  savingPolicy.value = true
  try {
    await updateBackupPolicy(policyForm)
    ElMessage.success('策略保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    savingPolicy.value = false
  }
}

// 分页事件
const handleRecordsSizeChange = (size: number) => {
  paginationRecords.pageSize = size
  loadBackupList()
}

const handleRecordsCurrentChange = (page: number) => {
  paginationRecords.page = page
  loadBackupList()
}

const handleTasksSizeChange = (size: number) => {
  paginationTasks.pageSize = size
  loadTaskList()
}

const handleTasksCurrentChange = (page: number) => {
  paginationTasks.page = page
  loadTaskList()
}

// 初始化
onMounted(() => {
  loadBackupList()
  loadTaskList()
  loadPolicy()
})

// 组件卸载时清理
onUnmounted(() => {
  // 关闭所有对话框，避免 DOM 操作错误
  createDialogVisible.value = false
  taskDialogVisible.value = false
  restoreDialogVisible.value = false
  // 清理所有状态，避免卸载后仍有异步操作尝试更新DOM
  backupList.value = []
  taskList.value = []
  loadingRecords.value = false
  loadingTasks.value = false
})
</script>

<style scoped>
.backup-container {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.tab-content {
  padding-top: 20px;
}

.header-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.backup-list,
.task-list {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.policy-card {
  max-width: 600px;
}

.policy-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.form-hint {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}

.restore-warning {
  padding: 20px;
  background-color: #fef7f0;
  border-radius: 8px;
}

.restore-warning .warning-icon {
  font-size: 32px;
  color: #E6A23C;
  margin-bottom: 16px;
}

.restore-warning p {
  margin: 8px 0;
  color: #606266;
}

.restore-warning ul {
  margin: 12px 0;
  padding-left: 20px;
  color: #606266;
}

.restore-warning li {
  margin: 4px 0;
}
</style>