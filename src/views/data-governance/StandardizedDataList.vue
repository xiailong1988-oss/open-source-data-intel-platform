<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { standardizedContentMockList } from '../../mock/dataGovernance'
import type {
  StandardizedContentItem,
  StandardizedContentStatus,
  StandardizedContentType,
} from '../../types/dataGovernance'

type FilterOption<T extends string> = T | '全部'

const contentList = ref<StandardizedContentItem[]>(standardizedContentMockList.map((item) => ({ ...item })))
const detailVisible = ref(false)
const selectedItem = ref<StandardizedContentItem>()
const route = useRoute()
const router = useRouter()

const filterForm = reactive({
  keyword: '',
  contentType: '全部' as FilterOption<StandardizedContentType>,
  source: '全部',
  status: '全部' as FilterOption<StandardizedContentStatus>,
  date: '',
})

const contentTypeOptions = computed(() => {
  const options = new Set(contentList.value.map((item) => item.contentType))
  return ['全部', ...options]
})

const sourceOptions = computed(() => {
  const options = new Set(contentList.value.map((item) => item.source))
  return ['全部', ...options]
})

const statusOptions = computed(() => {
  const options = new Set(contentList.value.map((item) => item.status))
  return ['全部', ...options]
})

const filteredList = computed(() =>
  contentList.value.filter((item) => {
    const hitKeyword =
      !filterForm.keyword ||
      item.title.includes(filterForm.keyword) ||
      item.summary.includes(filterForm.keyword)
    const hitType = filterForm.contentType === '全部' || item.contentType === filterForm.contentType
    const hitSource = filterForm.source === '全部' || item.source === filterForm.source
    const hitStatus = filterForm.status === '全部' || item.status === filterForm.status
    const hitDate = !filterForm.date || item.publishedAt.includes(filterForm.date)
    return hitKeyword && hitType && hitSource && hitStatus && hitDate
  }),
)

const statusTagMap: Record<string, 'success' | 'warning' | 'info'> = {
  已标准化: 'success',
  待复核: 'warning',
  已归档: 'info',
}

// 列表页的重置动作需要同时清理首页钻取和 query 进入时留下的所有筛选状态。
const resetFilter = () => {
  filterForm.keyword = ''
  filterForm.contentType = '全部'
  filterForm.source = '全部'
  filterForm.status = '全部'
  filterForm.date = ''
  ElMessage.success('筛选条件已重置')
}

const openDetail = (item: StandardizedContentItem) => {
  selectedItem.value = item
  detailVisible.value = true
}

const toEntity = (entityName: string) => {
  router.push({ path: '/knowledge-build/entity-management', query: { keyword: entityName, autoOpen: 'first' } })
}

const toSearchByTag = (tag: string) => {
  router.push({ path: '/smart-search', query: { keyword: tag, tag, from: 'standardized' } })
}

// 统一解析来自首页、图表、地图和详情页的 query 参数，
// 让标准化列表既能作为独立页面使用，也能承接其他页面的钻取进入。
const applyRouteQuery = () => {
  const keyword = String(route.query.keyword ?? '')
  const contentType = String(route.query.contentType ?? route.query.type ?? '全部')
  const source = String(route.query.source ?? '全部')
  const status = String(route.query.status ?? '全部')
  const date = String(route.query.date ?? '')
  const id = String(route.query.id ?? '')
  const timePreset = String(route.query.timePreset ?? '')
  const autoOpen = route.query.autoOpen === 'first'

  if (keyword) {
    filterForm.keyword = keyword
  }

  if (contentTypeOptions.value.includes(contentType)) {
    filterForm.contentType = contentType as FilterOption<StandardizedContentType>
  }

  if (sourceOptions.value.includes(source)) {
    filterForm.source = source
  }

  if (statusOptions.value.includes(status)) {
    filterForm.status = status as FilterOption<StandardizedContentStatus>
  }

  if (date) {
    filterForm.date = date
  }

  if (timePreset === 'today') {
    const today = new Date().toISOString().slice(0, 10)
    filterForm.date = today
  }

  const target = contentList.value.find((item) => item.id === id)
  if (target) {
    openDetail(target)
    return
  }

  if (autoOpen && filteredList.value.length) {
    openDetail(filteredList.value[0])
  }
}

watch(() => route.query, applyRouteQuery, { immediate: true })
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="page-card__header">
        <div class="platform-page-heading">
          <span class="platform-page-heading__eyebrow">Normalized Content</span>
          <h3>标准化数据列表</h3>
          <p>查看治理后的结构化内容，支持从摘要、标签、实体和原始来源继续追踪。</p>
        </div>
      </div>
    </template>

    <el-form :inline="true" :model="filterForm" class="filter-form">
      <el-form-item>
        <el-input v-model="filterForm.keyword" placeholder="搜索标题/摘要关键字" clearable />
      </el-form-item>
      <el-form-item>
        <el-select v-model="filterForm.contentType" style="width: 150px">
          <el-option v-for="item in contentTypeOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-select v-model="filterForm.source" style="width: 190px">
          <el-option v-for="item in sourceOptions" :key="item" :label="item" :value="item" />
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
      <el-table-column label="标题" min-width="210" show-overflow-tooltip>
        <template #default="{ row }">
          <el-button link class="cell-link" @click="openDetail(row)">{{ row.title }}</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="contentType" label="内容类型" width="110" />
      <el-table-column prop="source" label="来源" min-width="170" />
      <el-table-column prop="publishedAt" label="发布时间" width="160" />
      <el-table-column prop="region" label="地域" width="110" />
      <el-table-column prop="tagCount" label="标签数" width="90" />
      <el-table-column prop="entityCount" label="实体数" width="90" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="statusTagMap[row.status]">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-drawer v-model="detailVisible" title="标准化内容详情" size="760px">
    <template v-if="selectedItem">
      <el-descriptions :column="2" border class="detail-block">
        <el-descriptions-item label="标题" :span="2">{{ selectedItem.title }}</el-descriptions-item>
        <el-descriptions-item label="内容类型">{{ selectedItem.contentType }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag size="small" :type="statusTagMap[selectedItem.status]">{{ selectedItem.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="来源">{{ selectedItem.source }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ selectedItem.publishedAt }}</el-descriptions-item>
        <el-descriptions-item label="地域">{{ selectedItem.region }}</el-descriptions-item>
        <el-descriptions-item label="原始来源链接" :span="2">{{ selectedItem.sourceLink }}</el-descriptions-item>
      </el-descriptions>

      <el-card shadow="never" class="detail-block">
        <template #header>摘要</template>
        <p class="paragraph">{{ selectedItem.summary }}</p>
      </el-card>

      <el-card shadow="never" class="detail-block">
        <template #header>正文</template>
        <p class="paragraph">{{ selectedItem.content }}</p>
      </el-card>

      <el-card shadow="never" class="detail-block">
        <template #header>标签与实体信息</template>
        <div class="tag-row">
          <span class="tag-row__label">标签：</span>
          <el-tag
            v-for="tag in selectedItem.tags"
            :key="tag"
            class="tag-row__item tag-row__item--clickable"
            @click="toSearchByTag(tag)"
          >
            {{ tag }}
          </el-tag>
        </div>
        <div class="tag-row">
          <span class="tag-row__label">实体：</span>
          <el-tag
            v-for="entity in selectedItem.entities"
            :key="entity"
            type="info"
            class="tag-row__item tag-row__item--clickable"
            @click="toEntity(entity)"
          >
            {{ entity }}
          </el-tag>
        </div>
      </el-card>

      <el-card shadow="never" class="detail-block">
        <template #header>扩展属性 JSON</template>
        <pre class="json-block">{{ JSON.stringify(selectedItem.extAttributes, null, 2) }}</pre>
      </el-card>

      <el-card shadow="never" class="detail-block">
        <template #header>原始数据与标准数据对比</template>
        <el-row :gutter="12">
          <el-col :span="12">
            <div class="compare-box">
              <h4>原始数据</h4>
              <p>{{ selectedItem.rawContent }}</p>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="compare-box is-standardized">
              <h4>标准化数据</h4>
              <p>{{ selectedItem.standardizedContent }}</p>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </template>
  </el-drawer>
</template>

<style scoped>
.page-card {
  border: 1px solid var(--platform-border-subtle);
}

.page-card__header {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: 16px;
}

.filter-form {
  margin-bottom: 8px;
}

.cell-link {
  padding: 0;
  color: var(--platform-accent-strong);
}

.detail-block {
  margin-bottom: 14px;
  border: 1px solid var(--platform-border-subtle);
}

.paragraph {
  margin: 0;
  color: var(--platform-text-secondary);
  line-height: 1.8;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.tag-row__label {
  color: var(--platform-text-secondary);
  font-size: 13px;
}

.tag-row__item {
  margin-right: 2px;
}

.tag-row__item--clickable {
  cursor: pointer;
}

.json-block {
  margin: 0;
  border-radius: 10px;
  background: rgba(8, 15, 25, 0.72);
  padding: 10px;
  color: #d7e6f7;
  font-size: 12px;
}

.compare-box {
  min-height: 130px;
  border: 1px solid rgba(112, 145, 184, 0.16);
  border-radius: 12px;
  background: rgba(14, 25, 39, 0.72);
  padding: 10px;
}

.compare-box h4 {
  margin: 0 0 8px;
  color: var(--platform-text-secondary);
  font-size: 13px;
}

.compare-box p {
  margin: 0;
  color: var(--platform-text-secondary);
  line-height: 1.7;
  font-size: 13px;
}

.compare-box.is-standardized {
  border-color: rgba(100, 169, 255, 0.26);
  background: rgba(18, 34, 54, 0.88);
}
</style>
