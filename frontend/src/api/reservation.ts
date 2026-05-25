import request from '@/utils/request'

export type ReservationStatus = 'BOOKED' | 'CHECKED_IN' | 'COMPLETED' | 'CANCELLED' | 'EXPIRED'

export interface ReservationVo {
  id: number
  userId: number
  username?: string
  userRealName?: string
  seatId: number
  seatNo?: string
  roomId: number
  roomName?: string
  startTime: string
  endTime: string
  status: ReservationStatus
  checkInTime?: string
  checkOutTime?: string
  createdAt: string
}

export interface CreateReservationReq {
  seatId: number
  startTime: string
  endTime: string
}

export interface ReservationQuery {
  page?: number
  size?: number
  status?: ReservationStatus
  userId?: number
  seatId?: number
  roomId?: number
  startFrom?: string
  startTo?: string
}

export interface PageResult<T> {
  total: number
  pages: number
  current: number
  size: number
  records: T[]
}

export const reservationApi = {
  create: (data: CreateReservationReq) =>
    request.post<unknown, number>('/reservations', data),
  mine: (params: ReservationQuery) =>
    request.get<unknown, PageResult<ReservationVo>>('/reservations/mine', { params }),
  detail: (id: number) => request.get<unknown, ReservationVo>(`/reservations/${id}`),
  cancel: (id: number) => request.post<unknown, void>(`/reservations/${id}/cancel`),
  checkIn: (id: number) => request.post<unknown, void>(`/reservations/${id}/check-in`),
  checkOut: (id: number) => request.post<unknown, void>(`/reservations/${id}/check-out`)
}

export const adminReservationApi = {
  list: (params: ReservationQuery) =>
    request.get<unknown, PageResult<ReservationVo>>('/admin/reservations', { params }),
  detail: (id: number) => request.get<unknown, ReservationVo>(`/admin/reservations/${id}`),
  cancel: (id: number, reason: string) =>
    request.post<unknown, void>(`/admin/reservations/${id}/cancel`, { reason })
}
