<script setup lang="ts">
import type { WorkbenchMetricSummaryItem } from '../../types/ui'

const props = defineProps<{
  items: WorkbenchMetricSummaryItem[]
}>()

const isInteractive = (item: WorkbenchMetricSummaryItem) => item.clickable !== false && typeof item.action === 'function'
const isCompactValue = (value: WorkbenchMetricSummaryItem['value']) => String(value).length > 10
const handleItemClick = (item: WorkbenchMetricSummaryItem) => {
  if (!isInteractive(item)) {
    return
  }

  item.action?.()
}
</script>

<template>
  <div class="workbench-metric-grid">
    <el-card
      v-for="item in props.items"
      :key="item.id"
      shadow="hover"
      class="metric-card"
      :class="{ 'platform-clickable': isInteractive(item) }"
      @click="handleItemClick(item)"
    >
      <span class="metric-card__label">{{ item.label }}</span>
      <strong class="metric-card__value" :class="{ 'metric-card__value--compact': isCompactValue(item.value) }">
        {{ item.value }}
      </strong>
      <span class="metric-card__change">{{ item.hint }}</span>
    </el-card>
  </div>
</template>

<style scoped>
.workbench-metric-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: var(--platform-card-gap);
}

.metric-card__label {
  color: var(--platform-text-secondary);
  font-size: 13px;
}

.metric-card__value {
  display: block;
  margin-top: 8px;
  color: var(--platform-text-primary);
  font-size: 26px;
  line-height: 1.2;
  word-break: break-word;
}

.metric-card__value--compact {
  font-size: 22px;
}

.metric-card__change {
  display: inline-block;
  margin-top: 8px;
  color: var(--platform-text-secondary);
  font-size: 12px;
  line-height: 1.6;
}
</style>
