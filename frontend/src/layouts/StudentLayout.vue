<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useNotificationStore } from '@/stores/notification'
import {
  HomeFilled,
  School,
  Calendar,
  Star,
  ChatDotRound,
  User
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const notificationStore = useNotificationStore()

const tabs = [
  { path: '/student/home', label: '首页', icon: HomeFilled, key: 'home' },
  { path: '/student/rooms', label: '自习室', icon: School, key: 'rooms' },
  { path: '/student/reservations', label: '预约', icon: Calendar, key: 'reservations' },
  { path: '/student/recommend', label: '推荐', icon: Star, key: 'recommend' },
  { path: '/student/notifications', label: '消息', icon: ChatDotRound, key: 'notifications' },
  { path: '/student/profile', label: '我的', icon: User, key: 'profile' }
]

const title = computed(() => (route.meta.title as string) || '校园自习室')
const activeTab = computed(() => '/' + route.path.split('/').slice(1, 3).join('/'))

function logout() {
  userStore.logout()
  router.replace('/auth/login')
}

onMounted(async () => {
  if (userStore.isLoggedIn) {
    await notificationStore.refreshUnread()
    await notificationStore.ensureSubscribed()
  }
})
</script>

<template>
  <div class="student-layout">
    <header class="topbar">
      <span class="title">{{ title }}</span>
      <el-dropdown v-if="userStore.user" @command="(c: string) => c === 'logout' && logout()">
        <span class="user">
          {{ userStore.user.realName || userStore.user.username }}
          <el-tag size="small" type="info">信誉 {{ userStore.user.creditScore }}</el-tag>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </header>

    <main class="content">
      <RouterView />
    </main>

    <nav class="tabbar">
      <RouterLink v-for="t in tabs" :key="t.path" :to="t.path" class="tab" :class="{ active: activeTab === t.path }">
        <el-badge
          v-if="t.key === 'notifications'"
          :value="notificationStore.unreadCount"
          :hidden="notificationStore.unreadCount === 0"
          :max="99"
        >
          <el-icon :size="20"><component :is="t.icon" /></el-icon>
        </el-badge>
        <el-icon v-else :size="20"><component :is="t.icon" /></el-icon>
        <span>{{ t.label }}</span>
      </RouterLink>
    </nav>
  </div>
</template>

<style scoped>
.student-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}
.topbar {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}
.title { font-weight: 600; font-size: 16px; }
.user { display: inline-flex; align-items: center; gap: 8px; cursor: pointer; font-size: 13px; }
.content { flex: 1; padding-bottom: 64px; }
.tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  background: #fff;
  border-top: 1px solid #ebeef5;
  z-index: 10;
}
.tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 8px 0;
  font-size: 11px;
  color: #909399;
}
.tab.active { color: var(--el-color-primary); }
</style>
