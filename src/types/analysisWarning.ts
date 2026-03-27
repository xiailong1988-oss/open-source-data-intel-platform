export interface WarningMetric {
  id: string
  label: string
  value: string
  changeText: string
}

export interface WarningTrendPoint {
  date: string
  warningCount: number
  highRiskCount: number
  abnormalGrowthIndex: number
}

export interface WarningTopicRankItem {
  topic: string
  heat: number
  riskLevel: '高' | '中' | '低'
}

export interface RiskDistributionItem {
  level: '高' | '中' | '低'
  value: number
}

export interface WarningOverviewRecord {
  id: string
  title: string
  level: '高' | '中' | '低'
  sourceObject: string
  triggerRule: string
  time: string
  status: '待处理' | '处理中' | '已处理'
}

export interface WarningRuleItem {
  id: string
  name: string
  ruleType: '关键词触发' | '热度突增触发' | '地区异常触发' | '敏感事件触发'
  triggerMethod: string
  alertLevel: '高' | '中' | '低'
  status: '启用' | '停用'
  updatedAt: string
  description: string
}

export interface WarningRecordTimelineItem {
  time: string
  content: string
}

export interface WarningRecordItem {
  id: string
  title: string
  level: '高' | '中' | '低'
  sourceObject: string
  triggerRule: string
  time: string
  status: '待处理' | '处理中' | '已处理'
  description: string
  triggerReason: string
  relatedContents: string[]
  suggestion: string
  timeline: WarningRecordTimelineItem[]
}
