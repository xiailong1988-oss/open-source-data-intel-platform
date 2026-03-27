export type TrendDirection = 'up' | 'down' | 'flat'
export type TrendPolarity = 'higher-better' | 'lower-better'
export type AlertLevel = '紧急' | '高' | '中' | '低'
export type TaskStatus = '运行中' | '已完成' | '待执行' | '失败'
export type ReportStatus = '已发布' | '审核中' | '草稿'

export interface DashboardMetric {
  id: string
  label: string
  value: number
  unit?: string
  delta: number
  trend: TrendDirection
  trendPolarity?: TrendPolarity
  compareText: string
  description: string
  reserved?: Record<string, unknown>
}

export interface DashboardPieItem {
  name: string
  value: number
  code: string
  reserved?: Record<string, unknown>
}

export interface DashboardTrendPoint {
  date: string
  total: number
  dailyIncrease: number
  governed: number
  reserved?: Record<string, unknown>
}

export interface DashboardTopicRank {
  topic: string
  heat: number
  growthRate: number
  sourceCount: number
  riskLevel: AlertLevel
  reserved?: Record<string, unknown>
}

export interface DashboardAlert {
  id: string
  level: AlertLevel
  title: string
  source: string
  status: '待处置' | '处理中' | '已闭环'
  createdAt: string
  owner: string
  region: string
  reserved?: Record<string, unknown>
}

export interface DashboardCollectionTask {
  id: string
  taskName: string
  sourceType: string
  status: TaskStatus
  records: number
  duration: string
  lastRunAt: string
  nextRunAt: string
  owner: string
  reserved?: Record<string, unknown>
}

export interface DashboardHotTopic {
  id: string
  title: string
  category: string
  heat: number
  mentions: number
  updatedAt: string
  department: string
  reserved?: Record<string, unknown>
}

export interface DashboardReport {
  id: string
  name: string
  type: string
  status: ReportStatus
  author: string
  generatedAt: string
  pageCount: number
  summary: string
  reserved?: Record<string, unknown>
}

export type DashboardSpatialLayer = '综合' | '风险预警' | '重点事件' | '专题研判'
export type DashboardSpatialRiskLevel = '高' | '中' | '低'
export type DashboardSpatialLinkKind = 'warning' | 'event' | 'report' | 'entity'

export interface DashboardSpatialRegionMetric {
  heat: number
  objectCount: number
  alertCount: number
  eventCount: number
  mainTopic: string
}

export interface DashboardSpatialRegion {
  id: string
  adcode: string
  name: string
  center: [number, number]
  summary: string
  emphasis?: string
  todayFocus: boolean
  layerMetrics: Record<DashboardSpatialLayer, DashboardSpatialRegionMetric>
}

export interface DashboardSpatialLink {
  id: string
  title: string
  kind: DashboardSpatialLinkKind
}

export interface DashboardSpatialPoint {
  id: string
  name: string
  layer: DashboardSpatialLayer
  objectType: string
  category: string
  region: string
  coords: [number, number]
  location?: string
  riskLevel?: DashboardSpatialRiskLevel
  updatedAt: string
  status: string
  summary: string
  topic: string
  keywords: string[]
  relatedWarnings: DashboardSpatialLink[]
  relatedEvents: DashboardSpatialLink[]
  relatedReports: DashboardSpatialLink[]
  relatedEntities: DashboardSpatialLink[]
}

export interface DashboardSpatialHeadline {
  id: string
  label: string
  value: string
  hint: string
}

export interface DashboardSpatialEventThread {
  id: string
  title: string
  region: string
  time: string
  status: string
  summary: string
  keyword: string
  layer: DashboardSpatialLayer
  level?: AlertLevel | DashboardSpatialRiskLevel
  relatedPointId?: string
}

export interface DashboardSpatialTopicDeck {
  id: string
  title: string
  region: string
  heat: number
  description: string
  keyword: string
  layer: DashboardSpatialLayer
}

export interface DashboardQuickLink {
  id: string
  title: string
  description: string
  icon: string
  path: string
  query?: Record<string, string>
}

export interface DashboardRecentFocus {
  id: string
  title: string
  meta: string
  path: string
  query?: Record<string, string>
}

export interface DashboardSpatialDefaultSummary {
  briefing: string
  instructions: string[]
  keyRegions: string[]
  hotspotSummary: string
  layerBriefs: Record<DashboardSpatialLayer, string>
}

export interface DashboardSpatialOverview {
  mapName: string
  baseMapLabel: string
  mapCenter: [number, number]
  mapZoom: number
  layers: DashboardSpatialLayer[]
  headlines: DashboardSpatialHeadline[]
  regions: DashboardSpatialRegion[]
  points: DashboardSpatialPoint[]
  events: DashboardSpatialEventThread[]
  topics: DashboardSpatialTopicDeck[]
  quickLinks: DashboardQuickLink[]
  recentFocuses: DashboardRecentFocus[]
  defaultSummary: DashboardSpatialDefaultSummary
}

export interface DashboardMockData {
  metrics: DashboardMetric[]
  sourceDistribution: DashboardPieItem[]
  dataTypeDistribution: DashboardPieItem[]
  growthTrend: DashboardTrendPoint[]
  topicRanking: DashboardTopicRank[]
  latestAlerts: DashboardAlert[]
  recentTasks: DashboardCollectionTask[]
  hotTopics: DashboardHotTopic[]
  latestReports: DashboardReport[]
  spatialOverview: DashboardSpatialOverview
}
