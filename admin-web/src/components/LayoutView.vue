<template>
  <el-container style="height: 100vh;">
    <!-- 侧边栏 -->
    <el-aside width="220px" style="background-color: #304156;">
      <!-- Logo 区域 -->
      <div class="logo-container">
        <el-icon class="logo-icon"><Box /></el-icon>
        <span class="logo-text">后台管理系统</span>
      </div>

      <!-- 导航菜单 -->
      <el-menu
        router
        :default-active="$route.path"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        :unique-opened="true"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>监控看板</span>
        </el-menu-item>

        <el-sub-menu index="user-management">
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </template>
          <el-menu-item index="/user">用户列表</el-menu-item>
          <el-menu-item index="/role">角色权限</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/audit">
          <el-icon><DocumentChecked /></el-icon>
          <span>内容审核</span>
        </el-menu-item>

        <el-sub-menu index="data-management">
          <template #title>
            <el-icon><Collection /></el-icon>
            <span>数据管理</span>
          </template>
          <el-menu-item index="/data/artifact">文物数据</el-menu-item>
          <el-menu-item index="/data/knowledge">知识图谱</el-menu-item>
          <el-menu-item index="/data/ugc">UGC管理</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/backup">
          <el-icon><Coin /></el-icon>
          <span>备份恢复</span>
        </el-menu-item>

        <el-menu-item index="/log">
          <el-icon><Document /></el-icon>
          <span>日志管理</span>
        </el-menu-item>

        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <span>系统配置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部导航栏 -->
      <el-header style="display: flex; justify-content: flex-end; align-items: center; background: #fff; border-bottom: 1px solid #e6e6e6; padding: 0 20px;">
        <el-dropdown trigger="click" @command="handleCommand">
          <span class="user-info">
            <el-avatar :size="32" style="margin-right: 8px; background: #409EFF;">
              {{ userStore.nickname?.charAt(0) || 'U' }}
            </el-avatar>
            <span class="nickname">{{ userStore.nickname }}</span>
            <el-icon class="arrow-icon"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>个人中心
              </el-dropdown-item>
              <el-dropdown-item command="settings">
                <el-icon><Setting /></el-icon>系统设置
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <!-- 主内容区 -->
      <el-main style="background-color: #f0f2f5; padding: 20px;">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useUserStore } from '@/store/user'
import { useRouter } from 'vue-router'
import {
  Box,
  Odometer,
  User,
  DocumentChecked,
  Collection,
  Coin,
  Document,
  Setting,
  ArrowDown,
  SwitchButton
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()

// 处理下拉菜单命令
const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/settings')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      userStore.logout()
      break
  }
}
</script>

<style scoped>
/* Logo 样式 */
.logo-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
  background-color: #2b3a4a;
  border-bottom: 1px solid #3d4a5a;
}

.logo-icon {
  font-size: 28px;
  color: #409EFF;
  margin-right: 8px;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
}

/* 用户信息样式 */
.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.nickname {
  font-size: 14px;
  color: #333;
  margin-right: 6px;
}

.arrow-icon {
  font-size: 12px;
  color: #909399;
}

/* 菜单激活样式 */
:deep(.el-menu-item.is-active) {
  background-color: #263445 !important;
}

:deep(.el-menu-item:hover) {
  background-color: #263445 !important;
}

:deep(.el-sub-menu__title:hover) {
  background-color: #263445 !important;
}

/* 子菜单样式 */
:deep(.el-sub-menu .el-menu-item) {
  min-width: 0;
  padding-left: 50px !important;
}
</style>
