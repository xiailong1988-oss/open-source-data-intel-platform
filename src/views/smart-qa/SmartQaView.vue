<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import QaAnswerPanel from '../../components/qa/QaAnswerPanel.vue'
import { smartQaFallbackAnswer, smartQaHistoryMock, smartQaSuggestedQuestions } from '../../mock/smartQa'
import type { QaAnswer, QaHistoryItem } from '../../types/smartQa'

const route = useRoute()
const router = useRouter()

const historyList = ref<QaHistoryItem[]>(smartQaHistoryMock.map((item) => ({ ...item })))
const activeQuestionId = ref(historyList.value[0]?.id ?? '')
const currentAnswer = ref<QaAnswer | null>(historyList.value[0]?.answer ?? null)
const inputQuestion = ref(historyList.value[0]?.question ?? '')
const generating = ref(false)

const activeHistory = computed(() => historyList.value.find((item) => item.id === activeQuestionId.value))
const displayQuestion = computed(() => activeHistory.value?.question ?? inputQuestion.value)

// 智能问答页首次进入时，必须确保默认历史问题和默认回答立即可见。
// 该函数用于兜底恢复首条问答，避免页面首次挂载时出现空白回答区。
const ensureInitialState = () => {
  if (!historyList.value.length) {
    activeQuestionId.value = ''
    currentAnswer.value = null
    return
  }

  if (!activeQuestionId.value) {
    activeQuestionId.value = historyList.value[0].id
  }

  const matched = historyList.value.find((item) => item.id === activeQuestionId.value) ?? historyList.value[0]
  currentAnswer.value = matched.answer

  if (!inputQuestion.value) {
    inputQuestion.value = matched.question
  }
}

const switchHistory = (item: QaHistoryItem) => {
  if (generating.value) {
    return
  }

  activeQuestionId.value = item.id
  currentAnswer.value = item.answer
  inputQuestion.value = item.question
}

const formatNow = () => new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')

const upsertHistory = (question: string, answer: QaAnswer) => {
  const now = formatNow()
  const existingIndex = historyList.value.findIndex((item) => item.question === question)

  if (existingIndex > -1) {
    const existing = historyList.value[existingIndex]
    const updatedItem: QaHistoryItem = {
      ...existing,
      updatedAt: now,
      answer,
    }

    historyList.value.splice(existingIndex, 1)
    historyList.value.unshift(updatedItem)
    return updatedItem
  }

  const newItem: QaHistoryItem = {
    id: `QA-${Math.floor(Math.random() * 9000) + 1000}`,
    question,
    updatedAt: now,
    answer,
  }

  historyList.value.unshift(newItem)
  return newItem
}

const askQuestion = async (question: string) => {
  const text = question.trim()
  if (!text) {
    ElMessage.warning('请输入业务问题')
    return
  }

  if (generating.value) {
    ElMessage.info('正在分析中，请稍候')
    return
  }

  inputQuestion.value = text
  generating.value = true
  currentAnswer.value = null
  await new Promise((resolve) => setTimeout(resolve, 1200))

  const matchedHistory = smartQaHistoryMock.find((item) => item.question.includes(text) || text.includes(item.question))
  const answer = matchedHistory?.answer ?? smartQaFallbackAnswer(text)
  const historyItem = upsertHistory(text, answer)

  activeQuestionId.value = historyItem.id
  currentAnswer.value = answer
  generating.value = false

  ElMessage.success('问答结果已生成（mock）')
}

const submitQuestion = () => askQuestion(inputQuestion.value)

const clearInput = () => {
  inputQuestion.value = ''
}

const toContent = (title: string) => {
  router.push({ path: '/data-governance/standardized-list', query: { keyword: title, autoOpen: 'first' } })
}

const toEntity = (entityName: string) => {
  router.push({ path: '/knowledge-build/entity-management', query: { keyword: entityName, autoOpen: 'first' } })
}

const applyRouteQuery = async () => {
  // 问答页既支持普通菜单进入，也支持从首页/检索等页面带着 query 跳入。
  // 无 query 时恢复默认问答；有 query 时则执行预填或自动提问。
  const question = String(route.query.question ?? route.query.keyword ?? '')
  const autoAsk = route.query.autoAsk === 'true' || route.query.ask === '1'

  if (!question) {
    ensureInitialState()
    return
  }

  inputQuestion.value = question

  if (autoAsk) {
    await askQuestion(question)
    return
  }

  const existing = historyList.value.find((item) => item.question.includes(question) || question.includes(item.question))
  if (existing) {
    switchHistory(existing)
  }
}

watch(() => route.query, () => {
  void applyRouteQuery()
}, { immediate: true })

watch(
  () => [historyList.value.length, activeQuestionId.value] as const,
  () => {
    // 当历史记录或当前激活项变化后，如果回答被清空且页面不在生成中，
    // 再次兜底恢复默认状态，避免首次打开或异常切换时出现空白。
    if (!currentAnswer.value && !generating.value) {
      ensureInitialState()
    }
  },
)

onMounted(() => {
  ensureInitialState()
})
</script>

<template>
  <section class="smart-qa platform-page-shell">
    <el-card shadow="never" class="qa-page-header-card">
      <div class="qa-page-header platform-page-hero__header">
        <div class="platform-page-heading">
          <h2>智能问答助手</h2>
          <p>基于知识库和检索结果生成业务答案，辅助研判与决策。</p>
        </div>
        <div class="qa-page-header__actions platform-page-hero__meta">
          <el-tag :type="generating ? 'warning' : 'success'">{{ generating ? '分析中/生成中' : '知识增强问答（Mock）' }}</el-tag>
          <el-button link @click="clearInput">清空输入</el-button>
        </div>
      </div>
    </el-card>

    <div class="qa-shell__layout">
      <aside class="qa-shell__sidebar">
        <el-card shadow="never" class="history-card">
          <template #header>
            <div class="history-card__header">
              <span>历史问题</span>
              <small>共 {{ historyList.length }} 条</small>
            </div>
          </template>

          <el-empty v-if="!historyList.length" description="暂无历史问题" :image-size="76" />
          <el-scrollbar v-else class="history-scroll">
            <div class="history-list">
              <button
                v-for="item in historyList"
                :key="item.id"
                type="button"
                class="history-item"
                :class="{ 'is-active': item.id === activeQuestionId }"
                :disabled="generating"
                @click="switchHistory(item)"
              >
                <strong>{{ item.question }}</strong>
                <span>{{ item.updatedAt }}</span>
              </button>
            </div>
          </el-scrollbar>
        </el-card>
      </aside>

      <div class="qa-shell__main">
        <el-card shadow="never" class="qa-main-card">
          <div class="suggestion-row">
            <span>推荐提问：</span>
            <el-button
              v-for="item in smartQaSuggestedQuestions"
              :key="item"
              text
              :disabled="generating"
              @click="askQuestion(item)"
            >
              {{ item }}
            </el-button>
          </div>

          <QaAnswerPanel
            :question="displayQuestion"
            :answer="currentAnswer"
            :loading="generating"
            @ask-related="askQuestion"
            @source-click="toContent"
            @entity-click="toEntity"
          />

          <el-card shadow="never" class="qa-input-card">
            <el-input
              v-model="inputQuestion"
              type="textarea"
              :rows="3"
              placeholder="请输入业务问题，例如：当前有哪些高风险预警？"
              @keyup.ctrl.enter="submitQuestion"
            />
            <div class="qa-input-actions">
              <span>按 Ctrl + Enter 可快速发送</span>
              <el-button type="primary" :loading="generating" @click="submitQuestion">
                {{ generating ? '分析中/生成中...' : '提交问题' }}
              </el-button>
            </div>
          </el-card>
        </el-card>
      </div>
    </div>
  </section>
</template>

<style scoped>
.smart-qa {
  min-height: 100%;
}

.qa-page-header-card,
.history-card,
.qa-main-card,
.qa-input-card {
  border: 1px solid var(--platform-border-subtle);
}

.qa-page-header {
  gap: 14px;
}

.qa-page-header p {
  margin: 6px 0 0;
  color: var(--platform-text-secondary);
  font-size: 12px;
}

.qa-page-header__actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.qa-shell__layout {
  display: grid;
  grid-template-columns: minmax(300px, 360px) minmax(0, 1fr);
  gap: var(--platform-section-gap);
  align-items: start;
}

.qa-shell__sidebar,
.qa-shell__main {
  min-width: 0;
}

.qa-shell__sidebar {
  position: sticky;
  top: calc(var(--platform-header-height) + 18px);
}

.history-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: var(--platform-text-primary);
  font-size: 14px;
  font-weight: 600;
}

.history-card__header small {
  color: var(--platform-text-tertiary);
  font-weight: 400;
  font-size: 11px;
}

.history-scroll {
  max-height: min(980px, calc(100vh - 280px));
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.history-item {
  border: 1px solid rgba(112, 145, 184, 0.16);
  border-radius: 14px;
  background: rgba(14, 25, 39, 0.82);
  padding: 10px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}

.history-item:hover {
  transform: translateY(-1px);
  border-color: var(--platform-border-strong);
  background: rgba(19, 33, 51, 0.9);
}

.history-item:disabled {
  cursor: not-allowed;
  opacity: 0.72;
}

.history-item strong {
  display: block;
  color: var(--platform-text-primary);
  line-height: 1.4;
}

.history-item span {
  display: block;
  margin-top: 6px;
  color: var(--platform-text-tertiary);
  font-size: 11px;
}

.history-item.is-active {
  border-color: var(--platform-border-strong);
  background: rgba(22, 39, 61, 0.94);
}

.suggestion-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
  color: var(--platform-text-secondary);
  font-size: 12px;
}

.qa-input-card {
  margin-top: 10px;
}

.qa-input-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
  color: var(--platform-text-tertiary);
  font-size: 11px;
}

@media (max-width: 900px) {
  .qa-shell__layout {
    grid-template-columns: 1fr;
  }

  .qa-shell__sidebar {
    position: static;
  }
}
</style>
