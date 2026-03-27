<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { dataSourceMockList } from '../../mock/dataAccess'
import type { AccessMethod, DataSourceItem, SourceStatus, SourceType } from '../../types/dataAccess'

type FilterOption<T extends string> = T | '全部'
type FormMode = 'create' | 'edit'

interface DataSourceFormModel {
  id: string
  name: string
  sourceType: SourceType
  accessMethod: AccessMethod
  status: SourceStatus
  owner: string
  endpoint: string
  description: string
}

const sourceList = ref<DataSourceItem[]>(dataSourceMockList.map((item) => ({ ...item })))
const detailDrawerVisible = ref(false)
const selectedSource = ref<DataSourceItem>()
const formVisible = ref(false)
const formMode = ref<FormMode>('create')
const route = useRoute()

const filterForm = reactive({
  keyword: '',
  sourceType: '全部' as FilterOption<SourceType>,
  status: '全部' as FilterOption<SourceStatus>,
})

const formModel = reactive<DataSourceFormModel>({
  id: '',
  name: '',
  sourceType: '政务系统',
  accessMethod: 'API接入',
  status: '运行中',
  owner: '',
  endpoint: '',
  description: '',
})

const sourceTypeOptions = computed(() => {
  const options = new Set(sourceList.value.map((item) => item.sourceType))
  return ['全部', ...options]
})

const statusOptions = computed(() => {
  const options = new Set(sourceList.value.map((item) => item.status))
  return ['全部', ...options]
})

const filteredSources = computed(() =>
  sourceList.value.filter((item) => {
    const hitKeyword =
      !filterForm.keyword ||
      item.name.includes(filterForm.keyword) ||
      item.owner.includes(filterForm.keyword) ||
      item.endpoint.includes(filterForm.keyword)
    const hitType = filterForm.sourceType === '全部' || item.sourceType === filterForm.sourceType
    const hitStatus = filterForm.status === '全部' || item.status === filterForm.status
    return hitKeyword && hitType && hitStatus
  }),
)

const statusTagTypeMap: Record<string, 'success' | 'warning' | 'danger'> = {
  运行中: 'success',
  已停用: 'warning',
  异常: 'danger',
}

const formatTime = () => new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')

const resetFilter = () => {
  filterForm.keyword = ''
  filterForm.sourceType = '全部'
  filterForm.status = '全部'
}

const openCreateDialog = () => {
  formMode.value = 'create'
  formModel.id = ''
  formModel.name = ''
  formModel.sourceType = '政务系统'
  formModel.accessMethod = 'API接入'
  formModel.status = '运行中'
  formModel.owner = ''
  formModel.endpoint = ''
  formModel.description = ''
  formVisible.value = true
}

const openEditDialog = (item: DataSourceItem) => {
  formMode.value = 'edit'
  formModel.id = item.id
  formModel.name = item.name
  formModel.sourceType = item.sourceType
  formModel.accessMethod = item.accessMethod
  formModel.status = item.status
  formModel.owner = item.owner
  formModel.endpoint = item.endpoint
  formModel.description = item.description
  formVisible.value = true
}

const saveSource = () => {
  if (!formModel.name || !formModel.owner || !formModel.endpoint) {
    ElMessage.warning('请补全数据源名称、负责人和接入地址')
    return
  }

  if (formMode.value === 'create') {
    sourceList.value.unshift({
      id: `DS-${Math.floor(Math.random() * 9000) + 1000}`,
      name: formModel.name,
      sourceType: formModel.sourceType,
      accessMethod: formModel.accessMethod,
      status: formModel.status,
      owner: formModel.owner,
      endpoint: formModel.endpoint,
      latestCollectTime: '-',
      createdAt: formatTime(),
      description: formModel.description || '暂无描述',
    })
    ElMessage.success('已新增数据源（mock）')
  } else {
    const index = sourceList.value.findIndex((item) => item.id === formModel.id)
    if (index > -1) {
      sourceList.value[index] = {
        ...sourceList.value[index],
        name: formModel.name,
        sourceType: formModel.sourceType,
        accessMethod: formModel.accessMethod,
        status: formModel.status,
        owner: formModel.owner,
        endpoint: formModel.endpoint,
        description: formModel.description || '暂无描述',
      }
      ElMessage.success('已更新数据源（mock）')
    }
  }

  formVisible.value = false
}

const toggleSourceStatus = (item: DataSourceItem) => {
  item.status = item.status === '运行中' ? '已停用' : '运行中'
  ElMessage.success(`已${item.status === '运行中' ? '启用' : '停用'}：${item.name}`)
}

const openDetail = (item: DataSourceItem) => {
  selectedSource.value = item
  detailDrawerVisible.value = true
}

const applyRouteQuery = () => {
  const keyword = String(route.query.keyword ?? '')
  const sourceType = String(route.query.sourceType ?? '全部')
  const status = String(route.query.status ?? '全部')
  const sourceId = String(route.query.sourceId ?? '')
  const autoOpen = route.query.autoOpen === 'first'

  if (keyword) {
    filterForm.keyword = keyword
  }

  if (sourceTypeOptions.value.includes(sourceType)) {
    filterForm.sourceType = sourceType as FilterOption<SourceType>
  }

  if (statusOptions.value.includes(status)) {
    filterForm.status = status as FilterOption<SourceStatus>
  }

  const target = sourceList.value.find((item) => item.id === sourceId)
  if (target) {
    openDetail(target)
    return
  }

  if (autoOpen && filteredSources.value.length) {
    openDetail(filteredSources.value[0])
  }
}

watch(() => route.query, applyRouteQuery, { immediate: true })
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="page-card__header">
        <div class="platform-page-heading">
          <span class="platform-page-heading__eyebrow">Source Registry</span>
          <h3>数据源管理</h3>
          <p>管理多源数据的接入方式、负责人和运行状态，保障上游数据持续可用。</p>
        </div>
        <el-button type="primary" @click="openCreateDialog">新增数据源</el-button>
      </div>
    </template>

    <el-form :inline="true" :model="filterForm" class="filter-form">
      <el-form-item>
        <el-input v-model="filterForm.keyword" placeholder="搜索数据源名称/负责人/接入地址" clearable />
      </el-form-item>
      <el-form-item>
        <el-select v-model="filterForm.sourceType" style="width: 160px">
          <el-option v-for="item in sourceTypeOptions" :key="item" :label="item" :value="item" />
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

    <el-table :data="filteredSources" stripe>
      <el-table-column label="数据源名称" min-width="190" show-overflow-tooltip>
        <template #default="{ row }">
          <el-button link class="cell-link" @click="openDetail(row)">{{ row.name }}</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="sourceType" label="数据源类型" width="140" />
      <el-table-column prop="accessMethod" label="接入方式" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="statusTagTypeMap[row.status]">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="latestCollectTime" label="最近采集时间" width="160" />
      <el-table-column prop="createdAt" label="创建时间" width="160" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button link @click="toggleSourceStatus(row)">
            {{ row.status === '运行中' ? '停用' : '启用' }}
          </el-button>
          <el-button link @click="openDetail(row)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="formVisible" :title="formMode === 'create' ? '新增数据源' : '编辑数据源'" width="620px">
    <el-form :model="formModel" label-width="88px">
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="数据源名称">
            <el-input v-model="formModel.name" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="负责人">
            <el-input v-model="formModel.owner" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="数据源类型">
            <el-select v-model="formModel.sourceType">
              <el-option label="政务系统" value="政务系统" />
              <el-option label="业务系统" value="业务系统" />
              <el-option label="互联网公开数据" value="互联网公开数据" />
              <el-option label="物联网设备" value="物联网设备" />
              <el-option label="第三方平台" value="第三方平台" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="接入方式">
            <el-select v-model="formModel.accessMethod">
              <el-option label="API接入" value="API接入" />
              <el-option label="数据库同步" value="数据库同步" />
              <el-option label="文件导入" value="文件导入" />
              <el-option label="消息订阅" value="消息订阅" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="接入地址">
        <el-input v-model="formModel.endpoint" />
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="formModel.status">
          <el-radio label="运行中">运行中</el-radio>
          <el-radio label="已停用">已停用</el-radio>
          <el-radio label="异常">异常</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="formModel.description" type="textarea" :rows="3" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="formVisible = false">取消</el-button>
      <el-button type="primary" @click="saveSource">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailDrawerVisible" title="数据源详情" size="560px">
    <el-descriptions v-if="selectedSource" :column="1" border>
      <el-descriptions-item label="数据源名称">{{ selectedSource.name }}</el-descriptions-item>
      <el-descriptions-item label="数据源类型">{{ selectedSource.sourceType }}</el-descriptions-item>
      <el-descriptions-item label="接入方式">{{ selectedSource.accessMethod }}</el-descriptions-item>
      <el-descriptions-item label="当前状态">{{ selectedSource.status }}</el-descriptions-item>
      <el-descriptions-item label="负责人">{{ selectedSource.owner }}</el-descriptions-item>
      <el-descriptions-item label="接入地址">{{ selectedSource.endpoint }}</el-descriptions-item>
      <el-descriptions-item label="最近采集时间">{{ selectedSource.latestCollectTime }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ selectedSource.createdAt }}</el-descriptions-item>
      <el-descriptions-item label="描述信息">{{ selectedSource.description }}</el-descriptions-item>
    </el-descriptions>
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

@media (max-width: 900px) {
  .page-card__header {
    flex-direction: column;
  }
}
</style>
