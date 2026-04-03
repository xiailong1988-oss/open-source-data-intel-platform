<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { GridStack, type GridStackWidget } from 'gridstack'
import HaidianCockpitMapStage from './HaidianCockpitMapStage.vue'
import SituationBottomInfoBand from './SituationBottomInfoBand.vue'
import SituationLeftPulsePanel from './SituationLeftPulsePanel.vue'
import SituationMapHighlightLayer from './SituationMapHighlightLayer.vue'
import SituationMapOverlayBar from './SituationMapOverlayBar.vue'
import SituationSignalRail from './SituationSignalRail.vue'
import { getCockpitMapProviderFactoryState } from '../../lib/map/mapProviderFactory'
import { useAppStore } from '../../stores/app'
import type {
  DashboardCockpitBasemap,
  DashboardCockpitDisplayMode,
  DashboardCockpitHeadline,
  DashboardCockpitLayer,
  DashboardCockpitLink,
  DashboardCockpitOverview,
  DashboardCockpitRiskLevel,
  DashboardCockpitTickerItem,
  DashboardMapHighlight,
  DashboardMapLayoutSnapshot,
} from '../../types/dashboardCockpit'

const props = withDefaults(
  defineProps<{
    overview: DashboardCockpitOverview
    initialLayer?: DashboardCockpitLayer
  }>(),
  {
    initialLayer: '风险预警',
  },
)

type CockpitPanelId = 'left' | 'map' | 'rail' | 'bottom'

interface CockpitPanelLayout extends GridStackWidget {
  id: CockpitPanelId
  x: number
  y: number
  w: number
  h: number
  minW: number
  maxW: number
  minH: number
  maxH: number
}

const COCKPIT_GRID_COLUMNS = 12
const COCKPIT_GRID_ROWS = 12
const COCKPIT_GRID_MARGIN = 10
const LAYOUT_STORAGE_KEY = 'dashboard-cockpit-layout-v5'

const router = useRouter()
const appStore = useAppStore()
const { isSidebarHidden } = storeToRefs(appStore)
const workspaceRef = ref<HTMLElement | null>(null)
const gridRootRef = ref<HTMLElement | null>(null)
const panelLayouts = ref<CockpitPanelLayout[]>([])

let grid: GridStack | null = null
let workspaceResizeObserver: ResizeObserver | null = null

const panelConstraintMap: Record<
  CockpitPanelId,
  {
    minW: number
    maxW: number
    minH: number
    maxH: number
  }
> = {
  left: { minW: 2, maxW: 5, minH: 4, maxH: 12 },
  map: { minW: 4, maxW: 10, minH: 5, maxH: 12 },
  rail: { minW: 3, maxW: 6, minH: 5, maxH: 12 },
  bottom: { minW: 6, maxW: 12, minH: 3, maxH: 6 },
}

const defaultPanelLayouts = (): CockpitPanelLayout[] => [
  { id: 'left', x: 0, y: 0, w: 3, h: 8, ...panelConstraintMap.left },
  { id: 'map', x: 3, y: 0, w: 5, h: 8, ...panelConstraintMap.map },
  { id: 'rail', x: 8, y: 0, w: 4, h: 8, ...panelConstraintMap.rail },
  { id: 'bottom', x: 0, y: 8, w: 12, h: 4, ...panelConstraintMap.bottom },
]

const normalizeLayout = (layout: Partial<CockpitPanelLayout> & { id: CockpitPanelId }): CockpitPanelLayout => {
  const constraint = panelConstraintMap[layout.id]

  return {
    id: layout.id,
    x: Math.max(0, layout.x ?? 0),
    y: Math.max(0, layout.y ?? 0),
    w: Math.min(constraint.maxW, Math.max(constraint.minW, layout.w ?? constraint.minW)),
    h: Math.min(constraint.maxH, Math.max(constraint.minH, layout.h ?? constraint.minH)),
    minW: constraint.minW,
    maxW: constraint.maxW,
    minH: constraint.minH,
    maxH: constraint.maxH,
  }
}

const reconcileLayouts = (candidate: CockpitPanelLayout[]) => {
  const fallbackMap = new Map(defaultPanelLayouts().map((layout) => [layout.id, layout]))
  const candidateMap = new Map(candidate.map((layout) => [layout.id, layout]))

  return (['left', 'map', 'rail', 'bottom'] as CockpitPanelId[])
    .map((id) => normalizeLayout(candidateMap.get(id) ?? fallbackMap.get(id)!))
    .sort((left, right) => (left.y - right.y) || (left.x - right.x))
}

const captureLayoutsFromDom = () => {
  if (!gridRootRef.value) {
    return []
  }

  return Array.from(gridRootRef.value.querySelectorAll<HTMLElement>('.grid-stack-item[data-panel-id]'))
    .map((item) =>
      normalizeLayout({
        id: item.dataset.panelId as CockpitPanelId,
        x: Number(item.getAttribute('gs-x') ?? 0),
        y: Number(item.getAttribute('gs-y') ?? 0),
        w: Number(item.getAttribute('gs-w') ?? 1),
        h: Number(item.getAttribute('gs-h') ?? 1),
      }),
    )
    .sort((left, right) => (left.y - right.y) || (left.x - right.x))
}

const persistLayouts = (layouts: CockpitPanelLayout[]) => {
  window.localStorage.setItem(LAYOUT_STORAGE_KEY, JSON.stringify(layouts))
}

const destroyGrid = () => {
  if (!grid) {
    return
  }

  grid.offAll()
  grid.destroy(false)
  grid = null
}

const updateGridCellHeight = () => {
  if (!grid || !workspaceRef.value) {
    return
  }

  const availableHeight = workspaceRef.value.clientHeight
  const totalGap = COCKPIT_GRID_MARGIN * (COCKPIT_GRID_ROWS - 1)
  const nextCellHeight = Math.max(56, Math.floor((availableHeight - totalGap) / COCKPIT_GRID_ROWS))
  grid.cellHeight(nextCellHeight)
}

const initGrid = async () => {
  if (!gridRootRef.value) {
    return
  }

  destroyGrid()
  await nextTick()

  if (!gridRootRef.value) {
    return
  }

  grid = GridStack.init(
    {
      column: COCKPIT_GRID_COLUMNS,
      margin: COCKPIT_GRID_MARGIN,
      minRow: COCKPIT_GRID_ROWS,
      maxRow: COCKPIT_GRID_ROWS,
      float: false,
      animate: true,
      disableDrag: false,
      disableResize: false,
      alwaysShowResizeHandle: false,
      handle: '.haidian-cockpit__panel-shell',
      draggable: {
        cancel:
          'button,.leaflet-container,.leaflet-control-container,.leaflet-marker-icon,.leaflet-interactive,.leaflet-popup,.leaflet-tooltip,.map-highlight-layer__card',
      },
      resizable: { handles: 'all' },
      cellHeight: 72,
    },
    gridRootRef.value,
  )

  updateGridCellHeight()

  grid.on('change', () => {
    const nextLayouts = reconcileLayouts(captureLayoutsFromDom())
    panelLayouts.value = nextLayouts
    persistLayouts(nextLayouts)
  })
}

const displayPanels = computed(() => (panelLayouts.value.length ? panelLayouts.value : defaultPanelLayouts()))

const activeLayer = ref<DashboardCockpitLayer>(props.initialLayer)
const providerFactoryState = getCockpitMapProviderFactoryState()
const activeBasemap = ref<DashboardCockpitBasemap>(props.overview.defaultBasemap ?? providerFactoryState.defaultBasemap)
const selectedPointId = ref('')
const selectedZoneId = ref('')
const hoveredPointId = ref('')
const activeTickerIndex = ref(0)
const isPlaybackPaused = ref(false)
const isStreamHovered = ref(false)
const showAllHighlights = ref(false)
const mapLayout = ref<DashboardMapLayoutSnapshot | null>(null)
const mapCursor = ref({ visible: false, x: 0, y: 0 })
let tickerTimer: number | null = null

const filters = ref({
  timeRange: '今日' as '今日' | '近7日',
  area: '全部' as string,
  riskLevel: '全部' as DashboardCockpitRiskLevel | '全部',
  displayMode: '驾驶舱降噪' as DashboardCockpitDisplayMode,
})

const summarySignalMap: Record<string, { layer: DashboardCockpitLayer; risk?: DashboardCockpitRiskLevel | '全部' }> = {
  'high-risk': { layer: '风险预警', risk: '高' },
  'sudden-events': { layer: '突发事件', risk: '全部' },
  'hot-zones': { layer: '热点事件', risk: '全部' },
  'watch-targets': { layer: '重点关注', risk: '全部' },
}

const parseTime = (value: string) => new Date(value.replace(/-/g, '/')).getTime()
const referenceTime = parseTime('2026-03-25 23:59')
const isInRange = (value: string) => {
  const diff = referenceTime - parseTime(value)
  const day = 24 * 60 * 60 * 1000
  return filters.value.timeRange === '今日' ? diff <= day : diff <= day * 7
}

const pointIndex = computed(() => new Map(props.overview.points.map((point) => [point.id, point])))

const latestUpdate = computed(() =>
  [...props.overview.points].sort((left, right) => (left.occurredAt > right.occurredAt ? -1 : 1))[0]?.occurredAt ?? '暂无更新',
)

const signalItems = computed<DashboardCockpitHeadline[]>(() => props.overview.headlines)
const layerPoints = computed(() => props.overview.points.filter((point) => point.layer === activeLayer.value))
const filteredPoints = computed(() =>
  layerPoints.value.filter((point) => {
    if (filters.value.area !== '全部' && point.area !== filters.value.area) return false
    if (filters.value.riskLevel !== '全部' && point.riskLevel !== filters.value.riskLevel) return false
    return isInRange(point.occurredAt)
  }),
)

const filteredZones = computed(() =>
  props.overview.zones.filter((zone) => {
    if (filters.value.area !== '全部') {
      return (
        zone.name === filters.value.area ||
        zone.featuredPointIds.some((id) => filteredPoints.value.some((point) => point.id === id))
      )
    }

    return zone.featuredPointIds.some((id) => filteredPoints.value.some((point) => point.id === id))
  }),
)

const priorityPoints = computed(() => [...filteredPoints.value].sort((left, right) => right.priority - left.priority))
const highlightedPointIds = computed(() => {
  const quota = filters.value.displayMode === '驾驶舱降噪' ? 3 : 6
  return priorityPoints.value.slice(0, quota).map((point) => point.id)
})

const currentTicker = computed<DashboardCockpitTickerItem[]>(() => props.overview.ticker)
const currentTopics = computed(() => props.overview.topics.filter((item) => item.layer === activeLayer.value).slice(0, 2))
const currentProfile = computed(() => props.overview.layerProfiles[activeLayer.value])
const selectedPoint = computed(() => pointIndex.value.get(selectedPointId.value) ?? null)
const selectedZone = computed(() => props.overview.zones.find((zone) => zone.id === selectedZoneId.value) ?? null)
const defaultZone = computed(() => [...filteredZones.value].sort((left, right) => right.weight - left.weight)[0] ?? null)
const defaultPoint = computed(() => priorityPoints.value[0] ?? null)
const displayPoint = computed(() => selectedPoint.value ?? (!selectedZone.value ? defaultPoint.value : null))
const displayZone = computed(() => selectedZone.value ?? (!displayPoint.value ? defaultZone.value : null))
const selectedZonePoints = computed(() => {
  const zone = displayZone.value
  if (!zone) return []
  return filteredPoints.value.filter((point) => zone.featuredPointIds.includes(point.id)).slice(0, 4)
})

const focusMeta = computed(() => {
  if (displayPoint.value) {
    return [displayPoint.value.area, displayPoint.value.category, displayPoint.value.occurredAt, displayPoint.value.status]
  }

  if (displayZone.value) {
    return [`热度 ${displayZone.value.heat}`, `预警 ${displayZone.value.alertCount}`, `事件 ${displayZone.value.eventCount}`, displayZone.value.emphasis]
  }

  return [activeLayer.value, '海淀区', latestUpdate.value]
})

const focusSummary = computed(() => displayPoint.value?.description ?? displayZone.value?.description ?? currentProfile.value.summary)
const focusLinks = computed(() => displayPoint.value?.relatedLinks ?? [])
const visibleHighlights = computed(() =>
  props.overview.mapHighlights
    .filter((highlight) => {
      const point = pointIndex.value.get(highlight.pointId)
      if (!point) return false
      if (point.layer !== activeLayer.value) return false
      if (filters.value.area !== '全部' && point.area !== filters.value.area) return false
      if (filters.value.riskLevel !== '全部' && point.riskLevel !== filters.value.riskLevel) return false
      return isInRange(point.occurredAt)
    })
    .slice(0, 6),
)

const guideInstruction = computed(() =>
  showAllHighlights.value ? '联线层已展开，可从地图顶端或底端直接进入重点信息详情。' : currentProfile.value.instruction,
)

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

const focusPoint = (pointId: string) => {
  const target = pointIndex.value.get(pointId)
  if (!target) return

  selectedPointId.value = pointId
  selectedZoneId.value = props.overview.zones.find((zone) => zone.featuredPointIds.includes(pointId))?.id ?? ''

  const tickerIndex = currentTicker.value.findIndex((item) => item.relatedPointId === pointId)
  if (tickerIndex >= 0) {
    activeTickerIndex.value = tickerIndex
  }
}

const focusZone = (zoneId: string) => {
  const zone = props.overview.zones.find((item) => item.id === zoneId)
  if (!zone) return

  selectedZoneId.value = zoneId
  selectedPointId.value = ''
  filters.value.area = zone.name
}

const focusTopPriorityPoint = async () => {
  await nextTick()

  if (priorityPoints.value.length) {
    focusPoint(priorityPoints.value[0].id)
    return
  }

  if (defaultZone.value) {
    focusZone(defaultZone.value.id)
  }
}

const openSelectedDetail = () => {
  if (displayPoint.value) {
    openRouteLink(displayPoint.value.detailTarget)
    return
  }

  if (displayZone.value) {
    router.push({
      path: '/smart-search',
      query: { keyword: displayZone.value.keyword, region: '海淀区', mode: '语义检索', from: 'haidian-cockpit' },
    })
    return
  }

  ElMessage.info('请先选择点位或片区')
}

const openSearch = (keyword?: string) => {
  router.push({
    path: '/smart-search',
    query: {
      keyword: keyword ?? displayPoint.value?.title ?? displayZone.value?.keyword ?? '海淀区态势',
      region: '海淀区',
      mode: '语义检索',
      from: 'haidian-cockpit',
    },
  })
}

const openModuleDetail = (module: DashboardCockpitOverview['systemMetrics'][number]) => {
  openRouteLink(module.detailTarget)
}

const openBusinessBoard = (board: DashboardCockpitOverview['businessBoardMetrics'][number]) => {
  openRouteLink(board.detailTarget)
}

const openHighlight = (highlight: DashboardMapHighlight) => {
  hoveredPointId.value = ''
  focusPoint(highlight.pointId)
  openRouteLink(highlight.detailTarget)
}

const handleMapLayoutChange = (layout: DashboardMapLayoutSnapshot) => {
  mapLayout.value = layout
}

const handleMapPointerMove = (event: MouseEvent) => {
  const currentTarget = event.currentTarget as HTMLElement | null
  if (!currentTarget) return

  const rect = currentTarget.getBoundingClientRect()
  mapCursor.value = {
    visible: true,
    x: event.clientX - rect.left,
    y: event.clientY - rect.top,
  }
}

const handleMapPointerLeave = () => {
  mapCursor.value = { visible: false, x: 0, y: 0 }
  hoveredPointId.value = ''
}

const setLayer = async (layer: DashboardCockpitLayer) => {
  activeLayer.value = layer
  filters.value.area = '全部'
  filters.value.riskLevel = '全部'
  selectedPointId.value = ''
  selectedZoneId.value = ''
  hoveredPointId.value = ''
  await focusTopPriorityPoint()
}

const handleSignalClick = async (signalId: string) => {
  const target = summarySignalMap[signalId]
  if (!target) return

  activeLayer.value = target.layer
  filters.value.area = '全部'
  filters.value.riskLevel = target.risk ?? '全部'
  selectedZoneId.value = ''
  selectedPointId.value = ''
  hoveredPointId.value = ''

  await focusTopPriorityPoint()
}

const handleBasemapError = () => {
  activeBasemap.value = '驾驶舱暗色'
  ElMessage.warning('在线底图加载失败，已自动回退')
}

const toggleHighlightPanel = () => {
  showAllHighlights.value = !showAllHighlights.value
}

const clearTickerAutoplay = () => {
  if (tickerTimer !== null) {
    window.clearInterval(tickerTimer)
    tickerTimer = null
  }
}

const restartTickerAutoplay = () => {
  clearTickerAutoplay()

  if (isPlaybackPaused.value || isStreamHovered.value || currentTicker.value.length <= 1) {
    return
  }

  tickerTimer = window.setInterval(() => {
    activeTickerIndex.value = (activeTickerIndex.value + 1) % currentTicker.value.length
  }, 3800)
}

const setTickerIndex = (index: number) => {
  const target = currentTicker.value[index]
  if (!target) return
  activeTickerIndex.value = index
}

const playPreviousTicker = () => {
  if (!currentTicker.value.length) return
  const prevIndex = (activeTickerIndex.value - 1 + currentTicker.value.length) % currentTicker.value.length
  setTickerIndex(prevIndex)
}

const playNextTicker = () => {
  if (!currentTicker.value.length) return
  const nextIndex = (activeTickerIndex.value + 1) % currentTicker.value.length
  setTickerIndex(nextIndex)
}

const togglePlayback = () => {
  isPlaybackPaused.value = !isPlaybackPaused.value
}

const handleTickerHoverStart = () => {
  isStreamHovered.value = true
}

const handleTickerHoverEnd = () => {
  isStreamHovered.value = false
}

const openTickerDetail = async (index: number) => {
  const target = currentTicker.value[index]
  if (!target) return

  activeTickerIndex.value = index

  if (target.layer !== activeLayer.value) {
    activeLayer.value = target.layer
    filters.value.area = '全部'
    filters.value.riskLevel = '全部'
    selectedZoneId.value = ''
    selectedPointId.value = ''
    hoveredPointId.value = ''
    await nextTick()
  }

  focusPoint(target.relatedPointId)

  const point = pointIndex.value.get(target.relatedPointId)
  if (point) {
    openRouteLink(point.detailTarget)
    return
  }

  openSelectedDetail()
}

watch(
  () => currentTicker.value.map((item) => item.id).join(','),
  () => {
    activeTickerIndex.value = 0
    restartTickerAutoplay()
  },
)

watch(
  () => [isPlaybackPaused.value, isStreamHovered.value].join('|'),
  () => {
    restartTickerAutoplay()
  },
)

watch(
  () => [activeLayer.value, filters.value.timeRange, filters.value.area, filters.value.riskLevel].join('|'),
  () => {
    if (selectedPointId.value && !filteredPoints.value.some((point) => point.id === selectedPointId.value)) {
      selectedPointId.value = ''
    }

    if (selectedZoneId.value && !filteredZones.value.some((zone) => zone.id === selectedZoneId.value)) {
      selectedZoneId.value = ''
    }
  },
)

watch(
  () => isSidebarHidden.value,
  () => {
    window.setTimeout(() => updateGridCellHeight(), 260)
  },
)

onMounted(async () => {
  const savedLayout = window.localStorage.getItem(LAYOUT_STORAGE_KEY)
  if (savedLayout) {
    try {
      panelLayouts.value = reconcileLayouts(JSON.parse(savedLayout) as CockpitPanelLayout[])
    } catch {
      panelLayouts.value = defaultPanelLayouts()
    }
  } else {
    panelLayouts.value = defaultPanelLayouts()
  }

  await nextTick()
  await initGrid()

  if (workspaceRef.value) {
    workspaceResizeObserver = new ResizeObserver(() => updateGridCellHeight())
    workspaceResizeObserver.observe(workspaceRef.value)
  }

  await focusTopPriorityPoint()
  restartTickerAutoplay()
})

onBeforeUnmount(() => {
  clearTickerAutoplay()
  workspaceResizeObserver?.disconnect()
  workspaceResizeObserver = null
  destroyGrid()
})
</script>

<template>
  <section class="haidian-cockpit">
    <div ref="workspaceRef" class="haidian-cockpit__workspace">
      <div ref="gridRootRef" class="haidian-cockpit__grid grid-stack">
        <section
          v-for="panel in displayPanels"
          :key="panel.id"
          class="grid-stack-item haidian-cockpit__panel-item"
          :data-panel-id="panel.id"
          :gs-id="panel.id"
          :gs-x="panel.x"
          :gs-y="panel.y"
          :gs-w="panel.w"
          :gs-h="panel.h"
          :gs-min-w="panel.minW"
          :gs-max-w="panel.maxW"
          :gs-min-h="panel.minH"
          :gs-max-h="panel.maxH"
        >
          <div class="grid-stack-item-content">
            <article class="haidian-cockpit__panel-shell" :class="`haidian-cockpit__panel-shell--${panel.id}`">
              <SituationLeftPulsePanel
                v-if="panel.id === 'left'"
                class="haidian-cockpit__left-panel"
                :modules="props.overview.systemMetrics"
                @open-module="openModuleDetail"
              />

              <div
                v-else-if="panel.id === 'map'"
                class="haidian-cockpit__map-column"
                @mousemove="handleMapPointerMove"
                @mouseleave="handleMapPointerLeave"
              >
                <div class="haidian-cockpit__overlay">
                  <SituationMapOverlayBar
                    :signal-items="signalItems"
                    :layers="props.overview.layers"
                    :active-layer="activeLayer"
                    :is-highlight-expanded="showAllHighlights"
                    @signal-click="handleSignalClick"
                    @layer-select="setLayer"
                    @toggle-highlights="toggleHighlightPanel"
                  />
                </div>

                <HaidianCockpitMapStage
                  class="haidian-cockpit__map-stage"
                  :district="props.overview.district"
                  :map-bounds="props.overview.mapBounds"
                  :points="filteredPoints"
                  :zones="filteredZones"
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
                  @open-highlight="openHighlight"
                  @hover-highlight="(pointId) => (hoveredPointId = pointId)"
                  @leave-highlight="hoveredPointId = ''"
                />

                <div
                  v-if="mapCursor.visible"
                  class="haidian-cockpit__cursor-glow"
                  :style="{ left: `${mapCursor.x}px`, top: `${mapCursor.y}px` }"
                />
              </div>

              <SituationSignalRail
                v-else-if="panel.id === 'rail'"
                class="haidian-cockpit__signal-panel"
                :active-layer="activeLayer"
                :display-point="displayPoint"
                :display-zone="displayZone"
                :focus-summary="focusSummary"
                :focus-meta="focusMeta"
                :guide-instruction="guideInstruction"
                :focus-links="focusLinks"
                :zone-points="selectedZonePoints"
                :current-topics="currentTopics"
                :ticker-items="currentTicker"
                :active-ticker-index="activeTickerIndex"
                :is-playback-paused="isPlaybackPaused"
                @open-detail="openSelectedDetail"
                @open-search="() => openSearch(displayPoint?.title ?? displayZone?.keyword)"
                @open-link="openRouteLink"
                @topic-click="openSearch"
                @focus-point="focusPoint"
                @select-ticker="openTickerDetail"
                @previous="playPreviousTicker"
                @next="playNextTicker"
                @toggle-playback="togglePlayback"
                @stream-hover-start="handleTickerHoverStart"
                @stream-hover-end="handleTickerHoverEnd"
              />

              <SituationBottomInfoBand
                v-else
                class="haidian-cockpit__bottom-band"
                :boards="props.overview.businessBoardMetrics"
                @open-board="openBusinessBoard"
              />
            </article>
          </div>
        </section>
      </div>
    </div>
  </section>
</template>

<style scoped>
.haidian-cockpit {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  height: 100%;
  overflow: hidden;
}

.haidian-cockpit__workspace,
.haidian-cockpit__grid {
  min-width: 0;
  min-height: 0;
  height: 100%;
}

.haidian-cockpit__panel-item > .grid-stack-item-content {
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
  background:
    linear-gradient(180deg, rgba(8, 14, 24, 0.78) 0%, rgba(7, 14, 24, 0.62) 100%);
  box-shadow: 0 18px 44px rgba(2, 8, 15, 0.18);
  cursor: grab;
}

.haidian-cockpit__panel-shell--left,
.haidian-cockpit__panel-shell--rail,
.haidian-cockpit__panel-shell--bottom {
  padding: 8px;
}

.haidian-cockpit__panel-shell--map {
  padding: 6px;
}

.haidian-cockpit__left-panel,
.haidian-cockpit__signal-panel,
.haidian-cockpit__bottom-band {
  min-width: 0;
  min-height: 0;
  height: 100%;
  width: 100%;
}

.haidian-cockpit__map-column {
  position: relative;
  display: flex;
  flex: 1 1 auto;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  height: 100%;
}

.haidian-cockpit__map-column::before {
  content: '';
  position: absolute;
  inset: 10px;
  z-index: 410;
  border: 1px solid rgba(101, 168, 246, 0.04);
  border-radius: 24px;
  background:
    linear-gradient(rgba(84, 129, 191, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(84, 129, 191, 0.04) 1px, transparent 1px);
  background-size: 48px 48px;
  pointer-events: none;
  mix-blend-mode: screen;
}

.haidian-cockpit__map-column::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 24px;
  background:
    radial-gradient(circle at 10% 16%, rgba(72, 158, 255, 0.12), transparent 30%),
    radial-gradient(circle at 84% 18%, rgba(58, 210, 198, 0.08), transparent 26%);
  pointer-events: none;
}

.haidian-cockpit__overlay {
  position: absolute;
  top: 8px;
  left: 8px;
  right: 8px;
  z-index: 430;
  padding-right: 76px;
}

.haidian-cockpit__map-stage {
  flex: 1 1 auto;
  min-height: 0;
}

.haidian-cockpit__cursor-glow {
  position: absolute;
  z-index: 434;
  width: 24px;
  height: 24px;
  margin-left: -12px;
  margin-top: -12px;
  border-radius: 999px;
  pointer-events: none;
  background: radial-gradient(circle, rgba(135, 198, 255, 0.78) 0%, rgba(80, 168, 255, 0.18) 42%, transparent 72%);
  box-shadow: 0 0 28px rgba(96, 180, 255, 0.28);
}

.haidian-cockpit :deep(.grid-stack-placeholder > .placeholder-content) {
  border: 1px dashed rgba(127, 182, 255, 0.32);
  border-radius: 24px;
  background: rgba(12, 22, 35, 0.26);
}

.haidian-cockpit :deep(.ui-resizable-handle) {
  opacity: 0;
  background: transparent;
  transition: opacity 0.18s ease;
}

.haidian-cockpit :deep(.grid-stack-item:hover .ui-resizable-handle),
.haidian-cockpit :deep(.grid-stack-item.ui-resizable-resizing .ui-resizable-handle) {
  opacity: 0.12;
}

.haidian-cockpit :deep(.grid-stack-item.ui-draggable-dragging .haidian-cockpit__panel-shell) {
  cursor: grabbing;
}

.haidian-cockpit__panel-shell :deep(button),
.haidian-cockpit__panel-shell :deep(a) {
  cursor: pointer;
}

@media (max-width: 1180px) {
  .haidian-cockpit {
    height: auto;
    min-height: calc(100vh - 110px);
    overflow: visible;
  }

  .haidian-cockpit__workspace,
  .haidian-cockpit__grid {
    height: auto;
    min-height: 0;
  }
}
</style>
