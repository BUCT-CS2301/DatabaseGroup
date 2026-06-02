import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const ADMIN_ROLES = ['ADMIN', 'SUPER_ADMIN']
const AUDITOR_ROLES = ['AUDITOR', 'CONTENT_AUDITOR']

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
          meta: { title: '监控看板', requiresAuth: true, roles: ADMIN_ROLES }
        },
        {
          path: 'user',
          name: 'UserList',
          component: () => import('@/views/user/UserListView.vue'),
          meta: { title: '用户管理', requiresAuth: true, roles: ADMIN_ROLES }
        },
        {
          path: 'role',
          name: 'RoleList',
          component: () => import('@/views/role/RoleListView.vue'),
          meta: { title: '角色权限', requiresAuth: true, roles: ADMIN_ROLES }
        },
        {
          path: 'audit',
          name: 'AuditQueue',
          component: () => import('@/views/audit/AuditQueueView.vue'),
          meta: { title: '内容审核', requiresAuth: true, roles: [...ADMIN_ROLES, ...AUDITOR_ROLES] }
        },
        {
          path: 'data/artifact',
          name: 'ArtifactList',
          component: () => import('@/views/data/ArtifactListView.vue'),
          meta: { title: '文物数据', requiresAuth: true, roles: ADMIN_ROLES }
        },
        {
          path: 'data/knowledge',
          name: 'KnowledgeGraph',
          component: () => import('@/views/data/KnowledgeGraphView.vue'),
          meta: { title: '知识图谱', requiresAuth: true, roles: ADMIN_ROLES }
        },
        {
          path: 'data/ugc',
          name: 'UgcList',
          component: () => import('@/views/data/UgcListView.vue'),
          meta: { title: 'UGC管理', requiresAuth: true, roles: ADMIN_ROLES }
        },
        {
          path: 'backup',
          name: 'BackupList',
          component: () => import('@/views/backup/BackupListView.vue'),
          meta: { title: '备份恢复', requiresAuth: true, roles: ADMIN_ROLES }
        },
        {
          path: 'log',
          name: 'LogList',
          component: () => import('@/views/log/LogListView.vue'),
          meta: { title: '日志管理', requiresAuth: true, roles: ADMIN_ROLES }
        },
        {
          path: 'settings',
          name: 'Settings',
          component: () => import('@/views/settings/SettingsView.vue'),
          meta: { title: '系统配置', requiresAuth: true, roles: ADMIN_ROLES }
        }
      ]
    }
  ]
})

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const requiresAuth = to.meta.requiresAuth

  console.log('Router guard: navigating from', from.path, 'to', to.path)
  console.log('Router guard: user roles', userStore.roles)
  console.log('Router guard: requiresAuth', requiresAuth)
  console.log('Router guard: allowedRoles', to.meta.roles)

  if (to.path === '/' && !userStore.token) {
    console.log('Router guard: redirect to /login (no token)')
    next('/login')
    return
  }

  if (requiresAuth && !userStore.token) {
    console.log('Router guard: redirect to /login (auth required)')
    next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
    return
  }

  if (userStore.token && userStore.roles.length === 0) {
    console.log('Router guard: fetching user info...')
    try {
      await userStore.fetchCurrentUser()
      console.log('Router guard: user info fetched, roles:', userStore.roles)
    } catch (error) {
      console.log('Router guard: fetch user failed, redirect to login')
      await userStore.logout()
      next('/login')
      return
    }
  }

  if (to.path === '/login' && userStore.token) {
    console.log('Router guard: already logged in, redirect to', userStore.defaultRoute)
    next(userStore.defaultRoute)
    return
  }

  const allowedRoles = to.meta.roles as string[] | undefined
  if (requiresAuth && !userStore.hasAnyRole(allowedRoles)) {
    console.log('Router guard: no permission, redirect to', userStore.defaultRoute)
    next(userStore.defaultRoute)
    return
  }

  console.log('Router guard: proceed to', to.path)
  next()
})

export default router
