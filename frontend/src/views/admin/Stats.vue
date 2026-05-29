<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  statsApi,
  type FaultVo,
  type OccupancyVo,
  type PopularHourVo,
  type StatsQuery,
  type UsageVo,
  type ViolationVo
} from '@/api/stats'
import OccupancyChart from '@/components/charts/OccupancyChart.vue'
import UsageChart from '@/components/charts/UsageChart.vue'
import PopularHoursChart from '@/components/charts/PopularHoursChart.vue'
import ViolationChart from '@/components/charts/ViolationChart.vue'
import FaultChart from '@/components/charts/FaultChart.vue'

const loading = ref(false)
const range = ref<[string, string] | []>([])
const topN = ref(10)

const occupancy = ref<OccupancyVo[]>([])
const usage = ref<UsageVo[]>([])
const popularHours = ref<PopularHourVo[]>([])
const violations = ref<ViolationVo[]>([])
const faults = ref<FaultVo[]>([])

function buildQuery(): StatsQuery {
  return {
    from: range.value?.[0] || undefined,
    to: range.value?.[1] || undefined,
    topN: topN.value
  }
}

async function loadAll() {
  loading.value = true
  try {
    const q = buildQuery()
    const [o, u, p, v, f] = await Promise.all([
      statsApi.occupancy(q),
      statsApi.usage(q),
      statsApi.popularHours(q),
      statsApi.violations(q),
      statsApi.faults(q)
    ])
    occupancy.value = o
    usage.value = u
    popularHours.value = p
    violations.value = v
    faults.value = f
  } finally {
    loading.value = false
  }
}

const summary = reactive({
  totalRooms: 0,
  totalReservations: 0,
  totalUsers: 0,
  totalFaults: 0
})

function updateSummary() {
  summary.totalRooms = occupancy.value.length
  summary.totalReservations = occupancy.value.reduce((s, d) => s + (d.totalReservations || 0), 0)
  summary.totalUsers = usage.value.length
  summary.totalFaults = faults.value.reduce((s, d) => s + (d.totalFaults || 0), 0)
}

function downloadExport() {
  const url = statsApi.exportUrl(buildQuery())
  const token = localStorage.getItem('csh-token') || ''
  // 走 fetch + blob，确保带上 token
  ElMessage.info('正在准备下载…')
  fetch(url, { headers: token ? { Authorization: `Bearer ${token}` } : {} })
    .then((resp) => {
      if (!resp.ok) throw new Error(`HTTP ${resp.status}`)
      return resp.blob()
    })
    .then((blob) => {
      const a = document.createElement('a')
      a.href = URL.createObjectURL(blob)
      a.download = `campus-stats-${new Date().toISOString().slice(0, 10)}.xlsx`
      a.click()
      URL.revokeObjectURL(a.href)
      ElMessage.success('已下载')
    })
    .catch((e) => ElMessage.error('下载失败: ' + (e.message || e)))
}

async function refresh() {
  await loadAll()
  updateSummary()
}

onMounted(refresh)
</script>

<template>
  <div>
    <div class="header">
      <h2 class="page-title" style="margin: 0">数据统计</h2>
      <div class="controls">
        <el-date-picker
          v-model="range"
          type="daterange"
          value-format="YYYY-MM-DD"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 280px"
        />
        <el-input-number v-model="topN" :min="3" :max="50" />
        <el-button type="primary" :loading="loading" @click="refresh">刷新</el-button>
        <el-button type="success" @click="downloadExport">导出 Excel</el-button>
      </div>
    </div>

    <el-row :gutter="12" class="kpi">
      <el-col :span="6">
        <el-card shadow="never"><div class="kpi-label">自习室</div><div class="kpi-value">{{ summary.totalRooms }}</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never"><div class="kpi-label">预约总数</div><div class="kpi-value">{{ summary.totalReservations }}</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never"><div class="kpi-label">活跃用户</div><div class="kpi-value">{{ summary.totalUsers }}</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never"><div class="kpi-label">故障次数</div><div class="kpi-value">{{ summary.totalFaults }}</div></el-card>
      </el-col>
    </el-row>

    <el-empty
      v-if="!loading && !occupancy.length && !usage.length && !popularHours.length && !violations.length && !faults.length"
      description="暂无统计数据"
    />

    <el-row v-else :gutter="12" v-loading="loading">
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header><span>各自习室上座情况</span></template>
          <OccupancyChart :data="occupancy" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header><span>热门时段（0-23 时）</span></template>
          <PopularHoursChart :data="popularHours" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header><span>用户使用 Top {{ topN }}</span></template>
          <UsageChart :data="usage" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header><span>违规扣分 Top {{ topN }}</span></template>
          <ViolationChart :data="violations" />
        </el-card>
      </el-col>
      <el-col :span="24">
        <el-card shadow="never" class="chart-card">
          <template #header><span>自习室故障汇总</span></template>
          <FaultChart :data="faults" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.controls { display: flex; gap: 8px; align-items: center; }
.kpi { margin-bottom: 14px; }
.kpi-label { color: #909399; font-size: 13px; }
.kpi-value { font-size: 24px; font-weight: 600; margin-top: 6px; }
.chart-card { margin-bottom: 12px; }
.chart-card :deep(.el-card__body) { padding: 8px 12px 12px; }
</style>
