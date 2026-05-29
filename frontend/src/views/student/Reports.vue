<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  reportApi,
  REPORT_TYPES,
  type CreateReportReq,
  type ReportPage,
  type ReportStatus,
  type ReportVo
} from '@/api/report'

const activeTab = ref<'submit' | 'mine'>('submit')

// 提交表单
const formRef = ref()
const submitting = ref(false)
const form = reactive({
  type: '',
  description: '',
  targetUserText: '',
  reservationId: undefined as number | undefined,
  seatId: undefined as number | undefined,
  evidenceUrl: ''
})
const rules = {
  type: [{ required: true, message: '请选择举报类型', trigger: 'change' }],
  description: [{ required: true, message: '请填写举报描述', trigger: 'blur' }]
}

async function submit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const payload: CreateReportReq = {
      type: form.type,
      description: form.description,
      targetUserId: form.targetUserText ? Number(form.targetUserText) || undefined : undefined,
      reservationId: form.reservationId || undefined,
      seatId: form.seatId || undefined,
      evidenceUrl: form.evidenceUrl?.trim() || undefined
    }
    const id = await reportApi.create(payload)
    ElMessage.success(`举报已提交（#${id}），等待管理员处理`)
    formRef.value?.resetFields()
    activeTab.value = 'mine'
    await loadMine()
  } finally {
    submitting.value = false
  }
}

// 我的举报列表
const loading = ref(false)
const page = reactive({ current: 1, size: 10 })
const query = reactive<{ status: ReportStatus | '' }>({ status: '' })
const data = ref<ReportPage>({ total: 0, pages: 0, current: 1, size: 10, records: [] })

async function loadMine() {
  loading.value = true
  try {
    data.value = await reportApi.mine({
      page: page.current,
      size: page.size,
      status: query.status || undefined
    })
  } finally {
    loading.value = false
  }
}

function statusTag(s: ReportStatus) {
  return s === 'PENDING'
    ? { type: 'info', label: '待处理' }
    : s === 'PROCESSING'
      ? { type: 'warning', label: '处理中' }
      : s === 'RESOLVED'
        ? { type: 'success', label: '已核实' }
        : { type: 'danger', label: '已驳回' }
}

async function cancel(row: ReportVo) {
  await ElMessageBox.confirm(`确定撤销举报 #${row.id} 吗？`, '撤销举报', { type: 'warning' })
  await reportApi.cancel(row.id)
  ElMessage.success('已撤销')
  await loadMine()
}

onMounted(loadMine)
</script>

<template>
  <div class="page">
    <h2 class="page-title">违规举报</h2>

    <el-tabs v-model="activeTab" @tab-change="(v: any) => v === 'mine' && loadMine()">
      <el-tab-pane label="提交举报" name="submit">
        <el-card class="form-card" shadow="never">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="84px" label-position="left">
            <el-form-item label="类型" prop="type">
              <el-select v-model="form.type" placeholder="选择举报类型" style="width: 100%">
                <el-option v-for="t in REPORT_TYPES" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
            <el-form-item label="描述" prop="description">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="4"
                placeholder="发生时间 / 地点 / 经过，越具体越有利核实"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
            <el-form-item label="被举报人">
              <el-input
                v-model="form.targetUserText"
                placeholder="姓名/学号（可选）"
                maxlength="50"
              />
              <span class="hint">若知道对方姓名或学号可填，留空表示匿名场景</span>
            </el-form-item>
            <el-form-item label="预约 ID">
              <el-input
                v-model.number="form.reservationId"
                type="number"
                placeholder="可选"
              />
            </el-form-item>
            <el-form-item label="座位 ID">
              <el-input
                v-model.number="form.seatId"
                type="number"
                placeholder="可选"
              />
            </el-form-item>
            <el-form-item label="证据链接">
              <el-input v-model="form.evidenceUrl" placeholder="可选：照片/视频 URL" maxlength="500" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="submit">提交举报</el-button>
              <el-button @click="formRef?.resetFields()">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="我的举报" name="mine">
        <div class="filter">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 160px" @change="loadMine">
            <el-option label="待处理" value="PENDING" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已核实" value="RESOLVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
          <el-button @click="loadMine">刷新</el-button>
        </div>

        <el-table v-loading="loading" :data="data.records" stripe>
          <el-table-column prop="id" label="编号" width="70" />
          <el-table-column prop="type" label="类型" width="100" />
          <el-table-column prop="description" label="描述" show-overflow-tooltip />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusTag(row.status).type" size="small">{{ statusTag(row.status).label }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="result" label="处理结果" show-overflow-tooltip />
          <el-table-column prop="createdAt" label="提交时间" width="170" />
          <el-table-column label="操作" width="90">
            <template #default="{ row }">
              <el-button v-if="row.status === 'PENDING'" type="danger" size="small" link @click="cancel(row)">
                撤销
              </el-button>
              <span v-else class="muted">—</span>
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
            @current-change="loadMine"
            @size-change="loadMine"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.form-card { max-width: 640px; }
.hint { color: #909399; font-size: 12px; margin-left: 12px; }
.filter { display: flex; gap: 8px; margin-bottom: 12px; }
.pager { display: flex; justify-content: flex-end; margin-top: 12px; }
.muted { color: #c0c4cc; }
</style>
