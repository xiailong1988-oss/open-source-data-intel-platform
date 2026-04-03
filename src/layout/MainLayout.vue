<script setup lang="ts">
import { computed, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute } from 'vue-router'
import { useAppStore } from '../stores/app'
import SideMenu from './components/SideMenu.vue'
import TopBar from './components/TopBar.vue'

const appStore = useAppStore()
const { isCollapsed, isSidebarHidden, refreshToken } = storeToRefs(appStore)
const route = useRoute()

/**
 * 页面刷新动作通过变更 key 触发 router-view 重新挂载，
 * 让顶部刷新按钮可以在不改动各页实现的前提下统一生效。
 */
const pageViewKey = computed(() => `${route.fullPath}:${refreshToken.value}`)

/**
 * 首页和专题工作台更依赖大画布，因此给它们一套更宽的视图区策略。
 */
const isImmersiveRoute = computed(() => route.path === '/dashboard' || route.path.startsWith('/topic-workbench/'))

const asideWidth = computed(() => {
  if (isSidebarHidden.value) {
    return '0px'
  }

  return isCollapsed.value ? 'var(--platform-sidebar-collapse-width)' : 'var(--platform-sidebar-width)'
})

/**
 * 路由切换后统一记录当前视图的最近刷新时间，
 * 为顶部状态提示和后续数据健康提示提供同一时间基准。
 */
watch(
  () => route.fullPath,
  () => {
    appStore.markViewFresh()
  },
  { immediate: true },
)
</script>

<template>
  <el-container class="platform-layout">
    <el-aside
      class="platform-layout__aside"
      :class="{ 'platform-layout__aside--hidden': isSidebarHidden }"
      :width="asideWidth"
    >
      <SideMenu />
    </el-aside>

    <el-container class="platform-layout__body">
      <el-header class="platform-layout__header">
        <TopBar />
      </el-header>

      <el-main class="platform-layout__main" :class="{ 'platform-layout__main--immersive': isImmersiveRoute }">
        <div class="platform-layout__viewport" :class="{ 'platform-layout__viewport--immersive': isImmersiveRoute }">
          <router-view v-slot="{ Component }">
            <transition name="fade-slide" mode="out-in">
              <component :is="Component" :key="pageViewKey" />
            </transition>
          </router-view>
        </div>
        <div id="platform-drawer-host" class="platform-layout__drawer-host" aria-hidden="true" />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.platform-layout {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(100, 169, 255, 0.14) 0%, rgba(100, 169, 255, 0) 26%),
    radial-gradient(circle at right 20%, rgba(59, 210, 199, 0.08) 0%, rgba(59, 210, 199, 0) 18%),
    var(--platform-bg);
}

.platform-layout__aside {
  position: sticky;
  top: 0;
  height: 100vh;
  overflow: hidden;
  border-right: 1px solid rgba(112, 145, 184, 0.12);
  background: var(--platform-sidebar-bg);
  box-shadow: var(--platform-shadow-soft);
  transition:
    width 0.24s ease,
    border-color 0.24s ease,
    box-shadow 0.24s ease,
    opacity 0.24s ease;
}

.platform-layout__aside--hidden {
  border-right-color: transparent;
  box-shadow: none;
  opacity: 0;
}

.platform-layout__body {
  min-width: 0;
  min-height: 100vh;
}

.platform-layout__header {
  position: sticky;
  top: 0;
  z-index: 6;
  padding: 0;
  min-height: var(--platform-header-height);
  border-bottom: 1px solid var(--platform-header-border);
  background: var(--platform-header-bg);
  backdrop-filter: blur(14px);
}

.platform-layout__main {
  position: relative;
  min-width: 0;
  padding: clamp(14px, 0.9vw, 24px);
  background: transparent;
}

.platform-layout__main--immersive {
  padding-inline: clamp(10px, 0.64vw, 18px);
  padding-top: 10px;
  padding-bottom: 8px;
}

.platform-layout__viewport {
  display: flex;
  min-height: calc(100vh - var(--platform-header-height) - 2px);
  flex-direction: column;
  gap: var(--platform-section-gap);
  width: min(100%, var(--platform-page-max-width));
  margin: 0 auto;
  padding-bottom: 18px;
}

.platform-layout__viewport--immersive {
  width: min(100%, var(--platform-page-max-width-immersive));
  padding-bottom: 0;
}

.platform-layout__drawer-host {
  pointer-events: none;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(6px);
}
</style>
