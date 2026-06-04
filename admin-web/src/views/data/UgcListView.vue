<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2>UGC管理</h2>
        <p>管理用户生成的讲解音频、评论和问答内容</p>
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
            <el-option label="讲解音频" value="AUDIO" />
            <el-option label="评论" value="COMMENT" />
            <el-option label="问答" value="QA" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" style="width: 140px">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" clearable placeholder="搜索内容" style="width: 180px" />
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
        <el-table-column label="内容" min-width="280" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="content-preview">{{ row.content }}</span>
          </template>
        </el-table-column>
        <el-table-column label="关联文物" min-width="160">
          <template #default="{ row }">
            {{ row.relicTitle || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="提交人" min-width="120">
          <template #default="{ row }">
            {{ row.authorName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="200" fixed="right">
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
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <el-drawer v-model="detailVisible" title="UGC详情" size="520px">
      <el-descriptions v-if="currentRecord" :column="1" border>
        <el-descriptions-item label="内容类型">{{ formatType(currentRecord.type) }}</el-descriptions-item>
        <el-descriptions-item label="关联文物">{{ currentRecord.relicTitle || '-' }}</el-descriptions-item>
        <el-descriptions-item label="提交人">{{ currentRecord.authorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRecord.createTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(currentRecord.status)">
            {{ formatStatus(currentRecord.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentRecord.audioUrl" label="音频URL">
          <el-link :href="currentRecord.audioUrl" type="primary" target="_blank">
            {{ currentRecord.audioUrl }}
          </el-link>
        </el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ currentRecord.auditTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核备注">{{ currentRecord.auditRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="驳回原因">{{ currentRecord.rejectReason || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="currentRecord" class="detail-content">
        <h3>内容正文</h3>
        <p>{{ currentRecord.content }}</p>
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

interface UgcRecord {
  objectId: string
  type: string
  content: string
  audioUrl?: string
  relicTitle?: string
  authorId: string
  authorName: string
  status: string
  createTime: string
  auditTime?: string
  auditRemark?: string
  rejectReason?: string
}

interface UgcStatistics {
  totalCount: number
  pendingCount: number
  approvedCount: number
  rejectedCount: number
  audioCount: number
  commentCount: number
  qaCount: number
}

const loading = ref(false)
const submitting = ref(false)
const records = ref<UgcRecord[]>([])
const selectedRows = ref<UgcRecord[]>([])
const total = ref(0)
const detailVisible = ref(false)
const rejectVisible = ref(false)
const rejectMode = ref<'single' | 'batch'>('single')
const currentRecord = ref<UgcRecord | null>(null)
const rejectFormRef = ref<FormInstance>()

const query = reactive({
  page: 1,
  pageSize: 10,
  type: '',
  status: '',
  keyword: ''
})

const statistics = reactive<UgcStatistics>({
  totalCount: 0,
  pendingCount: 0,
  approvedCount: 0,
  rejectedCount: 0,
  audioCount: 0,
  commentCount: 0,
  qaCount: 0
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
  { label: '总数量', value: statistics.totalCount },
  { label: '待审核', value: statistics.pendingCount },
  { label: '已通过', value: statistics.approvedCount },
  { label: '已驳回', value: statistics.rejectedCount },
  { label: '讲解音频', value: statistics.audioCount },
  { label: '评论/问答', value: statistics.commentCount + statistics.qaCount }
])

function formatType(type: string) {
  const map: Record<string, string> = {
    AUDIO: '讲解音频',
    COMMENT: '评论',
    QA: '问答'
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

function statusType(status: string) {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'warning'
}

function canSelect(row: UgcRecord) {
  return row.status === 'PENDING'
}

// 模拟数据
const mockData: UgcRecord[] = [
  {
    objectId: 'ugc-001',
    type: 'AUDIO',
    content: '这是一段关于青铜鼎的详细讲解音频，介绍了鼎的用途、历史背景和艺术价值。',
    audioUrl: 'https://example.com/audio/001.mp3',
    relicTitle: '后母戊鼎',
    authorId: 'user-001',
    authorName: '张三',
    status: 'PENDING',
    createTime: '2026-05-23 14:30:00'
  },
  {
    objectId: 'ugc-002',
    type: 'COMMENT',
    content: '这件瓷器的釉色非常漂亮，颜色层次分明。',
    relicTitle: '青花瓷瓶',
    authorId: 'user-002',
    authorName: '李四',
    status: 'APPROVED',
    createTime: '2026-05-23 10:20:00',
    auditTime: '2026-05-23 11:00:00',
    auditRemark: '内容合规，审核通过'
  },
  {
    objectId: 'ugc-003',
    type: 'QA',
    content: '这幅画是什么时期的创作？作者是谁？',
    relicTitle: '清明上河图',
    authorId: 'user-003',
    authorName: '王五',
    status: 'PENDING',
    createTime: '2026-05-23 09:15:00'
  },
  {
    objectId: 'ugc-004',
    type: 'COMMENT',
    content: '展厅的灯光设计很有特色，把文物的美感充分展现出来了。',
    relicTitle: '金缕玉衣',
    authorId: 'user-004',
    authorName: '赵六',
    status: 'REJECTED',
    createTime: '2026-05-22 16:45:00',
    auditTime: '2026-05-22 17:30:00',
    rejectReason: '评论内容与文物无关'
  },
  {
    objectId: 'ugc-005',
    type: 'AUDIO',
    content: '唐三彩骆驼俑讲解：这件文物出土于西安，是唐代丝绸之路繁荣的见证。',
    audioUrl: 'https://example.com/audio/005.mp3',
    relicTitle: '唐三彩骆驼',
    authorId: 'user-005',
    authorName: '孙七',
    status: 'APPROVED',
    createTime: '2026-05-22 11:00:00',
    auditTime: '2026-05-22 12:00:00',
    auditRemark: '讲解内容准确，通过审核'
  },
  {
    objectId: 'ugc-006',
    type: 'QA',
    content: '这个玉璧的年代如何确定？有哪些鉴定依据？',
    relicTitle: '战国玉璧',
    authorId: 'user-006',
    authorName: '周八',
    status: 'PENDING',
    createTime: '2026-05-22 08:30:00'
  },
  {
    objectId: 'ugc-007',
    type: 'COMMENT',
    content: '第一次看到这么精美的漆器，了解到了古代工艺的精湛。',
    relicTitle: '西汉漆盘',
    authorId: 'user-007',
    authorName: '吴九',
    status: 'APPROVED',
    createTime: '2026-05-21 15:20:00',
    auditTime: '2026-05-21 16:00:00'
  },
  {
    objectId: 'ugc-008',
    type: 'COMMENT',
    content: '希望博物馆能举办更多关于丝绸之路的展览。',
    relicTitle: '汉代织锦',
    authorId: 'user-008',
    authorName: '郑十',
    status: 'PENDING',
    createTime: '2026-05-21 10:00:00'
  }
]

async function loadData() {
  loading.value = true
  try {
    // 模拟API调用延迟
    await new Promise(resolve => setTimeout(resolve, 300))

    let filtered = [...mockData]

    if (query.type) {
      filtered = filtered.filter(item => item.type === query.type)
    }

    if (query.status) {
      filtered = filtered.filter(item => item.status === query.status)
    }

    if (query.keyword) {
      const kw = query.keyword.toLowerCase()
      filtered = filtered.filter(item =>
        item.content.toLowerCase().includes(kw) ||
        item.authorName.toLowerCase().includes(kw) ||
        (item.relicTitle && item.relicTitle.toLowerCase().includes(kw))
      )
    }

    total.value = filtered.length

    const start = (query.page - 1) * query.pageSize
    const end = start + query.pageSize
    records.value = filtered.slice(start, end)

    updateStatistics()
  } finally {
    loading.value = false
  }
}

function updateStatistics() {
  statistics.totalCount = mockData.length
  statistics.pendingCount = mockData.filter(item => item.status === 'PENDING').length
  statistics.approvedCount = mockData.filter(item => item.status === 'APPROVED').length
  statistics.rejectedCount = mockData.filter(item => item.status === 'REJECTED').length
  statistics.audioCount = mockData.filter(item => item.type === 'AUDIO').length
  statistics.commentCount = mockData.filter(item => item.type === 'COMMENT').length
  statistics.qaCount = mockData.filter(item => item.type === 'QA').length
}

function handleSearch() {
  query.page = 1
  loadData()
}

function resetSearch() {
  query.type = ''
  query.status = ''
  query.keyword = ''
  handleSearch()
}

function refreshPage() {
  loadData()
}

function handleSelectionChange(rows: UgcRecord[]) {
  selectedRows.value = rows
}

function openDetail(row: UgcRecord) {
  currentRecord.value = row
  detailVisible.value = true
}

async function approveOne(row: UgcRecord) {
  await ElMessageBox.confirm('确定通过这条内容吗？', '审核确认', {
    type: 'success',
    confirmButtonText: '通过',
    cancelButtonText: '取消'
  })

  row.status = 'APPROVED'
  row.auditTime = new Date().toLocaleString()
  row.auditRemark = '审核通过'

  ElMessage.success('内容已通过')
  detailVisible.value = false
  loadData()
}

async function batchApprove() {
  await ElMessageBox.confirm(`确定通过选中的 ${selectedIds.value.length} 条内容吗？`, '批量审核确认', {
    type: 'success',
    confirmButtonText: '通过',
    cancelButtonText: '取消'
  })

  selectedRows.value.forEach(row => {
    row.status = 'APPROVED'
    row.auditTime = new Date().toLocaleString()
    row.auditRemark = '批量审核通过'
  })

  ElMessage.success('批量通过完成')
  loadData()
}

function resetRejectForm() {
  rejectForm.reason = ''
  rejectForm.remark = ''
  rejectFormRef.value?.clearValidate()
}

function openRejectDialog(row: UgcRecord) {
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
      selectedRows.value.forEach(row => {
        row.status = 'REJECTED'
        row.auditTime = new Date().toLocaleString()
        row.rejectReason = rejectForm.reason
      })
      ElMessage.success('批量驳回完成')
    } else if (currentRecord.value) {
      currentRecord.value.status = 'REJECTED'
      currentRecord.value.auditTime = new Date().toLocaleString()
      currentRecord.value.rejectReason = rejectForm.reason
      if (rejectForm.remark) {
        currentRecord.value.auditRemark = rejectForm.remark
      }
      ElMessage.success('内容已驳回')
    }
    rejectVisible.value = false
    detailVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadData()
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

.drawer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 18px;
}
</style>
