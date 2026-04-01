<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import HaidianCockpitMapStage from './HaidianCockpitMapStage.vue'
import SituationMapOverlayBar from './SituationMapOverlayBar.vue'
import SituationSignalRail from './SituationSignalRail.vue'
import SituationTopBar from './SituationTopBar.vue'
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

const router = useRouter()
const appStore = useAppStore()
const { isSidebarHidden } = storeToRefs(appStore)
const mapStageRef = ref<InstanceType<typeof HaidianCockpitMapStage> | null>(null)

const activeLayer = ref<DashboardCockpitLayer>(props.initialLayer)
const providerFactoryState = getCockpitMapProviderFactoryState()
const activeBasemap = ref<DashboardCockpitBasemap>(props.overview.defaultBasemap ?? providerFactoryState.defaultBasemap)
const selectedPointId = ref('')
const selectedZoneId = ref('')
const activeTickerIndex = ref(0)
const basemapNotice = ref('')
const isPlaybackPaused = ref(false)
const isStreamHovered = ref(false)
let tickerTimer: number | null = null

const filters = reactive({
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
  return filters.timeRange === '今日' ? diff <= day : diff <= day * 7
}

const latestUpdate = computed(() =>
  [...props.overview.points].sort((left, right) => (left.occurredAt > right.occurredAt ? -1 : 1))[0]?.occurredAt ?? '暂无更新',
)

const signalItems = computed<DashboardCockpitHeadline[]>(() => props.overview.headlines)
const layerPoints = computed(() => props.overview.points.filter((point) => point.layer === activeLayer.value))
const filteredPoints = computed(() =>
  layerPoints.value.filter((point) => {
    if (filters.area !== '全部' && point.area !== filters.area) return false
    if (filters.riskLevel !== '全部' && point.riskLevel !== filters.riskLevel) return false
    return isInRange(point.occurredAt)
  }),
)

const filteredZones = computed(() =>
  props.overview.zones.filter((zone) => {
    if (filters.area !== '全部') {
      return zone.name === filters.area || zone.featuredPointIds.some((id) => filteredPoints.value.some((point) => point.id === id))
    }

    return zone.featuredPointIds.some((id) => filteredPoints.value.some((point) => point.id === id))
  }),
)

const priorityPoints = computed(() => [...filteredPoints.value].sort((left, right) => right.priority - left.priority))

/**
 * 首页默认仍然启用降噪，只展开最高优先级的少量点位。
 * 这次骨架替换不改变已有正确的地图降噪策略，只把视觉壳子彻底换掉。
 */
const highlightedPointIds = computed(() => {
  const quota = filters.displayMode === '驾驶舱降噪' ? 3 : 6
  return priorityPoints.value.slice(0, quota).map((point) => point.id)
})

/**
 * 右侧 signal rail 继续作为全局重要情报流，
 * 不再被当前图层或局部区域硬切碎，这样才能像参照站一样成为独立的信息解释层。
 */
const currentTicker = computed<DashboardCockpitTickerItem[]>(() => props.overview.ticker)
const currentTopics = computed(() => props.overview.topics.filter((item) => item.layer === activeLayer.value).slice(0, 2))
const currentProfile = computed(() => props.overview.layerProfiles[activeLayer.value])
const selectedPoint = computed(() => filteredPoints.value.find((point) => point.id === selectedPointId.value) ?? null)
const selectedZone = computed(() => filteredZones.value.find((zone) => zone.id === selectedZoneId.value) ?? null)
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

const focusPoint = (pointId: string) => {
  const target = filteredPoints.value.find((point) => point.id === pointId)
  if (!target) return

  selectedPointId.value = pointId
  selectedZoneId.value = props.overview.zones.find((zone) => zone.featuredPointIds.includes(pointId))?.id ?? ''

  const tickerIndex = currentTicker.value.findIndex((item) => item.relatedPointId === pointId)
  if (tickerIndex >= 0) {
    activeTickerIndex.value = tickerIndex
  }
}

const focusZone = (zoneId: string) => {
  const zone = filteredZones.value.find((item) => item.id === zoneId)
  if (!zone) return

  selectedZoneId.value = zoneId
  selectedPointId.value = ''
  filters.area = zone.name
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

const focusDistrictView = () => {
  selectedPointId.value = ''
  selectedZoneId.value = ''
  filters.area = '全部'
  mapStageRef.value?.focusDistrict()
}

const resetView = async () => {
  activeLayer.value = props.initialLayer
  activeBasemap.value = props.overview.defaultBasemap ?? providerFactoryState.defaultBasemap
  filters.timeRange = '今日'
  filters.area = '全部'
  filters.riskLevel = '全部'
  filters.displayMode = '驾驶舱降噪'
  selectedPointId.value = ''
  selectedZoneId.value = ''
  activeTickerIndex.value = 0
  basemapNotice.value = ''
  isPlaybackPaused.value = false
  await nextTick()
  await focusTopPriorityPoint()
  mapStageRef.value?.focusDistrict()
  ElMessage.success('已重置海淀驾驶舱视图')
}

const toggleSidebarVisibility = () => {
  const nextHidden = !isSidebarHidden.value
  appStore.toggleSidebarVisibility()
  ElMessage.success(nextHidden ? '已切换为专注视图' : '已展开侧栏')
}

const setLayer = async (layer: DashboardCockpitLayer) => {
  activeLayer.value = layer
  filters.area = '全部'
  filters.riskLevel = '全部'
  selectedPointId.value = ''
  selectedZoneId.value = ''
  await focusTopPriorityPoint()
}

const handleSignalClick = async (signalId: string) => {
  const target = summarySignalMap[signalId]
  if (!target) return

  activeLayer.value = target.layer
  filters.area = '全部'
  filters.riskLevel = target.risk ?? '全部'
  selectedZoneId.value = ''
  selectedPointId.value = ''

  await focusTopPriorityPoint()
}

const handleBasemapError = () => {
  activeBasemap.value = '驾驶舱暗色'
  basemapNotice.value = '在线底图加载失败，已自动回退到驾驶舱暗色底图'
  ElMessage.warning('在线底图加载失败，已自动回退')
}

const clearTickerAutoplay = () => {
  if (tickerTimer !== null) {
    window.clearInterval(tickerTimer)
    tickerTimer = null
  }
}

/**
 * 自动滚动只服务右侧 rail 自身。
 * 这是当前首页已经做对的逻辑，本轮替换骨架时必须保留，不能让地图再次被自动播放带着跳。
 */
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

/**
 * 只有手动点击情报条目时，地图才联动并打开详情。
 * 自动滚动、上一条、下一条都不能再触发这条链路。
 */
const openTickerDetail = async (index: number) => {
  const target = currentTicker.value[index]
  if (!target) return

  activeTickerIndex.value = index

  if (target.layer !== activeLayer.value) {
    activeLayer.value = target.layer
    filters.area = '全部'
    filters.riskLevel = '全部'
    selectedZoneId.value = ''
    selectedPointId.value = ''
    await nextTick()
  }

  focusPoint(target.relatedPointId)

  const point = props.overview.points.find((item) => item.id === target.relatedPointId)
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
  () => [activeLayer.value, filters.timeRange, filters.area, filters.riskLevel].join('|'),
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
  () => activeLayer.value,
  () => {
    basemapNotice.value = ''
  },
)

onMounted(async () => {
  await focusTopPriorityPoint()
  restartTickerAutoplay()
})

onBeforeUnmount(() => {
  clearTickerAutoplay()
})
</script>

<template>
  <section class="haidian-cockpit">
    <SituationTopBar
      :district="props.overview.district"
      :active-layer="activeLayer"
      :active-basemap="activeBasemap"
      :latest-update="latestUpdate"
      :basemap-notice="basemapNotice"
      :is-sidebar-hidden="isSidebarHidden"
      @focus-district="focusDistrictView"
      @toggle-sidebar="toggleSidebarVisibility"
      @reset-view="resetView"
    />

    <div class="haidian-cockpit__stage">
      <div class="haidian-cockpit__map-column">
        <div class="haidian-cockpit__overlay">
          <SituationMapOverlayBar
            :signal-items="signalItems"
            :layers="props.overview.layers"
            :active-layer="activeLayer"
            @signal-click="handleSignalClick"
            @layer-select="setLayer"
          />
        </div>

        <HaidianCockpitMapStage
          ref="mapStageRef"
          :district="props.overview.district"
          :map-bounds="props.overview.mapBounds"
          :points="filteredPoints"
          :zones="filteredZones"
          :selected-point-id="selectedPointId"
          :selected-zone-id="selectedZoneId"
          :highlighted-point-ids="highlightedPointIds"
          :active-layer="activeLayer"
          :active-basemap="activeBasemap"
          @select-point="focusPoint"
          @select-zone="focusZone"
          @basemap-error="handleBasemapError"
        />
      </div>

      <SituationSignalRail
        :active-layer="activeLayer"
        :display-point="displayPoint"
        :display-zone="displayZone"
        :focus-summary="focusSummary"
        :focus-meta="focusMeta"
        :guide-instruction="currentProfile.instruction"
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
    </div>
  </section>
</template>

<style scoped>
.haidian-cockpit {
  display: flex;
  min-width: 0;
  min-height: calc(100vh - 118px);
  flex-direction: column;
  gap: 10px;
}

.haidian-cockpit__stage {
  display: grid;
  grid-template-columns: minmax(0, 1fr) clamp(380px, 26vw, 450px);
  gap: 18px;
  min-height: 0;
  flex: 1 1 auto;
}

.haidian-cockpit__map-column {
  position: relative;
  min-width: 0;
  min-height: calc(100vh - 184px);
}

.haidian-cockpit__overlay {
  position: absolute;
  top: 12px;
  left: 12px;
  right: 12px;
  z-index: 430;
}

@media (max-width: 1880px) {
  .haidian-cockpit__stage {
    grid-template-columns: minmax(0, 1fr) clamp(360px, 27vw, 420px);
  }
}

@media (max-width: 1560px) {
  .haidian-cockpit__stage {
    grid-template-columns: 1fr;
  }

  .haidian-cockpit__map-column {
    min-height: 700px;
  }
}

@media (max-width: 1200px) {
  .haidian-cockpit__map-column {
    min-height: 620px;
  }
}
</style>
