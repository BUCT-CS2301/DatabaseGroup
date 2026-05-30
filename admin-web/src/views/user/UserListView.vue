<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2>用户管理</h2>
        <p>维护账号资料、角色权限、登录状态和密码重置</p>
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
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" style="width: 140px">
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="query.role" clearable placeholder="全部角色" style="width: 170px">
            <el-option
              v-for="role in roleOptions"
              :key="role.roleCode"
              :label="role.roleName"
              :value="role.roleCode"
            />
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
        <el-table-column label="角色" min-width="150">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role" size="small" effect="plain">
              {{ formatRole(role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最近登录" min-width="170" />
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="360" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="openEditDialog(row)">编辑</el-button>
            <el-button type="primary" link :icon="UserFilled" @click="openRoleDialog(row)">改角色</el-button>
            <el-button type="warning" link :icon="Key" @click="openPasswordDialog(row)">重置密码</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editingUser ? '编辑用户' : '新增用户'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="Boolean(editingUser)" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="!editingUser" label="初始密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入初始密码" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" class="full-width">
            <el-option
              v-for="role in roleOptions"
              :key="role.roleCode"
              :label="role.roleName"
              :value="role.roleCode"
            />
          </el-select>
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

    <el-dialog v-model="roleDialogVisible" title="修改用户角色" width="460px">
      <el-form label-width="90px">
        <el-form-item label="用户">
          <el-input :model-value="selectedUser?.username" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="roleForm.role" class="full-width">
            <el-option
              v-for="role in roleOptions"
              :key="role.roleCode"
              :label="role.roleName"
              :value="role.roleCode"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveRole">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="passwordDialogVisible" title="重置用户密码" width="460px">
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="90px">
        <el-form-item label="用户">
          <el-input :model-value="selectedUser?.username" disabled />
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input v-model="passwordForm.password" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="savePassword">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Key, Plus, Refresh, Search, SwitchButton, UserFilled } from '@element-plus/icons-vue'
import { getAllRoles, type RoleOption } from '@/api/role'
import {
  createUser,
  deleteUser,
  getUserList,
  resetUserPassword,
  updateUser,
  updateUserRole,
  updateUserStatus,
  type SaveUserRequest,
  type UserRecord
} from '@/api/user'

const fallbackRoles: RoleOption[] = [
  { objectId: 'role-admin', roleName: '超级管理员', roleCode: 'ADMIN' },
  { objectId: 'role-auditor', roleName: '内容审核员', roleCode: 'AUDITOR' },
  { objectId: 'role-user', roleName: '普通用户', roleCode: 'USER' }
]

const loading = ref(false)
const saving = ref(false)
const users = ref<UserRecord[]>([])
const roleOptions = ref<RoleOption[]>(fallbackRoles)
const total = ref(0)
const dialogVisible = ref(false)
const roleDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const editingUser = ref<UserRecord | null>(null)
const selectedUser = ref<UserRecord | null>(null)
const formRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

const query = reactive({
  page: 1,
  pageSize: 10,
  keyword: '',
  status: '',
  role: ''
})

const form = reactive<SaveUserRequest>({
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  role: 'USER'
})

const roleForm = reactive({
  role: 'USER'
})

const passwordForm = reactive({
  password: '',
  confirmPassword: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

const passwordRules: FormRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== passwordForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

function formatStatus(status: string) {
  return status === 'ENABLED' ? '启用' : '禁用'
}

function formatRole(roleCode: string) {
  return roleOptions.value.find(role => role.roleCode === roleCode)?.roleName || roleCode
}

async function loadRoles() {
  try {
    const res = await getAllRoles()
    roleOptions.value = res.data.length ? res.data : fallbackRoles
  } catch (error) {
    roleOptions.value = fallbackRoles
  }
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
  query.role = ''
  handleSearch()
}

function resetForm() {
  form.username = ''
  form.password = ''
  form.nickname = ''
  form.email = ''
  form.phone = ''
  form.role = roleOptions.value[0]?.roleCode || 'USER'
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
  form.role = row.roles?.[0] || 'USER'
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

async function saveUser() {
  await formRef.value?.validate()
  saving.value = true
  try {
    const payload = { ...form, roles: form.role ? [form.role] : [] }
    if (editingUser.value) {
      await updateUser(editingUser.value.objectId, payload)
      ElMessage.success('用户已更新')
    } else {
      await createUser(payload)
      ElMessage.success('用户已创建')
    }
    dialogVisible.value = false
    loadUsers()
  } finally {
    saving.value = false
  }
}

function openRoleDialog(row: UserRecord) {
  selectedUser.value = row
  roleForm.role = row.roles?.[0] || 'USER'
  roleDialogVisible.value = true
}

async function saveRole() {
  if (!selectedUser.value) return
  saving.value = true
  try {
    await updateUserRole(selectedUser.value.objectId, roleForm.role)
    ElMessage.success('用户角色已更新')
    roleDialogVisible.value = false
    loadUsers()
  } finally {
    saving.value = false
  }
}

function openPasswordDialog(row: UserRecord) {
  selectedUser.value = row
  passwordForm.password = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
  passwordDialogVisible.value = true
}

async function savePassword() {
  await passwordFormRef.value?.validate()
  if (!selectedUser.value) return
  saving.value = true
  try {
    await resetUserPassword(selectedUser.value.objectId, passwordForm.password)
    ElMessage.success('密码已重置')
    passwordDialogVisible.value = false
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

onMounted(async () => {
  await loadRoles()
  loadUsers()
})
</script>

<style scoped>
.page {
  min-width: 1100px;
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

.full-width {
  width: 100%;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
