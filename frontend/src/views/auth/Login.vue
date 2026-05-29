<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { authApi, type LoginReq } from '@/api/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const form = reactive<LoginReq>({ username: '', password: '' })
const loading = ref(false)
const formRef = ref()

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function submit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    const resp = await authApi.login(form)
    userStore.setToken(resp.token)
    userStore.user = resp.user
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) ||
      (resp.user.role === 'ADMIN' ? '/admin/dashboard' : '/student/home')
    router.replace(redirect)
  } catch (e: any) {
    ElMessage.error(e?.message || '用户名或密码错误')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="brand-row">
        <span class="brand-mark">书</span>
        <div class="brand-text">
          <div class="brand-title">校园自习室</div>
          <div class="brand-sub">Study Hub</div>
        </div>
      </div>
      <p class="hint">学生与管理员统一登录入口</p>

      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="submit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" autofocus />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password @keyup.enter="submit" />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" style="width: 100%" @click="submit">
          登 录
        </el-button>
      </el-form>

      <div class="footer">
        还没有账号？<RouterLink to="/auth/register">立即注册</RouterLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--paper-bg);
  background-image:
    radial-gradient(circle at 18% 22%, rgba(90, 122, 82, 0.06), transparent 40%),
    radial-gradient(circle at 82% 78%, rgba(176, 74, 58, 0.04), transparent 45%);
}
.login-card {
  width: 380px;
  padding: 40px 36px 32px;
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
  margin-bottom: 4px;
}
.brand-mark {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  background: var(--accent);
  color: #fdfbf3;
  font-family: var(--font-serif);
  font-size: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.brand-text { line-height: 1.2; text-align: left; }
.brand-title {
  font-family: var(--font-serif);
  font-size: 18px;
  font-weight: 600;
  color: var(--ink-1);
}
.brand-sub {
  font-size: 11px;
  color: var(--ink-3);
  letter-spacing: 0.12em;
  margin-top: 2px;
}
.hint {
  color: var(--ink-3);
  font-size: 13px;
  text-align: center;
  margin: 6px 0 24px;
}
.footer {
  text-align: center;
  margin-top: 18px;
  font-size: 13px;
  color: var(--ink-2);
}
.footer a { color: var(--accent); }
</style>
