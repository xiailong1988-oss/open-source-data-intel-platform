<script setup lang="ts">
import ModuleWorkbenchLayout from '../../components/common/ModuleWorkbenchLayout.vue'

const tabs = [
  { label: '正式报告', path: '/report-center/reports' },
  { label: '简报中心', path: '/report-center/briefings' },
]
</script>

<template>
  <ModuleWorkbenchLayout
    eyebrow="Report Center"
    title="报告与简报工作台"
    description="统一承接正式分析报告与简报输出，形成从洞察沉淀到正式输出的双轨能力。"
    :tabs="tabs"
  >
    <router-view />
  </ModuleWorkbenchLayout>
</template>
