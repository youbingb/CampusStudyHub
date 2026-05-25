<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminReservationApi,
  type ReservationStatus,
  type ReservationVo
} from '@/api/reservation'
import { adminRoomApi, type RoomVo } from '@/api/room'

const rooms = ref<RoomVo[]>([])
const records = ref<ReservationVo[]>([])
const loading = ref(false)
const total = ref(0)

const filter = reactive<{
  page: number
  size: number
  status?: ReservationStatus
  roomId?: number
  startFrom?: string
  startTo?: string
}>({ page: 1, size: 10 })

const STATUS_OPTIONS: { value: ReservationStatus; label: string }[] = [
  { value: 'BOOKED', label: '待签到' },
  { value: 'CHECKED_IN', label: '使用中' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'CANCELLED', label: '已取消' },
  { value: 'EXPIRED', label: '超时' }
]

function statusTag(s: ReservationStatus): { type: 'success' | 'warning' | 'info' | 'danger'; label: string } {
  switch (s) {
    case 'BOOKED':     return { type: 'warning', label: '待签到' }
    case 'CHECKED_IN': return { type: 'success', label: '使用中' }
    case 'COMPLETED':  return { type: 'info', label: '已完成' }
    case 'CANCELLED':  return { type: 'info', label: '已取消' }
    case 'EXPIRED':    return { type: 'danger', label: '超时' }
  }
}

async function loadRooms() {
  rooms.value = await adminRoomApi.list()
}

async function load() {
  loading.value = true
  try {
    const r = await adminReservationApi.list({
      page: filter.page,
      size: filter.size,
      status: filter.status,
      roomId: filter.roomId,
      startFrom: filter.startFrom,
      startTo: filter.startTo
    })
    records.value = r.records
    total.value = r.total
  } finally {
    loading.value = false
  }
}

function resetFilter() {
  filter.status = undefined
  filter.roomId = undefined
  filter.startFrom = undefined
  filter.startTo = undefined
  filter.page = 1
  load()
}

function search() {
  filter.page = 1
  load()
}

async function doCancel(r: ReservationVo) {
  try {
    const { value } = await ElMessageBox.prompt(
      `请输入取消预约 #${r.id} 的原因`,
      '强制取消',
      { confirmButtonText: '提交', cancelButtonText: '关闭', inputPattern: /.+/, inputErrorMessage: '原因不能为空' }
    )
    await adminReservationApi.cancel(r.id, value)
    ElMessage.success('已取消')
    load()
  } catch {
    // 用户取消
  }
}

function fmt(s?: string): string {
  if (!s) return '-'
  return s.replace('T', ' ').slice(0, 19)
}

onMounted(async () => {
  await loadRooms()
  await load()
})
</script>

<template>
  <div>
    <div class="bar">
      <h2 class="page-title" style="margin: 0">预约订单</h2>
    </div>

    <el-form :model="filter" inline class="filter">
      <el-form-item label="状态">
        <el-select v-model="filter.status" placeholder="全部" clearable style="width: 140px">
          <el-option v-for="o in STATUS_OPTIONS" :key="o.value" :value="o.value" :label="o.label" />
        </el-select>
      </el-form-item>
      <el-form-item label="自习室">
        <el-select v-model="filter.roomId" placeholder="全部" clearable filterable style="width: 200px">
          <el-option v-for="r in rooms" :key="r.id" :value="r.id" :label="r.name" />
        </el-select>
      </el-form-item>
      <el-form-item label="开始时段">
        <el-date-picker
          v-model="filter.startFrom"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          format="YYYY-MM-DD HH:mm"
          placeholder="从"
          style="width: 200px"
        />
      </el-form-item>
      <el-form-item label="至">
        <el-date-picker
          v-model="filter.startTo"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          format="YYYY-MM-DD HH:mm"
          placeholder="到"
          style="width: 200px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="resetFilter">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="records" stripe border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="用户" min-width="120">
        <template #default="{ row }">
          <span>{{ row.userRealName || row.username || '-' }}</span>
          <small v-if="row.username && row.userRealName" style="color: #909399">（{{ row.username }}）</small>
        </template>
      </el-table-column>
      <el-table-column label="自习室 / 座位" min-width="160">
        <template #default="{ row }">{{ row.roomName }} · {{ row.seatNo }}</template>
      </el-table-column>
      <el-table-column label="时段" min-width="240">
        <template #default="{ row }">{{ fmt(row.startTime) }} → {{ fmt(row.endTime) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status).type">{{ statusTag(row.status).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="签到时间" min-width="170">
        <template #default="{ row }">{{ fmt(row.checkInTime) }}</template>
      </el-table-column>
      <el-table-column label="签退时间" min-width="170">
        <template #default="{ row }">{{ fmt(row.checkOutTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'BOOKED' || row.status === 'CHECKED_IN'"
            size="small"
            link
            type="danger"
            @click="doCancel(row)"
          >强制取消</el-button>
          <span v-else>-</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="filter.page"
        v-model:page-size="filter.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="load"
        @size-change="load"
      />
    </div>
  </div>
</template>

<style scoped>
.bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.filter { background: #fafafa; padding: 12px; border-radius: 4px; margin-bottom: 12px; }
.pager { margin-top: 16px; text-align: right; }
</style>
