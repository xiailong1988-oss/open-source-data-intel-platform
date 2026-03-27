<script setup lang="ts">
import ModuleWorkbenchLayout from '../../components/common/ModuleWorkbenchLayout.vue'

const tabs = [
  { label: '构建概览', path: '/knowledge-build/overview' },
  { label: '实体管理', path: '/knowledge-build/entity-management' },
  { label: '事件管理', path: '/knowledge-build/event-management' },
]
</script>

<template>
  <ModuleWorkbenchLayout
    eyebrow="Knowledge"
    title="知识构建工作区"
    description="打通“数据到知识”链路，沉淀实体、关系、事件与标签的多维知识资产。"
    :tabs="tabs"
  >
    <router-view />
  </ModuleWorkbenchLayout>
</template>
