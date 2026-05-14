<template>
  <div class="user-list-container">
    <div class="header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="openCreateModal">新增用户</el-button>
    </div>

    <div class="filter-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索用户名或昵称"
        class="search-input"
        @keyup.enter="loadUsers"
      />
      <el-select v-model="statusFilter" placeholder="状态筛选">
        <el-option label="全部" value="" />
        <el-option label="启用" value="ENABLED" />
        <el-option label="禁用" value="DISABLED" />
      </el-select>
      <el-button type="primary" @click="loadUsers">查询</el-button>
    </div>

    <el-table :data="users" border>
      <el-table-column prop="objectId" label="ID" width="120" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="userType" label="用户类型">
        <template #default="scope">
          <el-tag :type="getUserTypeTag(scope.row.userType)">
            {{ getUserTypeLabel(scope.row.userType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <el-switch
            :value="scope.row.status === 'ENABLED'"
            @change="toggleStatus(scope.row)"
            :disabled="scope.row.userType === 'ADMIN'"
          />
        </template>
      </el-table-column>
      <el-table-column prop="roles" label="角色" />
      <el-table-column prop="lastLoginTime" label="最后登录" />
      <el-table-column prop="createTime" label="创建时间" />
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button size="small" @click="openEditModal(scope.row)">编辑</el-button>
          <el-button size="small" @click="viewLogs(scope.row)">查看日志</el-button>
          <el-button
            size="small"
            type="danger"
            @click="handleDelete(scope.row)"
            :disabled="scope.row.userType === 'ADMIN'"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      :current-page="page"
      :page-size="pageSize"
      :total="total"
      @current-change="handlePageChange"
    />

    <el-dialog :title="isEdit ? '编辑用户' : '新增用户'" :visible.sync="modalVisible">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="用户名" v-if="!isEdit">
          <el-input v-model="formData.username" />
        </el-form-item>
        <el-form-item label="密码" v-if="!isEdit">
          <el-input type="password" v-model="formData.password" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="formData.nickname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="formData.email" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="formData.phone" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="modalVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, getUserList, createUser, updateUser, deleteUser, updateUserStatus } from '@/api/user'

const users = ref<User[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const statusFilter = ref('')
const modalVisible = ref(false)
const isEdit = ref(false)

const formData = reactive({
  objectId: '',
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: ''
})

function getUserTypeLabel(type: string) {
  const map: Record<string, string> = {
    ADMIN: '管理员',
    KNOWLEDGE_SERVICE: '知识服务用户',
    MOBILE: '掌上博物馆用户'
  }
  return map[type] || type
}

function getUserTypeTag(type: string) {
  const map: Record<string, string> = {
    ADMIN: 'danger',
    KNOWLEDGE_SERVICE: 'primary',
    MOBILE: 'success'
  }
  return map[type] || 'info'
}

function loadUsers() {
  getUserList({
    page: page.value,
    pageSize: pageSize.value,
    keyword: keyword.value,
    status: statusFilter.value
  }).then((res: any) => {
    if (res.data) {
      users.value = res.data.records || []
      total.value = res.data.total || 0
    }
  })
}

function handlePageChange(val: number) {
  page.value = val
  loadUsers()
}

function openCreateModal() {
  isEdit.value = false
  formData.objectId = ''
  formData.username = ''
  formData.password = ''
  formData.nickname = ''
  formData.email = ''
  formData.phone = ''
  modalVisible.value = true
}

function openEditModal(user: User) {
  isEdit.value = true
  formData.objectId = user.objectId
  formData.username = user.username
  formData.nickname = user.nickname
  formData.email = user.email
  formData.phone = user.phone
  modalVisible.value = true
}

function saveUser() {
  if (!isEdit.value) {
    if (!formData.username || !formData.password) {
      ElMessage.error('请填写用户名和密码')
      return
    }
    createUser({
      username: formData.username,
      password: formData.password,
      nickname: formData.nickname,
      email: formData.email,
      phone: formData.phone
    }).then(() => {
      ElMessage.success('创建成功')
      modalVisible.value = false
      loadUsers()
    }).catch(() => {
      ElMessage.error('创建失败')
    })
  } else {
    updateUser(formData.objectId, {
      nickname: formData.nickname,
      email: formData.email,
      phone: formData.phone
    }).then(() => {
      ElMessage.success('更新成功')
      modalVisible.value = false
      loadUsers()
    }).catch(() => {
      ElMessage.error('更新失败')
    })
  }
}

function toggleStatus(user: User) {
  const newStatus = user.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  updateUserStatus(user.objectId, newStatus).then(() => {
    user.status = newStatus
    ElMessage.success('状态更新成功')
  }).catch(() => {
    ElMessage.error('状态更新失败')
  })
}

function handleDelete(user: User) {
  ElMessageBox.confirm('确定删除该用户?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    deleteUser(user.objectId).then(() => {
      ElMessage.success('删除成功')
      loadUsers()
    }).catch(() => {
      ElMessage.error('删除失败')
    })
  })
}

function viewLogs(user: User) {
  ElMessage.info(`查看用户 ${user.username} 的行为日志`)
}

loadUsers()
</script>

<style scoped>
.user-list-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.search-input {
  width: 200px;
}
</style>