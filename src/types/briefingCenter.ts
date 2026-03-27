export type BriefingType = '每日态势简报' | '专题简报' | '区域简报' | '事件专报'
export type BriefingStatus = '已生成' | '生成中' | '待审核'

export interface BriefingTemplateItem {
  id: string
  title: string
  type: BriefingType
  description: string
  scene: string
  suggestedTopic: string
}

export interface BriefingListItem {
  id: string
  title: string
  type: BriefingType
  topic: string
  region: string
  generatedAt: string
  owner: string
  status: BriefingStatus
  summary: string
  sources: string[]
  keyPoints: string[]
  reportId?: string
}
