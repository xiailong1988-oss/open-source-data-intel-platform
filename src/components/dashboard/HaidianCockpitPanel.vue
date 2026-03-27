
<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Hide, RefreshRight, Setting, View } from '@element-plus/icons-vue'
import HaidianCockpitMapStage from './HaidianCockpitMapStage.vue'
import { useAppStore } from '../../stores/app'
import type {
  DashboardCockpitBasemap,
  DashboardCockpitDisplayMode,
  DashboardCockpitLayer,
  DashboardCockpitLink,
  DashboardCockpitOverview,
  DashboardCockpitRiskLevel,
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

const activeLayer = ref<DashboardCockpitLayer>(props.initialLayer)
const activeBasemap = ref<DashboardCockpitBasemap>(props.overview.baseMapOptions[0] ?? 'OSM 标准')
const selectedPointId = ref('')
const selectedZoneId = ref('')
const activeTickerIndex = ref(0)
const activePanel = ref<'layers' | 'filters' | 'basemap' | ''>('')
const basemapNotice = ref('')
let tickerTimer: number | null = null

const filters = reactive({
  timeRange: '今日' as '今日' | '近7日',
  area: '全部' as string,
  riskLevel: '全部' as DashboardCockpitRiskLevel | '全部',
  displayMode: '驾驶舱降噪' as DashboardCockpitDisplayMode,
})

const timeRangeOptions = ['今日', '近7日'] as const
const riskOptions: Array<DashboardCockpitRiskLevel | '全部'> = ['全部', '高', '中', '低']
const displayModeOptions: DashboardCockpitDisplayMode[] = ['驾驶舱降噪', '扩展标注']

const layerTagTypeMap: Record<DashboardCockpitLayer, 'danger' | 'warning' | 'success' | 'info'> = {
  风险预警: 'danger',
  突发事件: 'warning',
  热点事件: 'success',
  重点关注: 'info',
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

const layerPoints = computed(() => props.overview.points.filter((point) => point.layer === activeLayer.value))
const areaOptions = computed(() => ['全部', ...Array.from(new Set(layerPoints.value.map((point) => point.area)))])

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
const highlightedPointIds = computed(() => {
  const quota = filters.displayMode === '驾驶舱降噪' ? 3 : 6
  return priorityPoints.value.slice(0, quota).map((point) => point.id)
})

const currentTicker = computed(() =>
  props.overview.ticker
    .filter((item) => item.layer === activeLayer.value)
    .filter((item) => (filters.area === '全部' ? true : item.area === filters.area))
    .slice(0, 6),
)

const currentTopics = computed(() => props.overview.topics.filter((item) => item.layer === activeLayer.value).slice(0, 3))
const currentProfile = computed(() => props.overview.layerProfiles[activeLayer.value])
const selectedPoint = computed(() => filteredPoints.value.find((point) => point.id === selectedPointId.value) ?? null)
const selectedZone = computed(() => filteredZones.value.find((zone) => zone.id === selectedZoneId.value) ?? null)
const activeTickerItem = computed(() => currentTicker.value[activeTickerIndex.value] ?? null)
const defaultZone = computed(() => [...filteredZones.value].sort((left, right) => right.weight - left.weight)[0] ?? null)
const defaultPoint = computed(() => {
  const tickerPointId = activeTickerItem.value?.relatedPointId
  return priorityPoints.value.find((point) => point.id === tickerPointId) ?? priorityPoints.value[0] ?? null
})
const displayPoint = computed(() => selectedPoint.value ?? (!selectedZone.value ? defaultPoint.value : null))
const displayZone = computed(() => selectedZone.value ?? (!displayPoint.value ? defaultZone.value : null))
const selectedZonePoints = computed(() => {
  const zone = displayZone.value
  if (!zone) return []
  return filteredPoints.value.filter((point) => zone.featuredPointIds.includes(point.id)).slice(0, 6)
})

const signalItems = computed(() => [
  {
    id: 'high-risk',
    label: '高风险',
    value: `${props.overview.points.filter((item) => item.riskLevel === '高').length}`,
    hint: props.overview.points.find((item) => item.riskLevel === '高')?.title ?? '暂无高风险',
  },
  {
    id: 'emergency',
    label: '突发',
    value: `${props.overview.points.filter((item) => item.layer === '突发事件').length}`,
    hint: props.overview.points.find((item) => item.layer === '突发事件')?.title ?? '暂无突发事件',
  },
  {
    id: 'hot-zones',
    label: '热点片区',
    value: `${props.overview.zones.filter((item) => item.heat >= 84).length}`,
    hint: props.overview.zones.find((item) => item.heat >= 84)?.name ?? '暂无热点片区',
  },
  {
    id: 'watch',
    label: '盯防',
    value: `${props.overview.points.filter((item) => item.layer === '重点关注').length}`,
    hint: props.overview.points.find((item) => item.layer === '重点关注')?.title ?? '暂无重点盯防',
  },
])

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

/**
 * 用户点击信号条或切换图层后，必须直接看到一个具体对象，
 * 不能只切换筛选条件却不给详情承接。
 */
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
const selectTickerItem = (index: number) => {
  const target = currentTicker.value[index]
  if (!target) return
  activeTickerIndex.value = index
  focusPoint(target.relatedPointId)
}

const handleSignalClick = async (signalId: string) => {
  activePanel.value = ''
  filters.area = '全部'
  selectedZoneId.value = ''
  selectedPointId.value = ''

  if (signalId === 'high-risk') {
    activeLayer.value = '风险预警'
    filters.riskLevel = '高'
    await focusTopPriorityPoint()
    return
  }

  if (signalId === 'emergency') {
    activeLayer.value = '突发事件'
    filters.riskLevel = '全部'
    await focusTopPriorityPoint()
    return
  }

  if (signalId === 'hot-zones') {
    activeLayer.value = '热点事件'
    filters.riskLevel = '全部'
    await nextTick()
    if (defaultZone.value) {
      focusZone(defaultZone.value.id)
    }
    return
  }

  activeLayer.value = '重点关注'
  filters.riskLevel = '全部'
  await focusTopPriorityPoint()
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

const openLayerList = () => {
  if (activeLayer.value === '风险预警') {
    router.push({ path: '/analysis-warning/records', query: { keyword: filters.area === '全部' ? '海淀区' : filters.area, from: 'haidian-cockpit' } })
    return
  }

  if (activeLayer.value === '重点关注') {
    router.push({ path: '/knowledge-build/entity-management', query: { keyword: filters.area === '全部' ? '海淀区' : filters.area, from: 'haidian-cockpit' } })
    return
  }

  router.push({ path: '/knowledge-build/event-management', query: { keyword: filters.area === '全部' ? '海淀区' : filters.area, from: 'haidian-cockpit' } })
}

const resetView = async () => {
  activeLayer.value = props.initialLayer
  activeBasemap.value = props.overview.baseMapOptions[0] ?? 'OSM 标准'
  filters.timeRange = '今日'
  filters.area = '全部'
  filters.riskLevel = '全部'
  filters.displayMode = '驾驶舱降噪'
  selectedPointId.value = ''
  selectedZoneId.value = ''
  activeTickerIndex.value = 0
  activePanel.value = ''
  basemapNotice.value = ''
  await focusTopPriorityPoint()
  ElMessage.success('已重置海淀驾驶舱视图')
}

const toggleSidebarVisibility = () => {
  const nextHidden = !isSidebarHidden.value
  appStore.toggleSidebarVisibility()
  ElMessage.success(nextHidden ? '已切换为专注视图' : '已展开侧栏')
}

const togglePanel = (panel: 'layers' | 'filters' | 'basemap') => {
  activePanel.value = activePanel.value === panel ? '' : panel
}

const setLayer = async (layer: DashboardCockpitLayer) => {
  activeLayer.value = layer
  filters.area = '全部'
  filters.riskLevel = '全部'
  selectedPointId.value = ''
  selectedZoneId.value = ''
  activeTickerIndex.value = 0
  await focusTopPriorityPoint()
}

const setBasemap = (basemap: DashboardCockpitBasemap) => {
  activeBasemap.value = basemap
  basemapNotice.value = basemap === 'OSM 标准' ? '已切换到在线 OSM 底图' : '已切换到驾驶舱暗色底图'
}

const handleBasemapError = () => {
  if (activeBasemap.value === 'OSM 标准') {
    activeBasemap.value = '驾驶舱暗色'
    basemapNotice.value = '在线底图加载失败，已自动回退到驾驶舱暗色底图'
    ElMessage.warning('在线底图加载失败，已自动回退')
  }
}

const clearTickerAutoplay = () => {
  if (tickerTimer !== null) {
    window.clearInterval(tickerTimer)
    tickerTimer = null
  }
}

const restartTickerAutoplay = () => {
  clearTickerAutoplay()

  if (currentTicker.value.length <= 1) {
    return
  }

  tickerTimer = window.setInterval(() => {
    activeTickerIndex.value = (activeTickerIndex.value + 1) % currentTicker.value.length
    const nextItem = currentTicker.value[activeTickerIndex.value]
    if (nextItem) {
      focusPoint(nextItem.relatedPointId)
    }
  }, 5200)
}

watch(
  () => currentTicker.value.map((item) => item.id).join(','),
  async () => {
    activeTickerIndex.value = 0
    restartTickerAutoplay()
    if (!selectedPointId.value && currentTicker.value[0]) {
      focusPoint(currentTicker.value[0].relatedPointId)
    }
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
    <el-card shadow="never" class="haidian-cockpit__card">
      <div class="haidian-cockpit__chrome">
        <div class="haidian-cockpit__identity">
          <span class="haidian-cockpit__eyebrow">Haidian GIS</span>
          <small>{{ props.overview.district }}</small>
        </div>

        <div class="haidian-cockpit__status-strip">
          <span class="haidian-cockpit__status-pill">{{ activeLayer }}</span>
          <span class="haidian-cockpit__status-pill">{{ activeBasemap }}</span>
          <span class="haidian-cockpit__status-pill">最新 {{ latestUpdate }}</span>
          <span v-if="basemapNotice" class="haidian-cockpit__status-pill haidian-cockpit__status-pill--notice">{{ basemapNotice }}</span>
        </div>

        <div class="haidian-cockpit__chrome-actions">
          <el-button text size="small" :icon="isSidebarHidden ? View : Hide" @click="toggleSidebarVisibility">
            {{ isSidebarHidden ? '展开侧栏' : '专注视图' }}
          </el-button>
          <el-button text size="small" :icon="RefreshRight" @click="resetView">重置视图</el-button>
        </div>
      </div>

      <div class="haidian-cockpit__workspace">
        <div class="haidian-cockpit__map-stage">
          <HaidianCockpitMapStage
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

          <div class="haidian-cockpit__signal-strip">
            <button
              v-for="item in signalItems"
              :key="item.id"
              type="button"
              class="haidian-cockpit__signal-chip"
              @click="handleSignalClick(item.id)"
            >
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
              <small>{{ item.hint }}</small>
            </button>
          </div>

          <div class="haidian-cockpit__toolbar">
            <button type="button" class="haidian-cockpit__toolbar-button" :class="{ 'is-active': activePanel === 'layers' }" @click="togglePanel('layers')">
              <el-icon><Setting /></el-icon>
              图层
            </button>
            <button type="button" class="haidian-cockpit__toolbar-button" :class="{ 'is-active': activePanel === 'filters' }" @click="togglePanel('filters')">
              <el-icon><Setting /></el-icon>
              筛选
            </button>
            <button type="button" class="haidian-cockpit__toolbar-button" :class="{ 'is-active': activePanel === 'basemap' }" @click="togglePanel('basemap')">
              <el-icon><Setting /></el-icon>
              底图
            </button>
          </div>
          <transition name="panel-fade">
            <div v-if="activePanel" class="haidian-cockpit__control-panel">
              <template v-if="activePanel === 'layers'">
                <div class="haidian-cockpit__panel-section">
                  <strong>专题图层</strong>
                  <div class="haidian-cockpit__layer-list">
                    <button
                      v-for="layer in props.overview.layers"
                      :key="layer"
                      type="button"
                      class="haidian-cockpit__layer-button"
                      :class="{ 'is-active': layer === activeLayer }"
                      @click="setLayer(layer)"
                    >
                      {{ layer }}
                    </button>
                  </div>
                </div>
                <div class="haidian-cockpit__panel-section">
                  <strong>专题联动</strong>
                  <button v-for="topic in currentTopics" :key="topic.id" type="button" class="haidian-cockpit__topic-button" @click="openSearch(topic.keyword)">
                    <span>{{ topic.title }}</span>
                    <small>{{ topic.description }}</small>
                  </button>
                </div>
              </template>

              <template v-else-if="activePanel === 'filters'">
                <div class="haidian-cockpit__panel-section">
                  <strong>时间</strong>
                  <div class="haidian-cockpit__option-row">
                    <button
                      v-for="item in timeRangeOptions"
                      :key="item"
                      type="button"
                      class="haidian-cockpit__option-button"
                      :class="{ 'is-active': filters.timeRange === item }"
                      @click="filters.timeRange = item"
                    >
                      {{ item }}
                    </button>
                  </div>
                </div>
                <div class="haidian-cockpit__panel-section">
                  <strong>风险等级</strong>
                  <div class="haidian-cockpit__option-row">
                    <button
                      v-for="item in riskOptions"
                      :key="item"
                      type="button"
                      class="haidian-cockpit__option-button"
                      :class="{ 'is-active': filters.riskLevel === item }"
                      @click="filters.riskLevel = item"
                    >
                      {{ item }}
                    </button>
                  </div>
                </div>
                <div class="haidian-cockpit__panel-section">
                  <strong>片区</strong>
                  <div class="haidian-cockpit__option-grid">
                    <button
                      v-for="item in areaOptions"
                      :key="item"
                      type="button"
                      class="haidian-cockpit__option-button"
                      :class="{ 'is-active': filters.area === item }"
                      @click="filters.area = item"
                    >
                      {{ item }}
                    </button>
                  </div>
                </div>
                <div class="haidian-cockpit__panel-section">
                  <strong>观察方式</strong>
                  <div class="haidian-cockpit__option-row">
                    <button
                      v-for="mode in displayModeOptions"
                      :key="mode"
                      type="button"
                      class="haidian-cockpit__option-button"
                      :class="{ 'is-active': filters.displayMode === mode }"
                      @click="filters.displayMode = mode"
                    >
                      {{ mode }}
                    </button>
                  </div>
                </div>
              </template>

              <template v-else>
                <div class="haidian-cockpit__panel-section">
                  <strong>底图切换</strong>
                  <div class="haidian-cockpit__option-row">
                    <button
                      v-for="item in props.overview.baseMapOptions"
                      :key="item"
                      type="button"
                      class="haidian-cockpit__option-button"
                      :class="{ 'is-active': activeBasemap === item }"
                      @click="setBasemap(item)"
                    >
                      {{ item }}
                    </button>
                  </div>
                  <small class="haidian-cockpit__panel-note">OSM 标准提供更真实的道路和建筑语义；若网络波动，会自动回退到驾驶舱暗色底图。</small>
                </div>
              </template>
            </div>
          </transition>

          <div class="haidian-cockpit__map-note">
            <span>{{ currentProfile.priorityLabel }}</span>
            <small>{{ currentProfile.instruction }}</small>
          </div>
        </div>

        <aside class="haidian-cockpit__intel-rail">
          <div class="intel-rail__header">
            <div>
              <strong>重点情报流</strong>
              <span>沿地图右侧持续滚动，点击即可反向聚焦地图点位。</span>
            </div>
            <el-button text size="small" @click="openLayerList">进入列表</el-button>
          </div>

          <el-scrollbar class="intel-rail__stream">
            <div class="intel-rail__stream-list">
              <button
                v-for="(item, index) in currentTicker"
                :key="item.id"
                type="button"
                class="intel-rail__stream-item"
                :class="{ 'is-active': index === activeTickerIndex }"
                @click="selectTickerItem(index)"
              >
                <span class="intel-rail__stream-time">{{ item.time }}</span>
                <strong>{{ item.title }}</strong>
                <small>{{ item.area }} · {{ item.summary }}</small>
              </button>
            </div>
          </el-scrollbar>

          <div class="intel-rail__focus-card">
            <div class="intel-rail__focus-head">
              <div>
                <span class="intel-rail__eyebrow">Focus Detail</span>
                <strong>{{ displayPoint ? displayPoint.title : displayZone ? displayZone.name : '海淀区当前焦点' }}</strong>
              </div>
              <el-tag size="small" :type="displayPoint ? layerTagTypeMap[displayPoint.layer] : layerTagTypeMap[activeLayer]">
                {{ displayPoint ? displayPoint.riskLevel : activeLayer }}
              </el-tag>
            </div>

            <p class="intel-rail__focus-summary">
              {{ displayPoint ? displayPoint.description : displayZone ? displayZone.description : currentProfile.summary }}
            </p>

            <div class="intel-rail__meta">
              <span v-if="displayPoint">{{ displayPoint.area }}</span>
              <span v-if="displayPoint">{{ displayPoint.category }}</span>
              <span v-if="displayPoint">{{ displayPoint.occurredAt }}</span>
              <span v-if="displayZone">热度 {{ displayZone.heat }}</span>
              <span v-if="displayZone">预警 {{ displayZone.alertCount }}</span>
              <span v-if="displayZone">事件 {{ displayZone.eventCount }}</span>
            </div>

            <div class="intel-rail__actions">
              <el-button type="primary" @click="openSelectedDetail">查看详情</el-button>
              <el-button @click="openSearch(displayPoint?.title ?? displayZone?.keyword)">进入检索</el-button>
            </div>

            <div v-if="displayPoint?.relatedLinks?.length" class="intel-rail__link-list">
              <button
                v-for="link in displayPoint.relatedLinks"
                :key="`${link.kind}-${link.id}`"
                type="button"
                class="intel-rail__link-item"
                @click="openRouteLink(link)"
              >
                {{ link.title }}
              </button>
            </div>

            <div v-else-if="selectedZonePoints.length" class="intel-rail__link-list">
              <button v-for="point in selectedZonePoints" :key="point.id" type="button" class="intel-rail__link-item" @click="focusPoint(point.id)">
                {{ point.title }}
              </button>
            </div>
          </div>
          <div class="intel-rail__subsection">
            <span class="intel-rail__subsection-label">热点片区</span>
            <div class="intel-rail__chip-group">
              <button v-for="zone in filteredZones.slice(0, 4)" :key="zone.id" type="button" class="intel-rail__chip" @click="focusZone(zone.id)">
                {{ zone.name }}
              </button>
            </div>
          </div>

          <div class="intel-rail__subsection">
            <span class="intel-rail__subsection-label">快捷入口</span>
            <div class="intel-rail__quick-grid">
              <button
                v-for="item in props.overview.quickLinks.slice(0, 4)"
                :key="item.id"
                type="button"
                class="intel-rail__quick-item"
                @click="router.push({ path: item.path, query: item.query })"
              >
                <strong>{{ item.title }}</strong>
                <span>{{ item.description }}</span>
              </button>
            </div>
          </div>
        </aside>
      </div>
    </el-card>
  </section>
</template>

<style scoped>
.haidian-cockpit {
  display: flex;
  flex-direction: column;
}

.haidian-cockpit__card {
  overflow: hidden;
  border: 1px solid var(--platform-border-subtle);
  border-radius: 30px;
  background: linear-gradient(180deg, rgba(8, 16, 28, 0.98) 0%, rgba(6, 12, 20, 0.98) 100%);
}

.haidian-cockpit__card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 18px;
}

.haidian-cockpit__chrome {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
}

.haidian-cockpit__identity {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.haidian-cockpit__eyebrow {
  display: inline-flex;
  border-radius: 999px;
  padding: 4px 10px;
  background: rgba(82, 140, 223, 0.16);
  color: #d9e8fb;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.haidian-cockpit__identity small {
  color: var(--platform-text-tertiary);
  font-size: 12px;
}

.haidian-cockpit__status-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.haidian-cockpit__status-pill {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 5px 10px;
  background: rgba(18, 31, 49, 0.84);
  color: var(--platform-text-secondary);
  font-size: 11px;
  line-height: 1;
}

.haidian-cockpit__status-pill--notice {
  color: #f8d48e;
}

.haidian-cockpit__chrome-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.haidian-cockpit__workspace {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(320px, 0.52fr);
  gap: 16px;
  align-items: stretch;
}

.haidian-cockpit__map-stage {
  position: relative;
  min-height: 780px;
}

.haidian-cockpit__signal-strip {
  position: absolute;
  top: 14px;
  left: 14px;
  right: 220px;
  z-index: 420;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
  pointer-events: none;
}

.haidian-cockpit__signal-chip {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 3px;
  border: 1px solid rgba(118, 170, 242, 0.22);
  border-radius: 16px;
  padding: 9px 10px;
  background: rgba(7, 16, 28, 0.74);
  text-align: left;
  pointer-events: auto;
  transition: border-color 0.2s ease, transform 0.2s ease, background 0.2s ease;
  cursor: pointer;
}

.haidian-cockpit__signal-chip:hover {
  transform: translateY(-1px);
  border-color: rgba(118, 170, 242, 0.42);
  background: rgba(11, 22, 36, 0.92);
}

.haidian-cockpit__signal-chip span {
  color: var(--platform-text-tertiary);
  font-size: 11px;
}

.haidian-cockpit__signal-chip strong {
  color: #f3f8ff;
  font-size: 15px;
}

.haidian-cockpit__signal-chip small {
  color: var(--platform-text-secondary);
  font-size: 11px;
  line-height: 1.45;
}

.haidian-cockpit__toolbar {
  position: absolute;
  top: 14px;
  right: 14px;
  z-index: 420;
  display: flex;
  gap: 8px;
}
.haidian-cockpit__toolbar-button,
.haidian-cockpit__layer-button,
.haidian-cockpit__option-button,
.haidian-cockpit__topic-button,
.intel-rail__stream-item,
.intel-rail__link-item,
.intel-rail__chip,
.intel-rail__quick-item {
  cursor: pointer;
}

.haidian-cockpit__toolbar-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 1px solid rgba(120, 170, 240, 0.22);
  border-radius: 999px;
  padding: 7px 12px;
  background: rgba(7, 16, 28, 0.78);
  color: #eff5ff;
  font-size: 12px;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.haidian-cockpit__toolbar-button:hover,
.haidian-cockpit__toolbar-button.is-active {
  border-color: rgba(120, 170, 240, 0.5);
  background: rgba(16, 31, 49, 0.92);
}

.haidian-cockpit__control-panel {
  position: absolute;
  top: 64px;
  right: 14px;
  z-index: 430;
  display: flex;
  max-height: 68%;
  width: 290px;
  flex-direction: column;
  gap: 14px;
  overflow: auto;
  border: 1px solid rgba(118, 170, 242, 0.22);
  border-radius: 22px;
  padding: 16px;
  background: rgba(7, 16, 28, 0.92);
  box-shadow: 0 24px 40px rgba(4, 10, 18, 0.36);
}

.haidian-cockpit__panel-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.haidian-cockpit__panel-section strong,
.intel-rail__header strong,
.intel-rail__focus-head strong,
.intel-rail__quick-item strong,
.intel-rail__subsection-label {
  color: #f3f8ff;
}

.haidian-cockpit__layer-list,
.haidian-cockpit__option-row,
.haidian-cockpit__option-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.haidian-cockpit__option-grid {
  max-height: 120px;
  overflow: auto;
}

.haidian-cockpit__layer-button,
.haidian-cockpit__option-button {
  border: 1px solid rgba(118, 170, 242, 0.2);
  border-radius: 999px;
  padding: 7px 12px;
  background: rgba(18, 31, 49, 0.78);
  color: var(--platform-text-secondary);
  font-size: 12px;
  transition: border-color 0.2s ease, background 0.2s ease, color 0.2s ease;
}

.haidian-cockpit__layer-button:hover,
.haidian-cockpit__layer-button.is-active,
.haidian-cockpit__option-button:hover,
.haidian-cockpit__option-button.is-active {
  border-color: rgba(118, 170, 242, 0.52);
  background: rgba(34, 63, 96, 0.92);
  color: #f3f8ff;
}

.haidian-cockpit__topic-button {
  display: flex;
  flex-direction: column;
  gap: 6px;
  border: 1px solid rgba(118, 170, 242, 0.16);
  border-radius: 16px;
  padding: 10px 12px;
  background: rgba(16, 27, 42, 0.76);
  text-align: left;
}

.haidian-cockpit__topic-button span {
  color: #eff5ff;
  font-size: 13px;
  font-weight: 600;
}

.haidian-cockpit__topic-button small,
.haidian-cockpit__panel-note,
.intel-rail__header span,
.intel-rail__stream-item small,
.intel-rail__quick-item span,
.intel-rail__focus-summary,
.intel-rail__meta,
.intel-rail__link-item,
.haidian-cockpit__map-note small {
  color: var(--platform-text-secondary);
}

.haidian-cockpit__map-note {
  position: absolute;
  left: 16px;
  bottom: 16px;
  z-index: 420;
  display: inline-flex;
  max-width: min(540px, calc(100% - 340px));
  flex-direction: column;
  gap: 3px;
  border: 1px solid rgba(118, 170, 242, 0.16);
  border-radius: 18px;
  padding: 10px 12px;
  background: rgba(7, 16, 28, 0.72);
}

.haidian-cockpit__map-note span {
  color: #f3f8ff;
  font-size: 12px;
  font-weight: 700;
}

.haidian-cockpit__intel-rail {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 14px;
}

.intel-rail__header,
.intel-rail__focus-card,
.intel-rail__subsection {
  border: 1px solid rgba(118, 170, 242, 0.16);
  border-radius: 22px;
  background: rgba(8, 16, 28, 0.9);
}

.intel-rail__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
}

.intel-rail__header div {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}

.intel-rail__stream {
  min-height: 280px;
  border: 1px solid rgba(118, 170, 242, 0.16);
  border-radius: 22px;
  background: rgba(8, 16, 28, 0.9);
}

.intel-rail__stream-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
}

.intel-rail__stream-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  border: 1px solid rgba(118, 170, 242, 0.14);
  border-radius: 16px;
  padding: 12px 12px 13px;
  background: rgba(13, 24, 39, 0.84);
  text-align: left;
  transition: border-color 0.2s ease, background 0.2s ease, transform 0.2s ease;
}

.intel-rail__stream-item:hover,
.intel-rail__stream-item.is-active {
  transform: translateY(-1px);
  border-color: rgba(118, 170, 242, 0.36);
  background: rgba(22, 42, 67, 0.92);
}

.intel-rail__stream-time {
  color: #8ebdff;
  font-size: 11px;
  font-weight: 700;
}

.intel-rail__stream-item strong {
  color: #eff5ff;
  font-size: 14px;
  line-height: 1.45;
}

.intel-rail__focus-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px;
}

.intel-rail__focus-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.intel-rail__focus-head div {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 6px;
}
.intel-rail__eyebrow {
  color: #8ebdff;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.intel-rail__focus-summary {
  margin: 0;
  line-height: 1.75;
}

.intel-rail__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  font-size: 12px;
}

.intel-rail__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.intel-rail__link-list,
.intel-rail__chip-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.intel-rail__link-item,
.intel-rail__chip {
  border: 1px solid rgba(118, 170, 242, 0.16);
  border-radius: 999px;
  padding: 7px 12px;
  background: rgba(13, 24, 39, 0.86);
  transition: border-color 0.2s ease, background 0.2s ease;
}

.intel-rail__link-item:hover,
.intel-rail__chip:hover,
.intel-rail__quick-item:hover {
  border-color: rgba(118, 170, 242, 0.38);
  background: rgba(22, 42, 67, 0.9);
}

.intel-rail__subsection {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 14px 16px 16px;
}

.intel-rail__quick-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.intel-rail__quick-item {
  display: flex;
  min-height: 90px;
  flex-direction: column;
  gap: 8px;
  border: 1px solid rgba(118, 170, 242, 0.14);
  border-radius: 16px;
  padding: 12px;
  background: rgba(13, 24, 39, 0.84);
  text-align: left;
}

.panel-fade-enter-active,
.panel-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.panel-fade-enter-from,
.panel-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

@media (max-width: 1680px) {
  .haidian-cockpit__workspace {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 1440px) {
  .haidian-cockpit__chrome {
    grid-template-columns: 1fr;
  }

  .haidian-cockpit__signal-strip {
    right: 14px;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .haidian-cockpit__map-stage {
    min-height: 760px;
  }
}

@media (max-width: 1080px) {
  .haidian-cockpit__signal-strip {
    position: static;
    margin-top: 12px;
  }

  .haidian-cockpit__toolbar,
  .haidian-cockpit__control-panel,
  .haidian-cockpit__map-note {
    position: static;
    margin-top: 12px;
  }

  .haidian-cockpit__control-panel {
    width: auto;
    max-height: none;
  }

  .intel-rail__quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
