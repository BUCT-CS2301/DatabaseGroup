// import { defineStore } from 'pinia'
// import { ref } from 'vue'
// import request from '@/utils/request'
//
// export const useUserStore = defineStore('user', () => {
//   const token = ref<string>(localStorage.getItem('token') || '')
//   const nickname = ref('')
//   const permissions = ref<string[]>([])
//
//   // 登录：调用真实接口
//   async function login(username: string, password: string) {
//     const res = await request.post('/v1/auth/login', {
//       username,
//       password
//     })
//
//     token.value = res.data.accessToken
//     localStorage.setItem('token', token.value)
//     return res
//   }
//
//   // 登出
//   function logout() {
//     token.value = ''
//     nickname.value = ''
//     permissions.value = []
//     localStorage.removeItem('token')
//     router.push('/login')
//   }
//
//   return { token, nickname, permissions, login, logout }
// })


//假登录
import { defineStore } from 'pinia'
import { ref } from 'vue'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  // 模拟 token，刷新页面不掉
  const token = ref<string>(localStorage.getItem('token') || '')
  const nickname = ref('测试管理员')
  const permissions = ref<string[]>(['admin'])

  // 假登录 —— 后端没好，前端完全靠它测试！
  async function login(username: string, password: string) {
    // 模拟接口延迟
    await new Promise(resolve => setTimeout(resolve, 500))

    // 模拟登录成功
    token.value = 'test-token-' + Date.now()
    localStorage.setItem('token', token.value)

    return Promise.resolve({
      code: 200,
      message: '登录成功',
      data: { accessToken: token.value }
    })
  }

  // 登出
  function logout() {
    token.value = ''
    nickname.value = ''
    permissions.value = []
    localStorage.removeItem('token')
    router.push('/login')
  }

  return { token, nickname, permissions, login, logout }
})