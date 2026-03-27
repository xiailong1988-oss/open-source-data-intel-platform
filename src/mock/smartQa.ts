import type { QaAnswer, QaHistoryItem } from '../types/smartQa'

export const smartQaHistoryMock: QaHistoryItem[] = [
  {
    id: 'QA-9001',
    question: '最近有哪些热点事件？',
    updatedAt: '2026-03-16 09:15',
    answer: {
      coreAnswer:
        '近 24 小时热点主要集中在“重点产业链供给波动”“核心商圈客流异动”和“突发舆情传播加速”三类事件，其中产业链和舆情事件风险等级较高，建议优先关注处置时效。',
      evidenceSources: [
        { title: '重点产业链供给波动风险分析简报', source: '产业监测平台', publishedAt: '2026-03-16 08:45' },
        { title: '核心商圈夜间客流研判', source: '城市感知网关', publishedAt: '2026-03-16 08:12' },
        { title: '突发舆情传播链路加速预警记录', source: '互联网公开数据', publishedAt: '2026-03-16 07:58' },
      ],
      relatedEntities: ['华新智能制造有限公司', '中心商圈', '舆情平台节点A'],
      recommendations: ['高风险事件建议 2 小时内复核', '持续跟踪热点扩散链路', '联动报告中心生成简报'],
    },
  },
  {
    id: 'QA-9002',
    question: '某地区近期数据增长趋势如何？',
    updatedAt: '2026-03-16 08:40',
    answer: {
      coreAnswer:
        '高新区近 7 日数据总量保持上升，日均增幅约 4.8%，其中产业监测和设备时序数据增长明显。异常数据量总体下降，但在设备告警类数据上仍有波动。',
      evidenceSources: [
        { title: '数据增长趋势看板', source: '综合态势看板', publishedAt: '2026-03-16 08:00' },
        { title: '园区设备告警异常增长日志', source: '城市感知网关', publishedAt: '2026-03-15 23:16' },
      ],
      relatedEntities: ['高新区', '园区设备时序库', '产业链监测平台'],
      recommendations: ['关注告警数据峰值时段', '优化高新区数据采集频率', '生成区域趋势周报'],
    },
  },
]

export const smartQaSuggestedQuestions = [
  '最近有哪些热点事件？',
  '某地区近期数据增长趋势如何？',
  '某机构关联了哪些事件？',
  '当前有哪些高风险预警？',
]

export const smartQaFallbackAnswer = (question: string): QaAnswer => ({
  coreAnswer: `已基于知识库与检索结果完成分析：针对“${question}”，当前可确认存在结构化证据支撑，但建议结合最新预警和报告进行复核。`,
  evidenceSources: [
    { title: '综合态势看板数据快照', source: '平台看板', publishedAt: '2026-03-16 09:20' },
    { title: '分析预警记录汇总', source: '预警中心', publishedAt: '2026-03-16 09:18' },
  ],
  relatedEntities: ['综合研判中心', '风险预警引擎', '报告中心'],
  recommendations: ['补充限定时间范围后再次提问', '结合“分析预警”模块交叉验证', '一键生成专题报告（mock）'],
})
