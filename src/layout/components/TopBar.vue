<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import { Expand, Fold, Hide, RefreshRight, Search, UserFilled, View } from '@element-plus/icons-vue'
import { useAppStore } from '../../stores/app'
import GlobalCommandBar from './GlobalCommandBar.vue'
import type { WorkbenchCommandMode, WorkbenchTopicCode } from '../../types/ui'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const {
  isCollapsed,
  isSidebarHidden,
  systemTitle,
  currentUser,
  workbenchUiConfig,
  activeTopic,
  activeTopicCode,
  isRefreshingView,
  lastRefreshAt,
} = storeToRefs(appStore)

const commandKeyword = ref('')
const commandMode = ref<WorkbenchCommandMode>('智能检索')
const isCommandExpanded = ref(false)
const commandCenterRef = ref<HTMLElement | null>(null)

const isFocusRoute = computed(() => route.path === '/dashboard' || route.path.startsWith('/topic-workbench/'))
const showCockpitTitle = computed(() => route.path === '/dashboard')

const breadcrumbItems = computed(() =>
  route.matched
    .filter((record) => record.meta?.title)
    .map((record, index) => ({
      key: `${record.path}-${index}`,
      title: String(record.meta.title),
    })),
)

const commandPlaceholder = computed(() => workbenchUiConfig.value.placeholders[commandMode.value])
const sidebarToggleIcon = computed(() => (isCollapsed.value ? Expand : Fold))
const sidebarVisibilityIcon = computed(() => (isSidebarHidden.value ? View : Hide))
const sidebarVisibilityText = computed(() => (isSidebarHidden.value ? '展开侧栏' : '隐藏侧栏'))

const focusCommandInput = async () => {
  await nextTick()
  const input = commandCenterRef.value?.querySelector('input')
  if (input instanceof HTMLInputElement) {
    input.focus()
  }
}

const openCommandPanel = async () => {
  isCommandExpanded.value = true
  await focusCommandInput()
}

const closeCommandPanel = () => {
  isCommandExpanded.value = false
}

const toggleCommandPanel = () => {
  if (isCommandExpanded.value) {
    closeCommandPanel()
    return
  }

  void openCommandPanel()
}

/**
 * 统一折叠菜单，保留图标轨道模式，适合日常后台浏览。
 */
const toggleSidebar = () => appStore.toggleSidebar()

/**
 * 驾驶舱场景需要更大的内容画布，因此顶部提供显式的侧栏隐藏入口。
 */
const toggleSidebarVisibility = () => appStore.toggleSidebarVisibility()

/**
 * 顶部专题视角作为全局上下文开关使用，
 * 后续专题工作台会继续复用这组状态。
 */
const handleTopicChange = (topicCode: string | number | boolean) => {
  appStore.setActiveTopic(String(topicCode) as WorkbenchTopicCode)
  if (topicCode !== 'all') {
    ElMessage.info(`已切换到${activeTopic.value.label}专题视角`) 
  }
}

/**
 * 顶部统一入口直接复用现有智能检索与智能问答页面，
 * 通过 query 注入当前关键词和专题上下文，避免新增一套独立入口页。
 */
const pushToWorkbench = (question: string) => {
  const text = question.trim()
  if (!text) {
    ElMessage.warning(`请输入${commandMode.value === '智能检索' ? '检索线索' : '业务问题'}`)
    return
  }

  const topicCode = activeTopicCode.value !== 'all' ? activeTopicCode.value : undefined
  if (commandMode.value === '智能问答') {
    router.push({
      path: '/smart-qa',
      query: {
        question: text,
        autoAsk: 'true',
        topic: topicCode,
        from: 'topbar-command',
      },
    })
    return
  }

  router.push({
    path: '/smart-search',
    query: {
      keyword: text,
      mode: '语义检索',
      topic: topicCode,
      from: 'topbar-command',
    },
  })
}

/**
 * 推荐问题点击后直接回填输入框并触发统一入口跳转，
 * 保持顶部推荐、手动输入和回车检索三条链路行为一致。
 */
const handleQuickQuestion = (question: string) => {
  commandKeyword.value = question
  pushToWorkbench(question)
  closeCommandPanel()
}

const handleCommandSubmit = () => {
  pushToWorkbench(commandKeyword.value)
  if (commandKeyword.value.trim()) {
    closeCommandPanel()
  }
}

const handleDocumentMouseDown = (event: MouseEvent) => {
  if (!isCommandExpanded.value || !commandCenterRef.value) {
    return
  }

  const target = event.target
  if (target instanceof Node && commandCenterRef.value.contains(target)) {
    return
  }

  closeCommandPanel()
}

const handleDocumentKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Escape') {
    closeCommandPanel()
  }
}

/**
 * 顶部刷新按钮目前只驱动当前视图重挂载和 mock 刷新提示，
 * 后续接真实接口后可在 store 中集中扩展真实刷新动作。
 */
const handleRefresh = async () => {
  await appStore.refreshCurrentView()
  ElMessage.success('当前页面已刷新（mock）')
}

watch(
  () => route.fullPath,
  () => {
    closeCommandPanel()
  },
)

onMounted(() => {
  document.addEventListener('mousedown', handleDocumentMouseDown)
  document.addEventListener('keydown', handleDocumentKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleDocumentMouseDown)
  document.removeEventListener('keydown', handleDocumentKeydown)
})
</script>

<template>
  <div class="top-bar" :class="{ 'top-bar--focus': isFocusRoute }">
    <div class="top-bar__left">
      <div class="top-bar__layout-tools">
        <el-button :icon="sidebarToggleIcon" text @click="toggleSidebar" />
        <el-button :icon="sidebarVisibilityIcon" text @click="toggleSidebarVisibility">{{ sidebarVisibilityText }}</el-button>
      </div>
      <span class="top-bar__title">{{ systemTitle }}</span>
      <el-divider direction="vertical" />
      <el-breadcrumb separator="/">
        <el-breadcrumb-item v-for="item in breadcrumbItems" :key="item.key">
          {{ item.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div ref="commandCenterRef" class="top-bar__center">
      <span v-if="showCockpitTitle" class="top-bar__cockpit-title">海淀区综合态势驾驶舱</span>

      <div class="top-bar__command-anchor">
        <el-tooltip content="智能检索 / 智能问答" placement="bottom">
          <el-button
            circle
            class="top-bar__command-trigger"
            :class="{ 'is-active': isCommandExpanded }"
            :icon="Search"
            @click="toggleCommandPanel"
          />
        </el-tooltip>

        <transition name="command-panel">
          <div v-if="isCommandExpanded" class="top-bar__command-panel">
            <GlobalCommandBar
              v-model="commandKeyword"
              v-model:mode="commandMode"
              :modes="workbenchUiConfig.commandModes"
              :placeholder="commandPlaceholder"
              :quick-questions="workbenchUiConfig.quickQuestions"
              @pick-question="handleQuickQuestion"
              @submit="handleCommandSubmit"
            />
          </div>
        </transition>
      </div>
    </div>

    <div class="top-bar__right">
      <div class="top-bar__topic">
        <span>专题视角</span>
        <el-select :model-value="activeTopicCode" size="small" style="width: 118px" @change="handleTopicChange">
          <el-option v-for="item in workbenchUiConfig.topics" :key="item.code" :label="item.label" :value="item.code" />
        </el-select>
      </div>
      <div class="top-bar__refresh">
        <span>最近刷新 {{ lastRefreshAt }}</span>
        <el-button :icon="RefreshRight" :loading="isRefreshingView" text @click="handleRefresh" />
      </div>
      <span class="top-bar__user-meta">{{ currentUser.department }}</span>
      <el-dropdown>
        <div class="top-bar__user">
          <el-avatar :icon="UserFilled" :size="30" />
          <div class="top-bar__user-info">
            <strong>{{ currentUser.name }}</strong>
            <span>{{ currentUser.role }}</span>
          </div>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item>个人中心</el-dropdown-item>
            <el-dropdown-item>偏好设置</el-dropdown-item>
            <el-dropdown-item divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<style scoped>
.top-bar {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  width: 100%;
  min-height: var(--platform-header-height);
  align-items: center;
  gap: 12px;
  padding: 0 clamp(14px, 0.9vw, 20px);
}

.top-bar__left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.top-bar__layout-tools {
  display: flex;
  align-items: center;
  gap: 2px;
}

.top-bar__center {
  position: absolute;
  top: 50%;
  left: 50%;
  z-index: 3;
  transform: translate(-50%, -50%);
}

.top-bar__cockpit-title {
  position: relative;
  display: inline-flex;
  align-items: center;
  min-height: 38px;
  padding: 0 2px;
  font-size: clamp(18px, 1.15vw, 24px);
  font-weight: 800;
  letter-spacing: 0.14em;
  white-space: nowrap;
  color: transparent;
  background: linear-gradient(135deg, #eef8ff 0%, #8ad7ff 36%, #4fa8ff 62%, #57ffe1 100%);
  -webkit-background-clip: text;
  background-clip: text;
  text-shadow:
    0 0 10px rgba(106, 196, 255, 0.24),
    0 0 28px rgba(49, 131, 255, 0.18);
}

.top-bar__cockpit-title::after {
  content: '';
  position: absolute;
  left: 4px;
  right: 4px;
  bottom: 1px;
  height: 2px;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(91, 168, 255, 0) 0%, rgba(91, 168, 255, 0.78) 34%, rgba(79, 255, 221, 0.9) 72%, rgba(79, 255, 221, 0) 100%);
  box-shadow: 0 0 14px rgba(79, 199, 255, 0.32);
  opacity: 0.82;
}

.top-bar__command-anchor {
  position: absolute;
  top: 50%;
  left: calc(100% + 12px);
  transform: translateY(-50%);
}

.top-bar__command-trigger {
  width: 38px;
  height: 38px;
  border: 1px solid rgba(124, 156, 194, 0.16);
  background: rgba(255, 255, 255, 0.03);
  color: var(--platform-header-text-primary);
  box-shadow: none;
  transition: border-color 0.2s ease, background 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.top-bar__command-trigger:hover,
.top-bar__command-trigger.is-active {
  transform: translateY(-1px);
  border-color: rgba(141, 186, 245, 0.34);
  background: rgba(14, 27, 44, 0.86);
  color: #f4f8ff;
}

.top-bar__command-panel {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  z-index: 30;
  width: clamp(420px, 42vw, 720px);
  border: 1px solid rgba(124, 156, 194, 0.16);
  border-radius: 18px;
  background: rgba(4, 10, 18, 0.92);
  padding: 8px;
  box-shadow: var(--platform-shadow-pop);
  backdrop-filter: blur(18px);
}

.top-bar__command-panel :deep(.global-command-bar) {
  border-color: rgba(124, 156, 194, 0.12);
  background: rgba(8, 16, 26, 0.36);
}

.top-bar__title {
  color: var(--platform-header-text-primary);
  font-size: 13px;
  font-weight: 600;
}

.top-bar__left :deep(.el-breadcrumb) {
  min-width: 0;
}

.top-bar__left :deep(.el-breadcrumb__item .el-breadcrumb__inner) {
  color: var(--platform-header-text-secondary);
}

.top-bar__right {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: max-content;
}

.top-bar__topic,
.top-bar__refresh {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 36px;
  border: 1px solid rgba(124, 156, 194, 0.14);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.03);
  padding: 5px 10px;
}

.top-bar__topic span,
.top-bar__refresh span {
  color: var(--platform-header-text-secondary);
  font-size: 11px;
}

.top-bar__user-meta {
  color: var(--platform-header-text-secondary);
  font-size: 12px;
}

.top-bar__user {
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px solid rgba(124, 156, 194, 0.12);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.03);
  padding: 5px 9px;
  cursor: pointer;
  outline: none;
}

.top-bar__user-info {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.top-bar__user-info strong {
  color: var(--platform-header-text-primary);
  font-size: 12px;
}

.top-bar__user-info span {
  color: var(--platform-header-text-secondary);
  font-size: 11px;
}

.top-bar--focus {
  grid-template-columns: minmax(0, 1fr) auto;
}

.top-bar--focus .top-bar__title,
.top-bar--focus .top-bar__left :deep(.el-divider),
.top-bar--focus .top-bar__topic span,
.top-bar--focus .top-bar__refresh span,
.top-bar--focus .top-bar__user-meta,
.top-bar--focus .top-bar__user-info span {
  display: none;
}

.top-bar--focus .top-bar__topic,
.top-bar--focus .top-bar__refresh,
.top-bar--focus .top-bar__user {
  padding-inline: 10px;
}

@media (max-width: 1360px) {
  .top-bar,
  .top-bar--focus {
    grid-template-columns: minmax(0, 1fr) auto;
  }

  .top-bar__topic span,
  .top-bar__refresh span,
  .top-bar__title,
  .top-bar__user-meta {
    display: none;
  }

  .top-bar__cockpit-title {
    font-size: clamp(16px, 1.3vw, 20px);
  }
}

@media (max-width: 1024px) {
  .top-bar,
  .top-bar--focus {
    grid-template-columns: minmax(0, 1fr) auto;
  }

  .top-bar__topic,
  .top-bar__refresh,
  .top-bar__user-info,
  .top-bar__left :deep(.el-breadcrumb) {
    display: none;
  }

  .top-bar__command-panel {
    right: 0;
    width: min(90vw, 520px);
  }

  .top-bar__cockpit-title {
    display: none;
  }

  .top-bar__center {
    left: auto;
    right: 84px;
    transform: translateY(-50%);
  }

  .top-bar__command-anchor {
    position: static;
    transform: none;
  }
}

.command-panel-enter-active,
.command-panel-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.command-panel-enter-from,
.command-panel-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

@media (max-width: 1024px) {
  .command-panel-enter-from,
  .command-panel-leave-to {
    transform: translateY(-6px);
  }
}
</style>
