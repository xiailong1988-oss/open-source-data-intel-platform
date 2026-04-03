<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { GridStack, type GridStackWidget } from 'gridstack'
import type { DashboardSystemModule } from '../../types/dashboardCockpit'

interface SystemModuleLayout extends GridStackWidget {
  id: DashboardSystemModule['id']
  x: number
  y: number
  w: number
  h: number
  minH: number
  maxH: number
}

const props = defineProps<{
  modules: DashboardSystemModule[]
}>()

const emit = defineEmits<{
  (event: 'open-module', module: DashboardSystemModule): void
}>()

const STORAGE_KEY = 'dashboard-left-system-grid-v3'
const gridRoot = ref<HTMLElement | null>(null)
const widgetLayouts = ref<SystemModuleLayout[]>([])
const animatedMetricValues = reactive<Record<string, number>>({})
let grid: GridStack | null = null
let frameId = 0

const accentClassMap: Record<DashboardSystemModule['accent'], string> = {
  cyan: 'is-cyan',
  blue: 'is-blue',
  amber: 'is-amber',
  green: 'is-green',
}

const defaultLayouts = (): SystemModuleLayout[] =>
  props.modules.map((module, index) => ({
    id: module.id,
    x: 0,
    y: index,
    w: 1,
    h: 1,
    minH: 1,
    maxH: 2,
  }))

const reconcileLayouts = (candidate: SystemModuleLayout[]) => {
  const next = candidate.length ? [...candidate] : defaultLayouts()
  const existingIds = new Set(next.map((item) => item.id))

  props.modules.forEach((module, index) => {
    if (!existingIds.has(module.id)) {
      next.push({ id: module.id, x: 0, y: next.length || index, w: 1, h: 1, minH: 1, maxH: 2 })
    }
  })

  return next
    .filter((item) => props.modules.some((module) => module.id === item.id))
    .sort((left, right) => left.y - right.y)
    .map((item, index) => ({
      id: item.id,
      x: 0,
      y: index,
      w: 1,
      h: Math.min(2, Math.max(1, item.h ?? 1)),
      minH: 1,
      maxH: 2,
    }))
}

const moduleMap = computed(() => new Map(props.modules.map((module) => [module.id, module])))
const displayWidgets = computed(() =>
  widgetLayouts.value
    .map((layout) => {
      const module = moduleMap.value.get(layout.id)
      return module ? { layout, module } : null
    })
    .filter((item): item is { layout: SystemModuleLayout; module: DashboardSystemModule } => Boolean(item)),
)

const persistLayouts = (layouts: SystemModuleLayout[]) => {
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(layouts))
}

const captureLayoutsFromDom = () => {
  if (!gridRoot.value) return []

  return Array.from(gridRoot.value.querySelectorAll<HTMLElement>('.grid-stack-item[data-widget-id]'))
    .map((item) => ({
      id: item.dataset.widgetId as DashboardSystemModule['id'],
      x: 0,
      y: Number(item.getAttribute('gs-y') ?? 0),
      w: 1,
      h: Math.min(2, Math.max(1, Number(item.getAttribute('gs-h') ?? 1))),
      minH: 1,
      maxH: 2,
    }))
    .sort((left, right) => left.y - right.y)
}

const destroyGrid = () => {
  if (!grid) return
  grid.offAll()
  grid.destroy(false)
  grid = null
}

const initGrid = async () => {
  if (!gridRoot.value) return

  destroyGrid()
  await nextTick()

  grid = GridStack.init(
    {
      column: 1,
      cellHeight: 96,
      margin: 8,
      float: false,
      minRow: 4,
      disableDrag: false,
      disableResize: false,
      animate: true,
      alwaysShowResizeHandle: false,
      handle: '.situation-left-pulse__module',
      draggable: {
        cancel: 'button',
      },
      resizable: { handles: 's,se' },
    },
    gridRoot.value,
  )

  grid.on('change', () => {
    const nextLayouts = reconcileLayouts(captureLayoutsFromDom())
    widgetLayouts.value = nextLayouts
    persistLayouts(nextLayouts)
  })
}

const restoreDefault = async () => {
  const nextLayouts = defaultLayouts()
  widgetLayouts.value = nextLayouts
  persistLayouts(nextLayouts)
  await initGrid()
}

const animateMetrics = () => {
  if (frameId) {
    window.cancelAnimationFrame(frameId)
  }

  const metrics = props.modules.flatMap((module) => module.metrics)
  const fromEntries = Object.fromEntries(metrics.map((metric) => [metric.id, animatedMetricValues[metric.id] ?? 0]))
  const startedAt = performance.now()
  const duration = 760

  const tick = (timestamp: number) => {
    const progress = Math.min(1, (timestamp - startedAt) / duration)
    const eased = 1 - Math.pow(1 - progress, 3)

    metrics.forEach((metric) => {
      animatedMetricValues[metric.id] = fromEntries[metric.id] + (metric.value - fromEntries[metric.id]) * eased
    })

    if (progress < 1) {
      frameId = window.requestAnimationFrame(tick)
    }
  }

  frameId = window.requestAnimationFrame(tick)
}

watch(
  () => props.modules.map((module) => `${module.id}:${module.metrics.map((metric) => `${metric.id}:${metric.value}`).join(',')}`).join('|'),
  () => {
    widgetLayouts.value = reconcileLayouts(widgetLayouts.value)
    animateMetrics()
    void initGrid()
  },
  { immediate: true },
)

onMounted(async () => {
  const saved = window.localStorage.getItem(STORAGE_KEY)
  if (saved) {
    try {
      widgetLayouts.value = reconcileLayouts(JSON.parse(saved) as SystemModuleLayout[])
    } catch {
      widgetLayouts.value = defaultLayouts()
    }
  } else {
    widgetLayouts.value = defaultLayouts()
  }

  await initGrid()
  animateMetrics()
})

onBeforeUnmount(() => {
  destroyGrid()
  if (frameId) {
    window.cancelAnimationFrame(frameId)
  }
})
</script>

<template>
  <aside class="situation-left-pulse" v-motion :initial="{ opacity: 0, x: -18 }" :enter="{ opacity: 1, x: 0, transition: { duration: 620 } }">
    <header class="situation-left-pulse__shell-header">
      <div>
        <strong>系统统计快照</strong>
      </div>
      <button type="button" class="situation-left-pulse__reset" @click="restoreDefault">恢复默认</button>
    </header>

    <div ref="gridRoot" class="situation-left-pulse__grid grid-stack">
      <section
        v-for="(item, index) in displayWidgets"
        :key="item.module.id"
        class="grid-stack-item situation-left-pulse__item"
        :data-widget-id="item.module.id"
        :gs-id="item.module.id"
        :gs-x="item.layout.x"
        :gs-y="item.layout.y"
        :gs-w="item.layout.w"
        :gs-h="item.layout.h"
        :gs-min-h="item.layout.minH"
        :gs-max-h="item.layout.maxH"
      >
        <div class="grid-stack-item-content">
          <article
            class="situation-left-pulse__module"
            :class="accentClassMap[item.module.accent]"
            v-motion
            :initial="{ opacity: 0, y: 18, scale: 0.98 }"
            :enter="{ opacity: 1, y: 0, scale: 1, transition: { delay: 120 + index * 70, duration: 360 } }"
          >
            <header class="situation-left-pulse__module-head">
              <div>
                <strong>{{ item.module.title }}</strong>
              </div>
              <div class="situation-left-pulse__module-actions">
                <button type="button" class="situation-left-pulse__open" @click="emit('open-module', item.module)">打开</button>
              </div>
            </header>

            <div class="situation-left-pulse__metric-grid">
              <button
                v-for="metric in item.module.metrics"
                :key="metric.id"
                type="button"
                class="situation-left-pulse__metric"
                @click="emit('open-module', item.module)"
              >
                <span class="situation-left-pulse__metric-label">
                  <i :class="metric.status ?? 'neutral'" />
                  {{ metric.label }}
                </span>
                <strong>
                  {{ Math.round(animatedMetricValues[metric.id] ?? metric.value) }}
                  <small v-if="metric.suffix">{{ metric.suffix }}</small>
                </strong>
              </button>
            </div>
          </article>
        </div>
      </section>
    </div>
  </aside>
</template>

<style scoped>
.situation-left-pulse {
  display: flex;
  min-width: 0;
  min-height: 0;
  height: 100%;
  flex-direction: column;
  gap: 8px;
}

.situation-left-pulse__shell-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.situation-left-pulse__eyebrow {
  color: #83c2ff;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.situation-left-pulse__shell-header strong,
.situation-left-pulse__module strong,
.situation-left-pulse__metric strong {
  color: #eff6ff;
}

.situation-left-pulse__reset,
.situation-left-pulse__open {
  border: 1px solid rgba(118, 168, 240, 0.12);
  background: rgba(8, 16, 28, 0.42);
  color: rgba(202, 220, 244, 0.74);
  cursor: pointer;
}

.situation-left-pulse__reset,
.situation-left-pulse__open {
  min-height: 24px;
  padding: 0 8px;
  border-radius: 999px;
  font-size: 9px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.situation-left-pulse__grid {
  flex: 1 1 auto;
  min-height: 0;
  overflow: auto;
  padding-right: 2px;
}

.situation-left-pulse__item > .grid-stack-item-content {
  overflow: hidden;
}

.situation-left-pulse__module {
  position: relative;
  display: flex;
  height: 100%;
  min-height: 0;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid rgba(116, 168, 240, 0.1);
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(8, 16, 28, 0.88) 0%, rgba(9, 15, 25, 0.72) 100%);
  padding: 10px;
  backdrop-filter: blur(14px);
  cursor: grab;
}

.situation-left-pulse__module::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  opacity: 0.84;
}

.situation-left-pulse__module.is-cyan::before {
  background: linear-gradient(180deg, rgba(69, 209, 196, 0.08) 0%, transparent 42%);
}

.situation-left-pulse__module.is-blue::before {
  background: linear-gradient(180deg, rgba(98, 169, 255, 0.08) 0%, transparent 42%);
}

.situation-left-pulse__module.is-amber::before {
  background: linear-gradient(180deg, rgba(255, 190, 109, 0.08) 0%, transparent 42%);
}

.situation-left-pulse__module.is-green::before {
  background: linear-gradient(180deg, rgba(90, 201, 143, 0.08) 0%, transparent 42%);
}

.situation-left-pulse__module-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.situation-left-pulse__module-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.situation-left-pulse__metric-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px;
  min-height: 0;
}

.situation-left-pulse__metric {
  border: 1px solid rgba(116, 168, 240, 0.08);
  border-radius: 14px;
  background: rgba(5, 12, 20, 0.5);
  padding: 8px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.24s ease, border-color 0.24s ease, box-shadow 0.24s ease, background 0.24s ease;
}

.situation-left-pulse__metric:hover {
  transform: translateY(-2px);
  border-color: rgba(137, 190, 255, 0.22);
  box-shadow: 0 14px 28px rgba(2, 8, 15, 0.22);
  background: rgba(8, 18, 31, 0.74);
}

.situation-left-pulse__metric-label {
  display: flex;
  align-items: center;
  gap: 6px;
  color: rgba(191, 210, 233, 0.72);
  font-size: 10px;
  line-height: 1.35;
}

.situation-left-pulse__metric-label i {
  display: inline-flex;
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: rgba(120, 156, 191, 0.56);
}

.situation-left-pulse__metric-label i.online {
  background: #57d389;
}

.situation-left-pulse__metric-label i.warning {
  background: #ffb05e;
}

.situation-left-pulse__metric-label i.offline {
  background: #ff6f7e;
}

.situation-left-pulse__metric strong {
  display: block;
  margin-top: 6px;
  font-size: 20px;
  line-height: 1;
}

.situation-left-pulse__metric strong small {
  font-size: 10px;
}

.situation-left-pulse :deep(.grid-stack-item.ui-draggable-dragging .grid-stack-item-content),
.situation-left-pulse :deep(.grid-stack-item.ui-resizable-resizing .grid-stack-item-content) {
  box-shadow: 0 18px 34px rgba(2, 8, 15, 0.22);
}

.situation-left-pulse :deep(.ui-resizable-handle) {
  opacity: 0;
  background: transparent;
  transition: opacity 0.18s ease;
}

.situation-left-pulse :deep(.grid-stack-item:hover .ui-resizable-handle),
.situation-left-pulse :deep(.grid-stack-item.ui-resizable-resizing .ui-resizable-handle) {
  opacity: 0.12;
}

.situation-left-pulse :deep(.grid-stack-item.ui-draggable-dragging .situation-left-pulse__module) {
  cursor: grabbing;
}

@media (max-width: 1180px) {
  .situation-left-pulse__metric-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .situation-left-pulse__metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
