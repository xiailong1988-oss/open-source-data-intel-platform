<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import { Expand, Fold, Hide, RefreshRight, UserFilled, View } from '@element-plus/icons-vue'
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

const isFocusRoute = computed(() => route.path === '/dashboard' || route.path.startsWith('/topic-workbench/'))

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
}

/**
 * 顶部刷新按钮目前只驱动当前视图重挂载和 mock 刷新提示，
 * 后续接真实接口后可在 store 中集中扩展真实刷新动作。
 */
const handleRefresh = async () => {
  await appStore.refreshCurrentView()
  ElMessage.success('当前页面已刷新（mock）')
}
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

    <div class="top-bar__center">
      <GlobalCommandBar
        v-model="commandKeyword"
        v-model:mode="commandMode"
        :modes="workbenchUiConfig.commandModes"
        :placeholder="commandPlaceholder"
        :quick-questions="workbenchUiConfig.quickQuestions"
        @pick-question="handleQuickQuestion"
        @submit="pushToWorkbench(commandKeyword)"
      />
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
  display: grid;
  grid-template-columns: minmax(280px, 1fr) minmax(420px, 1.18fr) auto;
  width: 100%;
  min-height: var(--platform-header-height);
  align-items: center;
  gap: 16px;
  padding: 0 clamp(16px, 1vw, 24px);
}

.top-bar__left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.top-bar__layout-tools {
  display: flex;
  align-items: center;
  gap: 4px;
}

.top-bar__center {
  min-width: 0;
  max-width: 720px;
  width: 100%;
  justify-self: center;
}

.top-bar__title {
  color: var(--platform-header-text-primary);
  font-size: 14px;
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
  gap: 10px;
  min-width: max-content;
}

.top-bar__topic,
.top-bar__refresh {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 40px;
  border: 1px solid rgba(124, 156, 194, 0.14);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.03);
  padding: 6px 12px;
}

.top-bar__topic span,
.top-bar__refresh span {
  color: var(--platform-header-text-secondary);
  font-size: 12px;
}

.top-bar__user-meta {
  color: var(--platform-header-text-secondary);
  font-size: 13px;
}

.top-bar__user {
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px solid rgba(124, 156, 194, 0.12);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.03);
  padding: 6px 10px;
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
  font-size: 13px;
}

.top-bar__user-info span {
  color: var(--platform-header-text-secondary);
  font-size: 12px;
}

.top-bar--focus {
  grid-template-columns: minmax(260px, 0.95fr) minmax(380px, 1.26fr) auto;
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
    grid-template-columns: minmax(0, 1fr) minmax(300px, 420px) auto;
  }

  .top-bar__topic span,
  .top-bar__refresh span,
  .top-bar__title,
  .top-bar__user-meta {
    display: none;
  }
}

@media (max-width: 1024px) {
  .top-bar,
  .top-bar--focus {
    grid-template-columns: minmax(0, 1fr) auto;
  }

  .top-bar__center,
  .top-bar__topic,
  .top-bar__refresh,
  .top-bar__user-info,
  .top-bar__left :deep(.el-breadcrumb) {
    display: none;
  }
}
</style>
