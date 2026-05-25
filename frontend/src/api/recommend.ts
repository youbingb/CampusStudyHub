import request from '@/utils/request'

export interface RecommendVo {
  seatId: number
  seatNo: string
  rowNo: number
  colNo: number
  feature?: string
  roomId: number
  roomName?: string
  score: number
  roomPrefScore: number
  featurePrefScore: number
  neighborFreeScore: number
  sameSeatScore: number
  conflictScore: number
  reasons: string[]
}

export interface RecommendQuery {
  startTime: string
  endTime: string
  roomId?: number
  topN?: number
}

export const recommendApi = {
  recommend: (params: RecommendQuery) =>
    request.get<unknown, RecommendVo[]>('/recommend', { params })
}
