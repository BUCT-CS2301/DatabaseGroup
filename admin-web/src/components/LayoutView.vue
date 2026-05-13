<template>
  <el-container style="height: 100vh;">
    <!-- 侧边栏 -->
    <el-aside width="200px" style="background-color: #304156;">
      <el-menu
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">监控看板</el-menu-item>
        <el-menu-item index="/user">用户管理</el-menu-item>
        <el-menu-item index="/role">角色权限</el-menu-item>
        <el-menu-item index="/audit">内容审核</el-menu-item>
        <el-menu-item index="/data/artifact">文物数据</el-menu-item>
        <el-menu-item index="/data/knowledge">知识图谱</el-menu-item>
        <el-menu-item index="/data/ugc">UGC管理</el-menu-item>
        <el-menu-item index="/backup">备份恢复</el-menu-item>
        <el-menu-item index="/log">日志管理</el-menu-item>
        <el-menu-item index="/settings">系统配置</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部导航栏：用户名 + 下拉退出 -->
      <el-header style="display: flex; justify-content: flex-end; align-items: center; background: #fff; border-bottom: 1px solid #eee; padding: 0 20px;">
        <el-dropdown trigger="click">
          <span class="user-info">
            <el-icon><User /></el-icon>
            {{ userStore.nickname }}
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

      <!-- 主内容区 -->
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { User } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
</script>

<style scoped>
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
