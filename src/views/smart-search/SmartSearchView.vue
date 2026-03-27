<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageSectionHeader from '../../components/common/PageSectionHeader.vue'
import ViewStatePanel from '../../components/common/ViewStatePanel.vue'
import SearchResultCard from '../../components/search/SearchResultCard.vue'
import { smartSearchFilterOptions, smartSearchResultMock } from '../../mock/smartSearch'
import type { SearchMode, SearchResultItem } from '../../types/smartSearch'

interface ActiveFilterChip {
  key: 'dataTypes' | 'sources' | 'regions' | 'tags' | 'riskLevels' | 'timeRange'
  value: string
  label: string
}

const route = useRoute()
const router = useRouter()

const searchMode = ref<SearchMode>('全文检索')
const searchModeOptions: SearchMode[] = ['全文检索', '语义检索']
const inputKeyword = ref('')
const queryKeyword = ref('')
const searching = ref(false)
const detailVisible = ref(false)
const selectedResult = ref<SearchResultItem | null>(null)
const searchRequestId = ref(0)
const searchErrorMessage = ref('')

const filterForm = reactive({
  dataTypes: [] as string[],
  sources: [] as string[],
  regions: [] as string[],
  tags: [] as string[],
  riskLevels: [] as string[],
  timeRange: [] as string[],
})

const semanticAliasMap: Record<string, string[]> = {
  产业链: ['供应链', '产能', '交付', '库存'],
  客流: ['人流', '商圈', '换乘', '拥堵'],
  舆情: ['传播', '情绪', '敏感', '扩散'],
  风险: ['预警', '告警', '异常', '波动'],
}

const entityFallbackMap: Record<string, string[]> = {
  'SR-8001': ['华新智能制造有限公司', '高新区产业园'],
  'SR-8002': ['中心商圈', '城市感知网关'],
  'SR-8003': ['情报研判中心', '舆情平台节点A'],
  'SR-8004': ['政务交换库', '综合研判组'],
  'SR-8005': ['设备A-102', '设备B-021'],
  'SR-8006': ['北部片区服务中心', '民生服务平台'],
}

const eventFallbackMap: Record<string, string[]> = {
  'SR-8001': ['重点产业链供给波动'],
  'SR-8002': ['中心城区夜间客流密度异常'],
  'SR-8003': ['敏感舆情跨平台扩散'],
  'SR-8005': ['园区设备告警异常增长'],
}

const normalize = (text: string) => text.toLowerCase().trim()

const parseDate = (value: string) => {
  const timestamp = new Date(value.replace(/-/g, '/')).getTime()
  return Number.isNaN(timestamp) ? 0 : timestamp
}

const semanticTokens = computed(() => {
  const keyword = normalize(queryKeyword.value)
  if (!keyword) {
    return []
  }

  const aliases = semanticAliasMap[keyword] ?? []
  return Array.from(new Set([keyword, ...aliases]))
})

const keywordMatched = (item: SearchResultItem) => {
  if (!queryKeyword.value) {
    return true
  }

  const keyword = normalize(queryKeyword.value)
  const fullText = normalize(`${item.title} ${item.summary} ${item.content}`)
  const semanticText = normalize(`${item.title} ${item.semanticHints.join(' ')} ${item.tags.join(' ')}`)

  if (searchMode.value === '全文检索') {
    return fullText.includes(keyword)
  }

  return semanticTokens.value.some((token) => semanticText.includes(token)) || fullText.includes(keyword)
}

const filteredResults = computed(() =>
  smartSearchResultMock.filter((item) => {
    if (!keywordMatched(item)) {
      return false
    }

    if (filterForm.dataTypes.length && !filterForm.dataTypes.includes(item.dataType)) {
      return false
    }

    if (filterForm.sources.length && !filterForm.sources.includes(item.source)) {
      return false
    }

    if (filterForm.regions.length && !filterForm.regions.includes(item.region)) {
      return false
    }

    if (filterForm.riskLevels.length && !filterForm.riskLevels.includes(item.riskLevel)) {
      return false
    }

    if (filterForm.tags.length && !filterForm.tags.some((tag) => item.tags.includes(tag))) {
      return false
    }

    if (filterForm.timeRange.length === 2) {
      const [start, end] = filterForm.timeRange
      const time = parseDate(item.time)
      const startTime = parseDate(start)
      const endTime = parseDate(`${end} 23:59:59`)
      return time >= startTime && time <= endTime
    }

    return true
  }),
)

const sortedResults = computed(() => {
  const riskWeight: Record<string, number> = { 高: 3, 中: 2, 低: 1 }
  return [...filteredResults.value].sort((a, b) => {
    const diff = riskWeight[b.riskLevel] - riskWeight[a.riskLevel]
    if (diff !== 0) {
      return diff
    }
    return parseDate(b.time) - parseDate(a.time)
  })
})

const activeFilterChips = computed<ActiveFilterChip[]>(() => {
  const chips: ActiveFilterChip[] = []
  filterForm.dataTypes.forEach((item) => chips.push({ key: 'dataTypes', value: item, label: `类型：${item}` }))
  filterForm.sources.forEach((item) => chips.push({ key: 'sources', value: item, label: `来源：${item}` }))
  filterForm.regions.forEach((item) => chips.push({ key: 'regions', value: item, label: `地域：${item}` }))
  filterForm.tags.forEach((item) => chips.push({ key: 'tags', value: item, label: `标签：${item}` }))
  filterForm.riskLevels.forEach((item) => chips.push({ key: 'riskLevels', value: item, label: `风险：${item}` }))

  if (filterForm.timeRange.length === 2) {
    chips.push({
      key: 'timeRange',
      value: 'timeRange',
      label: `时间：${filterForm.timeRange[0]} 至 ${filterForm.timeRange[1]}`,
    })
  }

  return chips
})

const searchSummary = computed(() => {
  if (queryKeyword.value) {
    return `关键词“${queryKeyword.value}” · ${searchMode.value} · 共 ${sortedResults.value.length} 条`
  }
  return `未输入关键词时展示推荐结果 · 共 ${sortedResults.value.length} 条`
})

// 检索结果区统一按加载、空结果、异常和正常四种状态切换，
// 为后续接真实搜索接口时保留完整的状态展示结构。
const searchPanelState = computed<'ready' | 'empty' | 'error'>(() => {
  if (searchErrorMessage.value) {
    return 'error'
  }

  if (!sortedResults.value.length) {
    return 'empty'
  }

  return 'ready'
})

const detailSuggestionKeywords = computed(() => {
  if (!selectedResult.value) {
    return [] as string[]
  }

  return Array.from(new Set([...selectedResult.value.tags, ...selectedResult.value.semanticHints])).slice(0, 6)
})

const escapeRegExp = (value: string) => value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')

const highlight = (value: string) => {
  if (!queryKeyword.value) {
    return value
  }

  const reg = new RegExp(`(${escapeRegExp(queryKeyword.value)})`, 'gi')
  return value.replace(reg, '<mark class="search-hit">$1</mark>')
}

const resetFilter = (silent = false) => {
  filterForm.dataTypes = []
  filterForm.sources = []
  filterForm.regions = []
  filterForm.tags = []
  filterForm.riskLevels = []
  filterForm.timeRange = []

  if (!silent) {
    ElMessage.success('已重置筛选条件')
  }
}

const executeSearch = async (silent = false) => {
  const currentRequestId = ++searchRequestId.value
  queryKeyword.value = inputKeyword.value.trim()
  searching.value = true
  searchErrorMessage.value = ''

  try {
    await new Promise((resolve) => setTimeout(resolve, 550))
  } catch (error) {
    if (currentRequestId !== searchRequestId.value) {
      return
    }

    searchErrorMessage.value = error instanceof Error ? error.message : '检索服务暂不可用'
    searching.value = false
    if (!silent) {
      ElMessage.error('检索失败，请稍后重试')
    }
    return
  }

  if (currentRequestId !== searchRequestId.value) {
    return
  }

  searching.value = false

  if (!silent) {
    ElMessage.success(`检索完成，共命中 ${sortedResults.value.length} 条结果`)
  }
}

const clearAll = async () => {
  inputKeyword.value = ''
  queryKeyword.value = ''
  resetFilter(true)
  await executeSearch(true)
  ElMessage.success('已清空检索条件')
}

const removeFilter = (chip: ActiveFilterChip) => {
  if (chip.key === 'timeRange') {
    filterForm.timeRange = []
    return
  }

  filterForm[chip.key] = filterForm[chip.key].filter((item) => item !== chip.value)
}

const openDetail = (id: string) => {
  const result = smartSearchResultMock.find((item) => item.id === id)
  if (!result) {
    ElMessage.warning('未找到结果详情')
    return
  }

  selectedResult.value = {
    ...result,
    relatedEntities: result.relatedEntities?.length ? result.relatedEntities : entityFallbackMap[result.id] ?? [],
    relatedEvents: result.relatedEvents?.length ? result.relatedEvents : eventFallbackMap[result.id] ?? [],
  }
  detailVisible.value = true
}

const applySourceFilter = (source: string) => {
  filterForm.sources = [source]
}

const applyTagFilter = (tag: string) => {
  filterForm.tags = [tag]
}

const searchByKeyword = async (keyword: string, mode?: SearchMode) => {
  if (mode) {
    searchMode.value = mode
  }
  inputKeyword.value = keyword
  await executeSearch(true)
}

const toEntity = (entityName: string) => {
  router.push({ path: '/knowledge-build/entity-management', query: { keyword: entityName, autoOpen: 'first' } })
}

const toEvent = (eventName: string) => {
  router.push({ path: '/knowledge-build/event-management', query: { keyword: eventName, autoOpen: 'first' } })
}

const toContent = (title: string) => {
  router.push({ path: '/data-governance/standardized-list', query: { keyword: title, autoOpen: 'first' } })
}

// 智能检索页包含结果抽屉和异步检索状态，离开页面时必须主动回收，
// 否则抽屉包装层或未完成的检索状态可能残留在当前路由根节点上，影响全局菜单点击。
const cleanupSearchViewState = () => {
  searchRequestId.value += 1
  searching.value = false
  detailVisible.value = false
  selectedResult.value = null
}

const applyRouteQuery = async () => {
  const hasRouteSearch =
    route.query.keyword ||
    route.query.mode ||
    route.query.dataType ||
    route.query.source ||
    route.query.region ||
    route.query.tag ||
    route.query.riskLevel ||
    route.query.level

  if (!hasRouteSearch) {
    return
  }

  resetFilter(true)

  const keyword = String(route.query.keyword ?? '')
  const mode = String(route.query.mode ?? searchMode.value)
  const dataType = String(route.query.dataType ?? '')
  const source = String(route.query.source ?? '')
  const region = String(route.query.region ?? '')
  const tag = String(route.query.tag ?? '')
  const riskLevel = String(route.query.riskLevel ?? route.query.level ?? '')
  const autoOpen = route.query.autoOpen === 'first'

  if (searchModeOptions.includes(mode as SearchMode)) {
    searchMode.value = mode as SearchMode
  }

  inputKeyword.value = keyword

  if (dataType) {
    filterForm.dataTypes = [dataType]
  }
  if (source) {
    filterForm.sources = [source]
  }
  if (region) {
    filterForm.regions = [region]
  }
  if (tag) {
    filterForm.tags = [tag]
  }
  if (riskLevel) {
    filterForm.riskLevels = [riskLevel]
  }

  await executeSearch(true)

  if (autoOpen && sortedResults.value.length) {
    openDetail(sortedResults.value[0].id)
  }
}

watch(() => route.query, applyRouteQuery, { immediate: true })

onBeforeRouteLeave(() => {
  cleanupSearchViewState()
})
</script>

<template>
  <section class="smart-search stage3-page">
    <el-card shadow="never" class="search-header-card">
      <PageSectionHeader
        title="智能检索"
        description="支持全文与语义联动检索，基于多维条件快速定位高价值信息。"
        eyebrow="Search Hub"
      >
        <template #actions>
          <div class="search-header__actions">
            <el-tag type="info" effect="plain">{{ searchMode }}</el-tag>
            <el-button link @click="clearAll">清空条件</el-button>
          </div>
        </template>
      </PageSectionHeader>
      <div class="search-toolbar">
        <el-input
          v-model="inputKeyword"
          placeholder="输入关键词，例如：产业链、客流异动、舆情风险"
          clearable
          @keyup.enter="executeSearch"
        >
          <template #prepend>
            <el-segmented v-model="searchMode" :options="searchModeOptions" size="default" />
          </template>
          <template #append>
            <el-button :loading="searching" type="primary" @click="executeSearch">检索</el-button>
          </template>
        </el-input>
      </div>
      <div v-if="activeFilterChips.length" class="active-filter-row">
        <span>已生效筛选：</span>
        <el-tag
          v-for="chip in activeFilterChips"
          :key="`${chip.key}-${chip.value}`"
          closable
          effect="plain"
          @close="removeFilter(chip)"
        >
          {{ chip.label }}
        </el-tag>
      </div>
    </el-card>

    <div class="smart-search__layout">
      <aside class="smart-search__sidebar">
        <el-card shadow="never" class="filter-card">
          <template #header>
            <div class="filter-card__header">
              <span>筛选条件</span>
              <el-button text @click="resetFilter">重置</el-button>
            </div>
          </template>

          <div class="filter-group">
            <h4>数据类型</h4>
            <el-checkbox-group v-model="filterForm.dataTypes">
              <el-checkbox v-for="item in smartSearchFilterOptions.dataTypes" :key="item" :label="item">
                {{ item }}
              </el-checkbox>
            </el-checkbox-group>
          </div>

          <div class="filter-group">
            <h4>来源</h4>
            <el-select v-model="filterForm.sources" multiple collapse-tags collapse-tags-tooltip>
              <el-option v-for="item in smartSearchFilterOptions.sources" :key="item" :label="item" :value="item" />
            </el-select>
          </div>

          <div class="filter-group">
            <h4>时间范围</h4>
            <el-date-picker
              v-model="filterForm.timeRange"
              type="daterange"
              value-format="YYYY-MM-DD"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            />
          </div>

          <div class="filter-group">
            <h4>地域</h4>
            <el-select v-model="filterForm.regions" multiple collapse-tags collapse-tags-tooltip>
              <el-option v-for="item in smartSearchFilterOptions.regions" :key="item" :label="item" :value="item" />
            </el-select>
          </div>

          <div class="filter-group">
            <h4>标签</h4>
            <el-select v-model="filterForm.tags" multiple collapse-tags collapse-tags-tooltip>
              <el-option v-for="item in smartSearchFilterOptions.tags" :key="item" :label="item" :value="item" />
            </el-select>
          </div>

          <div class="filter-group">
            <h4>风险等级</h4>
            <el-checkbox-group v-model="filterForm.riskLevels">
              <el-checkbox v-for="item in smartSearchFilterOptions.riskLevels" :key="item" :label="item">
                {{ item }}
              </el-checkbox>
            </el-checkbox-group>
          </div>
        </el-card>
      </aside>

      <div class="smart-search__content">
        <el-card shadow="never" class="result-panel-card">
          <template #header>
            <div class="result-panel__header">
              <div>
                <strong>检索结果</strong>
                <p class="result-panel__sub">{{ searchSummary }}</p>
              </div>
            </div>
          </template>

          <el-skeleton :loading="searching" animated :rows="6">
            <template #default>
              <ViewStatePanel
                v-if="searchPanelState === 'error'"
                state="error"
                title="检索结果加载失败"
                :description="searchErrorMessage || '当前检索结果暂不可用，请稍后重试。'"
                @retry="executeSearch"
              />
              <ViewStatePanel
                v-else-if="searchPanelState === 'empty'"
                state="empty"
                title="没有匹配结果"
                description="请调整关键词、时间范围或专题筛选条件后再试。"
              >
                <template #actions>
                  <el-button type="primary" plain @click="clearAll">清空后重试</el-button>
                </template>
              </ViewStatePanel>
              <div v-else class="result-list">
                <SearchResultCard
                  v-for="item in sortedResults"
                  :key="item.id"
                  :item="item"
                  :mode="searchMode"
                  :highlighted-title="highlight(item.title)"
                  :highlighted-summary="highlight(item.summary)"
                  @detail="openDetail"
                  @pick-tag="applyTagFilter"
                  @pick-source="applySourceFilter"
                  @pick-entity="toEntity"
                />
              </div>
            </template>
          </el-skeleton>
        </el-card>
      </div>
    </div>

    <!-- 详情抽屉保持在页面主容器内，并在关闭时销毁节点，避免残留包装层覆盖全局布局。 -->
    <el-drawer
      v-model="detailVisible"
      title="检索结果详情"
      size="760px"
      destroy-on-close
      append-to-body
      @closed="selectedResult = null"
    >
      <template v-if="selectedResult">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="标题" :span="2">{{ selectedResult.title }}</el-descriptions-item>
          <el-descriptions-item label="来源">
            <el-button link class="cell-link" @click="applySourceFilter(selectedResult.source)">
              {{ selectedResult.source }}
            </el-button>
          </el-descriptions-item>
          <el-descriptions-item label="时间">{{ selectedResult.time }}</el-descriptions-item>
          <el-descriptions-item label="数据类型">{{ selectedResult.dataType }}</el-descriptions-item>
          <el-descriptions-item label="地域">{{ selectedResult.region }}</el-descriptions-item>
          <el-descriptions-item label="风险等级">{{ selectedResult.riskLevel }}</el-descriptions-item>
          <el-descriptions-item label="标签" :span="2">
            <el-tag
              v-for="tag in selectedResult.tags"
              :key="tag"
              class="detail-tag detail-tag--clickable"
              @click="applyTagFilter(tag)"
            >
              {{ tag }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-card shadow="never" class="detail-card">
          <template #header>摘要</template>
          <p>{{ selectedResult.summary }}</p>
        </el-card>

        <el-card shadow="never" class="detail-card">
          <template #header>正文片段</template>
          <p>{{ selectedResult.content }}</p>
        </el-card>

        <el-card shadow="never" class="detail-card">
          <template #header>相关实体与事件</template>
          <div class="detail-link-group">
            <span>相关实体：</span>
            <el-empty
              v-if="!(selectedResult.relatedEntities?.length ?? 0)"
              description="暂无关联实体"
              :image-size="54"
            />
            <template v-else>
              <el-tag
                v-for="entity in selectedResult.relatedEntities"
                :key="entity"
                type="info"
                class="detail-tag detail-tag--clickable"
                @click="toEntity(entity)"
              >
                {{ entity }}
              </el-tag>
            </template>
          </div>
          <div class="detail-link-group">
            <span>相关事件：</span>
            <el-empty
              v-if="!(selectedResult.relatedEvents?.length ?? 0)"
              description="暂无关联事件"
              :image-size="54"
            />
            <template v-else>
              <el-tag
                v-for="event in selectedResult.relatedEvents"
                :key="event"
                type="warning"
                class="detail-tag detail-tag--clickable"
                @click="toEvent(event)"
              >
                {{ event }}
              </el-tag>
            </template>
          </div>
        </el-card>

        <el-card shadow="never" class="detail-card">
          <template #header>继续检索</template>
          <el-button
            v-for="word in detailSuggestionKeywords"
            :key="word"
            text
            class="cell-link"
            @click="searchByKeyword(word, '语义检索')"
          >
            {{ word }}
          </el-button>
          <el-button text class="cell-link" @click="toContent(selectedResult.title)">查看标准化内容详情</el-button>
        </el-card>
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-drawer>
  </section>
</template>

<style scoped>
.smart-search {
  min-width: 0;
}

.search-header-card,
.filter-card,
.result-panel-card {
  border: 1px solid var(--platform-border-subtle);
}

.search-header-card {
  background:
    radial-gradient(circle at top right, rgba(100, 169, 255, 0.14) 0%, rgba(100, 169, 255, 0) 26%),
    var(--platform-card-bg);
}

.search-toolbar {
  margin-top: 18px;
}

.search-toolbar :deep(.el-input-group__prepend) {
  max-width: 220px;
}

.smart-search__layout {
  display: grid;
  grid-template-columns: minmax(300px, 360px) minmax(0, 1fr);
  gap: var(--platform-section-gap);
  align-items: start;
}

.smart-search__sidebar,
.smart-search__content {
  min-width: 0;
}

.smart-search__sidebar {
  position: sticky;
  top: calc(var(--platform-header-height) + 18px);
}

.active-filter-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
  color: var(--platform-text-secondary);
  font-size: 12px;
}

.filter-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-group {
  margin-bottom: 18px;
}

.filter-group h4 {
  margin: 0 0 8px;
  color: var(--platform-text-primary);
  font-size: 13px;
  letter-spacing: 0.03em;
}

.filter-group :deep(.el-checkbox) {
  display: flex;
  margin-bottom: 6px;
}

.filter-group :deep(.el-select),
.filter-group :deep(.el-date-editor) {
  width: 100%;
}

.result-panel__header strong {
  color: var(--platform-text-primary);
  font-size: 17px;
}

.result-panel__sub {
  margin: 6px 0 0;
  color: var(--platform-text-secondary);
  font-size: 12px;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.detail-card {
  margin-top: 12px;
  border: 1px solid var(--platform-border-subtle);
}

.detail-card p {
  margin: 0;
  color: var(--platform-text-secondary);
  line-height: 1.8;
}

.detail-link-group {
  margin-bottom: 10px;
}

.detail-link-group > span {
  display: inline-block;
  margin-bottom: 8px;
  color: var(--platform-text-secondary);
  font-size: 13px;
}

.detail-tag {
  margin: 0 8px 8px 0;
}

.detail-tag--clickable {
  cursor: pointer;
}

.cell-link {
  padding: 0;
  color: var(--platform-accent-strong);
}

@media (max-width: 1440px) {
  .smart-search__layout {
    grid-template-columns: 1fr;
  }

  .smart-search__sidebar {
    position: static;
  }
}

@media (max-width: 900px) {
  .search-toolbar :deep(.el-input-group__prepend) {
    max-width: 100%;
  }
}
</style>
