<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const moduleTitle = computed(() => String(route.meta.title ?? '业务模块'))
const moduleDescription = computed(() =>
  String(route.meta.description ?? '当前模块正在按阶段建设中，本阶段仅保留统一入口与导航。'),
)
</script>

<template>
  <section class="module-page">
    <el-card shadow="never" class="module-page__card">
      <template #header>
        <div class="module-page__header">
          <h2>{{ moduleTitle }}</h2>
          <el-tag type="info">阶段建设中</el-tag>
        </div>
      </template>

      <p class="module-page__description">{{ moduleDescription }}</p>

      <el-row :gutter="16" class="module-page__kpi">
        <el-col :xs="24" :md="8">
          <div class="placeholder-kpi">
            <span class="placeholder-kpi__label">已规划能力项</span>
            <strong>12</strong>
          </div>
        </el-col>
        <el-col :xs="24" :md="8">
          <div class="placeholder-kpi">
            <span class="placeholder-kpi__label">本阶段实现率</span>
            <strong>20%</strong>
          </div>
        </el-col>
        <el-col :xs="24" :md="8">
          <div class="placeholder-kpi">
            <span class="placeholder-kpi__label">下阶段计划任务</span>
            <strong>8</strong>
          </div>
        </el-col>
      </el-row>

      <el-alert type="info" show-icon :closable="false" class="module-page__alert">
        当前阶段已完成统一布局和入口导航，后续将按模块逐步接入业务页面与交互。
      </el-alert>
    </el-card>
  </section>
</template>

<style scoped>
.module-page__card {
  border: 1px solid var(--platform-border-subtle);
}

.module-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.module-page__header h2 {
  margin: 0;
  color: var(--platform-text-primary);
  font-size: 20px;
}

.module-page__description {
  margin: 4px 0 16px;
  color: var(--platform-text-secondary);
  line-height: 1.8;
}

.module-page__kpi {
  margin-bottom: 16px;
}

.placeholder-kpi {
  display: flex;
  min-height: 94px;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
  border: 1px solid var(--platform-border-subtle);
  border-radius: 10px;
  background: #f8fbff;
  padding: 14px;
}

.placeholder-kpi__label {
  color: var(--platform-text-tertiary);
  font-size: 12px;
}

.placeholder-kpi strong {
  color: var(--platform-text-primary);
  font-size: 28px;
}

.module-page__alert {
  border: 1px solid #cae3ff;
}
</style>
