import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/student/home' },
  {
    path: '/auth',
    children: [
      { path: 'login', name: 'Login', component: () => import('@/views/auth/Login.vue'), meta: { public: true } },
      { path: 'register', name: 'Register', component: () => import('@/views/auth/Register.vue'), meta: { public: true } }
    ]
  },
  {
    path: '/student',
    component: () => import('@/layouts/StudentLayout.vue'),
    redirect: '/student/home',
    children: [
      { path: 'home', name: 'StudentHome', component: () => import('@/views/student/Home.vue'), meta: { title: '首页' } },
      { path: 'rooms', name: 'StudentRooms', component: () => import('@/views/student/Rooms.vue'), meta: { title: '自习室' } },
      { path: 'rooms/:id', name: 'StudentSeatMap', component: () => import('@/views/student/SeatMap.vue'), meta: { title: '选座' } },
      { path: 'reservations', name: 'StudentMyReservations', component: () => import('@/views/student/MyReservations.vue'), meta: { title: '我的预约' } },
      { path: 'recommend', name: 'StudentRecommend', component: () => import('@/views/student/Recommend.vue'), meta: { title: '推荐' } },
      { path: 'reports', name: 'StudentReports', component: () => import('@/views/student/Reports.vue'), meta: { title: '我的举报' } },
      { path: 'notifications', name: 'StudentNotifications', component: () => import('@/views/student/Notifications.vue'), meta: { title: '消息' } },
      { path: 'profile', name: 'StudentProfile', component: () => import('@/views/student/Profile.vue'), meta: { title: '个人中心' } }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { requiresAdmin: true },
    children: [
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('@/views/admin/Dashboard.vue'), meta: { title: '控制台' } },
      { path: 'rooms', name: 'AdminRooms', component: () => import('@/views/admin/Rooms.vue'), meta: { title: '自习室管理' } },
      { path: 'seats', name: 'AdminSeats', component: () => import('@/views/admin/Seats.vue'), meta: { title: '座位管理' } },
      { path: 'reservations', name: 'AdminReservations', component: () => import('@/views/admin/Reservations.vue'), meta: { title: '预约订单' } },
      { path: 'reports', name: 'AdminReports', component: () => import('@/views/admin/Reports.vue'), meta: { title: '举报处理' } },
      { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/Users.vue'), meta: { title: '用户管理' } },
      { path: 'inspections', name: 'AdminInspections', component: () => import('@/views/admin/Inspections.vue'), meta: { title: '巡检记录' } },
      { path: 'announcements', name: 'AdminAnnouncements', component: () => import('@/views/admin/Announcements.vue'), meta: { title: '公告管理' } },
      { path: 'rules', name: 'AdminRules', component: () => import('@/views/admin/Rules.vue'), meta: { title: '预约规则' } },
      { path: 'stats', name: 'AdminStats', component: () => import('@/views/admin/Stats.vue'), meta: { title: '数据统计' } },
      { path: 'logs', name: 'AdminLogs', component: () => import('@/views/admin/Logs.vue'), meta: { title: '操作日志' } }
    ]
  },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/NotFound.vue'), meta: { public: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  if (to.meta.public) return true

  const userStore = useUserStore()

  // 启动时若有 token 但还没拉到用户信息，先恢复一次
  if (userStore.token && !userStore.user) {
    await userStore.tryRestore()
  }

  if (!userStore.isLoggedIn) {
    return { path: '/auth/login', query: { redirect: to.fullPath } }
  }

  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    return { path: '/student/home' }
  }

  return true
})

export default router
