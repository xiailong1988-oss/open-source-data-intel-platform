export interface KnowledgeMetric {
  id: string
  label: string
  value: string
  change: string
}

export interface EntityTypeDistributionItem {
  type: string
  value: number
}

export interface RelationTypeDistributionItem {
  type: string
  value: number
}

export interface HotEventItem {
  id: string
  eventName: string
  eventType: string
  heat: number
  riskLevel: '高' | '中' | '低'
  updatedAt: string
}

export interface EntityItem {
  id: string
  entityName: string
  entityType: '人物' | '机构' | '地点' | '项目' | '产品'
  standardName: string
  aliasCount: number
  relatedContentCount: number
  confidence: number
  updatedAt: string
  aliases: string[]
  relatedContents: string[]
  relatedEvents: string[]
  relatedRelations: string[]
  basicInfo: Record<string, string>
}

export interface EventTimelineItem {
  time: string
  content: string
}

export interface EventItem {
  id: string
  eventName: string
  eventType: '产业事件' | '公共安全' | '舆情事件' | '运营事件'
  location: string
  startTime: string
  riskLevel: '高' | '中' | '低'
  relatedContentCount: number
  status: '研判中' | '已处置' | '持续跟踪'
  summary: string
  timeline: EventTimelineItem[]
  involvedEntities: string[]
  relatedContents: string[]
  riskExplanation: string
}
