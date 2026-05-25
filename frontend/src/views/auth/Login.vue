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
  } catch {
    // request 拦截器已经提示了错误
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="brand">📚 校园自习室</h1>
      <p class="hint">学生 / 管理员统一登录入口</p>

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

      <div class="dev-tip">
        <strong>测试账号</strong>：admin / stu01～stu05，密码均 <code>123456</code><br />
        （登录接口由 Agent A 在 Phase 1 实现，当前调用会 404）
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 380px;
  padding: 36px 32px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
}
.brand { margin: 0; font-size: 22px; text-align: center; }
.hint { color: #909399; font-size: 13px; text-align: center; margin: 6px 0 24px; }
.footer { text-align: center; margin-top: 16px; font-size: 13px; color: #606266; }
.footer a { color: var(--el-color-primary); }
.dev-tip {
  margin-top: 20px;
  padding: 10px 12px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}
.dev-tip code { background: #ebeef5; padding: 0 4px; border-radius: 3px; }
</style>
