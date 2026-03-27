<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import WorkbenchHero from '../../components/common/WorkbenchHero.vue'
import WorkbenchMetricGrid from '../../components/common/WorkbenchMetricGrid.vue'
import ViewStatePanel from '../../components/common/ViewStatePanel.vue'
import { getReportList } from '../../services'
import type { ReportListItem, ReportType } from '../../types/reportCenter'

type TypeFilter = ReportType | '全部'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const loadError = ref('')
const reportList = ref<ReportListItem[]>([])
const dialogVisible = ref(false)
const creating = ref(false)
const downloadingIds = ref<string[]>([])

const filterForm = reactive({
  keyword: '',
  type: '全部' as TypeFilter,
})

const createForm = reactive({
  name: '',
  type: '专题报告' as ReportType,
  creator: '综合研判组',
})

const typeOptions: TypeFilter[] = ['全部', '日报', '周报', '专题报告', '快报']

const loadReports = async () => {
  loading.value = true
  loadError.value = ''

  try {
    reportList.value = await getReportList()
    applyRouteQuery()
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '报告列表加载失败'
  } finally {
    loading.value = false
  }
}

const filteredList = computed(() =>
  reportList.value.filter((item) => {
    const hitKeyword =
      !filterForm.keyword || item.name.includes(filterForm.keyword) || item.creator.includes(filterForm.keyword)
    const hitType = filterForm.type === '全部' || item.type === filterForm.type
    return hitKeyword && hitType
  }),
)

const summaryMetrics = computed(() => [
  {
    id: 'all',
    label: '正式报告总量',
    value: reportList.value.length,
    hint: '统一沉淀周报、日报、专题报告和快报。',
    action: () => {
      filterForm.keyword = ''
      filterForm.type = '全部'
    },
  },
  {
    id: 'ready',
    label: '已生成',
    value: reportList.value.filter((item) => item.status === '已生成').length,
    hint: '当前可直接打开查看的正式报告数量。',
    action: () => {
      filterForm.keyword = ''
      filterForm.type = '全部'
    },
  },
  {
    id: 'topic',
    label: '专题报告',
    value: reportList.value.filter((item) => item.type === '专题报告').length,
    hint: '与专题工作台和简报中心形成正式输出联动。',
    action: () => {
      filterForm.type = '专题报告'
    },
  },
  {
    id: 'briefings',
    label: '简报入口',
    value: '已联通',
    hint: '可通过上方标签直接切换到简报中心。',
    action: () => {
      router.push('/report-center/briefings')
    },
  },
])

const statusTagMap: Record<string, 'success' | 'warning' | 'info'> = {
  已生成: 'success',
  生成中: 'warning',
  待审核: 'info',
}

const formatNow = () => new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')

const resetFilter = () => {
  filterForm.keyword = ''
  filterForm.type = '全部'
  ElMessage.success('筛选条件已重置')
}

const openCreateDialog = () => {
  createForm.name = ''
  createForm.type = '专题报告'
  createForm.creator = '综合研判组'
  dialogVisible.value = true
}

const createReport = async () => {
  if (!createForm.name.trim()) {
    ElMessage.warning('请输入报告名称')
    return
  }

  creating.value = true
  await new Promise((resolve) => setTimeout(resolve, 700))

  const newReport: ReportListItem = {
    id: `RP-${Math.floor(Math.random() * 9000) + 1000}`,
    name: createForm.name.trim(),
    type: createForm.type,
    generatedAt: formatNow(),
    creator: createForm.creator,
    status: '生成中',
  }

  reportList.value.unshift(newReport)
  creating.value = false
  dialogVisible.value = false
  ElMessage.success('报告任务已创建，正在生成（mock）')

  setTimeout(() => {
    newReport.status = '已生成'
  }, 1400)
}

const viewDetail = (id: string) => {
  router.push(`/report-center/detail/${id}`)
}

const isDownloading = (id: string) => downloadingIds.value.includes(id)

const mockDownload = async (item: ReportListItem) => {
  downloadingIds.value.push(item.id)
  await new Promise((resolve) => setTimeout(resolve, 500))
  downloadingIds.value = downloadingIds.value.filter((id) => id !== item.id)
  ElMessage.success(`已触发下载：${item.name}（mock）`)
}

/**
 * 正式报告页继续兼容从首页、专题页和检索页带入的 query 参数，
 * 避免 drill-down 到报告中心后丢失筛选语义。
 */
const applyRouteQuery = () => {
  const keyword = String(route.query.keyword ?? '')
  const type = String(route.query.type ?? '全部')
  const reportId = String(route.query.reportId ?? '')
  const autoOpen = route.query.autoOpen === 'first'

  if (keyword) {
    filterForm.keyword = keyword
  }

  if (typeOptions.includes(type as TypeFilter)) {
    filterForm.type = type as TypeFilter
  }

  if (reportId) {
    viewDetail(reportId)
    return
  }

  if (autoOpen && filteredList.value.length) {
    viewDetail(filteredList.value[0].id)
  }
}

watch(() => route.query, applyRouteQuery)

onMounted(() => {
  void loadReports()
})
</script>

<template>
  <section class="report-center platform-page-shell">
    <WorkbenchHero
      eyebrow="Formal Reports"
      title="正式报告中心"
      description="汇总 AI 辅助分析报告、专题报告和周期报告，承接从工作台到正式输出的完整链路。"
    >
      <template #meta>
        <el-button @click="router.push('/report-center/briefings')">进入简报中心</el-button>
        <el-button type="primary" @click="openCreateDialog">新建报告</el-button>
      </template>
    </WorkbenchHero>

    <WorkbenchMetricGrid v-if="!loading && !loadError" :items="summaryMetrics" />

    <ViewStatePanel
      v-if="loading"
      state="loading"
      title="正在加载正式报告"
      description="正在同步报告列表与报告状态。"
    />
    <ViewStatePanel v-else-if="loadError" state="error" :description="loadError" @retry="loadReports" />
    <el-card v-else shadow="never" class="page-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item>
          <el-input v-model="filterForm.keyword" placeholder="搜索报告名称/创建人" clearable />
        </el-form-item>
        <el-form-item>
          <el-select v-model="filterForm.type" style="width: 140px">
            <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="filteredList" stripe>
        <el-table-column label="报告名称" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button link class="cell-link" @click="viewDetail(row.id)">{{ row.name }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="报告类型" width="100" />
        <el-table-column prop="generatedAt" label="生成时间" width="170" />
        <el-table-column prop="creator" label="创建人" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagMap[row.status]">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row.id)">查看详情</el-button>
            <el-button link :loading="isDownloading(row.id)" @click="mockDownload(row)">下载</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无报告数据" :image-size="72" />
        </template>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新建报告" width="580px">
      <el-form :model="createForm" label-width="88px">
        <el-form-item label="报告名称">
          <el-input v-model="createForm.name" placeholder="请输入报告名称" />
        </el-form-item>
        <el-form-item label="报告类型">
          <el-select v-model="createForm.type">
            <el-option label="日报" value="日报" />
            <el-option label="周报" value="周报" />
            <el-option label="专题报告" value="专题报告" />
            <el-option label="快报" value="快报" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建人">
          <el-input v-model="createForm.creator" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="createReport">生成报告</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.report-center {
  display: flex;
  flex-direction: column;
  gap: var(--platform-section-gap);
}

.page-card {
  border: 1px solid var(--platform-border-subtle);
}

.filter-form {
  margin-bottom: 10px;
}

.cell-link {
  padding: 0;
  color: var(--platform-accent-strong);
}
</style>
