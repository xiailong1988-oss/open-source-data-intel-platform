import L from 'leaflet'
import type { DashboardCockpitBasemap } from '../../types/dashboardCockpit'
import type { CockpitMapProviderFactoryState, CockpitTileProviderDescriptor } from './types'

const amapKey = import.meta.env.VITE_AMAP_KEY?.trim() ?? ''

const buildQuery = (base: string) => (amapKey ? `${base}&key=${encodeURIComponent(amapKey)}` : base)

const createAmapStandardProvider = (): CockpitTileProviderDescriptor => ({
  id: '高德标准',
  label: '高德标准',
  providerKind: 'amap',
  tone: 'online-light',
  mode: 'online',
  description: amapKey
    ? '高德在线底图主方案，优先用于首页驾驶舱。'
    : '高德在线底图主方案；当前环境未配置 key，先使用兼容瓦片方式运行。',
  requiresKey: true,
  keyConfigured: Boolean(amapKey),
  createTileLayer: () =>
    L.tileLayer(buildQuery('https://webrd0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=7&x={x}&y={y}&z={z}'), {
      subdomains: ['1', '2', '3', '4'],
      attribution: '&copy; 高德地图',
      className: 'cockpit-map__tile-layer cockpit-map__tile-layer--amap',
      maxZoom: 19,
    }),
})

const createAmapDarkProvider = (): CockpitTileProviderDescriptor => ({
  id: '高德深色',
  label: '高德深色',
  providerKind: 'amap',
  tone: 'online-dark',
  mode: 'online',
  description: amapKey
    ? '高德在线深色底图，适合作为夜间驾驶舱或深色主题底图。'
    : '高德在线深色底图；当前环境未配置 key，先使用兼容瓦片方式运行。',
  requiresKey: true,
  keyConfigured: Boolean(amapKey),
  createTileLayer: () =>
    L.tileLayer(buildQuery('https://webrd0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}'), {
      subdomains: ['1', '2', '3', '4'],
      attribution: '&copy; 高德地图',
      className: 'cockpit-map__tile-layer cockpit-map__tile-layer--amap-dark',
      maxZoom: 19,
    }),
})

const createOsmProvider = (): CockpitTileProviderDescriptor => ({
  id: 'OSM 标准',
  label: 'OSM 标准',
  providerKind: 'osm',
  tone: 'online-light',
  mode: 'online',
  description: '仅作为开发调试或在线底图降级使用，不再作为首页默认主底图。',
  createTileLayer: () =>
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      subdomains: ['a', 'b', 'c'],
      attribution: '&copy; OpenStreetMap contributors',
      className: 'cockpit-map__tile-layer cockpit-map__tile-layer--osm',
      maxZoom: 19,
    }),
})

const createDarkFallbackProvider = (): CockpitTileProviderDescriptor => ({
  id: '驾驶舱暗色',
  label: '驾驶舱暗色',
  providerKind: 'fallback',
  tone: 'fallback-dark',
  mode: 'fallback',
  description: '在线底图不可用时的本地深色回退底图，保证首页地图不因网络问题失效。',
  createTileLayer: () => null,
})

const providerRegistry: Record<DashboardCockpitBasemap, CockpitTileProviderDescriptor> = {
  高德标准: createAmapStandardProvider(),
  高德深色: createAmapDarkProvider(),
  'OSM 标准': createOsmProvider(),
  驾驶舱暗色: createDarkFallbackProvider(),
}

const providerOrder: DashboardCockpitBasemap[] = ['高德标准', '高德深色', 'OSM 标准', '驾驶舱暗色']

export const reservedCockpitProviders = [
  {
    kind: 'baidu',
    envKey: 'VITE_BMAP_AK',
    label: '百度地图',
    note: '本轮仅做 provider 预留，不在首页主实现中启用。',
  },
] as const

export const getCockpitBasemapProvider = (id: DashboardCockpitBasemap) => providerRegistry[id]

export const getCockpitMapProviderFactoryState = (): CockpitMapProviderFactoryState => ({
  defaultBasemap: '高德标准',
  availableBasemaps: providerOrder,
  reservedProviders: [...reservedCockpitProviders],
})
