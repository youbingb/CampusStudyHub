import request from '@/utils/request'

export type ReportStatus = 'PENDING' | 'PROCESSING' | 'RESOLVED' | 'REJECTED'

export interface ReportVo {
  id: number
  type: string
  description: string
  evidenceUrl?: string
  status: ReportStatus
  result?: string
  reporterId: number
  reporterName?: string
  targetUserId?: number
  targetUserName?: string
  reservationId?: number
  seatId?: number
  handlerId?: number
  handlerName?: string
  handledAt?: string
  createdAt: string
}

export interface CreateReportReq {
  type: string
  description: string
  targetUserId?: number
  reservationId?: number
  seatId?: number
  evidenceUrl?: string
}

export interface ReportQuery {
  status?: ReportStatus
  type?: string
  keyword?: string
  reporterId?: number
  targetUserId?: number
  page?: number
  size?: number
}

export interface ProcessReportReq {
  action: 'APPROVE' | 'REJECT'
  result?: string
  creditDelta?: number
  creditReason?: string
}

export interface ReportPage {
  total: number
  pages: number
  current: number
  size: number
  records: ReportVo[]
}

export const REPORT_TYPES = ['占座', '喧哗', '设施损坏', '其他'] as const

export const reportApi = {
  create: (data: CreateReportReq) => request.post<unknown, number>('/reports', data),
  mine: (params: ReportQuery) => request.get<unknown, ReportPage>('/reports/mine', { params }),
  detail: (id: number) => request.get<unknown, ReportVo>(`/reports/${id}`),
  cancel: (id: number) => request.delete<unknown, void>(`/reports/${id}`),

  adminList: (params: ReportQuery) => request.get<unknown, ReportPage>('/admin/reports', { params }),
  adminDetail: (id: number) => request.get<unknown, ReportVo>(`/admin/reports/${id}`),
  process: (id: number, data: ProcessReportReq) =>
    request.post<unknown, void>(`/admin/reports/${id}/process`, data)
}
