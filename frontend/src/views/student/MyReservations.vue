<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { reservationApi, type ReservationStatus, type ReservationVo } from '@/api/reservation'

interface Tab {
  key: 'ACTIVE' | 'HISTORY' | 'ALL'
  label: string
  statuses: ReservationStatus[] | null
}

const tabs: Tab[] = [
  { key: 'ACTIVE',  label: '进行中', statuses: ['BOOKED', 'CHECKED_IN'] },
  { key: 'HISTORY', label: '历史',   statuses: ['COMPLETED', 'CANCELLED', 'EXPIRED'] },
  { key: 'ALL',     label: '全部',   statuses: null }
]

const activeTab = ref<Tab['key']>('ACTIVE')
const records = ref<ReservationVo[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)

const STATUS_TEXT: Record<ReservationStatus, string> = {
  BOOKED:     '待签到',
  CHECKED_IN: '使用中',
  COMPLETED:  '已完成',
  CANCELLED:  '已取消',
  EXPIRED:    '超时'
}
const STATUS_TYPE: Record<ReservationStatus, 'success' | 'warning' | 'info' | 'danger' | 'primary'> = {
  BOOKED:     'warning',
  CHECKED_IN: 'success',
  COMPLETED:  'info',
  CANCELLED:  'info',
  EXPIRED:    'danger'
}

function fmt(s?: string): string {
  if (!s) return '-'
  return s.replace('T', ' ').slice(0, 19)
}

async function load() {
  loading.value = true
  try {
    const tab = tabs.find((t) => t.key === activeTab.value)!
    if (tab.statuses && tab.statuses.length === 1) {
      const r = await reservationApi.mine({ page: page.value, size: size.value, status: tab.statuses[0] })
      records.value = r.records
      total.value = r.total
    } else if (tab.statuses && tab.statuses.length > 1) {
      // Multi-status: fetch all and filter client-side
      const r = await reservationApi.mine({ page: 1, size: 1000 })
      const filtered = r.records.filter((x) => tab.statuses!.includes(x.status))
      const start = (page.value - 1) * size.value
      records.value = filtered.slice(start, start + size.value)
      total.value = filtered.length
    } else {
      const r = await reservationApi.mine({ page: page.value, size: size.value })
      records.value = r.records
      total.value = r.total
    }
  } finally {
    loading.value = false
  }
}

async function doCheckIn(r: ReservationVo) {
  try {
    await ElMessageBox.confirm(`确定签到 ${r.roomName} · ${r.seatNo}？`, '提示', { type: 'info' })
  } catch { return }
  try {
    await reservationApi.checkIn(r.id)
    ElMessage.success('签到成功')
    load()
  } catch {
    // 拦截器已 toast
  }
}

async function doCheckOut(r: ReservationVo) {
  try {
    await ElMessageBox.confirm(`确认提前签退 ${r.roomName} · ${r.seatNo}？`, '提示', { type: 'warning' })
  } catch { return }
  await reservationApi.checkOut(r.id)
  ElMessage.success('已签退')
  load()
}

async function doCancel(r: ReservationVo) {
  try {
    await ElMessageBox.confirm(`确认取消该预约？`, '提示', { type: 'warning' })
  } catch { return }
  await reservationApi.cancel(r.id)
  ElMessage.success('已取消')
  load()
}

function switchTab(key: Tab['key']) {
  activeTab.value = key
  page.value = 1
  load()
}

onMounted(load)
</script>

<template>
  <div class="page">
    <h2 class="page-title">我的预约</h2>

    <el-tabs v-model="activeTab" @tab-change="(name: any) => switchTab(name as Tab['key'])">
      <el-tab-pane v-for="t in tabs" :key="t.key" :name="t.key" :label="t.label" />
    </el-tabs>

    <el-empty v-if="!loading && records.length === 0" description="暂无预约" />

    <div v-loading="loading" class="list">
      <el-card v-for="r in records" :key="r.id" shadow="never" class="item">
        <div class="row">
          <span class="title">{{ r.roomName }} · {{ r.seatNo }}</span>
          <el-tag :type="STATUS_TYPE[r.status]" size="small">{{ STATUS_TEXT[r.status] }}</el-tag>
        </div>
        <div class="meta">
          <div><el-icon><Clock /></el-icon> {{ fmt(r.startTime) }} ~ {{ fmt(r.endTime) }}</div>
          <div v-if="r.checkInTime"><el-icon><Check /></el-icon> 签到 {{ fmt(r.checkInTime) }}</div>
          <div v-if="r.checkOutTime"><el-icon><CircleClose /></el-icon> 签退 {{ fmt(r.checkOutTime) }}</div>
        </div>
        <div class="actions">
          <el-button v-if="r.status === 'BOOKED'" type="primary" size="small" @click="doCheckIn(r)">签到</el-button>
          <el-button v-if="r.status === 'BOOKED'" size="small" @click="doCancel(r)">取消</el-button>
          <el-button v-if="r.status === 'CHECKED_IN'" type="warning" size="small" @click="doCheckOut(r)">签退</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script lang="ts">
import { Clock, Check, CircleClose } from '@element-plus/icons-vue'
export default { components: { Clock, Check, CircleClose } }
</script>

<style scoped>
.list { display: flex; flex-direction: column; gap: 8px; }
.item .row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.item .title { font-weight: 600; font-size: 14px; }
.meta { font-size: 12px; color: #606266; display: flex; flex-direction: column; gap: 4px; }
.meta .el-icon { vertical-align: -3px; margin-right: 4px; }
.actions { margin-top: 8px; text-align: right; }
</style>
