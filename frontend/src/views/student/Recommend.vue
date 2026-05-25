<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { roomApi, type RoomVo } from '@/api/room'
import { recommendApi, type RecommendVo } from '@/api/recommend'
import { reservationApi } from '@/api/reservation'

const router = useRouter()
const rooms = ref<RoomVo[]>([])
const list = ref<RecommendVo[]>([])
const loading = ref(false)

const form = reactive<{
  startTime: string
  endTime: string
  roomId?: number
  topN: number
}>({
  startTime: defaultStart(),
  endTime: defaultEnd(),
  topN: 5
})

function defaultStart(): string {
  const d = new Date(Date.now() + 30 * 60 * 1000)
  return toLocalIso(d)
}
function defaultEnd(): string {
  const d = new Date(Date.now() + 2 * 60 * 60 * 1000 + 30 * 60 * 1000)
  return toLocalIso(d)
}
function toLocalIso(d: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:00`
}

async function loadRooms() {
  try {
    rooms.value = await roomApi.list()
  } catch {
    // 拦截器已 toast
  }
}

async function search() {
  if (form.endTime <= form.startTime) {
    ElMessage.warning('结束时间必须晚于开始时间')
    return
  }
  loading.value = true
  try {
    list.value = await recommendApi.recommend({
      startTime: form.startTime,
      endTime: form.endTime,
      roomId: form.roomId,
      topN: form.topN
    })
  } catch {
    // 拦截器已 toast
  } finally {
    loading.value = false
  }
}

async function reserveOne(r: RecommendVo) {
  try {
    await reservationApi.create({
      seatId: r.seatId,
      startTime: form.startTime,
      endTime: form.endTime
    })
    ElMessage.success('预约成功')
    router.push('/student/reservations')
  } catch {
    // 拦截器已 toast
  }
}

function scoreColor(s: number): 'success' | 'warning' | 'info' {
  if (s >= 0.7) return 'success'
  if (s >= 0.4) return 'warning'
  return 'info'
}

function pct(v: number): string {
  return `${Math.round(v * 100)}%`
}

onMounted(() => {
  loadRooms()
  search()
})
</script>

<template>
  <div class="page">
    <h2 class="page-title">为你推荐</h2>

    <el-card shadow="never">
      <el-form :model="form" label-width="80px">
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            format="YYYY-MM-DD HH:mm"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            format="YYYY-MM-DD HH:mm"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="自习室">
          <el-select v-model="form.roomId" placeholder="不限" clearable filterable style="width: 100%">
            <el-option v-for="r in rooms" :key="r.id" :value="r.id" :label="r.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="返回数">
          <el-input-number v-model="form.topN" :min="1" :max="20" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="search">查询推荐</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-empty v-if="!loading && list.length === 0" description="该时段暂无可推荐座位" />

    <div v-loading="loading" class="list">
      <el-card v-for="(r, idx) in list" :key="r.seatId" shadow="hover" class="item">
        <div class="row">
          <span class="rank">#{{ idx + 1 }}</span>
          <span class="title">{{ r.roomName }} · {{ r.seatNo }}</span>
          <el-tag :type="scoreColor(r.score)" size="small">{{ pct(r.score) }}</el-tag>
        </div>
        <div class="reasons">
          <el-tag v-for="(t, i) in r.reasons" :key="i" size="small" type="info" effect="plain" round>{{ t }}</el-tag>
          <small v-if="r.reasons.length === 0" style="color:#909399">基础推荐</small>
        </div>
        <div class="factors">
          <span title="房间偏好">🏠 {{ pct(r.roomPrefScore) }}</span>
          <span title="特性偏好">✨ {{ pct(r.featurePrefScore) }}</span>
          <span title="邻座空闲">🪑 {{ pct(r.neighborFreeScore) }}</span>
          <span title="老位置">⭐ {{ pct(r.sameSeatScore) }}</span>
          <span title="时段无冲突">⏱ {{ pct(r.conflictScore) }}</span>
        </div>
        <div class="actions">
          <el-button size="small" @click="router.push(`/student/rooms/${r.roomId}`)">看平面图</el-button>
          <el-button size="small" type="primary" @click="reserveOne(r)">直接预约</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.list { display: flex; flex-direction: column; gap: 8px; margin-top: 12px; }
.item .row { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.item .rank { color: var(--el-color-primary); font-weight: 600; font-size: 14px; }
.item .title { font-weight: 600; flex: 1; font-size: 14px; }
.reasons { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 8px; }
.factors { display: flex; gap: 12px; color: #606266; font-size: 12px; margin-bottom: 8px; }
.actions { text-align: right; }
</style>
