<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { logApi, type OperationLogPage } from '@/api/log'

const MODULES = ['举报', '巡检', '公告', '预约规则', '统计', '用户', '自习室', '预约']

const loading = ref(false)
const page = reactive({ current: 1, size: 20 })
const query = reactive<{
  module: string
  action: string
  username: string
  range: [string, string] | []
}>({ module: '', action: '', username: '', range: [] })
const data = ref<OperationLogPage>({ total: 0, pages: 0, current: 1, size: 20, records: [] })

async function load() {
  loading.value = true
  try {
    data.value = await logApi.list({
      page: page.current,
      size: page.size,
      module: query.module || undefined,
      action: query.action?.trim() || undefined,
      username: query.username?.trim() || undefined,
      from: query.range?.[0] || undefined,
      to: query.range?.[1] || undefined
    })
  } finally {
    loading.value = false
  }
}

function reset() {
  query.module = ''
  query.action = ''
  query.username = ''
  query.range = []
  page.current = 1
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <h2 class="page-title">操作日志</h2>

    <el-card shadow="never" class="filter-card">
      <el-form inline>
        <el-form-item label="模块">
          <el-select v-model="query.module" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="m in MODULES" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item label="动作">
          <el-input v-model="query.action" placeholder="如：新增/删除" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="query.username" placeholder="模糊匹配" clearable style="width: 160px" />
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
        </el-form-item>
      </el-form>
    </el-card>

    <el-table v-loading="loading" :data="data.records" stripe style="margin-top: 12px">
      <el-table-column prop="id" label="编号" width="80" />
      <el-table-column prop="createdAt" label="时间" width="170" />
      <el-table-column label="用户" width="160">
        <template #default="{ row }">
          <span v-if="row.username">{{ row.username }}</span>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="module" label="模块" width="120" />
      <el-table-column prop="action" label="动作" width="140" />
      <el-table-column prop="targetId" label="目标 ID" width="120" />
      <el-table-column prop="ip" label="IP" width="140" />
      <el-table-column prop="ua" label="User-Agent" show-overflow-tooltip />
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="data.total"
        :page-sizes="[20, 50, 100]"
        background
        layout="total, sizes, prev, pager, next"
        @current-change="load"
        @size-change="load"
      />
    </div>
  </div>
</template>

<style scoped>
.filter-card :deep(.el-card__body) { padding: 16px; }
.pager { display: flex; justify-content: flex-end; margin-top: 12px; }
.muted { color: #c0c4cc; }
</style>
