<script setup lang="ts">
import ModuleWorkbenchLayout from '../../components/common/ModuleWorkbenchLayout.vue'

const tabs = [
  { label: '数据源管理', path: '/data-access/source-management' },
  { label: '采集任务管理', path: '/data-access/task-management' },
  { label: '信源列表', path: '/data-access/source-intelligence' },
  { label: '数据健康', path: '/data-access/data-health' },
]
</script>

<template>
  <ModuleWorkbenchLayout
    eyebrow="Data Access"
    title="数据接入工作台"
    description="统一管理数据源接入、采集任务调度、信源观察与数据健康状态，保障多源数据稳定进入分析链路。"
    :tabs="tabs"
  >
    <router-view />
  </ModuleWorkbenchLayout>
</template>
