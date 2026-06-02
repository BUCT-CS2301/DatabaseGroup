import request from '@/utils/request'

export interface BackupRecord {
  id: string
  name: string
  type: 'full' | 'incremental' | 'config'
  size: string
  status: 'success' | 'running' | 'failed'
  createTime: string
  operator: string
  remark?: string
}

export interface BackupTask {
  id: string
  name: string
  type: 'full' | 'incremental' | 'config'
  cron: string
  enabled: boolean
  retentionDays: number
  createTime: string
  updateTime: string
}

export interface BackupPolicy {
  retentionDays: number
  autoCleanEnabled: boolean
  maxBackups: number
}

export function getBackupList(params: {
  page: number
  pageSize: number
  type?: string
  status?: string
}): Promise<{ data: BackupRecord[]; total: number }> {
  return request({
    url: '/api/v1/backup/records',
    method: 'get',
    params
  })
}

export function createBackup(data: {
  name: string
  type: 'full' | 'incremental' | 'config'
  remark?: string
}): Promise<{ id: string }> {
  return request({
    url: '/api/v1/backup/manual',
    method: 'post',
    data
  })
}

export function downloadBackup(id: string): Promise<void> {
  return request({
    url: `/api/v1/backup/records/${id}/download`,
    method: 'get',
    responseType: 'blob'
  })
}

export function restoreBackup(id: string): Promise<void> {
  return request({
    url: `/api/v1/backup/restore/${id}`,
    method: 'post'
  })
}

export function deleteBackup(id: string): Promise<void> {
  return request({
    url: `/api/v1/backup/records/${id}`,
    method: 'delete'
  })
}

export function getBackupTasks(params: {
  page: number
  pageSize: number
}): Promise<{ data: BackupTask[]; total: number }> {
  return request({
    url: '/api/v1/backup/schedules',
    method: 'get',
    params
  })
}

export function createBackupTask(data: {
  name: string
  type: 'full' | 'incremental' | 'config'
  cron: string
  retentionDays: number
}): Promise<{ id: string }> {
  return request({
    url: '/api/v1/backup/schedules',
    method: 'post',
    data
  })
}

export function updateBackupTask(id: string, data: {
  name?: string
  type?: 'full' | 'incremental' | 'config'
  cron?: string
  enabled?: boolean
  retentionDays?: number
}): Promise<void> {
  return request({
    url: `/api/v1/backup/schedules/${id}`,
    method: 'put',
    data
  })
}

export function deleteBackupTask(id: string): Promise<void> {
  return request({
    url: `/api/v1/backup/schedules/${id}`,
    method: 'delete'
  })
}

export function getBackupPolicy(): Promise<BackupPolicy> {
  return request({
    url: '/api/v1/backup/storage-info',
    method: 'get'
  })
}

export function updateBackupPolicy(data: BackupPolicy): Promise<void> {
  return request({
    url: '/api/v1/backup/storage-info',
    method: 'put',
    data
  })
}