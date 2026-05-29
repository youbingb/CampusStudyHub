<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import {
  notificationApi,
  type NotificationVo,
  type NotificationType,
  type NotificationQuery
} from '@/api/notification'
import { useNotificationStore } from '@/stores/notification'

const store = useNotificationStore()

const list = ref<NotificationVo[]>([])
const total = ref(0)
const loading = ref(false)
const query = ref<NotificationQuery>({ page: 1, size: 20, readFlag: undefined, type: undefined })

const typeLabel: Record<NotificationType, string> = {
  RESERVATION_CREATED: '预约创建',
  RESERVATION_CANCELLED: '预约取消',
  RESERVATION_EXPIRED: '预约超时',
  RESERVATION_REMINDER: '预约提醒',
  REPORT_FILED: '举报受理',
  REPORT_RESOLVED: '举报处理',
  CREDIT_CHANGED: '信誉变更',
  ANNOUNCEMENT: '系统公告',
  SYSTEM: '系统消息'
}

const typeTag: Record<NotificationType, '' | 'success' | 'warning' | 'info' | 'danger'> = {
  RESERVATION_CREATED: 'success',
  RESERVATION_CANCELLED: 'info',
  RESERVATION_EXPIRED: 'warning',
  RESERVATION_REMINDER: '',
  REPORT_FILED: 'info',
  REPORT_RESOLVED: 'success',
  CREDIT_CHANGED: 'warning',
  ANNOUNCEMENT: 'danger',
  SYSTEM: 'info'
}

async function load() {
  loading.value = true
  try {
    const page = await notificationApi.list(query.value)
    list.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

async function markRead(n: NotificationVo) {
  if (n.readFlag === 1) return
  await notificationApi.markRead(n.id)
  n.readFlag = 1
  store.decrementUnread()
}

async function markAll() {
  await ElMessageBox.confirm('确认将所有未读通知标记为已读?', '提示', { type: 'warning' })
  const affected = await notificationApi.markAllRead()
  ElMessage.success(`已标记 ${affected} 条`)
  list.value.forEach((n) => (n.readFlag = 1))
  store.decrementUnread(affected)
}

function changePage(p: number) {
  query.value.page = p
  load()
}

function changeFilter() {
  query.value.page = 1
  load()
}

// 监听 WS 实时推送的新通知；当前页是第 1 页 + 无筛选时直接 prepend，否则保留在 store 中等用户切回首页
watch(
  () => store.recent.length,
  () => {
    if (!store.recent.length) return
    const latest = store.recent[0]
    if (list.value.some((n) => n.id === latest.id)) return
    if (query.value.page === 1 && query.value.readFlag === undefined && !query.value.type) {
      list.value = [{ ...latest, readFlag: 0 }, ...list.value].slice(0, query.value.size || 20)
      total.value += 1
    }
  }
)

onMounted(async () => {
  await store.ensureSubscribed()
  await load()
  // 若全部消息列表为空，则强制归零未读数，避免侧栏 badge 与实际列表不一致
  if (query.value.readFlag === undefined && !query.value.type && total.value === 0) {
    store.unreadCount = 0
  }
  await store.refreshUnread()
})
</script>

<template>
  <div class="page">
    <div class="header">
      <h2 class="page-title">站内消息</h2>
      <el-tag v-if="store.unreadCount > 0" type="danger">{{ store.unreadCount }} 条未读</el-tag>
    </div>

    <el-card shadow="never" class="toolbar">
      <el-radio-group v-model="query.readFlag" @change="changeFilter">
        <el-radio-button :label="undefined">全部</el-radio-button>
        <el-radio-button :label="0">未读</el-radio-button>
        <el-radio-button :label="1">已读</el-radio-button>
      </el-radio-group>

      <el-select
        v-model="query.type"
        placeholder="按类型筛选"
        clearable
        style="width: 160px; margin-left: 12px"
        @change="changeFilter"
      >
        <el-option v-for="(label, key) in typeLabel" :key="key" :label="label" :value="key" />
      </el-select>

      <el-button
        type="primary"
        :disabled="!store.unreadCount"
        style="margin-left: auto"
        @click="markAll"
      >
        全部已读
      </el-button>
    </el-card>

    <el-card v-loading="loading" shadow="never" class="list-card">
      <div v-if="!list.length" class="empty">暂无消息</div>
      <div v-else>
        <div
          v-for="n in list"
          :key="n.id"
          class="item"
          :class="{ unread: n.readFlag === 0 }"
          @click="markRead(n)"
        >
          <div class="item-head">
            <el-tag size="small" :type="typeTag[n.type]">{{ typeLabel[n.type] }}</el-tag>
            <span class="title">{{ n.title }}</span>
            <span class="time">{{ dayjs(n.createdAt).format('MM-DD HH:mm') }}</span>
          </div>
          <div class="content">{{ n.content }}</div>
        </div>
      </div>

      <div v-if="total > (query.size || 20)" class="pager">
        <el-pagination
          :current-page="query.page"
          :page-size="query.size"
          :total="total"
          layout="prev, pager, next, total"
          @current-change="changePage"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.page { padding: 16px; }
.header { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.page-title { margin: 0; font-size: 18px; }
.toolbar { margin-bottom: 12px; }
.toolbar :deep(.el-card__body) { display: flex; align-items: center; width: 100%; padding: 8px 12px; }
.list-card :deep(.el-card__body) { padding: 0; }
.empty { padding: 32px; text-align: center; color: #909399; }
.item {
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background 0.15s;
}
.item:hover { background: #f5f7fa; }
.item.unread { background: var(--accent-soft); }
.item.unread .title { font-weight: 600; }
.item-head { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.item-head .title { flex: 1; font-size: 14px; }
.item-head .time { color: #909399; font-size: 12px; }
.content { color: #606266; font-size: 13px; line-height: 1.5; }
.pager { padding: 12px; display: flex; justify-content: center; }
</style>
