import request from '@/utils/request'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export interface CurrentUser {
  objectId: string
  username: string
  nickname: string
  avatar?: string
  roles: string[]
  permissions: string[]
}

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export const loginApi = (data: LoginRequest) => {
  return request({
    url: '/api/v1/auth/login',
    method: 'post',
    data
  }) as unknown as Promise<ApiResponse<LoginResponse>>
}

export const getCurrentUserApi = () => {
  return request({
    url: '/api/v1/auth/current-user',
    method: 'get'
  }) as unknown as Promise<ApiResponse<CurrentUser>>
}

export const logoutApi = () => {
  return request({
    url: '/api/v1/auth/logout',
    method: 'post'
  }) as unknown as Promise<ApiResponse<null>>
}
