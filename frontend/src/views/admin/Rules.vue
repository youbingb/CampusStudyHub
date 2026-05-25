<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ruleApi, type RuleVo, type UpdateRuleReq } from '@/api/rule'

const loading = ref(false)
const submitting = ref(false)
const formRef = ref()
const updatedAt = ref<string>('')

const form = reactive<UpdateRuleReq>({
  maxDaily: 2,
  maxAdvanceDays: 3,
  minCredit: 60,
  checkInGraceMin: 15,
  maxDurationHours: 4,
  noShowCreditPenalty: 5
})

const rules = {
  maxDaily: [{ required: true, message: '请输入每日预约数上限', trigger: 'blur' }],
  maxAdvanceDays: [{ required: true, message: '请输入提前预约天数上限', trigger: 'blur' }],
  minCredit: [{ required: true, message: '请输入信誉门槛', trigger: 'blur' }],
  checkInGraceMin: [{ required: true, message: '请输入签到宽限分钟数', trigger: 'blur' }],
  maxDurationHours: [{ required: true, message: '请输入单次最长小时数', trigger: 'blur' }],
  noShowCreditPenalty: [{ required: true, message: '请输入违约扣分', trigger: 'blur' }]
}

function applyVo(vo: RuleVo) {
  form.maxDaily = vo.maxDaily
  form.maxAdvanceDays = vo.maxAdvanceDays
  form.minCredit = vo.minCredit
  form.checkInGraceMin = vo.checkInGraceMin
  form.maxDurationHours = vo.maxDurationHours
  form.noShowCreditPenalty = vo.noShowCreditPenalty
  updatedAt.value = vo.updatedAt
}

async function load() {
  loading.value = true
  try {
    const vo = await ruleApi.admin()
    applyVo(vo)
  } finally {
    loading.value = false
  }
}

async function save() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const vo = await ruleApi.update(form)
    applyVo(vo)
    ElMessage.success('已保存')
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <div>
    <h2 class="page-title">预约规则</h2>

    <el-card v-loading="loading" shadow="never" class="form-card">
      <template #header>
        <div class="card-header">
          <span>全局预约规则</span>
          <small v-if="updatedAt" class="muted">最近更新 {{ updatedAt }}</small>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="160px"
        label-position="left"
      >
        <el-form-item label="每日预约数上限" prop="maxDaily">
          <el-input-number v-model="form.maxDaily" :min="1" :max="20" />
          <span class="hint">同一学生每日最多可创建几条预约</span>
        </el-form-item>
        <el-form-item label="提前预约天数" prop="maxAdvanceDays">
          <el-input-number v-model="form.maxAdvanceDays" :min="0" :max="30" />
          <span class="hint">最多可提前多少天预约（0 = 仅当天）</span>
        </el-form-item>
        <el-form-item label="信誉门槛" prop="minCredit">
          <el-input-number v-model="form.minCredit" :min="0" :max="100" />
          <span class="hint">信誉低于此值禁止预约</span>
        </el-form-item>
        <el-form-item label="签到宽限分钟" prop="checkInGraceMin">
          <el-input-number v-model="form.checkInGraceMin" :min="0" :max="120" />
          <span class="hint">预约开始后超过此分钟数仍未签到判定违约</span>
        </el-form-item>
        <el-form-item label="单次最长小时" prop="maxDurationHours">
          <el-input-number v-model="form.maxDurationHours" :min="1" :max="12" />
          <span class="hint">单条预约最长持续小时数</span>
        </el-form-item>
        <el-form-item label="违约扣分" prop="noShowCreditPenalty">
          <el-input-number v-model="form.noShowCreditPenalty" :min="0" :max="50" />
          <span class="hint">未签到自动释放时扣减的信誉分</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="save">保存</el-button>
          <el-button @click="load">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.form-card { max-width: 720px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.muted { color: #909399; font-size: 12px; }
.hint { color: #909399; font-size: 12px; margin-left: 12px; }
</style>
