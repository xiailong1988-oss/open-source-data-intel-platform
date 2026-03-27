<script setup lang="ts">
import ModuleWorkbenchLayout from '../../components/common/ModuleWorkbenchLayout.vue'

const tabs = [
  { label: '治理概览', path: '/data-governance/overview' },
  { label: '标准化数据列表', path: '/data-governance/standardized-list' },
]
</script>

<template>
  <ModuleWorkbenchLayout
    eyebrow="Governance"
    title="数据治理工作区"
    description="面向治理中台构建质量闭环，覆盖清洗、去重、标准化与异常治理全流程。"
    :tabs="tabs"
  >
    <router-view />
  </ModuleWorkbenchLayout>
</template>
