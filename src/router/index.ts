import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { ElLoading } from 'element-plus'
import { menuItems } from './menu'

const MainLayout = () => import('../layout/MainLayout.vue')
const OverviewDashboard = () => import('../views/dashboard/OverviewDashboard.vue')
const TopicWorkbenchView = () => import('../views/topics/TopicWorkbenchView.vue')

const DataAccessLayout = () => import('../views/data-access/DataAccessLayout.vue')
const DataSourceManagement = () => import('../views/data-access/DataSourceManagement.vue')
const CollectionTaskManagement = () => import('../views/data-access/CollectionTaskManagement.vue')
const SourceIntelligenceView = () => import('../views/data-access/SourceIntelligenceView.vue')
const DataHealthView = () => import('../views/data-access/DataHealthView.vue')

const DataGovernanceLayout = () => import('../views/data-governance/DataGovernanceLayout.vue')
const GovernanceOverview = () => import('../views/data-governance/GovernanceOverview.vue')
const StandardizedDataList = () => import('../views/data-governance/StandardizedDataList.vue')

const KnowledgeBuildLayout = () => import('../views/knowledge-build/KnowledgeBuildLayout.vue')
const KnowledgeOverview = () => import('../views/knowledge-build/KnowledgeOverview.vue')
const EntityManagement = () => import('../views/knowledge-build/EntityManagement.vue')
const EventManagement = () => import('../views/knowledge-build/EventManagement.vue')

const SmartSearchView = () => import('../views/smart-search/SmartSearchView.vue')
const SmartQaView = () => import('../views/smart-qa/SmartQaView.vue')

const AnalysisWarningLayout = () => import('../views/analysis-warning/AnalysisWarningLayout.vue')
const AnalysisWarningOverview = () => import('../views/analysis-warning/AnalysisWarningOverview.vue')
const WarningRuleManagement = () => import('../views/analysis-warning/WarningRuleManagement.vue')
const WarningRecordList = () => import('../views/analysis-warning/WarningRecordList.vue')

const ReportCenterLayout = () => import('../views/report-center/ReportCenterLayout.vue')
const ReportCenterList = () => import('../views/report-center/ReportCenterList.vue')
const BriefingCenterView = () => import('../views/report-center/BriefingCenterView.vue')
const ReportDetailView = () => import('../views/report-center/ReportDetailView.vue')

const SystemManagementLayout = () => import('../views/system-management/SystemManagementLayout.vue')
const UserManagement = () => import('../views/system-management/UserManagement.vue')
const RolePermissionManagement = () => import('../views/system-management/RolePermissionManagement.vue')
const SystemConfigView = () => import('../views/system-management/SystemConfigView.vue')

/**
 * 统一从侧边菜单配置中提取一级模块元信息，
 * 让菜单标题、模块说明和路由骨架始终共用同一份来源。
 */
const resolveMenuMeta = (path: string) => {
  const menuItem = menuItems.find((item) => item.path === path)
  return {
    title: menuItem?.title ?? '业务模块',
    icon: menuItem?.icon,
    description: menuItem?.description ?? '当前模块正在按规划持续建设。',
  }
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: OverviewDashboard,
        meta: resolveMenuMeta('/dashboard'),
      },
      {
        path: 'topic-workbench/economy',
        name: 'EconomyTopicWorkbench',
        component: TopicWorkbenchView,
        props: { topicCode: 'economy' },
        meta: {
          title: '经济专题工作台',
          description: '围绕产业动态、供应链和投资项目变化构建的专题工作台。',
          activeMenu: '/dashboard',
        },
      },
      {
        path: 'topic-workbench/innovation',
        name: 'InnovationTopicWorkbench',
        component: TopicWorkbenchView,
        props: { topicCode: 'innovation' },
        meta: {
          title: '创新专题工作台',
          description: '围绕创新机构、技术热点和协同攻关构建的专题工作台。',
          activeMenu: '/dashboard',
        },
      },
      {
        path: 'data-access',
        component: DataAccessLayout,
        redirect: '/data-access/source-management',
        meta: resolveMenuMeta('/data-access'),
        children: [
          {
            path: 'source-management',
            name: 'DataSourceManagement',
            component: DataSourceManagement,
            meta: { title: '数据源管理' },
          },
          {
            path: 'task-management',
            name: 'CollectionTaskManagement',
            component: CollectionTaskManagement,
            meta: { title: '采集任务管理' },
          },
          {
            path: 'source-intelligence',
            name: 'SourceIntelligence',
            component: SourceIntelligenceView,
            meta: { title: '信源列表' },
          },
          {
            path: 'data-health',
            name: 'DataHealth',
            component: DataHealthView,
            meta: { title: '数据健康' },
          },
        ],
      },
      {
        path: 'data-governance',
        component: DataGovernanceLayout,
        redirect: '/data-governance/overview',
        meta: resolveMenuMeta('/data-governance'),
        children: [
          {
            path: 'overview',
            name: 'GovernanceOverview',
            component: GovernanceOverview,
            meta: { title: '治理概览' },
          },
          {
            path: 'standardized-list',
            name: 'StandardizedDataList',
            component: StandardizedDataList,
            meta: { title: '标准化数据列表' },
          },
        ],
      },
      {
        path: 'knowledge-build',
        component: KnowledgeBuildLayout,
        redirect: '/knowledge-build/overview',
        meta: resolveMenuMeta('/knowledge-build'),
        children: [
          {
            path: 'overview',
            name: 'KnowledgeOverview',
            component: KnowledgeOverview,
            meta: { title: '构建概览' },
          },
          {
            path: 'entity-management',
            name: 'EntityManagement',
            component: EntityManagement,
            meta: { title: '实体管理' },
          },
          {
            path: 'event-management',
            name: 'EventManagement',
            component: EventManagement,
            meta: { title: '事件管理' },
          },
        ],
      },
      {
        path: 'smart-search',
        name: 'SmartSearch',
        component: SmartSearchView,
        meta: resolveMenuMeta('/smart-search'),
      },
      {
        path: 'smart-qa',
        name: 'SmartQA',
        component: SmartQaView,
        meta: resolveMenuMeta('/smart-qa'),
      },
      {
        path: 'analysis-warning',
        component: AnalysisWarningLayout,
        redirect: '/analysis-warning/overview',
        meta: resolveMenuMeta('/analysis-warning'),
        children: [
          {
            path: 'overview',
            name: 'AnalysisWarningOverview',
            component: AnalysisWarningOverview,
            meta: { title: '预警概览' },
          },
          {
            path: 'rule-management',
            name: 'WarningRuleManagement',
            component: WarningRuleManagement,
            meta: { title: '预警规则管理' },
          },
          {
            path: 'records',
            name: 'WarningRecordList',
            component: WarningRecordList,
            meta: { title: '预警记录' },
          },
        ],
      },
      {
        path: 'report-center',
        component: ReportCenterLayout,
        redirect: '/report-center/reports',
        meta: resolveMenuMeta('/report-center'),
        children: [
          {
            path: 'reports',
            name: 'ReportCenter',
            component: ReportCenterList,
            meta: { title: '正式报告' },
          },
          {
            path: 'briefings',
            name: 'BriefingCenter',
            component: BriefingCenterView,
            meta: { title: '简报中心' },
          },
        ],
      },
      {
        path: 'report-center/detail/:id',
        name: 'ReportDetail',
        component: ReportDetailView,
        meta: {
          title: '报告详情',
          activeMenu: '/report-center',
        },
      },
      {
        path: 'system-management',
        component: SystemManagementLayout,
        redirect: '/system-management/user-management',
        meta: resolveMenuMeta('/system-management'),
        children: [
          {
            path: 'user-management',
            name: 'SystemUserManagement',
            component: UserManagement,
            meta: { title: '用户管理' },
          },
          {
            path: 'role-permission',
            name: 'RolePermissionManagement',
            component: RolePermissionManagement,
            meta: { title: '角色权限管理' },
          },
          {
            path: 'system-config',
            name: 'SystemConfig',
            component: SystemConfigView,
            meta: { title: '系统配置' },
          },
        ],
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

let pageLoading: ReturnType<typeof ElLoading.service> | null = null
let loadingTimer: ReturnType<typeof setTimeout> | null = null

router.beforeEach((to, from) => {
  if (to.fullPath === from.fullPath) {
    return true
  }

  if (loadingTimer) {
    clearTimeout(loadingTimer)
    loadingTimer = null
  }

  pageLoading?.close()
  loadingTimer = setTimeout(() => {
    /**
     * 使用非锁定 loading，避免在某些复杂工作台首屏挂载时遮挡侧边菜单点击。
     */
    pageLoading = ElLoading.service({
      lock: false,
      text: '页面加载中...',
      background: 'rgba(242, 246, 251, 0.35)',
    })
    loadingTimer = null
  }, 120)

  return true
})

router.afterEach(() => {
  if (loadingTimer) {
    clearTimeout(loadingTimer)
    loadingTimer = null
  }
  pageLoading?.close()
  pageLoading = null
})

router.onError(() => {
  if (loadingTimer) {
    clearTimeout(loadingTimer)
    loadingTimer = null
  }
  pageLoading?.close()
  pageLoading = null
})

export default router
