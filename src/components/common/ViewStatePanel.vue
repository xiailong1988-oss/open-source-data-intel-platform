<script setup lang="ts">
import { computed } from 'vue'
import { CircleCloseFilled, DocumentRemove, Loading, WarningFilled } from '@element-plus/icons-vue'

type ViewState = 'loading' | 'empty' | 'error'

/**
 * 统一承载加载态、空态和异常态，减少各页面散落的 `el-empty`、文案和重试按钮写法。
 */
const props = withDefaults(
  defineProps<{
    state: ViewState
    title?: string
    description?: string
    updatedAt?: string
    retryText?: string
    compact?: boolean
  }>(),
  {
    title: '',
    description: '',
    updatedAt: '',
    retryText: '重试',
    compact: false,
  },
)

defineEmits<{
  (event: 'retry'): void
}>()

const stateMeta = computed(() => {
  if (props.state === 'loading') {
    return {
      icon: Loading,
      title: props.title || '正在加载数据',
      description: props.description || '正在同步页面数据，请稍候。',
    }
  }

  if (props.state === 'error') {
    return {
      icon: CircleCloseFilled,
      title: props.title || '数据加载失败',
      description: props.description || '当前模块暂时无法返回数据，可稍后重试。',
    }
  }

  return {
    icon: DocumentRemove,
    title: props.title || '暂无可展示内容',
    description: props.description || '当前筛选条件下没有可展示的数据。',
  }
})

const stateClass = computed(() => `view-state-panel--${props.state}`)
</script>

<template>
  <section class="view-state-panel" :class="[stateClass, { 'view-state-panel--compact': compact }]">
    <div class="view-state-panel__icon">
      <el-icon :size="compact ? 20 : 24" :class="{ 'is-rotating': state === 'loading' }">
        <component :is="stateMeta.icon" />
      </el-icon>
    </div>
    <div class="view-state-panel__content">
      <strong>{{ stateMeta.title }}</strong>
      <p>{{ stateMeta.description }}</p>
      <div v-if="updatedAt" class="view-state-panel__meta">
        <el-icon><WarningFilled /></el-icon>
        <span>最近刷新：{{ updatedAt }}</span>
      </div>
    </div>
    <div v-if="$slots.actions || state === 'error'" class="view-state-panel__actions">
      <slot name="actions">
        <el-button v-if="state === 'error'" type="primary" plain @click="$emit('retry')">{{ retryText }}</el-button>
      </slot>
    </div>
  </section>
</template>

<style scoped>
.view-state-panel {
  display: flex;
  align-items: center;
  gap: 14px;
  min-height: 136px;
  border: 1px dashed var(--platform-border-subtle);
  border-radius: 18px;
  background: linear-gradient(180deg, var(--platform-card-bg-muted) 0%, var(--platform-card-bg) 100%);
  padding: 22px;
}

.view-state-panel--compact {
  min-height: 112px;
  padding: 18px;
}

.view-state-panel__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 14px;
  flex-shrink: 0;
}

.view-state-panel__content {
  min-width: 0;
  flex: 1;
}

.view-state-panel__content strong {
  display: block;
  color: var(--platform-text-primary);
  font-size: 15px;
}

.view-state-panel__content p {
  margin: 8px 0 0;
  color: var(--platform-text-secondary);
  font-size: 13px;
  line-height: 1.7;
}

.view-state-panel__meta {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
  color: var(--platform-text-tertiary);
  font-size: 12px;
}

.view-state-panel__actions {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.view-state-panel--loading .view-state-panel__icon {
  background: rgba(54, 120, 217, 0.12);
  color: var(--platform-accent);
}

.view-state-panel--empty .view-state-panel__icon {
  background: rgba(145, 160, 178, 0.14);
  color: var(--platform-text-secondary);
}

.view-state-panel--error .view-state-panel__icon {
  background: rgba(224, 82, 82, 0.12);
  color: var(--platform-risk-high);
}

.is-rotating {
  animation: state-spin 1s linear infinite;
}

@keyframes state-spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 900px) {
  .view-state-panel {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
