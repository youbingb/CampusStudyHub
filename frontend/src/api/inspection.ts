import request from '@/utils/request'

export interface InspectionVo {
  id: number
  roomId: number
  roomName?: string
  inspectorId: number
  inspectorName?: string
  content?: string
  issues: number[]
  createdAt: string
}

export interface CreateInspectionReq {
  roomId: number
  content?: string
  issues?: number[]
}

export interface InspectionQuery {
  roomId?: number
  inspectorId?: number
  from?: string
  to?: string
  page?: number
  size?: number
}

export interface InspectionPage {
  total: number
  pages: number
  current: number
  size: number
  records: InspectionVo[]
}

export const inspectionApi = {
  list: (params: InspectionQuery) =>
    request.get<unknown, InspectionPage>('/admin/inspections', { params }),
  detail: (id: number) => request.get<unknown, InspectionVo>(`/admin/inspections/${id}`),
  create: (data: CreateInspectionReq) =>
    request.post<unknown, number>('/admin/inspections', data),
  delete: (id: number) => request.delete<unknown, void>(`/admin/inspections/${id}`)
}
