<script setup lang="ts">
import ModuleWorkbenchLayout from '../../components/common/ModuleWorkbenchLayout.vue'

const tabs = [
  { label: '用户管理', path: '/system-management/user-management' },
  { label: '角色权限管理', path: '/system-management/role-permission' },
  { label: '系统配置', path: '/system-management/system-config' },
]
</script>

<template>
  <ModuleWorkbenchLayout
    eyebrow="System Center"
    title="系统管理工作区"
    description="统一管理平台用户、角色权限与运行参数，体现系统级配置与治理能力。"
    :tabs="tabs"
  >
    <router-view />
  </ModuleWorkbenchLayout>
</template>
