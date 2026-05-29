<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import {
  adminRoomApi,
  adminSeatApi,
  type BatchCreateSeatReq,
  type CreateSeatReq,
  type RoomVo,
  type SeatStatus,
  type SeatVo,
  type UpdateSeatReq
} from '@/api/room'

const rooms = ref<RoomVo[]>([])
const selectedRoomId = ref<number | null>(null)
const seats = ref<SeatVo[]>([])
const loading = ref(false)

const createDialog = ref(false)
const editDialog = ref(false)
const batchDialog = ref(false)
const faultDialog = ref(false)

const createForm = reactive<CreateSeatReq>({
  roomId: 0,
  seatNo: '',
  rowNo: 1,
  colNo: 1,
  feature: ''
})
const editingSeat = ref<SeatVo | null>(null)
const editForm = reactive<UpdateSeatReq>({})
const batchForm = reactive<BatchCreateSeatReq>({
  roomId: 0,
  rows: 5,
  cols: 6,
  prefix: 'A',
  feature: ''
})
const faultTarget = ref<SeatVo | null>(null)
const faultReason = ref('')

const createFormRef = ref<FormInstance | null>(null)
const batchFormRef = ref<FormInstance | null>(null)

const STATUS_OPTIONS: { value: SeatStatus; label: string }[] = [
  { value: 'AVAILABLE', label: '空闲' },
  { value: 'RESERVED', label: '已预约' },
  { value: 'OCCUPIED', label: '使用中' },
  { value: 'FAULT', label: '故障' }
]

const FEATURE_MAP: Record<string, string> = {
  window: '靠窗',
  socket: '有插座',
  quiet: '安静区',
  near_door: '靠门',
  near_ac: '靠空调',
  computer: '有电脑',
  power: '有电源',
  network: '有网口'
}

function parseFeatures(raw?: string): string[] {
  if (!raw) return []
  try {
    const arr = JSON.parse(raw)
    if (Array.isArray(arr)) return arr.map((f: string) => FEATURE_MAP[f] || f)
  } catch {}
  return [raw]
}

function statusTag(s: SeatStatus): { type: 'success' | 'warning' | 'info' | 'danger'; label: string } {
  switch (s) {
    case 'AVAILABLE': return { type: 'success', label: '空闲' }
    case 'RESERVED':  return { type: 'warning', label: '已预约' }
    case 'OCCUPIED':  return { type: 'info', label: '使用中' }
    case 'FAULT':     return { type: 'danger', label: '故障' }
  }
}

async function loadRooms() {
  rooms.value = await adminRoomApi.list()
  if (!selectedRoomId.value && rooms.value.length > 0) {
    selectedRoomId.value = rooms.value[0].id
  }
}

async function loadSeats() {
  if (!selectedRoomId.value) {
    seats.value = []
    return
  }
  loading.value = true
  try {
    seats.value = await adminSeatApi.listByRoom(selectedRoomId.value)
  } finally {
    loading.value = false
  }
}

watch(selectedRoomId, loadSeats)

function openCreate() {
  if (!selectedRoomId.value) {
    ElMessage.warning('请先选择自习室')
    return
  }
  Object.assign(createForm, {
    roomId: selectedRoomId.value,
    seatNo: '',
    rowNo: 1,
    colNo: 1,
    feature: ''
  })
  createDialog.value = true
}

function openBatch() {
  if (!selectedRoomId.value) {
    ElMessage.warning('请先选择自习室')
    return
  }
  Object.assign(batchForm, {
    roomId: selectedRoomId.value,
    rows: 5,
    cols: 6,
    prefix: 'A',
    feature: ''
  })
  batchDialog.value = true
}

function openEdit(s: SeatVo) {
  editingSeat.value = s
  Object.assign(editForm, {
    seatNo: s.seatNo,
    rowNo: s.rowNo,
    colNo: s.colNo,
    status: s.status,
    feature: s.feature
  })
  editDialog.value = true
}

function openFault(s: SeatVo) {
  faultTarget.value = s
  faultReason.value = ''
  faultDialog.value = true
}

async function submitCreate() {
  if (!createFormRef.value) return
  const valid = await createFormRef.value.validate().catch(() => false)
  if (!valid) return
  await adminSeatApi.create(createForm)
  ElMessage.success('已创建')
  createDialog.value = false
  loadSeats()
}

async function submitBatch() {
  if (!batchFormRef.value) return
  const valid = await batchFormRef.value.validate().catch(() => false)
  if (!valid) return
  const created = await adminSeatApi.batchCreate(batchForm)
  ElMessage.success(`本次新增 ${created} 个座位`)
  batchDialog.value = false
  loadSeats()
}

async function submitEdit() {
  if (!editingSeat.value) return
  await adminSeatApi.update(editingSeat.value.id, editForm)
  ElMessage.success('已更新')
  editDialog.value = false
  loadSeats()
}

async function submitFault() {
  if (!faultTarget.value) return
  if (!faultReason.value.trim()) {
    ElMessage.warning('请填写故障原因')
    return
  }
  await adminSeatApi.markFault(faultTarget.value.id, faultReason.value.trim())
  ElMessage.success('已标记故障')
  faultDialog.value = false
  loadSeats()
}

async function clearFault(s: SeatVo) {
  try {
    await ElMessageBox.confirm(`确认解除座位 ${s.seatNo} 的故障？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  await adminSeatApi.clearFault(s.id)
  ElMessage.success('已解除')
  loadSeats()
}

async function refresh(s: SeatVo) {
  await adminSeatApi.refresh(s.id)
  ElMessage.success('已重算状态')
  loadSeats()
}

async function remove(s: SeatVo) {
  try {
    await ElMessageBox.confirm(`确认删除座位 ${s.seatNo}？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  await adminSeatApi.remove(s.id)
  ElMessage.success('已删除')
  loadSeats()
}

onMounted(async () => {
  await loadRooms()
  await loadSeats()
})
</script>

<template>
  <div>
    <div class="bar">
      <h2 class="page-title" style="margin: 0">座位管理</h2>
      <div class="actions">
        <el-select
          v-model="selectedRoomId"
          placeholder="选择自习室"
          style="width: 200px"
          filterable
        >
          <el-option v-for="r in rooms" :key="r.id" :label="r.name" :value="r.id" />
        </el-select>
        <el-button :disabled="!selectedRoomId" @click="openCreate">新建座位</el-button>
        <el-button type="primary" :disabled="!selectedRoomId" @click="openBatch">批量生成</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="seats" stripe border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="seatNo" label="编号" width="100" />
      <el-table-column label="位置" width="120">
        <template #default="{ row }">{{ row.rowNo }} 排 {{ row.colNo }} 列</template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status).type">{{ statusTag(row.status).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="特性" min-width="200">
        <template #default="{ row }">
          <el-tag v-for="f in parseFeatures(row.feature)" :key="f" size="small" style="margin-right: 4px">{{ f }}</el-tag>
          <span v-if="!row.feature" style="color: #c0c4cc">—</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="320" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" link @click="refresh(row)">重算</el-button>
          <el-button v-if="row.status !== 'FAULT'" size="small" link type="warning" @click="openFault(row)">标记故障</el-button>
          <el-button v-else size="small" link type="success" @click="clearFault(row)">解除故障</el-button>
          <el-button size="small" link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新建座位 -->
    <el-dialog v-model="createDialog" title="新建座位" width="420px">
      <el-form ref="createFormRef" :model="createForm" label-width="80px"
        :rules="{
          seatNo: [{ required: true, message: '请输入编号', trigger: 'blur' }],
          rowNo: [{ required: true, message: '请输入行号', trigger: 'blur' }],
          colNo: [{ required: true, message: '请输入列号', trigger: 'blur' }]
        }">
        <el-form-item label="编号" prop="seatNo">
          <el-input v-model="createForm.seatNo" placeholder="如 A1-01" />
        </el-form-item>
        <el-form-item label="行号" prop="rowNo">
          <el-input-number v-model="createForm.rowNo" :min="1" />
        </el-form-item>
        <el-form-item label="列号" prop="colNo">
          <el-input-number v-model="createForm.colNo" :min="1" />
        </el-form-item>
        <el-form-item label="特性">
          <el-input v-model="createForm.feature" placeholder="如 window,socket" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialog = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">保存</el-button>
      </template>
    </el-dialog>

    <!-- 批量生成 -->
    <el-dialog v-model="batchDialog" title="批量生成座位" width="420px">
      <el-form ref="batchFormRef" :model="batchForm" label-width="80px"
        :rules="{
          rows: [{ required: true, message: '请输入行数', trigger: 'blur' }],
          cols: [{ required: true, message: '请输入列数', trigger: 'blur' }]
        }">
        <el-form-item label="行数" prop="rows">
          <el-input-number v-model="batchForm.rows" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="列数" prop="cols">
          <el-input-number v-model="batchForm.cols" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="编号前缀">
          <el-input v-model="batchForm.prefix" placeholder="A，将生成 A1-01 / A1-02 ..." />
        </el-form-item>
        <el-form-item label="特性">
          <el-input v-model="batchForm.feature" placeholder="如 window,socket" />
        </el-form-item>
        <el-alert type="info" :closable="false" show-icon>
          编号格式：<code>&lt;前缀&gt;&lt;行号&gt;-&lt;列号&gt;</code>。已存在编号会自动跳过。
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="batchDialog = false">取消</el-button>
        <el-button type="primary" @click="submitBatch">生成</el-button>
      </template>
    </el-dialog>

    <!-- 编辑座位 -->
    <el-dialog v-model="editDialog" title="编辑座位" width="420px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="编号">
          <el-input v-model="editForm.seatNo" />
        </el-form-item>
        <el-form-item label="行号">
          <el-input-number v-model="editForm.rowNo" :min="1" />
        </el-form-item>
        <el-form-item label="列号">
          <el-input-number v-model="editForm.colNo" :min="1" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status">
            <el-option v-for="o in STATUS_OPTIONS" :key="o.value" :value="o.value" :label="o.label" />
          </el-select>
        </el-form-item>
        <el-form-item label="特性">
          <el-input v-model="editForm.feature" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 标记故障 -->
    <el-dialog v-model="faultDialog" title="标记故障" width="420px">
      <p v-if="faultTarget" style="margin-top: 0">座位 <strong>{{ faultTarget.seatNo }}</strong></p>
      <el-input v-model="faultReason" type="textarea" :rows="3" placeholder="请描述故障情况" />
      <template #footer>
        <el-button @click="faultDialog = false">取消</el-button>
        <el-button type="warning" @click="submitFault">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.actions { display: flex; gap: 8px; align-items: center; }
</style>
