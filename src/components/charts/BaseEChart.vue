<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { init, type ECharts, type EChartsOption } from '../../lib/echarts'

const props = withDefaults(
  defineProps<{
    option: EChartsOption
    height?: string
  }>(),
  {
    height: '300px',
  },
)
const emit = defineEmits<{
  (event: 'chart-click', params: unknown): void
}>()

const chartRef = ref<HTMLDivElement>()
let chartInstance: ECharts | undefined
let resizeObserver: ResizeObserver | undefined

const renderChart = () => {
  if (!chartRef.value) {
    return
  }

  if (!chartInstance) {
    chartInstance = init(chartRef.value)
  }

  chartInstance.setOption(props.option, true)
  chartInstance.off('click')
  chartInstance.on('click', (params) => {
    emit('chart-click', params)
  })
}

const handleResize = () => {
  chartInstance?.resize()
}

watch(
  () => props.option,
  () => renderChart(),
  { deep: true },
)

onMounted(() => {
  renderChart()
  if (chartRef.value) {
    resizeObserver = new ResizeObserver(() => handleResize())
    resizeObserver.observe(chartRef.value)
  }
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  resizeObserver?.disconnect()
  resizeObserver = undefined
  chartInstance?.dispose()
  chartInstance = undefined
})
</script>

<template>
  <div ref="chartRef" class="base-chart" :style="{ height }" />
</template>

<style scoped>
.base-chart {
  width: 100%;
}
</style>


