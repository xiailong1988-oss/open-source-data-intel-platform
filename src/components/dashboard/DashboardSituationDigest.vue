<script setup lang="ts">
import PageSectionHeader from '../common/PageSectionHeader.vue'

interface DashboardDigestSummaryItem {
  id: string
  label: string
  value: string
  hint: string
}

interface DashboardDigestAgendaItem {
  id: string
  title: string
  meta: string
  actionLabel: string
}

/**
 * 首页态势摘要组件专门承接“值守摘要 / 今日主线 / 重点区域”三类信息，
 * 让首页第一屏在地图之外还有一块稳定的高价值决策摘要区。
 */
defineProps<{
  summaryItems: DashboardDigestSummaryItem[]
  agendaItems: DashboardDigestAgendaItem[]
  keyRegions: string[]
}>()

defineEmits<{
  (event: 'summary-click', id: string): void
  (event: 'agenda-click', id: string): void
  (event: 'region-click', region: string): void
}>()
</script>

<template>
  <el-card shadow="never" class="digest-card">
    <PageSectionHeader
      title="态势摘要"
      description="围绕值守摘要、今日主线和重点区域，强化首页作为总入口的指挥感。"
      eyebrow="Digest"
      compact
    />

    <div class="digest-card__summary-grid">
      <button
        v-for="item in summaryItems"
        :key="item.id"
        type="button"
        class="digest-card__summary-item"
        @click="$emit('summary-click', item.id)"
      >
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.hint }}</small>
      </button>
    </div>

    <div class="digest-card__section">
      <div class="digest-card__section-head">
        <strong>今日主线</strong>
        <span>从首页直接进入当前最值得跟进的对象</span>
      </div>
      <div class="digest-card__agenda-list">
        <button
          v-for="item in agendaItems"
          :key="item.id"
          type="button"
          class="digest-card__agenda-item"
          @click="$emit('agenda-click', item.id)"
        >
          <div>
            <strong>{{ item.title }}</strong>
            <span>{{ item.meta }}</span>
          </div>
          <em>{{ item.actionLabel }}</em>
        </button>
      </div>
    </div>

    <div class="digest-card__section">
      <div class="digest-card__section-head">
        <strong>重点区域</strong>
        <span>与地图总览保持同一批重点区域语义</span>
      </div>
      <div class="digest-card__region-list">
        <button
          v-for="item in keyRegions"
          :key="item"
          type="button"
          class="digest-card__region-item"
          @click="$emit('region-click', item)"
        >
          {{ item }}
        </button>
      </div>
    </div>
  </el-card>
</template>

<style scoped>
.digest-card {
  height: 100%;
  border: 1px solid var(--platform-border-subtle);
}

.digest-card__summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.digest-card__summary-item,
.digest-card__agenda-item,
.digest-card__region-item {
  width: 100%;
  border: 1px solid var(--platform-border-subtle);
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(18, 31, 49, 0.88) 0%, rgba(10, 20, 31, 0.88) 100%);
  color: var(--platform-text-primary);
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.digest-card__summary-item:hover,
.digest-card__agenda-item:hover,
.digest-card__region-item:hover {
  transform: translateY(-2px);
  border-color: var(--platform-border-strong);
  box-shadow: 0 18px 38px rgba(2, 10, 20, 0.32);
}

.digest-card__summary-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px 16px;
  text-align: left;
}

.digest-card__summary-item span,
.digest-card__agenda-item span,
.digest-card__section-head span {
  color: var(--platform-text-tertiary);
  font-size: 12px;
}

.digest-card__summary-item strong {
  color: var(--platform-text-primary);
  font-size: 24px;
  line-height: 1.1;
}

.digest-card__summary-item small {
  color: var(--platform-text-secondary);
  font-size: 12px;
  line-height: 1.6;
}

.digest-card__section {
  margin-top: 18px;
}

.digest-card__section-head {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.digest-card__section-head strong {
  color: var(--platform-text-primary);
  font-size: 14px;
}

.digest-card__agenda-list,
.digest-card__region-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 12px;
}

.digest-card__agenda-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  text-align: left;
}

.digest-card__agenda-item strong {
  display: block;
  color: var(--platform-text-primary);
  font-size: 14px;
}

.digest-card__agenda-item em {
  color: var(--platform-accent-strong);
  font-size: 12px;
  font-style: normal;
  white-space: nowrap;
}

.digest-card__region-list {
  flex-direction: row;
  flex-wrap: wrap;
}

.digest-card__region-item {
  width: auto;
  padding: 10px 14px;
  font-size: 13px;
}

@media (max-width: 900px) {
  .digest-card__summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
