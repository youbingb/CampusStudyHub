import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

type Handler = (msg: IMessage) => void

class WsClient {
  private client: Client | null = null
  private subs = new Map<string, StompSubscription>()
  private pendingSubs: Array<{ destination: string; handler: Handler }> = []
  private connected = false

  connect(token?: string): Promise<void> {
    if (this.client && this.connected) return Promise.resolve()
    return new Promise((resolve, reject) => {
      this.client = new Client({
        webSocketFactory: () => new SockJS('/ws') as any,
        connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
        reconnectDelay: 5000,
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,
        debug: () => {}
      })

      this.client.onConnect = () => {
        this.connected = true
        // 重连后重订
        for (const { destination, handler } of this.pendingSubs) {
          this.doSubscribe(destination, handler)
        }
        this.pendingSubs = []
        resolve()
      }

      this.client.onStompError = (frame) => {
        console.warn('[ws] STOMP error', frame.headers['message'])
        reject(new Error(frame.headers['message']))
      }

      this.client.onWebSocketClose = () => {
        this.connected = false
      }

      this.client.activate()
    })
  }

  private doSubscribe(destination: string, handler: Handler) {
    if (!this.client || !this.connected) {
      this.pendingSubs.push({ destination, handler })
      return
    }
    const sub = this.client.subscribe(destination, handler)
    this.subs.set(destination, sub)
  }

  subscribe(destination: string, handler: Handler): () => void {
    this.doSubscribe(destination, handler)
    return () => this.unsubscribe(destination)
  }

  unsubscribe(destination: string) {
    const sub = this.subs.get(destination)
    if (sub) {
      sub.unsubscribe()
      this.subs.delete(destination)
    }
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate()
      this.client = null
      this.subs.clear()
      this.pendingSubs = []
      this.connected = false
    }
  }
}

export const ws = new WsClient()
