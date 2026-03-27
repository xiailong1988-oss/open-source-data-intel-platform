<script setup lang="ts">
import ModuleWorkbenchLayout from '../../components/common/ModuleWorkbenchLayout.vue'

const tabs = [
  { label: '预警概览', path: '/analysis-warning/overview' },
  { label: '预警规则管理', path: '/analysis-warning/rule-management' },
  { label: '预警记录', path: '/analysis-warning/records' },
]
</script>

<template>
  <ModuleWorkbenchLayout
    eyebrow="Alert Center"
    title="分析预警工作区"
    description="围绕风险发现、预警研判与处置建议，形成可追踪的辅助决策闭环。"
    :tabs="tabs"
  >
    <router-view />
  </ModuleWorkbenchLayout>
</template>
