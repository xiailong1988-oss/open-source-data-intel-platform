<script setup lang="ts">
import type { QaAnswer } from '../../types/smartQa'

defineProps<{
  question: string
  answer: QaAnswer | null
  loading: boolean
}>()

defineEmits<{
  (event: 'ask-related', question: string): void
  (event: 'source-click', title: string): void
  (event: 'entity-click', entity: string): void
}>()
</script>

<template>
  <div class="qa-answer-panel">
    <el-skeleton :loading="loading" animated :rows="8">
      <template #template>
        <el-card class="qa-card"><el-skeleton-item variant="h3" style="width: 70%" /></el-card>
      </template>
      <template #default>
        <el-empty v-if="!answer" description="请输入业务问题开始分析" />
        <template v-else>
          <el-card shadow="never" class="qa-card">
            <template #header>
              <div class="qa-card__header">核心回答</div>
            </template>
            <h3 class="qa-question">{{ question }}</h3>
            <p class="qa-text">{{ answer.coreAnswer }}</p>
          </el-card>

          <el-card shadow="never" class="qa-card">
            <template #header>
              <div class="qa-card__header">依据来源</div>
            </template>
            <el-empty v-if="!answer.evidenceSources.length" description="暂无来源信息" :image-size="64" />
            <el-timeline v-else>
              <el-timeline-item v-for="(item, index) in answer.evidenceSources" :key="`${item.title}-${index}`">
                <el-button link class="cell-link" @click="$emit('source-click', item.title)">
                  {{ item.title }}
                </el-button>
                <p>{{ item.source }} · {{ item.publishedAt }}</p>
              </el-timeline-item>
            </el-timeline>
          </el-card>

          <el-card shadow="never" class="qa-card">
            <template #header>
              <div class="qa-card__header">相关实体</div>
            </template>
            <el-empty v-if="!answer.relatedEntities.length" description="暂无实体关联" :image-size="64" />
            <template v-else>
              <el-tag
                v-for="item in answer.relatedEntities"
                :key="item"
                class="qa-tag qa-tag--clickable"
                @click="$emit('entity-click', item)"
              >
                {{ item }}
              </el-tag>
            </template>
          </el-card>

          <el-card shadow="never" class="qa-card">
            <template #header>
              <div class="qa-card__header">相关推荐问题</div>
            </template>
            <el-empty v-if="!answer.recommendations.length" description="暂无推荐问题" :image-size="64" />
            <template v-else>
              <el-button
                v-for="item in answer.recommendations"
                :key="item"
                text
                class="qa-recommend"
                @click="$emit('ask-related', item)"
              >
                {{ item }}
              </el-button>
            </template>
          </el-card>
        </template>
      </template>
    </el-skeleton>
  </div>
</template>

<style scoped>
.qa-answer-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.qa-card {
  border: 1px solid var(--platform-border-subtle);
  background: var(--platform-card-bg-muted);
}

.qa-card__header {
  color: var(--platform-text-primary);
  font-size: 15px;
  font-weight: 600;
}

.qa-question {
  margin: 0;
  color: var(--platform-text-primary);
  font-size: 18px;
}

.qa-text {
  margin: 10px 0 0;
  color: var(--platform-text-secondary);
  line-height: 1.8;
}

.qa-tag {
  margin: 0 8px 8px 0;
}

.qa-tag--clickable {
  cursor: pointer;
}

.qa-recommend,
.cell-link {
  display: block;
  padding: 0;
  color: var(--platform-accent-strong);
}
</style>
