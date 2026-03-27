export type WorkbenchCommandMode = '智能检索' | '智能问答'

export type WorkbenchTopicCode = 'all' | 'economy' | 'innovation'

export interface WorkbenchTopicOption {
  code: WorkbenchTopicCode
  label: string
  description: string
}

export interface WorkbenchUiConfig {
  commandModes: WorkbenchCommandMode[]
  topics: WorkbenchTopicOption[]
  quickQuestions: string[]
  placeholders: Record<WorkbenchCommandMode, string>
}

/**
 * 工作台摘要指标统一使用这组字段，
 * 让首页、预警中心、事件中心和后续模块页可以共享同一套指标栅格组件。
 */
export interface WorkbenchMetricSummaryItem {
  id: string
  label: string
  value: string | number
  hint: string
  action?: () => void
  clickable?: boolean
}
