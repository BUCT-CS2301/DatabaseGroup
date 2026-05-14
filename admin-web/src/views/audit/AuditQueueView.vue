<template>
  <div class="audit-queue-container">
    <div class="header">
      <h2>内容审核</h2>
      <div class="header-actions">
        <el-button type="success" @click="handleBatchApprove" :disabled="selectedIds.length === 0">
          批量通过
        </el-button>
        <el-button type="danger" @click="handleBatchReject" :disabled="selectedIds.length === 0">
          批量拒绝
        </el-button>
      </div>
    </div>

    <div class="filter-bar">
      <el-select v-model="contentTypeFilter" placeholder="内容类型">
        <el-option label="全部" value="ALL" />
        <el-option label="评论" value="COMMENT" />
        <el-option label="图片" value="IMAGE" />
        <el-option label="音频" value="AUDIO" />
        <el-option label="视频" value="VIDEO" />
        <el-option label="动态" value="DYNAMIC" />
      </el-select>
      <el-select v-model="statusFilter" placeholder="审核状态">
        <el-option label="全部" value="ALL" />
        <el-option label="待审核" value="PENDING" />
        <el-option label="已通过" value="APPROVED" />
        <el-option label="已拒绝" value="REJECTED" />
      </el-select>
      <el-button type="primary" @click="loadQueue">查询</el-button>
    </div>

    <el-table :data="queueList" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="objectId" label="ID" width="120" />
      <el-table-column prop="contentType" label="内容类型">
        <template #default="scope">
          <el-tag>{{ getContentTypeLabel(scope.row.contentType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="contentText" label="内容" :show-overflow-tooltip="true" />
      <el-table-column prop="autoAuditResult" label="自动审核">
        <template #default="scope">
          <el-tag :type="getAutoAuditTag(scope.row.autoAuditResult)">
            {{ getAutoAuditLabel(scope.row.autoAuditResult) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <el-tag :type="getStatusTag(scope.row.status)">
            {{ getStatusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="submitTime" label="提交时间" />
      <el-table-column prop="auditRemark" label="审核备注" />
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button
            size="small"
            type="success"
            @click="handleApprove(scope.row)"
            :disabled="scope.row.status !== 'PENDING'"
          >通过</el-button>
          <el-button
            size="small"
            type="danger"
            @click="openRejectModal(scope.row)"
            :disabled="scope.row.status !== 'PENDING'"
          >拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      :current-page="page"
      :page-size="pageSize"
      :total="total"
      @current-change="handlePageChange"
    />

    <el-dialog title="拒绝审核" :visible.sync="rejectModalVisible">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因">
          <el-select v-model="rejectForm.reason" placeholder="请选择拒绝原因">
            <el-option label="包含违规内容" value="包含违规内容" />
            <el-option label="广告骚扰" value="广告骚扰" />
            <el-option label="恶意攻击" value="恶意攻击" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="rejectForm.remark" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="rejectModalVisible = false">取消</el-button>
        <el-button type="danger" @click="handleReject">确认拒绝</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { AuditQueue, getAuditQueue, approve, reject, batchApprove, batchReject } from '@/api/audit'

const queueList = ref<AuditQueue[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const contentTypeFilter = ref('ALL')
const statusFilter = ref('ALL')
const selectedIds = ref<string[]>([])
const rejectModalVisible = ref(false)
const currentRejectItem = ref<AuditQueue | null>(null)

const rejectForm = reactive({
  reason: '',
  remark: ''
})

function getContentTypeLabel(type: string) {
  const map: Record<string, string> = {
    COMMENT: '评论',
    IMAGE: '图片',
    AUDIO: '音频',
    VIDEO: '视频',
    DYNAMIC: '动态'
  }
  return map[type] || type
}

function getAutoAuditLabel(result: string) {
  const map: Record<string, string> = {
    PASS: '通过',
    REJECT: '拒绝',
    MANUAL: '需人工',
    PENDING: '待审核'
  }
  return map[result] || result
}

function getAutoAuditTag(result: string) {
  const map: Record<string, string> = {
    PASS: 'success',
    REJECT: 'danger',
    MANUAL: 'warning',
    PENDING: 'info'
  }
  return map[result] || 'info'
}

function getStatusLabel(status: string) {
  const map: Record<string, string> = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝'
  }
  return map[status] || status
}

function getStatusTag(status: string) {
  const map: Record<string, string> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return map[status] || 'info'
}

function loadQueue() {
  getAuditQueue({
    page: page.value,
    pageSize: pageSize.value,
    type: contentTypeFilter.value,
    status: statusFilter.value
  }).then((res: any) => {
    if (res.data) {
      queueList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  })
}

function handlePageChange(val: number) {
  page.value = val
  loadQueue()
}

function handleSelectionChange(selection: AuditQueue[]) {
  selectedIds.value = selection.map(item => item.objectId)
}

function handleApprove(item: AuditQueue) {
  approve(item.objectId).then(() => {
    item.status = 'APPROVED'
    ElMessage.success('审核通过')
  }).catch(() => {
    ElMessage.error('审核失败')
  })
}

function openRejectModal(item: AuditQueue) {
  currentRejectItem.value = item
  rejectForm.reason = ''
  rejectForm.remark = ''
  rejectModalVisible.value = true
}

function handleReject() {
  if (!rejectForm.reason) {
    ElMessage.error('请选择拒绝原因')
    return
  }
  if (currentRejectItem.value) {
    reject(currentRejectItem.value.objectId, rejectForm.reason, rejectForm.remark).then(() => {
      currentRejectItem.value!.status = 'REJECTED'
      rejectModalVisible.value = false
      ElMessage.success('已拒绝')
    }).catch(() => {
      ElMessage.error('操作失败')
    })
  }
}

function handleBatchApprove() {
  batchApprove(selectedIds.value).then(() => {
    queueList.value = queueList.value.filter(item => !selectedIds.value.includes(item.objectId))
    selectedIds.value = []
    ElMessage.success('批量通过成功')
  }).catch(() => {
    ElMessage.error('批量操作失败')
  })
}

function handleBatchReject() {
  ElMessageBox.prompt('请输入拒绝原因', '批量拒绝', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(({ value }: { value: string }) => {
    if (!value) {
      ElMessage.error('请输入拒绝原因')
      return
    }
    batchReject(selectedIds.value, value).then(() => {
      queueList.value = queueList.value.filter(item => !selectedIds.value.includes(item.objectId))
      selectedIds.value = []
      ElMessage.success('批量拒绝成功')
    }).catch(() => {
      ElMessage.error('批量操作失败')
    })
  })
}

loadQueue()
</script>

<style scoped>
.audit-queue-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
</style>