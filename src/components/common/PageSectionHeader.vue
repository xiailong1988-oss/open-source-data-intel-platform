<script setup lang="ts">
/**
 * 统一页面分区标题组件，收敛标题、副标题和右侧操作区的排版，
 * 避免首页、检索等重点页面继续各自维护一套头部结构。
 */
defineProps<{
  title: string
  description?: string
  eyebrow?: string
  compact?: boolean
}>()
</script>

<template>
  <div class="page-section-header" :class="{ 'page-section-header--compact': compact }">
    <div class="page-section-header__content">
      <h3>{{ title }}</h3>
      <p v-if="description">{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="page-section-header__actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<style scoped>
.page-section-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.page-section-header--compact h3 {
  font-size: 14px;
}

.page-section-header__content {
  min-width: 0;
}

.page-section-header h3 {
  margin: 0;
  color: var(--platform-text-primary);
  font-size: 15px;
  line-height: 1.35;
}

.page-section-header p {
  margin: 4px 0 0;
  color: var(--platform-text-secondary);
  font-size: 12px;
  line-height: 1.5;
}

.page-section-header__actions {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

@media (max-width: 900px) {
  .page-section-header {
    flex-direction: column;
  }
}
</style>
