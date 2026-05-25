<script setup lang="ts">
import { computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import type { FaultVo } from '@/api/stats'

use([CanvasRenderer, BarChart, GridComponent, TooltipComponent, LegendComponent])

const props = defineProps<{ data: FaultVo[] }>()

const option = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  legend: { data: ['故障总数', '未修复'] },
  grid: { left: 40, right: 30, bottom: 30, top: 40 },
  xAxis: { type: 'category', data: props.data.map((d) => d.roomName) },
  yAxis: { type: 'value', name: '次数' },
  series: [
    {
      name: '故障总数',
      type: 'bar',
      data: props.data.map((d) => d.totalFaults),
      itemStyle: { color: '#909399' }
    },
    {
      name: '未修复',
      type: 'bar',
      data: props.data.map((d) => d.openFaults),
      itemStyle: { color: '#F56C6C' }
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
