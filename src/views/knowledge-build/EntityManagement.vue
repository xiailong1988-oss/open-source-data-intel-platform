<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { entityMockList } from '../../mock/knowledgeBuild'
import type { EntityItem } from '../../types/knowledgeBuild'

type EntityTypeFilter = EntityItem['entityType'] | '全部'

const entityList = ref<EntityItem[]>(entityMockList.map((item) => ({ ...item })))
const detailVisible = ref(false)
const selectedEntity = ref<EntityItem>()
const route = useRoute()
const router = useRouter()

const filterForm = reactive({
  keyword: '',
  entityType: '全部' as EntityTypeFilter,
})

const entityTypeOptions = computed(() => {
  const options = new Set(entityList.value.map((item) => item.entityType))
  return ['全部', ...options]
})

const filteredList = computed(() =>
  entityList.value.filter((item) => {
    const hitKeyword =
      !filterForm.keyword ||
      item.entityName.includes(filterForm.keyword) ||
      item.standardName.includes(filterForm.keyword)
    const hitType = filterForm.entityType === '全部' || item.entityType === filterForm.entityType
    return hitKeyword && hitType
  }),
)

const resetFilter = () => {
  filterForm.keyword = ''
  filterForm.entityType = '全部'
  ElMessage.success('筛选条件已重置')
}

const openDetail = (item: EntityItem) => {
  selectedEntity.value = item
  detailVisible.value = true
}

const toEvent = (eventName: string) => {
  router.push({ path: '/knowledge-build/event-management', query: { keyword: eventName, autoOpen: 'first' } })
}

const toContent = (contentTitle: string) => {
  router.push({ path: '/data-governance/standardized-list', query: { keyword: contentTitle, autoOpen: 'first' } })
}

const applyRouteQuery = () => {
  const keyword = String(route.query.keyword ?? '')
  const entityType = String(route.query.entityType ?? '全部')
  const entityId = String(route.query.entityId ?? '')
  const autoOpen = route.query.autoOpen === 'first'

  if (keyword) {
    filterForm.keyword = keyword
  }

  if (entityTypeOptions.value.includes(entityType)) {
    filterForm.entityType = entityType as EntityTypeFilter
  }

  const matched = entityList.value.find((item) => item.id === entityId)
  if (matched) {
    openDetail(matched)
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
          <span class="platform-page-heading__eyebrow">Entity Registry</span>
          <h3>实体管理</h3>
          <p>沉淀标准实体、别名、关联事件与内容，作为后续问答、预警和报告的对象底座。</p>
        </div>
      </div>
    </template>

    <el-form :inline="true" :model="filterForm" class="filter-form">
      <el-form-item>
        <el-input v-model="filterForm.keyword" placeholder="搜索实体名称/标准名称" clearable />
      </el-form-item>
      <el-form-item>
        <el-select v-model="filterForm.entityType" style="width: 140px">
          <el-option v-for="item in entityTypeOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="resetFilter">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="filteredList" stripe>
      <el-table-column label="实体名称" min-width="190">
        <template #default="{ row }">
          <el-button link class="cell-link" @click="openDetail(row)">{{ row.entityName }}</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="entityType" label="实体类型" width="100" />
      <el-table-column prop="standardName" label="标准名称" min-width="200" />
      <el-table-column prop="aliasCount" label="别名数量" width="90" />
      <el-table-column prop="relatedContentCount" label="关联内容数" width="110" />
      <el-table-column label="置信度" width="100">
        <template #default="{ row }">
          {{ (row.confidence * 100).toFixed(1) }}%
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新时间" width="160" />
      <el-table-column label="操作" width="110" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-drawer v-model="detailVisible" title="实体详情" size="720px">
    <template v-if="selectedEntity">
      <el-card shadow="never" class="detail-card">
        <template #header>基础信息</template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="实体名称">{{ selectedEntity.entityName }}</el-descriptions-item>
          <el-descriptions-item label="标准名称">{{ selectedEntity.standardName }}</el-descriptions-item>
          <el-descriptions-item label="实体类型">{{ selectedEntity.entityType }}</el-descriptions-item>
          <el-descriptions-item label="置信度">
            {{ (selectedEntity.confidence * 100).toFixed(1) }}%
          </el-descriptions-item>
          <el-descriptions-item
            v-for="(value, key) in selectedEntity.basicInfo"
            :key="key"
            :label="key"
            :span="1"
          >
            {{ value }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" class="detail-card">
        <template #header>别名</template>
        <el-tag v-for="item in selectedEntity.aliases" :key="item" class="tag-item">{{ item }}</el-tag>
      </el-card>

      <el-card shadow="never" class="detail-card">
        <template #header>关联内容</template>
        <el-timeline>
          <el-timeline-item v-for="item in selectedEntity.relatedContents" :key="item">
            <el-button link class="cell-link" @click="toContent(item)">{{ item }}</el-button>
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <el-card shadow="never" class="detail-card">
        <template #header>关联事件</template>
        <el-tag
          v-for="item in selectedEntity.relatedEvents"
          :key="item"
          type="warning"
          class="tag-item tag-item--clickable"
          @click="toEvent(item)"
        >
          {{ item }}
        </el-tag>
      </el-card>

      <el-card shadow="never" class="detail-card">
        <template #header>关联关系</template>
        <el-tag v-for="item in selectedEntity.relatedRelations" :key="item" type="info" class="tag-item">
          {{ item }}
        </el-tag>
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

.detail-card {
  margin-bottom: 14px;
  border: 1px solid var(--platform-border-subtle);
}

.tag-item {
  margin: 0 8px 8px 0;
}

.tag-item--clickable {
  cursor: pointer;
}

.cell-link {
  padding: 0;
  color: var(--platform-accent-strong);
}
</style>
