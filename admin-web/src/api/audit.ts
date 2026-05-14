import request from '@/utils/request'

export interface AuditQueue {
  objectId: string
  contentType: string
  contentText: string
  contentUrl: string
  authorId: string
  autoAuditResult: string
  autoAuditDetail: string
  status: string
  submitTime: string
  auditUserId: string
  auditTime: string
  auditRemark: string
  rejectReason: string
}

export interface AuditRule {
  objectId: string
  ruleType: string
  configJson: string
  enabled: number
  updateTime: string
}

export interface AuditStatistics {
  totalSubmitted: number
  autoApproved: number
  autoRejected: number
  manualApproved: number
  manualRejected: number
  pendingCount: number
}

export function getRules() {
  return request({
    url: '/api/v1/audit/rules',
    method: 'get'
  })
}

export function updateRules(data: AuditRule) {
  return request({
    url: '/api/v1/audit/rules',
    method: 'put',
    data
  })
}

export function getAuditQueue(params: {
  page?: number
  pageSize?: number
  type?: string
  status?: string
}) {
  return request({
    url: '/api/v1/audit/queue',
    method: 'get',
    params
  })
}

export function getAuditDetail(objectId: string) {
  return request({
    url: `/api/v1/audit/queue/${objectId}`,
    method: 'get'
  })
}

export function approve(objectId: string, remark?: string) {
  return request({
    url: `/api/v1/audit/queue/${objectId}/approve`,
    method: 'post',
    data: remark ? { remark } : {}
  })
}

export function reject(objectId: string, reason: string, remark?: string) {
  return request({
    url: `/api/v1/audit/queue/${objectId}/reject`,
    method: 'post',
    data: { reason, remark }
  })
}

export function batchApprove(objectIds: string[], remark?: string) {
  return request({
    url: '/api/v1/audit/queue/batch-approve',
    method: 'post',
    data: { objectIds, remark }
  })
}

export function batchReject(objectIds: string[], reason: string) {
  return request({
    url: '/api/v1/audit/queue/batch-reject',
    method: 'post',
    data: { objectIds, reason }
  })
}

export function getStatistics(params: {
  startDate: string
  endDate: string
}) {
  return request({
    url: '/api/v1/audit/statistics',
    method: 'get',
    params
  })
}