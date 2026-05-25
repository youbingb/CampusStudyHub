import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ws } from '@/utils/ws'
import { useUserStore } from './user'

/**
 * 全局 WS 连接管理。
 * 业务模块订阅各自主题：
 *   /topic/rooms/{roomId}/seats     （Agent B 用 - 座位状态广播）
 *   /user/queue/notifications       （Agent A 用 - 个人站内消息）
 */
export const useWsStore = defineStore('ws', () => {
  const connected = ref(false)

  async function ensureConnected() {
    if (connected.value) return
    const userStore = useUserStore()
    try {
      await ws.connect(userStore.token || undefined)
      connected.value = true
    } catch (e) {
      console.warn('[ws] connect failed', e)
    }
  }

  function disconnect() {
    ws.disconnect()
    connected.value = false
  }

  return { connected, ensureConnected, disconnect }
})
