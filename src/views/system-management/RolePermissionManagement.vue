<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { TreeInstance } from 'element-plus'
import PageSectionHeader from '../../components/common/PageSectionHeader.vue'
import WorkbenchHero from '../../components/common/WorkbenchHero.vue'
import WorkbenchMetricGrid from '../../components/common/WorkbenchMetricGrid.vue'
import { permissionTreeMock, rolePermissionMockList } from '../../mock/systemManagement'
import type { PermissionNode, RolePermissionItem } from '../../types/systemManagement'

const roleList = ref<RolePermissionItem[]>(
  rolePermissionMockList.map((item) => ({ ...item, permissions: [...item.permissions] })),
)
const selectedRoleId = ref(roleList.value[0]?.id ?? '')
const dialogVisible = ref(false)
const saving = ref(false)
const permissionTreeRef = ref<TreeInstance>()

const selectedRole = computed(() => roleList.value.find((item) => item.id === selectedRoleId.value) ?? null)
const treeExpandedKeys = computed(() => permissionTreeMock.map((item) => item.id))
const formatCount = (value: number) => new Intl.NumberFormat('zh-CN').format(value)

/**
 * 角色详情和权限树需要反复根据权限 id 反查中文名称。
 * 这里提前把权限树拍平成索引，避免在多个面板里重复手写遍历逻辑。
 */
const permissionLabelMap = computed(() => {
  const map = new Map<string, string>()

  const walk = (nodes: PermissionNode[]) => {
    nodes.forEach((node) => {
      map.set(node.id, node.label)
      if (node.children?.length) {
        walk(node.children)
      }
    })
  }

  walk(permissionTreeMock)
  return map
})

const focusedPermissionLabels = computed(() => {
  if (!selectedRole.value) {
    return []
  }

  return selectedRole.value.permissions
    .map((permissionId) => permissionLabelMap.value.get(permissionId) ?? permissionId)
    .slice(0, 8)
})

const summaryCards = computed(() => {
  const totalUsers = roleList.value.reduce((sum, item) => sum + item.userCount, 0)
  const totalPermissions = new Set(roleList.value.flatMap((item) => item.permissions)).size
  const adminRoles = roleList.value.filter((item) => item.permissions.includes('perm-config-manage')).length
  const reportRoles = roleList.value.filter((item) => item.permissions.includes('perm-report-export')).length

  return [
    {
      id: 'roles',
      label: '角色总量',
      value: formatCount(roleList.value.length),
      hint: '当前系统内已定义的角色数量',
      action: () => {
        selectedRoleId.value = roleList.value[0]?.id ?? ''
      },
    },
    {
      id: 'users',
      label: '角色覆盖用户',
      value: formatCount(totalUsers),
      hint: '所有角色当前承接的用户规模',
      action: () => {
        const topRole = [...roleList.value].sort((left, right) => right.userCount - left.userCount)[0]
        if (topRole) {
          selectedRoleId.value = topRole.id
        }
      },
    },
    {
      id: 'system',
      label: '系统级角色',
      value: formatCount(adminRoles),
      hint: '拥有系统配置或权限分配能力的角色',
      action: () => {
        const adminRole = roleList.value.find((item) => item.permissions.includes('perm-config-manage'))
        if (adminRole) {
          selectedRoleId.value = adminRole.id
        }
      },
    },
    {
      id: 'report',
      label: '报告权限角色',
      value: formatCount(reportRoles),
      hint: '具备报告查看、导出或创建能力的角色',
      action: () => {
        const reportRole = roleList.value.find((item) => item.permissions.includes('perm-report-export'))
        if (reportRole) {
          selectedRoleId.value = reportRole.id
        }
      },
    },
    {
      id: 'permission',
      label: '独立权限点',
      value: formatCount(totalPermissions),
      hint: '当前角色池覆盖到的权限粒度',
      action: () => {
        if (selectedRole.value) {
          openPermissionDialog(selectedRole.value)
        }
      },
    },
  ]
})

const moduleInsights = computed(() =>
  permissionTreeMock.map((moduleNode) => ({
    name: moduleNode.label,
    enabledRoles: roleList.value.filter((role) =>
      (moduleNode.children ?? []).some((permission) => role.permissions.includes(permission.id)),
    ).length,
  })),
)

const latestUpdatedRoles = computed(() =>
  [...roleList.value]
    .sort((left, right) => right.updatedAt.localeCompare(left.updatedAt))
    .slice(0, 4),
)

const selectRoleByModule = (moduleName: string) => {
  const matchedNode = permissionTreeMock.find((item) => item.label === moduleName)
  if (!matchedNode) {
    return
  }

  const matchedRole = roleList.value.find((role) =>
    (matchedNode.children ?? []).some((permission) => role.permissions.includes(permission.id)),
  )

  if (matchedRole) {
    selectedRoleId.value = matchedRole.id
  }
}

const openPermissionDialog = (role?: RolePermissionItem) => {
  if (role) {
    selectedRoleId.value = role.id
  }

  if (!selectedRole.value) {
    return
  }

  dialogVisible.value = true
  requestAnimationFrame(() => {
    permissionTreeRef.value?.setCheckedKeys(selectedRole.value?.permissions ?? [])
  })
}

const selectRole = (role: RolePermissionItem | null) => {
  if (!role) {
    return
  }

  selectedRoleId.value = role.id
}

const savePermissions = async () => {
  if (!selectedRole.value) {
    return
  }

  const checkedKeys = permissionTreeRef.value?.getCheckedKeys(false) as string[]
  saving.value = true
  await new Promise((resolve) => setTimeout(resolve, 600))

  selectedRole.value.permissions = checkedKeys
  selectedRole.value.updatedAt = new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')

  saving.value = false
  dialogVisible.value = false
  ElMessage.success('角色权限已更新（mock）')
}
</script>

<template>
  <section class="role-center stage4-page">
    <WorkbenchHero
      eyebrow="Role Center"
      title="角色权限工作台"
      description="围绕角色定义、权限覆盖、模块分布和当前焦点角色组织主辅工作区，让权限原型既可查看也可直接进入配置动作。"
    >
      <template #meta>
        <el-button type="primary" plain @click="openPermissionDialog()" :disabled="!selectedRole">
          分配权限
        </el-button>
      </template>
    </WorkbenchHero>

    <WorkbenchMetricGrid :items="summaryCards" />

    <div class="role-center__main-grid">
      <div class="role-center__primary">
        <el-card shadow="never" class="page-card">
          <template #header>
            <div class="role-center__card-header">
              <div class="platform-page-heading">
                <span class="platform-page-heading__eyebrow">Roles</span>
                <h3>角色列表</h3>
                <p>用于快速切换当前焦点角色，并从列表进入权限分配动作。</p>
              </div>
              <el-tag effect="plain" type="info">当前 {{ roleList.length }} 个角色</el-tag>
            </div>
          </template>

          <el-table :data="roleList" stripe highlight-current-row @current-change="selectRole" @row-click="selectRole">
            <el-table-column prop="roleName" label="角色名称" width="130" />
            <el-table-column prop="roleCode" label="角色编码" min-width="140" />
            <el-table-column prop="userCount" label="用户数" width="90" />
            <el-table-column prop="updatedAt" label="更新时间" width="160" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openPermissionDialog(row)">分配权限</el-button>
              </template>
            </el-table-column>
            <template #empty>
              <el-empty description="暂无角色数据" :image-size="72" />
            </template>
          </el-table>
        </el-card>

        <el-card shadow="never" class="detail-card">
          <PageSectionHeader
            title="权限树展示"
            description="主工作区展示当前焦点角色的完整权限树，便于校验模块覆盖情况。"
            eyebrow="Permissions"
            compact
          />

          <el-empty v-if="!selectedRole" description="请选择角色" :image-size="80" />
          <el-tree
            v-else
            :key="selectedRole.id"
            class="role-center__permission-tree"
            :data="permissionTreeMock"
            node-key="id"
            default-expand-all
            show-checkbox
            :default-checked-keys="selectedRole.permissions"
            :expand-on-click-node="false"
            disabled
          />
        </el-card>
      </div>

      <div class="role-center__aside">
        <el-card shadow="never" class="detail-card role-center__focus-card">
          <PageSectionHeader
            title="当前角色焦点"
            description="用于汇总角色说明、用户规模和高频权限点，方便作为权限配置前的确认区。"
            eyebrow="Focus"
            compact
          />

          <template v-if="selectedRole">
            <div class="role-center__focus-head">
              <div>
                <h4>{{ selectedRole.roleName }}</h4>
                <p>{{ selectedRole.description }}</p>
              </div>
              <el-tag size="small" effect="plain">{{ selectedRole.roleCode }}</el-tag>
            </div>

            <el-descriptions :column="1" border>
              <el-descriptions-item label="角色编码">{{ selectedRole.roleCode }}</el-descriptions-item>
              <el-descriptions-item label="覆盖用户">{{ selectedRole.userCount }}</el-descriptions-item>
              <el-descriptions-item label="更新时间">{{ selectedRole.updatedAt }}</el-descriptions-item>
              <el-descriptions-item label="权限数量">{{ selectedRole.permissions.length }}</el-descriptions-item>
            </el-descriptions>

            <div class="role-center__action-row">
              <el-button type="primary" @click="openPermissionDialog(selectedRole)">分配权限</el-button>
            </div>

            <div class="role-center__subsection">
              <span class="role-center__subsection-title">高频权限点</span>
              <div class="role-center__tag-group">
                <el-tag v-for="permission in focusedPermissionLabels" :key="permission" class="tag-item">
                  {{ permission }}
                </el-tag>
              </div>
            </div>
          </template>

          <el-empty v-else description="暂无可展示的角色焦点" :image-size="72" />
        </el-card>

        <el-card shadow="never" class="detail-card">
          <PageSectionHeader
            title="模块覆盖观察"
            description="帮助快速判断不同功能模块被多少个角色覆盖。"
            eyebrow="Signals"
            compact
          />
          <div class="role-center__list-block">
            <button
              v-for="item in moduleInsights"
              :key="item.name"
              type="button"
              class="role-center__list-item"
              @click="selectRoleByModule(item.name)"
            >
              <span>{{ item.name }}</span>
              <strong>{{ formatCount(item.enabledRoles) }}</strong>
            </button>
          </div>
        </el-card>

        <el-card shadow="never" class="detail-card">
          <PageSectionHeader
            title="最近更新角色"
            description="便于快速回看最近有权限调整动作的角色。"
            eyebrow="Recent"
            compact
          />
          <div class="role-center__list-block">
            <button
              v-for="item in latestUpdatedRoles"
              :key="item.id"
              type="button"
              class="role-center__list-item role-center__list-item--column"
              @click="selectRole(item)"
            >
              <strong>{{ item.roleName }}</strong>
              <span>{{ item.updatedAt }}</span>
            </button>
          </div>
        </el-card>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" title="分配权限" width="720px">
      <el-alert type="info" :closable="false" show-icon>
        当前角色：{{ selectedRole?.roleName || '--' }}。勾选权限并保存即可完成 mock 分配。
      </el-alert>

      <el-tree
        ref="permissionTreeRef"
        class="dialog-tree"
        :data="permissionTreeMock"
        node-key="id"
        show-checkbox
        default-expand-all
        :default-expanded-keys="treeExpandedKeys"
        :expand-on-click-node="false"
      />

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="savePermissions">保存权限</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.role-center {
  min-width: 0;
}

.role-center__metric-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: var(--platform-card-gap);
}

.role-center__main-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(340px, 0.92fr);
  gap: var(--platform-section-gap);
  align-items: start;
}

.role-center__primary,
.role-center__aside {
  display: flex;
  flex-direction: column;
  gap: var(--platform-section-gap);
}

.role-center__card-header,
.role-center__focus-head {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: 16px;
}

.role-center__focus-head {
  margin-top: 18px;
  margin-bottom: 18px;
}

.role-center__focus-head h4 {
  margin: 0;
  color: var(--platform-text-primary);
  font-size: 18px;
}

.role-center__focus-head p {
  margin: 10px 0 0;
  color: var(--platform-text-secondary);
  line-height: 1.7;
}

.role-center__action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.role-center__subsection {
  margin-top: 18px;
}

.role-center__subsection-title {
  display: inline-flex;
  margin-bottom: 12px;
  color: var(--platform-text-secondary);
  font-size: 12px;
  letter-spacing: 0.06em;
}

.role-center__tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.role-center__list-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 18px;
}

.role-center__list-item {
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

.role-center__list-item--column {
  align-items: flex-start;
  flex-direction: column;
}

.role-center__list-item:hover {
  transform: translateY(-2px);
  border-color: var(--platform-border-strong);
  background: rgba(14, 26, 42, 0.94);
}

.role-center__list-item span {
  color: var(--platform-text-secondary);
  font-size: 12px;
}

.role-center__list-item strong {
  color: var(--platform-text-primary);
  font-size: 14px;
}

.role-center__permission-tree {
  margin-top: 18px;
  border: 1px solid var(--platform-border-subtle);
  border-radius: 18px;
  background: rgba(10, 18, 30, 0.74);
  padding: 16px 18px;
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

.dialog-tree {
  margin-top: 12px;
  max-height: 380px;
  overflow: auto;
  border: 1px solid var(--platform-border-subtle);
  border-radius: 18px;
  padding: 12px 14px;
}

.tag-item {
  border-color: rgba(99, 159, 255, 0.18);
  background: rgba(99, 159, 255, 0.1);
  color: var(--platform-accent-strong);
}

@media (max-width: 1800px) {
  .role-center__metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .role-center__main-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .role-center__metric-grid {
    grid-template-columns: 1fr;
  }

  .role-center__card-header,
  .role-center__focus-head {
    flex-direction: column;
  }
}
</style>
