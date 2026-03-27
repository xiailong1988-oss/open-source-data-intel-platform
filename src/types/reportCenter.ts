export type ReportType = '日报' | '周报' | '专题报告' | '快报'
export type ReportStatus = '已生成' | '生成中' | '待审核'

export interface ReportListItem {
  id: string
  name: string
  type: ReportType
  generatedAt: string
  creator: string
  status: ReportStatus
}

export interface ReportTrendPoint {
  date: string
  value: number
}

export interface ReportKeyEventItem {
  eventName: string
  level: '高' | '中' | '低'
  impact: string
  status: string
}

export interface ReportDetail {
  id: string
  title: string
  type: ReportType
  generatedAt: string
  creator: string
  version: string
  summary: string
  coreFindings: string[]
  trendData: ReportTrendPoint[]
  keyEvents: ReportKeyEventItem[]
  riskJudgment: string
  suggestions: string[]
  relatedEntities?: string[]
  referencedContents?: string[]
}
