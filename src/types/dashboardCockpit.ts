import type { DashboardQuickLink, DashboardRecentFocus } from './dashboard'

export type DashboardCockpitLayer = '风险预警' | '突发事件' | '热点事件' | '重点关注'
export type DashboardCockpitRiskLevel = '高' | '中' | '低'
export type DashboardCockpitPointKind = 'warning' | 'emergency' | 'hotspot' | 'watch'
export type DashboardCockpitLinkKind = 'warning' | 'event' | 'report' | 'entity' | 'search'
export type DashboardCockpitDisplayMode = '驾驶舱降噪' | '扩展标注'
export type DashboardCockpitBasemap = '高德标准' | '高德深色' | 'OSM 标准' | '驾驶舱暗色'
export type DashboardSystemModuleId = 'collection' | 'storage' | 'resource' | 'agents'
export type DashboardBusinessBoardId =
  | 'industrial-development'
  | 'tech-innovation'
  | 'featured-economy'
  | 'public-service'
  | 'safety-governance'
  | 'urban-rural-construction'

export interface DashboardCockpitHeadline {
  id: string
  label: string
  value: string
  hint: string
}

export interface DashboardCockpitZone {
  id: string
  name: string
  center: [number, number]
  weight: number
  heat: number
  alertCount: number
  eventCount: number
  description: string
  keyword: string
  emphasis: string
  featuredPointIds: string[]
}

export interface DashboardCockpitLink {
  id: string
  title: string
  kind: DashboardCockpitLinkKind
  path?: string
  query?: Record<string, string>
}

export interface DashboardCockpitPoint {
  id: string
  title: string
  shortLabel: string
  layer: DashboardCockpitLayer
  kind: DashboardCockpitPointKind
  area: string
  coords: [number, number]
  riskLevel: DashboardCockpitRiskLevel
  priority: number
  category: string
  occurredAt: string
  status: string
  summary: string
  description: string
  keywords: string[]
  relatedLinks: DashboardCockpitLink[]
  detailTarget: DashboardCockpitLink
}

export interface DashboardCockpitTickerItem {
  id: string
  layer: DashboardCockpitLayer
  title: string
  time: string
  area: string
  level: DashboardCockpitRiskLevel
  status: string
  summary: string
  relatedPointId: string
}

export interface DashboardCockpitTopicSwitch {
  id: string
  layer: DashboardCockpitLayer
  title: string
  description: string
  keyword: string
  relatedPointIds: string[]
}

export interface DashboardCockpitLayerProfile {
  summary: string
  instruction: string
  priorityLabel: string
}

export interface DashboardSystemMetricItem {
  id: string
  label: string
  value: number
  suffix?: string
  status?: 'online' | 'warning' | 'offline' | 'neutral'
  detail: string
}

export interface DashboardSystemModule {
  id: DashboardSystemModuleId
  title: string
  eyebrow: string
  accent: 'cyan' | 'blue' | 'amber' | 'green'
  detailTarget: DashboardCockpitLink
  metrics: DashboardSystemMetricItem[]
}

export interface DashboardBusinessBoardMetric {
  id: DashboardBusinessBoardId
  title: string
  eyebrow: string
  currentValue: number
  unit?: string
  increment: string
  trend: 'up' | 'flat' | 'down'
  riskLevel: DashboardCockpitRiskLevel
  status: string
  detailTarget: DashboardCockpitLink
  highlights: Array<{
    label: string
    value: string
  }>
}

export interface DashboardMapHighlight {
  id: string
  pointId: string
  title: string
  time: string
  area: string
  level: DashboardCockpitRiskLevel
  status: string
  preferredAnchor: 'top' | 'bottom'
  detailTarget: DashboardCockpitLink
}

export interface DashboardMapProjectedPoint {
  pointId: string
  x: number
  y: number
}

export interface DashboardMapLayoutSnapshot {
  width: number
  height: number
  points: DashboardMapProjectedPoint[]
}

export interface DashboardCockpitOverview {
  district: string
  mapName: string
  baseMapLabel: string
  defaultBasemap: DashboardCockpitBasemap
  baseMapOptions: DashboardCockpitBasemap[]
  mapCenter: [number, number]
  mapZoom: number
  mapBounds: [[number, number], [number, number]]
  layers: DashboardCockpitLayer[]
  headlines: DashboardCockpitHeadline[]
  zones: DashboardCockpitZone[]
  points: DashboardCockpitPoint[]
  ticker: DashboardCockpitTickerItem[]
  topics: DashboardCockpitTopicSwitch[]
  quickLinks: DashboardQuickLink[]
  recentFocuses: DashboardRecentFocus[]
  layerProfiles: Record<DashboardCockpitLayer, DashboardCockpitLayerProfile>
  defaultTips: string[]
  systemMetrics: DashboardSystemModule[]
  businessBoardMetrics: DashboardBusinessBoardMetric[]
  mapHighlights: DashboardMapHighlight[]
}
