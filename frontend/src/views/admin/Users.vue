<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { userApi, type UserQuery } from '@/api/user'
import type { UserInfo, Role } from '@/stores/user'

const list = ref<UserInfo[]>([])
const total = ref(0)
const loading = ref(false)
const query = reactive<UserQuery>({ page: 1, size: 10, keyword: '', role: undefined, status: undefined })

async function load() {
  loading.value = true
  try {
    const page = await userApi.adminList(query)
    list.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

async function toggleStatus(u: UserInfo) {
  const next: 0 | 1 = u.status === 1 ? 0 : 1
  const action = next === 1 ? '启用' : '禁用'
  await ElMessageBox.confirm(`确认${action}用户 ${u.username}?`, '提示', {
    type: 'warning'
  })
  await userApi.adminUpdateStatus(u.id, next)
  u.status = next
  ElMessage.success(`已${action}`)
}

const creditDialog = reactive({ visible: false, user: null as UserInfo | null, delta: 0, reason: '' })

function openCredit(u: UserInfo) {
  creditDialog.visible = true
  creditDialog.user = u
  creditDialog.delta = 0
  creditDialog.reason = ''
}

async function submitCredit() {
  if (!creditDialog.user) return
  if (!creditDialog.delta) {
    ElMessage.warning('请输入 delta（正数加分，负数扣分）')
    return
  }
  if (!creditDialog.reason.trim()) {
    ElMessage.warning('请填写原因')
    return
  }
  const newScore = await userApi.adminAdjustCredit(creditDialog.user.id, {
    delta: creditDialog.delta,
    reason: creditDialog.reason.trim()
  })
  creditDialog.user.creditScore = newScore
  creditDialog.visible = false
  ElMessage.success(`已调整，当前信誉 ${newScore}`)
}

function search() {
  query.page = 1
  load()
}

function changePage(p: number) {
  query.page = p
  load()
}

const roleOptions: { label: string; value: Role }[] = [
  { label: '学生', value: 'STUDENT' },
  { label: '管理员', value: 'ADMIN' }
]
const statusOptions: { label: string; value: 0 | 1 }[] = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
]

onMounted(load)
</script>

<template>
  <div>
    <h2 class="page-title">用户管理</h2>

    <el-card shadow="never" class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索用户名 / 姓名 / 学号 / 手机"
        style="width: 260px"
        clearable
        @keyup.enter="search"
      />
      <el-select v-model="query.role" placeholder="角色" clearable style="width: 120px; margin-left: 8px">
        <el-option v-for="o in roleOptions" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px; margin-left: 8px">
        <el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>
      <el-button type="primary" style="margin-left: 8px" @click="search">查询</el-button>
    </el-card>

    <el-table v-loading="loading" :data="list" stripe border>
      <el-table-column prop="id" label="ID" width="64" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="phone" label="手机" width="130" />
      <el-table-column label="角色" width="90">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small">
            {{ row.role === 'ADMIN' ? '管理员' : '学生' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="信誉" width="90">
        <template #default="{ row }">
          <el-tag :type="row.creditScore >= 80 ? 'success' : 'warning'" size="small">
            {{ row.creditScore }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" @click="openCredit(row)">调信誉</el-button>
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="changePage"
        @size-change="search"
      />
    </div>

    <el-dialog v-model="creditDialog.visible" title="手动调整信誉" width="420px">
      <div v-if="creditDialog.user" class="cd-head">
        目标用户：<b>{{ creditDialog.user.username }}</b>
        <span class="muted">（当前 {{ creditDialog.user.creditScore }} 分）</span>
      </div>
      <el-form label-width="64px" style="margin-top: 12px">
        <el-form-item label="delta">
          <el-input-number v-model="creditDialog.delta" :min="-100" :max="100" />
          <span class="muted" style="margin-left: 8px">正数加分，负数扣分</span>
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="creditDialog.reason" placeholder="必填，会进入 credit_log 和站内通知" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="creditDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitCredit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-title { margin: 0 0 12px; font-size: 18px; }
.toolbar { margin-bottom: 12px; }
.toolbar :deep(.el-card__body) { display: flex; align-items: center; padding: 8px 12px; }
.pager { margin-top: 12px; display: flex; justify-content: flex-end; }
.cd-head { font-size: 14px; }
.muted { color: #909399; font-size: 12px; }
</style>
