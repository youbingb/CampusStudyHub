<script setup lang="ts">
import { computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'
import type { OccupancyVo } from '@/api/stats'

use([CanvasRenderer, BarChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent])

const props = defineProps<{ data: OccupancyVo[] }>()

const option = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  legend: { data: ['预约总数', '已签到/完成', '使用小时'] },
  grid: { left: 50, right: 60, bottom: 30, top: 40 },
  xAxis: { type: 'category', data: props.data.map((d) => d.roomName) },
  yAxis: [
    { type: 'value', name: '次数' },
    { type: 'value', name: '小时', position: 'right' }
  ],
  series: [
    {
      name: '预约总数',
      type: 'bar',
      data: props.data.map((d) => d.totalReservations),
      itemStyle: { color: '#409EFF' }
    },
    {
      name: '已签到/完成',
      type: 'bar',
      data: props.data.map((d) => d.completedReservations),
      itemStyle: { color: '#67C23A' }
    },
    {
      name: '使用小时',
      type: 'bar',
      yAxisIndex: 1,
      data: props.data.map((d) => Math.round((d.totalSeatHours || 0) * 10) / 10),
      itemStyle: { color: '#E6A23C' }
    }
  ]
}))
</script>

<template>
  <VChart class="chart" :option="option" autoresize />
</template>

<style scoped>
.chart { width: 100%; height: 320px; }
</style>
