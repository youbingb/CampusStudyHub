<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { userApi, type UpdateProfileReq, type ChangePasswordReq } from '@/api/user'

const userStore = useUserStore()

const profile = reactive<UpdateProfileReq>({
  realName: userStore.user?.realName || '',
  phone: userStore.user?.phone || '',
  email: userStore.user?.email || ''
})
const profileLoading = ref(false)
const profileFormRef = ref()
const profileRules = {
  phone: [{ pattern: /^$|^1\d{10}$/, message: '手机号格式错误', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式错误', trigger: 'blur' }]
}

async function saveProfile() {
  await profileFormRef.value?.validate()
  profileLoading.value = true
  try {
    const u = await userApi.updateMe(profile)
    userStore.user = u
    ElMessage.success('资料已更新')
  } finally {
    profileLoading.value = false
  }
}

const pwd = reactive<ChangePasswordReq & { confirm: string }>({
  oldPassword: '',
  newPassword: '',
  confirm: ''
})
const pwdLoading = ref(false)
const pwdFormRef = ref()
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 64, message: '密码长度 6-64', trigger: 'blur' }
  ],
  confirm: [
    { required: true, message: '请再输入一次', trigger: 'blur' },
    {
      validator: (_r: unknown, v: string, cb: (err?: Error) => void) =>
        v === pwd.newPassword ? cb() : cb(new Error('两次输入不一致')),
      trigger: 'blur'
    }
  ]
}

async function changePassword() {
  await pwdFormRef.value?.validate()
  pwdLoading.value = true
  try {
    await userApi.changePassword({ oldPassword: pwd.oldPassword, newPassword: pwd.newPassword })
    ElMessage.success('密码已更新，请重新登录')
    userStore.logout()
    location.href = '/auth/login'
  } finally {
    pwdLoading.value = false
  }
}
</script>

<template>
  <div class="page">
    <h2 class="page-title">个人中心</h2>

    <el-card v-if="userStore.user" shadow="never" class="card">
      <template #header>基本信息</template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="用户名">{{ userStore.user.username }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ userStore.user.studentNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag size="small" :type="userStore.user.role === 'ADMIN' ? 'danger' : 'info'">
            {{ userStore.user.role === 'ADMIN' ? '管理员' : '学生' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="信誉分">
          <el-tag :type="userStore.user.creditScore >= 80 ? 'success' : 'warning'">
            {{ userStore.user.creditScore }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" class="card">
      <template #header>编辑资料</template>
      <el-form
        ref="profileFormRef"
        :model="profile"
        :rules="profileRules"
        label-width="80px"
        @submit.prevent="saveProfile"
      >
        <el-form-item label="姓名">
          <el-input v-model="profile.realName" />
        </el-form-item>
        <el-form-item label="手机" prop="phone">
          <el-input v-model="profile.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profile.email" />
        </el-form-item>
        <el-button type="primary" :loading="profileLoading" @click="saveProfile">保存资料</el-button>
      </el-form>
    </el-card>

    <el-card shadow="never" class="card">
      <template #header>修改密码</template>
      <el-form
        ref="pwdFormRef"
        :model="pwd"
        :rules="pwdRules"
        label-width="80px"
        @submit.prevent="changePassword"
      >
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="pwd.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwd.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirm">
          <el-input v-model="pwd.confirm" type="password" show-password />
        </el-form-item>
        <el-button type="warning" :loading="pwdLoading" @click="changePassword">提交修改</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.page { padding: 16px; }
.page-title { margin: 0 0 12px; font-size: 18px; }
.card { margin-bottom: 12px; }
</style>
