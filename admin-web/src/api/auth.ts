// 接口方法待实现
import request from '@/utils/request'

// 直接在这里定义接口类型
export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export const loginApi = (data: LoginRequest) => {
  return request<ApiResponse<LoginResponse>>({
    url: '/auth/login',
    method: 'post',
    data
  })
}