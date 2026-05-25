<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { roomApi, type RoomVo } from '@/api/room'

const router = useRouter()
const rooms = ref<RoomVo[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    rooms.value = await roomApi.list()
  } catch (e) {
    // 拦截器已 toast
  } finally {
    loading.value = false
  }
}

function enterRoom(r: RoomVo) {
  if (r.status !== 1) {
    ElMessage.warning('该自习室当前关闭')
    return
  }
  router.push(`/student/rooms/${r.id}`)
}

function availabilityType(r: RoomVo): 'success' | 'warning' | 'danger' {
  const total = r.totalSeats || 0
  const free = r.availableSeats || 0
  if (total === 0) return 'warning'
  const ratio = free / total
  if (ratio > 0.5) return 'success'
  if (ratio > 0.1) return 'warning'
  return 'danger'
}

onMounted(load)
</script>

<template>
  <div class="page">
    <h2 class="page-title">自习室</h2>

    <el-empty v-if="!loading && rooms.length === 0" description="暂无开放自习室" />

    <div v-loading="loading" class="grid">
      <el-card
        v-for="r in rooms"
        :key="r.id"
        shadow="hover"
        class="room-card"
        @click="enterRoom(r)"
      >
        <div class="head">
          <span class="name">{{ r.name }}</span>
          <el-tag :type="availabilityType(r)" size="small">
            {{ r.availableSeats ?? 0 }} / {{ r.totalSeats ?? 0 }} 可用
          </el-tag>
        </div>
        <div class="meta">
          <div v-if="r.location"><el-icon><Location /></el-icon> {{ r.location }}</div>
          <div><el-icon><Clock /></el-icon> {{ r.openTime }} – {{ r.closeTime }}</div>
          <div v-if="r.description" class="desc">{{ r.description }}</div>
        </div>
        <div class="footer">
          <el-button type="primary" size="small" :icon="Right">进入选座</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script lang="ts">
import { Location, Clock, Right } from '@element-plus/icons-vue'
export default { components: { Location, Clock, Right } }
</script>

<style scoped>
.grid {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
}
.room-card { cursor: pointer; }
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.name { font-size: 16px; font-weight: 600; }
.meta { font-size: 13px; color: #606266; display: flex; flex-direction: column; gap: 4px; }
.meta .el-icon { vertical-align: -3px; margin-right: 4px; }
.desc { color: #909399; font-size: 12px; margin-top: 4px; }
.footer { margin-top: 12px; text-align: right; }
</style>
