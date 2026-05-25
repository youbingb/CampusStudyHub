<script setup lang="ts">
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()
</script>

<template>
  <div class="page">
    <h2 class="page-title">个人中心</h2>
    <el-descriptions v-if="userStore.user" :column="1" border>
      <el-descriptions-item label="用户名">{{ userStore.user.username }}</el-descriptions-item>
      <el-descriptions-item label="姓名">{{ userStore.user.realName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="学号">{{ userStore.user.studentNo || '-' }}</el-descriptions-item>
      <el-descriptions-item label="手机">{{ userStore.user.phone || '-' }}</el-descriptions-item>
      <el-descriptions-item label="邮箱">{{ userStore.user.email || '-' }}</el-descriptions-item>
      <el-descriptions-item label="角色">{{ userStore.user.role }}</el-descriptions-item>
      <el-descriptions-item label="信誉">{{ userStore.user.creditScore }}</el-descriptions-item>
    </el-descriptions>
    <div class="placeholder-card" style="margin-top: 16px">
      <small>本页由 Agent A 实现（Phase 1：编辑资料 + Phase 5：信誉变更记录 Tab）</small>
    </div>
  </div>
</template>
