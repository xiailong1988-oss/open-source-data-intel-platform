export interface PlatformMenuItem {
  name: string
  path: string
  title: string
  icon: string
  description: string
}

export const menuItems: PlatformMenuItem[] = [
  {
    name: 'Dashboard',
    path: '/dashboard',
    title: '综合态势',
    icon: 'DataAnalysis',
    description: '统一查看 GIS 态势、热点区域、重点事件和专题工作台入口。',
  },
  {
    name: 'DataAccess',
    path: '/data-access',
    title: '数据接入',
    icon: 'Connection',
    description: '管理数据源、采集任务、信源列表与数据健康状态。',
  },
  {
    name: 'DataGovernance',
    path: '/data-governance',
    title: '数据治理',
    icon: 'Operation',
    description: '查看治理概览、标准化数据和质量过程。',
  },
  {
    name: 'KnowledgeBuild',
    path: '/knowledge-build',
    title: '知识构建',
    icon: 'Share',
    description: '围绕实体、事件与知识资产沉淀构建统一研判空间。',
  },
  {
    name: 'SmartSearch',
    path: '/smart-search',
    title: '智能检索',
    icon: 'Search',
    description: '支持全文与语义检索、专题筛选和结果钻取。',
  },
  {
    name: 'SmartQA',
    path: '/smart-qa',
    title: '智能问答',
    icon: 'ChatDotRound',
    description: '融合知识、检索和专题上下文的问答入口。',
  },
  {
    name: 'AnalysisWarning',
    path: '/analysis-warning',
    title: '分析预警',
    icon: 'WarningFilled',
    description: '聚焦风险发现、规则配置、预警记录与闭环处置。',
  },
  {
    name: 'ReportCenter',
    path: '/report-center',
    title: '报告中心',
    icon: 'Document',
    description: '管理正式报告与简报输出，承接专题和态势的正式成果。',
  },
  {
    name: 'SystemManagement',
    path: '/system-management',
    title: '系统管理',
    icon: 'Setting',
    description: '统一管理用户、角色权限和平台运行配置。',
  },
]
