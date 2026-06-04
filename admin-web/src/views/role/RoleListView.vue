<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2>角色权限</h2>
        <p>查看角色、角色人数和权限范围，自定义新角色</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增角色</el-button>
    </div>

    <el-card class="toolbar" shadow="never">
      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            clearable
            placeholder="角色名称 / 编码"
            :prefix-icon="Search"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="roles" row-key="objectId" border>
        <el-table-column prop="roleName" label="角色名称" min-width="150" />
        <el-table-column prop="roleCode" label="角色编码" min-width="140" />
        <el-table-column label="类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isSystem ? 'success' : 'info'">
              {{ row.isSystem ? '初始角色' : '自定义' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="用户人数" width="110" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openUsersDrawer(row)">
              {{ row.userCount || 0 }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="180" show-overflow-tooltip />
        <el-table-column label="拥有权限" min-width="360">
          <template #default="{ row }">
            <div class="permission-tags">
              <el-tag
                v-for="permission in row.permissions"
                :key="permission"
                size="small"
                effect="plain"
              >
                {{ formatPermission(permission) }}
              </el-tag>
              <span v-if="!row.permissions?.length" class="empty-text">未配置</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="openUsersDrawer(row)">查看用户</el-button>
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
          @size-change="loadRoles"
          @current-change="loadRoles"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增自定义角色" width="620px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="例如：数据维护员" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="例如：DATA_OPERATOR" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="角色用途说明" />
        </el-form-item>
        <el-form-item label="权限" prop="permissions">
          <el-checkbox-group v-model="form.permissions" class="permission-grid">
            <el-checkbox
              v-for="permission in permissions"
              :key="permission.code"
              :label="permission.code"
            >
              {{ permission.name }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveRole">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="usersDrawerVisible" :title="`${selectedRole?.roleName || ''} 下的用户`" size="620px">
      <el-table v-loading="usersLoading" :data="roleUsers" border>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="170" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
              {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          v-model:current-page="userQuery.page"
          v-model:page-size="userQuery.pageSize"
          layout="total, prev, pager, next"
          :total="userTotal"
          @current-change="loadRoleUsers"
        />
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import {
  createRole,
  getAllPermissions,
  getRoleList,
  getRoleUsers,
  type PermissionOption,
  type RoleOption,
  type RoleUserRecord,
  type SaveRoleRequest
} from '@/api/role'

const loading = ref(false)
const saving = ref(false)
const usersLoading = ref(false)
const roles = ref<RoleOption[]>([])
const permissions = ref<PermissionOption[]>([])
const roleUsers = ref<RoleUserRecord[]>([])
const selectedRole = ref<RoleOption | null>(null)
const total = ref(0)
const userTotal = ref(0)
const dialogVisible = ref(false)
const usersDrawerVisible = ref(false)
const formRef = ref<FormInstance>()

const query = reactive({
  page: 1,
  pageSize: 10,
  keyword: ''
})

const userQuery = reactive({
  page: 1,
  pageSize: 10
})

const form = reactive<SaveRoleRequest>({
  roleName: '',
  roleCode: '',
  description: '',
  permissions: []
})

const rules: FormRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  permissions: [{ required: true, type: 'array', min: 1, message: '至少选择一个权限', trigger: 'change' }]
}

function formatPermission(code: string) {
  return permissions.value.find(permission => permission.code === code)?.name || code
}

async function loadPermissions() {
  const res = await getAllPermissions()
  permissions.value = res.data || []
}

async function loadRoles() {
  loading.value = true
  try {
    const res = await getRoleList(query)
    roles.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  loadRoles()
}

function resetSearch() {
  query.keyword = ''
  handleSearch()
}

function openCreateDialog() {
  form.roleName = ''
  form.roleCode = ''
  form.description = ''
  form.permissions = []
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

async function saveRole() {
  await formRef.value?.validate()
  saving.value = true
  try {
    await createRole({
      ...form,
      roleCode: form.roleCode.trim().toUpperCase()
    })
    ElMessage.success('角色已创建')
    dialogVisible.value = false
    loadRoles()
  } finally {
    saving.value = false
  }
}

async function openUsersDrawer(role: RoleOption) {
  selectedRole.value = role
  userQuery.page = 1
  usersDrawerVisible.value = true
  await loadRoleUsers()
}

async function loadRoleUsers() {
  if (!selectedRole.value) return
  usersLoading.value = true
  try {
    const res = await getRoleUsers(selectedRole.value.objectId, userQuery)
    roleUsers.value = res.data.records || []
    userTotal.value = res.data.total || 0
  } finally {
    usersLoading.value = false
  }
}

onMounted(async () => {
  await loadPermissions()
  loadRoles()
})

// 组件卸载时清理
onUnmounted(() => {
  dialogVisible.value = false
  usersDrawerVisible.value = false
})
</script>

<style scoped>
.page {
  min-width: 1080px;
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

.permission-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.permission-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 14px;
}

.empty-text {
  color: #9aa4b2;
  font-size: 13px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
