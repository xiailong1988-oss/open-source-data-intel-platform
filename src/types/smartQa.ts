export interface QaSourceItem {
  title: string
  source: string
  publishedAt: string
}

export interface QaAnswer {
  coreAnswer: string
  evidenceSources: QaSourceItem[]
  relatedEntities: string[]
  recommendations: string[]
}

export interface QaHistoryItem {
  id: string
  question: string
  updatedAt: string
  answer: QaAnswer
}
