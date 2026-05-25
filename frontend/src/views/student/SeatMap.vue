<script setup lang="ts">
import { useRoute } from 'vue-router'
const route = useRoute()
</script>

<template>
  <div class="page">
    <h2 class="page-title">座位平面图 · 房间 #{{ route.params.id }}</h2>
    <div class="placeholder-card">
      <small>本页由 Agent B 实现（Phase 2：SeatMap.vue 平面图 + WebSocket 实时刷色 + 选座预约 Drawer）</small>
    </div>
  </div>
</template>
