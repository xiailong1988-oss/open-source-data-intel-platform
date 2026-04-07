<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageSectionHeader from '../../components/common/PageSectionHeader.vue'
import WorkbenchHero from '../../components/common/WorkbenchHero.vue'
import WorkbenchMetricGrid from '../../components/common/WorkbenchMetricGrid.vue'
import { warningRulesMock } from '../../mock/analysisWarning'
import type { WarningRuleItem } from '../../types/analysisWarning'

type RuleStatusFilter = WarningRuleItem['status'] | '全部'
type RuleTypeFilter = WarningRuleItem['ruleType'] | '全部'
type AlertLevelFilter = WarningRuleItem['alertLevel'] | '全部'
type FormMode = 'create' | 'edit'

const ruleList = ref<WarningRuleItem[]>(warningRulesMock.map((item) => ({ ...item })))
const drawerVisible = ref(false)
const selectedRule = ref<WarningRuleItem | null>(null)
const dialogVisible = ref(false)
const formMode = ref<FormMode>('create')
const saving = ref(false)
const togglingIds = ref<string[]>([])
const route = useRoute()
const router = useRouter()

const filterForm = reactive({
  keyword: '',
  status: '全部' as RuleStatusFilter,
  ruleType: '全部' as RuleTypeFilter,
  alertLevel: '全部' as AlertLevelFilter,
})

const formModel = reactive<WarningRuleItem>({
  id: '',
  name: '',
  ruleType: '关键词触发',
  triggerMethod: '',
  alertLevel: '中',
  status: '启用',
  updatedAt: '',
  description: '',
})

const statusOptions: RuleStatusFilter[] = ['全部', '启用', '停用']
const ruleTypeOptions: RuleTypeFilter[] = ['全部', '关键词触发', '热度突增触发', '地区异常触发', '敏感事件触发']
const levelOptions: AlertLevelFilter[] = ['全部', '高', '中', '低']

const formatCount = (value: number) => new Intl.NumberFormat('zh-CN').format(value)

/**
 * 规则配置页同时承担“配置列表”和“观察台”的职责，
 * 这里统一回到默认筛选态，避免用户从别的钻取入口返回后保留旧过滤条件。
 */
const resetFilterState = () => {
  filterForm.keyword = ''
  filterForm.status = '全部'
  filterForm.ruleType = '全部'
  filterForm.alertLevel = '全部'
}

const filteredRules = computed(() =>
  ruleList.value.filter((item) => {
    const hitKeyword =
      !filterForm.keyword || item.name.includes(filterForm.keyword) || item.triggerMethod.includes(filterForm.keyword)
    const hitStatus = filterForm.status === '全部' || item.status === filterForm.status
    const hitRuleType = filterForm.ruleType === '全部' || item.ruleType === filterForm.ruleType
    const hitAlertLevel = filterForm.alertLevel === '全部' || item.alertLevel === filterForm.alertLevel
    return hitKeyword && hitStatus && hitRuleType && hitAlertLevel
  }),
)

const levelTagMap: Record<string, 'danger' | 'warning' | 'info'> = {
  高: 'danger',
  中: 'warning',
  低: 'info',
}

const statusTagMap: Record<string, 'success' | 'info'> = {
  启用: 'success',
  停用: 'info',
}

const focusedRule = computed(() => selectedRule.value ?? filteredRules.value[0] ?? null)

const summaryCards = computed(() => {
  const total = filteredRules.value.length
  const enabled = filteredRules.value.filter((item) => item.status === '启用').length
  const disabled = filteredRules.value.filter((item) => item.status === '停用').length
  const highLevel = filteredRules.value.filter((item) => item.alertLevel === '高').length

  return [
    {
      id: 'all',
      label: '规则总量',
      value: formatCount(total),
      hint: '当前筛选范围内的配置规则',
      action: () => resetFilterState(),
    },
    {
      id: 'enabled',
      label: '启用规则',
      value: formatCount(enabled),
      hint: '持续参与告警触发的规则',
      action: () => {
        filterForm.status = '启用'
      },
    },
    {
      id: 'disabled',
      label: '停用规则',
      value: formatCount(disabled),
      hint: '需要复核配置合理性',
      action: () => {
        filterForm.status = '停用'
      },
    },
    {
      id: 'high',
      label: '高等级规则',
      value: formatCount(highLevel),
      hint: '触发后优先进入高风险处置链路',
      action: () => {
        filterForm.alertLevel = '高'
      },
    },
  ]
})

const typeInsights = computed(() =>
  ruleTypeOptions
    .filter((item) => item !== '全部')
    .map((type) => ({
      type,
      count: filteredRules.value.filter((item) => item.ruleType === type).length,
    })),
)

const latestUpdatedRules = computed(() =>
  [...filteredRules.value].sort((left, right) => right.updatedAt.localeCompare(left.updatedAt)).slice(0, 4),
)

const nowTime = () => new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')

const resetFilter = () => {
  resetFilterState()
  ElMessage.success('筛选条件已重置')
}

const openCreate = () => {
  formMode.value = 'create'
  formModel.id = ''
  formModel.name = ''
  formModel.ruleType = '关键词触发'
  formModel.triggerMethod = ''
  formModel.alertLevel = '中'
  formModel.status = '启用'
  formModel.updatedAt = ''
  formModel.description = ''
  dialogVisible.value = true
}

const openEdit = (item: WarningRuleItem) => {
  formMode.value = 'edit'
  formModel.id = item.id
  formModel.name = item.name
  formModel.ruleType = item.ruleType
  formModel.triggerMethod = item.triggerMethod
  formModel.alertLevel = item.alertLevel
  formModel.status = item.status
  formModel.updatedAt = item.updatedAt
  formModel.description = item.description
  dialogVisible.value = true
}

const saveRule = async () => {
  if (!formModel.name || !formModel.triggerMethod || !formModel.description) {
    ElMessage.warning('请补全规则名称、触发方式和规则说明')
    return
  }

  saving.value = true
  await new Promise((resolve) => setTimeout(resolve, 600))

  if (formMode.value === 'create') {
    ruleList.value.unshift({
      ...formModel,
      id: `RULE-${Math.floor(Math.random() * 9000) + 1000}`,
      updatedAt: nowTime(),
    })
    ElMessage.success('规则已新增（mock）')
  } else {
    const index = ruleList.value.findIndex((item) => item.id === formModel.id)
    if (index > -1) {
      ruleList.value[index] = {
        ...ruleList.value[index],
        ...formModel,
        updatedAt: nowTime(),
      }
      ElMessage.success('规则已更新（mock）')
    }
  }

  saving.value = false
  dialogVisible.value = false
}

const toggleRule = async (item: WarningRuleItem) => {
  togglingIds.value.push(item.id)
  await new Promise((resolve) => setTimeout(resolve, 500))

  item.status = item.status === '启用' ? '停用' : '启用'
  item.updatedAt = nowTime()
  togglingIds.value = togglingIds.value.filter((id) => id !== item.id)

  ElMessage.success(`规则已${item.status === '启用' ? '启用' : '停用'}`)
}

const isToggling = (id: string) => togglingIds.value.includes(id)

const openDetail = (item: WarningRuleItem) => {
  selectedRule.value = item
  drawerVisible.value = true
}

const toSearch = (text: string) => {
  router.push({ path: '/smart-search', query: { keyword: text, mode: '全文检索' } })
}

const toRecords = (ruleName: string) => {
  router.push({ path: '/analysis-warning/records', query: { keyword: ruleName, from: 'rule-center' } })
}

/**
 * 统一解析来自概览页、预警记录页和首页的 drill-down 参数。
 * 规则页支持按关键词、启停状态、规则类型、告警等级和指定规则定位详情。
 */
const applyRouteQuery = () => {
  resetFilterState()
  drawerVisible.value = false
  selectedRule.value = null

  const keyword = String(route.query.keyword ?? '')
  const status = String(route.query.status ?? '全部')
  const ruleType = String(route.query.ruleType ?? '全部')
  const alertLevel = String(route.query.alertLevel ?? route.query.level ?? '全部')
  const ruleId = String(route.query.ruleId ?? '')
  const autoOpen = route.query.autoOpen === 'first'

  if (keyword) {
    filterForm.keyword = keyword
  }

  if (statusOptions.includes(status as RuleStatusFilter)) {
    filterForm.status = status as RuleStatusFilter
  }

  if (ruleTypeOptions.includes(ruleType as RuleTypeFilter)) {
    filterForm.ruleType = ruleType as RuleTypeFilter
  }

  if (levelOptions.includes(alertLevel as AlertLevelFilter)) {
    filterForm.alertLevel = alertLevel as AlertLevelFilter
  }

  const matched = ruleList.value.find((item) => item.id === ruleId)
  if (matched) {
    openDetail(matched)
    return
  }

  if (autoOpen && filteredRules.value.length) {
    openDetail(filteredRules.value[0])
  }
}

watch(() => route.query, applyRouteQuery, { immediate: true })

watch(filteredRules, (list) => {
  if (!selectedRule.value) {
    return
  }

  if (!list.some((item) => item.id === selectedRule.value?.id)) {
    selectedRule.value = list[0] ?? null
  }
})
</script>

<template>
  <section class="warning-rules stage3-page">
    <WorkbenchHero
      eyebrow="Rule Center"
      title="预警规则配置工作台"
      description="把规则配置、启停状态、运行观察和详情查看收束在一个工作区内，便于从概览进入到具体规则的调整动作。"
    >
      <template #meta>
        <el-button type="primary" @click="openCreate">新增规则</el-button>
      </template>
    </WorkbenchHero>

    <WorkbenchMetricGrid :items="summaryCards" />

    <div class="warning-rules__main-grid">
      <div class="warning-rules__primary">
        <el-card shadow="never" class="page-card">
          <template #header>
            <div class="warning-rules__card-header">
              <div class="platform-page-heading">
                <h3>规则列表</h3>
                <p>围绕规则名称、触发方式、启停状态和告警等级管理预警配置能力。</p>
              </div>
              <el-tag effect="plain" type="info">当前 {{ filteredRules.length }} 条</el-tag>
            </div>
          </template>

          <el-form :inline="true" :model="filterForm" class="filter-form">
            <el-form-item>
              <el-input v-model="filterForm.keyword" placeholder="搜索规则名称/触发方式" clearable />
            </el-form-item>
            <el-form-item>
              <el-select v-model="filterForm.ruleType" style="width: 140px">
                <el-option v-for="item in ruleTypeOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-select v-model="filterForm.alertLevel" style="width: 120px">
                <el-option v-for="item in levelOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-select v-model="filterForm.status" style="width: 120px">
                <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button @click="resetFilter">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="filteredRules" stripe>
            <el-table-column label="规则名称" min-width="220" show-overflow-tooltip>
              <template #default="{ row }">
                <el-button link class="cell-link" @click="openDetail(row)">{{ row.name }}</el-button>
              </template>
            </el-table-column>
            <el-table-column prop="ruleType" label="规则类型" width="140" />
            <el-table-column label="触发方式" min-width="220" show-overflow-tooltip>
              <template #default="{ row }">
                <el-button link class="cell-link" @click="toSearch(row.triggerMethod)">{{ row.triggerMethod }}</el-button>
              </template>
            </el-table-column>
            <el-table-column label="告警等级" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="levelTagMap[row.alertLevel]">{{ row.alertLevel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="statusTagMap[row.status]">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="updatedAt" label="更新时间" width="160" />
            <el-table-column label="操作" width="260" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
                <el-button link :loading="isToggling(row.id)" @click="toggleRule(row)">
                  {{ row.status === '启用' ? '停用' : '启用' }}
                </el-button>
                <el-button link @click="openDetail(row)">查看详情</el-button>
              </template>
            </el-table-column>
            <template #empty>
              <el-empty description="暂无规则数据" :image-size="72" />
            </template>
          </el-table>
        </el-card>
      </div>

      <div class="warning-rules__aside">
        <el-card shadow="never" class="detail-card warning-rules__focus-card">
          <PageSectionHeader
            title="当前规则焦点"
            description="用于快速确认规则类型、触发方式和后续联动入口。"
            eyebrow="Focus"
            compact
          />

          <template v-if="focusedRule">
            <div class="warning-rules__focus-head">
              <div>
                <h4>{{ focusedRule.name }}</h4>
                <p>{{ focusedRule.description }}</p>
              </div>
              <el-tag size="small" :type="levelTagMap[focusedRule.alertLevel]">{{ focusedRule.alertLevel }}级</el-tag>
            </div>

            <el-descriptions :column="1" border>
              <el-descriptions-item label="规则类型">{{ focusedRule.ruleType }}</el-descriptions-item>
              <el-descriptions-item label="触发方式">{{ focusedRule.triggerMethod }}</el-descriptions-item>
              <el-descriptions-item label="状态">{{ focusedRule.status }}</el-descriptions-item>
              <el-descriptions-item label="更新时间">{{ focusedRule.updatedAt }}</el-descriptions-item>
            </el-descriptions>

            <div class="warning-rules__action-row">
              <el-button type="primary" @click="openDetail(focusedRule)">查看详情</el-button>
              <el-button @click="openEdit(focusedRule)">编辑规则</el-button>
              <el-button @click="toRecords(focusedRule.name)">查看关联记录</el-button>
            </div>
          </template>

          <el-empty v-else description="暂无可展示的规则焦点" :image-size="72" />
        </el-card>

        <el-card shadow="never" class="detail-card">
          <PageSectionHeader
            title="规则类型观察"
            description="帮助快速判断当前规则池在触发策略上的分布。"
            eyebrow="Signals"
            compact
          />
          <div class="warning-rules__list-block">
            <button
              v-for="item in typeInsights"
              :key="item.type"
              type="button"
              class="warning-rules__list-item"
              @click="filterForm.ruleType = item.type"
            >
              <span>{{ item.type }}</span>
              <strong>{{ formatCount(item.count) }}</strong>
            </button>
          </div>
        </el-card>

        <el-card shadow="never" class="detail-card">
          <PageSectionHeader
            title="最近更新"
            description="快速定位最近维护过的规则，便于回看配置变更。"
            eyebrow="Recent"
            compact
          />
          <div class="warning-rules__list-block">
            <button
              v-for="item in latestUpdatedRules"
              :key="item.id"
              type="button"
              class="warning-rules__list-item warning-rules__list-item--column"
              @click="openDetail(item)"
            >
              <strong>{{ item.name }}</strong>
              <span>{{ item.updatedAt }}</span>
            </button>
          </div>
        </el-card>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="formMode === 'create' ? '新增规则' : '编辑规则'" width="640px">
      <el-form :model="formModel" label-width="90px">
        <el-form-item label="规则名称">
          <el-input v-model="formModel.name" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="规则类型">
              <el-select v-model="formModel.ruleType">
                <el-option label="关键词触发" value="关键词触发" />
                <el-option label="热度突增触发" value="热度突增触发" />
                <el-option label="地区异常触发" value="地区异常触发" />
                <el-option label="敏感事件触发" value="敏感事件触发" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="告警等级">
              <el-select v-model="formModel.alertLevel">
                <el-option label="高" value="高" />
                <el-option label="中" value="中" />
                <el-option label="低" value="低" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="触发方式">
          <el-input v-model="formModel.triggerMethod" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formModel.status">
            <el-radio label="启用">启用</el-radio>
            <el-radio label="停用">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="规则说明">
          <el-input v-model="formModel.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveRule">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="drawerVisible" title="规则详情" size="560px" destroy-on-close append-to-body>
      <el-descriptions v-if="selectedRule" :column="1" border>
        <el-descriptions-item label="规则名称">{{ selectedRule.name }}</el-descriptions-item>
        <el-descriptions-item label="规则类型">{{ selectedRule.ruleType }}</el-descriptions-item>
        <el-descriptions-item label="触发方式">{{ selectedRule.triggerMethod }}</el-descriptions-item>
        <el-descriptions-item label="告警等级">{{ selectedRule.alertLevel }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ selectedRule.status }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ selectedRule.updatedAt }}</el-descriptions-item>
        <el-descriptions-item label="规则说明">{{ selectedRule.description }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="drawerVisible = false">关闭</el-button>
      </template>
    </el-drawer>
  </section>
</template>

<style scoped>
.warning-rules {
  min-width: 0;
}

.warning-rules__metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--platform-card-gap);
}

.warning-rules__main-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.65fr) minmax(340px, 0.95fr);
  gap: var(--platform-section-gap);
  align-items: start;
}

.warning-rules__aside {
  display: flex;
  flex-direction: column;
  gap: var(--platform-section-gap);
}

.warning-rules__card-header,
.warning-rules__focus-head {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: 16px;
}

.warning-rules__focus-head {
  margin-top: 18px;
  margin-bottom: 18px;
}

.warning-rules__focus-head h4 {
  margin: 0;
  color: var(--platform-text-primary);
  font-size: 18px;
}

.warning-rules__focus-head p {
  margin: 10px 0 0;
  color: var(--platform-text-secondary);
  line-height: 1.7;
}

.warning-rules__action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.warning-rules__list-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 18px;
}

.warning-rules__list-item {
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

.warning-rules__list-item--column {
  align-items: flex-start;
  flex-direction: column;
}

.warning-rules__list-item:hover {
  transform: translateY(-2px);
  border-color: var(--platform-border-strong);
  background: rgba(14, 26, 42, 0.94);
}

.warning-rules__list-item span {
  color: var(--platform-text-secondary);
  font-size: 12px;
}

.warning-rules__list-item strong {
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

@media (max-width: 1800px) {
  .warning-rules__metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .warning-rules__main-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .warning-rules__metric-grid {
    grid-template-columns: 1fr;
  }

  .warning-rules__card-header,
  .warning-rules__focus-head {
    flex-direction: column;
  }
}
</style>
