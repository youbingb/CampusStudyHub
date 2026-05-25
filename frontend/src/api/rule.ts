import request from '@/utils/request'

export interface RuleVo {
  id: number
  maxDaily: number
  maxAdvanceDays: number
  minCredit: number
  checkInGraceMin: number
  maxDurationHours: number
  noShowCreditPenalty: number
  updatedAt: string
}

export interface UpdateRuleReq {
  maxDaily?: number
  maxAdvanceDays?: number
  minCredit?: number
  checkInGraceMin?: number
  maxDurationHours?: number
  noShowCreditPenalty?: number
}

export const ruleApi = {
  current: () => request.get<unknown, RuleVo>('/rules/current'),
  admin: () => request.get<unknown, RuleVo>('/admin/rules'),
  update: (data: UpdateRuleReq) => request.put<unknown, RuleVo>('/admin/rules', data)
}
