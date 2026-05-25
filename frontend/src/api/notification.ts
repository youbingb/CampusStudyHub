import request from '@/utils/request'

export type NotificationType =
  | 'RESERVATION_CREATED'
  | 'RESERVATION_CANCELLED'
  | 'RESERVATION_EXPIRED'
  | 'RESERVATION_REMINDER'
  | 'REPORT_FILED'
  | 'REPORT_RESOLVED'
  | 'CREDIT_CHANGED'
  | 'ANNOUNCEMENT'
  | 'SYSTEM'

export interface NotificationVo {
  id: number
  type: NotificationType
  title: string
  content: string
  readFlag: number
  relatedId: number | null
  createdAt: string
}

export interface NotificationPayload {
  id: number
  type: NotificationType
  title: string
  content: string
  relatedId: number | null
  createdAt: string
}

export interface NotificationQuery {
  page?: number
  size?: number
  readFlag?: 0 | 1
  type?: NotificationType
}

export interface NotificationPage {
  total: number
  pages: number
  current: number
  size: number
  records: NotificationVo[]
}

export const notificationApi = {
  list: (q: NotificationQuery = {}) =>
    request.get<unknown, NotificationPage>('/notifications', { params: q }),
  unreadCount: () => request.get<unknown, number>('/notifications/unread-count'),
  markRead: (id: number) => request.put<unknown, void>(`/notifications/${id}/read`),
  markAllRead: () => request.put<unknown, number>('/notifications/read-all')
}
