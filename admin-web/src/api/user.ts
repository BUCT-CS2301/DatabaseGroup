import request from '@/utils/request'

export interface User {
  objectId: string
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
  userType: string
  status: string
  roles: string[]
  lastLoginTime: string
  createTime: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

export function getUserList(params: {
  page?: number
  pageSize?: number
  keyword?: string
  status?: string
}) {
  return request({
    url: '/api/v1/users',
    method: 'get',
    params
  })
}

export function getUserDetail(objectId: string) {
  return request({
    url: `/api/v1/users/${objectId}`,
    method: 'get'
  })
}

export function createUser(data: {
  username: string
  password: string
  nickname: string
  email?: string
  phone?: string
  roleIds?: string[]
}) {
  return request({
    url: '/api/v1/users',
    method: 'post',
    data
  })
}

export function updateUser(objectId: string, data: {
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
  roleIds?: string[]
}) {
  return request({
    url: `/api/v1/users/${objectId}`,
    method: 'put',
    data
  })
}

export function deleteUser(objectId: string) {
  return request({
    url: `/api/v1/users/${objectId}`,
    method: 'delete'
  })
}

export function updateUserStatus(objectId: string, status: string) {
  return request({
    url: `/api/v1/users/${objectId}/status`,
    method: 'put',
    data: { status }
  })
}

export function getUserLogs(objectId: string, params: {
  page?: number
  pageSize?: number
}) {
  return request({
    url: `/api/v1/users/${objectId}/logs`,
    method: 'get',
    params
  })
}