import request from '@/utils/request'

export type SeatStatus = 'AVAILABLE' | 'RESERVED' | 'OCCUPIED' | 'FAULT'

export interface RoomVo {
  id: number
  name: string
  location?: string
  capacity?: number
  openTime?: string
  closeTime?: string
  status: number
  description?: string
  totalSeats?: number
  availableSeats?: number
}

export interface SeatVo {
  id: number
  roomId: number
  seatNo: string
  rowNo: number
  colNo: number
  status: SeatStatus
  feature?: string
}

export interface CreateRoomReq {
  name: string
  location?: string
  capacity?: number
  openTime?: string
  closeTime?: string
  description?: string
}

export interface UpdateRoomReq {
  name?: string
  location?: string
  capacity?: number
  openTime?: string
  closeTime?: string
  status?: number
  description?: string
}

export interface CreateSeatReq {
  roomId: number
  seatNo: string
  rowNo: number
  colNo: number
  feature?: string
}

export interface UpdateSeatReq {
  seatNo?: string
  rowNo?: number
  colNo?: number
  status?: SeatStatus
  feature?: string
}

export interface BatchCreateSeatReq {
  roomId: number
  rows: number
  cols: number
  prefix?: string
  feature?: string
}

export const roomApi = {
  list: () => request.get<unknown, RoomVo[]>('/rooms'),
  detail: (id: number) => request.get<unknown, RoomVo>(`/rooms/${id}`),
  seatsByRoom: (roomId: number) => request.get<unknown, SeatVo[]>(`/seats/by-room/${roomId}`),
  seatDetail: (id: number) => request.get<unknown, SeatVo>(`/seats/${id}`)
}

export const adminRoomApi = {
  list: () => request.get<unknown, RoomVo[]>('/admin/rooms'),
  create: (data: CreateRoomReq) => request.post<unknown, number>('/admin/rooms', data),
  update: (id: number, data: UpdateRoomReq) => request.put<unknown, void>(`/admin/rooms/${id}`, data),
  remove: (id: number) => request.delete<unknown, void>(`/admin/rooms/${id}`)
}

export const adminSeatApi = {
  listByRoom: (roomId: number) => request.get<unknown, SeatVo[]>(`/admin/seats/by-room/${roomId}`),
  create: (data: CreateSeatReq) => request.post<unknown, number>('/admin/seats', data),
  batchCreate: (data: BatchCreateSeatReq) => request.post<unknown, number>('/admin/seats/batch', data),
  update: (id: number, data: UpdateSeatReq) => request.put<unknown, void>(`/admin/seats/${id}`, data),
  remove: (id: number) => request.delete<unknown, void>(`/admin/seats/${id}`),
  markFault: (id: number, reason: string) =>
    request.post<unknown, void>(`/admin/seats/${id}/fault`, { reason }),
  clearFault: (id: number) => request.post<unknown, void>(`/admin/seats/${id}/fault/clear`),
  refresh: (id: number) => request.post<unknown, void>(`/admin/seats/${id}/refresh`)
}
