<script setup lang="ts">
import { computed, type Component } from 'vue'
import { useRouter } from 'vue-router'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import PageSectionHeader from '../common/PageSectionHeader.vue'
import ViewStatePanel from '../common/ViewStatePanel.vue'
import type { DashboardQuickLink, DashboardRecentFocus } from '../../types/dashboard'

const props = defineProps<{
  quickLinks: DashboardQuickLink[]
  recentFocuses: DashboardRecentFocus[]
}>()

const router = useRouter()
const iconMap = ElementPlusIconsVue as Record<string, Component>
const hasQuickLinks = computed(() => props.quickLinks.length > 0)
const hasRecentFocuses = computed(() => props.recentFocuses.length > 0)

/**
 * 快捷入口图标统一从 Element Plus 图标集中解析，
 * 避免 mock 配置和组件实现之间出现强耦合。
 */
const resolveIcon = (icon: string) => iconMap[icon] ?? iconMap.DataAnalysis

/**
 * 工作台快捷入口和最近关注都走同一套路由跳转封装，
 * 便于后续补 query 上下文和埋点时集中扩展。
 */
const goToLink = (path: string, query?: Record<string, string>) => {
  router.push({ path, query })
}

/**
 * 快捷入口优先承担“从首页进入核心工作页”的职责。
 */
const openQuickLink = (item: DashboardQuickLink) => {
  goToLink(item.path, item.query)
}

/**
 * 最近关注用于承接最近报告、预警和检索入口的继续追踪。
 */
const openRecentFocus = (item: DashboardRecentFocus) => {
  goToLink(item.path, item.query)
}
</script>

<template>
  <section class="workbench">
    <el-card shadow="never" class="workbench-card">
      <div class="workbench-card__grid">
        <div class="workbench-card__left">
          <PageSectionHeader
            title="工作台快捷入口"
            description="保留高频能力入口，便于从综合态势页直接进入重点模块。"
            compact
          />

          <ViewStatePanel
            v-if="!hasQuickLinks"
            state="empty"
            compact
            title="暂无快捷入口"
            description="当前没有配置快捷入口，可在后续专题和能力扩展时补充。"
          />
          <div v-else class="quick-grid">
            <button
              v-for="item in props.quickLinks"
              :key="item.id"
              type="button"
              class="quick-item"
              @click="openQuickLink(item)"
            >
              <span class="quick-item__icon">
                <el-icon :size="18">
                  <component :is="resolveIcon(item.icon)" />
                </el-icon>
              </span>
              <div class="quick-item__content">
                <strong>{{ item.title }}</strong>
                <span>{{ item.description }}</span>
              </div>
            </button>
          </div>
        </div>

        <div class="workbench-card__right">
          <PageSectionHeader title="最近关注" description="最近查看的报告、预警和专题入口，可继续追踪。" compact />

          <ViewStatePanel
            v-if="!hasRecentFocuses"
            state="empty"
            compact
            title="暂无最近关注"
            description="后续可在报告、专题和告警联动里自动沉淀最近关注对象。"
          />
          <div v-else class="focus-list">
            <button
              v-for="item in props.recentFocuses"
              :key="item.id"
              type="button"
              class="focus-item"
              @click="openRecentFocus(item)"
            >
              <strong>{{ item.title }}</strong>
              <span>{{ item.meta }}</span>
            </button>
          </div>
        </div>
      </div>
    </el-card>
  </section>
</template>

<style scoped>
.workbench-card {
  border: 1px solid var(--platform-border-subtle);
}

.workbench-card__grid {
  display: grid;
  grid-template-columns: minmax(0, 1.7fr) minmax(320px, 1fr);
  gap: var(--platform-card-gap);
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 16px;
}

.quick-item,
.focus-item {
  border: 1px solid var(--platform-border-subtle);
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(18, 31, 49, 0.9) 0%, rgba(11, 20, 31, 0.9) 100%);
  padding: 16px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.quick-item:hover,
.focus-item:hover {
  transform: translateY(-2px);
  border-color: var(--platform-border-strong);
  box-shadow: 0 18px 38px rgba(2, 10, 19, 0.34);
}

.quick-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.quick-item__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(100, 169, 255, 0.18) 0%, rgba(59, 210, 199, 0.14) 100%);
  color: var(--platform-accent-strong);
}

.quick-item__content strong,
.focus-item strong {
  display: block;
  color: var(--platform-text-primary);
  font-size: 14px;
}

.quick-item__content span,
.focus-item span {
  display: block;
  margin-top: 6px;
  color: var(--platform-text-tertiary);
  font-size: 12px;
  line-height: 1.6;
}

.focus-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 14px;
}

@media (max-width: 1280px) {
  .workbench-card__grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
