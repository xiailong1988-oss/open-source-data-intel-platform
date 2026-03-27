export type SourceCatalogCategory = '政务数据' | '产业数据' | '互联网公开信息' | '物联感知' | '科研协同'
export type SourceCatalogStatus = '稳定' | '关注' | '异常'
export type HealthStatus = '健康' | '延迟' | '异常'

export interface SourceCatalogItem {
  id: string
  name: string
  category: SourceCatalogCategory
  status: SourceCatalogStatus
  owner: string
  refreshAt: string
  coverage: string
  description: string
  relatedDataSourceId?: string
}

export interface SourceHealthItem {
  id: string
  sourceName: string
  status: HealthStatus
  freshnessMinutes: number
  failureCount: number
  successRate: number
  lastRefreshAt: string
  nextRefreshAt: string
  issueSummary: string
}

export interface SourceRefreshLogItem {
  id: string
  sourceName: string
  time: string
  action: string
  result: string
  duration: string
  operator: string
}
