<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { announcementApi, type AnnouncementVo } from '@/api/announcement'
import {
  Calendar,
  School,
  Star,
  ChatDotRound,
  Warning,
  User
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const announcements = ref<AnnouncementVo[]>([])
const loading = ref(false)

const shortcuts = [
  { label: '自习室', icon: School, path: '/student/rooms', color: '#5a7a52' },
  { label: '我的预约', icon: Calendar, path: '/student/reservations', color: '#e6a23c' },
  { label: '智能推荐', icon: Star, path: '/student/recommend', color: '#409eff' },
  { label: '消息通知', icon: ChatDotRound, path: '/student/notifications', color: '#909399' },
  { label: '违规举报', icon: Warning, path: '/student/reports', color: '#f56c6c' },
  { label: '个人中心', icon: User, path: '/student/profile', color: '#67c23a' }
]

async function loadAnnouncements() {
  loading.value = true
  try {
    const data = await announcementApi.active(5)
    announcements.value = Array.isArray(data) ? data : []
  } catch {
    announcements.value = []
  } finally {
    loading.value = false
  }
}

function fmtTime(s?: string): string {
  if (!s) return ''
  return s.replace('T', ' ').slice(0, 16)
}

onMounted(loadAnnouncements)
</script>

<template>
  <div class="home-page">
    <div class="welcome">
      <h2>你好，{{ userStore.user?.realName || userStore.user?.username || '同学' }}</h2>
      <p class="sub">欢迎回到校园自习室，祝你学习愉快</p>
    </div>

    <div class="shortcuts">
      <div
        v-for="item in shortcuts"
        :key="item.path"
        class="shortcut-card"
        @click="router.push(item.path)"
      >
        <div class="icon-wrap" :style="{ background: item.color + '18', color: item.color }">
          <el-icon :size="24"><component :is="item.icon" /></el-icon>
        </div>
        <span class="label">{{ item.label }}</span>
      </div>
    </div>

    <div class="section">
      <h3 class="section-title">最新公告</h3>
      <div v-loading="loading">
        <el-empty v-if="!loading && announcements.length === 0" description="暂无公告" :image-size="80" />
        <el-carousel
          v-else-if="announcements.length > 1"
          :interval="5000"
          height="120px"
          indicator-position="outside"
          class="carousel"
        >
          <el-carousel-item v-for="a in announcements" :key="a.id">
            <div class="announcement-card">
              <div class="announcement-title">{{ a.title }}</div>
              <div class="announcement-content">{{ a.content }}</div>
              <div class="announcement-time">{{ fmtTime(a.publishedAt) }}</div>
            </div>
          </el-carousel-item>
        </el-carousel>
        <div v-else-if="announcements.length === 1" class="announcement-card">
          <div class="announcement-title">{{ announcements[0].title }}</div>
          <div class="announcement-content">{{ announcements[0].content }}</div>
          <div class="announcement-time">{{ fmtTime(announcements[0].publishedAt) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  padding: 24px 28px;
  max-width: 800px;
}
.welcome {
  margin-bottom: 24px;
}
.welcome h2 {
  font-family: var(--font-serif);
  font-size: 22px;
  font-weight: 600;
  color: var(--ink-1);
  margin: 0;
}
.welcome .sub {
  color: var(--ink-3);
  font-size: 14px;
  margin: 6px 0 0;
}

.shortcuts {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 28px;
}
.shortcut-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--paper-bg);
  border: 1px solid var(--paper-border);
  border-radius: 8px;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.15s;
}
.shortcut-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}
.icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.label {
  font-size: 14px;
  font-weight: 500;
  color: var(--ink-1);
}

.section {
  margin-bottom: 20px;
}
.section-title {
  font-family: var(--font-serif);
  font-size: 16px;
  font-weight: 600;
  color: var(--ink-1);
  margin: 0 0 12px;
}

.carousel {
  border-radius: 8px;
  overflow: hidden;
}
.announcement-card {
  padding: 16px 20px;
  background: var(--paper-bg);
  border: 1px solid var(--paper-border);
  border-radius: 8px;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.announcement-title {
  font-weight: 600;
  font-size: 15px;
  color: var(--ink-1);
  margin-bottom: 8px;
}
.announcement-content {
  font-size: 13px;
  color: var(--ink-2);
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.announcement-time {
  font-size: 12px;
  color: var(--ink-3);
  margin-top: 8px;
}
</style>
