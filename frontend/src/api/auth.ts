import request from '@/utils/request'
import type { UserInfo } from '@/stores/user'

export interface LoginReq {
  username: string
  password: string
}

export interface LoginResp {
  token: string
  user: UserInfo
}

export interface RegisterReq {
  username: string
  password: string
  realName?: string
  studentNo?: string
  phone?: string
  email?: string
}

/** Phase 1 由 Agent A 实现 */
export const authApi = {
  login: (data: LoginReq) => request.post<unknown, LoginResp>('/auth/login', data),
  register: (data: RegisterReq) => request.post<unknown, number>('/auth/register', data),
  me: () => request.get<unknown, UserInfo>('/auth/me'),
  logout: () => request.post<unknown, void>('/auth/logout')
}
