import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'
import { ws } from '@/utils/ws'

export type Role = 'STUDENT' | 'ADMIN'

export interface UserInfo {
  id: number
  username: string
  realName?: string
  studentNo?: string
  phone?: string
  email?: string
  role: Role
  creditScore: number
  status: number
}

const TOKEN_KEY = 'csh-token'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  function setToken(t: string) {
    token.value = t
    if (t) localStorage.setItem(TOKEN_KEY, t)
    else localStorage.removeItem(TOKEN_KEY)
  }

  async function fetchMe(): Promise<UserInfo | null> {
    try {
      // Phase 1 由 Agent A 实现 /api/auth/me
      const u = await request.get<unknown, UserInfo>('/auth/me')
      user.value = u
      return u
    } catch {
      user.value = null
      return null
    }
  }

  async function tryRestore() {
    if (!token.value) return
    await fetchMe()
  }

  function logout() {
    setToken('')
    user.value = null
    try {
      ws.disconnect()
    } catch {
      // ignore
    }
  }

  return { token, user, isLoggedIn, isAdmin, setToken, fetchMe, tryRestore, logout }
})
