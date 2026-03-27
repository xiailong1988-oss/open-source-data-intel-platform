<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageSectionHeader from '../../components/common/PageSectionHeader.vue'
import WorkbenchHero from '../../components/common/WorkbenchHero.vue'
import WorkbenchMetricGrid from '../../components/common/WorkbenchMetricGrid.vue'
import { systemConfigMock } from '../../mock/systemManagement'
import type { PlatformTheme, SystemConfigModel } from '../../types/systemManagement'

const formModel = reactive<SystemConfigModel>({ ...systemConfigMock })
const saving = ref(false)

const themeOptions: PlatformTheme[] = ['政务蓝', '深海蓝', '银灰白']
const themeDescriptionMap: Record<PlatformTheme, string> = {
  政务蓝: '适合指挥、治理和政企风格的默认工作台主题。',
  深海蓝: '更强调控制台和监测场景的深色层次。',
  银灰白: '适合轻量演示和打印导出的中性主题。',
}

const metricCards = computed(() => [
  {
    id: 'platform',
    label: '平台名称',
    value: formModel.platformName,
    hint: '当前系统对外展示的工作台名称',
    clickable: false,
  },
  {
    id: 'theme',
    label: '默认主题',
    value: formModel.defaultTheme,
    hint: themeDescriptionMap[formModel.defaultTheme],
    clickable: false,
  },
  {
    id: 'retention',
    label: '数据保留周期',
    value: `${formModel.dataRetentionDays} 天`,
    hint: '用于约束原始数据和治理数据的保留窗口',
    clickable: false,
  },
  {
    id: 'warning',
    label: '预警阈值',
    value: `${formModel.warningThreshold}`,
    hint: '用于表达当前默认预警敏感度',
    clickable: false,
  },
])

const thresholdLevel = computed(() => {
  if (formModel.warningThreshold >= 85) {
    return '高敏感'
  }

  if (formModel.warningThreshold >= 65) {
    return '均衡'
  }

  return '宽松'
})

const configPreviewItems = computed(() => [
  { label: '平台名称', value: formModel.platformName },
  { label: '默认主题', value: formModel.defaultTheme },
  { label: '数据保留', value: `${formModel.dataRetentionDays} 天` },
  { label: '预警阈值', value: `${formModel.warningThreshold}（${thresholdLevel.value}）` },
  { label: '日志保留', value: `${formModel.logRetentionDays} 天` },
  { label: '通知邮箱', value: formModel.notificationEmail },
])

const governanceHints = computed(() => [
  `模型占位：${formModel.modelPlaceholder}`,
  `检索参数：${formModel.retrievalPlaceholder}`,
  `日志保留：${formModel.logRetentionDays} 天`,
])

/**
 * 系统配置页当前仍是纯前端 mock。
 * 恢复默认时必须回到同一份基线配置，方便后续接入真实接口前保持页面演示一致性。
 */
const resetConfig = () => {
  Object.assign(formModel, systemConfigMock)
  ElMessage.success('配置已恢复为默认 mock 值')
}

const saveConfig = async () => {
  saving.value = true
  await new Promise((resolve) => setTimeout(resolve, 700))
  saving.value = false
  ElMessage.success('系统配置已保存（mock）')
}
</script>

<template>
  <section class="system-config-center stage4-page">
    <WorkbenchHero
      eyebrow="Config Center"
      title="系统配置工作台"
      description="统一承接平台名称、主题、数据保留、检索参数和预警阈值等配置项，让系统管理具备更清晰的主辅层级和配置预览能力。"
    >
      <template #meta>
        <el-tag effect="plain">当前为前端 mock 配置</el-tag>
        <el-tag effect="plain" type="warning">支持恢复默认</el-tag>
      </template>
    </WorkbenchHero>

    <WorkbenchMetricGrid :items="metricCards" />

    <div class="system-config-center__main-grid">
      <div class="system-config-center__primary">
        <el-card shadow="never" class="page-card">
          <template #header>
            <div class="system-config-center__card-header">
              <div class="platform-page-heading">
                <span class="platform-page-heading__eyebrow">Form</span>
                <h3>配置表单区</h3>
                <p>围绕平台基础信息、检索参数和预警阈值提供统一配置入口，保持与后续真实接口的字段组织一致。</p>
              </div>
              <div class="system-config-center__header-actions">
                <el-button @click="resetConfig">恢复默认</el-button>
                <el-button type="primary" :loading="saving" @click="saveConfig">保存配置</el-button>
              </div>
            </div>
          </template>

          <el-form :model="formModel" label-width="132px" class="config-form">
            <el-form-item label="平台名称">
              <el-input v-model="formModel.platformName" />
            </el-form-item>

            <el-form-item label="默认主题">
              <el-select v-model="formModel.defaultTheme" style="width: 220px">
                <el-option v-for="item in themeOptions" :key="item" :label="item" :value="item" />
              </el-select>
              <span class="form-tip">{{ themeDescriptionMap[formModel.defaultTheme] }}</span>
            </el-form-item>

            <el-form-item label="数据保留周期">
              <el-input-number v-model="formModel.dataRetentionDays" :min="30" :max="3650" />
              <span class="form-suffix">天</span>
            </el-form-item>

            <el-form-item label="模型配置占位">
              <el-input v-model="formModel.modelPlaceholder" />
            </el-form-item>

            <el-form-item label="检索参数占位">
              <el-input v-model="formModel.retrievalPlaceholder" type="textarea" :rows="2" />
            </el-form-item>

            <el-form-item label="预警默认阈值">
              <el-slider v-model="formModel.warningThreshold" :min="10" :max="100" show-input />
              <span class="form-tip">当前阈值等级：{{ thresholdLevel }}</span>
            </el-form-item>

            <el-form-item label="日志保留周期">
              <el-input-number v-model="formModel.logRetentionDays" :min="7" :max="3650" />
              <span class="form-suffix">天</span>
            </el-form-item>

            <el-form-item label="通知邮箱">
              <el-input v-model="formModel.notificationEmail" />
            </el-form-item>

            <el-form-item>
              <el-button @click="resetConfig">恢复默认</el-button>
              <el-button type="primary" :loading="saving" @click="saveConfig">保存配置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>

      <div class="system-config-center__aside">
        <el-card shadow="never" class="detail-card preview-card">
          <PageSectionHeader
            title="配置预览"
            description="右侧实时展示当前配置摘要，方便确认保存前的系统状态。"
            eyebrow="Preview"
            compact
          />
          <el-descriptions :column="1" border class="system-config-center__desc">
            <el-descriptions-item v-for="item in configPreviewItems" :key="item.label" :label="item.label">
              {{ item.value }}
            </el-descriptions-item>
          </el-descriptions>
          <el-alert class="preview-tip" type="info" :closable="false" show-icon>
            当前页面为系统配置原型，保存动作为前端 mock，不连接真实后端。
          </el-alert>
        </el-card>

        <el-card shadow="never" class="detail-card">
          <PageSectionHeader
            title="当前配置侧记"
            description="把本轮配置里最关键的参数抽出来，便于做演示讲解和后续接口映射。"
            eyebrow="Notes"
            compact
          />
          <div class="system-config-center__list-block">
            <div v-for="item in governanceHints" :key="item" class="system-config-center__list-item system-config-center__list-item--static">
              <strong>{{ item }}</strong>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="detail-card">
          <PageSectionHeader
            title="配置落地建议"
            description="帮助后续把 mock 配置逐步迁移到真实后端与权限控制。"
            eyebrow="Guide"
            compact
          />
          <div class="system-config-center__list-block">
            <div class="system-config-center__list-item system-config-center__list-item--static">
              <strong>后端接入顺序</strong>
              <span>平台基础配置 -> 检索参数 -> 预警阈值 -> 审计日志</span>
            </div>
            <div class="system-config-center__list-item system-config-center__list-item--static">
              <strong>权限控制建议</strong>
              <span>仅平台管理员与运维负责人开放保存权限。</span>
            </div>
            <div class="system-config-center__list-item system-config-center__list-item--static">
              <strong>配置校验建议</strong>
              <span>接后端后优先补邮箱校验、阈值区间约束和修改审计。</span>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </section>
</template>

<style scoped>
.system-config-center {
  min-width: 0;
}

.system-config-center__metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--platform-card-gap);
}

.system-config-center__main-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(340px, 0.95fr);
  gap: var(--platform-section-gap);
  align-items: start;
}

.system-config-center__aside {
  display: flex;
  flex-direction: column;
  gap: var(--platform-section-gap);
}

.system-config-center__card-header {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: 16px;
}

.system-config-center__header-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.system-config-center__desc {
  margin-top: 18px;
}

.system-config-center__list-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 18px;
}

.system-config-center__list-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  border: 1px solid var(--platform-border-subtle);
  border-radius: 16px;
  background: rgba(11, 21, 34, 0.82);
  padding: 14px 16px;
}

.system-config-center__list-item strong {
  color: var(--platform-text-primary);
  font-size: 14px;
}

.system-config-center__list-item span {
  color: var(--platform-text-secondary);
  font-size: 12px;
  line-height: 1.7;
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
  line-height: 1.2;
}

.metric-card__value--small {
  font-size: 22px;
}

.metric-card__change {
  display: inline-block;
  margin-top: 10px;
  color: var(--platform-text-tertiary);
  font-size: 12px;
  line-height: 1.7;
}

.detail-card {
  border: 1px solid var(--platform-border-subtle);
}

.form-suffix,
.form-tip {
  margin-left: 8px;
  color: var(--platform-text-tertiary);
  font-size: 12px;
}

.preview-tip {
  margin-top: 12px;
}

@media (max-width: 1800px) {
  .system-config-center__metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .system-config-center__main-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .system-config-center__metric-grid {
    grid-template-columns: 1fr;
  }

  .system-config-center__card-header {
    flex-direction: column;
  }
}
</style>
