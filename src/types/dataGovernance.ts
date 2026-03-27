export type DataGovernanceTrendPoint = {
  date: string
  qualityScore: number
  standardizationRate: number
  abnormalCount: number
}

export type PipelineStatusPoint = {
  stage: string
  total: number
  success: number
  failed: number
}

export type AbnormalDataItem = {
  id: string
  title: string
  source: string
  abnormalType: string
  severity: '高' | '中' | '低'
  discoveredAt: string
  status: '待处理' | '处理中' | '已修复'
  owner: string
}

export type GovernanceMetric = {
  id: string
  label: string
  value: string
  compareText: string
  trend: 'up' | 'down'
}

export type StandardizedContentStatus = '已标准化' | '待复核' | '已归档'
export type StandardizedContentType = '政策文件' | '舆情资讯' | '业务公告' | '运行日志' | '事件简报'

export interface StandardizedContentItem {
  id: string
  title: string
  contentType: StandardizedContentType
  source: string
  publishedAt: string
  region: string
  tagCount: number
  entityCount: number
  status: StandardizedContentStatus
  summary: string
  content: string
  rawContent: string
  standardizedContent: string
  sourceLink: string
  tags: string[]
  entities: string[]
  extAttributes: Record<string, unknown>
}
