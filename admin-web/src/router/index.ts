// import { createRouter, createWebHistory } from 'vue-router'
// import { useUserStore } from '@/store/user'
//
// const router = createRouter({
//   history: createWebHistory(),
//   routes: [
//     {
//       path: '/login',
//       name: 'Login',
//       component: () => import('@/views/login/LoginView.vue'),
//       meta: { title: '登录' }
//     },
//     {
//       path: '/',
//       redirect: '/dashboard',
//       component: () => import('@/components/LayoutView.vue'),
//       children: [
//         {
//           path: 'dashboard',
//           name: 'Dashboard',
//           component: () => import('@/views/dashboard/DashboardView.vue'),
//           meta: { title: '监控看板' }
//         },
//         {
//           path: 'user',
//           name: 'UserList',
//           component: () => import('@/views/user/UserListView.vue'),
//           meta: { title: '用户管理' }
//         },
//         {
//           path: 'role',
//           name: 'RoleList',
//           component: () => import('@/views/role/RoleListView.vue'),
//           meta: { title: '角色权限' }
//         },
//         {
//           path: 'audit',
//           name: 'AuditQueue',
//           component: () => import('@/views/audit/AuditQueueView.vue'),
//           meta: { title: '内容审核' }
//         },
//         {
//           path: 'data/artifact',
//           name: 'ArtifactList',
//           component: () => import('@/views/data/ArtifactListView.vue'),
//           meta: { title: '文物数据' }
//         },
//         {
//           path: 'data/knowledge',
//           name: 'KnowledgeGraph',
//           component: () => import('@/views/data/KnowledgeGraphView.vue'),
//           meta: { title: '知识图谱' }
//         },
//         {
//           path: 'data/ugc',
//           name: 'UgcList',
//           component: () => import('@/views/data/UgcListView.vue'),
//           meta: { title: 'UGC管理' }
//         },
//         {
//           path: 'backup',
//           name: 'BackupList',
//           component: () => import('@/views/backup/BackupListView.vue'),
//           meta: { title: '备份恢复' }
//         },
//         {
//           path: 'log',
//           name: 'LogList',
//           component: () => import('@/views/log/LogListView.vue'),
//           meta: { title: '日志管理' }
//         },
//         {
//           path: 'settings',
//           name: 'Settings',
//           component: () => import('@/views/settings/SettingsView.vue'),
//           meta: { title: '系统配置' }
//         }
//       ]
//     }
//   ]
// })
//
// //2026.5.13
// router.beforeEach((to, from, next) => {
//   const userStore = useUserStore()
//   const requiresAuth = to.meta.requiresAuth
//
//   // 需要登录的页面
//   if (requiresAuth) {
//     if (userStore.token) {
//       next() // 已登录，放行
//     } else {
//       next(`/login?redirect=${to.fullPath}`) // 未登录，跳登录
//     }
//   } else {
//     // 不需要登录的页面（登录页）
//     if (to.path === '/login' && userStore.token) {
//       next('/dashboard') // 已登录，不让进登录页，直接去主页
//     } else {
//       next()
//     }
//   }
// })
//
//
// export default router



import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/LoginView.vue'),
      meta: { title: '登录', requiresAuth: false }
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
          meta: { title: '监控看板', requiresAuth: true }
        },
        {
          path: 'user',
          name: 'UserList',
          component: () => import('@/views/user/UserListView.vue'),
          meta: { title: '用户管理', requiresAuth: true }
        },
        {
          path: 'role',
          name: 'RoleList',
          component: () => import('@/views/role/RoleListView.vue'),
          meta: { title: '角色权限', requiresAuth: true }
        },
        {
          path: 'audit',
          name: 'AuditQueue',
          component: () => import('@/views/audit/AuditQueueView.vue'),
          meta: { title: '内容审核', requiresAuth: true }
        },
        {
          path: 'data/artifact',
          name: 'ArtifactList',
          component: () => import('@/views/data/ArtifactListView.vue'),
          meta: { title: '文物数据', requiresAuth: true }
        },
        {
          path: 'data/knowledge',
          name: 'KnowledgeGraph',
          component: () => import('@/views/data/KnowledgeGraphView.vue'),
          meta: { title: '知识图谱', requiresAuth: true }
        },
        {
          path: 'data/ugc',
          name: 'UgcList',
          component: () => import('@/views/data/UgcListView.vue'),
          meta: { title: 'UGC管理', requiresAuth: true }
        },
        {
          path: 'backup',
          name: 'BackupList',
          component: () => import('@/views/backup/BackupListView.vue'),
          meta: { title: '备份恢复', requiresAuth: true }
        },
        {
          path: 'log',
          name: 'LogList',
          component: () => import('@/views/log/LogListView.vue'),
          meta: { title: '日志管理', requiresAuth: true }
        },
        {
          path: 'settings',
          name: 'Settings',
          component: () => import('@/views/settings/SettingsView.vue'),
          meta: { title: '系统配置', requiresAuth: true }
        }
      ]
    }
  ]
})

//路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const requiresAuth = to.meta.requiresAuth

  console.log('跳转页面:', to.path, '需要登录:', requiresAuth, '当前token:', userStore.token)

  if (requiresAuth && !userStore.token) {
    // 未登录 → 强制跳登录页
    next('/login')
  } else if (to.path === '/login' && userStore.token) {
    // 已登录访问登录页 → 跳主页
    next('/dashboard')
  } else {
    next()
  }
})

export default router
