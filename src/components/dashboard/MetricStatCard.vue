<script setup lang="ts">
import type { Component } from 'vue'
import { computed } from 'vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import type { DashboardMetric } from '../../types/dashboard'

const props = defineProps<{
  item: DashboardMetric
}>()
defineEmits<{
  (event: 'click', item: DashboardMetric): void
}>()

const iconMap = ElementPlusIconsVue as Record<string, Component>
const metricIconMap: Record<string, string> = {
  dataSourceTotal: 'Grid',
  newDataToday: 'UploadFilled',
  governedData: 'Finished',
  entityCount: 'Aim',
  alertCount: 'Warning',
  reportCount: 'Document',
}

const metricIcon = computed(() => iconMap[metricIconMap[props.item.id]] ?? iconMap.DataAnalysis)

const trendText = computed(() => {
  if (props.item.trend === 'up') {
    return '上升'
  }
  if (props.item.trend === 'down') {
    return '下降'
  }
  return '持平'
})

const isPositiveTrend = computed(() => {
  if (props.item.trend === 'flat') {
    return true
  }

  const polarity = props.item.trendPolarity ?? 'higher-better'
  if (polarity === 'lower-better') {
    return props.item.trend === 'down'
  }

  return props.item.trend === 'up'
})

const tagType = computed(() => {
  if (props.item.trend === 'flat') {
    return 'info'
  }
  if (isPositiveTrend.value) {
    return 'success'
  }
  return 'danger'
})

const formatValue = (value: number) => new Intl.NumberFormat('zh-CN').format(value)
</script>

<template>
  <el-card class="metric-card metric-card--clickable" shadow="hover" @click="$emit('click', item)">
    <div class="metric-card__main">
      <div class="metric-card__content">
        <p class="metric-card__label">{{ item.label }}</p>
        <div class="metric-card__value">
          <strong>{{ formatValue(item.value) }}</strong>
          <span v-if="item.unit">{{ item.unit }}</span>
        </div>
        <p class="metric-card__description">{{ item.description }}</p>
      </div>
      <div class="metric-card__icon">
        <el-icon :size="22">
          <component :is="metricIcon" />
        </el-icon>
      </div>
    </div>
    <div class="metric-card__footer">
      <el-tag size="small" :type="tagType">{{ trendText }} {{ Math.abs(item.delta) }}%</el-tag>
      <span>{{ item.compareText }}</span>
    </div>
  </el-card>
</template>

<style scoped>
.metric-card {
  border: 1px solid var(--platform-border-subtle);
  background:
    radial-gradient(circle at top right, rgba(100, 169, 255, 0.14) 0%, rgba(100, 169, 255, 0) 34%),
    var(--platform-card-bg);
}

.metric-card--clickable {
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.metric-card--clickable:hover {
  transform: translateY(-2px);
  border-color: var(--platform-border-strong);
  box-shadow: 0 20px 40px rgba(2, 10, 20, 0.34);
}

.metric-card__main {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.metric-card__content {
  min-width: 0;
}

.metric-card__label {
  margin: 0;
  color: var(--platform-text-secondary);
  font-size: 13px;
}

.metric-card__value {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-top: 8px;
}

.metric-card__value strong {
  color: var(--platform-text-primary);
  font-size: 28px;
  line-height: 1;
}

.metric-card__value span {
  color: var(--platform-text-secondary);
  font-size: 13px;
}

.metric-card__description {
  margin: 8px 0 0;
  overflow: hidden;
  color: var(--platform-text-secondary);
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 12px;
}

.metric-card__icon {
  display: flex;
  height: 38px;
  width: 38px;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(100, 169, 255, 0.18) 0%, rgba(59, 210, 199, 0.12) 100%);
  color: var(--platform-accent-strong);
}

.metric-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14px;
  color: var(--platform-text-tertiary);
  font-size: 12px;
}
</style>
