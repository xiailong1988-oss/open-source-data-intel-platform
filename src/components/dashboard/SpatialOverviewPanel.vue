<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CollectionTag, FullScreen, InfoFilled, Loading, RefreshRight } from '@element-plus/icons-vue'
import { getMap, registerMap, type EChartsOption } from '../../lib/echarts'
import BaseEChart from '../charts/BaseEChart.vue'
import type {
  DashboardSpatialDefaultSummary,
  DashboardSpatialEventThread,
  DashboardSpatialLayer,
  DashboardSpatialLink,
  DashboardSpatialOverview,
  DashboardSpatialPoint,
  DashboardSpatialRiskLevel,
  DashboardSpatialTopicDeck,
} from '../../types/dashboard'

const props = withDefaults(
  defineProps<{
    overview: DashboardSpatialOverview
    eyebrow?: string
    title?: string
    description?: string
  }>(),
  {
    eyebrow: 'GIS Situation',
    title: '北京空间态势总览',
    description: '以北京行政区底图为主视图，围绕风险预警、重点事件和专题研判组织首页信息结构。',
  },
)

const router = useRouter()
const overview = props.overview

const MAP_NAME = 'platform-beijing-gis-map'
const mapLoading = ref(false)
const mapReady = ref(false)
const mapLoadError = ref('')

// GIS 态势图必须基于真实的北京行政区底图注册，
// 后续切回首页时直接复用已注册地图，避免重复请求和闪烁。
const ensureBeijingMap = async () => {
  if (getMap(MAP_NAME)) {
    mapReady.value = true
    return
  }

  mapLoading.value = true
  mapLoadError.value = ''

  try {
    const response = await fetch('/maps/beijing-full.json')
    if (!response.ok) {
      throw new Error(`北京底图加载失败：${response.status}`)
    }

    registerMap(MAP_NAME, await response.json())
    mapReady.value = true
  } catch (error) {
    mapLoadError.value = error instanceof Error ? error.message : '北京底图加载失败'
  } finally {
    mapLoading.value = false
  }
}

onMounted(() => {
  void ensureBeijingMap()
})

type TimePreset = '今日' | '近7日' | '近30日'
type RiskFilter = DashboardSpatialRiskLevel | '全部'
type ObjectTypeFilter = string | '全部'

const activeLayer = ref<DashboardSpatialLayer>('综合')
const legendVisible = ref(true)
const selectedPointId = ref('')
const selectedRegionId = ref('')

const filters = reactive({
  timeRange: '今日' as TimePreset,
  region: '全部',
  riskLevel: '全部' as RiskFilter,
  objectType: '全部' as ObjectTypeFilter,
})

const timeRangeOptions: TimePreset[] = ['今日', '近7日', '近30日']
const riskOptions: RiskFilter[] = ['全部', '高', '中', '低']

const layerColorMap: Record<DashboardSpatialLayer, string> = {
  综合: '#4c9cff',
  风险预警: '#df655d',
  重点事件: '#53c7b7',
  专题研判: '#f0b15f',
}

const layerActionLabelMap: Record<DashboardSpatialLayer, string> = {
  综合: '总览态势',
  风险预警: '风险处置',
  重点事件: '事件追踪',
  专题研判: '专题研判',
}

const alertTagTypeMap: Record<string, 'danger' | 'warning' | 'info' | 'success'> = {
  紧急: 'danger',
  高: 'warning',
  中: 'info',
  低: 'success',
}

const hexToRgb = (value: string) => {
  const normalized = value.replace('#', '')
  const segments =
    normalized.length === 3
      ? normalized.split('').map((item) => `${item}${item}`)
      : normalized.match(/.{1,2}/g) ?? ['4c', '9c', 'ff']
  return segments.map((segment) => Number.parseInt(segment, 16)).join(', ')
}

const resolveSymbol = (objectType: string) => {
  if (objectType.includes('预警')) return 'triangle'
  if (objectType.includes('事件')) return 'circle'
  if (objectType.includes('专题')) return 'diamond'
  if (objectType.includes('企业')) return 'rect'
  return 'roundRect'
}

const resolvePointSize = (point: DashboardSpatialPoint) => {
  if (point.riskLevel === '高') return 20
  if (point.riskLevel === '中') return 17
  if (point.layer === '综合') return 18
  return 15
}

const resolvePointColor = (point: DashboardSpatialPoint) => {
  if (point.riskLevel === '高') return '#df655d'
  if (point.riskLevel === '中') return '#f0b15f'
  if (point.riskLevel === '低') return '#68bd84'
  return layerColorMap[point.layer]
}

const referenceTime = new Date('2026-03-16T23:59:59').getTime()
const parseTime = (value: string) => {
  const timestamp = new Date(value.replace(/-/g, '/')).getTime()
  return Number.isNaN(timestamp) ? referenceTime : timestamp
}

const isInRange = (value: string) => {
  const diff = referenceTime - parseTime(value)
  const day = 24 * 60 * 60 * 1000
  if (filters.timeRange === '今日') return diff <= day
  if (filters.timeRange === '近7日') return diff <= day * 7
  return diff <= day * 30
}

const rawLayerPoints = computed(() =>
  props.overview.points.filter((point) => activeLayer.value === '综合' || point.layer === activeLayer.value),
)

const objectTypeOptions = computed<ObjectTypeFilter[]>(() => {
  const types = Array.from(new Set(rawLayerPoints.value.map((point) => point.objectType)))
  return ['全部', ...types]
})

const regionOptions = computed(() => ['全部', ...props.overview.regions.map((region) => region.name)])

const filteredPoints = computed(() =>
  rawLayerPoints.value.filter((point) => {
    if (filters.region !== '全部' && point.region !== filters.region) return false
    if (filters.riskLevel !== '全部' && point.riskLevel !== filters.riskLevel) return false
    if (filters.objectType !== '全部' && point.objectType !== filters.objectType) return false
    return isInRange(point.updatedAt)
  }),
)

const visibleRegions = computed(() =>
  props.overview.regions.filter((region) => filters.region === '全部' || region.name === filters.region),
)

const selectedPoint = computed(() => filteredPoints.value.find((point) => point.id === selectedPointId.value) ?? null)
const selectedRegion = computed(() => visibleRegions.value.find((region) => region.id === selectedRegionId.value) ?? null)
const currentSummary = computed<DashboardSpatialDefaultSummary>(() => props.overview.defaultSummary)
const currentLayerBrief = computed(() => currentSummary.value.layerBriefs[activeLayer.value])

const regionRanking = computed(() =>
  [...visibleRegions.value]
    .sort((left, right) => right.layerMetrics[activeLayer.value].heat - left.layerMetrics[activeLayer.value].heat)
    .slice(0, 6),
)

const hotspotRegions = computed(() => regionRanking.value.filter((region) => region.layerMetrics[activeLayer.value].heat >= 78).slice(0, 4))
const todayFocusRegions = computed(() =>
  visibleRegions.value
    .filter((region) => region.todayFocus)
    .sort((left, right) => right.layerMetrics[activeLayer.value].heat - left.layerMetrics[activeLayer.value].heat)
    .slice(0, 4),
)

const filteredEvents = computed(() =>
  props.overview.events
    .filter((event) => activeLayer.value === '综合' || event.layer === activeLayer.value)
    .filter((event) => (filters.region === '全部' ? true : event.region === filters.region))
    .slice(0, 5),
)

const filteredTopics = computed(() =>
  props.overview.topics
    .filter((topic) => activeLayer.value === '综合' || topic.layer === activeLayer.value)
    .filter((topic) => (filters.region === '全部' ? true : topic.region === filters.region))
    .sort((left, right) => right.heat - left.heat)
    .slice(0, 4),
)
const currentRegionPoints = computed(() => {
  if (!selectedRegion.value) return []
  return filteredPoints.value.filter((point) => point.region === selectedRegion.value?.name).slice(0, 6)
})

const highRiskPointCount = computed(() => filteredPoints.value.filter((point) => point.riskLevel === '高').length)
const regionMapData = computed(() =>
  props.overview.regions.map((region) => {
    const metric = region.layerMetrics[activeLayer.value]
    const isActive = region.id === selectedRegionId.value
    const regionVisible = filters.region === '全部' || region.name === filters.region
    const opacity = regionVisible ? 0.18 + metric.heat / 200 : 0.08
    return {
      name: region.name,
      value: metric.heat,
      itemStyle: {
        areaColor: isActive
          ? '#d8ebff'
          : `rgba(${hexToRgb(layerColorMap[activeLayer.value])}, ${Math.min(opacity, 0.64)})`,
        borderColor: isActive ? '#63b1ff' : 'rgba(144, 177, 215, 0.82)',
        borderWidth: isActive ? 2.6 : 1.2,
      },
      label: { color: isActive ? '#112e50' : '#d4e4ff', fontWeight: isActive ? 700 : 500 },
    }
  }),
)

const hotspotSeriesData = computed(() =>
  hotspotRegions.value.map((region) => ({
    name: `${region.name}热点`,
    value: [...region.center, region.layerMetrics[activeLayer.value].heat],
    regionId: region.id,
    regionName: region.name,
    topic: region.layerMetrics[activeLayer.value].mainTopic,
    kind: 'hotspot',
    symbolSize: 18 + region.layerMetrics[activeLayer.value].heat / 10,
  })),
)

const pointSeriesData = computed(() =>
  filteredPoints.value.map((point) => ({
    name: point.name,
    value: point.coords,
    pointId: point.id,
    regionId: props.overview.regions.find((region) => region.name === point.region)?.id,
    regionName: point.region,
    kind: 'point',
    symbol: resolveSymbol(point.objectType),
    symbolSize: resolvePointSize(point),
    itemStyle: {
      color: resolvePointColor(point),
      borderColor: point.id === selectedPointId.value ? '#ffffff' : '#091628',
      borderWidth: point.id === selectedPointId.value ? 2.4 : 1.1,
      shadowBlur: point.riskLevel === '高' ? 14 : 8,
      shadowColor: 'rgba(8, 22, 39, 0.42)',
    },
  })),
)

const legendItems = computed(() => {
  if (activeLayer.value === '风险预警') {
    return [
      { label: '高风险预警', color: '#df655d' },
      { label: '中风险预警', color: '#f0b15f' },
      { label: '低风险预警', color: '#68bd84' },
      { label: '热点区域', color: '#4c9cff' },
    ]
  }

  if (activeLayer.value === '重点事件') {
    return [
      { label: '重点事件', color: '#53c7b7' },
      { label: '保障任务', color: '#6fd8c9' },
      { label: '关联热区', color: '#4c9cff' },
    ]
  }

  if (activeLayer.value === '专题研判') {
    return [
      { label: '专题目标', color: '#f0b15f' },
      { label: '重点对象', color: '#ffcf80' },
      { label: '专题热区', color: '#4c9cff' },
    ]
  }

  return [
    { label: '重点对象', color: '#4c9cff' },
    { label: '热点区域', color: '#7cb8ff' },
    { label: '风险点位', color: '#df655d' },
  ]
})

const formatTooltip = (params: unknown) => {
  const payload = params as {
    name?: string
    data?: { kind?: 'point' | 'hotspot'; pointId?: string; regionName?: string; topic?: string }
  }

  if (payload.data?.kind === 'point' && payload.data.pointId) {
    const point = filteredPoints.value.find((item) => item.id === payload.data?.pointId)
    if (!point) return payload.name ?? ''
    return [
      `<strong>${point.name}</strong>`,
      `${point.region}${point.location ? ` · ${point.location}` : ''}`,
      `专题：${point.topic}`,
      `状态：${point.status}`,
      `更新时间：${point.updatedAt}`,
    ].join('<br/>')
  }

  if (payload.data?.kind === 'hotspot') {
    return [`<strong>${payload.data.regionName}</strong>`, `热点主题：${payload.data.topic ?? '-'}`, '点击后进入区域专题结果'].join('<br/>')
  }

  const region = props.overview.regions.find((item) => item.name === payload.name)
  if (!region) return payload.name ?? ''
  const metric = region.layerMetrics[activeLayer.value]
  return [
    `<strong>${region.name}</strong>`,
    region.summary,
    `热度值：${metric.heat}`,
    `对象总数：${metric.objectCount}`,
    `预警数：${metric.alertCount}`,
    `事件数：${metric.eventCount}`,
    `主热点：${metric.mainTopic}`,
  ].join('<br/>')
}

const mapOption = computed<EChartsOption>(() => ({
  animationDurationUpdate: 320,
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(7, 18, 32, 0.92)',
    borderColor: 'rgba(117, 168, 240, 0.5)',
    textStyle: { color: '#eff6ff' },
    formatter: (params: unknown) => formatTooltip(params),
  },
  geo: {
    map: MAP_NAME,
    roam: true,
    center: overview.mapCenter,
    zoom: overview.mapZoom,
    scaleLimit: { min: 1, max: 4 },
    itemStyle: {
      areaColor: 'rgba(67, 111, 166, 0.24)',
      borderColor: 'rgba(131, 171, 220, 0.76)',
      borderWidth: 1.1,
      shadowBlur: 12,
      shadowColor: 'rgba(2, 10, 20, 0.24)',
    },
    emphasis: { itemStyle: { areaColor: '#cce4ff' }, label: { color: '#12395f' } },
    label: { show: true, color: '#d6e6ff', fontSize: 11, lineHeight: 14 },
  },
  series: [
    {
      type: 'map',
      map: MAP_NAME,
      geoIndex: 0,
      selectedMode: false,
      data: regionMapData.value,
      zlevel: 1,
      emphasis: { label: { show: true } },
    },
    {
      type: 'scatter',
      coordinateSystem: 'geo',
      data: pointSeriesData.value,
      zlevel: 3,
      symbolSize: (_value: unknown, params: unknown) => Number((((params as { data?: { symbolSize?: number } }).data?.symbolSize) ?? 16)),
      emphasis: {
        scale: true,
        label: {
          show: true,
          formatter: '{b}',
          position: 'top',
          color: '#112e50',
          backgroundColor: 'rgba(255,255,255,0.94)',
          padding: [4, 8],
          borderRadius: 12,
        },
      },
    },
    {
      type: 'effectScatter',
      coordinateSystem: 'geo',
      data: hotspotSeriesData.value,
      zlevel: 4,
      rippleEffect: { scale: 3.8, brushType: 'stroke' },
      itemStyle: { color: '#79b7ff' },
      symbolSize: (_value: unknown, params: unknown) => Number((((params as { data?: { symbolSize?: number } }).data?.symbolSize) ?? 18)),
    },
  ],
}))

watch(() => [activeLayer.value, filters.region, filters.riskLevel, filters.objectType, filters.timeRange] as const, () => {
  if (selectedPointId.value && !filteredPoints.value.some((point) => point.id === selectedPointId.value)) selectedPointId.value = ''
  if (selectedRegionId.value && !visibleRegions.value.some((region) => region.id === selectedRegionId.value)) selectedRegionId.value = ''
})

watch(() => activeLayer.value, (value, oldValue) => {
  if (value === oldValue) return
  selectedPointId.value = ''
  selectedRegionId.value = ''
  filters.objectType = '全部'
  ElMessage.success(`已切换至${value}图层`)
})
const createFallbackLink = (id: string, title: string, kind: DashboardSpatialLink['kind']): DashboardSpatialLink => ({ id, title, kind })

const selectRegion = (regionId: string) => {
  if (!regionId) return
  selectedRegionId.value = regionId
  selectedPointId.value = ''
}

const focusPoint = (pointId: string) => {
  const target = filteredPoints.value.find((point) => point.id === pointId)
  if (!target) return
  selectedPointId.value = pointId
  selectedRegionId.value = props.overview.regions.find((region) => region.name === target.region)?.id ?? ''
}

const focusEventThread = (item: DashboardSpatialEventThread) => {
  if (item.relatedPointId) {
    focusPoint(item.relatedPointId)
    return
  }
  const matchedRegion = props.overview.regions.find((region) => region.name === item.region)
  if (matchedRegion) selectRegion(matchedRegion.id)
}

const openTopicDeck = (item: DashboardSpatialTopicDeck) => {
  goToSearch(item.region, item.keyword)
}

const handleMapClick = (params: unknown) => {
  const payload = params as {
    name?: string
    data?: { kind?: 'point' | 'hotspot'; pointId?: string; regionId?: string; regionName?: string; topic?: string }
  }

  if (payload.data?.kind === 'point' && payload.data.pointId) {
    selectedPointId.value = payload.data.pointId
    selectedRegionId.value = payload.data.regionId ?? ''
    return
  }

  if (payload.data?.kind === 'hotspot' && payload.data.regionName) {
    goToSearch(payload.data.regionName, payload.data.topic ?? payload.data.regionName)
    return
  }

  if (!payload.name) return
  const matchedRegion = props.overview.regions.find((region) => region.name === payload.name)
  if (!matchedRegion) return
  selectedRegionId.value = matchedRegion.id
  selectedPointId.value = ''
}

const handleHeadlineClick = (headlineId: string) => {
  if (headlineId === 'daily-alerts') {
    router.push({ path: '/analysis-warning/records', query: { from: 'dashboard-map', level: '高' } })
    return
  }
  if (headlineId === 'hot-regions') {
    selectRegion(regionRanking.value[0]?.id ?? '')
    return
  }
  if (headlineId === 'focus-events') {
    router.push({ path: '/knowledge-build/event-management', query: { from: 'dashboard-map' } })
    return
  }
  goToSearch(undefined, filteredTopics.value[0]?.keyword)
}

const mockFullscreen = () => ElMessage.success('已切换为 GIS 大屏预览模式（演示）')

const resetView = () => {
  activeLayer.value = '综合'
  filters.timeRange = '今日'
  filters.region = '全部'
  filters.riskLevel = '全部'
  filters.objectType = '全部'
  selectedPointId.value = ''
  selectedRegionId.value = ''
  ElMessage.success('已重置 GIS 态势视图')
}

const showLayerGuide = () => ElMessage.info(currentLayerBrief.value)
const toggleLegend = () => {
  legendVisible.value = !legendVisible.value
  ElMessage.success(`${legendVisible.value ? '已展开' : '已收起'}图例说明`)
}

const openLink = (link: DashboardSpatialLink) => {
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
  router.push({ path: `/report-center/detail/${link.id}`, query: { reportName: link.title } })
}

const goToSearch = (regionName?: string, keyword?: string) => {
  const finalRegion = regionName ?? selectedPoint.value?.region ?? selectedRegion.value?.name ?? ''
  const finalKeyword = keyword ?? selectedPoint.value?.topic ?? selectedRegion.value?.layerMetrics[activeLayer.value].mainTopic ?? filteredTopics.value[0]?.keyword ?? ''
  const query: Record<string, string> = { mode: activeLayer.value === '风险预警' ? '全文检索' : '语义检索', from: 'dashboard-map' }
  if (finalRegion) query.region = finalRegion
  if (finalKeyword) query.keyword = finalKeyword
  router.push({ path: '/smart-search', query })
}

const openSelectedDetail = () => {
  if (selectedPoint.value) {
    const point = selectedPoint.value
    if (point.objectType.includes('预警')) {
      openLink(point.relatedWarnings[0] ?? createFallbackLink(point.id, point.name, 'warning'))
      return
    }
    if (point.objectType.includes('事件')) {
      openLink(point.relatedEvents[0] ?? createFallbackLink(point.id, point.name, 'event'))
      return
    }
    openLink(point.relatedEntities[0] ?? createFallbackLink(point.id, point.name, 'entity'))
    return
  }
  if (selectedRegion.value) {
    goToSearch(selectedRegion.value.name, selectedRegion.value.layerMetrics[activeLayer.value].mainTopic)
    return
  }
  ElMessage.info('请先选择地图区域或点位')
}

const openWarnings = () => {
  if (selectedPoint.value?.relatedWarnings.length) {
    openLink(selectedPoint.value.relatedWarnings[0])
    return
  }
  if (selectedRegion.value) {
    router.push({ path: '/analysis-warning/records', query: { keyword: selectedRegion.value.layerMetrics[activeLayer.value].mainTopic, from: 'dashboard-map' } })
    return
  }
  ElMessage.info('当前没有可联动的预警信息')
}

const openEvents = () => {
  if (selectedPoint.value?.relatedEvents.length) {
    openLink(selectedPoint.value.relatedEvents[0])
    return
  }
  if (selectedRegion.value) {
    router.push({ path: '/knowledge-build/event-management', query: { keyword: selectedRegion.value.layerMetrics[activeLayer.value].mainTopic, from: 'dashboard-map' } })
    return
  }
  ElMessage.info('当前没有可联动的事件信息')
}

const openReports = () => {
  if (selectedPoint.value?.relatedReports.length) {
    openLink(selectedPoint.value.relatedReports[0])
    return
  }
  router.push({ path: '/report-center', query: { from: 'dashboard-map', type: '专题报告' } })
}

const generateTopicView = () => {
  const topic = selectedPoint.value?.topic ?? selectedRegion.value?.layerMetrics[activeLayer.value].mainTopic ?? filteredTopics.value[0]?.keyword ?? activeLayer.value
  ElMessage.success(`已生成“${topic}”专题预览（演示）`)
}
</script>

<template>
  <section class="spatial-overview">
    <div class="spatial-overview__map">
      <el-card shadow="never" class="spatial-card">
        <template #header>
          <div class="spatial-card__header">
            <div>
              <span class="spatial-card__eyebrow">{{ props.eyebrow }}</span>
              <h3>{{ props.title }}</h3>
              <p>{{ props.description }}</p>
            </div>
            <div class="spatial-card__tools">
              <el-tag effect="plain" type="info">{{ overview.baseMapLabel }}</el-tag>
              <el-button :icon="FullScreen" text @click="mockFullscreen">全屏查看</el-button>
              <el-button :icon="RefreshRight" text @click="resetView">重置视图</el-button>
              <el-button :icon="InfoFilled" text @click="showLayerGuide">图层说明</el-button>
              <el-button :icon="CollectionTag" text @click="toggleLegend">{{ legendVisible ? '收起图例' : '展开图例' }}</el-button>
            </div>
          </div>
        </template>

        <div class="spatial-card__headline-grid">
          <button v-for="item in overview.headlines" :key="item.id" type="button" class="spatial-card__headline-item" @click="handleHeadlineClick(item.id)">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.hint }}</small>
          </button>
        </div>

        <el-tabs v-model="activeLayer" class="spatial-card__tabs">
          <el-tab-pane v-for="layer in overview.layers" :key="layer" :name="layer" :label="layer" />
        </el-tabs>

        <div class="spatial-card__filters">
          <el-select v-model="filters.timeRange" size="small" style="width: 110px"><el-option v-for="item in timeRangeOptions" :key="item" :label="item" :value="item" /></el-select>
          <el-select v-model="filters.region" size="small" style="width: 130px"><el-option v-for="item in regionOptions" :key="item" :label="item" :value="item" /></el-select>
          <el-select v-model="filters.riskLevel" size="small" style="width: 120px"><el-option v-for="item in riskOptions" :key="item" :label="item" :value="item" /></el-select>
          <el-select v-model="filters.objectType" size="small" style="width: 160px"><el-option v-for="item in objectTypeOptions" :key="item" :label="item" :value="item" /></el-select>
        </div>

        <div class="spatial-card__stats">
          <div class="spatial-stat"><span>当前图层</span><strong>{{ activeLayer }}</strong></div>
          <div class="spatial-stat"><span>可见点位</span><strong>{{ filteredPoints.length }}</strong></div>
          <div class="spatial-stat"><span>高风险点位</span><strong>{{ highRiskPointCount }}</strong></div>
          <div class="spatial-stat"><span>重点事件</span><strong>{{ filteredEvents.length }}</strong></div>
        </div>
        <div v-if="todayFocusRegions.length" class="spatial-card__focus-strip">
          <button v-for="item in todayFocusRegions" :key="item.id" type="button" class="spatial-card__focus-item" @click="selectRegion(item.id)">
            <span>{{ item.name }}</span>
            <strong>{{ item.emphasis || item.layerMetrics[activeLayer].mainTopic }}</strong>
            <small>{{ item.layerMetrics[activeLayer].mainTopic }}</small>
          </button>
        </div>

        <div v-if="legendVisible" class="spatial-card__legend">
          <div v-for="item in legendItems" :key="item.label" class="legend-item">
            <span class="legend-item__dot" :style="{ background: item.color }" />
            <span>{{ item.label }}</span>
          </div>
        </div>

        <div class="spatial-card__canvas">
          <template v-if="mapReady">
            <BaseEChart :option="mapOption" height="clamp(560px, 58vh, 820px)" @chart-click="handleMapClick" />
          </template>
          <div v-else-if="mapLoading" class="spatial-card__loading">
            <el-icon class="is-loading" :size="28"><Loading /></el-icon>
            <span>正在加载北京 GIS 底图...</span>
          </div>
          <div v-else-if="mapLoadError" class="spatial-card__loading spatial-card__loading--error">
            <span>{{ mapLoadError }}</span>
            <el-button size="small" type="primary" @click="ensureBeijingMap">重新加载</el-button>
          </div>
          <div v-if="mapReady && !filteredPoints.length" class="spatial-card__empty">
            <el-empty description="当前筛选条件下暂无可见点位" :image-size="72" />
          </div>
        </div>

        <div class="spatial-card__support-grid">
          <section class="support-card">
            <div class="support-card__head"><strong>热点区域观察</strong><span>{{ layerActionLabelMap[activeLayer] }}</span></div>
            <button v-for="item in regionRanking" :key="item.id" type="button" class="support-card__item" @click="selectRegion(item.id)">
              <div><strong>{{ item.name }}</strong><span>{{ item.layerMetrics[activeLayer].mainTopic }}</span></div>
              <em>热度 {{ item.layerMetrics[activeLayer].heat }}</em>
            </button>
          </section>

          <section class="support-card">
            <div class="support-card__head"><strong>重点事件流</strong><span>围绕地图持续追踪</span></div>
            <button v-for="item in filteredEvents" :key="item.id" type="button" class="support-card__item" @click="focusEventThread(item)">
              <div><strong>{{ item.title }}</strong><span>{{ item.region }} · {{ item.time }} · {{ item.status }}</span></div>
              <el-tag v-if="item.level" size="small" :type="alertTagTypeMap[item.level] ?? 'info'">{{ item.level }}</el-tag>
            </button>
          </section>

          <section class="support-card">
            <div class="support-card__head"><strong>专题切换建议</strong><span>从地图直接进入研判</span></div>
            <button v-for="item in filteredTopics" :key="item.id" type="button" class="support-card__item" @click="openTopicDeck(item)">
              <div><strong>{{ item.title }}</strong><span>{{ item.region }} · 热度 {{ item.heat }}</span></div>
              <em>进入专题</em>
            </button>
          </section>
        </div>
      </el-card>
    </div>

    <div class="spatial-overview__panel">
      <el-card shadow="never" class="insight-card">
        <template #header>
          <div class="insight-card__header">
            <div>
              <span class="insight-card__eyebrow">Intel Panel</span>
              <h3>联动情报面板</h3>
              <p>承接地图选中结果，显示区域摘要、对象详情和继续钻取入口。</p>
            </div>
            <el-tag effect="plain" type="info">{{ activeLayer }}</el-tag>
          </div>
        </template>

        <template v-if="selectedPoint">
          <div class="insight-card__focus">
            <div class="insight-card__focus-title">
              <strong>{{ selectedPoint.name }}</strong>
              <el-tag size="small" :type="selectedPoint.riskLevel === '高' ? 'danger' : selectedPoint.riskLevel === '中' ? 'warning' : 'info'">{{ selectedPoint.riskLevel ?? '常规' }}</el-tag>
            </div>
            <div class="insight-card__meta"><span>{{ selectedPoint.objectType }}</span><span>{{ selectedPoint.region }}</span><span>{{ selectedPoint.location }}</span><span>{{ selectedPoint.updatedAt }}</span></div>
            <p>{{ selectedPoint.summary }}</p>
          </div>
          <el-descriptions :column="1" border size="small" class="insight-card__desc">
            <el-descriptions-item label="所属专题">{{ selectedPoint.topic }}</el-descriptions-item>
            <el-descriptions-item label="对象类型">{{ selectedPoint.category }}</el-descriptions-item>
            <el-descriptions-item label="当前状态">{{ selectedPoint.status }}</el-descriptions-item>
            <el-descriptions-item label="关键词">{{ selectedPoint.keywords.join(' / ') }}</el-descriptions-item>
          </el-descriptions>
          <div class="link-section"><span class="link-section__title">相关预警</span><el-empty v-if="!selectedPoint.relatedWarnings.length" description="暂无关联预警" :image-size="56" /><el-button v-for="item in selectedPoint.relatedWarnings" v-else :key="`${item.kind}-${item.id}`" text class="link-button" @click="openLink(item)">{{ item.title }}</el-button></div>
          <div class="link-section"><span class="link-section__title">相关事件</span><el-empty v-if="!selectedPoint.relatedEvents.length" description="暂无关联事件" :image-size="56" /><el-button v-for="item in selectedPoint.relatedEvents" v-else :key="`${item.kind}-${item.id}`" text class="link-button" @click="openLink(item)">{{ item.title }}</el-button></div>
          <div class="link-section"><span class="link-section__title">相关报告</span><el-empty v-if="!selectedPoint.relatedReports.length" description="暂无关联报告" :image-size="56" /><el-button v-for="item in selectedPoint.relatedReports" v-else :key="`${item.kind}-${item.id}`" text class="link-button" @click="openLink(item)">{{ item.title }}</el-button></div>
          <div class="link-section"><span class="link-section__title">相关实体</span><el-empty v-if="!selectedPoint.relatedEntities.length" description="暂无关联实体" :image-size="56" /><el-button v-for="item in selectedPoint.relatedEntities" v-else :key="`${item.kind}-${item.id}`" text class="link-button" @click="openLink(item)">{{ item.title }}</el-button></div>
        </template>

        <template v-else-if="selectedRegion">
          <div class="insight-card__focus">
            <div class="insight-card__focus-title"><strong>{{ selectedRegion.name }}</strong><el-tag size="small" type="success">区域统计</el-tag></div>
            <div class="insight-card__meta"><span>{{ selectedRegion.emphasis }}</span><span>主题：{{ selectedRegion.layerMetrics[activeLayer].mainTopic }}</span><span>热度：{{ selectedRegion.layerMetrics[activeLayer].heat }}</span></div>
            <p>{{ selectedRegion.summary }}</p>
          </div>
          <div class="region-grid">
            <div class="region-grid__item"><span>对象总数</span><strong>{{ selectedRegion.layerMetrics[activeLayer].objectCount }}</strong></div>
            <div class="region-grid__item"><span>预警数</span><strong>{{ selectedRegion.layerMetrics[activeLayer].alertCount }}</strong></div>
            <div class="region-grid__item"><span>事件数</span><strong>{{ selectedRegion.layerMetrics[activeLayer].eventCount }}</strong></div>
            <div class="region-grid__item"><span>主热点</span><strong>{{ selectedRegion.layerMetrics[activeLayer].mainTopic }}</strong></div>
          </div>
          <div class="link-section"><span class="link-section__title">区域关联对象</span><el-empty v-if="!currentRegionPoints.length" description="暂无区域对象" :image-size="56" /><el-button v-for="item in currentRegionPoints" v-else :key="item.id" text class="link-button" @click="focusPoint(item.id)">{{ item.name }}</el-button></div>
        </template>

        <template v-else>
          <div class="insight-card__default">
            <el-alert type="info" :closable="false" show-icon>{{ currentLayerBrief }}</el-alert>
            <div class="default-section"><strong>态势简报</strong><p>{{ currentSummary.briefing }}</p></div>
            <div class="default-section"><strong>地图使用说明</strong><ul><li v-for="item in currentSummary.instructions" :key="item">{{ item }}</li></ul></div>
            <div class="default-section"><strong>今日重点区域</strong><div class="tag-row"><el-tag v-for="item in currentSummary.keyRegions" :key="item" type="info" effect="plain">{{ item }}</el-tag></div></div>
            <div class="default-section"><strong>热点分布摘要</strong><p>{{ currentSummary.hotspotSummary }}</p></div>
          </div>
        </template>

        <div class="action-row">
          <el-button type="primary" @click="openSelectedDetail">查看详情</el-button>
          <el-button @click="openWarnings">查看关联预警</el-button>
          <el-button @click="openEvents">查看关联事件</el-button>
          <el-button @click="openReports">查看相关报告</el-button>
          <el-button @click="goToSearch()">进入智能检索</el-button>
          <el-button @click="generateTopicView">生成专题查看</el-button>
        </div>
      </el-card>
    </div>
  </section>
</template>

<style scoped>
.spatial-overview { display: grid; grid-template-columns: minmax(0, 2fr) minmax(360px, 0.96fr); gap: var(--platform-section-gap); align-items: stretch; }
.spatial-card, .insight-card { height: 100%; border: 1px solid var(--platform-border-subtle); }
.spatial-card__header, .insight-card__header { display: grid; grid-template-columns: minmax(0, 1fr) auto; align-items: flex-start; gap: 14px; }
.spatial-card__eyebrow, .insight-card__eyebrow { display: inline-block; color: var(--platform-text-tertiary); font-size: 11px; letter-spacing: 0.16em; text-transform: uppercase; }
.spatial-card__header h3, .insight-card__header h3 { margin: 6px 0 0; color: var(--platform-text-primary); font-size: 22px; }
.spatial-card__header p, .insight-card__header p { margin: 10px 0 0; color: var(--platform-text-secondary); font-size: 13px; line-height: 1.7; }
.spatial-card__tools { display: flex; flex-wrap: wrap; justify-content: flex-end; gap: 6px; }
.spatial-card__headline-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; margin-bottom: 14px; }
.spatial-card__headline-item, .support-card__item, .spatial-card__focus-item { transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease; }
.spatial-card__headline-item:hover, .support-card__item:hover, .spatial-card__focus-item:hover { transform: translateY(-2px); border-color: var(--platform-border-strong); box-shadow: 0 18px 38px rgba(2, 10, 20, 0.28); }
.spatial-card__headline-item { display: flex; flex-direction: column; gap: 8px; border: 1px solid rgba(118, 151, 186, 0.18); border-radius: 16px; background: linear-gradient(180deg, rgba(15, 27, 42, 0.94) 0%, rgba(10, 18, 29, 0.96) 100%); padding: 14px 16px; color: var(--platform-text-primary); text-align: left; cursor: pointer; }
.spatial-card__headline-item span { color: var(--platform-text-secondary); font-size: 12px; }
.spatial-card__headline-item strong { color: var(--platform-text-primary); font-size: 26px; }
.spatial-card__headline-item small { color: var(--platform-text-tertiary); line-height: 1.6; }
.spatial-card__tabs { margin-top: -2px; }
.spatial-card__filters, .spatial-card__stats, .spatial-card__focus-strip { display: grid; gap: 12px; }
.spatial-card__filters { grid-template-columns: repeat(4, minmax(0, 1fr)); margin-bottom: 14px; }
.spatial-card__stats { grid-template-columns: repeat(4, minmax(0, 1fr)); margin-bottom: 14px; }
.spatial-card__focus-strip { grid-template-columns: repeat(4, minmax(0, 1fr)); margin-bottom: 12px; }
.spatial-stat, .spatial-card__focus-item, .support-card { border: 1px solid rgba(112, 145, 184, 0.18); border-radius: 16px; background: linear-gradient(180deg, rgba(18, 31, 49, 0.88) 0%, rgba(10, 20, 31, 0.88) 100%); }
.spatial-stat { padding: 14px 16px; }
.spatial-stat span, .spatial-card__focus-item span, .spatial-card__focus-item small, .support-card__head span, .support-card__item span, .support-card__item em { color: var(--platform-text-tertiary); font-size: 12px; }
.spatial-stat strong { display: block; margin-top: 6px; color: var(--platform-text-primary); font-size: 20px; }
.spatial-card__focus-item { display: flex; flex-direction: column; gap: 6px; padding: 12px 14px; color: var(--platform-text-primary); text-align: left; cursor: pointer; background: rgba(13, 24, 38, 0.86); }
.spatial-card__focus-item strong, .support-card__item strong, .support-card__head strong, .link-section__title, .default-section strong { color: var(--platform-text-primary); }
.spatial-card__focus-item strong { font-size: 14px; }
.spatial-card__legend { display: flex; flex-wrap: wrap; gap: 10px 14px; margin-bottom: 12px; padding: 12px; border: 1px dashed rgba(112, 145, 184, 0.18); border-radius: 16px; background: rgba(10, 19, 31, 0.62); }
.legend-item { display: inline-flex; align-items: center; gap: 8px; color: var(--platform-text-secondary); font-size: 12px; }
.legend-item__dot { width: 10px; height: 10px; border-radius: 50%; }
.spatial-card__canvas { position: relative; min-height: clamp(560px, 58vh, 820px); overflow: hidden; border-radius: 22px; background: radial-gradient(circle at 30% 20%, rgba(94, 158, 255, 0.2), transparent 30%), radial-gradient(circle at 78% 26%, rgba(83, 199, 183, 0.1), transparent 22%), linear-gradient(180deg, rgba(6, 15, 26, 0.98) 0%, rgba(8, 15, 24, 1) 100%); }
.spatial-card__loading, .spatial-card__empty { position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 12px; }
.spatial-card__empty { pointer-events: none; }
.spatial-card__loading { color: var(--platform-text-secondary); }
.spatial-card__loading--error { color: #ffd1cc; }
.spatial-card__support-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 14px; margin-top: 14px; }
.support-card { padding: 14px; border-radius: 18px; }
.support-card__head { display: flex; align-items: flex-start; justify-content: space-between; gap: 10px; margin-bottom: 12px; }
.support-card__item { display: flex; align-items: center; justify-content: space-between; gap: 12px; width: 100%; border: 1px solid rgba(118, 151, 186, 0.14); border-radius: 14px; background: rgba(13, 24, 38, 0.74); padding: 12px; color: var(--platform-text-primary); text-align: left; cursor: pointer; }
.support-card__item + .support-card__item { margin-top: 10px; }
.insight-card__focus { margin-bottom: 14px; padding: 14px; border: 1px solid rgba(112, 145, 184, 0.18); border-radius: 16px; background: linear-gradient(180deg, rgba(18, 31, 49, 0.9) 0%, rgba(10, 20, 31, 0.9) 100%); }
.insight-card__focus-title { display: flex; align-items: center; justify-content: space-between; gap: 10px; }
.insight-card__focus-title strong { color: var(--platform-text-primary); font-size: 18px; }
.insight-card__meta { display: flex; flex-wrap: wrap; gap: 8px 12px; margin-top: 8px; color: var(--platform-text-tertiary); font-size: 12px; }
.insight-card__focus p, .default-section p { margin: 10px 0 0; color: var(--platform-text-secondary); line-height: 1.7; }
.insight-card__desc, .link-section { margin-bottom: 14px; }
.link-button { display: block; padding: 0; color: var(--platform-accent-strong); }
.region-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; margin-bottom: 14px; }
.region-grid__item { padding: 12px; border: 1px solid rgba(112, 145, 184, 0.16); border-radius: 14px; background: rgba(13, 23, 37, 0.78); }
.region-grid__item span { color: var(--platform-text-tertiary); font-size: 12px; }
.region-grid__item strong { display: block; margin-top: 6px; color: var(--platform-text-primary); font-size: 18px; }
.insight-card__default { display: flex; flex-direction: column; gap: 14px; }
.default-section ul { margin: 0; padding-left: 18px; color: var(--platform-text-secondary); line-height: 1.8; }
.tag-row { display: flex; flex-wrap: wrap; gap: 8px; }
.action-row { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 10px; }
@media (max-width: 1800px) {
  .spatial-card__headline-grid, .spatial-card__stats, .spatial-card__focus-strip { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .spatial-card__support-grid { grid-template-columns: 1fr; }
}
@media (max-width: 1600px) { .spatial-overview { grid-template-columns: 1fr; } }
@media (max-width: 1280px) { .spatial-card__filters { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 768px) {
  .spatial-card__header, .insight-card__header { grid-template-columns: 1fr; }
  .spatial-card__tools { justify-content: flex-start; }
  .spatial-card__headline-grid, .spatial-card__filters, .spatial-card__stats, .spatial-card__focus-strip, .region-grid { grid-template-columns: 1fr; }
}
</style>
