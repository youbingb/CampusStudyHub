import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ElNotification } from 'element-plus'
import { notificationApi, type NotificationPayload } from '@/api/notification'
import { useWsStore } from './ws'
import { useUserStore } from './user'
import { ws } from '@/utils/ws'

const TOPIC = '/user/queue/notifications'

export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)
  /** 实时推送进来的最新通知（最多保留 20 条，用于其它页面 watch 实时插入） */
  const recent = ref<NotificationPayload[]>([])
  const subscribed = ref(false)

  async function refreshUnread() {
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) {
      unreadCount.value = 0
      return
    }
    try {
      unreadCount.value = await notificationApi.unreadCount()
    } catch {
      // ignore
    }
  }

  async function ensureSubscribed() {
    if (subscribed.value) return
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) return
    const wsStore = useWsStore()
    await wsStore.ensureConnected()
    ws.subscribe(TOPIC, (msg) => {
      try {
        const payload = JSON.parse(msg.body) as NotificationPayload
        onIncoming(payload)
      } catch (e) {
        console.warn('[notification] parse payload failed', e)
      }
    })
    subscribed.value = true
  }

  function onIncoming(p: NotificationPayload) {
    unreadCount.value += 1
    recent.value = [p, ...recent.value].slice(0, 20)
    ElNotification({
      title: p.title,
      message: p.content,
      type: p.type === 'CREDIT_CHANGED' ? 'warning' : 'info',
      position: 'top-right',
      duration: 4500
    })
  }

  function decrementUnread(by = 1) {
    unreadCount.value = Math.max(0, unreadCount.value - by)
  }

  function reset() {
    unreadCount.value = 0
    recent.value = []
    subscribed.value = false
  }

  return { unreadCount, recent, subscribed, refreshUnread, ensureSubscribed, decrementUnread, reset }
})
