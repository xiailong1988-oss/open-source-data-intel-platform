<script setup lang="ts">
import type { SearchMode, SearchResultItem } from '../../types/smartSearch'

defineProps<{
  item: SearchResultItem
  highlightedTitle: string
  highlightedSummary: string
  mode: SearchMode
}>()

defineEmits<{
  (event: 'detail', id: string): void
  (event: 'pick-tag', tag: string): void
  (event: 'pick-source', source: string): void
  (event: 'pick-entity', entity: string): void
}>()
</script>

<template>
  <el-card shadow="hover" class="result-card">
    <div class="result-card__head">
      <el-button link class="result-card__title-link" @click="$emit('detail', item.id)">
        <h3 class="result-card__title" v-html="highlightedTitle" />
      </el-button>
      <el-tag size="small" :type="item.riskLevel === '高' ? 'danger' : item.riskLevel === '中' ? 'warning' : 'info'">
        {{ item.riskLevel }}风险
      </el-tag>
    </div>
    <p class="result-card__summary" v-html="highlightedSummary" />

    <div class="result-card__meta">
      <el-button link class="result-card__meta-link" @click="$emit('pick-source', item.source)">来源：{{ item.source }}</el-button>
      <span>数据类型：{{ item.dataType }}</span>
      <span>地域：{{ item.region }}</span>
      <span>时间：{{ item.time }}</span>
    </div>
    <div v-if="mode === '语义检索'" class="result-card__semantic">
      <span>语义线索：</span>
      <el-tag v-for="hint in item.semanticHints" :key="hint" size="small" effect="plain" class="result-card__hint">
        {{ hint }}
      </el-tag>
    </div>

    <div class="result-card__foot">
      <div class="result-card__tags">
        <el-tag
          v-for="tag in item.tags"
          :key="tag"
          size="small"
          effect="plain"
          class="result-card__tag-clickable"
          @click="$emit('pick-tag', tag)"
        >
          {{ tag }}
        </el-tag>
        <el-tag
          v-for="entity in item.relatedEntities ?? []"
          :key="`${item.id}-${entity}`"
          size="small"
          type="info"
          effect="plain"
          class="result-card__tag-clickable"
          @click="$emit('pick-entity', entity)"
        >
          {{ entity }}
        </el-tag>
      </div>
      <el-button type="primary" link @click="$emit('detail', item.id)">查看详情</el-button>
    </div>
  </el-card>
</template>

<style scoped>
.result-card {
  border: 1px solid var(--platform-border-subtle);
  background: var(--platform-card-bg-muted);
}

.result-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.result-card__title-link {
  display: block;
  padding: 0;
}

.result-card__title {
  margin: 0;
  color: var(--platform-text-primary);
  font-size: 18px;
  text-align: left;
}

.result-card__summary {
  margin: 12px 0 0;
  color: var(--platform-text-secondary);
  line-height: 1.8;
}

.result-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 10px;
  color: var(--platform-text-tertiary);
  font-size: 12px;
}

.result-card__meta-link {
  padding: 0;
  color: var(--platform-accent-strong);
  font-size: 12px;
}

.result-card__foot {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 12px;
}

.result-card__semantic {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
  color: var(--platform-text-tertiary);
  font-size: 12px;
}

.result-card__hint {
  margin: 0;
}

.result-card__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.result-card__tag-clickable {
  cursor: pointer;
}

.result-card :deep(mark.search-hit) {
  border-radius: 4px;
  background: rgba(255, 188, 108, 0.24);
  padding: 0 2px;
  color: #ffd9a6;
}
</style>
