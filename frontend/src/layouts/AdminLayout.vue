<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
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

const menu = [
  { path: '/admin/dashboard', label: '控制台', icon: Monitor },
  { path: '/admin/rooms', label: '自习室管理', icon: School },
  { path: '/admin/seats', label: '座位管理', icon: Grid },
  { path: '/admin/reservations', label: '预约订单', icon: Calendar },
  { path: '/admin/reports', label: '举报处理', icon: Warning },
  { path: '/admin/users', label: '用户管理', icon: User },
  { path: '/admin/inspections', label: '巡检记录', icon: Tickets },
  { path: '/admin/announcements', label: '公告管理', icon: Bell },
  { path: '/admin/rules', label: '预约规则', icon: Setting },
  { path: '/admin/stats', label: '数据统计', icon: DataAnalysis },
  { path: '/admin/logs', label: '操作日志', icon: Document }
]

const activePath = computed(() => route.path)

function logout() {
  userStore.logout()
  router.replace('/auth/login')
}
</script>

<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="aside">
      <div class="brand">📚 校园自习室<br /><small>管理后台</small></div>
      <el-menu
        :default-active="activePath"
        :router="true"
        background-color="#001529"
        text-color="#cfd8dc"
        active-text-color="#fff"
        class="menu"
      >
        <el-menu-item v-for="m in menu" :key="m.path" :index="m.path">
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <span class="title">{{ (route.meta.title as string) || '管理后台' }}</span>
        <el-dropdown v-if="userStore.user" @command="(c) => c === 'logout' && logout()">
          <span class="user">{{ userStore.user.realName || userStore.user.username }}</span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main>
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.admin-layout { min-height: 100vh; }
.aside { background: #001529; color: #fff; }
.brand {
  padding: 18px 16px;
  font-weight: 600;
  font-size: 16px;
  border-bottom: 1px solid #1a2a3a;
}
.brand small { font-weight: normal; font-size: 12px; opacity: 0.7; }
.menu { border-right: none; }
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}
.title { font-weight: 600; font-size: 16px; }
.user { cursor: pointer; font-size: 13px; }
</style>
