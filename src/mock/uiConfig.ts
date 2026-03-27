import type { WorkbenchUiConfig } from '../types/ui'

export const workbenchUiConfigMock: WorkbenchUiConfig = {
  commandModes: ['智能检索', '智能问答'],
  topics: [
    {
      code: 'all',
      label: '全域视角',
      description: '查看全平台综合态势、事件和告警线索。',
    },
    {
      code: 'economy',
      label: '经济专题',
      description: '聚焦产业链变化、重点项目和经济相关态势。',
    },
    {
      code: 'innovation',
      label: '创新专题',
      description: '聚焦科研机构、创新项目和技术热点。',
    },
  ],
  quickQuestions: [
    '今天有哪些高优先级告警？',
    '科创核心区近期有哪些热点事件？',
    '帮我看一下产业链波动相关线索。',
  ],
  placeholders: {
    智能检索: '输入情报线索、区域或对象名称，快速进入检索结果',
    智能问答: '输入业务问题，直接发起情报问答',
  },
}
