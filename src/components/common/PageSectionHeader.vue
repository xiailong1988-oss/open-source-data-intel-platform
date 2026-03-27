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
      <span v-if="eyebrow" class="page-section-header__eyebrow">{{ eyebrow }}</span>
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
  gap: 16px;
}

.page-section-header--compact h3 {
  font-size: 16px;
}

.page-section-header__content {
  min-width: 0;
}

.page-section-header__eyebrow {
  display: inline-flex;
  margin-bottom: 6px;
  border-radius: 999px;
  background: var(--platform-accent-soft);
  padding: 4px 10px;
  color: var(--platform-accent-strong);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.08em;
}

.page-section-header h3 {
  margin: 0;
  color: var(--platform-text-primary);
  font-size: 18px;
}

.page-section-header p {
  margin: 8px 0 0;
  color: var(--platform-text-secondary);
  font-size: 13px;
  line-height: 1.7;
}

.page-section-header__actions {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

@media (max-width: 900px) {
  .page-section-header {
    flex-direction: column;
  }
}
</style>
