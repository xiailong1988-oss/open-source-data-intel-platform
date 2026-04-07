<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import L, { type CircleMarker, type GeoJSON, type LayerGroup, type Map as LeafletMap, type Marker, type TileLayer } from 'leaflet'
import type { Feature, FeatureCollection, MultiPolygon, Polygon, Position } from 'geojson'
import 'leaflet/dist/leaflet.css'
import { getCockpitBasemapProvider } from '../../lib/map/mapProviderFactory'
import type {
  DashboardCockpitBasemap,
  DashboardCockpitLayer,
  DashboardCockpitPoint,
  DashboardCockpitRegion,
  DashboardCockpitRiskLevel,
  DashboardCockpitZone,
  DashboardMapLayoutSnapshot,
} from '../../types/dashboardCockpit'

const props = defineProps<{
  district: string
  mapBounds: [[number, number], [number, number]]
  currentRegion: DashboardCockpitRegion
  points: DashboardCockpitPoint[]
  zones: DashboardCockpitZone[]
  selectedPointId: string
  selectedZoneId: string
  hoveredPointId?: string
  highlightedPointIds: string[]
  activeLayer: DashboardCockpitLayer
  activeBasemap: DashboardCockpitBasemap
}>()

const emit = defineEmits<{
  (event: 'select-point', pointId: string): void
  (event: 'select-zone', zoneId: string): void
  (event: 'basemap-error'): void
  (event: 'layout-change', layout: DashboardMapLayoutSnapshot): void
}>()

const mapRoot = ref<HTMLDivElement | null>(null)

let map: LeafletMap | null = null
let baseTileLayer: TileLayer | null = null
let boundaryLayer: GeoJSON | null = null
let boundaryMaskLayer: L.Polygon | null = null
let regionLayerGroup: LayerGroup | null = null
let zoneLayerGroup: LayerGroup | null = null
let pointLayerGroup: LayerGroup | null = null
let regionLabelMarker: Marker | null = null
let resizeObserver: ResizeObserver | null = null
let boundaryGeoJson: FeatureCollection<Polygon | MultiPolygon> | null = null
let tileErrorReported = false
let hasInitialFocusApplied = false
let hasPendingViewportFocus = false

const pointRegistry = new Map<string, Marker>()
const zoneRegistry = new Map<string, CircleMarker>()

const districtBounds = () => L.latLngBounds(props.mapBounds)
const regionBounds = () => L.latLngBounds(props.currentRegion.bounds)
const latLngOf = (coords: [number, number]) => L.latLng(coords[1], coords[0])
const hasRenderableViewport = () =>
  Boolean(map && mapRoot.value && mapRoot.value.clientWidth > 80 && mapRoot.value.clientHeight > 80)

const ensureRenderableViewport = () => {
  if (!map || !hasRenderableViewport()) {
    hasPendingViewportFocus = true
    return false
  }

  map.invalidateSize()
  hasPendingViewportFocus = false
  return true
}

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
 * 首页地图只接受程序化视角切换。
 * 用户仍可点击点位和片区，但不能自由拖拽或缩放视图。
 */
const ensureMap = () => {
  if (!mapRoot.value || map) {
    return
  }

  map = L.map(mapRoot.value, {
    zoomControl: false,
    attributionControl: true,
    preferCanvas: true,
    minZoom: 10,
    maxZoom: 18,
    zoomSnap: 0.25,
    dragging: false,
    scrollWheelZoom: false,
    doubleClickZoom: false,
    touchZoom: false,
    boxZoom: false,
    keyboard: false,
    zoomAnimation: true,
    fadeAnimation: true,
    markerZoomAnimation: true,
  })

  map.setView(districtBounds().getCenter(), 11)
  regionLayerGroup = L.layerGroup().addTo(map)
  zoneLayerGroup = L.layerGroup().addTo(map)
  pointLayerGroup = L.layerGroup().addTo(map)

  resizeObserver = new ResizeObserver(() => {
    if (hasRenderableViewport()) {
      map?.invalidateSize()
    }
    emitPointLayout()

    if (!hasPendingViewportFocus || !hasRenderableViewport()) {
      return
    }

    if (props.selectedPointId || props.selectedZoneId) {
      void syncFocus()
      return
    }

    if (!hasInitialFocusApplied) {
      hasInitialFocusApplied = true
    }

    void focusRegion(false)
  })
  resizeObserver.observe(mapRoot.value)
}

const getBoundaryHoles = () => {
  if (!boundaryGeoJson) {
    return []
  }

  const positionToLatLng = ([lng, lat]: Position) => L.latLng(lat, lng)

  return boundaryGeoJson.features.flatMap((feature: Feature<Polygon | MultiPolygon>) => {
    if (feature.geometry.type === 'Polygon') {
      return feature.geometry.coordinates.map((ring) => ring.map(positionToLatLng))
    }

    if (feature.geometry.type === 'MultiPolygon') {
      return feature.geometry.coordinates.flatMap((polygon) => polygon.map((ring) => ring.map(positionToLatLng)))
    }

    return []
  })
}

const updateSurfaceTone = () => {
  if (!mapRoot.value) {
    return
  }

  const provider = getCockpitBasemapProvider(props.activeBasemap)
  mapRoot.value.classList.toggle('cockpit-map--online-light', provider.tone === 'online-light')
  mapRoot.value.classList.toggle('cockpit-map--online-dark', provider.tone === 'online-dark')
  mapRoot.value.classList.toggle('cockpit-map--fallback-dark', provider.tone === 'fallback-dark')
}

const applyBasemap = () => {
  if (!map || !mapRoot.value) {
    return
  }

  updateSurfaceTone()

  if (baseTileLayer) {
    map.removeLayer(baseTileLayer)
    baseTileLayer = null
  }

  const provider = getCockpitBasemapProvider(props.activeBasemap)
  if (!provider.createTileLayer) {
    return
  }

  tileErrorReported = false
  baseTileLayer = provider.createTileLayer({
    onTileError: () => {
      if (!tileErrorReported) {
        tileErrorReported = true
        emit('basemap-error')
      }
    },
  })

  if (!baseTileLayer) {
    return
  }

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

  if (boundaryMaskLayer) {
    map.removeLayer(boundaryMaskLayer)
    boundaryMaskLayer = null
  }

  if (boundaryLayer) {
    map.removeLayer(boundaryLayer)
    boundaryLayer = null
  }

  boundaryMaskLayer = L.polygon(
    [
      [
        L.latLng(39.74, 115.95),
        L.latLng(39.74, 116.58),
        L.latLng(40.24, 116.58),
        L.latLng(40.24, 115.95),
      ],
      ...getBoundaryHoles(),
    ],
    {
      stroke: false,
      fillColor: props.activeBasemap === '高德标准' || props.activeBasemap === 'OSM 标准' ? '#081320' : '#09121d',
      fillOpacity: props.activeBasemap === '高德标准' || props.activeBasemap === 'OSM 标准' ? 0.22 : 0.12,
      interactive: false,
    },
  ).addTo(map)

  boundaryLayer = L.geoJSON(boundaryGeoJson, {
    style: () => ({
      color: props.currentRegion.id === 'haidian-all' ? '#8bdcff' : '#4f8ee6',
      weight: props.currentRegion.id === 'haidian-all' ? 3 : 2.6,
      fillColor: props.currentRegion.id === 'haidian-all' ? '#143a63' : '#173352',
      fillOpacity: props.currentRegion.id === 'haidian-all' ? 0.2 : 0.14,
      opacity: 0.95,
    }),
  }).addTo(map)
}

const drawRegionFocus = () => {
  if (!regionLayerGroup) {
    return
  }

  regionLayerGroup.clearLayers()

  if (regionLabelMarker) {
    regionLabelMarker.remove()
    regionLabelMarker = null
  }

  if (props.currentRegion.id === 'haidian-all') {
    return
  }

  const rectangle = L.rectangle(props.currentRegion.bounds, {
    color: '#7ddaff',
    weight: 1.8,
    opacity: 0.96,
    dashArray: '6 6',
    fillColor: '#1c446b',
    fillOpacity: 0.08,
  })

  rectangle.on('click', () => {
    void focusRegion(true)
  })
  rectangle.addTo(regionLayerGroup)

  regionLabelMarker = L.marker(latLngOf(props.currentRegion.center), {
    icon: L.divIcon({
      className: 'cockpit-map-region-label-wrapper',
      html: `<div class="cockpit-map-region-label">${props.currentRegion.name}</div>`,
      iconSize: [128, 34],
      iconAnchor: [64, 17],
    }),
    interactive: false,
  }).addTo(regionLayerGroup)
}

const createMarkerHtml = (point: DashboardCockpitPoint, highlighted: boolean) => {
  const classes = [
    'cockpit-map-marker',
    `cockpit-map-marker--${point.riskLevel}`,
    point.layer === '风险预警' ? 'is-warning' : '',
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

const emitPointLayout = () => {
  if (!map || !mapRoot.value) {
    return
  }

  const points = props.points.map((point) => {
    const projected = map!.latLngToContainerPoint(latLngOf(point.coords))
    return {
      pointId: point.id,
      x: projected.x,
      y: projected.y,
    }
  })

  emit('layout-change', {
    width: mapRoot.value.clientWidth,
    height: mapRoot.value.clientHeight,
    points,
  })
}

const drawZones = () => {
  if (!zoneLayerGroup) {
    return
  }

  zoneLayerGroup.clearLayers()
  zoneRegistry.clear()

  props.zones.forEach((zone) => {
    const zoneMarker = L.circleMarker(latLngOf(zone.center), {
      radius: Math.max(15, Math.min(26, zone.heat / 4)),
      color: props.selectedZoneId === zone.id ? '#f8fbff' : 'rgba(255,255,255,0.26)',
      weight: props.selectedZoneId === zone.id ? 2.2 : 1.1,
      fillColor: zone.heat >= 92 ? '#ff7d73' : zone.heat >= 84 ? '#ffb561' : '#68a7ff',
      fillOpacity: props.selectedZoneId === zone.id ? 0.42 : 0.24,
    })

    zoneMarker.bindTooltip(zone.name, {
      permanent: zone.heat >= 90,
      direction: 'top',
      offset: [0, -8],
      className: 'cockpit-map-zone-tooltip',
      opacity: 1,
    })

    zoneMarker.on('click', () => emit('select-zone', zone.id))
    zoneMarker.addTo(zoneLayerGroup!)
    zoneRegistry.set(zone.id, zoneMarker)
  })
}

const drawPoints = () => {
  if (!pointLayerGroup) {
    return
  }

  pointLayerGroup.clearLayers()
  pointRegistry.clear()

  const highlightedIds = new Set(props.highlightedPointIds)

  props.points.forEach((point) => {
    const marker = L.marker(latLngOf(point.coords), {
      icon: L.divIcon({
        className: '',
        html: createMarkerHtml(point, highlightedIds.has(point.id) || props.hoveredPointId === point.id),
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
    marker.addTo(pointLayerGroup!)
    pointRegistry.set(point.id, marker)
  })
}

const focusDistrict = (animate = true) => {
  if (!map || !ensureRenderableViewport()) {
    return
  }

  const bounds = districtBounds()
  try {
    if (animate) {
      map.flyToBounds(bounds, {
        paddingTopLeft: [52, 40],
        paddingBottomRight: [52, 40],
        maxZoom: 11.35,
        duration: 0.48,
      })
      return
    }

    map.fitBounds(bounds, {
      paddingTopLeft: [52, 40],
      paddingBottomRight: [52, 40],
      maxZoom: 11.35,
    })
  } catch {
    hasPendingViewportFocus = true
  }
}

const focusRegion = async (animate = true) => {
  if (!map || !ensureRenderableViewport()) {
    return
  }

  if (props.currentRegion.id === 'haidian-all') {
    focusDistrict(animate)
    return
  }

  await nextTick()

  const bounds = regionBounds()
  try {
    if (animate) {
      map.flyToBounds(bounds, {
        paddingTopLeft: [48, 40],
        paddingBottomRight: [48, 40],
        maxZoom: props.currentRegion.zoom,
        duration: 0.52,
      })
      return
    }

    map.fitBounds(bounds, {
      paddingTopLeft: [48, 40],
      paddingBottomRight: [48, 40],
      maxZoom: props.currentRegion.zoom,
    })
  } catch {
    hasPendingViewportFocus = true
  }
}

const syncFocus = async () => {
  if (!map || !ensureRenderableViewport()) {
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
      try {
        map.flyTo(marker.getLatLng(), Math.max(props.currentRegion.zoom, 13.05), { duration: 0.45 })
        marker.openPopup()
      } catch {
        hasPendingViewportFocus = true
      }
      return
    }
  }

  if (props.selectedZoneId) {
    const zoneMarker = zoneRegistry.get(props.selectedZoneId)
    if (zoneMarker) {
      try {
        map.flyTo(zoneMarker.getLatLng(), Math.max(props.currentRegion.zoom, 12.45), { duration: 0.45 })
      } catch {
        hasPendingViewportFocus = true
      }
      return
    }
  }
}

const refreshMap = async () => {
  if (!map) {
    return
  }

  applyBasemap()
  renderBoundary()
  drawRegionFocus()
  drawZones()
  drawPoints()
  await syncFocus()
  emitPointLayout()

  if (!hasInitialFocusApplied) {
    hasInitialFocusApplied = true
    await focusRegion(false)
  }
}

const loadBoundary = async () => {
  const response = await fetch('/maps/haidian.json')
  if (!response.ok) {
    throw new Error(`海淀区边界加载失败：${response.status}`)
  }

  boundaryGeoJson = (await response.json()) as FeatureCollection<Polygon | MultiPolygon>
}

onMounted(async () => {
  ensureMap()
  map?.on('move zoom resize', emitPointLayout)
  await loadBoundary()
  await refreshMap()
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  resizeObserver = null
  map?.stop()
  map?.off()
  baseTileLayer?.off()
  map?.off('move zoom resize', emitPointLayout)
  map?.remove()
  map = null
})

watch(
  () => [
    props.activeBasemap,
    props.activeLayer,
    props.currentRegion.id,
    props.selectedPointId,
    props.selectedZoneId,
    props.hoveredPointId,
    props.points,
    props.zones,
    props.highlightedPointIds,
  ],
  async (_, __, onCleanup) => {
    let cancelled = false
    onCleanup(() => {
      cancelled = true
    })

    await refreshMap()
    if (!cancelled && !props.selectedPointId && !props.selectedZoneId) {
      await focusRegion(true)
    }
  },
  { deep: true },
)

defineExpose({
  focusRegion: () => focusRegion(true),
  focusDistrict: () => focusDistrict(true),
})
</script>

<template>
  <div ref="mapRoot" class="cockpit-map cockpit-map--fallback-dark" />
</template>

<style scoped>
.cockpit-map {
  position: relative;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  border: 1px solid rgba(118, 170, 242, 0.05);
  border-radius: 24px;
  background:
    radial-gradient(circle at 18% 18%, rgba(75, 132, 214, 0.22), transparent 34%),
    radial-gradient(circle at 76% 22%, rgba(72, 189, 165, 0.12), transparent 30%),
    linear-gradient(180deg, rgba(7, 19, 32, 0.96) 0%, rgba(8, 14, 24, 0.98) 100%);
  box-shadow:
    inset 0 1px 0 rgba(146, 184, 236, 0.03),
    0 18px 44px rgba(2, 8, 15, 0.18);
}

.cockpit-map::before {
  content: '';
  position: absolute;
  inset: 0;
  z-index: 399;
  background:
    linear-gradient(rgba(120, 162, 214, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(120, 162, 214, 0.04) 1px, transparent 1px);
  background-size: 56px 56px;
  opacity: 0.42;
  pointer-events: none;
}

.cockpit-map::after {
  content: '';
  position: absolute;
  inset: 0;
  z-index: 400;
  background: linear-gradient(180deg, transparent 0%, rgba(85, 158, 255, 0.06) 48%, transparent 100%);
  transform: translateY(-100%);
  animation: cockpit-map-scan 12s linear infinite;
  pointer-events: none;
}

.cockpit-map--online-light {
  background: #d5dbe3;
}

.cockpit-map--online-dark {
  background: #0f1726;
}

.cockpit-map--fallback-dark {
  background:
    radial-gradient(circle at 18% 18%, rgba(75, 132, 214, 0.22), transparent 34%),
    radial-gradient(circle at 76% 22%, rgba(72, 189, 165, 0.12), transparent 30%),
    linear-gradient(180deg, rgba(7, 19, 32, 0.96) 0%, rgba(8, 14, 24, 0.98) 100%);
}

.cockpit-map:deep(.leaflet-container) {
  height: 100%;
  width: 100%;
  background: transparent;
  font-family: inherit;
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

.cockpit-map:deep(.cockpit-map-region-label) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 96px;
  min-height: 30px;
  border: 1px solid rgba(121, 216, 255, 0.34);
  border-radius: 999px;
  background: rgba(7, 16, 28, 0.82);
  color: #ecf8ff;
  font-size: 12px;
  font-weight: 700;
  box-shadow: 0 12px 28px rgba(2, 8, 15, 0.24);
  backdrop-filter: blur(14px);
}

.cockpit-map:deep(.cockpit-map-marker) {
  position: relative;
  display: flex;
  height: 28px;
  width: 28px;
  align-items: center;
  justify-content: center;
  transition: transform 0.22s ease;
}

.cockpit-map:deep(.cockpit-map-marker__pulse) {
  position: absolute;
  inset: 0;
  border-radius: 999px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.4) 0%, transparent 68%);
  opacity: 0;
}

.cockpit-map:deep(.cockpit-map-marker.is-warning .cockpit-map-marker__pulse) {
  background: radial-gradient(circle, rgba(255, 108, 108, 0.58) 0%, rgba(255, 108, 108, 0.08) 62%, transparent 76%);
  animation: cockpit-warning-pulse 1.7s ease-out infinite;
  opacity: 1;
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

.cockpit-map:deep(.cockpit-map-marker.is-warning .cockpit-map-marker__dot) {
  box-shadow:
    0 0 0 3px rgba(8, 16, 28, 0.42),
    0 0 18px rgba(255, 107, 107, 0.54),
    0 0 34px rgba(255, 107, 107, 0.24);
  animation: cockpit-warning-dot 1.4s ease-in-out infinite;
}

.cockpit-map:deep(.cockpit-map-marker:hover) {
  transform: scale(1.08);
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

@keyframes cockpit-warning-pulse {
  0% {
    transform: scale(0.72);
    opacity: 0.8;
  }

  70% {
    transform: scale(2.2);
    opacity: 0;
  }

  100% {
    opacity: 0;
  }
}

@keyframes cockpit-warning-dot {
  0%,
  100% {
    transform: scale(1);
    filter: saturate(1);
  }

  50% {
    transform: scale(1.18);
    filter: saturate(1.18);
  }
}

@keyframes cockpit-map-scan {
  0% {
    transform: translateY(-100%);
    opacity: 0;
  }

  16% {
    opacity: 0.3;
  }

  50% {
    opacity: 0.58;
  }

  100% {
    transform: translateY(100%);
    opacity: 0;
  }
}
</style>
