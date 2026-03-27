<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

interface ModuleTab {
  label: string
  path: string
}

/**
 * 统一承接各模块二级页的页头、模块说明和页签切换，
 * 让数据接入、治理、知识、预警、系统管理等页面共享同一套工作台骨架。
 */
const props = defineProps<{
  title: string
  description: string
  eyebrow?: string
  tabs: ModuleTab[]
}>()

const route = useRoute()
const router = useRouter()

const activeTab = computed({
  get: () => route.path,
  set: (path: string) => {
    router.push(path)
  },
})

const visibleTabs = computed(() => props.tabs.filter((item) => item.path))
</script>

<template>
  <section class="module-workbench-layout platform-page-shell">
    <div class="platform-page-hero">
      <div class="platform-page-hero__header">
        <div class="platform-page-heading">
          <span v-if="eyebrow" class="platform-page-heading__eyebrow">{{ eyebrow }}</span>
          <h2>{{ title }}</h2>
          <p>{{ description }}</p>
        </div>
        <div v-if="$slots.actions" class="platform-page-hero__meta">
          <slot name="actions" />
        </div>
      </div>

      <el-tabs v-model="activeTab" class="module-workbench-layout__tabs">
        <el-tab-pane v-for="item in visibleTabs" :key="item.path" :name="item.path" :label="item.label" />
      </el-tabs>
    </div>

    <div class="module-workbench-layout__body">
      <slot />
    </div>
  </section>
</template>

<style scoped>
.module-workbench-layout__tabs {
  margin-top: 4px;
}

.module-workbench-layout__body {
  display: flex;
  flex-direction: column;
  gap: var(--platform-section-gap);
  min-width: 0;
}
</style>
