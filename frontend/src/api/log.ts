import request from '@/utils/request'

export interface OperationLogVo {
  id: number
  userId?: number
  username?: string
  module?: string
  action?: string
  targetId?: string
  ip?: string
  ua?: string
  createdAt: string
}

export interface OperationLogQuery {
  module?: string
  action?: string
  username?: string
  userId?: number
  from?: string
  to?: string
  page?: number
  size?: number
}

export interface OperationLogPage {
  total: number
  pages: number
  current: number
  size: number
  records: OperationLogVo[]
}

export const logApi = {
  list: (params: OperationLogQuery) =>
    request.get<unknown, OperationLogPage>('/admin/logs', { params })
}
