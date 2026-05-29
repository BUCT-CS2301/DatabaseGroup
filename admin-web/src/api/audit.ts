import request from '@/utils/request'

export interface AuditAuthor {
  objectId: string
}

export interface AuditRecord {
  objectId: string
  type: string
  content: string
  contentUrl?: string
  author: AuditAuthor
  autoAuditResult: string
  autoAuditDetail?: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | string
  submitTime: string
  auditRemark?: string
  rejectReason?: string
}

export interface AuditListParams {
  page: number
  pageSize: number
  type?: string
  status?: string
}

export interface AuditListResult {
  records: AuditRecord[]
  total: number
  page: number
  pageSize: number
}

export interface AuditStatistics {
  totalSubmitted: number
  autoApproved: number
  autoRejected: number
  manualApproved: number
  manualRejected: number
  pendingCount: number
}

export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export const getAuditQueue = (params: AuditListParams) => {
  return request({
    url: '/api/v1/audit/queue',
    method: 'get',
    params
  }) as unknown as Promise<ApiResult<AuditListResult>>
}

export const getAuditDetail = (objectId: string) => {
  return request({
    url: `/v1/audit/queue/${objectId}`,
    method: 'get'
  }) as unknown as Promise<ApiResult<AuditRecord>>
}

export const approveAudit = (objectId: string, remark?: string) => {
  return request({
    url: `/v1/audit/queue/${objectId}/approve`,
    method: 'post',
    data: { remark }
  }) as unknown as Promise<ApiResult<null>>
}

export const rejectAudit = (objectId: string, reason: string, remark?: string) => {
  return request({
    url: `/v1/audit/queue/${objectId}/reject`,
    method: 'post',
    data: { reason, remark }
  }) as unknown as Promise<ApiResult<null>>
}

export const batchApproveAudit = (objectIds: string[], remark?: string) => {
  return request({
    url: '/api/v1/audit/queue/batch-approve',
    method: 'post',
    data: { objectIds, remark }
  }) as unknown as Promise<ApiResult<null>>
}

export const batchRejectAudit = (objectIds: string[], reason: string) => {
  return request({
    url: '/api/v1/audit/queue/batch-reject',
    method: 'post',
    data: { objectIds, reason }
  }) as unknown as Promise<ApiResult<null>>
}

export const getAuditStatistics = () => {
  return request({
    url: '/api/v1/audit/statistics',
    method: 'get'
  }) as unknown as Promise<ApiResult<AuditStatistics>>
}
