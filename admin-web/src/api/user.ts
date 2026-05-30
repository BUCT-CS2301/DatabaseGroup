import request from '@/utils/request'

export interface UserRecord {
  objectId: string
  username: string
  nickname: string
  email: string
  phone: string
  status: 'ENABLED' | 'DISABLED' | string
  roles: string[]
  lastLoginTime?: string
  createTime?: string
}

export interface UserListParams {
  page: number
  pageSize: number
  keyword?: string
  status?: string
  role?: string
}

export interface UserListResult {
  records: UserRecord[]
  total: number
  page: number
  pageSize: number
}

export interface SaveUserRequest {
  username?: string
  password?: string
  nickname: string
  email: string
  phone: string
  role?: string
  roles?: string[]
}

export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export const getUserList = (params: UserListParams) => {
  return request({
    url: '/api/v1/users',
    method: 'get',
    params
  }) as unknown as Promise<ApiResult<UserListResult>>
}

export const createUser = (data: SaveUserRequest) => {
  return request({
    url: '/api/v1/users',
    method: 'post',
    data
  }) as unknown as Promise<ApiResult<UserRecord>>
}

export const updateUser = (objectId: string, data: SaveUserRequest) => {
  return request({
    url: `/v1/users/${objectId}`,
    method: 'put',
    data
  }) as unknown as Promise<ApiResult<UserRecord>>
}

export const deleteUser = (objectId: string) => {
  return request({
    url: `/v1/users/${objectId}`,
    method: 'delete'
  }) as unknown as Promise<ApiResult<null>>
}

export const updateUserStatus = (objectId: string, status: string) => {
  return request({
    url: `/v1/users/${objectId}/status`,
    method: 'put',
    data: { status }
  }) as unknown as Promise<ApiResult<{ objectId: string; status: string }>>
}

export const updateUserRole = (objectId: string, role: string) => {
  return request({
    url: `/v1/users/${objectId}/role`,
    method: 'put',
    data: { role }
  }) as unknown as Promise<ApiResult<{ objectId: string; roles: string[] }>>
}

export const resetUserPassword = (objectId: string, password: string) => {
  return request({
    url: `/v1/users/${objectId}/password`,
    method: 'put',
    data: { password }
  }) as unknown as Promise<ApiResult<null>>
}
