<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import Sortable from 'sortablejs'
import type { DashboardBusinessBoardMetric } from '../../types/dashboardCockpit'

const props = defineProps<{
  boards: DashboardBusinessBoardMetric[]
}>()

const emit = defineEmits<{
  (event: 'open-board', board: DashboardBusinessBoardMetric): void
}>()

const STORAGE_KEY = 'dashboard-business-board-layout'
const boardOrder = ref<string[]>([])
const boardRoot = ref<HTMLElement | null>(null)
const animatedValues = reactive<Record<string, number>>({})
let sortable: Sortable | null = null
let frameId = 0

const orderedBoards = computed(() => {
  const order = boardOrder.value.length ? boardOrder.value : props.boards.map((item) => item.id)
  return order
    .map((id) => props.boards.find((item) => item.id === id))
    .filter((item): item is DashboardBusinessBoardMetric => Boolean(item))
})

const persistOrder = () => {
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(boardOrder.value))
}

const restoreDefault = async () => {
  boardOrder.value = props.boards.map((item) => item.id)
  persistOrder()
  await nextTick()
  sortable?.sort(boardOrder.value)
}

const animateBoards = () => {
  if (frameId) {
    window.cancelAnimationFrame(frameId)
  }

  const startedAt = performance.now()
  const fromEntries = Object.fromEntries(orderedBoards.value.map((board) => [board.id, animatedValues[board.id] ?? 0]))
  const duration = 880

  const tick = (timestamp: number) => {
    const progress = Math.min(1, (timestamp - startedAt) / duration)
    const eased = 1 - Math.pow(1 - progress, 3)

    orderedBoards.value.forEach((board) => {
      animatedValues[board.id] = fromEntries[board.id] + (board.currentValue - fromEntries[board.id]) * eased
    })

    if (progress < 1) {
      frameId = window.requestAnimationFrame(tick)
    }
  }

  frameId = window.requestAnimationFrame(tick)
}

watch(
  () => props.boards.map((board) => `${board.id}:${board.currentValue}`).join('|'),
  () => {
    animateBoards()
  },
  { immediate: true },
)

onMounted(async () => {
  const saved = window.localStorage.getItem(STORAGE_KEY)
  if (saved) {
    try {
      const parsed = JSON.parse(saved) as string[]
      if (Array.isArray(parsed) && parsed.length) {
        boardOrder.value = parsed
      }
    } catch {
      boardOrder.value = props.boards.map((item) => item.id)
    }
  } else {
    boardOrder.value = props.boards.map((item) => item.id)
  }

  await nextTick()

  if (boardRoot.value) {
    sortable = new Sortable(boardRoot.value, {
      animation: 180,
      handle: '.situation-bottom-band__drag',
      ghostClass: 'is-ghost',
      dragClass: 'is-dragging',
      onEnd: () => {
        if (!boardRoot.value) return
        boardOrder.value = Array.from(boardRoot.value.querySelectorAll<HTMLElement>('[data-board-id]')).map((item) => item.dataset.boardId ?? '')
        persistOrder()
      },
    })
  }
})

onBeforeUnmount(() => {
  sortable?.destroy()
  sortable = null
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
        <span class="situation-bottom-band__eyebrow">Business Matrix</span>
        <strong>六大板块摘要矩阵</strong>
      </div>
      <button type="button" class="situation-bottom-band__reset" @click="restoreDefault">恢复默认布局</button>
    </header>

    <div ref="boardRoot" class="situation-bottom-band__grid">
      <article
        v-for="(board, index) in orderedBoards"
        :key="board.id"
        :data-board-id="board.id"
        class="situation-bottom-band__card"
        :class="[`is-${board.riskLevel}`, `is-${board.trend}`]"
        v-motion
        :initial="{ opacity: 0, y: 22, scale: 0.98 }"
        :enter="{ opacity: 1, y: 0, scale: 1, transition: { delay: 200 + index * 60, duration: 420 } }"
      >
        <header class="situation-bottom-band__card-head">
          <div>
            <span class="situation-bottom-band__eyebrow">{{ board.eyebrow }}</span>
            <strong>{{ board.title }}</strong>
          </div>
          <button type="button" class="situation-bottom-band__drag" title="拖拽换位">⋮⋮</button>
        </header>

        <button type="button" class="situation-bottom-band__card-main" @click="emit('open-board', board)">
          <div class="situation-bottom-band__value-line">
            <strong>
              {{ Math.round(animatedValues[board.id] ?? board.currentValue) }}
              <small v-if="board.unit">{{ board.unit }}</small>
            </strong>
            <span>{{ board.increment }}</span>
          </div>
          <p>{{ board.summary }}</p>
          <div class="situation-bottom-band__highlights">
            <span v-for="item in board.highlights" :key="item.label">
              <strong>{{ item.value }}</strong>
              <small>{{ item.label }}</small>
            </span>
          </div>
        </button>
      </article>
    </div>
  </section>
</template>

<style scoped>
.situation-bottom-band {
  display: flex;
  flex-direction: column;
  gap: 12px;
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
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
}

.situation-bottom-band__card {
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(118, 168, 240, 0.1);
  border-radius: 20px;
  background: linear-gradient(180deg, rgba(8, 15, 25, 0.8) 0%, rgba(9, 16, 26, 0.68) 100%);
  padding: 12px;
  min-width: 0;
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

.situation-bottom-band__value-line {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
}

.situation-bottom-band__value-line strong {
  font-size: 30px;
  line-height: 1;
}

.situation-bottom-band__value-line strong small {
  font-size: 12px;
}

.situation-bottom-band__value-line span,
.situation-bottom-band__card p,
.situation-bottom-band__highlights small {
  color: rgba(192, 210, 233, 0.68);
}

.situation-bottom-band__card p {
  min-height: 48px;
  margin: 0;
  line-height: 1.55;
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
  font-size: 16px;
}

.situation-bottom-band__card:hover {
  border-color: rgba(137, 190, 255, 0.24);
  box-shadow: 0 14px 28px rgba(2, 8, 15, 0.2);
}

.is-ghost {
  opacity: 0.38;
}

.is-dragging {
  cursor: grabbing;
}

@media (max-width: 1880px) {
  .situation-bottom-band__grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1180px) {
  .situation-bottom-band__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .situation-bottom-band__grid {
    grid-template-columns: 1fr;
  }
}
</style>
