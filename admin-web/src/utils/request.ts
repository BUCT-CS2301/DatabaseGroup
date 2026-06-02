// import axios from 'axios'
// import type { AxiosInstance, AxiosResponse } from 'axios'
// import { ElMessage } from 'element-plus'
// import router from '@/router'
//
// const service: AxiosInstance = axios.create({
//   baseURL: '/api',
//   timeout: 15000
// })
//
// service.interceptors.request.use(
//   (config) => {
//     const token = localStorage.getItem('token')
//     if (token) {
//       config.headers.Authorization = `Bearer ${token}`
//     }
//     return config
//   },
//   (error) => Promise.reject(error)
// )
//
// service.interceptors.response.use(
//   (response: AxiosResponse) => {
//     const res = response.data
//     if (res.code && res.code !== 200) {
//       ElMessage.error(res.message || '请求失败')
//       if (res.code === 401 || res.code === 403) {
//         router.push('/login')
//       }
//       return Promise.reject(new Error(res.message))
//     }
//     return response
//   },
//   (error) => {
//     ElMessage.error('网络错误，请稍后重试')
//     return Promise.reject(error)
//   }
// )
//
// export default service



import axios from 'axios'
import type { AxiosInstance, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/store/user'

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截
service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      }
      return Promise.reject(res)
    }
    return res
  },
  (error) => {
    if (error.response && error.response.status === 404) {
      console.warn('API endpoint not found:', error.config.url)
    } else {
      ElMessage.error('网络错误，请检查后端是否启动')
    }
    return Promise.reject(error)
  }
)

export default service