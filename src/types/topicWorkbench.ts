import type { DashboardSpatialOverview } from './dashboard'

export type TopicWorkbenchCode = 'economy' | 'innovation'
export type TopicAlertLevel = '高' | '中' | '低'

export interface TopicWorkbenchMetric {
  id: string
  label: string
  value: string | number
  hint: string
}

export interface TopicSignalPoint {
  label: string
  value: number
}

export interface TopicFocusEvent {
  id: string
  title: string
  region: string
  time: string
  status: string
  level: TopicAlertLevel
  summary: string
  keyword: string
}

export interface TopicQuestionSuggestion {
  id: string
  question: string
  description: string
}

export interface TopicBriefingReference {
  id: string
  title: string
  type: string
  generatedAt: string
  status: string
  summary: string
  reportId?: string
}

export interface TopicWorkbenchData {
  code: TopicWorkbenchCode
  eyebrow: string
  title: string
  description: string
  heroHint: string
  metrics: TopicWorkbenchMetric[]
  signalTitle: string
  signalSubtitle: string
  signalData: TopicSignalPoint[]
  overview: DashboardSpatialOverview
  focusEvents: TopicFocusEvent[]
  recommendedQuestions: TopicQuestionSuggestion[]
  briefingReferences: TopicBriefingReference[]
  briefingKeyword: string
}
