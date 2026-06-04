<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2>内容审核</h2>
        <p>处理待审文本、图片和用户提交内容</p>
      </div>
      <div class="header-actions">
        <el-button :icon="Refresh" @click="refreshPage">刷新</el-button>
        <el-button type="success" :icon="Check" :disabled="!selectedIds.length" @click="batchApprove">
          批量通过
        </el-button>
        <el-button type="danger" :icon="Close" :disabled="!selectedIds.length" @click="openBatchReject">
          批量驳回
        </el-button>
      </div>
    </div>

    <div class="stats-grid">
      <el-card v-for="item in statCards" :key="item.label" shadow="never" class="stat-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </el-card>
    </div>

    <el-card class="toolbar" shadow="never">
      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="内容类型">
          <el-select v-model="query.type" clearable placeholder="全部类型" style="width: 140px">
            <el-option label="文本" value="TEXT" />
            <el-option label="图片" value="IMAGE" />
            <el-option label="评论" value="COMMENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" style="width: 140px">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="records"
        row-key="objectId"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" :selectable="canSelect" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag effect="plain">{{ formatType(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="内容" min-width="320" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="content-preview">{{ row.content }}</span>
          </template>
        </el-table-column>
        <el-table-column label="提交人" min-width="160">
          <template #default="{ row }">
            {{ row.author?.objectId || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="机审结果" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="autoResultType(row.autoAuditResult)">
              {{ formatAutoResult(row.autoAuditResult) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" min-width="170" />
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="openDetail(row)">详情</el-button>
            <el-button
              type="success"
              link
              :icon="Check"
              :disabled="row.status !== 'PENDING'"
              @click="approveOne(row)"
            >
              通过
            </el-button>
            <el-button
              type="danger"
              link
              :icon="Close"
              :disabled="row.status !== 'PENDING'"
              @click="openRejectDialog(row)"
            >
              驳回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50]"
          :total="total"
          @size-change="loadQueue"
          @current-change="loadQueue"
        />
      </div>
    </el-card>

    <el-drawer v-model="detailVisible" title="审核详情" size="520px">
      <el-descriptions v-if="currentRecord" :column="1" border>
        <el-descriptions-item label="内容类型">{{ formatType(currentRecord.type) }}</el-descriptions-item>
        <el-descriptions-item label="提交人">{{ currentRecord.author?.objectId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ currentRecord.submitTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ formatStatus(currentRecord.status) }}</el-descriptions-item>
        <el-descriptions-item label="机审结果">{{ formatAutoResult(currentRecord.autoAuditResult) }}</el-descriptions-item>
        <el-descriptions-item label="机审说明">{{ currentRecord.autoAuditDetail || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核备注">{{ currentRecord.auditRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="驳回原因">{{ currentRecord.rejectReason || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="currentRecord" class="detail-content">
        <h3>内容正文</h3>
        <p>{{ currentRecord.content }}</p>
        <el-image v-if="currentRecord.contentUrl" :src="currentRecord.contentUrl" fit="contain" />
      </div>
      <div v-if="currentRecord?.status === 'PENDING'" class="drawer-actions">
        <el-button type="success" :icon="Check" @click="approveOne(currentRecord)">通过</el-button>
        <el-button type="danger" :icon="Close" @click="openRejectDialog(currentRecord)">驳回</el-button>
      </div>
    </el-drawer>

    <el-dialog v-model="rejectVisible" :title="rejectMode === 'batch' ? '批量驳回' : '驳回内容'" width="500px" destroy-on-close>
      <el-form ref="rejectFormRef" :model="rejectForm" :rules="rejectRules" label-width="86px">
        <el-form-item label="驳回原因" prop="reason">
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请填写驳回原因"
          />
        </el-form-item>
        <el-form-item v-if="rejectMode === 'single'" label="审核备注">
          <el-input v-model="rejectForm.remark" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Close, Refresh, Search, View } from '@element-plus/icons-vue'
import {
  approveAudit,
  batchApproveAudit,
  batchRejectAudit,
  getAuditDetail,
  getAuditQueue,
  getAuditStatistics,
  rejectAudit,
  type AuditRecord,
  type AuditStatistics
} from '@/api/audit'

const loading = ref(false)
const submitting = ref(false)
const records = ref<AuditRecord[]>([])
const selectedRows = ref<AuditRecord[]>([])
const total = ref(0)
const detailVisible = ref(false)
const rejectVisible = ref(false)
const rejectMode = ref<'single' | 'batch'>('single')
const currentRecord = ref<AuditRecord | null>(null)
const rejectFormRef = ref<FormInstance>()

const query = reactive({
  page: 1,
  pageSize: 10,
  type: '',
  status: 'PENDING'
})

const statistics = reactive<AuditStatistics>({
  totalSubmitted: 0,
  autoApproved: 0,
  autoRejected: 0,
  manualApproved: 0,
  manualRejected: 0,
  pendingCount: 0
})

const rejectForm = reactive({
  reason: '',
  remark: ''
})

const rejectRules: FormRules = {
  reason: [{ required: true, message: '请填写驳回原因', trigger: 'blur' }]
}

const selectedIds = computed(() => selectedRows.value.map(row => row.objectId))
const statCards = computed(() => [
  { label: '总提交', value: statistics.totalSubmitted },
  { label: '待审核', value: statistics.pendingCount },
  { label: '人工通过', value: statistics.manualApproved },
  { label: '人工驳回', value: statistics.manualRejected },
  { label: '机审通过', value: statistics.autoApproved },
  { label: '机审驳回', value: statistics.autoRejected }
])

function formatType(type: string) {
  const map: Record<string, string> = {
    TEXT: '文本',
    IMAGE: '图片',
    COMMENT: '评论'
  }
  return map[type] || type || '-'
}

function formatStatus(status: string) {
  const map: Record<string, string> = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已驳回'
  }
  return map[status] || status || '-'
}

function formatAutoResult(result: string) {
  const map: Record<string, string> = {
    PASS: '通过',
    REJECT: '拦截',
    MANUAL: '人工'
  }
  return map[result] || result || '-'
}

function statusType(status: string) {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'warning'
}

function autoResultType(result: string) {
  if (result === 'PASS') return 'success'
  if (result === 'REJECT') return 'danger'
  return 'warning'
}

function canSelect(row: AuditRecord) {
  return row.status === 'PENDING'
}

async function loadQueue() {
  loading.value = true
  try {
    const res = await getAuditQueue(query)
    records.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

async function loadStatistics() {
  const res = await getAuditStatistics()
  Object.assign(statistics, res.data)
}

function handleSearch() {
  query.page = 1
  loadQueue()
}

function resetSearch() {
  query.type = ''
  query.status = ''
  handleSearch()
}

function refreshPage() {
  loadQueue()
  loadStatistics()
}

function handleSelectionChange(rows: AuditRecord[]) {
  selectedRows.value = rows
}

async function openDetail(row: AuditRecord) {
  const res = await getAuditDetail(row.objectId)
  currentRecord.value = res.data
  detailVisible.value = true
}

async function approveOne(row: AuditRecord) {
  await ElMessageBox.confirm('确定通过这条内容吗？', '审核确认', {
    type: 'success',
    confirmButtonText: '通过',
    cancelButtonText: '取消'
  })
  await approveAudit(row.objectId, '审核通过')
  ElMessage.success('内容已通过')
  detailVisible.value = false
  refreshPage()
}

async function batchApprove() {
  await ElMessageBox.confirm(`确定通过选中的 ${selectedIds.value.length} 条内容吗？`, '批量审核确认', {
    type: 'success',
    confirmButtonText: '通过',
    cancelButtonText: '取消'
  })
  await batchApproveAudit(selectedIds.value, '批量审核通过')
  ElMessage.success('批量通过完成')
  refreshPage()
}

function resetRejectForm() {
  rejectForm.reason = ''
  rejectForm.remark = ''
  rejectFormRef.value?.clearValidate()
}

function openRejectDialog(row: AuditRecord) {
  currentRecord.value = row
  rejectMode.value = 'single'
  resetRejectForm()
  rejectVisible.value = true
}

function openBatchReject() {
  rejectMode.value = 'batch'
  resetRejectForm()
  rejectVisible.value = true
}

async function confirmReject() {
  await rejectFormRef.value?.validate()
  submitting.value = true
  try {
    if (rejectMode.value === 'batch') {
      await batchRejectAudit(selectedIds.value, rejectForm.reason)
      ElMessage.success('批量驳回完成')
    } else if (currentRecord.value) {
      await rejectAudit(currentRecord.value.objectId, rejectForm.reason, rejectForm.remark)
      ElMessage.success('内容已驳回')
    }
    rejectVisible.value = false
    detailVisible.value = false
    refreshPage()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  refreshPage()
})

// 组件卸载时清理
onUnmounted(() => {
  rejectVisible.value = false
})
</script>

<style scoped>
.page {
  min-width: 1040px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
}

.page-header p {
  margin: 6px 0 0;
  color: #7a8599;
  font-size: 13px;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(120px, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px 16px;
}

.stat-card span {
  color: #7a8599;
  font-size: 13px;
}

.stat-card strong {
  color: #1f2d3d;
  font-size: 24px;
  line-height: 1;
}

.toolbar {
  margin-bottom: 14px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  row-gap: 8px;
}

.content-preview {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.detail-content {
  margin-top: 18px;
}

.detail-content h3 {
  margin: 0 0 10px;
  font-size: 16px;
}

.detail-content p {
  margin: 0;
  padding: 12px;
  min-height: 120px;
  line-height: 1.7;
  white-space: pre-wrap;
  background: #f6f8fb;
  border: 1px solid #edf0f5;
  border-radius: 6px;
}

.detail-content .el-image {
  width: 100%;
  max-height: 260px;
  margin-top: 12px;
  background: #f6f8fb;
  border-radius: 6px;
}

.drawer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 18px;
}
</style>
