<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi, type RegisterReq } from '@/api/auth'

const router = useRouter()
const form = reactive<RegisterReq>({
  username: '',
  password: '',
  realName: '',
  studentNo: '',
  phone: '',
  email: ''
})
const loading = ref(false)
const formRef = ref()

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ]
}

async function submit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await authApi.register(form)
    ElMessage.success('注册成功，请登录')
    router.replace('/auth/login')
  } catch {
    /* handled by interceptor */
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <div class="card">
      <div class="brand-row">
        <span class="brand-mark">书</span>
        <div class="brand-text">
          <div class="brand-title">注册账号</div>
          <div class="brand-sub">学生注册</div>
        </div>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="username"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="学号"><el-input v-model="form.studentNo" /></el-form-item>
        <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-button type="primary" :loading="loading" style="width: 100%" @click="submit">注 册</el-button>
      </el-form>
      <div class="footer">已有账号？<RouterLink to="/auth/login">去登录</RouterLink></div>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--paper-bg);
  background-image:
    radial-gradient(circle at 18% 22%, rgba(90, 122, 82, 0.06), transparent 40%),
    radial-gradient(circle at 82% 78%, rgba(176, 74, 58, 0.04), transparent 45%);
  padding: 24px 0;
}
.card {
  width: 420px;
  padding: 32px 36px;
  background: var(--paper-card);
  border: 1px solid var(--paper-border);
  border-radius: var(--radius-2);
  box-shadow: var(--shadow-2);
}
.brand-row {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: center;
  margin-bottom: 20px;
}
.brand-mark {
  width: 38px;
  height: 38px;
  border-radius: 6px;
  background: var(--accent);
  color: #fdfbf3;
  font-family: var(--font-serif);
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.brand-text { line-height: 1.2; text-align: left; }
.brand-title {
  font-family: var(--font-serif);
  font-size: 17px;
  font-weight: 600;
  color: var(--ink-1);
}
.brand-sub {
  font-size: 11px;
  color: var(--ink-3);
  letter-spacing: 0.1em;
  margin-top: 2px;
}
.footer { text-align: center; margin-top: 14px; font-size: 13px; color: var(--ink-2); }
.footer a { color: var(--accent); }
</style>
