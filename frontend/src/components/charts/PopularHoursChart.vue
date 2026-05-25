<script setup lang="ts">
import { computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, MarkAreaComponent } from 'echarts/components'
import type { PopularHourVo } from '@/api/stats'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, MarkAreaComponent])

const props = defineProps<{ data: PopularHourVo[] }>()

const option = computed(() => {
  const map = new Map(props.data.map((d) => [d.hour, d.reservationCount]))
  const hours = Array.from({ length: 24 }, (_, i) => i)
  const counts = hours.map((h) => map.get(h) || 0)
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 30, bottom: 30, top: 30 },
    xAxis: { type: 'category', data: hours.map((h) => `${h}:00`) },
    yAxis: { type: 'value', name: '预约数' },
    series: [
      {
        name: '预约数',
        type: 'line',
        smooth: true,
        areaStyle: {},
        data: counts,
        itemStyle: { color: '#409EFF' },
        markArea: {
          silent: true,
          itemStyle: { color: 'rgba(64, 158, 255, 0.06)' },
          data: [[{ xAxis: '8:00' }, { xAxis: '11:00' }], [{ xAxis: '14:00' }, { xAxis: '17:00' }], [{ xAxis: '19:00' }, { xAxis: '21:00' }]]
        }
      }
    ]
  }
})
</script>

<template>
  <VChart class="chart" :option="option" autoresize />
</template>

<style scoped>
.chart { width: 100%; height: 320px; }
</style>
