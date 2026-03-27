import type { ReportDetail, ReportListItem } from '../types/reportCenter'

export const reportListMock: ReportListItem[] = [
  {
    id: 'RP-202603-301',
    name: '重点产业链运行风险周报',
    type: '周报',
    generatedAt: '2026-03-16 08:40',
    creator: '分析一室',
    status: '已生成',
  },
  {
    id: 'RP-202603-302',
    name: '中心城区客流态势快报',
    type: '快报',
    generatedAt: '2026-03-16 08:05',
    creator: '城市运行组',
    status: '已生成',
  },
  {
    id: 'RP-202603-303',
    name: '突发舆情传播链专题报告',
    type: '专题报告',
    generatedAt: '2026-03-16 07:30',
    creator: '情报研判中心',
    status: '待审核',
  },
  {
    id: 'RP-202603-304',
    name: '综合预警态势日报',
    type: '日报',
    generatedAt: '2026-03-16 06:50',
    creator: '值班分析组',
    status: '生成中',
  },
]

export const reportDetailMockMap: Record<string, ReportDetail> = {
  'RP-202603-301': {
    id: 'RP-202603-301',
    title: '重点产业链运行风险周报',
    type: '周报',
    generatedAt: '2026-03-16 08:40',
    creator: '分析一室',
    version: 'v1.3',
    summary:
      '本报告基于产业链监测、预警记录与知识图谱关联分析生成。过去7日产业链整体可控，但在关键零部件供给、库存安全边际与舆情扰动方面存在阶段性风险。',
    coreFindings: [
      '中上游交付时效连续下降，供给压力抬升。',
      '核心企业库存周转天数上升，影响重点项目排产。',
      '舆情主题与产业事件耦合增强，需防范次生风险。',
    ],
    trendData: [
      { date: '03-10', value: 72 },
      { date: '03-11', value: 74 },
      { date: '03-12', value: 76 },
      { date: '03-13', value: 79 },
      { date: '03-14', value: 81 },
      { date: '03-15', value: 84 },
      { date: '03-16', value: 86 },
    ],
    keyEvents: [
      { eventName: '重点产业链供给波动', level: '高', impact: '影响交付时效', status: '持续跟踪' },
      { eventName: '核心节点库存预警', level: '中', impact: '影响排产节奏', status: '处理中' },
      { eventName: '舆情传播加速', level: '高', impact: '影响市场预期', status: '待复核' },
    ],
    riskJudgment: '综合判断为中高风险偏高，建议48小时内持续跟踪并联动处置。',
    suggestions: ['建立重点企业日度监测清单', '评估替代供应链方案', '联动舆情与预警中心统一口径'],
    relatedEntities: ['华新智能制造有限公司', '高新区产业园', '综合研判中心'],
    referencedContents: ['重点产业链供给波动风险分析简报', '企业产能监测快报', '预警协同处置工单'],
  },
  'RP-202603-302': {
    id: 'RP-202603-302',
    title: '中心城区客流态势快报',
    type: '快报',
    generatedAt: '2026-03-16 08:05',
    creator: '城市运行组',
    version: 'v1.1',
    summary: '核心商圈夜间客流高于历史均值，重点时段与活动节点重合，建议加强人流疏导。',
    coreFindings: ['客流峰值高于历史均值18%', '换乘枢纽压力上升', '重点点位告警触发频次增加'],
    trendData: [
      { date: '03-10', value: 58 },
      { date: '03-11', value: 61 },
      { date: '03-12', value: 60 },
      { date: '03-13', value: 64 },
      { date: '03-14', value: 66 },
      { date: '03-15', value: 71 },
      { date: '03-16', value: 75 },
    ],
    keyEvents: [
      { eventName: '夜间客流密度异常', level: '中', impact: '局部拥堵风险', status: '处理中' },
      { eventName: '商圈活动叠加', level: '低', impact: '客流短时提升', status: '已处置' },
    ],
    riskJudgment: '当前风险可控，需保持高峰时段分流与现场秩序维护。',
    suggestions: ['热点区域增设动态引导', '提升实时客流监测频率', '形成节假日专项预案'],
    relatedEntities: ['中心商圈', '城市感知网关'],
    referencedContents: ['中心城区夜间客流异常增长专题研判', '客流监测日报'],
  },
}
