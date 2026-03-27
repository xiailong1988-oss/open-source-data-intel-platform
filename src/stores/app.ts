import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { workbenchUiConfigMock } from '../mock/uiConfig'
import type { WorkbenchTopicCode } from '../types/ui'

interface PlatformUser {
  id: string
  name: string
  role: string
  department: string
}

export const useAppStore = defineStore('app', () => {
  const isCollapsed = ref(false)
  const isSidebarHidden = ref(false)
  const systemTitle = ref('开源数据智能分析平台')
  const activeTopicCode = ref<WorkbenchTopicCode>(workbenchUiConfigMock.topics[0].code)
  const refreshToken = ref(0)
  const isRefreshingView = ref(false)
  const lastRefreshAt = ref('')
  const currentUser = ref<PlatformUser>({
    id: 'USR-10018',
    name: '张明远',
    role: '平台管理员',
    department: '数据治理中心',
  })
  const workbenchUiConfig = ref(workbenchUiConfigMock)

  const activeTopic = computed(
    () => workbenchUiConfig.value.topics.find((item) => item.code === activeTopicCode.value) ?? workbenchUiConfig.value.topics[0],
  )

  /**
   * 刷新时间统一格式化为中文短时间，便于直接展示在顶部状态区。
   */
  const formatRefreshTime = () =>
    new Date().toLocaleString('zh-CN', {
      hour12: false,
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    })

  /**
   * 统一管理左侧菜单折叠状态，避免布局组件之间各自维护一份本地状态。
   */
  const toggleSidebar = () => {
    if (isSidebarHidden.value) {
      isSidebarHidden.value = false
      return
    }

    isCollapsed.value = !isCollapsed.value
  }

  /**
   * 驾驶舱和专题工作台需要比普通后台更大的画布，
   * 这里额外提供“隐藏侧栏”模式，让用户可以在保留路由和顶栏稳定的前提下获得更大内容区。
   */
  const toggleSidebarVisibility = () => {
    isSidebarHidden.value = !isSidebarHidden.value
  }

  /**
   * 当页面需要主动恢复侧栏时，通过这组方法统一处理，避免不同页面自己控制布局状态。
   */
  const setSidebarHidden = (hidden: boolean) => {
    isSidebarHidden.value = hidden
  }

  /**
   * 统一记录当前视图最近一次可用刷新时间，供顶部刷新状态和后续数据健康提示复用。
   */
  const markViewFresh = () => {
    lastRefreshAt.value = formatRefreshTime()
  }

  /**
   * 通过刷新 token 触发当前路由视图重新挂载，先用本地 mock 模拟刷新过程。
   * 后续接入真实接口时，可以在这里集中串联页面级刷新动作。
   */
  const refreshCurrentView = async () => {
    if (isRefreshingView.value) {
      return
    }

    isRefreshingView.value = true
    await new Promise((resolve) => setTimeout(resolve, 360))
    refreshToken.value += 1
    markViewFresh()
    isRefreshingView.value = false
  }

  /**
   * 保存当前专题上下文，供顶部统一入口、首页工作台和后续专题页面共用。
   */
  const setActiveTopic = (topicCode: WorkbenchTopicCode) => {
    activeTopicCode.value = topicCode
  }

  markViewFresh()

  return {
    isCollapsed,
    isSidebarHidden,
    systemTitle,
    currentUser,
    workbenchUiConfig,
    activeTopicCode,
    activeTopic,
    refreshToken,
    isRefreshingView,
    lastRefreshAt,
    toggleSidebar,
    toggleSidebarVisibility,
    setSidebarHidden,
    setActiveTopic,
    markViewFresh,
    refreshCurrentView,
  }
})
