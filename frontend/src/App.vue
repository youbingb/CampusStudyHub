<script setup lang="ts">
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

onMounted(() => {
  // 启动时若 localStorage 有 token，尝试拉一次 /me 恢复登录态
  userStore.tryRestore()
})
</script>

<template>
  <RouterView />
</template>

<style>
#app {
  min-height: 100vh;
}
</style>
