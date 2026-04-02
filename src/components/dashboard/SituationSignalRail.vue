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

const STREAM_ROW_HEIGHT = 68
const STREAM_VISIBLE_ROWS = 6

/**
 * 右侧 rail 这轮直接按单一 signal layer 处理。
 * 中部是持续流动的情报轨，底部只保留极轻的焦点解释和动作，不再做业务面板堆叠。
 */
const props = defineProps<{
  activeLayer: DashboardCockpitLayer
  displayPoint: DashboardCockpitPoint | null
  displayZone: DashboardCockpitZone | null
  focusSummary: string
  focusMeta: string[]
  guideInstruction: string
  focusLinks: DashboardCockpitLink[]
  zonePoints: DashboardCockpitPoint[]
  currentTopics: DashboardCockpitTopicSwitch[]
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

const focusTitle = computed(() => {
  if (props.displayPoint) return props.displayPoint.title
  if (props.displayZone) return props.displayZone.name
  return '海淀区当前焦点'
})

const focusCaption = computed(() => {
  if (props.displayPoint) return `${props.displayPoint.layer} / ${props.displayPoint.area}`
  if (props.displayZone) return `重点片区 / ${props.displayZone.name}`
  return `${props.activeLayer} / 海淀区`
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

const streamTrackStyle = computed(() => ({
  transform: `translateY(-${streamVisualIndex.value * STREAM_ROW_HEIGHT}px)`,
  transition: streamAnimate.value ? 'transform 560ms linear' : 'none',
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
    }, 580)
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
    class="situation-signal-rail"
    v-motion
    :initial="{ opacity: 0, x: 18 }"
    :enter="{ opacity: 1, x: 0, transition: { delay: 160, duration: 620 } }"
    @mouseenter="emit('stream-hover-start')"
    @mouseleave="emit('stream-hover-end')"
  >
    <header class="situation-signal-rail__header">
      <div class="situation-signal-rail__title-group">
        <strong>重要情报流</strong>
        <small>持续更新海淀区风险、事件与重点关注对象。</small>
      </div>

      <div class="situation-signal-rail__controls">
        <button type="button" :disabled="!tickerItems.length" @click="emit('previous')">
          <el-icon><ArrowUp /></el-icon>
        </button>
        <button type="button" :disabled="tickerItems.length <= 1" @click="emit('toggle-playback')">
          <el-icon><VideoPlay v-if="isPlaybackPaused" /><VideoPause v-else /></el-icon>
        </button>
        <button type="button" :disabled="!tickerItems.length" @click="emit('next')">
          <el-icon><ArrowDown /></el-icon>
        </button>
      </div>
    </header>

      <div class="situation-signal-rail__meta">
        <span>{{ tickerItems.length }} 条</span>
        <span>{{ isPlaybackPaused ? '暂停' : '滚动中' }}</span>
        <span>{{ activeLayer }}</span>
      </div>

    <div v-if="tickerItems.length" class="situation-signal-rail__stream">
      <div class="situation-signal-rail__viewport">
        <div class="situation-signal-rail__track" :style="streamTrackStyle">
          <button
            v-for="(item, index) in streamRenderItems"
            :key="`${item.id}-${index}`"
            type="button"
            class="situation-signal-rail__item"
            :class="{ 'is-active': index % tickerItems.length === activeTickerIndex }"
            @click="emit('select-ticker', index % tickerItems.length)"
          >
            <div class="situation-signal-rail__item-meta">
              <span class="is-layer">{{ item.layer }}</span>
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

    <div v-else class="situation-signal-rail__empty">
      <strong>当前暂无可滚动情报</strong>
      <small>请切换图层或等待新数据进入海淀区情报流。</small>
    </div>

    <footer class="situation-signal-rail__focus">
      <div class="situation-signal-rail__focus-head">
        <span class="situation-signal-rail__focus-caption">{{ focusCaption }}</span>
      </div>

      <strong class="situation-signal-rail__focus-title">{{ focusTitle }}</strong>
      <div class="situation-signal-rail__focus-meta">
        <span v-for="item in focusMeta" :key="item">{{ item }}</span>
      </div>
      <p class="situation-signal-rail__focus-summary">{{ focusSummary }}</p>
      <small class="situation-signal-rail__guide">{{ guideInstruction }}</small>

      <div class="situation-signal-rail__actions">
        <button type="button" @click="emit('open-detail')">查看详情</button>
        <button type="button" @click="emit('open-search')">检索溯源</button>
      </div>

      <div v-if="railActions.length" class="situation-signal-rail__quick-list">
        <button
          v-for="item in railActions"
          :key="item.id"
          type="button"
          class="situation-signal-rail__quick-item"
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
    </footer>
  </aside>
</template>

<style scoped>
.situation-signal-rail {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 8px;
  min-height: 0;
  height: 100%;
  overflow: hidden;
  padding: 2px 0 4px 10px;
  border-left: 1px solid rgba(116, 170, 240, 0.08);
  background:
    linear-gradient(180deg, rgba(7, 15, 24, 0.12) 0%, rgba(7, 15, 24, 0.38) 100%);
}

.situation-signal-rail__header {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: flex-start;
}

.situation-signal-rail__title-group {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 2px;
}

.situation-signal-rail__eyebrow {
  color: #8fbfff;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.situation-signal-rail__title-group strong,
.situation-signal-rail__item strong,
.situation-signal-rail__focus-title,
.situation-signal-rail__empty strong {
  color: #f1f7ff;
}

.situation-signal-rail__title-group strong {
  font-size: 16px;
  line-height: 1.2;
}

.situation-signal-rail__title-group small,
.situation-signal-rail__item small,
.situation-signal-rail__focus-summary,
.situation-signal-rail__guide,
.situation-signal-rail__empty small {
  color: rgba(210, 224, 242, 0.68);
  line-height: 1.5;
  font-size: 11px;
}

.situation-signal-rail__controls {
  display: flex;
  gap: 6px;
}

.situation-signal-rail__controls button,
.situation-signal-rail__actions button,
.situation-signal-rail__quick-item {
  border: 1px solid rgba(118, 168, 238, 0.12);
  background: rgba(7, 15, 24, 0.36);
  color: #e8f1fb;
  cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease, color 0.2s ease;
}

.situation-signal-rail__controls button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
}

.situation-signal-rail__controls button:hover,
.situation-signal-rail__actions button:hover,
.situation-signal-rail__quick-item:hover {
  border-color: rgba(118, 168, 238, 0.28);
  background: rgba(18, 33, 52, 0.74);
  color: #f4f8ff;
}

.situation-signal-rail__controls button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.situation-signal-rail__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.situation-signal-rail__meta span,
.situation-signal-rail__focus-meta span,
.situation-signal-rail__focus-caption {
  display: inline-flex;
  align-items: center;
  min-height: 22px;
  border: 1px solid rgba(118, 168, 238, 0.08);
  padding: 0 8px;
  color: rgba(196, 214, 236, 0.7);
  font-size: 10px;
  background: rgba(7, 15, 25, 0.18);
}

.situation-signal-rail__stream {
  min-height: 0;
  flex: 1 1 auto;
  overflow: hidden;
}

.situation-signal-rail__viewport {
  height: 100%;
  min-height: 300px;
  overflow: hidden;
  border: 1px solid rgba(118, 168, 238, 0.08);
  border-radius: 16px;
  background:
    linear-gradient(180deg, rgba(7, 15, 25, 0.52) 0%, rgba(7, 15, 25, 0.18) 100%);
  box-shadow: inset 0 1px 0 rgba(157, 199, 255, 0.03);
}

.situation-signal-rail__track {
  display: flex;
  flex-direction: column;
}

.situation-signal-rail__item {
  display: flex;
  min-height: 68px;
  flex-direction: column;
  justify-content: center;
  gap: 5px;
  border: 0;
  border-bottom: 1px solid rgba(118, 168, 238, 0.04);
  padding: 9px 12px;
  background: transparent;
  text-align: left;
  cursor: pointer;
  transition:
    background 0.28s ease,
    border-color 0.28s ease,
    transform 0.28s ease,
    box-shadow 0.28s ease;
}

.situation-signal-rail__item strong {
  font-size: 13px;
  line-height: 1.4;
}

.situation-signal-rail__item:hover,
.situation-signal-rail__item.is-active {
  background:
    linear-gradient(90deg, rgba(56, 117, 189, 0.12), rgba(18, 36, 59, 0.36));
  border-color: rgba(118, 168, 238, 0.16);
  transform: translateX(-2px);
  box-shadow: inset 2px 0 0 rgba(123, 194, 255, 0.78);
}

.situation-signal-rail__item-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 5px 8px;
  color: #91c2ff;
  font-size: 10px;
}

.situation-signal-rail__item-meta .is-layer {
  color: #ffd789;
  font-weight: 700;
}

.situation-signal-rail__focus {
  display: flex;
  flex-direction: column;
  gap: 6px;
  border-top: 1px solid rgba(118, 168, 238, 0.08);
  padding-top: 8px;
  flex: 0 0 auto;
  max-height: 200px;
  overflow: auto;
}

.situation-signal-rail__focus-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.situation-signal-rail__focus-title {
  font-size: 15px;
  line-height: 1.45;
}

.situation-signal-rail__focus-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.situation-signal-rail__focus-summary,
.situation-signal-rail__guide {
  margin: 0;
}

.situation-signal-rail__focus-summary {
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.situation-signal-rail__actions,
.situation-signal-rail__quick-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.situation-signal-rail__actions button,
.situation-signal-rail__quick-item {
  min-height: 28px;
  padding: 0 10px;
  font-size: 11px;
}

.situation-signal-rail__quick-list {
  margin-top: -1px;
}

.situation-signal-rail__empty {
  display: flex;
  flex-direction: column;
  gap: 6px;
  border-top: 1px dashed rgba(118, 168, 238, 0.14);
  border-bottom: 1px dashed rgba(118, 168, 238, 0.14);
  padding: 16px 0;
}

@media (max-width: 1180px) {
  .situation-signal-rail {
    border-left: 0;
    border-top: 1px solid rgba(116, 170, 240, 0.12);
    padding: 8px 0 0;
  }

  .situation-signal-rail__viewport {
    min-height: 260px;
  }
}
</style>
