<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { GridStack, type GridStackWidget } from 'gridstack'
import type { DashboardBusinessBoardMetric } from '../../types/dashboardCockpit'

interface BoardLayout extends GridStackWidget {
  id: DashboardBusinessBoardMetric['id']
  x: number
  y: number
  w: number
  h: number
  minW: number
  maxW: number
  minH: number
  maxH: number
}

const props = defineProps<{
  boards: DashboardBusinessBoardMetric[]
}>()

const emit = defineEmits<{
  (event: 'open-board', board: DashboardBusinessBoardMetric): void
}>()

const STORAGE_KEY = 'dashboard-business-board-grid-v3'
const gridRoot = ref<HTMLElement | null>(null)
const boardLayouts = ref<BoardLayout[]>([])
const animatedValues = reactive<Record<string, number>>({})
let grid: GridStack | null = null
let frameId = 0

const defaultLayouts = (): BoardLayout[] =>
  props.boards.map((board, index) => ({
    id: board.id,
    x: index,
    y: 0,
    w: 1,
    h: 1,
    minW: 1,
    maxW: 3,
    minH: 1,
    maxH: 2,
  }))

const reconcileLayouts = (candidate: BoardLayout[]) => {
  const next = candidate.length ? [...candidate] : defaultLayouts()
  const existingIds = new Set(next.map((item) => item.id))

  props.boards.forEach((board, index) => {
    if (!existingIds.has(board.id)) {
      next.push({ id: board.id, x: index, y: 0, w: 1, h: 1, minW: 1, maxW: 3, minH: 1, maxH: 2 })
    }
  })

  return next
    .filter((item) => props.boards.some((board) => board.id === item.id))
    .map((item) => ({
      id: item.id,
      x: Math.max(0, item.x ?? 0),
      y: Math.max(0, item.y ?? 0),
      w: Math.min(3, Math.max(1, item.w ?? 1)),
      h: Math.min(2, Math.max(1, item.h ?? 1)),
      minW: 1,
      maxW: 3,
      minH: 1,
      maxH: 2,
    }))
    .sort((left, right) => (left.y - right.y) || (left.x - right.x))
}

const boardMap = computed(() => new Map(props.boards.map((board) => [board.id, board])))
const displayBoards = computed(() =>
  boardLayouts.value
    .map((layout) => {
      const board = boardMap.value.get(layout.id)
      return board ? { layout, board } : null
    })
    .filter((item): item is { layout: BoardLayout; board: DashboardBusinessBoardMetric } => Boolean(item)),
)

const persistLayouts = (layouts: BoardLayout[]) => {
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(layouts))
}

const captureLayoutsFromDom = () => {
  if (!gridRoot.value) return []

  return Array.from(gridRoot.value.querySelectorAll<HTMLElement>('.grid-stack-item[data-board-id]'))
    .map((item) => ({
      id: item.dataset.boardId as DashboardBusinessBoardMetric['id'],
      x: Number(item.getAttribute('gs-x') ?? 0),
      y: Number(item.getAttribute('gs-y') ?? 0),
      w: Math.min(3, Math.max(1, Number(item.getAttribute('gs-w') ?? 1))),
      h: Math.min(2, Math.max(1, Number(item.getAttribute('gs-h') ?? 1))),
      minW: 1,
      maxW: 3,
      minH: 1,
      maxH: 2,
    }))
    .sort((left, right) => (left.y - right.y) || (left.x - right.x))
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
      column: 6,
      cellHeight: 138,
      margin: 12,
      float: false,
      minRow: 1,
      disableDrag: false,
      disableResize: false,
      animate: true,
      alwaysShowResizeHandle: true,
      handle: '.situation-bottom-band__drag',
      resizable: { handles: 'e,se,s,sw,w' },
    },
    gridRoot.value,
  )

  grid.on('change', () => {
    const nextLayouts = reconcileLayouts(captureLayoutsFromDom())
    boardLayouts.value = nextLayouts
    persistLayouts(nextLayouts)
  })
}

const restoreDefault = async () => {
  const nextLayouts = defaultLayouts()
  boardLayouts.value = nextLayouts
  persistLayouts(nextLayouts)
  await initGrid()
}

const animateBoards = () => {
  if (frameId) {
    window.cancelAnimationFrame(frameId)
  }

  const startedAt = performance.now()
  const fromEntries = Object.fromEntries(props.boards.map((board) => [board.id, animatedValues[board.id] ?? 0]))
  const duration = 780

  const tick = (timestamp: number) => {
    const progress = Math.min(1, (timestamp - startedAt) / duration)
    const eased = 1 - Math.pow(1 - progress, 3)

    props.boards.forEach((board) => {
      animatedValues[board.id] = fromEntries[board.id] + (board.currentValue - fromEntries[board.id]) * eased
    })

    if (progress < 1) {
      frameId = window.requestAnimationFrame(tick)
    }
  }

  frameId = window.requestAnimationFrame(tick)
}

watch(
  () => props.boards.map((board) => `${board.id}:${board.currentValue}:${board.increment}:${board.status}`).join('|'),
  () => {
    boardLayouts.value = reconcileLayouts(boardLayouts.value)
    animateBoards()
    void initGrid()
  },
  { immediate: true },
)

onMounted(async () => {
  const saved = window.localStorage.getItem(STORAGE_KEY)
  if (saved) {
    try {
      boardLayouts.value = reconcileLayouts(JSON.parse(saved) as BoardLayout[])
    } catch {
      boardLayouts.value = defaultLayouts()
    }
  } else {
    boardLayouts.value = defaultLayouts()
  }

  await initGrid()
  animateBoards()
})

onBeforeUnmount(() => {
  destroyGrid()
  if (frameId) {
    window.cancelAnimationFrame(frameId)
  }
})
</script>

<template>
  <section
    class="situation-bottom-band"
    v-motion
    :initial="{ opacity: 0, y: 20 }"
    :enter="{ opacity: 1, y: 0, transition: { delay: 260, duration: 620 } }"
  >
    <header class="situation-bottom-band__shell-header">
      <div>
        <span class="situation-bottom-band__eyebrow">Board Matrix</span>
        <strong>六大板块摘要矩阵</strong>
      </div>
      <button type="button" class="situation-bottom-band__reset" @click="restoreDefault">恢复默认布局</button>
    </header>

    <div ref="gridRoot" class="situation-bottom-band__grid grid-stack">
      <article
        v-for="(item, index) in displayBoards"
        :key="item.board.id"
        class="grid-stack-item situation-bottom-band__item"
        :data-board-id="item.board.id"
        :gs-id="item.board.id"
        :gs-x="item.layout.x"
        :gs-y="item.layout.y"
        :gs-w="item.layout.w"
        :gs-h="item.layout.h"
        :gs-min-w="item.layout.minW"
        :gs-max-w="item.layout.maxW"
        :gs-min-h="item.layout.minH"
        :gs-max-h="item.layout.maxH"
      >
        <div class="grid-stack-item-content">
          <section
            class="situation-bottom-band__card"
            :class="[`is-${item.board.riskLevel}`, `is-${item.board.trend}`]"
            v-motion
            :initial="{ opacity: 0, y: 18, scale: 0.98 }"
            :enter="{ opacity: 1, y: 0, scale: 1, transition: { delay: 180 + index * 55, duration: 360 } }"
          >
            <header class="situation-bottom-band__card-head">
              <div>
                <span class="situation-bottom-band__eyebrow">{{ item.board.eyebrow }}</span>
                <strong>{{ item.board.title }}</strong>
              </div>
              <button type="button" class="situation-bottom-band__drag" title="拖拽或缩放模块">⋮⋮</button>
            </header>

            <button type="button" class="situation-bottom-band__card-main" @click="emit('open-board', item.board)">
              <div class="situation-bottom-band__value-line">
                <strong>
                  {{ Math.round(animatedValues[item.board.id] ?? item.board.currentValue) }}
                  <small v-if="item.board.unit">{{ item.board.unit }}</small>
                </strong>
                <span>{{ item.board.increment }}</span>
              </div>

              <div class="situation-bottom-band__status-line">
                <em :class="`is-${item.board.riskLevel}`">{{ item.board.status }}</em>
                <small>{{ item.board.trend === 'up' ? 'trend up' : item.board.trend === 'down' ? 'trend down' : 'trend flat' }}</small>
              </div>

              <div class="situation-bottom-band__highlights">
                <span v-for="highlight in item.board.highlights" :key="highlight.label">
                  <strong>{{ highlight.value }}</strong>
                  <small>{{ highlight.label }}</small>
                </span>
              </div>
            </button>
          </section>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.situation-bottom-band {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
}

.situation-bottom-band__shell-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.situation-bottom-band__eyebrow {
  color: #83c2ff;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.situation-bottom-band__shell-header strong,
.situation-bottom-band__card strong {
  color: #eff6ff;
}

.situation-bottom-band__reset,
.situation-bottom-band__drag {
  border: 1px solid rgba(118, 168, 240, 0.12);
  background: rgba(8, 16, 28, 0.42);
  color: rgba(202, 220, 244, 0.74);
  cursor: pointer;
}

.situation-bottom-band__reset {
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 10px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.situation-bottom-band__grid {
  min-height: 0;
}

.situation-bottom-band__item > .grid-stack-item-content {
  overflow: hidden;
}

.situation-bottom-band__card {
  position: relative;
  display: flex;
  height: 100%;
  min-height: 0;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid rgba(118, 168, 240, 0.1);
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(8, 15, 25, 0.8) 0%, rgba(9, 16, 26, 0.68) 100%);
  padding: 12px;
}

.situation-bottom-band__card::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  opacity: 0.85;
}

.situation-bottom-band__card.is-高::before {
  background: linear-gradient(180deg, rgba(255, 113, 123, 0.08) 0%, transparent 42%);
}

.situation-bottom-band__card.is-中::before {
  background: linear-gradient(180deg, rgba(255, 190, 109, 0.08) 0%, transparent 42%);
}

.situation-bottom-band__card.is-低::before {
  background: linear-gradient(180deg, rgba(90, 201, 143, 0.08) 0%, transparent 42%);
}

.situation-bottom-band__card-head {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: flex-start;
  margin-bottom: 10px;
}

.situation-bottom-band__drag {
  width: 28px;
  min-height: 28px;
  border-radius: 12px;
  font-size: 14px;
}

.situation-bottom-band__card-main {
  display: flex;
  width: 100%;
  min-width: 0;
  flex-direction: column;
  gap: 10px;
  border: 0;
  background: transparent;
  padding: 0;
  text-align: left;
  cursor: pointer;
}

.situation-bottom-band__value-line,
.situation-bottom-band__status-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.situation-bottom-band__value-line strong {
  font-size: 28px;
  line-height: 1;
}

.situation-bottom-band__value-line strong small {
  font-size: 12px;
}

.situation-bottom-band__value-line span,
.situation-bottom-band__status-line small,
.situation-bottom-band__highlights small {
  color: rgba(192, 210, 233, 0.68);
}

.situation-bottom-band__status-line em {
  display: inline-flex;
  align-items: center;
  min-height: 22px;
  border-radius: 999px;
  padding: 0 8px;
  font-style: normal;
  font-size: 10px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.situation-bottom-band__status-line em.is-高 {
  background: rgba(255, 107, 107, 0.12);
  color: #ff9e9e;
}

.situation-bottom-band__status-line em.is-中 {
  background: rgba(255, 178, 94, 0.12);
  color: #ffd08e;
}

.situation-bottom-band__status-line em.is-低 {
  background: rgba(95, 201, 143, 0.12);
  color: #98e3b9;
}

.situation-bottom-band__highlights {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.situation-bottom-band__highlights span {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
  border: 1px solid rgba(118, 168, 240, 0.08);
  border-radius: 12px;
  background: rgba(5, 12, 20, 0.42);
  padding: 8px;
}

.situation-bottom-band__highlights strong {
  font-size: 15px;
}

.situation-bottom-band__card:hover {
  border-color: rgba(137, 190, 255, 0.24);
  box-shadow: 0 14px 28px rgba(2, 8, 15, 0.2);
}

.situation-bottom-band :deep(.grid-stack-item.ui-draggable-dragging .grid-stack-item-content),
.situation-bottom-band :deep(.grid-stack-item.ui-resizable-resizing .grid-stack-item-content) {
  box-shadow: 0 18px 34px rgba(2, 8, 15, 0.22);
}

.situation-bottom-band :deep(.ui-resizable-handle) {
  filter: brightness(1.25);
}
</style>
