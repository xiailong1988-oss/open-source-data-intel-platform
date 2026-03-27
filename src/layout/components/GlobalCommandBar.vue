<script setup lang="ts">
import { computed } from 'vue'
import { ChatDotRound, Promotion, Search } from '@element-plus/icons-vue'
import type { WorkbenchCommandMode } from '../../types/ui'

/**
 * 顶部统一检索 / 问答入口。
 * 当前先用本地路由联动承接智能入口，后续可无缝替换为统一搜索与问答网关。
 */
const props = defineProps<{
  modelValue: string
  mode: WorkbenchCommandMode
  modes: WorkbenchCommandMode[]
  placeholder: string
  quickQuestions: string[]
  disabled?: boolean
}>()

const emit = defineEmits<{
  (event: 'update:modelValue', value: string): void
  (event: 'update:mode', value: WorkbenchCommandMode): void
  (event: 'submit'): void
  (event: 'pick-question', value: string): void
}>()

const currentIcon = computed(() => (props.mode === '智能问答' ? ChatDotRound : Search))

/**
 * 统一模式切换事件，避免父组件分别对 segmented 的不同值类型做兼容。
 */
const handleModeChange = (value: string | number | boolean) => {
  emit('update:mode', String(value) as WorkbenchCommandMode)
}
</script>

<template>
  <div class="global-command-bar">
    <el-segmented :model-value="mode" :options="modes" class="global-command-bar__mode" @change="handleModeChange" />
    <el-input
      :model-value="modelValue"
      :placeholder="placeholder"
      clearable
      class="global-command-bar__input"
      @update:model-value="emit('update:modelValue', $event)"
      @keyup.enter="emit('submit')"
    >
      <template #prefix>
        <el-icon><component :is="currentIcon" /></el-icon>
      </template>
      <template #append>
        <el-button :disabled="disabled" type="primary" @click="emit('submit')">
          <el-icon><Promotion /></el-icon>
        </el-button>
      </template>
    </el-input>
    <el-popover placement="bottom-end" :width="280" trigger="click">
      <template #reference>
        <el-button text class="global-command-bar__quick-trigger">推荐问题</el-button>
      </template>
      <div class="global-command-bar__quick-list">
        <button
          v-for="question in quickQuestions"
          :key="question"
          type="button"
          class="global-command-bar__quick-item"
          @click="emit('pick-question', question)"
        >
          {{ question }}
        </button>
      </div>
    </el-popover>
  </div>
</template>

<style scoped>
.global-command-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  flex: 1;
  border: 1px solid rgba(124, 156, 194, 0.14);
  border-radius: 16px;
  background: rgba(8, 16, 26, 0.62);
  padding: 6px;
  backdrop-filter: blur(14px);
}

.global-command-bar__mode {
  flex-shrink: 0;
}

.global-command-bar__input {
  min-width: 0;
  flex: 1;
}

.global-command-bar__input :deep(.el-input-group__prepend) {
  box-shadow: none;
}

.global-command-bar__input :deep(.el-input__wrapper) {
  background: transparent;
  box-shadow: none !important;
}

.global-command-bar__input :deep(.el-input-group__append) {
  background: transparent;
  box-shadow: none;
}

.global-command-bar__quick-trigger {
  color: var(--platform-header-text-secondary);
}

.global-command-bar__quick-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.global-command-bar__quick-item {
  border: 1px solid var(--platform-border-subtle);
  border-radius: 12px;
  background: var(--platform-card-bg-muted);
  padding: 10px 12px;
  text-align: left;
  color: var(--platform-text-primary);
  cursor: pointer;
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.global-command-bar__quick-item:hover {
  transform: translateY(-1px);
  border-color: var(--platform-accent);
}

@media (max-width: 1280px) {
  .global-command-bar__quick-trigger {
    display: none;
  }
}

@media (max-width: 1080px) {
  .global-command-bar {
    display: none;
  }
}
</style>
