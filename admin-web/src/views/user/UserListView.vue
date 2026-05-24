<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2>用户管理</h2>
        <p>维护后台账号、联系方式和启用状态</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增用户</el-button>
    </div>

    <el-card class="toolbar" shadow="never">
      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            clearable
            placeholder="用户名 / 昵称 / 邮箱"
            :prefix-icon="Search"
            @keyup.enter="loadUsers"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" style="width: 140px">
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="users" row-key="objectId" border>
        <el-table-column prop="username" label="用户名" min-width="130" />
        <el-table-column prop="nickname" label="昵称" min-width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column label="角色" min-width="120">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role" size="small" effect="plain">
              {{ role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最近登录" min-width="170" />
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="openEditDialog(row)">编辑</el-button>
            <el-button
              :type="row.status === 'ENABLED' ? 'warning' : 'success'"
              link
              :icon="SwitchButton"
              @click="toggleStatus(row)"
            >
              {{ row.status === 'ENABLED' ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link :icon="Delete" @click="removeUser(row)">删除</el-button>
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
          @size-change="loadUsers"
          @current-change="loadUsers"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingUser ? '编辑用户' : '新增用户'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="86px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="Boolean(editingUser)" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="!editingUser" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入初始密码" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Plus, Refresh, Search, SwitchButton } from '@element-plus/icons-vue'
import {
  createUser,
  deleteUser,
  getUserList,
  updateUser,
  updateUserStatus,
  type SaveUserRequest,
  type UserRecord
} from '@/api/user'

const loading = ref(false)
const saving = ref(false)
const users = ref<UserRecord[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const editingUser = ref<UserRecord | null>(null)
const formRef = ref<FormInstance>()

const query = reactive({
  page: 1,
  pageSize: 10,
  keyword: '',
  status: ''
})

const form = reactive<SaveUserRequest>({
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

function formatStatus(status: string) {
  return status === 'ENABLED' ? '启用' : '禁用'
}

async function loadUsers() {
  loading.value = true
  try {
    const res = await getUserList(query)
    users.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  loadUsers()
}

function resetSearch() {
  query.keyword = ''
  query.status = ''
  handleSearch()
}

function resetForm() {
  form.username = ''
  form.password = ''
  form.nickname = ''
  form.email = ''
  form.phone = ''
  formRef.value?.clearValidate()
}

function openCreateDialog() {
  editingUser.value = null
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row: UserRecord) {
  editingUser.value = row
  form.username = row.username
  form.password = ''
  form.nickname = row.nickname
  form.email = row.email
  form.phone = row.phone
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

async function saveUser() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (editingUser.value) {
      await updateUser(editingUser.value.objectId, form)
      ElMessage.success('用户已更新')
    } else {
      await createUser(form)
      ElMessage.success('用户已创建')
    }
    dialogVisible.value = false
    loadUsers()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: UserRecord) {
  const nextStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  await updateUserStatus(row.objectId, nextStatus)
  ElMessage.success(`用户已${nextStatus === 'ENABLED' ? '启用' : '禁用'}`)
  loadUsers()
}

async function removeUser(row: UserRecord) {
  await ElMessageBox.confirm(`确定删除用户“${row.username}”吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  })
  await deleteUser(row.objectId)
  ElMessage.success('用户已删除')
  loadUsers()
}

onMounted(loadUsers)
</script>

<style scoped>
.page {
  min-width: 960px;
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

.toolbar {
  margin-bottom: 14px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  row-gap: 8px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
