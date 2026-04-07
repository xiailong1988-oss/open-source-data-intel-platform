<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import type {
  DashboardCockpitFeedFilter,
  DashboardCockpitLayer,
  DashboardCockpitStreamEntry,
} from '../../types/dashboardCockpit'

const props = defineProps<{
  items: DashboardCockpitStreamEntry[]
  activeItemId: string
  activeFilter: DashboardCockpitFeedFilter
  layers: DashboardCockpitLayer[]
  currentRegionName: string
  mode: 'dynamic' | 'static'
}>()

const emit = defineEmits<{
  (event: 'select-item', pointId: string): void
  (event: 'open-item', pointId: string): void
  (event: 'change-filter', filter: DashboardCockpitFeedFilter): void
  (event: 'change-mode', mode: 'dynamic' | 'static'): void
  (event: 'hover-start'): void
  (event: 'hover-end'): void
}>()

const viewportRef = ref<HTMLElement | null>(null)
const trackRef = ref<HTMLElement | null>(null)
const translateY = ref(0)
const loopDistance = ref(0)
const isHovered = ref(false)

let frameId = 0
let resizeObserver: ResizeObserver | null = null
let lastTimestamp = 0

const scrollSpeed = 26

const renderItems = computed(() => {
  if (props.mode !== 'dynamic' || props.items.length <= 1) {
    return props.items
  }

  return props.items.concat(props.items)
})

const trackStyle = computed(() =>
  props.mode === 'dynamic'
    ? {
        transform: `translate3d(0, -${translateY.value}px, 0)`,
      }
    : undefined,
)

const stopLoop = () => {
  if (frameId) {
    window.cancelAnimationFrame(frameId)
    frameId = 0
  }
  lastTimestamp = 0
}

const measureLoop = async () => {
  await nextTick()

  const track = trackRef.value
  if (!track || props.mode !== 'dynamic' || props.items.length <= 1) {
    loopDistance.value = 0
    translateY.value = 0
    stopLoop()
    return
  }

  const originals = Array.from(track.querySelectorAll<HTMLElement>('[data-original-item="true"]'))
  if (!originals.length) {
    loopDistance.value = 0
    translateY.value = 0
    stopLoop()
    return
  }

  const firstTop = originals[0]?.offsetTop ?? 0
  const last = originals[originals.length - 1]
  const lastBottom = (last?.offsetTop ?? 0) + (last?.offsetHeight ?? 0)
  loopDistance.value = Math.max(0, lastBottom - firstTop + 10)

  if (translateY.value >= loopDistance.value) {
    translateY.value = 0
  }

  startLoop()
}

const tick = (timestamp: number) => {
  if (!loopDistance.value || props.mode !== 'dynamic' || isHovered.value) {
    lastTimestamp = timestamp
    frameId = window.requestAnimationFrame(tick)
    return
  }

  if (!lastTimestamp) {
    lastTimestamp = timestamp
  }

  const delta = Math.min(48, timestamp - lastTimestamp)
  lastTimestamp = timestamp
  translateY.value += (delta / 1000) * scrollSpeed

  if (translateY.value >= loopDistance.value) {
    translateY.value -= loopDistance.value
  }

  frameId = window.requestAnimationFrame(tick)
}

const startLoop = () => {
  stopLoop()

  if (!loopDistance.value || props.mode !== 'dynamic' || props.items.length <= 1) {
    return
  }

  frameId = window.requestAnimationFrame(tick)
}

const handleHoverStart = () => {
  isHovered.value = true
  emit('hover-start')
}

const handleHoverEnd = () => {
  isHovered.value = false
  emit('hover-end')
}

watch(
  () => [props.mode, props.items.map((item) => item.id).join('|')].join('::'),
  () => {
    void measureLoop()
  },
  { immediate: true },
)

onMounted(() => {
  resizeObserver = new ResizeObserver(() => {
    void measureLoop()
  })

  if (viewportRef.value) {
    resizeObserver.observe(viewportRef.value)
  }
  if (trackRef.value) {
    resizeObserver.observe(trackRef.value)
  }

  void measureLoop()
})

onBeforeUnmount(() => {
  stopLoop()
  resizeObserver?.disconnect()
})
</script>

<template>
  <section class="osint-stream-panel" @mouseenter="handleHoverStart" @mouseleave="handleHoverEnd">
    <header class="osint-stream-panel__head">
      <div>
        <strong>主情报流</strong>
        <small>{{ currentRegionName }} · {{ items.length }} 条动态</small>
      </div>

      <div class="osint-stream-panel__actions">
        <button
          type="button"
          class="osint-stream-panel__mode"
          :class="{ 'is-active': mode === 'dynamic' }"
          @click="emit('change-mode', 'dynamic')"
        >
          动态模式
        </button>
        <button
          type="button"
          class="osint-stream-panel__mode"
          :class="{ 'is-active': mode === 'static' }"
          @click="emit('change-mode', 'static')"
        >
          静态模式
        </button>
      </div>
    </header>

    <div class="osint-stream-panel__filters">
      <button
        type="button"
        class="osint-stream-panel__filter"
        :class="{ 'is-active': activeFilter === '全部' }"
        @click="emit('change-filter', '全部')"
      >
        全部类型
      </button>
      <button
        v-for="layer in layers"
        :key="layer"
        type="button"
        class="osint-stream-panel__filter"
        :class="{ 'is-active': activeFilter === layer }"
        @click="emit('change-filter', layer)"
      >
        {{ layer }}
      </button>
    </div>

    <div v-if="items.length" ref="viewportRef" class="osint-stream-panel__viewport" :class="{ 'is-static': mode === 'static' }">
      <div ref="trackRef" class="osint-stream-panel__track" :style="trackStyle">
        <button
          v-for="(item, index) in renderItems"
          :key="`${item.id}-${index}`"
          type="button"
          class="osint-stream-panel__item"
          :class="{ 'is-active': item.id === activeItemId }"
          :data-original-item="index < items.length"
          @click="emit('select-item', item.relatedPointId)"
          @dblclick="emit('open-item', item.relatedPointId)"
        >
          <span class="osint-stream-panel__bullet" />
          <strong>{{ item.title }}</strong>
        </button>
      </div>
    </div>

    <div v-else class="osint-stream-panel__empty">当前筛选下暂无情报动态，请切换地区或类型。</div>
  </section>
</template>

<style scoped>
.osint-stream-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
  height: 100%;
  min-height: 0;
}

.osint-stream-panel__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.osint-stream-panel__head strong {
  color: #eff6ff;
  font-size: 16px;
}

.osint-stream-panel__head small,
.osint-stream-panel__empty {
  color: rgba(203, 220, 240, 0.72);
  font-size: 11px;
}

.osint-stream-panel__actions,
.osint-stream-panel__filters {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.osint-stream-panel__filters {
  overflow-x: auto;
  scrollbar-width: none;
}

.osint-stream-panel__filters::-webkit-scrollbar {
  display: none;
}

.osint-stream-panel__mode,
.osint-stream-panel__filter {
  min-height: 26px;
  border: 1px solid rgba(118, 171, 246, 0.1);
  border-radius: 999px;
  padding: 0 10px;
  background: rgba(8, 16, 27, 0.46);
  color: rgba(216, 229, 244, 0.76);
  font-size: 11px;
  cursor: pointer;
  transition: border-color 0.22s ease, background 0.22s ease, color 0.22s ease, transform 0.22s ease;
}

.osint-stream-panel__mode:hover,
.osint-stream-panel__filter:hover,
.osint-stream-panel__mode.is-active,
.osint-stream-panel__filter.is-active {
  transform: translateY(-1px);
  border-color: rgba(124, 190, 255, 0.34);
  background: rgba(18, 34, 52, 0.82);
  color: #f4f9ff;
}

.osint-stream-panel__viewport {
  min-height: 0;
  flex: 1 1 auto;
  overflow: hidden;
  border: 1px solid rgba(118, 171, 246, 0.1);
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(8, 16, 27, 0.78) 0%, rgba(8, 15, 25, 0.58) 100%);
  padding: 8px;
}

.osint-stream-panel__viewport.is-static {
  overflow: auto;
}

.osint-stream-panel__track {
  display: flex;
  flex-direction: column;
  gap: 8px;
  will-change: transform;
}

.osint-stream-panel__viewport.is-static .osint-stream-panel__track {
  transform: none !important;
}

.osint-stream-panel__item {
  display: grid;
  grid-template-columns: 8px minmax(0, 1fr);
  align-items: center;
  gap: 8px;
  min-height: 56px;
  border: 1px solid rgba(118, 171, 246, 0.08);
  border-radius: 16px;
  background: rgba(9, 17, 27, 0.72);
  padding: 0 12px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.22s ease, border-color 0.22s ease, box-shadow 0.22s ease, background 0.22s ease;
}

.osint-stream-panel__item:hover,
.osint-stream-panel__item.is-active {
  transform: translateY(-1px);
  border-color: rgba(126, 192, 255, 0.28);
  box-shadow: 0 14px 28px rgba(3, 10, 18, 0.22);
  background: rgba(16, 30, 46, 0.88);
}

.osint-stream-panel__bullet {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: linear-gradient(180deg, #78c6ff 0%, #53a4ff 100%);
  box-shadow: 0 0 12px rgba(107, 190, 255, 0.4);
}

.osint-stream-panel__item strong {
  overflow: hidden;
  color: #f2f7ff;
  display: -webkit-box;
  font-size: 15px;
  line-height: 1.45;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.osint-stream-panel__empty {
  min-height: 72px;
  border: 1px dashed rgba(118, 171, 246, 0.12);
  border-radius: 16px;
  padding: 18px 16px;
}
</style>
