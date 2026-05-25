<script setup lang="ts">
import { computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import type { UsageVo } from '@/api/stats'

use([CanvasRenderer, BarChart, GridComponent, TooltipComponent, LegendComponent])

const props = defineProps<{ data: UsageVo[] }>()

const option = computed(() => {
  const sorted = [...props.data].sort((a, b) => a.reservationCount - b.reservationCount)
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['预约数', '使用小时'] },
    grid: { left: 100, right: 40, bottom: 30, top: 40 },
    xAxis: { type: 'value' },
    yAxis: {
      type: 'category',
      data: sorted.map((d) => d.realName || d.username)
    },
    series: [
      {
        name: '预约数',
        type: 'bar',
        data: sorted.map((d) => d.reservationCount),
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '使用小时',
        type: 'bar',
        data: sorted.map((d) => Math.round((d.totalHours || 0) * 10) / 10),
        itemStyle: { color: '#67C23A' }
      }
    ]
  }
})
</script>

<template>
  <VChart class="chart" :option="option" autoresize />
</template>

<style scoped>
.chart { width: 100%; height: 360px; }
</style>
