<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import {
  adminRoomApi,
  type CreateRoomReq,
  type RoomVo,
  type UpdateRoomReq
} from '@/api/room'

const rooms = ref<RoomVo[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive<CreateRoomReq & { status?: number }>({
  name: '',
  location: '',
  capacity: 0,
  openTime: '07:00',
  closeTime: '22:30',
  description: '',
  status: 1
})
const formRef = ref<FormInstance | null>(null)
const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    rooms.value = await adminRoomApi.list()
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, {
    name: '',
    location: '',
    capacity: 0,
    openTime: '07:00',
    closeTime: '22:30',
    description: '',
    status: 1
  })
  dialogVisible.value = true
}

function openEdit(r: RoomVo) {
  editingId.value = r.id
  Object.assign(form, {
    name: r.name,
    location: r.location || '',
    capacity: r.capacity || 0,
    openTime: r.openTime || '07:00',
    closeTime: r.closeTime || '22:30',
    description: r.description || '',
    status: r.status
  })
  dialogVisible.value = true
}

async function submit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (editingId.value == null) {
      await adminRoomApi.create({
        name: form.name,
        location: form.location,
        capacity: form.capacity,
        openTime: form.openTime,
        closeTime: form.closeTime,
        description: form.description
      })
      ElMessage.success('新建成功')
    } else {
      const data: UpdateRoomReq = {
        name: form.name,
        location: form.location,
        capacity: form.capacity,
        openTime: form.openTime,
        closeTime: form.closeTime,
        status: form.status,
        description: form.description
      }
      await adminRoomApi.update(editingId.value, data)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    load()
  } catch {
    // 拦截器已 toast
  }
}

async function toggleStatus(r: RoomVo) {
  try {
    await adminRoomApi.update(r.id, { status: r.status === 1 ? 0 : 1 })
    ElMessage.success(r.status === 1 ? '已关闭' : '已开放')
    load()
  } catch {
    // 拦截器已 toast
  }
}

async function remove(r: RoomVo) {
  try {
    await ElMessageBox.confirm(`确认删除自习室 "${r.name}"？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  await adminRoomApi.remove(r.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <div class="bar">
      <h2 class="page-title" style="margin: 0">自习室管理</h2>
      <el-button type="primary" @click="openCreate">新建自习室</el-button>
    </div>

    <el-table v-loading="loading" :data="rooms" stripe border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" min-width="120" />
      <el-table-column prop="location" label="位置" min-width="140" />
      <el-table-column label="开放时段" width="160">
        <template #default="{ row }">{{ row.openTime }} – {{ row.closeTime }}</template>
      </el-table-column>
      <el-table-column label="座位" width="120">
        <template #default="{ row }">
          <span>{{ row.availableSeats }}/{{ row.totalSeats }} 可用</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '开放' : '关闭' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="说明" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '关闭' : '开放' }}
          </el-button>
          <el-button size="small" link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId == null ? '新建自习室' : '编辑自习室'"
      width="500px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="如 主楼一层" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="form.location" placeholder="如 主楼 101" />
        </el-form-item>
        <el-form-item label="容量">
          <el-input-number v-model="form.capacity" :min="0" :max="500" />
        </el-form-item>
        <el-form-item label="开放时间">
          <el-input v-model="form.openTime" placeholder="HH:mm" style="width: 120px" />
          <span style="margin: 0 8px">至</span>
          <el-input v-model="form.closeTime" placeholder="HH:mm" style="width: 120px" />
        </el-form-item>
        <el-form-item v-if="editingId != null" label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="开放" inactive-text="关闭" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
</style>
