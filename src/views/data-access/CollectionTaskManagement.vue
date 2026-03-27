<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageSectionHeader from '../../components/common/PageSectionHeader.vue'
import WorkbenchHero from '../../components/common/WorkbenchHero.vue'
import WorkbenchMetricGrid from '../../components/common/WorkbenchMetricGrid.vue'
import { collectionTaskLogMockMap, collectionTaskMockList } from '../../mock/dataAccess'
import type { CollectionTaskItem, TaskLogEntry, TaskStatus, TaskType } from '../../types/dataAccess'

type FilterOption<T extends string> = T | '全部'

const taskList = ref<CollectionTaskItem[]>(collectionTaskMockList.map((item) => ({ ...item })))
const runningMap = reactive<Record<string, boolean>>({})
const logDialogVisible = ref(false)
const currentLogs = ref<TaskLogEntry[]>([])
const currentTaskName = ref('')
const selectedTask = ref<CollectionTaskItem | null>(taskList.value[0] ?? null)
const route = useRoute()
const router = useRouter()

const filterForm = reactive({
  keyword: '',
  taskType: '全部' as FilterOption<TaskType>,
  status: '全部' as FilterOption<TaskStatus>,
})

const taskTypeOptions = computed<Array<FilterOption<TaskType>>>(() => {
  const options = new Set(taskList.value.map((item) => item.taskType))
  return ['全部', ...options]
})

const statusOptions = computed<Array<FilterOption<TaskStatus>>>(() => {
  const options = new Set(taskList.value.map((item) => item.status))
  return ['全部', ...options]
})

const filteredTasks = computed(() =>
  taskList.value.filter((item) => {
    const hitKeyword =
      !filterForm.keyword ||
      item.taskName.includes(filterForm.keyword) ||
      item.sourceName.includes(filterForm.keyword)
    const hitType = filterForm.taskType === '全部' || item.taskType === filterForm.taskType
    const hitStatus = filterForm.status === '全部' || item.status === filterForm.status
    return hitKeyword && hitType && hitStatus
  }),
)

const statusTagMap: Record<TaskStatus, 'success' | 'warning' | 'danger'> = {
  运行中: 'success',
  已暂停: 'warning',
  异常: 'danger',
}

const resultTagMap: Record<CollectionTaskItem['latestResult'], 'success' | 'warning' | 'danger'> = {
  成功: 'success',
  失败: 'danger',
  部分成功: 'warning',
}
const resolveStatusType = (status: TaskStatus) => statusTagMap[status]
const resolveResultType = (result: CollectionTaskItem['latestResult']) => resultTagMap[result]

const formatCount = (value: number) => new Intl.NumberFormat('zh-CN').format(value)
const formatRate = (value: number) => `${value.toFixed(1)}%`
const formatNow = () => new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')

/**
 * 采集任务页需要同时接收菜单直达和 drill-down 跳转。
 * 每次重新消费 query 之前都先回到默认筛选态，避免把上一次的筛选残留到新的任务观察范围里。
 */
const resetFilterState = () => {
  filterForm.keyword = ''
  filterForm.taskType = '全部'
  filterForm.status = '全部'
}

const focusedTask = computed(() => selectedTask.value ?? filteredTasks.value[0] ?? null)

const summaryCards = computed(() => {
  const running = filteredTasks.value.filter((item) => item.status === '运行中').length
  const paused = filteredTasks.value.filter((item) => item.status === '已暂停').length
  const abnormal = filteredTasks.value.filter((item) => item.status === '异常').length
  const recordTotal = filteredTasks.value.reduce((sum, item) => sum + item.records, 0)

  return [
    {
      id: 'all',
      label: '当前任务数',
      value: formatCount(filteredTasks.value.length),
      hint: '当前筛选范围内的采集任务',
      action: () => resetFilterState(),
    },
    {
      id: 'running',
      label: '运行中任务',
      value: formatCount(running),
      hint: '可继续跟踪执行结果与吞吐变化',
      action: () => {
        filterForm.status = '运行中'
      },
    },
    {
      id: 'abnormal',
      label: '异常 / 暂停',
      value: formatCount(paused + abnormal),
      hint: '优先关注恢复和补偿动作',
      action: () => {
        filterForm.status = abnormal ? '异常' : '已暂停'
      },
    },
    {
      id: 'records',
      label: '累计采集记录',
      value: formatCount(recordTotal),
      hint: '当前任务池的采集吞吐总量',
      action: () => resetFilterState(),
    },
  ]
})

const taskTypeInsights = computed(() =>
  taskTypeOptions.value
    .filter((item): item is TaskType => item !== '全部')
    .map((type) => ({
      type,
      count: filteredTasks.value.filter((item) => item.taskType === type).length,
    }))
    .sort((left, right) => right.count - left.count),
)

const attentionQueue = computed(() =>
  filteredTasks.value
    .filter((item) => item.status !== '运行中' || item.latestResult !== '成功')
    .slice(0, 4),
)

const focusLogs = computed(() => {
  const current = focusedTask.value
  if (!current) {
    return []
  }

  return (collectionTaskLogMockMap[current.id] ?? []).slice(0, 3)
})

const resetFilter = () => {
  resetFilterState()
  ElMessage.success('筛选条件已重置')
}

const selectTask = (task: CollectionTaskItem | null) => {
  if (!task) {
    return
  }
  selectedTask.value = task
}

const runTask = async (task: CollectionTaskItem) => {
  runningMap[task.id] = true
  await new Promise((resolve) => setTimeout(resolve, 1000))

  task.latestResult = '成功'
  task.latestRunTime = formatNow()
  task.status = '运行中'
  task.records = Math.max(task.records, 1000) + Math.floor(Math.random() * 3000)
  task.successRate = Number((95 + Math.random() * 5).toFixed(1))
  runningMap[task.id] = false
  selectedTask.value = task
  ElMessage.success(`任务「${task.taskName}」执行完成（mock）`)
}

const toggleTaskStatus = (task: CollectionTaskItem) => {
  if (task.status === '异常') {
    task.status = '运行中'
    selectedTask.value = task
    ElMessage.success(`任务「${task.taskName}」已恢复运行`)
    return
  }

  task.status = task.status === '运行中' ? '已暂停' : '运行中'
  selectedTask.value = task
  ElMessage.success(`任务「${task.taskName}」状态已更新为 ${task.status}`)
}

const openLogs = (task: CollectionTaskItem) => {
  selectedTask.value = task
  currentTaskName.value = task.taskName
  currentLogs.value = collectionTaskLogMockMap[task.id] ?? []
  logDialogVisible.value = true
}

const toSourceManagement = (sourceName: string) => {
  router.push({ path: '/data-access/source-management', query: { keyword: sourceName, from: 'task' } })
}

/**
 * 统一接管首页、概览页和数据源列表页传来的 query。
 * 支持按关键词、任务类型、状态和指定任务 id 定位，并允许自动拉起日志面板做首屏聚焦。
 */
const applyRouteQuery = () => {
  resetFilterState()
  logDialogVisible.value = false
  selectedTask.value = taskList.value[0] ?? null

  const keyword = String(route.query.keyword ?? '')
  const taskType = String(route.query.taskType ?? '全部')
  const status = String(route.query.status ?? '全部')
  const taskId = String(route.query.taskId ?? '')
  const autoOpenLog = route.query.autoOpenLog === 'first'

  if (keyword) {
    filterForm.keyword = keyword
  }

  if (taskTypeOptions.value.includes(taskType as FilterOption<TaskType>)) {
    filterForm.taskType = taskType as FilterOption<TaskType>
  }

  if (statusOptions.value.includes(status as FilterOption<TaskStatus>)) {
    filterForm.status = status as FilterOption<TaskStatus>
  }

  const matchedTask = taskList.value.find((item) => item.id === taskId)
  if (matchedTask) {
    openLogs(matchedTask)
    return
  }

  selectedTask.value = filteredTasks.value[0] ?? taskList.value[0] ?? null

  if (autoOpenLog && filteredTasks.value.length) {
    openLogs(filteredTasks.value[0])
  }
}

watch(() => route.query, applyRouteQuery, { immediate: true })

watch(filteredTasks, (list) => {
  if (!selectedTask.value) {
    selectedTask.value = list[0] ?? null
    return
  }

  if (!list.some((item) => item.id === selectedTask.value?.id)) {
    selectedTask.value = list[0] ?? null
  }
})
</script>

<template>
  <section class="collection-task-center stage4-page">
    <WorkbenchHero
      eyebrow="Task Center"
      title="采集任务调度工作台"
      description="把任务筛选、执行反馈、焦点日志和异常观察统一在一个工作区内，方便从数据接入角度快速判断当前采集链路是否稳定。"
    >
      <template #meta>
        <el-tag effect="plain">支持 drill-down 接管</el-tag>
        <el-tag effect="plain" type="warning">异常任务即时聚焦</el-tag>
        <el-tag effect="plain" type="success">执行日志可快速追踪</el-tag>
      </template>
    </WorkbenchHero>

    <WorkbenchMetricGrid :items="summaryCards" />

    <div class="collection-task-center__main-grid">
      <div class="collection-task-center__primary">
        <el-card shadow="never" class="page-card">
          <template #header>
            <div class="collection-task-center__card-header">
              <div class="platform-page-heading">
                <span class="platform-page-heading__eyebrow">Queue</span>
                <h3>采集任务队列</h3>
                <p>按任务名称、数据源、任务类型和状态聚焦当前观察范围，并从列表直接进入日志和数据源联动。</p>
              </div>
              <el-tag effect="plain" type="info">当前 {{ filteredTasks.length }} 条</el-tag>
            </div>
          </template>

          <el-form :inline="true" :model="filterForm" class="filter-form">
            <el-form-item>
              <el-input v-model="filterForm.keyword" placeholder="搜索任务名称/对应数据源" clearable />
            </el-form-item>
            <el-form-item>
              <el-select v-model="filterForm.taskType" style="width: 150px">
                <el-option v-for="item in taskTypeOptions" :key="item" :label="item" :value="item" />
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

          <el-table :data="filteredTasks" stripe highlight-current-row @row-click="selectTask">
            <el-table-column label="任务名称" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">
                <el-button link class="cell-link" @click="openLogs(row)">{{ row.taskName }}</el-button>
              </template>
            </el-table-column>
            <el-table-column label="对应数据源" min-width="170" show-overflow-tooltip>
              <template #default="{ row }">
                <el-button link class="cell-link" @click="toSourceManagement(row.sourceName)">{{ row.sourceName }}</el-button>
              </template>
            </el-table-column>
            <el-table-column prop="taskType" label="任务类型" width="120" />
            <el-table-column prop="frequency" label="执行频率" width="120" />
            <el-table-column label="最近执行结果" width="120">
              <template #default="{ row }">
                <el-tag size="small" :type="resolveResultType(row.latestResult)">{{ row.latestResult }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="resolveStatusType(row.status)">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="latestRunTime" label="最近执行时间" width="170" />
            <el-table-column label="操作" width="240" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :loading="runningMap[row.id]" @click="runTask(row)">
                  手动执行
                </el-button>
                <el-button link @click="toggleTaskStatus(row)">
                  {{ row.status === '运行中' ? '停用' : '启用' }}
                </el-button>
                <el-button link @click="openLogs(row)">执行日志</el-button>
              </template>
            </el-table-column>
            <template #empty>
              <el-empty description="暂无采集任务" :image-size="72" />
            </template>
          </el-table>
        </el-card>
      </div>

      <div class="collection-task-center__aside">
        <el-card shadow="never" class="detail-card collection-task-center__focus-card">
          <PageSectionHeader
            title="当前任务焦点"
            description="右侧持续跟随当前选中的任务，帮助快速核对执行结果、当前状态和日志入口。"
            eyebrow="Focus"
            compact
          />

          <template v-if="focusedTask">
            <div class="collection-task-center__focus-head">
              <div>
                <h4>{{ focusedTask.taskName }}</h4>
                <p>{{ focusedTask.sourceName }}</p>
              </div>
              <el-tag size="small" :type="resolveStatusType(focusedTask.status)">{{ focusedTask.status }}</el-tag>
            </div>

            <el-descriptions :column="1" border>
              <el-descriptions-item label="任务类型">{{ focusedTask.taskType }}</el-descriptions-item>
              <el-descriptions-item label="执行频率">{{ focusedTask.frequency }}</el-descriptions-item>
              <el-descriptions-item label="最近结果">{{ focusedTask.latestResult }}</el-descriptions-item>
              <el-descriptions-item label="最近执行时间">{{ focusedTask.latestRunTime }}</el-descriptions-item>
              <el-descriptions-item label="累计记录">{{ formatCount(focusedTask.records) }}</el-descriptions-item>
              <el-descriptions-item label="成功率">{{ formatRate(focusedTask.successRate) }}</el-descriptions-item>
            </el-descriptions>

            <div class="collection-task-center__action-row">
              <el-button type="primary" :loading="runningMap[focusedTask.id]" @click="runTask(focusedTask)">
                手动执行
              </el-button>
              <el-button @click="openLogs(focusedTask)">查看日志</el-button>
              <el-button @click="toSourceManagement(focusedTask.sourceName)">查看数据源</el-button>
            </div>

            <div class="collection-task-center__subsection">
              <span class="collection-task-center__subsection-title">最近日志摘要</span>
              <el-timeline>
                <el-timeline-item
                  v-for="log in focusLogs"
                  :key="log.id"
                  :timestamp="log.time"
                  :type="log.level === 'ERROR' ? 'danger' : log.level === 'WARN' ? 'warning' : 'primary'"
                >
                  <span class="task-log-level">{{ log.level }}</span>
                  <span>{{ log.message }}</span>
                </el-timeline-item>
              </el-timeline>
            </div>
          </template>

          <el-empty v-else description="暂无可展示的任务焦点" :image-size="72" />
        </el-card>

        <el-card shadow="never" class="detail-card">
          <PageSectionHeader
            title="任务类型观察"
            description="帮助快速定位当前任务池里的重点采集方式。"
            eyebrow="Signals"
            compact
          />
          <div class="collection-task-center__list-block">
            <button
              v-for="item in taskTypeInsights"
              :key="item.type"
              type="button"
              class="collection-task-center__list-item"
              @click="filterForm.taskType = item.type"
            >
              <span>{{ item.type }}</span>
              <strong>{{ formatCount(item.count) }}</strong>
            </button>
          </div>
        </el-card>

        <el-card shadow="never" class="detail-card">
          <PageSectionHeader
            title="待关注任务"
            description="优先收纳异常、暂停或最近结果不稳定的任务。"
            eyebrow="Attention"
            compact
          />
          <div class="collection-task-center__list-block">
            <button
              v-for="item in attentionQueue"
              :key="item.id"
              type="button"
              class="collection-task-center__list-item collection-task-center__list-item--column"
              @click="selectTask(item)"
            >
              <strong>{{ item.taskName }}</strong>
              <span>{{ item.status }} / {{ item.latestResult }}</span>
            </button>
            <el-empty v-if="!attentionQueue.length" description="当前无重点关注任务" :image-size="64" />
          </div>
        </el-card>
      </div>
    </div>

    <el-dialog
      v-model="logDialogVisible"
      :title="`执行日志 - ${currentTaskName}`"
      width="760px"
      destroy-on-close
      append-to-body
    >
      <el-timeline class="task-log-timeline">
        <el-timeline-item
          v-for="log in currentLogs"
          :key="log.id"
          :timestamp="log.time"
          :type="log.level === 'ERROR' ? 'danger' : log.level === 'WARN' ? 'warning' : 'primary'"
        >
          <span class="task-log-level">{{ log.level }}</span>
          <span>{{ log.message }}</span>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </section>
</template>

<style scoped>
.collection-task-center {
  min-width: 0;
}

.collection-task-center__metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--platform-card-gap);
}

.collection-task-center__main-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.65fr) minmax(340px, 0.95fr);
  gap: var(--platform-section-gap);
  align-items: start;
}

.collection-task-center__aside {
  display: flex;
  flex-direction: column;
  gap: var(--platform-section-gap);
}

.collection-task-center__card-header,
.collection-task-center__focus-head {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: 16px;
}

.collection-task-center__focus-head {
  margin-top: 18px;
  margin-bottom: 18px;
}

.collection-task-center__focus-head h4 {
  margin: 0;
  color: var(--platform-text-primary);
  font-size: 18px;
}

.collection-task-center__focus-head p {
  margin: 10px 0 0;
  color: var(--platform-text-secondary);
  line-height: 1.7;
}

.collection-task-center__action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.collection-task-center__subsection {
  margin-top: 18px;
}

.collection-task-center__subsection-title {
  display: inline-flex;
  margin-bottom: 12px;
  color: var(--platform-text-secondary);
  font-size: 12px;
  letter-spacing: 0.06em;
}

.collection-task-center__list-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 18px;
}

.collection-task-center__list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  width: 100%;
  border: 1px solid var(--platform-border-subtle);
  border-radius: 16px;
  background: rgba(11, 21, 34, 0.82);
  padding: 14px 16px;
  color: var(--platform-text-primary);
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}

.collection-task-center__list-item--column {
  align-items: flex-start;
  flex-direction: column;
}

.collection-task-center__list-item:hover {
  transform: translateY(-2px);
  border-color: var(--platform-border-strong);
  background: rgba(14, 26, 42, 0.94);
}

.collection-task-center__list-item span {
  color: var(--platform-text-secondary);
  font-size: 12px;
}

.collection-task-center__list-item strong {
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

.cell-link {
  padding: 0;
  color: var(--platform-accent-strong);
}

.task-log-level {
  display: inline-block;
  min-width: 52px;
  color: var(--platform-accent-strong);
  font-weight: 600;
}

.task-log-timeline {
  max-height: 430px;
  overflow-y: auto;
}

@media (max-width: 1800px) {
  .collection-task-center__metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .collection-task-center__main-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .collection-task-center__metric-grid {
    grid-template-columns: 1fr;
  }

  .collection-task-center__card-header,
  .collection-task-center__focus-head {
    flex-direction: column;
  }
}
</style>
