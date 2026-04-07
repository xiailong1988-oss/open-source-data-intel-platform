<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageSectionHeader from '../../components/common/PageSectionHeader.vue'
import WorkbenchHero from '../../components/common/WorkbenchHero.vue'
import WorkbenchMetricGrid from '../../components/common/WorkbenchMetricGrid.vue'
import { eventMockList } from '../../mock/knowledgeBuild'
import type { EventItem } from '../../types/knowledgeBuild'

type EventTypeFilter = EventItem['eventType'] | '全部'
type RiskFilter = EventItem['riskLevel'] | '全部'
type StatusFilter = EventItem['status'] | '全部'

const eventList = ref<EventItem[]>(eventMockList.map((item) => ({ ...item })))
const detailVisible = ref(false)
const selectedEvent = ref<EventItem | null>(null)
const route = useRoute()
const router = useRouter()

const filterForm = reactive({
  keyword: '',
  eventType: '全部' as EventTypeFilter,
  riskLevel: '全部' as RiskFilter,
  status: '全部' as StatusFilter,
})

const eventTypeOptions = computed(() => {
  const options = new Set(eventList.value.map((item) => item.eventType))
  return ['全部', ...options]
})

const riskOptions: RiskFilter[] = ['全部', '高', '中', '低']

const statusOptions = computed(() => {
  const options = new Set(eventList.value.map((item) => item.status))
  return ['全部', ...options]
})

const formatCount = (value: number) => new Intl.NumberFormat('zh-CN').format(value)

/**
 * 统一重置事件中心的筛选状态。
 * 路由 drill-down 回到当前页或 query 被清空时，都需要以这份默认状态重新计算页面内容，避免旧筛选残留。
 */
const resetFilterState = () => {
  filterForm.keyword = ''
  filterForm.eventType = '全部'
  filterForm.riskLevel = '全部'
  filterForm.status = '全部'
}

const filteredList = computed(() =>
  eventList.value.filter((item) => {
    const hitKeyword =
      !filterForm.keyword ||
      item.eventName.includes(filterForm.keyword) ||
      item.location.includes(filterForm.keyword) ||
      item.involvedEntities.some((entity) => entity.includes(filterForm.keyword))
    const hitType = filterForm.eventType === '全部' || item.eventType === filterForm.eventType
    const hitRisk = filterForm.riskLevel === '全部' || item.riskLevel === filterForm.riskLevel
    const hitStatus = filterForm.status === '全部' || item.status === filterForm.status
    return hitKeyword && hitType && hitRisk && hitStatus
  }),
)

const riskTagMap: Record<string, 'danger' | 'warning' | 'info'> = {
  高: 'danger',
  中: 'warning',
  低: 'info',
}

const statusTagMap: Record<string, 'warning' | 'success' | 'info'> = {
  研判中: 'warning',
  已处置: 'success',
  持续跟踪: 'info',
}

const focusedEvent = computed(() => selectedEvent.value ?? filteredList.value[0] ?? null)

const summaryCards = computed(() => {
  const total = filteredList.value.length
  const highRisk = filteredList.value.filter((item) => item.riskLevel === '高').length
  const tracking = filteredList.value.filter((item) => item.status === '持续跟踪').length
  const researching = filteredList.value.filter((item) => item.status === '研判中').length

  return [
    {
      id: 'all',
      label: '当前事件数',
      value: formatCount(total),
      hint: '当前筛选范围内的事件总量',
      action: () => resetFilterState(),
    },
    {
      id: 'high',
      label: '高风险事件',
      value: formatCount(highRisk),
      hint: '优先进入持续跟踪与会商流程',
      action: () => {
        filterForm.riskLevel = '高'
      },
    },
    {
      id: 'tracking',
      label: '持续跟踪',
      value: formatCount(tracking),
      hint: '适合作为当前研判焦点',
      action: () => {
        filterForm.status = '持续跟踪'
      },
    },
    {
      id: 'researching',
      label: '研判中事件',
      value: formatCount(researching),
      hint: '待补充研判结论与处置建议',
      action: () => {
        filterForm.status = '研判中'
      },
    },
  ]
})

const riskBreakdown = computed(() =>
  riskOptions
    .filter((item) => item !== '全部')
    .map((level) => ({
      level,
      count: filteredList.value.filter((item) => item.riskLevel === level).length,
    })),
)

const statusBreakdown = computed<Array<{ status: Exclude<StatusFilter, '全部'>; count: number }>>(() =>
  statusOptions.value
    .filter((item) => item !== '全部')
    .map((status) => ({
      status: status as Exclude<StatusFilter, '全部'>,
      count: filteredList.value.filter((item) => item.status === status).length,
    })),
)

const locationInsights = computed(() => {
  const counter = new Map<string, { name: string; count: number; highRiskCount: number }>()

  filteredList.value.forEach((item) => {
    const current = counter.get(item.location) ?? { name: item.location, count: 0, highRiskCount: 0 }
    current.count += 1
    if (item.riskLevel === '高') {
      current.highRiskCount += 1
    }
    counter.set(item.location, current)
  })

  return Array.from(counter.values()).sort((left, right) => {
    if (right.highRiskCount !== left.highRiskCount) {
      return right.highRiskCount - left.highRiskCount
    }
    return right.count - left.count
  })
})

const entityFocusList = computed(() => {
  const current = focusedEvent.value
  if (!current) {
    return []
  }

  return current.involvedEntities.slice(0, 6)
})

const resetFilter = () => {
  resetFilterState()
  ElMessage.success('筛选条件已重置')
}

const openDetail = (item: EventItem) => {
  selectedEvent.value = item
  detailVisible.value = true
}

const toEntity = (entityName: string) => {
  router.push({ path: '/knowledge-build/entity-management', query: { keyword: entityName, autoOpen: 'first' } })
}

const toContent = (contentTitle: string) => {
  router.push({ path: '/data-governance/standardized-list', query: { keyword: contentTitle, autoOpen: 'first' } })
}

const toSearch = (keyword: string) => {
  router.push({ path: '/smart-search', query: { keyword, mode: '语义检索', from: 'event-center' } })
}

const toWarning = (keyword: string) => {
  router.push({ path: '/analysis-warning/records', query: { keyword, autoOpen: 'first', from: 'event-center' } })
}

const applyLocationFocus = (location: string) => {
  filterForm.keyword = location
  selectedEvent.value = filteredList.value.find((item) => item.location === location) ?? null
}

/**
 * 统一消费首页、概览页和其他对象页传入的 query 参数。
 * 事件中心支持按关键词、事件类型、风险等级、状态和事件 id 直接定位焦点事件。
 */
const applyRouteQuery = () => {
  resetFilterState()
  detailVisible.value = false
  selectedEvent.value = null

  const keyword = String(route.query.keyword ?? '')
  const eventType = String(route.query.eventType ?? '全部')
  const riskLevel = String(route.query.riskLevel ?? route.query.level ?? '全部')
  const status = String(route.query.status ?? '全部')
  const eventId = String(route.query.eventId ?? '')
  const autoOpen = route.query.autoOpen === 'first'

  if (keyword) {
    filterForm.keyword = keyword
  }

  if (eventTypeOptions.value.includes(eventType)) {
    filterForm.eventType = eventType as EventTypeFilter
  }

  if (riskOptions.includes(riskLevel as RiskFilter)) {
    filterForm.riskLevel = riskLevel as RiskFilter
  }

  if (statusOptions.value.includes(status)) {
    filterForm.status = status as StatusFilter
  }

  const matched = eventList.value.find((item) => item.id === eventId)
  if (matched) {
    openDetail(matched)
    return
  }

  if (autoOpen && filteredList.value.length) {
    openDetail(filteredList.value[0])
  }
}

watch(() => route.query, applyRouteQuery, { immediate: true })

watch(filteredList, (list) => {
  if (!selectedEvent.value) {
    return
  }

  if (!list.some((item) => item.id === selectedEvent.value?.id)) {
    selectedEvent.value = list[0] ?? null
  }
})
</script>

<template>
  <section class="event-center stage3-page">
    <WorkbenchHero
      eyebrow="Event Center"
      title="事件中心工作台"
      description="围绕事件识别、风险分级、关联对象与处置建议组织主工作区，让事件管理从单纯列表转为可追踪的分析入口。"
    >
      <template #meta>
        <el-tag effect="plain">支持 drill-down</el-tag>
        <el-tag effect="plain" type="warning">风险分级观察</el-tag>
        <el-tag effect="plain" type="success">关联实体可继续追踪</el-tag>
      </template>
    </WorkbenchHero>

    <WorkbenchMetricGrid :items="summaryCards" />

    <div class="event-center__main-grid">
      <div class="event-center__primary">
        <el-card shadow="never" class="page-card">
          <template #header>
            <div class="event-center__card-header">
              <div class="platform-page-heading">
                <h3>事件队列</h3>
                <p>按事件类型、风险等级、状态和关键词收敛当前观察范围，支持从列表进入详情与关联对象。</p>
              </div>
              <el-tag effect="plain" type="info">当前 {{ filteredList.length }} 条</el-tag>
            </div>
          </template>

          <el-form :inline="true" :model="filterForm" class="filter-form">
            <el-form-item>
              <el-input v-model="filterForm.keyword" placeholder="搜索事件名称/地点/关联实体" clearable />
            </el-form-item>
            <el-form-item>
              <el-select v-model="filterForm.eventType" style="width: 140px">
                <el-option v-for="item in eventTypeOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-select v-model="filterForm.riskLevel" style="width: 110px">
                <el-option v-for="item in riskOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-select v-model="filterForm.status" style="width: 130px">
                <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button @click="resetFilter">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="filteredList" stripe>
            <el-table-column label="事件名称" min-width="220" show-overflow-tooltip>
              <template #default="{ row }">
                <el-button link class="cell-link" @click="openDetail(row)">{{ row.eventName }}</el-button>
              </template>
            </el-table-column>
            <el-table-column prop="eventType" label="事件类型" width="120" />
            <el-table-column prop="location" label="发生区域" width="120" />
            <el-table-column prop="startTime" label="开始时间" width="160" />
            <el-table-column label="风险等级" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="riskTagMap[row.riskLevel]">{{ row.riskLevel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="relatedContentCount" label="关联内容数" width="110" />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag size="small" :type="statusTagMap[row.status]">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openDetail(row)">查看详情</el-button>
              </template>
            </el-table-column>
            <template #empty>
              <el-empty description="当前筛选条件下暂无事件" :image-size="72" />
            </template>
          </el-table>
        </el-card>
      </div>

      <div class="event-center__aside">
        <el-card shadow="never" class="detail-card event-center__focus-card">
          <PageSectionHeader
            title="当前事件焦点"
            description="右侧面板始终跟随当前选中的事件，方便在列表、详情和关联对象之间切换。"
            eyebrow="Focus"
            compact
          />

          <template v-if="focusedEvent">
            <div class="event-center__focus-head">
              <div>
                <h4>{{ focusedEvent.eventName }}</h4>
                <p>{{ focusedEvent.summary }}</p>
              </div>
              <el-tag size="small" :type="riskTagMap[focusedEvent.riskLevel]">{{ focusedEvent.riskLevel }}风险</el-tag>
            </div>

            <el-descriptions :column="1" border>
              <el-descriptions-item label="事件类型">{{ focusedEvent.eventType }}</el-descriptions-item>
              <el-descriptions-item label="发生区域">{{ focusedEvent.location }}</el-descriptions-item>
              <el-descriptions-item label="开始时间">{{ focusedEvent.startTime }}</el-descriptions-item>
              <el-descriptions-item label="处置状态">{{ focusedEvent.status }}</el-descriptions-item>
            </el-descriptions>

            <div class="event-center__action-row">
              <el-button type="primary" @click="openDetail(focusedEvent)">查看详情</el-button>
              <el-button @click="toSearch(focusedEvent.eventName)">进入智能检索</el-button>
              <el-button @click="toWarning(focusedEvent.eventName)">查看相关预警</el-button>
            </div>

            <div class="event-center__subsection">
              <span class="event-center__subsection-title">关联实体</span>
              <div class="event-center__tag-group">
                <el-tag
                  v-for="entity in entityFocusList"
                  :key="entity"
                  class="tag-item tag-item--clickable"
                  @click="toEntity(entity)"
                >
                  {{ entity }}
                </el-tag>
              </div>
            </div>

            <div class="event-center__subsection">
              <span class="event-center__subsection-title">最近时间线</span>
              <el-timeline>
                <el-timeline-item
                  v-for="(item, index) in focusedEvent.timeline"
                  :key="`${item.time}-${index}`"
                  :timestamp="item.time"
                >
                  {{ item.content }}
                </el-timeline-item>
              </el-timeline>
            </div>
          </template>

          <el-empty v-else description="暂无可展示的事件焦点" :image-size="72" />
        </el-card>

        <el-card shadow="never" class="detail-card">
          <PageSectionHeader
            title="风险与状态分布"
            description="从当前筛选结果中快速切换关注层级，保持事件中心的工作台节奏。"
            eyebrow="Signals"
            compact
          />

          <div class="event-center__split-panel">
            <div class="event-center__summary-list">
              <button
                v-for="item in riskBreakdown"
                :key="item.level"
                type="button"
                class="event-center__summary-item"
                @click="filterForm.riskLevel = item.level"
              >
                <span>{{ item.level }}风险</span>
                <strong>{{ formatCount(item.count) }}</strong>
              </button>
            </div>
            <div class="event-center__summary-list">
              <button
                v-for="item in statusBreakdown"
                :key="item.status"
                type="button"
                class="event-center__summary-item"
                @click="filterForm.status = item.status"
              >
                <span>{{ item.status }}</span>
                <strong>{{ formatCount(item.count) }}</strong>
              </button>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="detail-card">
          <PageSectionHeader
            title="区域观察"
            description="识别当前筛选下的重点区域，支持回到列表继续钻取。"
            eyebrow="Locations"
            compact
          />
          <div class="event-center__location-list">
            <button
              v-for="item in locationInsights"
              :key="item.name"
              type="button"
              class="event-center__location-item"
              @click="applyLocationFocus(item.name)"
            >
              <div>
                <strong>{{ item.name }}</strong>
                <span>事件 {{ formatCount(item.count) }} 条</span>
              </div>
              <el-tag size="small" :type="item.highRiskCount ? 'danger' : 'info'">
                高风险 {{ formatCount(item.highRiskCount) }}
              </el-tag>
            </button>
          </div>
        </el-card>
      </div>
    </div>

    <el-drawer v-model="detailVisible" title="事件详情" size="760px" destroy-on-close append-to-body>
      <template v-if="selectedEvent">
        <el-card shadow="never" class="detail-card">
          <template #header>事件摘要</template>
          <p class="paragraph">{{ selectedEvent.summary }}</p>
        </el-card>

        <el-card shadow="never" class="detail-card">
          <template #header>时间线</template>
          <el-timeline>
            <el-timeline-item
              v-for="(item, index) in selectedEvent.timeline"
              :key="`${item.time}-${index}`"
              :timestamp="item.time"
            >
              {{ item.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>

        <el-card shadow="never" class="detail-card">
          <template #header>涉及实体</template>
          <el-tag
            v-for="entity in selectedEvent.involvedEntities"
            :key="entity"
            class="tag-item tag-item--clickable"
            @click="toEntity(entity)"
          >
            {{ entity }}
          </el-tag>
        </el-card>

        <el-card shadow="never" class="detail-card">
          <template #header>关联内容</template>
          <el-timeline>
            <el-timeline-item v-for="content in selectedEvent.relatedContents" :key="content">
              <el-button link class="cell-link" @click="toContent(content)">{{ content }}</el-button>
            </el-timeline-item>
          </el-timeline>
        </el-card>

        <el-card shadow="never" class="detail-card">
          <template #header>风险等级说明</template>
          <div class="risk-box">
            <el-tag :type="riskTagMap[selectedEvent.riskLevel]">风险等级：{{ selectedEvent.riskLevel }}</el-tag>
            <p class="paragraph">{{ selectedEvent.riskExplanation }}</p>
          </div>
        </el-card>
      </template>
    </el-drawer>
  </section>
</template>

<style scoped>
.event-center {
  min-width: 0;
}

.event-center__metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--platform-card-gap);
}

.event-center__main-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.7fr) minmax(340px, 0.95fr);
  gap: var(--platform-section-gap);
  align-items: start;
}

.event-center__primary,
.event-center__aside {
  min-width: 0;
}

.event-center__aside {
  display: flex;
  flex-direction: column;
  gap: var(--platform-section-gap);
}

.event-center__card-header {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: 16px;
}

.event-center__focus-card {
  overflow: visible;
}

.event-center__focus-head {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: 14px;
  margin-top: 18px;
  margin-bottom: 18px;
}

.event-center__focus-head h4 {
  margin: 0;
  color: var(--platform-text-primary);
  font-size: 18px;
}

.event-center__focus-head p {
  margin: 10px 0 0;
  color: var(--platform-text-secondary);
  line-height: 1.7;
}

.event-center__action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.event-center__subsection {
  margin-top: 18px;
}

.event-center__subsection-title {
  display: inline-flex;
  margin-bottom: 12px;
  color: var(--platform-text-secondary);
  font-size: 12px;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.event-center__tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.event-center__split-panel {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.event-center__summary-list,
.event-center__location-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 18px;
}

.event-center__summary-item,
.event-center__location-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  border: 1px solid var(--platform-border-subtle);
  border-radius: 16px;
  background: rgba(11, 21, 34, 0.82);
  padding: 14px 16px;
  color: var(--platform-text-primary);
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}

.event-center__summary-item:hover,
.event-center__location-item:hover {
  transform: translateY(-2px);
  border-color: var(--platform-border-strong);
  background: rgba(14, 26, 42, 0.94);
}

.event-center__summary-item span,
.event-center__location-item span {
  color: var(--platform-text-secondary);
  font-size: 12px;
}

.event-center__summary-item strong,
.event-center__location-item strong {
  color: var(--platform-text-primary);
  font-size: 14px;
}

.metric-card__label {
  color: var(--platform-text-secondary);
  font-size: 13px;
}

.metric-card__value {
  display: block;
  margin-top: 10px;
  color: var(--platform-text-primary);
  font-size: 30px;
  line-height: 1;
}

.metric-card__change {
  display: inline-block;
  margin-top: 10px;
  color: var(--platform-text-tertiary);
  font-size: 12px;
}

.detail-card {
  border: 1px solid var(--platform-border-subtle);
}

.event-center :deep(.el-drawer__body) .detail-card + .detail-card {
  margin-top: 14px;
}

.paragraph {
  margin: 0;
  color: var(--platform-text-secondary);
  line-height: 1.8;
}

.tag-item--clickable {
  cursor: pointer;
}

.cell-link {
  padding: 0;
  color: var(--platform-accent-strong);
}

.risk-box {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

@media (max-width: 1800px) {
  .event-center__metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .event-center__main-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .event-center__metric-grid,
  .event-center__split-panel {
    grid-template-columns: 1fr;
  }

  .event-center__card-header,
  .event-center__focus-head {
    flex-direction: column;
  }
}
</style>
