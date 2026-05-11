import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref('')
  const nickname = ref('')
  const permissions = ref<string[]>([])

  async function login(username: string, password: string) {
    // TODO: 调用登录接口
  }

  function logout() {
    token.value = ''
    nickname.value = ''
    permissions.value = []
    localStorage.removeItem('token')
  }

  return { token, nickname, permissions, login, logout }
})
