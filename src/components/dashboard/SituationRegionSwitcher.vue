<script setup lang="ts">
import { computed } from 'vue'
import type { DashboardCockpitRegion } from '../../types/dashboardCockpit'

const props = withDefaults(
  defineProps<{
    regions: DashboardCockpitRegion[]
    activeRegionId: string
    latestUpdate: string
    overlay?: boolean
    showHeader?: boolean
  }>(),
  {
    overlay: false,
    showHeader: true,
  },
)

const emit = defineEmits<{
  (event: 'select-region', regionId: string): void
}>()

const activeRegion = computed(
  () => props.regions.find((region) => region.id === props.activeRegionId) ?? props.regions[0],
)
</script>

<template>
  <section class="situation-region-switcher" :class="{ 'is-overlay': overlay }">
    <header v-if="showHeader" class="situation-region-switcher__head">
      <div class="situation-region-switcher__title">
        <strong>区域切换态势</strong>
        <small>{{ activeRegion?.description }}</small>
      </div>
      <div class="situation-region-switcher__meta">
        <span>{{ activeRegion?.emphasis }}</span>
        <span>最近更新 {{ latestUpdate }}</span>
      </div>
    </header>

    <div class="situation-region-switcher__chips">
      <button
        v-for="region in regions"
        :key="region.id"
        type="button"
        class="situation-region-switcher__chip"
        :class="{ 'is-active': region.id === activeRegionId }"
        @click="emit('select-region', region.id)"
      >
        <span>{{ region.type === 'district' ? '全域' : '重点地区' }}</span>
        <strong>{{ region.name }}</strong>
      </button>
    </div>
  </section>
</template>

<style scoped>
.situation-region-switcher {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 8px;
}

.situation-region-switcher.is-overlay {
  max-width: min(100%, 540px);
  gap: 0;
  border: 1px solid rgba(118, 171, 246, 0.08);
  border-radius: 14px;
  background: linear-gradient(180deg, rgba(7, 14, 23, 0.34) 0%, rgba(7, 14, 23, 0.18) 100%);
  padding: 6px 8px;
  backdrop-filter: blur(10px);
  box-shadow: 0 12px 24px rgba(2, 8, 15, 0.14);
}

.situation-region-switcher__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
}

.situation-region-switcher__title,
.situation-region-switcher__meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.situation-region-switcher__title strong {
  color: #eef6ff;
  font-size: 18px;
  line-height: 1.1;
}

.situation-region-switcher__title small,
.situation-region-switcher__meta span {
  color: rgba(203, 219, 238, 0.74);
  font-size: 12px;
  line-height: 1.5;
}

.situation-region-switcher__meta {
  align-items: flex-end;
}

.situation-region-switcher__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.situation-region-switcher.is-overlay .situation-region-switcher__chips {
  flex-wrap: nowrap;
  overflow-x: auto;
  scrollbar-width: none;
}

.situation-region-switcher.is-overlay .situation-region-switcher__chips::-webkit-scrollbar {
  display: none;
}

.situation-region-switcher__chip {
  display: inline-flex;
  min-width: 132px;
  flex-direction: column;
  gap: 5px;
  border: 1px solid rgba(110, 161, 236, 0.08);
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(10, 18, 28, 0.56) 0%, rgba(9, 18, 30, 0.32) 100%);
  padding: 10px 14px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.22s ease, border-color 0.22s ease, box-shadow 0.22s ease, background 0.22s ease;
}

.situation-region-switcher.is-overlay .situation-region-switcher__chip {
  min-width: auto;
  min-height: 30px;
  align-items: center;
  justify-content: center;
  gap: 0;
  padding: 0 10px;
  border-radius: 12px;
  background: rgba(10, 18, 28, 0.16);
}

.situation-region-switcher__chip span {
  color: rgba(151, 191, 242, 0.72);
  font-size: 10px;
}

.situation-region-switcher.is-overlay .situation-region-switcher__chip span {
  display: none;
}

.situation-region-switcher__chip strong {
  color: #eff6ff;
  font-size: 14px;
  line-height: 1.2;
}

.situation-region-switcher.is-overlay .situation-region-switcher__chip strong {
  font-size: 12px;
  white-space: nowrap;
}

.situation-region-switcher__chip:hover,
.situation-region-switcher__chip.is-active {
  transform: translateY(-1px);
  border-color: rgba(124, 188, 255, 0.22);
  background: linear-gradient(180deg, rgba(16, 32, 49, 0.54) 0%, rgba(10, 19, 31, 0.36) 100%);
  box-shadow: 0 10px 20px rgba(3, 10, 19, 0.16);
}

.situation-region-switcher__chip.is-active {
  box-shadow:
    inset 0 0 0 1px rgba(118, 216, 255, 0.12),
    0 10px 20px rgba(3, 10, 19, 0.2);
}

@media (max-width: 1180px) {
  .situation-region-switcher__head {
    flex-direction: column;
  }

  .situation-region-switcher__meta {
    align-items: flex-start;
  }

  .situation-region-switcher.is-overlay {
    max-width: 100%;
  }
}
</style>
