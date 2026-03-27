<script setup lang="ts">
import type { EChartsOption } from '../../lib/echarts'
import BaseEChart from './BaseEChart.vue'

defineEmits<{
  (event: 'chart-click', params: unknown): void
}>()

withDefaults(
  defineProps<{
    title: string
    subtitle?: string
    option: EChartsOption
    height?: string
    isEmpty?: boolean
    emptyText?: string
  }>(),
  {
    subtitle: '',
    height: '300px',
    isEmpty: false,
    emptyText: '暂无图表数据',
  },
)
</script>

<template>
  <el-card class="chart-card" shadow="never">
    <template #header>
      <div class="chart-card__header">
        <span>{{ title }}</span>
        <small v-if="subtitle">{{ subtitle }}</small>
      </div>
    </template>
    <div v-if="isEmpty" class="chart-card__empty">
      <el-empty :description="emptyText" :image-size="76" />
    </div>
    <div v-else class="chart-card__body">
      <BaseEChart :option="option" :height="height" @chart-click="$emit('chart-click', $event)" />
    </div>
  </el-card>
</template>

<style scoped>
.chart-card {
  display: flex;
  height: 100%;
  flex-direction: column;
  border: 1px solid var(--platform-border-subtle);
}

.chart-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  color: var(--platform-text-primary);
  font-size: 15px;
  font-weight: 600;
}

.chart-card__header small {
  color: var(--platform-text-secondary);
  font-size: 12px;
  font-weight: 400;
}

.chart-card__body {
  flex: 1;
}

.chart-card__empty {
  display: flex;
  flex: 1;
  min-height: 260px;
  align-items: center;
  justify-content: center;
}
</style>

