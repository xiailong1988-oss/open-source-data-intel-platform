import type { TileLayer } from 'leaflet'
import type { DashboardCockpitBasemap } from '../../types/dashboardCockpit'

export type CockpitMapProviderKind = 'amap' | 'osm' | 'fallback' | 'baidu'
export type CockpitMapSurfaceTone = 'online-light' | 'online-dark' | 'fallback-dark'

export interface CockpitTileProviderContext {
  onTileError: () => void
}

/**
 * 首页驾驶舱当前只对“底图来源”做 provider 抽象，
 * 这样可以保留现有 Leaflet 叠加层、点位和联动结构，
 * 同时把高德 / OSM / 本地回退之间的差异收拢到一处。
 */
export interface CockpitTileProviderDescriptor {
  id: DashboardCockpitBasemap
  label: string
  providerKind: CockpitMapProviderKind
  tone: CockpitMapSurfaceTone
  mode: 'online' | 'fallback' | 'reserved'
  description: string
  requiresKey?: boolean
  keyConfigured?: boolean
  createTileLayer?: (context: CockpitTileProviderContext) => TileLayer | null
}

export interface CockpitMapProviderFactoryState {
  defaultBasemap: DashboardCockpitBasemap
  availableBasemaps: DashboardCockpitBasemap[]
  reservedProviders: Array<{
    kind: CockpitMapProviderKind
    envKey: string
    label: string
    note: string
  }>
}
