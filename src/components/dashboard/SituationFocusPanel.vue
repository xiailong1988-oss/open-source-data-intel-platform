<script setup lang="ts">
import type {
  DashboardCockpitLink,
  DashboardCockpitPoint,
  DashboardCockpitTopicSwitch,
  DashboardCockpitZone,
} from '../../types/dashboardCockpit'

defineProps<{
  displayPoint: DashboardCockpitPoint | null
  displayZone: DashboardCockpitZone | null
  regionName: string
  regionEmphasis: string
  focusSummary: string
  focusLinks: DashboardCockpitLink[]
  currentTopics: DashboardCockpitTopicSwitch[]
}>()

defineEmits<{
  (event: 'open-link', link: DashboardCockpitLink): void
  (event: 'topic-click', keyword: string): void
}>()
</script>

<template>
  <section class="focus-panel">
    <header class="focus-panel__head">
      <div>
        <strong>当前焦点详情</strong>
        <small>{{ regionName }} · {{ regionEmphasis }}</small>
      </div>
    </header>

    <div class="focus-panel__hero">
      <span>{{ displayPoint?.layer ?? '区域焦点' }}</span>
      <strong>{{ displayPoint?.title ?? displayZone?.name ?? regionName }}</strong>
      <div class="focus-panel__meta">
        <span v-if="displayPoint">{{ displayPoint.area }}</span>
        <span v-if="displayPoint">{{ displayPoint.occurredAt }}</span>
        <span v-if="displayPoint">{{ displayPoint.status }}</span>
        <span v-else-if="displayZone">热度 {{ displayZone.heat }}</span>
        <span v-else>值守总览</span>
      </div>
      <p>{{ focusSummary }}</p>
    </div>

    <div class="focus-panel__links">
      <button v-for="link in focusLinks" :key="link.id" type="button" @click="$emit('open-link', link)">
        {{ link.title }}
      </button>
    </div>

    <div class="focus-panel__topics">
      <header>
        <strong>专题联动</strong>
      </header>

      <button
        v-for="topic in currentTopics"
        :key="topic.id"
        type="button"
        class="focus-panel__topic"
        @click="$emit('topic-click', topic.keyword)"
      >
        <strong>{{ topic.title }}</strong>
        <small>{{ topic.description }}</small>
      </button>
    </div>
  </section>
</template>

<style scoped>
.focus-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
}

.focus-panel__head strong,
.focus-panel__hero strong,
.focus-panel__topics strong {
  color: #eff6ff;
}

.focus-panel__head small,
.focus-panel__hero span,
.focus-panel__hero p,
.focus-panel__meta,
.focus-panel__topic small {
  color: rgba(206, 221, 240, 0.74);
}

.focus-panel__head strong {
  font-size: 18px;
}

.focus-panel__head small,
.focus-panel__hero span,
.focus-panel__meta,
.focus-panel__topic small {
  font-size: 12px;
}

.focus-panel__hero {
  display: flex;
  flex-direction: column;
  gap: 8px;
  border: 1px solid rgba(118, 171, 246, 0.08);
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(8, 16, 27, 0.78) 0%, rgba(8, 15, 25, 0.58) 100%);
  padding: 14px;
}

.focus-panel__hero strong {
  font-size: 22px;
  line-height: 1.4;
}

.focus-panel__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.focus-panel__hero p {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
}

.focus-panel__links {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.focus-panel__links button,
.focus-panel__topic {
  border: 1px solid rgba(118, 171, 246, 0.1);
  border-radius: 16px;
  background: rgba(8, 16, 27, 0.48);
  color: rgba(224, 234, 245, 0.82);
  cursor: pointer;
  transition: border-color 0.22s ease, background 0.22s ease, transform 0.22s ease;
}

.focus-panel__links button {
  min-height: 28px;
  padding: 0 12px;
  font-size: 12px;
}

.focus-panel__links button:hover,
.focus-panel__topic:hover {
  transform: translateY(-1px);
  border-color: rgba(123, 191, 255, 0.32);
  background: rgba(18, 34, 52, 0.82);
}

.focus-panel__topics {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 0;
}

.focus-panel__topic {
  display: flex;
  flex-direction: column;
  gap: 5px;
  padding: 12px;
  text-align: left;
}

.focus-panel__topic strong {
  font-size: 14px;
}
</style>
