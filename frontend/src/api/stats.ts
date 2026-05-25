import request from '@/utils/request'

export interface OccupancyVo {
  roomId: number
  roomName: string
  capacity: number
  totalReservations: number
  completedReservations: number
  totalSeatHours: number
}

export interface UsageVo {
  userId: number
  username: string
  realName?: string
  studentNo?: string
  reservationCount: number
  completedCount: number
  noShowCount: number
  totalHours: number
}

export interface PopularHourVo {
  hour: number
  reservationCount: number
}

export interface ViolationVo {
  userId: number
  username: string
  realName?: string
  studentNo?: string
  creditScore: number
  violationCount: number
  totalDeduction: number
}

export interface FaultVo {
  roomId: number
  roomName: string
  totalFaults: number
  openFaults: number
  latestFaultAt?: string
}

export interface StatsQuery {
  from?: string
  to?: string
  topN?: number
}

export const statsApi = {
  occupancy: (params: StatsQuery) =>
    request.get<unknown, OccupancyVo[]>('/admin/stats/occupancy', { params }),
  usage: (params: StatsQuery) =>
    request.get<unknown, UsageVo[]>('/admin/stats/usage', { params }),
  popularHours: (params: StatsQuery) =>
    request.get<unknown, PopularHourVo[]>('/admin/stats/popular-hours', { params }),
  violations: (params: StatsQuery) =>
    request.get<unknown, ViolationVo[]>('/admin/stats/violations', { params }),
  faults: (params: StatsQuery) =>
    request.get<unknown, FaultVo[]>('/admin/stats/faults', { params }),
  exportUrl: (params: StatsQuery) => {
    const usp = new URLSearchParams()
    if (params.from) usp.set('from', params.from)
    if (params.to) usp.set('to', params.to)
    if (params.topN) usp.set('topN', String(params.topN))
    const q = usp.toString()
    return `/api/admin/stats/export${q ? '?' + q : ''}`
  }
}
