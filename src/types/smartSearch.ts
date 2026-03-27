export type SearchMode = '全文检索' | '语义检索'
export type SearchRiskLevel = '高' | '中' | '低'

export interface SearchResultItem {
  id: string
  title: string
  summary: string
  source: string
  time: string
  dataType: string
  region: string
  tags: string[]
  riskLevel: SearchRiskLevel
  content: string
  semanticHints: string[]
  relatedEntities?: string[]
  relatedEvents?: string[]
  reserved?: Record<string, unknown>
}

export interface SearchFilterOptions {
  dataTypes: string[]
  sources: string[]
  regions: string[]
  tags: string[]
  riskLevels: SearchRiskLevel[]
}
