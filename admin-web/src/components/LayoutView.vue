<template>
  <el-container class="layout">
    <el-aside width="200px" class="sidebar">
      <el-menu
        :default-active="route.path"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item 
          v-for="item in visibleMenus" 
          :key="item.path" 
          :index="item.path"
        >
          {{ item.title }}
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <el-tag v-for="role in userStore.roles" :key="role" size="small" effect="plain">
          {{ formatRole(role) }}
        </el-tag>
        <el-dropdown trigger="click">
          <span class="user-info">
            <el-icon><User /></el-icon>
            {{ userStore.nickname || userStore.username }}
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="userStore.logout()">
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { User } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const route = useRoute()
const userStore = useUserStore()

const menus = [
  { path: '/dashboard', title: '监控看板', roles: ['ADMIN', 'SUPER_ADMIN'] },
  { path: '/user', title: '用户管理', roles: ['ADMIN', 'SUPER_ADMIN'] },
  { path: '/role', title: '角色权限', roles: ['ADMIN', 'SUPER_ADMIN'] },
  { path: '/audit', title: '内容审核', roles: ['ADMIN', 'SUPER_ADMIN', 'AUDITOR', 'CONTENT_AUDITOR'] },
  { path: '/data/artifact', title: '文物数据', roles: ['ADMIN', 'SUPER_ADMIN'] },
  { path: '/data/knowledge', title: '知识图谱', roles: ['ADMIN', 'SUPER_ADMIN'] },
  { path: '/data/ugc', title: 'UGC管理', roles: ['ADMIN', 'SUPER_ADMIN'] },
  { path: '/backup', title: '备份恢复', roles: ['ADMIN', 'SUPER_ADMIN'] },
  { path: '/log', title: '日志管理', roles: ['ADMIN', 'SUPER_ADMIN'] },
  { path: '/settings', title: '系统配置', roles: ['ADMIN', 'SUPER_ADMIN'] }
]

const visibleMenus = computed(() => menus.filter(item => userStore.hasAnyRole(item.roles)))

function formatRole(role: string) {
  const roleMap: Record<string, string> = {
    ADMIN: '超级管理员',
    SUPER_ADMIN: '超级管理员',
    AUDITOR: '内容审核员',
    CONTENT_AUDITOR: '内容审核员'
  }
  return roleMap[role] || role
}
</script>

<style scoped>
.layout {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
}

.sidebar .el-menu {
  border-right: 0;
}

.header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
  background: #fff;
  border-bottom: 1px solid #eee;
  padding: 0 20px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-size: 15px;
}

.user-info .el-icon {
  margin-right: 6px;
  font-size: 16px;
}
</style>
