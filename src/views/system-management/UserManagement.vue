<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import PageSectionHeader from '../../components/common/PageSectionHeader.vue'
import WorkbenchHero from '../../components/common/WorkbenchHero.vue'
import WorkbenchMetricGrid from '../../components/common/WorkbenchMetricGrid.vue'
import { systemUserMockList } from '../../mock/systemManagement'
import type { SystemUserItem, UserRole, UserStatus } from '../../types/systemManagement'

type UserStatusFilter = UserStatus | '全部'
type FormMode = 'create' | 'edit'

const userList = ref<SystemUserItem[]>(systemUserMockList.map((item) => ({ ...item })))
const selectedUser = ref<SystemUserItem | null>(userList.value[0] ?? null)
const formVisible = ref(false)
const formMode = ref<FormMode>('create')
const saving = ref(false)
const togglingIds = ref<string[]>([])

const filterForm = reactive({
  keyword: '',
  status: '全部' as UserStatusFilter,
})

const formModel = reactive<SystemUserItem>({
  id: '',
  username: '',
  realName: '',
  role: '分析师',
  department: '',
  status: '启用',
  createdAt: '',
  phone: '',
  email: '',
})

const roleOptions: UserRole[] = ['平台管理员', '分析师', '审计员', '运维工程师', '业务主管']
const statusOptions: UserStatusFilter[] = ['全部', '启用', '停用']

const filteredUsers = computed(() =>
  userList.value.filter((item) => {
    const hitKeyword =
      !filterForm.keyword ||
      item.username.includes(filterForm.keyword) ||
      item.realName.includes(filterForm.keyword) ||
      item.role.includes(filterForm.keyword) ||
      item.department.includes(filterForm.keyword)
    const hitStatus = filterForm.status === '全部' || item.status === filterForm.status
    return hitKeyword && hitStatus
  }),
)

const statusTagMap: Record<UserStatus, 'success' | 'info'> = {
  启用: 'success',
  停用: 'info',
}

const resolveStatusType = (status: UserStatus) => statusTagMap[status]
const formatCount = (value: number) => new Intl.NumberFormat('zh-CN').format(value)
const nowTime = () => new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')

const focusedUser = computed(() => selectedUser.value ?? filteredUsers.value[0] ?? null)

const summaryCards = computed(() => {
  const enabled = filteredUsers.value.filter((item) => item.status === '启用').length
  const disabled = filteredUsers.value.filter((item) => item.status === '停用').length
  const adminCount = filteredUsers.value.filter((item) => item.role === '平台管理员').length
  const analystCount = filteredUsers.value.filter((item) => item.role === '分析师').length

  return [
    {
      id: 'all',
      label: '用户总量',
      value: formatCount(filteredUsers.value.length),
      hint: '当前筛选范围内的平台用户',
      action: () => {
        filterForm.keyword = ''
        filterForm.status = '全部'
      },
    },
    {
      id: 'enabled',
      label: '启用账号',
      value: formatCount(enabled),
      hint: '当前可正常登录与操作的平台账号',
      action: () => {
        filterForm.status = '启用'
      },
    },
    {
      id: 'disabled',
      label: '停用账号',
      value: formatCount(disabled),
      hint: '需要关注权限回收与状态恢复',
      action: () => {
        filterForm.status = '停用'
      },
    },
    {
      id: 'analyst',
      label: '分析岗位',
      value: formatCount(adminCount + analystCount),
      hint: '与平台分析、治理和管理核心流程直接相关',
      action: () => {
        filterForm.keyword = '分析'
      },
    },
  ]
})

const roleInsights = computed(() =>
  roleOptions
    .map((role) => ({
      role,
      count: filteredUsers.value.filter((item) => item.role === role).length,
    }))
    .sort((left, right) => right.count - left.count),
)

const departmentInsights = computed(() => {
  const counter = new Map<string, number>()

  filteredUsers.value.forEach((item) => {
    counter.set(item.department, (counter.get(item.department) ?? 0) + 1)
  })

  return Array.from(counter.entries())
    .map(([department, count]) => ({ department, count }))
    .sort((left, right) => right.count - left.count)
})

const recentUsers = computed(() =>
  [...filteredUsers.value]
    .sort((left, right) => right.createdAt.localeCompare(left.createdAt))
    .slice(0, 4),
)

/**
 * 用户表单在新增和编辑之间频繁切换。
 * 统一用一个重置函数回到默认状态，避免旧表单字段残留到下一次操作里。
 */
const resetFormModel = () => {
  formModel.id = ''
  formModel.username = ''
  formModel.realName = ''
  formModel.role = '分析师'
  formModel.department = ''
  formModel.status = '启用'
  formModel.createdAt = ''
  formModel.phone = ''
  formModel.email = ''
}

const resetFilter = () => {
  filterForm.keyword = ''
  filterForm.status = '全部'
  ElMessage.success('筛选条件已重置')
}

const selectUser = (item: SystemUserItem | null) => {
  if (!item) {
    return
  }

  selectedUser.value = item
}

const openCreate = () => {
  formMode.value = 'create'
  resetFormModel()
  formVisible.value = true
}

const openEdit = (item: SystemUserItem) => {
  formMode.value = 'edit'
  selectedUser.value = item
  formModel.id = item.id
  formModel.username = item.username
  formModel.realName = item.realName
  formModel.role = item.role
  formModel.department = item.department
  formModel.status = item.status
  formModel.createdAt = item.createdAt
  formModel.phone = item.phone
  formModel.email = item.email
  formVisible.value = true
}

const saveUser = async () => {
  if (!formModel.username || !formModel.realName || !formModel.department || !formModel.email) {
    ElMessage.warning('请补全用户名、姓名、部门和邮箱')
    return
  }

  saving.value = true
  await new Promise((resolve) => setTimeout(resolve, 600))

  if (formMode.value === 'create') {
    const createdUser: SystemUserItem = {
      ...formModel,
      id: `USR-${Math.floor(Math.random() * 9000) + 1000}`,
      createdAt: nowTime(),
    }

    userList.value.unshift(createdUser)
    selectedUser.value = createdUser
    ElMessage.success('用户已新增（mock）')
  } else {
    const index = userList.value.findIndex((item) => item.id === formModel.id)
    if (index > -1) {
      userList.value[index] = {
        ...userList.value[index],
        ...formModel,
      }
      selectedUser.value = userList.value[index]
      ElMessage.success('用户信息已更新（mock）')
    }
  }

  saving.value = false
  formVisible.value = false
}

const isToggling = (id: string) => togglingIds.value.includes(id)

const toggleUser = async (item: SystemUserItem) => {
  togglingIds.value.push(item.id)
  await new Promise((resolve) => setTimeout(resolve, 400))

  item.status = item.status === '启用' ? '停用' : '启用'
  togglingIds.value = togglingIds.value.filter((id) => id !== item.id)
  selectedUser.value = item
  ElMessage.success(`用户已${item.status === '启用' ? '启用' : '停用'}`)
}

watch(filteredUsers, (list) => {
  if (!selectedUser.value) {
    selectedUser.value = list[0] ?? null
    return
  }

  if (!list.some((item) => item.id === selectedUser.value?.id)) {
    selectedUser.value = list[0] ?? null
  }
})
</script>

<template>
  <section class="user-center stage4-page">
    <WorkbenchHero
      eyebrow="User Center"
      title="用户管理工作台"
      description="围绕账号状态、角色归属、组织分布和当前焦点用户组织统一工作区，让系统管理不再只是单纯的表格维护页。"
    >
      <template #meta>
        <el-button type="primary" @click="openCreate">新增用户</el-button>
      </template>
    </WorkbenchHero>

    <WorkbenchMetricGrid :items="summaryCards" />

    <div class="user-center__main-grid">
      <div class="user-center__primary">
        <el-card shadow="never" class="page-card">
          <template #header>
            <div class="user-center__card-header">
              <div class="platform-page-heading">
                <h3>用户列表</h3>
                <p>支持按用户名、姓名、部门和状态聚焦当前账号池，并继续进入编辑和启停动作。</p>
              </div>
              <el-tag effect="plain" type="info">当前 {{ filteredUsers.length }} 人</el-tag>
            </div>
          </template>

          <el-form :inline="true" :model="filterForm" class="filter-form">
            <el-form-item>
              <el-input v-model="filterForm.keyword" placeholder="搜索用户名/姓名/部门" clearable />
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

          <el-table :data="filteredUsers" stripe highlight-current-row @row-click="selectUser">
            <el-table-column prop="username" label="用户名" width="150" />
            <el-table-column prop="realName" label="姓名" width="110" />
            <el-table-column prop="role" label="角色" width="130" />
            <el-table-column prop="department" label="部门" min-width="180" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="resolveStatusType(row.status)">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="170" />
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
                <el-button link :loading="isToggling(row.id)" @click="toggleUser(row)">
                  {{ row.status === '启用' ? '停用' : '启用' }}
                </el-button>
              </template>
            </el-table-column>
            <template #empty>
              <el-empty description="暂无用户数据" :image-size="72" />
            </template>
          </el-table>
        </el-card>
      </div>

      <div class="user-center__aside">
        <el-card shadow="never" class="detail-card user-center__focus-card">
          <PageSectionHeader
            title="当前用户焦点"
            description="右侧面板持续跟随当前选中的用户，方便快速核对账号信息、角色归属和操作入口。"
            eyebrow="Focus"
            compact
          />

          <template v-if="focusedUser">
            <div class="user-center__focus-head">
              <div>
                <h4>{{ focusedUser.realName }}</h4>
                <p>{{ focusedUser.username }} / {{ focusedUser.department }}</p>
              </div>
              <el-tag size="small" :type="resolveStatusType(focusedUser.status)">{{ focusedUser.status }}</el-tag>
            </div>

            <el-descriptions :column="1" border>
              <el-descriptions-item label="角色">{{ focusedUser.role }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ focusedUser.phone }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ focusedUser.email }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ focusedUser.createdAt }}</el-descriptions-item>
            </el-descriptions>

            <div class="user-center__action-row">
              <el-button type="primary" @click="openEdit(focusedUser)">编辑用户</el-button>
              <el-button :loading="isToggling(focusedUser.id)" @click="toggleUser(focusedUser)">
                {{ focusedUser.status === '启用' ? '停用账号' : '启用账号' }}
              </el-button>
            </div>
          </template>

          <el-empty v-else description="暂无可展示的用户焦点" :image-size="72" />
        </el-card>

        <el-card shadow="never" class="detail-card">
          <PageSectionHeader
            title="角色分布观察"
            description="帮助快速识别当前账号池在不同岗位上的配比。"
            eyebrow="Signals"
            compact
          />
          <div class="user-center__list-block">
            <button
              v-for="item in roleInsights"
              :key="item.role"
              type="button"
              class="user-center__list-item"
              @click="filterForm.keyword = item.role"
            >
              <span>{{ item.role }}</span>
              <strong>{{ formatCount(item.count) }}</strong>
            </button>
          </div>
        </el-card>

        <el-card shadow="never" class="detail-card">
          <PageSectionHeader
            title="部门与最近账号"
            description="从组织分布和最近新增两个角度补足系统管理观察。"
            eyebrow="Recent"
            compact
          />
          <div class="user-center__subsection">
            <span class="user-center__subsection-title">部门分布</span>
            <div class="user-center__list-block user-center__list-block--compact">
              <button
                v-for="item in departmentInsights"
                :key="item.department"
                type="button"
                class="user-center__list-item"
                @click="filterForm.keyword = item.department"
              >
                <span>{{ item.department }}</span>
                <strong>{{ formatCount(item.count) }}</strong>
              </button>
            </div>
          </div>

          <div class="user-center__subsection">
            <span class="user-center__subsection-title">最近新增</span>
            <div class="user-center__list-block user-center__list-block--compact">
              <button
                v-for="item in recentUsers"
                :key="item.id"
                type="button"
                class="user-center__list-item user-center__list-item--column"
                @click="selectUser(item)"
              >
                <strong>{{ item.realName }}</strong>
                <span>{{ item.createdAt }}</span>
              </button>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <el-dialog v-model="formVisible" :title="formMode === 'create' ? '新增用户' : '编辑用户'" width="640px">
      <el-form :model="formModel" label-width="88px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="用户名">
              <el-input v-model="formModel.username" placeholder="例如：analyst.liu" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名">
              <el-input v-model="formModel.realName" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="角色">
              <el-select v-model="formModel.role">
                <el-option v-for="item in roleOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="formModel.status">
                <el-radio label="启用">启用</el-radio>
                <el-radio label="停用">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="部门">
          <el-input v-model="formModel.department" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="formModel.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="formModel.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.user-center {
  min-width: 0;
}

.user-center__metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--platform-card-gap);
}

.user-center__main-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.65fr) minmax(340px, 0.95fr);
  gap: var(--platform-section-gap);
  align-items: start;
}

.user-center__aside {
  display: flex;
  flex-direction: column;
  gap: var(--platform-section-gap);
}

.user-center__card-header,
.user-center__focus-head {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: 16px;
}

.user-center__focus-head {
  margin-top: 18px;
  margin-bottom: 18px;
}

.user-center__focus-head h4 {
  margin: 0;
  color: var(--platform-text-primary);
  font-size: 18px;
}

.user-center__focus-head p {
  margin: 10px 0 0;
  color: var(--platform-text-secondary);
  line-height: 1.7;
}

.user-center__action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.user-center__subsection + .user-center__subsection {
  margin-top: 18px;
}

.user-center__subsection-title {
  display: inline-flex;
  margin-bottom: 12px;
  color: var(--platform-text-secondary);
  font-size: 12px;
  letter-spacing: 0.06em;
}

.user-center__list-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 18px;
}

.user-center__list-block--compact {
  margin-top: 0;
}

.user-center__list-item {
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

.user-center__list-item--column {
  align-items: flex-start;
  flex-direction: column;
}

.user-center__list-item:hover {
  transform: translateY(-2px);
  border-color: var(--platform-border-strong);
  background: rgba(14, 26, 42, 0.94);
}

.user-center__list-item span {
  color: var(--platform-text-secondary);
  font-size: 12px;
}

.user-center__list-item strong {
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

@media (max-width: 1800px) {
  .user-center__metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .user-center__main-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .user-center__metric-grid {
    grid-template-columns: 1fr;
  }

  .user-center__card-header,
  .user-center__focus-head {
    flex-direction: column;
  }
}
</style>
