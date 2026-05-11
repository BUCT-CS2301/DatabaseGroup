<template>
  <div class="layout">
    <el-container>
      <el-aside width="200px">
        <el-menu router>
          <el-menu-item index="/dashboard">监控看板</el-menu-item>
          <el-menu-item index="/user">用户管理</el-menu-item>
          <el-menu-item index="/role">角色权限</el-menu-item>
          <el-menu-item index="/audit">内容审核</el-menu-item>
          <el-sub-menu index="data">
            <template #title>数据管理</template>
            <el-menu-item index="/data/artifact">文物数据</el-menu-item>
            <el-menu-item index="/data/knowledge">知识图谱</el-menu-item>
            <el-menu-item index="/data/ugc">UGC管理</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/backup">备份恢复</el-menu-item>
          <el-menu-item index="/log">日志管理</el-menu-item>
          <el-menu-item index="/settings">系统配置</el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header>后台管理</el-header>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.layout { height: 100%; }
</style>
