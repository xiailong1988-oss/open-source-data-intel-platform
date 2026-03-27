<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import L, { type CircleMarker, type GeoJSON, type Map as LeafletMap, type Marker, type TileLayer } from 'leaflet'
import 'leaflet/dist/leaflet.css'
import type {
  DashboardCockpitBasemap,
  DashboardCockpitLayer,
  DashboardCockpitPoint,
  DashboardCockpitRiskLevel,
  DashboardCockpitZone,
} from '../../types/dashboardCockpit'

const props = defineProps<{
  district: string
  mapBounds: [[number, number], [number, number]]
  points: DashboardCockpitPoint[]
  zones: DashboardCockpitZone[]
  selectedPointId: string
  selectedZoneId: string
  highlightedPointIds: string[]
  activeLayer: DashboardCockpitLayer
  activeBasemap: DashboardCockpitBasemap
}>()

const emit = defineEmits<{
  (event: 'select-point', pointId: string): void
  (event: 'select-zone', zoneId: string): void
  (event: 'basemap-error'): void
}>()

const mapRoot = ref<HTMLDivElement | null>(null)

let map: LeafletMap | null = null
let baseTileLayer: TileLayer | null = null
let boundaryLayer: GeoJSON | null = null
let zoneLayerGroup: L.LayerGroup | null = null
let pointLayerGroup: L.LayerGroup | null = null
let resizeObserver: ResizeObserver | null = null
let boundaryGeoJson: GeoJSON.GeoJsonObject | null = null
let tileErrorReported = false

const pointRegistry = new Map<string, Marker>()
const zoneRegistry = new Map<string, CircleMarker>()

const latLngOf = (coords: [number, number]) => L.latLng(coords[1], coords[0])

const riskPalette: Record<DashboardCockpitRiskLevel, string> = {
  高: '#ff6b6b',
  中: '#ffb15c',
  低: '#4ed3a5',
}

const layerPalette: Record<DashboardCockpitLayer, string> = {
  风险预警: '#ff6b6b',
  突发事件: '#4fd9d2',
  热点事件: '#6fa7ff',
  重点关注: '#ffc56b',
}

/**
 * 首页地图只在首页使用 Leaflet，
 * 这样可以在不推翻全站 ECharts 体系的前提下引入在线底图与真实地图交互。
 */
const ensureMap = () => {
  if (!mapRoot.value || map) {
    return
  }

  map = L.map(mapRoot.value, {
    zoomControl: false,
    attributionControl: true,
    preferCanvas: true,
    minZoom: 11,
    maxZoom: 16,
    zoomSnap: 0.25,
    maxBounds: L.latLngBounds(props.mapBounds),
    maxBoundsViscosity: 0.85,
  })

  map.fitBounds(L.latLngBounds(props.mapBounds).pad(-0.08))
  L.control.zoom({ position: 'bottomleft' }).addTo(map)
  zoneLayerGroup = L.layerGroup().addTo(map)
  pointLayerGroup = L.layerGroup().addTo(map)

  resizeObserver = new ResizeObserver(() => {
    map?.invalidateSize()
  })
  resizeObserver.observe(mapRoot.value)
}

const createTileLayer = () =>
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    subdomains: ['a', 'b', 'c'],
    attribution: '&copy; OpenStreetMap contributors',
    className: 'cockpit-map__tile-layer',
  })

/**
 * 在线底图如果加载失败，必须能优雅回退到本地驾驶舱暗色底图，
 * 避免因为网络抖动让首页地图完全失效。
 */
const applyBasemap = () => {
  if (!map || !mapRoot.value) {
    return
  }

  mapRoot.value.classList.toggle('cockpit-map--online', props.activeBasemap === 'OSM 标准')
  mapRoot.value.classList.toggle('cockpit-map--dark', props.activeBasemap === '驾驶舱暗色')

  if (baseTileLayer) {
    map.removeLayer(baseTileLayer)
    baseTileLayer = null
  }

  if (props.activeBasemap !== 'OSM 标准') {
    return
  }

  tileErrorReported = false
  baseTileLayer = createTileLayer()
  baseTileLayer.on('tileerror', () => {
    if (!tileErrorReported) {
      tileErrorReported = true
      emit('basemap-error')
    }
  })
  baseTileLayer.addTo(map)
}

const renderBoundary = () => {
  if (!map || !boundaryGeoJson) {
    return
  }

  if (boundaryLayer) {
    map.removeLayer(boundaryLayer)
    boundaryLayer = null
  }

  boundaryLayer = L.geoJSON(boundaryGeoJson, {
    style: () => ({
      color: props.activeBasemap === 'OSM 标准' ? '#2c5f93' : '#7cb6ff',
      weight: props.activeBasemap === 'OSM 标准' ? 2.2 : 2.6,
      fillColor: props.activeBasemap === 'OSM 标准' ? '#3f7cb9' : '#173352',
      fillOpacity: props.activeBasemap === 'OSM 标准' ? 0.09 : 0.18,
      opacity: 0.95,
    }),
  }).addTo(map)
}

const createMarkerHtml = (point: DashboardCockpitPoint, highlighted: boolean) => {
  const classes = [
    'cockpit-map-marker',
    `cockpit-map-marker--${point.riskLevel}`,
    highlighted ? 'is-highlighted' : '',
    props.selectedPointId === point.id ? 'is-selected' : '',
  ]
    .filter(Boolean)
    .join(' ')

  return `
    <div class="${classes}">
      <span class="cockpit-map-marker__pulse"></span>
      <span class="cockpit-map-marker__dot" style="--marker-color:${riskPalette[point.riskLevel]}"></span>
    </div>
  `
}

const tooltipHtml = (point: DashboardCockpitPoint) => `
  <div class="cockpit-map-label">
    <strong>${point.shortLabel}</strong>
    <span>${point.area}</span>
  </div>
`

const popupHtml = (point: DashboardCockpitPoint) => `
  <div class="cockpit-map-callout">
    <span class="cockpit-map-callout__tag" style="background:${layerPalette[point.layer]}">${point.layer}</span>
    <strong>${point.title}</strong>
    <div class="cockpit-map-callout__meta">${point.area} · ${point.occurredAt} · ${point.status}</div>
    <p>${point.summary}</p>
  </div>
`

const drawZones = () => {
  if (!zoneLayerGroup) {
    return
  }

  const zoneGroup = zoneLayerGroup
  zoneGroup.clearLayers()
  zoneRegistry.clear()

  props.zones.forEach((zone) => {
    const zoneMarker = L.circleMarker(latLngOf(zone.center), {
      radius: Math.max(16, Math.min(26, zone.heat / 4)),
      color: props.selectedZoneId === zone.id ? '#f8fbff' : 'rgba(255,255,255,0.22)',
      weight: props.selectedZoneId === zone.id ? 2.4 : 1.1,
      fillColor: zone.heat >= 92 ? '#ff7d73' : zone.heat >= 84 ? '#ffb561' : '#68a7ff',
      fillOpacity: props.selectedZoneId === zone.id ? 0.42 : 0.22,
    })

    zoneMarker.bindTooltip(zone.name, {
      permanent: zone.heat >= 90,
      direction: 'top',
      offset: [0, -8],
      className: 'cockpit-map-zone-tooltip',
      opacity: 1,
    })

    zoneMarker.on('click', () => emit('select-zone', zone.id))
    zoneMarker.addTo(zoneGroup)
    zoneRegistry.set(zone.id, zoneMarker)
  })
}

const drawPoints = () => {
  if (!pointLayerGroup) {
    return
  }

  const pointGroup = pointLayerGroup
  pointGroup.clearLayers()
  pointRegistry.clear()

  const highlightedIds = new Set(props.highlightedPointIds)

  props.points.forEach((point) => {
    const marker = L.marker(latLngOf(point.coords), {
      icon: L.divIcon({
        className: '',
        html: createMarkerHtml(point, highlightedIds.has(point.id)),
        iconSize: [28, 28],
        iconAnchor: [14, 14],
      }),
      keyboard: false,
      riseOnHover: true,
    })

    if (highlightedIds.has(point.id) || props.selectedPointId === point.id) {
      marker.bindTooltip(tooltipHtml(point), {
        permanent: true,
        direction: 'top',
        offset: [0, -16],
        className: 'cockpit-map-tooltip',
        opacity: 1,
      })
    }

    marker.bindPopup(popupHtml(point), {
      closeButton: false,
      autoClose: false,
      className: 'cockpit-map-popup',
      offset: [0, -12],
      minWidth: 248,
      maxWidth: 280,
    })

    marker.on('click', () => emit('select-point', point.id))
    marker.addTo(pointGroup)
    pointRegistry.set(point.id, marker)
  })
}

const syncFocus = async () => {
  if (!map) {
    return
  }

  pointRegistry.forEach((marker, pointId) => {
    if (props.selectedPointId === pointId) {
      marker.openPopup()
    } else {
      marker.closePopup()
    }
  })

  if (props.selectedPointId) {
    const marker = pointRegistry.get(props.selectedPointId)
    if (marker) {
      map.flyTo(marker.getLatLng(), Math.max(map.getZoom(), 12.5), { duration: 0.45 })
      marker.openPopup()
      return
    }
  }

  if (props.selectedZoneId) {
    const zoneMarker = zoneRegistry.get(props.selectedZoneId)
    if (zoneMarker) {
      map.flyTo(zoneMarker.getLatLng(), Math.max(map.getZoom(), 12), { duration: 0.45 })
      return
    }
  }

  await nextTick()
  map.fitBounds(L.latLngBounds(props.mapBounds).pad(-0.08))
}

const refreshMap = async () => {
  if (!map) {
    return
  }

  applyBasemap()
  renderBoundary()
  drawZones()
  drawPoints()
  await syncFocus()
}

const loadBoundary = async () => {
  const response = await fetch('/maps/haidian.json')
  if (!response.ok) {
    throw new Error(`海淀区边界加载失败：${response.status}`)
  }

  boundaryGeoJson = await response.json()
}

onMounted(async () => {
  ensureMap()
  await loadBoundary()
  await refreshMap()
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  resizeObserver = null
  map?.remove()
  map = null
})

watch(
  () => [props.activeBasemap, props.activeLayer, props.selectedPointId, props.selectedZoneId, props.points, props.zones, props.highlightedPointIds],
  async () => {
    await refreshMap()
  },
  { deep: true },
)
</script>

<template>
  <div ref="mapRoot" class="cockpit-map cockpit-map--dark" />
</template>

<style scoped>
.cockpit-map {
  position: relative;
  height: 100%;
  min-height: 680px;
  overflow: hidden;
  border-radius: 28px;
  background:
    radial-gradient(circle at 18% 18%, rgba(75, 132, 214, 0.22), transparent 34%),
    radial-gradient(circle at 76% 22%, rgba(72, 189, 165, 0.12), transparent 30%),
    linear-gradient(180deg, rgba(7, 19, 32, 0.96) 0%, rgba(8, 14, 24, 0.98) 100%);
}

.cockpit-map--online {
  background: #d5dbe3;
}

.cockpit-map:deep(.leaflet-container) {
  height: 100%;
  width: 100%;
  background: transparent;
  font-family: inherit;
}

.cockpit-map:deep(.leaflet-control-zoom) {
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 18px 36px rgba(4, 10, 18, 0.28);
}

.cockpit-map:deep(.leaflet-control-zoom a) {
  background: rgba(7, 17, 29, 0.82);
  color: #eff6ff;
}

.cockpit-map:deep(.leaflet-control-attribution) {
  background: rgba(8, 16, 28, 0.74);
  color: rgba(234, 242, 255, 0.78);
  font-size: 11px;
}

.cockpit-map:deep(.leaflet-control-attribution a) {
  color: #8ebdff;
}

.cockpit-map:deep(.cockpit-map-popup .leaflet-popup-content-wrapper) {
  border: 1px solid rgba(111, 169, 255, 0.28);
  border-radius: 20px;
  background: rgba(7, 16, 28, 0.95);
  box-shadow: 0 28px 48px rgba(3, 10, 18, 0.35);
  color: #eff5ff;
}

.cockpit-map:deep(.cockpit-map-popup .leaflet-popup-tip) {
  background: rgba(7, 16, 28, 0.95);
}

.cockpit-map:deep(.leaflet-popup-content) {
  margin: 0;
}

.cockpit-map:deep(.cockpit-map-callout) {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px 16px;
}

.cockpit-map:deep(.cockpit-map-callout__tag) {
  display: inline-flex;
  width: fit-content;
  border-radius: 999px;
  padding: 2px 8px;
  color: #f7fbff;
  font-size: 11px;
  font-weight: 700;
}

.cockpit-map:deep(.cockpit-map-callout strong) {
  font-size: 15px;
  line-height: 1.45;
}

.cockpit-map:deep(.cockpit-map-callout__meta) {
  color: rgba(222, 232, 245, 0.74);
  font-size: 12px;
}

.cockpit-map:deep(.cockpit-map-callout p) {
  margin: 0;
  color: #d6e3f3;
  line-height: 1.7;
  font-size: 13px;
}

.cockpit-map:deep(.cockpit-map-tooltip .leaflet-tooltip),
.cockpit-map:deep(.cockpit-map-zone-tooltip .leaflet-tooltip) {
  border: 1px solid rgba(117, 171, 245, 0.26);
  border-radius: 14px;
  background: rgba(7, 16, 28, 0.9);
  box-shadow: 0 18px 40px rgba(3, 10, 18, 0.3);
  color: #eff6ff;
}

.cockpit-map:deep(.cockpit-map-tooltip .leaflet-tooltip-top::before),
.cockpit-map:deep(.cockpit-map-zone-tooltip .leaflet-tooltip-top::before) {
  border-top-color: rgba(7, 16, 28, 0.9);
}

.cockpit-map:deep(.cockpit-map-label) {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 112px;
}

.cockpit-map:deep(.cockpit-map-label strong) {
  font-size: 12px;
}

.cockpit-map:deep(.cockpit-map-label span) {
  color: rgba(222, 232, 245, 0.72);
  font-size: 11px;
}

.cockpit-map:deep(.cockpit-map-marker) {
  position: relative;
  display: flex;
  height: 28px;
  width: 28px;
  align-items: center;
  justify-content: center;
}

.cockpit-map:deep(.cockpit-map-marker__pulse) {
  position: absolute;
  inset: 0;
  border-radius: 999px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.4) 0%, transparent 68%);
  opacity: 0;
}

.cockpit-map:deep(.cockpit-map-marker.is-highlighted .cockpit-map-marker__pulse),
.cockpit-map:deep(.cockpit-map-marker.is-selected .cockpit-map-marker__pulse) {
  animation: cockpit-marker-pulse 2.4s ease-out infinite;
  opacity: 1;
}

.cockpit-map:deep(.cockpit-map-marker__dot) {
  position: relative;
  z-index: 1;
  display: block;
  height: 12px;
  width: 12px;
  border: 3px solid rgba(245, 250, 255, 0.88);
  border-radius: 999px;
  background: var(--marker-color);
  box-shadow: 0 0 0 3px rgba(8, 16, 28, 0.4), 0 14px 24px rgba(5, 12, 20, 0.35);
}

.cockpit-map:deep(.cockpit-map-marker.is-selected .cockpit-map-marker__dot) {
  transform: scale(1.18);
}

@keyframes cockpit-marker-pulse {
  0% {
    transform: scale(0.8);
    opacity: 0.85;
  }

  70% {
    transform: scale(1.9);
    opacity: 0;
  }

  100% {
    opacity: 0;
  }
}
</style>
