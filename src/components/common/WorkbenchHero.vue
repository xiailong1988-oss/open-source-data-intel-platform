<script setup lang="ts">
/**
 * 统一承接工作台页的首屏 Hero 区，
 * 避免首页、事件中心、预警中心和系统管理页继续各自维护一套相似的标题结构。
 */
defineProps<{
  eyebrow?: string
  title: string
  description: string
  level?: 'h1' | 'h2' | 'h3'
}>()
</script>

<template>
  <div class="platform-page-hero workbench-hero">
    <div class="platform-page-hero__header">
      <div class="platform-page-heading">
        <h1 v-if="level === 'h1'">{{ title }}</h1>
        <h2 v-else-if="level === 'h2'">{{ title }}</h2>
        <h3 v-else>{{ title }}</h3>
        <p>{{ description }}</p>
      </div>
      <div v-if="$slots.meta" class="platform-page-hero__meta">
        <slot name="meta" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.workbench-hero {
  min-width: 0;
}
</style>
