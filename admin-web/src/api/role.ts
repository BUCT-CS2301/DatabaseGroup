import request from '@/utils/request'

export interface RoleOption {
  objectId: string
  roleName: string
  roleCode: string
  description?: string
  permissions?: string[]
  userCount?: number
  isSystem?: boolean
  createTime?: string
  updateTime?: string
}

export interface PermissionOption {
  code: string
  name: string
}

export interface RoleListParams {
  page: number
  pageSize: number
  keyword?: string
}

export interface RoleListResult {
  records: RoleOption[]
  total: number
  page: number
  pageSize: number
}

export interface RoleUserRecord {
  objectId: string
  username: string
  nickname: string
  email: string
  phone: string
  status: string
  createTime?: string
}

export interface RoleUserListResult {
  records: RoleUserRecord[]
  total: number
  page: number
  pageSize: number
}

export interface SaveRoleRequest {
  roleName: string
  roleCode: string
  description?: string
  permissions: string[]
}

export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export const getRoleList = (params: RoleListParams) => {
  return request({
    url: '/v1/roles',
    method: 'get',
    params
  }) as unknown as Promise<ApiResult<RoleListResult>>
}

export const getAllRoles = () => {
  return request({
    url: '/v1/roles/all',
    method: 'get'
  }) as unknown as Promise<ApiResult<RoleOption[]>>
}

export const getAllPermissions = () => {
  return request({
    url: '/v1/roles/permissions',
    method: 'get'
  }) as unknown as Promise<ApiResult<PermissionOption[]>>
}

export const createRole = (data: SaveRoleRequest) => {
  return request({
    url: '/v1/roles',
    method: 'post',
    data
  }) as unknown as Promise<ApiResult<RoleOption>>
}

export const getRoleUsers = (objectId: string, params: { page: number; pageSize: number }) => {
  return request({
    url: `/v1/roles/${objectId}/users`,
    method: 'get',
    params
  }) as unknown as Promise<ApiResult<RoleUserListResult>>
}
