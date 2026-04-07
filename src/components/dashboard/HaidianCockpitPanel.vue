<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { GridStack, type GridStackWidget } from 'gridstack'
import HaidianCockpitMapStage from './HaidianCockpitMapStage.vue'
import SituationBottomInfoBand from './SituationBottomInfoBand.vue'
import SituationLeftPulsePanel from './SituationLeftPulsePanel.vue'
import SituationMapControlBar from './SituationMapControlBar.vue'
import SituationMapHighlightLayer from './SituationMapHighlightLayer.vue'
import SituationOsintStreamPanel from './SituationOsintStreamPanel.vue'
import SituationRegionSwitcher from './SituationRegionSwitcher.vue'
import type {
  DashboardCockpitBasemap,
  DashboardCockpitFeedFilter,
  DashboardCockpitLayer,
  DashboardCockpitLink,
  DashboardCockpitOverview,
  DashboardCockpitPoint,
  DashboardCockpitRegion,
  DashboardCockpitStreamEntry,
  DashboardMapHighlight,
  DashboardMapLayoutSnapshot,
} from '../../types/dashboardCockpit'

type CockpitShellPanelId = 'left' | 'map' | 'stream' | 'bottom'

interface CockpitShellLayout extends GridStackWidget {
  id: CockpitShellPanelId
  x: number
  y: number
  w: number
  h: number
  minW: number
  maxW: number
  minH: number
  maxH: number
}

const props = withDefaults(
  defineProps<{
    overview: DashboardCockpitOverview
    initialLayer?: DashboardCockpitLayer
  }>(),
  {
    initialLayer: '风险预警',
  },
)

const router = useRouter()
const mapStageRef = ref<InstanceType<typeof HaidianCockpitMapStage> | null>(null)
const shellGridRoot = ref<HTMLElement | null>(null)

const SHELL_STORAGE_KEY = 'dashboard-cockpit-shell-grid-v2'

const activeLayer = ref<DashboardCockpitLayer>(props.initialLayer)
const activeBasemap = ref<DashboardCockpitBasemap>(props.overview.defaultBasemap)
const currentRegionId = ref(props.overview.regions[0]?.id ?? 'haidian-all')
const currentStreamFilter = ref<DashboardCockpitFeedFilter>('全部')
const streamMode = ref<'dynamic' | 'static'>('dynamic')
const selectedPointId = ref('')
const selectedZoneId = ref('')
const hoveredPointId = ref('')
const showAllHighlights = ref(true)
const mapLayout = ref<DashboardMapLayoutSnapshot | null>(null)
const shellLayouts = ref<CockpitShellLayout[]>([])

let shellGrid: GridStack | null = null

const sourceLabelMap: Record<DashboardCockpitLayer, string> = {
  风险预警: '风险联动引擎',
  突发事件: '现场处置链路',
  热点事件: '多源传播监测',
  重点关注: '重点对象档案',
}

const defaultShellLayouts = (): CockpitShellLayout[] => [
  { id: 'left', x: 0, y: 0, w: 2, h: 5, minW: 2, maxW: 4, minH: 4, maxH: 8 },
  { id: 'map', x: 2, y: 0, w: 7, h: 5, minW: 5, maxW: 8, minH: 4, maxH: 8 },
  { id: 'stream', x: 9, y: 0, w: 3, h: 5, minW: 2, maxW: 5, minH: 4, maxH: 8 },
  { id: 'bottom', x: 0, y: 5, w: 12, h: 3, minW: 6, maxW: 12, minH: 2, maxH: 5 },
]

const reconcileShellLayouts = (candidate: CockpitShellLayout[]) => {
  const defaults = defaultShellLayouts()
  const next = candidate.length ? [...candidate] : defaults
  const existingIds = new Set(next.map((item) => item.id))

  defaults.forEach((layout) => {
    if (!existingIds.has(layout.id)) {
      next.push(layout)
    }
  })

  return next
    .filter((item): item is CockpitShellLayout => defaults.some((layout) => layout.id === item.id))
    .map((item) => {
      const fallback = defaults.find((layout) => layout.id === item.id)!
      return {
        ...fallback,
        ...item,
        x: Math.max(0, item.x ?? fallback.x),
        y: Math.max(0, item.y ?? fallback.y),
        w: Math.min(fallback.maxW, Math.max(fallback.minW, item.w ?? fallback.w)),
        h: Math.min(fallback.maxH, Math.max(fallback.minH, item.h ?? fallback.h)),
      }
    })
    .sort((left, right) => (left.y - right.y) || (left.x - right.x))
}

const shellLayoutMap = computed(() => new Map(shellLayouts.value.map((layout) => [layout.id, layout])))
const getShellLayout = (id: CockpitShellPanelId) =>
  shellLayoutMap.value.get(id) ?? defaultShellLayouts().find((layout) => layout.id === id)!

const regions = computed(() => props.overview.regions.filter((region) => region.enabled).sort((a, b) => a.order - b.order))
const regionMap = computed(() => new Map(regions.value.map((region) => [region.id, region])))
const currentRegion = computed<DashboardCockpitRegion>(
  () => regionMap.value.get(currentRegionId.value) ?? regions.value[0] ?? props.overview.regions[0]!,
)
const pointIndex = computed(() => new Map(props.overview.points.map((point) => [point.id, point])))

const parseTime = (value: string) => new Date(value.replace(/-/g, '/')).getTime()

const latestUpdate = computed(
  () => [...props.overview.points].sort((left, right) => parseTime(right.occurredAt) - parseTime(left.occurredAt))[0]?.occurredAt ?? '暂无更新',
)

const matchRegionByArea = (area: string) =>
  regions.value.find((region) => region.id !== 'haidian-all' && region.areaKeywords.includes(area))?.id ?? 'haidian-all'

const matchesRegion = (area: string, region: DashboardCockpitRegion) =>
  region.id === 'haidian-all' ? true : region.areaKeywords.includes(area)

const currentRegionPoints = computed(() =>
  props.overview.points.filter((point) => matchesRegion(point.area, currentRegion.value)),
)

const currentRegionZones = computed(() =>
  props.overview.zones.filter((zone) =>
    currentRegion.value.id === 'haidian-all'
      ? true
      : currentRegion.value.zoneIds.includes(zone.id) ||
        zone.featuredPointIds.some((pointId) => currentRegionPoints.value.some((point) => point.id === pointId)),
  ),
)

const currentMapPoints = computed(() =>
  currentRegionPoints.value
    .filter((point) => point.layer === activeLayer.value)
    .sort((left, right) => right.priority - left.priority || parseTime(right.occurredAt) - parseTime(left.occurredAt)),
)

const currentStreamItems = computed<DashboardCockpitStreamEntry[]>(() =>
  currentRegionPoints.value
    .filter((point) => (currentStreamFilter.value === '全部' ? true : point.layer === currentStreamFilter.value))
    .sort((left, right) => parseTime(right.occurredAt) - parseTime(left.occurredAt) || right.priority - left.priority)
    .map((point) => ({
      id: point.id,
      title: point.title,
      summary: point.description,
      time: point.occurredAt,
      regionId: matchRegionByArea(point.area),
      regionName: point.area,
      eventType: point.layer,
      source: sourceLabelMap[point.layer],
      tags: point.keywords.slice(0, 4),
      relatedPointId: point.id,
      priority: point.priority,
      status: point.status,
    })),
)

const highlightedPointIds = computed(() => currentMapPoints.value.slice(0, 4).map((point) => point.id))
const visibleHighlights = computed<DashboardMapHighlight[]>(() =>
  props.overview.mapHighlights
    .filter((highlight) => {
      const point = pointIndex.value.get(highlight.pointId)
      if (!point) return false
      if (point.layer !== activeLayer.value) return false
      if (!matchesRegion(point.area, currentRegion.value)) return false
      return true
    })
    .slice(0, 6),
)

const layerStats = computed(() =>
  props.overview.layers.map((layer) => ({
    layer,
    count: currentRegionPoints.value.filter((point) => point.layer === layer).length,
  })),
)

const districtShare = computed(() => {
  if (currentRegion.value.id === 'haidian-all') {
    return 100
  }

  return Math.max(1, Math.round((currentRegionPoints.value.length / props.overview.points.length) * 100))
})

const displayPoint = computed<DashboardCockpitPoint | null>(() => {
  if (selectedPointId.value) {
    return pointIndex.value.get(selectedPointId.value) ?? null
  }

  return currentMapPoints.value[0] ?? currentRegionPoints.value[0] ?? null
})

const displayBusinessBoards = computed(() => {
  if (currentRegion.value.id === 'haidian-all') {
    return props.overview.businessBoardMetrics
  }

  const shareFactor = Math.min(0.86, Math.max(0.28, districtShare.value / 100 + 0.08))

  return props.overview.businessBoardMetrics.map((board) => ({
    ...board,
    currentValue: Math.max(1, Math.round(board.currentValue * shareFactor)),
    increment: `当前地区 ${currentRegion.value.shortName} 联动`,
    status: `${currentRegion.value.shortName}${board.status}`,
    highlights: board.highlights.map((highlight, index) => ({
      ...highlight,
      value: index === 0 ? `${Math.max(1, layerStats.value[index % layerStats.value.length]?.count ?? 0)} 项` : highlight.value,
    })),
  }))
})

const persistShellLayouts = (layouts: CockpitShellLayout[]) => {
  window.localStorage.setItem(SHELL_STORAGE_KEY, JSON.stringify(layouts))
}

const captureShellLayoutsFromDom = () => {
  if (!shellGridRoot.value) return []

  return Array.from(shellGridRoot.value.querySelectorAll<HTMLElement>('.grid-stack-item[data-panel-id]'))
    .map((item) => {
      const id = item.dataset.panelId as CockpitShellPanelId
      const fallback = defaultShellLayouts().find((layout) => layout.id === id)
      return {
        id,
        x: Number(item.getAttribute('gs-x') ?? fallback?.x ?? 0),
        y: Number(item.getAttribute('gs-y') ?? fallback?.y ?? 0),
        w: Number(item.getAttribute('gs-w') ?? fallback?.w ?? 1),
        h: Number(item.getAttribute('gs-h') ?? fallback?.h ?? 1),
        minW: fallback?.minW ?? 1,
        maxW: fallback?.maxW ?? 12,
        minH: fallback?.minH ?? 1,
        maxH: fallback?.maxH ?? 8,
      } satisfies CockpitShellLayout
    })
    .sort((left, right) => (left.y - right.y) || (left.x - right.x))
}

const destroyShellGrid = () => {
  if (!shellGrid) return
  shellGrid.offAll()
  shellGrid.destroy(false)
  shellGrid = null
}

const initShellGrid = async () => {
  if (!shellGridRoot.value) return

  destroyShellGrid()
  await nextTick()

  shellGrid = GridStack.init(
    {
      column: 12,
      cellHeight: 92,
      margin: 8,
      float: false,
      minRow: 8,
      disableDrag: false,
      disableResize: false,
      animate: true,
      alwaysShowResizeHandle: false,
      handle: '.haidian-cockpit__panel-shell',
      draggable: {
        cancel:
          'button,a,input,textarea,select,.leaflet-container,.leaflet-control,.leaflet-popup,.leaflet-tooltip,.leaflet-marker-icon,.leaflet-interactive,.map-highlight-layer__card,.situation-left-pulse__grid,.situation-left-pulse__module,.situation-bottom-band__grid,.situation-bottom-band__card,.ui-resizable-handle',
      },
      resizable: { handles: 'e,se,s,sw,w' },
    },
    shellGridRoot.value,
  )

  shellGrid.on('change', () => {
    const nextLayouts = reconcileShellLayouts(captureShellLayoutsFromDom())
    shellLayouts.value = nextLayouts
    persistShellLayouts(nextLayouts)
  })
}

const restoreShellLayout = async () => {
  const nextLayouts = defaultShellLayouts()
  shellLayouts.value = nextLayouts
  persistShellLayouts(nextLayouts)
  await initShellGrid()
  await nextTick()
  await mapStageRef.value?.focusRegion()
}

const openRouteLink = (link: DashboardCockpitLink) => {
  if (link.path) {
    router.push({ path: link.path, query: link.query })
    return
  }

  if (link.kind === 'warning') {
    router.push({ path: '/analysis-warning/records', query: { keyword: link.title, autoOpen: 'first' } })
    return
  }

  if (link.kind === 'event') {
    router.push({ path: '/knowledge-build/event-management', query: { keyword: link.title, autoOpen: 'first' } })
    return
  }

  if (link.kind === 'entity') {
    router.push({ path: '/knowledge-build/entity-management', query: { keyword: link.title, autoOpen: 'first' } })
    return
  }

  router.push({ path: '/smart-search', query: { keyword: link.title, region: '海淀区', mode: '语义检索' } })
}

const openBusinessBoard = (board: DashboardCockpitOverview['businessBoardMetrics'][number]) => {
  openRouteLink(board.detailTarget)
}

const openModuleDetail = (module: DashboardCockpitOverview['systemMetrics'][number]) => {
  openRouteLink(module.detailTarget)
}

const focusPoint = async (pointId: string, switchLayer = true) => {
  const target = pointIndex.value.get(pointId)
  if (!target) {
    return
  }

  if (switchLayer && target.layer !== activeLayer.value) {
    activeLayer.value = target.layer
  }

  selectedPointId.value = pointId
  selectedZoneId.value = props.overview.zones.find((zone) => zone.featuredPointIds.includes(pointId))?.id ?? ''
  hoveredPointId.value = ''

  await nextTick()
}

const focusZone = async (zoneId: string) => {
  const target = props.overview.zones.find((zone) => zone.id === zoneId)
  if (!target) {
    return
  }

  const regionId = regions.value.find((region) => region.zoneIds.includes(zoneId))?.id
  if (regionId) {
    currentRegionId.value = regionId
  }

  selectedZoneId.value = zoneId
  selectedPointId.value = ''
  await nextTick()
}

const openPointDetail = (pointId: string) => {
  const point = pointIndex.value.get(pointId)
  if (!point) {
    ElMessage.info('当前对象暂无详情入口')
    return
  }

  openRouteLink(point.detailTarget)
}

const focusFirstAvailable = async () => {
  await nextTick()

  const firstPoint = currentMapPoints.value[0] ?? currentRegionPoints.value[0]
  if (firstPoint) {
    await focusPoint(firstPoint.id, false)
    return
  }

  const firstZone = currentRegionZones.value[0]
  if (firstZone) {
    selectedZoneId.value = firstZone.id
  }
}

const ensureRegionHasVisibleLayer = () => {
  if (currentRegionPoints.value.some((point) => point.layer === activeLayer.value)) {
    return
  }

  const fallbackLayer = props.overview.layers.find((layer) =>
    currentRegionPoints.value.some((point) => point.layer === layer),
  )

  if (fallbackLayer) {
    activeLayer.value = fallbackLayer
  }
}

const setCurrentRegion = async (regionId: string) => {
  if (regionId === currentRegionId.value) {
    await mapStageRef.value?.focusRegion()
    return
  }

  currentRegionId.value = regionId
  selectedPointId.value = ''
  selectedZoneId.value = ''

  await nextTick()
  ensureRegionHasVisibleLayer()
  await focusFirstAvailable()
}

const setLayer = async (layer: DashboardCockpitLayer) => {
  activeLayer.value = layer
  currentStreamFilter.value = layer
  selectedPointId.value = ''
  selectedZoneId.value = ''
  hoveredPointId.value = ''

  await focusFirstAvailable()
}

const setStreamFilter = async (filter: DashboardCockpitFeedFilter) => {
  currentStreamFilter.value = filter

  if (filter !== '全部') {
    activeLayer.value = filter
  }

  await focusFirstAvailable()
}

const setStreamMode = (mode: 'dynamic' | 'static') => {
  streamMode.value = mode
}

const handleBasemapError = () => {
  activeBasemap.value = '驾驶舱暗色'
  ElMessage.warning('在线底图加载失败，已自动回退到本地底图')
}

const toggleHighlightPanel = () => {
  showAllHighlights.value = !showAllHighlights.value
}

const handleMapLayoutChange = (layout: DashboardMapLayoutSnapshot) => {
  mapLayout.value = layout
}

const resetMapView = async () => {
  selectedPointId.value = ''
  selectedZoneId.value = ''
  await nextTick()
  await mapStageRef.value?.focusRegion()
}

watch(
  () => [currentRegionId.value, activeLayer.value].join('|'),
  async () => {
    ensureRegionHasVisibleLayer()

    if (selectedPointId.value && !currentRegionPoints.value.some((point) => point.id === selectedPointId.value)) {
      selectedPointId.value = ''
    }

    if (selectedZoneId.value && !currentRegionZones.value.some((zone) => zone.id === selectedZoneId.value)) {
      selectedZoneId.value = ''
    }

    if (currentStreamFilter.value !== '全部' && !currentRegionPoints.value.some((point) => point.layer === currentStreamFilter.value)) {
      currentStreamFilter.value = activeLayer.value
    }

    await nextTick()
  },
)

onMounted(async () => {
  const saved = window.localStorage.getItem(SHELL_STORAGE_KEY)
  if (saved) {
    try {
      shellLayouts.value = reconcileShellLayouts(JSON.parse(saved) as CockpitShellLayout[])
    } catch {
      shellLayouts.value = defaultShellLayouts()
    }
  } else {
    shellLayouts.value = defaultShellLayouts()
  }

  await initShellGrid()
  ensureRegionHasVisibleLayer()
  await focusFirstAvailable()
})

onBeforeUnmount(() => {
  destroyShellGrid()
})
</script>

<template>
  <section class="haidian-cockpit">
    <div ref="shellGridRoot" class="haidian-cockpit__grid grid-stack">
      <section
        class="grid-stack-item haidian-cockpit__grid-item"
        data-panel-id="left"
        :gs-id="getShellLayout('left').id"
        :gs-x="getShellLayout('left').x"
        :gs-y="getShellLayout('left').y"
        :gs-w="getShellLayout('left').w"
        :gs-h="getShellLayout('left').h"
        :gs-min-w="getShellLayout('left').minW"
        :gs-max-w="getShellLayout('left').maxW"
        :gs-min-h="getShellLayout('left').minH"
        :gs-max-h="getShellLayout('left').maxH"
      >
        <div class="grid-stack-item-content">
          <article class="haidian-cockpit__panel-shell haidian-cockpit__panel-shell--left">
            <SituationLeftPulsePanel
              class="haidian-cockpit__left-panel"
              :modules="props.overview.systemMetrics"
              @open-module="openModuleDetail"
              @restore-layout="restoreShellLayout"
            />
          </article>
        </div>
      </section>

      <section
        class="grid-stack-item haidian-cockpit__grid-item"
        data-panel-id="map"
        :gs-id="getShellLayout('map').id"
        :gs-x="getShellLayout('map').x"
        :gs-y="getShellLayout('map').y"
        :gs-w="getShellLayout('map').w"
        :gs-h="getShellLayout('map').h"
        :gs-min-w="getShellLayout('map').minW"
        :gs-max-w="getShellLayout('map').maxW"
        :gs-min-h="getShellLayout('map').minH"
        :gs-max-h="getShellLayout('map').maxH"
      >
        <div class="grid-stack-item-content">
          <article class="haidian-cockpit__panel-shell haidian-cockpit__panel-shell--map">
            <div class="haidian-cockpit__map-stage-shell">
              <HaidianCockpitMapStage
                ref="mapStageRef"
                class="haidian-cockpit__map-stage"
                :district="props.overview.district"
                :map-bounds="props.overview.mapBounds"
                :current-region="currentRegion"
                :points="currentMapPoints"
                :zones="currentRegionZones"
                :selected-point-id="selectedPointId"
                :selected-zone-id="selectedZoneId"
                :hovered-point-id="hoveredPointId"
                :highlighted-point-ids="highlightedPointIds"
                :active-layer="activeLayer"
                :active-basemap="activeBasemap"
                @select-point="focusPoint"
                @select-zone="focusZone"
                @layout-change="handleMapLayoutChange"
                @basemap-error="handleBasemapError"
              />

              <SituationMapHighlightLayer
                :visible="showAllHighlights"
                :layout="mapLayout"
                :highlights="visibleHighlights"
                :selected-point-id="selectedPointId"
                :hovered-point-id="hoveredPointId"
                @open-highlight="(highlight) => openRouteLink(highlight.detailTarget)"
                @hover-highlight="(pointId) => (hoveredPointId = pointId)"
                @leave-highlight="hoveredPointId = ''"
              />

              <div class="haidian-cockpit__map-overlay haidian-cockpit__map-overlay--top">
                <SituationRegionSwitcher
                  overlay
                  :show-header="false"
                  :regions="regions"
                  :active-region-id="currentRegionId"
                  :latest-update="latestUpdate"
                  @select-region="setCurrentRegion"
                />
              </div>

              <div class="haidian-cockpit__map-overlay haidian-cockpit__map-overlay--bottom">
                <SituationMapControlBar
                  overlay
                  :show-meta="false"
                  :active-region-name="currentRegion.name"
                  :layers="props.overview.layers"
                  :active-layer="activeLayer"
                  :active-basemap="activeBasemap"
                  :is-highlight-expanded="showAllHighlights"
                  @select-layer="setLayer"
                  @toggle-highlights="toggleHighlightPanel"
                  @reset-view="resetMapView"
                />
              </div>
            </div>
          </article>
        </div>
      </section>

      <section
        class="grid-stack-item haidian-cockpit__grid-item"
        data-panel-id="stream"
        :gs-id="getShellLayout('stream').id"
        :gs-x="getShellLayout('stream').x"
        :gs-y="getShellLayout('stream').y"
        :gs-w="getShellLayout('stream').w"
        :gs-h="getShellLayout('stream').h"
        :gs-min-w="getShellLayout('stream').minW"
        :gs-max-w="getShellLayout('stream').maxW"
        :gs-min-h="getShellLayout('stream').minH"
        :gs-max-h="getShellLayout('stream').maxH"
      >
        <div class="grid-stack-item-content">
          <article class="haidian-cockpit__panel-shell haidian-cockpit__panel-shell--stream">
            <div class="haidian-cockpit__stream-shell">
              <SituationOsintStreamPanel
                :items="currentStreamItems"
                :active-item-id="displayPoint?.id ?? ''"
                :active-filter="currentStreamFilter"
                :layers="props.overview.layers"
                :current-region-name="currentRegion.name"
                :mode="streamMode"
                @select-item="focusPoint"
                @open-item="openPointDetail"
                @change-filter="setStreamFilter"
                @change-mode="setStreamMode"
              />
            </div>
          </article>
        </div>
      </section>

      <section
        class="grid-stack-item haidian-cockpit__grid-item"
        data-panel-id="bottom"
        :gs-id="getShellLayout('bottom').id"
        :gs-x="getShellLayout('bottom').x"
        :gs-y="getShellLayout('bottom').y"
        :gs-w="getShellLayout('bottom').w"
        :gs-h="getShellLayout('bottom').h"
        :gs-min-w="getShellLayout('bottom').minW"
        :gs-max-w="getShellLayout('bottom').maxW"
        :gs-min-h="getShellLayout('bottom').minH"
        :gs-max-h="getShellLayout('bottom').maxH"
      >
        <div class="grid-stack-item-content">
          <article class="haidian-cockpit__panel-shell haidian-cockpit__panel-shell--bottom">
            <SituationBottomInfoBand
              class="haidian-cockpit__bottom-band"
              :boards="displayBusinessBoards"
              @open-board="openBusinessBoard"
              @restore-layout="restoreShellLayout"
            />
          </article>
        </div>
      </section>
    </div>
  </section>
</template>

<style scoped>
.haidian-cockpit {
  min-width: 0;
  min-height: 0;
  height: 100%;
  overflow: auto;
  padding-right: 2px;
}

.haidian-cockpit__grid {
  min-height: 0;
}

.haidian-cockpit__grid-item > .grid-stack-item-content {
  overflow: hidden;
}

.haidian-cockpit__panel-shell {
  position: relative;
  display: flex;
  min-width: 0;
  min-height: 0;
  height: 100%;
  overflow: hidden;
  border: 1px solid rgba(109, 155, 222, 0.1);
  border-radius: 24px;
  background: linear-gradient(180deg, rgba(8, 14, 24, 0.82) 0%, rgba(7, 14, 24, 0.62) 100%);
  box-shadow: 0 18px 44px rgba(2, 8, 15, 0.18);
  cursor: grab;
}

.haidian-cockpit__panel-shell--left,
.haidian-cockpit__panel-shell--stream,
.haidian-cockpit__panel-shell--bottom {
  padding: 10px;
}

.haidian-cockpit__panel-shell--map {
  padding: 4px;
}

.haidian-cockpit__left-panel,
.haidian-cockpit__bottom-band {
  width: 100%;
  height: 100%;
  min-width: 0;
  min-height: 0;
}

.haidian-cockpit__stream-shell {
  min-width: 0;
  min-height: 0;
  width: 100%;
  overflow: hidden;
}

.haidian-cockpit__map-stage-shell {
  position: relative;
  flex: 1 1 auto;
  min-height: 100%;
}

.haidian-cockpit__map-stage {
  height: 100%;
}

.haidian-cockpit__map-overlay {
  position: absolute;
  z-index: 430;
  display: flex;
  pointer-events: none;
}

.haidian-cockpit__map-overlay > * {
  width: max-content;
  max-width: 100%;
  pointer-events: auto;
}

.haidian-cockpit__map-overlay--top {
  top: 10px;
  left: 10px;
  right: 10px;
  justify-content: flex-start;
}

.haidian-cockpit__map-overlay--bottom {
  left: 10px;
  right: 10px;
  bottom: 10px;
  justify-content: center;
}

.haidian-cockpit__map-overlay--top > * {
  max-width: min(100%, 540px);
}

.haidian-cockpit__map-overlay--bottom > * {
  max-width: min(100%, 760px);
}

.haidian-cockpit :deep(.ui-resizable-handle) {
  display: block !important;
  opacity: 0;
  background: transparent;
  transition: opacity 0.18s ease;
}

.haidian-cockpit :deep(.grid-stack-item:hover .ui-resizable-handle),
.haidian-cockpit :deep(.grid-stack-item.ui-resizable-resizing .ui-resizable-handle) {
  opacity: 0.14;
}

.haidian-cockpit :deep(.grid-stack-item.ui-draggable-dragging .haidian-cockpit__panel-shell) {
  cursor: grabbing;
}

.haidian-cockpit :deep(.grid-stack-item.ui-draggable-dragging .grid-stack-item-content),
.haidian-cockpit :deep(.grid-stack-item.ui-resizable-resizing .grid-stack-item-content) {
  box-shadow: 0 18px 34px rgba(2, 8, 15, 0.22);
}

@media (max-width: 1180px) {
  .haidian-cockpit__map-overlay--top,
  .haidian-cockpit__map-overlay--bottom {
    left: 10px;
    right: 10px;
  }

  .haidian-cockpit__map-overlay--bottom {
    bottom: 10px;
    justify-content: stretch;
  }

  .haidian-cockpit__map-overlay--bottom > * {
    width: 100%;
  }
}
</style>
