import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import router from '@/router'
import { getCurrentUserApi, loginApi, logoutApi } from '@/api/auth'

const ADMIN_ROLES = ['ADMIN', 'SUPER_ADMIN']
const AUDITOR_ROLES = ['AUDITOR', 'CONTENT_AUDITOR']

function normalizeRoles(roles: string[]) {
  return roles.map(role => role.toUpperCase())
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const refreshToken = ref<string>(localStorage.getItem('refreshToken') || '')
  const objectId = ref<string>(localStorage.getItem('objectId') || '')
  const username = ref<string>(localStorage.getItem('username') || '')
  const nickname = ref<string>(localStorage.getItem('nickname') || '')
  const roles = ref<string[]>(normalizeRoles(JSON.parse(localStorage.getItem('roles') || '[]')))
  const permissions = ref<string[]>(JSON.parse(localStorage.getItem('permissions') || '[]'))

  const isAdmin = computed(() => roles.value.some(role => ADMIN_ROLES.includes(role)))
  const isAuditor = computed(() => roles.value.some(role => AUDITOR_ROLES.includes(role)))
  const defaultRoute = computed(() => {
    if (isAuditor.value && !isAdmin.value) return '/audit'
    return '/dashboard'
  })

  function persistProfile() {
    localStorage.setItem('token', token.value)
    localStorage.setItem('refreshToken', refreshToken.value)
    localStorage.setItem('objectId', objectId.value)
    localStorage.setItem('username', username.value)
    localStorage.setItem('nickname', nickname.value)
    localStorage.setItem('roles', JSON.stringify(roles.value))
    localStorage.setItem('permissions', JSON.stringify(permissions.value))
  }

  function clearProfile() {
    token.value = ''
    refreshToken.value = ''
    objectId.value = ''
    username.value = ''
    nickname.value = ''
    roles.value = []
    permissions.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('objectId')
    localStorage.removeItem('username')
    localStorage.removeItem('nickname')
    localStorage.removeItem('roles')
    localStorage.removeItem('permissions')
  }

  async function fetchCurrentUser() {
    const res = await getCurrentUserApi()
    objectId.value = res.data.objectId
    username.value = res.data.username
    nickname.value = res.data.nickname || res.data.username
    roles.value = normalizeRoles(res.data.roles || [])
    permissions.value = res.data.permissions || []
    persistProfile()
    return res.data
  }

  async function login(loginName: string, password: string) {
    const res = await loginApi({ username: loginName, password })
    token.value = res.data.accessToken
    refreshToken.value = res.data.refreshToken
    persistProfile()
    await fetchCurrentUser()
    return res
  }

  async function logout() {
    if (token.value) {
      try {
        await logoutApi()
      } catch (error) {
        console.warn('logout request failed', error)
      }
    }
    clearProfile()
    router.push('/login')
  }

  function hasAnyRole(allowedRoles?: string[]) {
    if (!allowedRoles || allowedRoles.length === 0) return true
    const normalizedAllowedRoles = normalizeRoles(allowedRoles)
    return roles.value.some(role => normalizedAllowedRoles.includes(role))
  }

  return {
    token,
    refreshToken,
    objectId,
    username,
    nickname,
    roles,
    permissions,
    isAdmin,
    isAuditor,
    defaultRoute,
    login,
    logout,
    fetchCurrentUser,
    hasAnyRole
  }
})
