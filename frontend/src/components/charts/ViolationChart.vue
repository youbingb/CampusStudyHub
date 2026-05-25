<script setup lang="ts">
import { computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import type { ViolationVo } from '@/api/stats'

use([CanvasRenderer, BarChart, GridComponent, TooltipComponent, LegendComponent])

const props = defineProps<{ data: ViolationVo[] }>()

const option = computed(() => {
  const sorted = [...props.data].sort((a, b) => a.totalDeduction - b.totalDeduction)
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['扣分合计', '违规次数'] },
    grid: { left: 100, right: 40, bottom: 30, top: 40 },
    xAxis: { type: 'value' },
    yAxis: {
      type: 'category',
      data: sorted.map((d) => d.realName || d.username)
    },
    series: [
      {
        name: '扣分合计',
        type: 'bar',
        data: sorted.map((d) => d.totalDeduction),
        itemStyle: { color: '#F56C6C' }
      },
      {
        name: '违规次数',
        type: 'bar',
        data: sorted.map((d) => d.violationCount),
        itemStyle: { color: '#E6A23C' }
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
