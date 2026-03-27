<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import WorkbenchHero from '../../components/common/WorkbenchHero.vue'
import WorkbenchMetricGrid from '../../components/common/WorkbenchMetricGrid.vue'
import PageSectionHeader from '../../components/common/PageSectionHeader.vue'
import ViewStatePanel from '../../components/common/ViewStatePanel.vue'
import { getSourceCatalog } from '../../services'
import type { SourceCatalogItem, SourceCatalogStatus } from '../../types/sourceCenter'

const router = useRouter()
const loading = ref(true)
const loadError = ref('')
const keyword = ref('')
const category = ref('全部')
const status = ref<'全部' | SourceCatalogStatus>('全部')
const selectedSourceId = ref('')
const sourceList = ref<SourceCatalogItem[]>([])

const loadSourceCatalog = async () => {
  loading.value = true
  loadError.value = ''

  try {
    sourceList.value = await getSourceCatalog()
    if (!selectedSourceId.value && sourceList.value.length) {
      selectedSourceId.value = sourceList.value[0].id
    }
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '信源列表加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void loadSourceCatalog()
})

const categoryOptions = computed(() => ['全部', ...Array.from(new Set(sourceList.value.map((item) => item.category)))])

const filteredList = computed(() =>
  sourceList.value.filter((item) => {
    const hitKeyword = !keyword.value || item.name.includes(keyword.value) || item.description.includes(keyword.value)
    const hitCategory = category.value === '全部' || item.category === category.value
    const hitStatus = status.value === '全部' || item.status === status.value
    return hitKeyword && hitCategory && hitStatus
  }),
)

const selectedSource = computed(
  () => filteredList.value.find((item) => item.id === selectedSourceId.value) ?? filteredList.value[0] ?? null,
)

const metrics = computed(() => [
  { id: 'all', label: '信源总数', value: sourceList.value.length, hint: '覆盖政务、产业、公开信息、感知和科研协同五类信源', action: () => (status.value = '全部') },
  { id: 'stable', label: '稳定信源', value: sourceList.value.filter((item) => item.status === '稳定').length, hint: '当前可稳定支撑首页与专题工作台的信源数量', action: () => (status.value = '稳定') },
  { id: 'watch', label: '关注信源', value: sourceList.value.filter((item) => item.status === '关注').length, hint: '更新节奏较低，需要与任务和健康页联动观察', action: () => (status.value = '关注') },
  { id: 'abnormal', label: '异常信源', value: sourceList.value.filter((item) => item.status === '异常').length, hint: '建议优先进入数据健康页面定位失败与延迟原因', action: () => (status.value = '异常') },
])

const openDataSource = () => {
  if (!selectedSource.value?.relatedDataSourceId) {
    return
  }
  router.push({ path: '/data-access/source-management', query: { sourceId: selectedSource.value.relatedDataSourceId, autoOpen: 'first' } })
}

const openSearch = () => {
  if (!selectedSource.value) {
    return
  }
  router.push({ path: '/smart-search', query: { keyword: selectedSource.value.name, mode: '全文检索' } })
}
</script>

<template>
  <section class="source-intelligence stage3-page">
    <WorkbenchHero
      eyebrow="Source Intelligence"
      title="信源列表工作台"
      description="展示系统数据来自哪里、当前覆盖哪些业务主题，以及最近一次刷新状态。"
    >
      <template #meta>
        <el-button @click="router.push('/data-access/data-health')">查看数据健康</el-button>
      </template>
    </WorkbenchHero>

    <WorkbenchMetricGrid v-if="!loading && !loadError" :items="metrics" />

    <ViewStatePanel v-if="loading" state="loading" title="正在加载信源列表" description="正在同步信源分类、状态与最近刷新时间。" />
    <ViewStatePanel v-else-if="loadError" state="error" :description="loadError" @retry="loadSourceCatalog" />
    <template v-else>
      <div class="source-intelligence__layout">
        <el-card shadow="never" class="source-panel">
          <template #header>
            <PageSectionHeader title="信源列表" description="支持分类筛选、状态观察与最近刷新时间查看。" compact />
          </template>
          <div class="source-filter-row">
            <el-input v-model="keyword" placeholder="搜索信源名称 / 描述" clearable />
            <el-select v-model="category" style="width: 160px">
              <el-option v-for="item in categoryOptions" :key="item" :label="item" :value="item" />
            </el-select>
            <el-select v-model="status" style="width: 130px">
              <el-option label="全部" value="全部" />
              <el-option label="稳定" value="稳定" />
              <el-option label="关注" value="关注" />
              <el-option label="异常" value="异常" />
            </el-select>
          </div>

          <el-table :data="filteredList" stripe @row-click="selectedSourceId = $event.id">
            <el-table-column label="信源名称" min-width="220" show-overflow-tooltip>
              <template #default="{ row }">
                <el-button link class="cell-link" @click.stop="selectedSourceId = row.id">{{ row.name }}</el-button>
              </template>
            </el-table-column>
            <el-table-column prop="category" label="分类" width="130" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === '稳定' ? 'success' : row.status === '关注' ? 'warning' : 'danger'" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="refreshAt" label="最近刷新时间" width="170" />
            <el-table-column prop="coverage" label="覆盖范围" min-width="240" show-overflow-tooltip />
            <template #empty>
              <el-empty description="暂无信源信息" :image-size="72" />
            </template>
          </el-table>
        </el-card>

        <el-card shadow="never" class="source-panel">
          <template #header>
            <PageSectionHeader title="信源焦点" description="承接当前选中信源的覆盖范围、负责人和继续联动入口。" compact />
          </template>
          <ViewStatePanel v-if="!selectedSource" state="empty" compact title="暂无信源焦点" description="请选择左侧信源后查看详情。" />
          <template v-else>
            <div class="source-focus">
              <strong>{{ selectedSource.name }}</strong>
              <div class="source-focus__meta">
                <span>{{ selectedSource.category }}</span>
                <span>{{ selectedSource.owner }}</span>
                <span>{{ selectedSource.refreshAt }}</span>
              </div>
              <p>{{ selectedSource.description }}</p>
            </div>
            <div class="source-focus__block">
              <span class="source-focus__label">覆盖范围</span>
              <p>{{ selectedSource.coverage }}</p>
            </div>
            <div class="source-focus__actions">
              <el-button type="primary" @click="openDataSource">查看对应数据源</el-button>
              <el-button @click="openSearch">进入检索</el-button>
              <el-button @click="router.push('/data-access/data-health')">查看健康状态</el-button>
            </div>
          </template>
        </el-card>
      </div>
    </template>
  </section>
</template>

<style scoped>
.source-intelligence {
  display: flex;
  flex-direction: column;
  gap: var(--platform-section-gap);
}

.source-intelligence__layout {
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(360px, 1fr);
  gap: var(--platform-section-gap);
}

.source-panel {
  border: 1px solid var(--platform-border-subtle);
}

.source-filter-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 160px 130px;
  gap: 12px;
  margin-bottom: 12px;
}

.source-focus strong {
  color: var(--platform-text-primary);
  font-size: 18px;
}

.source-focus__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  margin-top: 10px;
  color: var(--platform-text-tertiary);
  font-size: 12px;
}

.source-focus p,
.source-focus__block p {
  color: var(--platform-text-secondary);
  line-height: 1.8;
}

.source-focus__block {
  margin-top: 14px;
}

.source-focus__label {
  display: inline-block;
  color: var(--platform-text-primary);
  font-size: 13px;
  font-weight: 600;
}

.source-focus__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.cell-link {
  padding: 0;
  color: var(--platform-accent-strong);
}

@media (max-width: 1400px) {
  .source-intelligence__layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .source-filter-row {
    grid-template-columns: 1fr;
  }
}
</style>
