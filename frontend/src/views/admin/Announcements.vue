<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  announcementApi,
  type AnnouncementPage,
  type AnnouncementVo,
  type CreateAnnouncementReq,
  type UpdateAnnouncementReq
} from '@/api/announcement'

const loading = ref(false)
const page = reactive({ current: 1, size: 10 })
const query = reactive<{ keyword: string; status: number | undefined }>({
  keyword: '',
  status: undefined
})
const data = ref<AnnouncementPage>({ total: 0, pages: 0, current: 1, size: 10, records: [] })

async function load() {
  loading.value = true
  try {
    data.value = await announcementApi.adminList({
      page: page.current,
      size: page.size,
      keyword: query.keyword?.trim() || undefined,
      status: query.status
    })
  } finally {
    loading.value = false
  }
}

function reset() {
  query.keyword = ''
  query.status = undefined
  page.current = 1
  load()
}

const dialogVisible = ref(false)
const editing = ref<AnnouncementVo | null>(null)
const submitting = ref(false)
const formRef = ref()
const form = reactive<CreateAnnouncementReq>({ title: '', content: '', publishNow: true })
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

function openCreate() {
  editing.value = null
  form.title = ''
  form.content = ''
  form.publishNow = true
  dialogVisible.value = true
}

function openEdit(row: AnnouncementVo) {
  editing.value = row
  form.title = row.title
  form.content = row.content
  form.publishNow = row.status === 1
  dialogVisible.value = true
}

async function submit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (editing.value) {
      const patch: UpdateAnnouncementReq = { title: form.title, content: form.content }
      await announcementApi.update(editing.value.id, patch)
      if (form.publishNow && editing.value.status !== 1) {
        await announcementApi.publish(editing.value.id)
      } else if (!form.publishNow && editing.value.status === 1) {
        await announcementApi.unpublish(editing.value.id)
      }
      ElMessage.success('已更新')
    } else {
      await announcementApi.create(form)
      ElMessage.success(form.publishNow ? '已发布' : '已存为草稿')
    }
    dialogVisible.value = false
    await load()
  } finally {
    submitting.value = false
  }
}

async function togglePublish(row: AnnouncementVo) {
  const action = row.status === 1 ? 'unpublish' : 'publish'
  await ElMessageBox.confirm(
    row.status === 1 ? `确定下架公告"${row.title}"吗？` : `确定发布公告"${row.title}"吗？`,
    '提示',
    { type: 'warning' }
  )
  await announcementApi[action](row.id)
  ElMessage.success(row.status === 1 ? '已下架' : '已发布')
  await load()
}

async function remove(row: AnnouncementVo) {
  await ElMessageBox.confirm(`确定删除公告"${row.title}"吗？`, '删除公告', { type: 'warning' })
  await announcementApi.delete(row.id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <div>
    <h2 class="page-title">公告管理</h2>

    <el-card shadow="never" class="filter-card">
      <el-form inline>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="标题关键字" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px">
            <el-option label="已发布" :value="1" />
            <el-option label="草稿" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="reset">重置</el-button>
          <el-button type="success" @click="openCreate">新增公告</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table v-loading="loading" :data="data.records" stripe style="margin-top: 12px">
      <el-table-column prop="id" label="编号" width="80" />
      <el-table-column prop="title" label="标题" show-overflow-tooltip min-width="220" />
      <el-table-column prop="publisherName" label="发布人" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="publishedAt" label="发布时间" width="170" />
      <el-table-column prop="updatedAt" label="更新时间" width="170" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link size="small" @click="openEdit(row)">编辑</el-button>
          <el-button
            link
            size="small"
            :type="row.status === 1 ? 'warning' : 'success'"
            @click="togglePublish(row)"
          >
            {{ row.status === 1 ? '下架' : '发布' }}
          </el-button>
          <el-button link size="small" type="danger" @click="remove(row)">删除</el-button>
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

    <el-dialog
      v-model="dialogVisible"
      :title="editing ? '编辑公告' : '新增公告'"
      width="640px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" label-position="left">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="支持纯文本"
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="form.publishNow">{{ editing ? '设为已发布' : '立即发布' }}</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.filter-card :deep(.el-card__body) { padding: 16px; }
.pager { display: flex; justify-content: flex-end; margin-top: 12px; }
</style>
