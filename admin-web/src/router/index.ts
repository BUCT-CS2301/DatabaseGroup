import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/LoginView.vue'),
      meta: { title: '登录' }
    },
    {
      path: '/',
      redirect: '/dashboard',
      component: () => import('@/components/LayoutView.vue'),
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/DashboardView.vue'),
          meta: { title: '监控看板' }
        },
        {
          path: 'user',
          name: 'UserList',
          component: () => import('@/views/user/UserListView.vue'),
          meta: { title: '用户管理' }
        },
        {
          path: 'role',
          name: 'RoleList',
          component: () => import('@/views/role/RoleListView.vue'),
          meta: { title: '角色权限' }
        },
        {
          path: 'audit',
          name: 'AuditQueue',
          component: () => import('@/views/audit/AuditQueueView.vue'),
          meta: { title: '内容审核' }
        },
        {
          path: 'data/artifact',
          name: 'ArtifactList',
          component: () => import('@/views/data/ArtifactListView.vue'),
          meta: { title: '文物数据' }
        },
        {
          path: 'data/knowledge',
          name: 'KnowledgeGraph',
          component: () => import('@/views/data/KnowledgeGraphView.vue'),
          meta: { title: '知识图谱' }
        },
        {
          path: 'data/ugc',
          name: 'UgcList',
          component: () => import('@/views/data/UgcListView.vue'),
          meta: { title: 'UGC管理' }
        },
        {
          path: 'backup',
          name: 'BackupList',
          component: () => import('@/views/backup/BackupListView.vue'),
          meta: { title: '备份恢复' }
        },
        {
          path: 'log',
          name: 'LogList',
          component: () => import('@/views/log/LogListView.vue'),
          meta: { title: '日志管理' }
        },
        {
          path: 'settings',
          name: 'Settings',
          component: () => import('@/views/settings/SettingsView.vue'),
          meta: { title: '系统配置' }
        }
      ]
    }
  ]
})

export default router
