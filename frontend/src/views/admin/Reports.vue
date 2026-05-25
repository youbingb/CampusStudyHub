<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  reportApi,
  REPORT_TYPES,
  type ProcessReportReq,
  type ReportPage,
  type ReportStatus,
  type ReportVo
} from '@/api/report'

const loading = ref(false)
const page = reactive({ current: 1, size: 10 })
const query = reactive<{
  status: ReportStatus | ''
  type: string
  keyword: string
}>({ status: '', type: '', keyword: '' })
const data = ref<ReportPage>({ total: 0, pages: 0, current: 1, size: 10, records: [] })

async function load() {
  loading.value = true
  try {
    data.value = await reportApi.adminList({
      page: page.current,
      size: page.size,
      status: query.status || undefined,
      type: query.type || undefined,
      keyword: query.keyword?.trim() || undefined
    })
  } finally {
    loading.value = false
  }
}

function reset() {
  query.status = ''
  query.type = ''
  query.keyword = ''
  page.current = 1
  load()
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

// 处理 dialog
const dialogVisible = ref(false)
const current = ref<ReportVo | null>(null)
const processForm = reactive<ProcessReportReq>({
  action: 'APPROVE',
  result: '',
  creditDelta: -5,
  creditReason: ''
})
const processing = ref(false)

function openProcess(row: ReportVo) {
  current.value = row
  processForm.action = 'APPROVE'
  processForm.result = ''
  processForm.creditDelta = row.targetUserId ? -5 : 0
  processForm.creditReason = ''
  dialogVisible.value = true
}

async function submitProcess() {
  if (!current.value) return
  processing.value = true
  try {
    await reportApi.process(current.value.id, {
      action: processForm.action,
      result: processForm.result?.trim() || undefined,
      creditDelta:
        processForm.action === 'APPROVE' && current.value.targetUserId
          ? processForm.creditDelta || 0
          : 0,
      creditReason: processForm.creditReason?.trim() || undefined
    })
    ElMessage.success('处理完成')
    dialogVisible.value = false
    await load()
  } finally {
    processing.value = false
  }
}

onMounted(load)
</script>

<template>
  <div>
    <h2 class="page-title">举报处理</h2>

    <el-card shadow="never" class="filter-card">
      <el-form inline>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px">
            <el-option label="待处理" value="PENDING" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已核实" value="RESOLVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.type" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="t in REPORT_TYPES" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="描述关键字" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table v-loading="loading" :data="data.records" stripe style="margin-top: 12px">
      <el-table-column prop="id" label="编号" width="70" />
      <el-table-column prop="type" label="类型" width="100" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column label="举报人" width="120">
        <template #default="{ row }">
          {{ row.reporterName || `#${row.reporterId}` }}
        </template>
      </el-table-column>
      <el-table-column label="被举报人" width="120">
        <template #default="{ row }">
          <span v-if="row.targetUserId">{{ row.targetUserName || `#${row.targetUserId}` }}</span>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status).type" size="small">{{ statusTag(row.status).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="提交时间" width="170" />
      <el-table-column prop="result" label="处理结果" show-overflow-tooltip />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'PENDING' || row.status === 'PROCESSING'"
            type="primary"
            size="small"
            link
            @click="openProcess(row)"
          >处理</el-button>
          <span v-else class="muted">已完结</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="data.total"
        :page-sizes="[10, 20, 50, 100]"
        background
        layout="total, sizes, prev, pager, next"
        @current-change="load"
        @size-change="load"
      />
    </div>

    <el-dialog v-model="dialogVisible" title="处理举报" width="520px">
      <template v-if="current">
        <div class="meta">
          <p><b>编号：</b>#{{ current.id }}</p>
          <p><b>类型：</b>{{ current.type }}</p>
          <p><b>举报人：</b>{{ current.reporterName || `#${current.reporterId}` }}</p>
          <p v-if="current.targetUserId">
            <b>被举报人：</b>{{ current.targetUserName || `#${current.targetUserId}` }}
          </p>
          <p><b>描述：</b>{{ current.description }}</p>
          <p v-if="current.evidenceUrl"><b>证据：</b>{{ current.evidenceUrl }}</p>
        </div>

        <el-divider />

        <el-form :model="processForm" label-width="80px" label-position="left">
          <el-form-item label="处理结果">
            <el-radio-group v-model="processForm.action">
              <el-radio-button value="APPROVE">核实通过</el-radio-button>
              <el-radio-button value="REJECT">驳回</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="说明">
            <el-input
              v-model="processForm.result"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="处理意见，会发送给举报人"
            />
          </el-form-item>
          <el-form-item
            v-if="processForm.action === 'APPROVE' && current.targetUserId"
            label="信誉变化"
          >
            <el-input-number v-model="processForm.creditDelta" :min="-50" :max="50" />
            <span class="hint">负数扣分，0 不动；不存在被举报人时本项忽略</span>
          </el-form-item>
          <el-form-item
            v-if="processForm.action === 'APPROVE' && current.targetUserId && (processForm.creditDelta || 0) !== 0"
            label="信誉原因"
          >
            <el-input v-model="processForm.creditReason" placeholder="默认 '举报核实: 类型'，可自定义" maxlength="200" />
          </el-form-item>
        </el-form>
      </template>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="processing" @click="submitProcess">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.filter-card :deep(.el-card__body) { padding: 16px; }
.pager { display: flex; justify-content: flex-end; margin-top: 12px; }
.muted { color: #c0c4cc; }
.meta p { margin: 4px 0; font-size: 13px; }
.hint { color: #909399; font-size: 12px; margin-left: 12px; }
</style>
