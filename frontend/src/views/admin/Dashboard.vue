<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { statsApi } from '@/api/stats'
import { reportApi } from '@/api/report'
import { adminRoomApi } from '@/api/room'
import {
  School,
  Calendar,
  Warning,
  User
} from '@element-plus/icons-vue'

const loading = ref(false)
const summary = ref({
  totalRooms: 0,
  todayReservations: 0,
  pendingReports: 0,
  totalUsers: 0
})

async function load() {
  loading.value = true
  try {
    const [rooms, occupancy, usage, reports] = await Promise.all([
      adminRoomApi.list(),
      statsApi.occupancy({}),
      statsApi.usage({}),
      reportApi.adminList({ status: 'PENDING', page: 1, size: 1 })
    ])
    summary.value.totalRooms = rooms.length
    summary.value.todayReservations = occupancy.reduce((s, d) => s + d.totalReservations, 0)
    summary.value.totalUsers = usage.length
    summary.value.pendingReports = reports.total
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading">
    <h2 class="page-title">控制台</h2>

    <el-row :gutter="16" class="kpi-row">
      <el-col :span="6">
        <el-card shadow="never" class="kpi-card">
          <div class="kpi-inner">
            <div class="kpi-icon" style="background: #5a7a5218; color: #5a7a52">
              <el-icon :size="28"><School /></el-icon>
            </div>
            <div class="kpi-body">
              <div class="kpi-label">自习室总数</div>
              <div class="kpi-value">{{ summary.totalRooms }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="kpi-card">
          <div class="kpi-inner">
            <div class="kpi-icon" style="background: #e6a23c18; color: #e6a23c">
              <el-icon :size="28"><Calendar /></el-icon>
            </div>
            <div class="kpi-body">
              <div class="kpi-label">预约总数</div>
              <div class="kpi-value">{{ summary.todayReservations }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="kpi-card">
          <div class="kpi-inner">
            <div class="kpi-icon" style="background: #f56c6c18; color: #f56c6c">
              <el-icon :size="28"><Warning /></el-icon>
            </div>
            <div class="kpi-body">
              <div class="kpi-label">待处理举报</div>
              <div class="kpi-value">{{ summary.pendingReports }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="kpi-card">
          <div class="kpi-inner">
            <div class="kpi-icon" style="background: #409eff18; color: #409eff">
              <el-icon :size="28"><User /></el-icon>
            </div>
            <div class="kpi-body">
              <div class="kpi-label">活跃用户</div>
              <div class="kpi-value">{{ summary.totalUsers }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="quick-links">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header><span>快捷入口</span></template>
          <div class="links">
            <el-button type="primary" link @click="$router.push('/admin/rooms')">自习室管理</el-button>
            <el-button type="primary" link @click="$router.push('/admin/reservations')">预约订单</el-button>
            <el-button type="primary" link @click="$router.push('/admin/reports')">举报处理</el-button>
            <el-button type="primary" link @click="$router.push('/admin/users')">用户管理</el-button>
            <el-button type="primary" link @click="$router.push('/admin/stats')">数据统计</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.page-title {
  margin: 0 0 16px;
  font-family: var(--font-serif);
  font-size: 20px;
  font-weight: 600;
  color: var(--ink-1);
}
.kpi-row {
  margin-bottom: 16px;
}
.kpi-card {
  cursor: default;
}
.kpi-inner {
  display: flex;
  align-items: center;
  gap: 14px;
}
.kpi-icon {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.kpi-label {
  font-size: 13px;
  color: var(--ink-3);
}
.kpi-value {
  font-size: 26px;
  font-weight: 700;
  color: var(--ink-1);
  margin-top: 4px;
}
.links {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}
</style>