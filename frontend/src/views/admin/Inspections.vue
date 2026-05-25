<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  inspectionApi,
  type CreateInspectionReq,
  type InspectionPage,
  type InspectionVo
} from '@/api/inspection'
import { adminRoomApi, adminSeatApi, type RoomVo, type SeatVo } from '@/api/room'

const loading = ref(false)
const page = reactive({ current: 1, size: 10 })
const query = reactive<{
  roomId: number | undefined
  range: [string, string] | []
}>({ roomId: undefined, range: [] })

const data = ref<InspectionPage>({ total: 0, pages: 0, current: 1, size: 10, records: [] })
const rooms = ref<RoomVo[]>([])

async function loadRooms() {
  rooms.value = await adminRoomApi.list()
}

function roomName(id: number) {
  return rooms.value.find((r) => r.id === id)?.name || `#${id}`
}

async function load() {
  loading.value = true
  try {
    data.value = await inspectionApi.list({
      page: page.current,
      size: page.size,
      roomId: query.roomId,
      from: query.range?.[0] || undefined,
      to: query.range?.[1] || undefined
    })
  } finally {
    loading.value = false
  }
}

function reset() {
  query.roomId = undefined
  query.range = []
  page.current = 1
  load()
}

// 新增 dialog
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive<CreateInspectionReq>({ roomId: 0, content: '', issues: [] })
const seatOptions = ref<SeatVo[]>([])
const seatLoading = ref(false)
const rules = { roomId: [{ required: true, message: '请选择自习室', trigger: 'change' }] }

function openCreate() {
  form.roomId = 0
  form.content = ''
  form.issues = []
  seatOptions.value = []
  dialogVisible.value = true
}

watch(
  () => form.roomId,
  async (rid) => {
    form.issues = []
    if (!rid) {
      seatOptions.value = []
      return
    }
    seatLoading.value = true
    try {
      seatOptions.value = await adminSeatApi.listByRoom(rid)
    } finally {
      seatLoading.value = false
    }
  }
)

async function submit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    await inspectionApi.create({
      roomId: form.roomId,
      content: form.content?.trim() || undefined,
      issues: form.issues && form.issues.length ? form.issues : undefined
    })
    ElMessage.success('已记录巡检' + (form.issues?.length ? `，已标记 ${form.issues.length} 个故障座位` : ''))
    dialogVisible.value = false
    await load()
  } finally {
    submitting.value = false
  }
}

async function remove(row: InspectionVo) {
  await ElMessageBox.confirm(`确定删除巡检记录 #${row.id} 吗？`, '提示', { type: 'warning' })
  await inspectionApi.delete(row.id)
  ElMessage.success('已删除')
  await load()
}

// 详情 dialog
const detailVisible = ref(false)
const detail = ref<InspectionVo | null>(null)

function seatLabel(id: number) {
  const s = seatOptions.value.find((x) => x.id === id)
  return s ? `${s.seatNo} (第${s.rowNo}排${s.colNo}列)` : `#${id}`
}

async function openDetail(row: InspectionVo) {
  detail.value = row
  if (row.roomId) {
    try {
      seatOptions.value = await adminSeatApi.listByRoom(row.roomId)
    } catch { /* ignore */ }
  }
  detailVisible.value = true
}

onMounted(async () => {
  await loadRooms()
  await load()
})
</script>

<template>
  <div>
    <h2 class="page-title">巡检记录</h2>

    <el-card shadow="never" class="filter-card">
      <el-form inline>
        <el-form-item label="自习室">
          <el-select v-model="query.roomId" placeholder="全部" clearable style="width: 200px">
            <el-option v-for="r in rooms" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="query.range"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="reset">重置</el-button>
          <el-button type="success" @click="openCreate">新增巡检</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table v-loading="loading" :data="data.records" stripe style="margin-top: 12px">
      <el-table-column prop="id" label="编号" width="80" />
      <el-table-column label="自习室" width="180">
        <template #default="{ row }">
          {{ row.roomName || roomName(row.roomId) }}
        </template>
      </el-table-column>
      <el-table-column prop="inspectorName" label="巡检人" width="120" />
      <el-table-column prop="content" label="备注" show-overflow-tooltip />
      <el-table-column label="故障座位" width="120">
        <template #default="{ row }">
          <el-tag :type="row.issues?.length ? 'danger' : 'info'" size="small">
            {{ row.issues?.length || 0 }} 个
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="时间" width="170" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link @click="openDetail(row)">详情</el-button>
          <el-button size="small" link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="data.total"
        :page-sizes="[10, 20, 50]"
        background
        layout="total, sizes, prev, pager, next"
        @current-change="load"
        @size-change="load"
      />
    </div>

    <el-dialog v-model="dialogVisible" title="新增巡检" width="540px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="84px" label-position="left">
        <el-form-item label="自习室" prop="roomId">
          <el-select v-model="form.roomId" placeholder="选择自习室" style="width: 100%">
            <el-option v-for="r in rooms" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.content" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="故障座位">
          <el-select
            v-model="form.issues"
            multiple
            filterable
            collapse-tags
            collapse-tags-tooltip
            :loading="seatLoading"
            placeholder="选择有故障的座位（可多选）"
            style="width: 100%"
            :disabled="!form.roomId"
          >
            <el-option
              v-for="s in seatOptions"
              :key="s.id"
              :label="`${s.seatNo} (第${s.rowNo}排${s.colNo}列)`"
              :value="s.id"
            />
          </el-select>
          <div class="hint">选中的座位将被标记为 FAULT 并实时广播</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="巡检详情" width="520px">
      <template v-if="detail">
        <p><b>编号：</b>#{{ detail.id }}</p>
        <p><b>自习室：</b>{{ detail.roomName || roomName(detail.roomId) }}</p>
        <p><b>巡检人：</b>{{ detail.inspectorName || `#${detail.inspectorId}` }}</p>
        <p><b>时间：</b>{{ detail.createdAt }}</p>
        <p><b>备注：</b>{{ detail.content || '—' }}</p>
        <p>
          <b>故障座位：</b>
          <span v-if="!detail.issues?.length">无</span>
          <template v-else>
            <el-tag v-for="id in detail.issues" :key="id" type="danger" size="small" style="margin: 2px 4px 2px 0">
              {{ seatLabel(id) }}
            </el-tag>
          </template>
        </p>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.filter-card :deep(.el-card__body) { padding: 16px; }
.pager { display: flex; justify-content: flex-end; margin-top: 12px; }
.hint { color: #909399; font-size: 12px; margin-top: 4px; }
</style>
