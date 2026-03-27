<script setup lang="ts">
import type { Component } from 'vue'
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { useAppStore } from '../../stores/app'
import { menuItems } from '../../router/menu'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const { isCollapsed, systemTitle } = storeToRefs(appStore)

const iconMap = ElementPlusIconsVue as Record<string, Component>
const activePath = computed(() => {
  const matchedActiveMenu = [...route.matched]
    .reverse()
    .find((record) => record.meta?.activeMenu)?.meta?.activeMenu
  if (typeof matchedActiveMenu === 'string') {
    return matchedActiveMenu
  }

  const firstPath = route.path.split('/').filter(Boolean)[0]
  return firstPath ? `/${firstPath}` : '/dashboard'
})

const resolveIcon = (icon: string) => iconMap[icon] ?? iconMap.Menu

const handleMenuSelect = async (path: string) => {
  if (activePath.value === path) {
    return
  }
  await router.push(path)
}
</script>

<template>
  <aside class="side-menu">
    <div class="side-menu__logo">
      <div class="side-menu__logo-badge">DI</div>
      <span v-show="!isCollapsed" class="side-menu__logo-text">{{ systemTitle }}</span>
    </div>

    <el-scrollbar>
      <el-menu
        :default-active="activePath"
        :collapse="isCollapsed"
        class="side-menu__nav"
        background-color="transparent"
        active-text-color="#ffffff"
        text-color="#c6d4ea"
        @select="handleMenuSelect"
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="resolveIcon(item.icon)" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-scrollbar>
  </aside>
</template>

<style scoped>
.side-menu {
  display: flex;
  height: 100%;
  flex-direction: column;
  background:
    radial-gradient(circle at top, rgba(100, 169, 255, 0.16) 0%, rgba(100, 169, 255, 0) 26%),
    var(--platform-sidebar-bg);
}

.side-menu__logo {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: var(--platform-header-height);
  padding: 0 18px;
  border-bottom: 1px solid rgba(220, 235, 255, 0.08);
}

.side-menu__logo-badge {
  display: flex;
  height: 34px;
  width: 34px;
  align-items: center;
  justify-content: center;
  border-radius: 11px;
  background: linear-gradient(135deg, rgba(101, 170, 255, 0.94) 0%, rgba(42, 107, 189, 0.94) 100%);
  box-shadow: 0 10px 22px rgba(28, 92, 176, 0.24);
  color: #f4f8ff;
  font-size: 12px;
  font-weight: 700;
}

.side-menu__logo-text {
  overflow: hidden;
  color: var(--platform-header-text-primary);
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  font-weight: 600;
}

.side-menu__nav {
  border-right: none;
  padding: 18px 10px 22px;
}

.side-menu__nav :deep(.el-menu-item) {
  min-height: 46px;
  margin-bottom: 8px;
  border-radius: 14px;
  color: var(--platform-header-text-secondary);
  transition: background 0.2s ease, transform 0.2s ease, color 0.2s ease;
}

.side-menu__nav :deep(.el-menu-item:hover) {
  transform: translateX(2px);
  background: rgba(100, 169, 255, 0.1);
  color: var(--platform-header-text-primary);
}

.side-menu__nav :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(71, 143, 241, 0.9) 0%, rgba(38, 95, 170, 0.86) 100%);
  box-shadow: 0 12px 28px rgba(18, 56, 107, 0.24);
}

.side-menu__nav :deep(.el-menu-item .el-icon) {
  margin-right: 10px;
  color: inherit;
}
</style>
