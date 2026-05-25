import request from '@/utils/request'

export interface AnnouncementVo {
  id: number
  title: string
  content: string
  publisherId: number
  publisherName?: string
  status: number
  publishedAt?: string
  createdAt: string
  updatedAt: string
}

export interface CreateAnnouncementReq {
  title: string
  content: string
  publishNow?: boolean
}

export interface UpdateAnnouncementReq {
  title?: string
  content?: string
}

export interface AnnouncementQuery {
  keyword?: string
  status?: number
  page?: number
  size?: number
}

export interface AnnouncementPage {
  total: number
  pages: number
  current: number
  size: number
  records: AnnouncementVo[]
}

export const announcementApi = {
  // 学生侧
  list: (params: AnnouncementQuery) =>
    request.get<unknown, AnnouncementPage>('/announcements', { params }),
  detail: (id: number) => request.get<unknown, AnnouncementVo>(`/announcements/${id}`),
  active: (limit = 5) =>
    request.get<unknown, AnnouncementVo[]>('/announcements/active', { params: { limit } }),

  // 管理侧
  adminList: (params: AnnouncementQuery) =>
    request.get<unknown, AnnouncementPage>('/admin/announcements', { params }),
  adminDetail: (id: number) =>
    request.get<unknown, AnnouncementVo>(`/admin/announcements/${id}`),
  create: (data: CreateAnnouncementReq) =>
    request.post<unknown, number>('/admin/announcements', data),
  update: (id: number, data: UpdateAnnouncementReq) =>
    request.put<unknown, void>(`/admin/announcements/${id}`, data),
  publish: (id: number) =>
    request.post<unknown, void>(`/admin/announcements/${id}/publish`),
  unpublish: (id: number) =>
    request.post<unknown, void>(`/admin/announcements/${id}/unpublish`),
  delete: (id: number) => request.delete<unknown, void>(`/admin/announcements/${id}`)
}
