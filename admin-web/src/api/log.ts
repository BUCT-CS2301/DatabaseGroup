import request from '@/utils/request'

export interface LogRecord {
  id: string
  type: 'operation' | 'login' | 'system' | 'error' | 'security' | 'audit'
  username: string
  module: string
  action: string
  description: string
  ip: string
  userAgent?: string
  result: 'success' | 'failed' | 'error'
  resultData?: string
  params?: string
  createTime: string
}

export interface LogStats {
  operationCount: number
  loginCount: number
  errorCount: number
  securityCount: number
}

export function getLogList(params: {
  page: number
  pageSize: number
  type?: string
  username?: string
  module?: string
  action?: string
  keyword?: string
  startTime?: string
  endTime?: string
}): Promise<{ data: LogRecord[]; total: number }> {
  return request({
    url: '/api/v1/log/list',
    method: 'get',
    params
  })
}

export function getLogDetail(id: string): Promise<LogRecord> {
  return request({
    url: `/api/v1/log/${id}`,
    method: 'get'
  })
}

export function getLogStats(): Promise<LogStats> {
  return request({
    url: '/api/v1/log/stats',
    method: 'get'
  })
}

export function exportLogs(params: {
  format: 'csv' | 'excel'
  range: 'all' | 'current' | 'filtered'
  type?: string
  username?: string
  module?: string
  action?: string
  keyword?: string
  startTime?: string
  endTime?: string
}): Promise<Blob> {
  return request({
    url: '/api/v1/log/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
