import request from '@/utils/request'
import type { UserInfo, Role } from '@/stores/user'

export interface UpdateProfileReq {
  realName?: string
  phone?: string
  email?: string
}

export interface ChangePasswordReq {
  oldPassword: string
  newPassword: string
}

export interface UserQuery {
  page?: number
  size?: number
  keyword?: string
  role?: Role
  status?: 0 | 1
}

export interface UserPage {
  total: number
  pages: number
  current: number
  size: number
  records: UserInfo[]
}

export interface AdjustCreditReq {
  delta: number
  reason: string
}

export const userApi = {
  /** 学生自助 */
  updateMe: (data: UpdateProfileReq) => request.put<unknown, UserInfo>('/users/me', data),
  changePassword: (data: ChangePasswordReq) =>
    request.post<unknown, void>('/users/me/password', data),

  /** 管理端 */
  adminList: (q: UserQuery = {}) =>
    request.get<unknown, UserPage>('/admin/users', { params: q }),
  adminUpdateStatus: (id: number, status: 0 | 1) =>
    request.put<unknown, void>(`/admin/users/${id}/status`, { status }),
  adminAdjustCredit: (id: number, data: AdjustCreditReq) =>
    request.post<unknown, number>(`/admin/users/${id}/credit`, data)
}
