import request from '@/utils/request'

export interface DashboardStats {
  onlineUsers: number        // 在线用户�?
  todayNewUsers: number      // 今日新增用户�?
  todayContentSubmit: number // 今日内容提交�?
  pendingAudit: number       // 审核队列积压�?
  relicCount: number         // 文物总数
  userCount: number          // 用户总数
  todayVisit: number         // 今日访问�?
  contentCount: number       // 内容总数
}

export interface VisitTrend {
  date: string
  value: number
}

export interface GrowthTrend {
  date: string
  relicCount: number
  userCount: number
  contentCount: number
}

export interface PendingAudit {
  objectId: string
  contentType: string
  content: string
  submitTime: string
}

export interface SystemLog {
  timestamp: string
  type: string
  content: string
}

// 获取实时统计数据
export function getStats(): Promise<DashboardStats> {
  return request({
    url: '/v1/dashboard/stats',
    method: 'get'
  })
}

// 获取访问量趋�?
export function getVisitTrend(period: 'day' | 'week' | 'month'): Promise<VisitTrend[]> {
  return request({
    url: '/v1/dashboard/visit-trend',
    method: 'get',
    params: { period }
  })
}

// 获取数据增长趋势
export function getGrowthTrend(period: 'week' | 'month' | 'year'): Promise<GrowthTrend[]> {
  return request({
    url: '/v1/dashboard/growth-trend',
    method: 'get',
    params: { period }
  })
}

// 获取待审核内�?
export function getPendingAudits(): Promise<PendingAudit[]> {
  return request({
    url: '/v1/dashboard/pending-audits',
    method: 'get'
  })
}

// 获取系统日志
export function getSystemLogs(): Promise<SystemLog[]> {
  return request({
    url: '/v1/dashboard/system-logs',
    method: 'get'
  })
}