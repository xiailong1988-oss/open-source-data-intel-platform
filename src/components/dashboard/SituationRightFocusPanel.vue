<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { ArrowDown, ArrowUp, VideoPause, VideoPlay } from '@element-plus/icons-vue'
import type {
  DashboardCockpitLayer,
  DashboardCockpitLink,
  DashboardCockpitPoint,
  DashboardCockpitTickerItem,
  DashboardCockpitTopicSwitch,
  DashboardCockpitZone,
} from '../../types/dashboardCockpit'

const STREAM_ROW_HEIGHT = 104
const STREAM_VISIBLE_ROWS = 5

/**
 * 右侧 rail 继续压缩为更单一的 signal layer：
 * 上半部是持续滚动的信息流，下半部是焦点解释和极少量动作。
 */
const props = defineProps<{
  activeLayer: DashboardCockpitLayer
  displayPoint: DashboardCockpitPoint | null
  displayZone: DashboardCockpitZone | null
  focusSummary: string
  focusMeta: string[]
  focusTag: string
  currentTopics: DashboardCockpitTopicSwitch[]
  guideSummary: string
  guideInstruction: string
  focusLinks: DashboardCockpitLink[]
  zonePoints: DashboardCockpitPoint[]
  tickerItems: DashboardCockpitTickerItem[]
  activeTickerIndex: number
  isPlaybackPaused: boolean
}>()

const emit = defineEmits<{
  (event: 'open-detail'): void
  (event: 'open-search'): void
  (event: 'open-link', link: DashboardCockpitLink): void
  (event: 'topic-click', keyword: string): void
  (event: 'focus-point', pointId: string): void
  (event: 'select-ticker', index: number): void
  (event: 'previous'): void
  (event: 'next'): void
  (event: 'toggle-playback'): void
  (event: 'stream-hover-start'): void
  (event: 'stream-hover-end'): void
}>()

const streamVisualIndex = ref(0)
const streamAnimate = ref(true)
let streamResetTimer: number | null = null

const streamRenderItems = computed(() => {
  if (!props.tickerItems.length) return []
  return props.tickerItems.concat(props.tickerItems.slice(0, STREAM_VISIBLE_ROWS))
})

const railActions = computed(() => {
  if (props.focusLinks.length) {
    return props.focusLinks.slice(0, 2).map((item) => ({
      id: item.id,
      label: item.title,
      kind: 'link' as const,
      link: item,
    }))
  }

  if (props.zonePoints.length) {
    return props.zonePoints.slice(0, 2).map((item) => ({
      id: item.id,
      label: item.title,
      kind: 'point' as const,
      pointId: item.id,
    }))
  }

  return props.currentTopics.slice(0, 2).map((item) => ({
    id: item.id,
    label: item.title,
    kind: 'topic' as const,
    keyword: item.keyword,
  }))
})

const streamViewportHeight = computed(() => STREAM_ROW_HEIGHT * STREAM_VISIBLE_ROWS)
const streamTrackStyle = computed(() => ({
  transform: `translateY(-${streamVisualIndex.value * STREAM_ROW_HEIGHT}px)`,
  transition: streamAnimate.value ? 'transform 640ms linear' : 'none',
}))

const syncStreamVisualIndex = async (nextIndex: number, prevIndex: number) => {
  const length = props.tickerItems.length

  if (streamResetTimer !== null) {
    window.clearTimeout(streamResetTimer)
    streamResetTimer = null
  }

  if (!length || length === 1) {
    streamAnimate.value = false
    streamVisualIndex.value = 0
    return
  }

  if (nextIndex === 0 && prevIndex === length - 1) {
    streamAnimate.value = true
    streamVisualIndex.value = length
    streamResetTimer = window.setTimeout(async () => {
      streamAnimate.value = false
      streamVisualIndex.value = 0
      await nextTick()
      streamAnimate.value = true
    }, 660)
    return
  }

  streamAnimate.value = true
  streamVisualIndex.value = nextIndex
}

watch(
  () => props.activeTickerIndex,
  (nextIndex, prevIndex) => {
    void syncStreamVisualIndex(nextIndex, prevIndex ?? 0)
  },
  { immediate: true },
)

watch(
  () => props.tickerItems.map((item) => item.id).join(','),
  async () => {
    if (streamResetTimer !== null) {
      window.clearTimeout(streamResetTimer)
      streamResetTimer = null
    }
    streamAnimate.value = false
    streamVisualIndex.value = props.activeTickerIndex
    await nextTick()
    streamAnimate.value = true
  },
)

onBeforeUnmount(() => {
  if (streamResetTimer !== null) {
    window.clearTimeout(streamResetTimer)
  }
})
</script>

<template>
  <aside
    class="situation-rail"
    @mouseenter="emit('stream-hover-start')"
    @mouseleave="emit('stream-hover-end')"
  >
    <div class="situation-rail__header">
      <div>
        <span class="situation-rail__eyebrow">Signal Guide</span>
        <strong>{{ activeLayer }} / Mixed Intel</strong>
      </div>

      <div class="situation-rail__controls">
        <el-button circle size="small" :disabled="!tickerItems.length" @click="emit('previous')">
          <el-icon><ArrowUp /></el-icon>
        </el-button>
        <el-button circle size="small" :disabled="tickerItems.length <= 1" @click="emit('toggle-playback')">
          <el-icon><VideoPlay v-if="isPlaybackPaused" /><VideoPause v-else /></el-icon>
        </el-button>
        <el-button circle size="small" :disabled="!tickerItems.length" @click="emit('next')">
          <el-icon><ArrowDown /></el-icon>
        </el-button>
      </div>
    </div>

    <div class="situation-rail__meta-line">
      <span>{{ tickerItems.length }} signals</span>
      <span>{{ isPlaybackPaused ? 'paused' : 'active' }}</span>
      <span>{{ displayPoint ? displayPoint.area : displayZone ? displayZone.name : 'haidian' }}</span>
    </div>

    <div v-if="tickerItems.length" class="situation-stream">
      <div class="situation-stream__viewport" :style="{ height: `${streamViewportHeight}px` }">
        <div class="situation-stream__track" :style="streamTrackStyle">
          <button
            v-for="(item, index) in streamRenderItems"
            :key="`${item.id}-${index}`"
            type="button"
            class="situation-stream__item"
            :class="{ 'is-active': index % tickerItems.length === activeTickerIndex }"
            @click="emit('select-ticker', index % tickerItems.length)"
          >
            <div class="situation-stream__meta">
              <span class="situation-stream__layer">{{ item.layer }}</span>
              <span>{{ item.time }}</span>
              <span>{{ item.area }}</span>
              <span>{{ item.status }}</span>
            </div>
            <strong>{{ item.title }}</strong>
            <small>{{ item.summary }}</small>
          </button>
        </div>
      </div>
    </div>

    <div v-else class="situation-rail__empty">
      <strong>当前暂无可滚动情报</strong>
      <small>请切换图层或等待数据刷新。</small>
    </div>

    <section class="situation-rail__focus">
      <div class="situation-rail__focus-head">
        <span class="situation-rail__eyebrow">Focus Detail</span>
        <el-tag size="small" :type="focusTag">{{ displayPoint ? displayPoint.riskLevel : activeLayer }}</el-tag>
      </div>

      <strong class="situation-rail__focus-title">
        {{ displayPoint ? displayPoint.title : displayZone ? displayZone.name : '海淀区当前焦点' }}
      </strong>

      <div class="situation-rail__focus-meta">
        <span v-for="item in focusMeta" :key="item">{{ item }}</span>
      </div>

      <p class="situation-rail__focus-summary">{{ focusSummary }}</p>

      <div class="situation-rail__guide">
        <span class="situation-rail__eyebrow">Guide</span>
        <p>{{ guideInstruction }}</p>
        <small>{{ guideSummary }}</small>
      </div>

      <div class="situation-rail__actions">
        <el-button type="primary" @click="emit('open-detail')">查看详情</el-button>
        <el-button @click="emit('open-search')">进入检索</el-button>
      </div>

      <div v-if="railActions.length" class="situation-rail__quick-list">
        <button
          v-for="item in railActions"
          :key="item.id"
          type="button"
          class="situation-rail__quick-item"
          @click="
            item.kind === 'link'
              ? emit('open-link', item.link)
              : item.kind === 'point'
                ? emit('focus-point', item.pointId)
                : emit('topic-click', item.keyword)
          "
        >
          {{ item.label }}
        </button>
      </div>
    </section>
  </aside>
</template>

<style scoped>
.situation-rail {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 12px;
  height: 100%;
  border-left: 1px solid rgba(119, 167, 236, 0.14);
  padding: 2px 0 4px 16px;
}

.situation-rail__header,
.situation-rail__focus-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.situation-rail__eyebrow {
  color: #8ebdff;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.situation-rail__header strong,
.situation-rail__focus-title,
.situation-stream__item strong,
.situation-rail__empty strong {
  color: #f0f6ff;
}

.situation-rail__header strong {
  font-size: 15px;
  letter-spacing: 0.06em;
}

.situation-rail__controls {
  display: flex;
  gap: 8px;
}

.situation-rail__meta-line {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  color: rgba(191, 210, 232, 0.76);
  font-size: 10px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.situation-rail__meta-line span {
  display: inline-flex;
  align-items: center;
  border: 1px solid rgba(116, 170, 240, 0.08);
  border-radius: 4px;
  padding: 4px 7px;
  background: rgba(7, 15, 27, 0.24);
}

.situation-stream__viewport {
  overflow: hidden;
  border-top: 1px solid rgba(116, 170, 240, 0.12);
  border-bottom: 1px solid rgba(116, 170, 240, 0.12);
  background: linear-gradient(180deg, rgba(7, 15, 27, 0.48) 0%, rgba(7, 15, 27, 0.22) 100%);
}

.situation-stream__track {
  display: flex;
  flex-direction: column;
}

.situation-stream__item {
  display: flex;
  min-height: 104px;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
  border: 0;
  border-bottom: 1px solid rgba(116, 170, 240, 0.08);
  padding: 12px 12px 11px 2px;
  background: transparent;
  text-align: left;
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease, padding-left 0.18s ease;
}

.situation-stream__item:hover,
.situation-stream__item.is-active {
  background: rgba(21, 41, 66, 0.52);
  border-color: rgba(116, 170, 240, 0.18);
  padding-left: 8px;
}

.situation-stream__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 10px;
  color: #8ebdff;
  font-size: 10px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.situation-stream__layer {
  color: #ffd787;
  font-weight: 700;
}

.situation-stream__item small,
.situation-rail__focus-summary,
.situation-rail__guide p,
.situation-rail__guide small,
.situation-rail__empty small {
  color: rgba(215, 227, 242, 0.72);
  line-height: 1.6;
}

.situation-rail__focus {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-top: 6px;
}

.situation-rail__focus-title {
  font-size: 17px;
  line-height: 1.45;
}

.situation-rail__focus-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 10px;
  color: rgba(198, 216, 238, 0.68);
  font-size: 12px;
}

.situation-rail__focus-summary {
  margin: 0;
}

.situation-rail__guide {
  display: flex;
  flex-direction: column;
  gap: 4px;
  border-top: 1px solid rgba(116, 170, 240, 0.1);
  padding-top: 10px;
}

.situation-rail__guide p,
.situation-rail__guide small {
  margin: 0;
}

.situation-rail__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.situation-rail__quick-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.situation-rail__quick-item {
  border: 1px solid rgba(116, 170, 240, 0.1);
  border-radius: 4px;
  padding: 8px 10px;
  background: rgba(7, 15, 27, 0.22);
  color: rgba(230, 239, 250, 0.9);
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.situation-rail__quick-item:hover {
  border-color: rgba(116, 170, 240, 0.24);
  background: rgba(18, 33, 52, 0.58);
}

.situation-rail__empty {
  display: flex;
  flex-direction: column;
  gap: 4px;
  border: 1px dashed rgba(116, 170, 240, 0.12);
  padding: 14px 12px;
  background: rgba(7, 15, 27, 0.18);
}

@media (max-width: 1560px) {
  .situation-rail {
    border-left: 0;
    border-top: 1px solid rgba(119, 167, 236, 0.12);
    padding: 16px 0 0;
  }
}
</style>
