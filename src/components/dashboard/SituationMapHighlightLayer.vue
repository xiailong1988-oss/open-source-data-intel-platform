<script setup lang="ts">
import { computed } from 'vue'
import type {
  DashboardMapHighlight,
  DashboardMapLayoutSnapshot,
  DashboardCockpitRiskLevel,
  DashboardMapProjectedPoint,
} from '../../types/dashboardCockpit'

const props = defineProps<{
  visible: boolean
  layout: DashboardMapLayoutSnapshot | null
  highlights: DashboardMapHighlight[]
  selectedPointId: string
  hoveredPointId: string
}>()

const emit = defineEmits<{
  (event: 'open-highlight', highlight: DashboardMapHighlight): void
  (event: 'hover-highlight', pointId: string): void
  (event: 'leave-highlight'): void
}>()

const riskClassMap: Record<DashboardCockpitRiskLevel, string> = {
  高: 'is-high',
  中: 'is-medium',
  低: 'is-low',
}

const anchorOffset = {
  top: 84,
  bottom: 86,
}

const groups = computed(() => {
  const top = props.highlights.filter((item) => item.preferredAnchor === 'top')
  const bottom = props.highlights.filter((item) => item.preferredAnchor === 'bottom')

  return { top, bottom }
})

const pointMap = computed(() => {
  const entries: Array<[string, DashboardMapProjectedPoint]> = props.layout?.points.map((item) => [item.pointId, item]) ?? []
  return new Map<string, DashboardMapProjectedPoint>(entries)
})

const getAnchorX = (index: number, total: number) => {
  if (!props.layout) return 0
  return (props.layout.width / (total + 1)) * (index + 1)
}

const resolveLinePath = (highlight: DashboardMapHighlight, index: number, total: number) => {
  const point = pointMap.value.get(highlight.pointId)
  const layout = props.layout
  if (!point || !layout) return ''

  const anchorX = getAnchorX(index, total)
  const anchorY = highlight.preferredAnchor === 'top' ? anchorOffset.top : layout.height - anchorOffset.bottom
  const bendY = highlight.preferredAnchor === 'top' ? anchorY + 30 : anchorY - 30

  return `M ${anchorX} ${anchorY} L ${anchorX} ${bendY} L ${point.x} ${point.y}`
}
</script>

<template>
  <div class="map-highlight-layer">
    <div v-if="visible && layout" class="map-highlight-layer__overlay">
      <svg class="map-highlight-layer__lines" :viewBox="`0 0 ${layout.width} ${layout.height}`" preserveAspectRatio="none">
        <template v-for="(group, anchor) in groups" :key="anchor">
          <path
            v-for="(highlight, index) in group"
            :key="highlight.id"
            class="map-highlight-layer__line"
            :class="[
              riskClassMap[highlight.level],
              {
                'is-active': highlight.pointId === selectedPointId || highlight.pointId === hoveredPointId,
              },
            ]"
            :d="resolveLinePath(highlight, index, group.length)"
          />
        </template>
      </svg>

      <div class="map-highlight-layer__cards map-highlight-layer__cards--top">
        <button
          v-for="(highlight, index) in groups.top"
          :key="highlight.id"
          type="button"
          class="map-highlight-layer__card"
          :class="[
            riskClassMap[highlight.level],
            {
              'is-active': highlight.pointId === selectedPointId || highlight.pointId === hoveredPointId,
            },
          ]"
          :style="{ left: `${((index + 1) / (groups.top.length + 1)) * 100}%` }"
          @mouseenter="emit('hover-highlight', highlight.pointId)"
          @mouseleave="emit('leave-highlight')"
          @click="emit('open-highlight', highlight)"
        >
          <span>{{ highlight.time }} · {{ highlight.area }}</span>
          <strong>{{ highlight.title }}</strong>
          <small>{{ highlight.status }}</small>
        </button>
      </div>

      <div class="map-highlight-layer__cards map-highlight-layer__cards--bottom">
        <button
          v-for="(highlight, index) in groups.bottom"
          :key="highlight.id"
          type="button"
          class="map-highlight-layer__card"
          :class="[
            riskClassMap[highlight.level],
            {
              'is-active': highlight.pointId === selectedPointId || highlight.pointId === hoveredPointId,
            },
          ]"
          :style="{ left: `${((index + 1) / (groups.bottom.length + 1)) * 100}%` }"
          @mouseenter="emit('hover-highlight', highlight.pointId)"
          @mouseleave="emit('leave-highlight')"
          @click="emit('open-highlight', highlight)"
        >
          <span>{{ highlight.time }} · {{ highlight.area }}</span>
          <strong>{{ highlight.title }}</strong>
          <small>{{ highlight.status }}</small>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.map-highlight-layer {
  position: absolute;
  inset: 0;
  z-index: 425;
  pointer-events: none;
}

.map-highlight-layer__overlay {
  position: absolute;
  inset: 0;
}

.map-highlight-layer__lines {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.map-highlight-layer__line {
  fill: none;
  stroke: rgba(149, 198, 255, 0.24);
  stroke-width: 1.4;
  stroke-linecap: round;
  stroke-linejoin: round;
  stroke-dasharray: 4 6;
  opacity: 0.7;
  transition: stroke 0.22s ease, stroke-width 0.22s ease, opacity 0.22s ease;
}

.map-highlight-layer__line.is-high {
  stroke: rgba(255, 118, 118, 0.74);
}

.map-highlight-layer__line.is-medium {
  stroke: rgba(255, 191, 109, 0.62);
}

.map-highlight-layer__line.is-low {
  stroke: rgba(95, 203, 144, 0.58);
}

.map-highlight-layer__line.is-active {
  stroke-width: 2.2;
  opacity: 1;
}

.map-highlight-layer__cards {
  position: absolute;
  left: 0;
  right: 0;
  height: 70px;
  pointer-events: none;
}

.map-highlight-layer__cards--top {
  top: 74px;
}

.map-highlight-layer__cards--bottom {
  bottom: 18px;
}

.map-highlight-layer__card {
  position: absolute;
  width: min(220px, 22vw);
  transform: translateX(-50%);
  border: 1px solid rgba(118, 171, 245, 0.12);
  border-radius: 16px;
  background: rgba(6, 14, 24, 0.72);
  padding: 8px 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  text-align: left;
  color: #f1f7ff;
  cursor: pointer;
  backdrop-filter: blur(14px);
  pointer-events: auto;
  transition: border-color 0.22s ease, box-shadow 0.22s ease, transform 0.22s ease;
}

.map-highlight-layer__card:hover,
.map-highlight-layer__card.is-active {
  transform: translateX(-50%) translateY(-2px);
  border-color: rgba(150, 204, 255, 0.38);
  box-shadow: 0 18px 34px rgba(4, 10, 18, 0.28);
}

.map-highlight-layer__card span,
.map-highlight-layer__card small {
  color: rgba(207, 224, 245, 0.72);
  font-size: 11px;
}

.map-highlight-layer__card strong {
  font-size: 13px;
  line-height: 1.45;
}

.map-highlight-layer__card.is-high {
  box-shadow: inset 0 0 0 1px rgba(255, 108, 108, 0.1);
}

.map-highlight-layer__card.is-medium {
  box-shadow: inset 0 0 0 1px rgba(255, 190, 109, 0.1);
}

.map-highlight-layer__card.is-low {
  box-shadow: inset 0 0 0 1px rgba(94, 201, 143, 0.1);
}

@media (max-width: 1440px) {
  .map-highlight-layer__card {
    width: min(188px, 21vw);
  }
}

@media (max-width: 1180px) {
  .map-highlight-layer {
    display: none;
  }
}
</style>
