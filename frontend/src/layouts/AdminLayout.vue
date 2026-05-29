<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useNotificationStore } from '@/stores/notification'
import {
  Monitor,
  School,
  Grid,
  Calendar,
  Warning,
  User,
  Tickets,
  Bell,
  Setting,
  DataAnalysis,
  Document
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const notificationStore = useNotificationStore()

const navItems = [
  { path: '/admin/dashboard', label: '控制台', icon: Monitor },
  { path: '/admin/rooms', label: '自习室', icon: School },
  { path: '/admin/seats', label: '座位', icon: Grid },
  { path: '/admin/reservations', label: '预约订单', icon: Calendar },
  { path: '/admin/reports', label: '举报处理', icon: Warning },
  { path: '/admin/users', label: '用户管理', icon: User },
  { path: '/admin/inspections', label: '巡检记录', icon: Tickets },
  { path: '/admin/announcements', label: '公告', icon: Bell },
  { path: '/admin/rules', label: '预约规则', icon: Setting },
  { path: '/admin/stats', label: '数据统计', icon: DataAnalysis },
  { path: '/admin/logs', label: '操作日志', icon: Document }
]

const activePath = computed(() => route.path)
const title = computed(() => (route.meta.title as string) || '管理后台')

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
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">书</span>
        <div class="brand-text">
          <div class="brand-title">校园自习室</div>
          <div class="brand-sub">管理后台</div>
        </div>
      </div>

      <nav class="nav">
        <RouterLink
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: activePath === item.path }"
        >
          <el-icon :size="18"><component :is="item.icon" /></el-icon>
          <span class="nav-label">{{ item.label }}</span>
        </RouterLink>
      </nav>

      <div v-if="userStore.user" class="user-block">
        <div class="user-name">{{ userStore.user.realName || userStore.user.username }}</div>
        <div class="user-meta">管理员</div>
      </div>
    </aside>

    <div class="main">
      <header class="topbar">
        <h1 class="topbar-title">{{ title }}</h1>
        <el-dropdown
          v-if="userStore.user"
          @command="(c: string) => c === 'logout' && logout()"
        >
          <span class="topbar-user">
            {{ userStore.user.realName || userStore.user.username }}
            <el-icon><ArrowDown /></el-icon>
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
    </div>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: var(--paper-bg);
}

.sidebar {
  width: 220px;
  flex-shrink: 0;
  background: var(--paper-sidebar);
  border-right: 1px solid var(--paper-border);
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
  overflow-y: auto;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 22px 20px 20px;
  border-bottom: 1px solid var(--paper-border);
  flex-shrink: 0;
}
.brand-mark {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  background: var(--accent);
  color: #fdfbf3;
  font-family: var(--font-serif);
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.brand-text { line-height: 1.2; }
.brand-title {
  font-family: var(--font-serif);
  font-size: 16px;
  font-weight: 600;
  color: var(--ink-1);
}
.brand-sub {
  font-size: 11px;
  color: var(--ink-3);
  letter-spacing: 0.1em;
  margin-top: 2px;
}

.nav {
  flex: 1;
  padding: 14px 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border-radius: 6px;
  color: var(--ink-2);
  font-size: 14px;
  transition: background 0.15s, color 0.15s;
}
.nav-item:hover {
  background: rgba(90, 122, 82, 0.08);
  color: var(--ink-1);
}
.nav-item.active {
  background: var(--accent-soft);
  color: var(--accent-active);
  font-weight: 500;
}
.nav-item.active::before {
  content: '';
  position: absolute;
  left: -12px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 18px;
  background: var(--accent);
  border-radius: 0 2px 2px 0;
}
.nav-label { flex: 1; }

.user-block {
  padding: 14px 20px;
  border-top: 1px solid var(--paper-border);
  flex-shrink: 0;
}
.user-name {
  font-size: 14px;
  color: var(--ink-1);
  font-weight: 500;
}
.user-meta {
  font-size: 12px;
  color: var(--ink-3);
  margin-top: 2px;
}

.main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.topbar {
  height: 56px;
  padding: 0 28px;
  background: var(--paper-bg);
  border-bottom: 1px solid var(--paper-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 5;
}
.topbar-title {
  font-family: var(--font-serif);
  font-size: 17px;
  font-weight: 600;
  margin: 0;
  color: var(--ink-1);
}
.topbar-user {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--ink-2);
  cursor: pointer;
}
.content {
  flex: 1;
}
</style>
