<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { roomApi, type RoomVo, type SeatVo, type SeatStatus } from '@/api/room'
import { ws } from '@/utils/ws'
import { useWsStore } from '@/stores/ws'

const route = useRoute()
const router = useRouter()
const wsStore = useWsStore()

const roomId = computed(() => Number(route.params.id))
const room = ref<RoomVo | null>(null)
const seats = ref<SeatVo[]>([])
const loading = ref(false)
const selectedSeat = ref<SeatVo | null>(null)
const drawerVisible = ref(false)

let unsubscribe: (() => void) | null = null

const cols = computed(() => {
  if (seats.value.length === 0) return 0
  return Math.max(...seats.value.map((s) => s.colNo))
})
const rows = computed(() => {
  if (seats.value.length === 0) return 0
  return Math.max(...seats.value.map((s) => s.rowNo))
})

const seatGrid = computed(() => {
  // 转为 grid[row][col] 方便渲染
  const map = new Map<string, SeatVo>()
  for (const s of seats.value) {
    map.set(`${s.rowNo}-${s.colNo}`, s)
  }
  const grid: (SeatVo | null)[][] = []
  for (let r = 1; r <= rows.value; r++) {
    const row: (SeatVo | null)[] = []
    for (let c = 1; c <= cols.value; c++) {
      row.push(map.get(`${r}-${c}`) || null)
    }
    grid.push(row)
  }
  return grid
})

const stats = computed(() => {
  const acc = { AVAILABLE: 0, RESERVED: 0, OCCUPIED: 0, FAULT: 0 } as Record<SeatStatus, number>
  for (const s of seats.value) acc[s.status]++
  return acc
})

async function loadAll() {
  loading.value = true
  try {
    const [r, list] = await Promise.all([
      roomApi.detail(roomId.value),
      roomApi.seatsByRoom(roomId.value)
    ])
    room.value = r
    seats.value = list
  } catch (e) {
    // 拦截器已 toast
  } finally {
    loading.value = false
  }
}

async function subscribeSeats() {
  await wsStore.ensureConnected()
  const topic = `/topic/rooms/${roomId.value}/seats`
  unsubscribe = ws.subscribe(topic, (msg) => {
    try {
      const payload = JSON.parse(msg.body) as { seatId: number; status: SeatStatus; updatedAt: string }
      const idx = seats.value.findIndex((s) => s.id === payload.seatId)
      if (idx >= 0) {
        seats.value[idx] = { ...seats.value[idx], status: payload.status }
        if (selectedSeat.value?.id === payload.seatId) {
          selectedSeat.value = { ...selectedSeat.value, status: payload.status }
        }
      }
    } catch (e) {
      console.warn('[seat-ws] parse failed', e)
    }
  })
}

function openSeat(s: SeatVo | null) {
  if (!s) return
  selectedSeat.value = s
  drawerVisible.value = true
}

function statusLabel(s: SeatStatus): string {
  return { AVAILABLE: '空闲', RESERVED: '已预约', OCCUPIED: '使用中', FAULT: '故障' }[s]
}

function reserveSeat() {
  if (!selectedSeat.value) return
  // Phase 3 由 Agent B 实现：把当前座位 ID 带去预约页 / 弹预约 Drawer
  ElMessage.info('预约入口将由 Phase 3 接入')
}

onMounted(async () => {
  await loadAll()
  subscribeSeats()
})

onUnmounted(() => {
  if (unsubscribe) unsubscribe()
})
</script>

<template>
  <div class="page seat-map-page">
    <div class="header">
      <el-button :icon="ArrowLeft" link @click="router.back()">返回</el-button>
      <h2 class="page-title">{{ room?.name || '座位平面图' }}</h2>
      <div v-if="room" class="meta">
        <span v-if="room.location"><el-icon><Location /></el-icon> {{ room.location }}</span>
        <span><el-icon><Clock /></el-icon> {{ room.openTime }}–{{ room.closeTime }}</span>
      </div>
    </div>

    <div class="legend">
      <span class="dot AVAILABLE"></span>空闲 {{ stats.AVAILABLE }}
      <span class="dot RESERVED"></span>已预约 {{ stats.RESERVED }}
      <span class="dot OCCUPIED"></span>使用中 {{ stats.OCCUPIED }}
      <span class="dot FAULT"></span>故障 {{ stats.FAULT }}
    </div>

    <div v-loading="loading" class="map">
      <div class="screen">📺 屏幕方向</div>
      <div v-if="!loading && seats.length === 0" class="empty">
        <el-empty description="该自习室还没有座位" />
      </div>
      <div v-else class="grid" :style="{ gridTemplateColumns: `repeat(${cols}, 36px)` }">
        <template v-for="(row, ri) in seatGrid" :key="ri">
          <button
            v-for="(s, ci) in row"
            :key="ri + '-' + ci"
            class="seat"
            :class="s ? s.status : 'EMPTY'"
            :disabled="!s || s.status === 'FAULT'"
            @click="openSeat(s)"
          >
            {{ s ? s.seatNo.replace(/^[A-Za-z]+/, '') : '' }}
          </button>
        </template>
      </div>
    </div>

    <el-drawer v-model="drawerVisible" :title="selectedSeat?.seatNo" direction="btt" size="40%">
      <template v-if="selectedSeat">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="座位编号">{{ selectedSeat.seatNo }}</el-descriptions-item>
          <el-descriptions-item label="位置">第 {{ selectedSeat.rowNo }} 排 / 第 {{ selectedSeat.colNo }} 列</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedSeat.status === 'AVAILABLE' ? 'success' :
              selectedSeat.status === 'RESERVED' ? 'warning' :
              selectedSeat.status === 'OCCUPIED' ? 'info' : 'danger'">
              {{ statusLabel(selectedSeat.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedSeat.feature" label="特性">{{ selectedSeat.feature }}</el-descriptions-item>
        </el-descriptions>
        <div class="drawer-footer">
          <el-button
            type="primary"
            :disabled="selectedSeat.status !== 'AVAILABLE'"
            @click="reserveSeat"
          >
            预约此座位（Phase 3）
          </el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script lang="ts">
import { ArrowLeft, Location, Clock } from '@element-plus/icons-vue'
export default { components: { ArrowLeft, Location, Clock } }
</script>

<style scoped>
.seat-map-page { padding-bottom: 80px; }
.header { display: flex; flex-direction: column; gap: 4px; margin-bottom: 8px; }
.header .page-title { margin: 0; }
.header .meta { font-size: 12px; color: #909399; display: flex; gap: 12px; }
.legend { display: flex; gap: 12px; flex-wrap: wrap; margin-bottom: 12px; font-size: 12px; color: #606266; align-items: center; }
.legend .dot { display: inline-block; width: 12px; height: 12px; border-radius: 3px; margin-right: 4px; vertical-align: middle; }
.dot.AVAILABLE { background: #67c23a; }
.dot.RESERVED { background: #e6a23c; }
.dot.OCCUPIED { background: #909399; }
.dot.FAULT { background: #f56c6c; }

.map { background: #fff; border-radius: 8px; padding: 16px; }
.screen { text-align: center; color: #909399; font-size: 13px; padding: 6px; margin-bottom: 12px; background: #fafafa; border-radius: 4px; }
.grid { display: grid; gap: 6px; justify-content: center; }
.empty { padding: 40px 0; }

.seat {
  width: 36px; height: 36px; border-radius: 6px; border: 1px solid transparent;
  font-size: 11px; color: #fff; cursor: pointer; font-family: inherit;
  display: flex; align-items: center; justify-content: center;
}
.seat.AVAILABLE { background: #67c23a; }
.seat.RESERVED  { background: #e6a23c; }
.seat.OCCUPIED  { background: #909399; }
.seat.FAULT     { background: #f56c6c; cursor: not-allowed; }
.seat.EMPTY     { background: transparent; border: 1px dashed #e4e7ed; cursor: default; color: transparent; }
.seat:disabled { opacity: 0.7; }
.seat:hover:not(:disabled) { filter: brightness(1.08); transform: scale(1.04); transition: transform 0.1s; }

.drawer-footer { margin-top: 16px; text-align: right; }
</style>
