import type {
  RiskDistributionItem,
  WarningMetric,
  WarningOverviewRecord,
  WarningRecordItem,
  WarningRuleItem,
  WarningTopicRankItem,
  WarningTrendPoint,
} from '../types/analysisWarning'

export const warningMetricsMock: WarningMetric[] = [
  { id: 'todayWarnings', label: '今日告警数', value: '56', changeText: '较昨日 +8.7%' },
  { id: 'highRiskEvents', label: '高风险事件数', value: '14', changeText: '较昨日 +2' },
  { id: 'hotTopics', label: '热点专题数', value: '19', changeText: '较昨日 +3' },
  { id: 'abnormalGrowth', label: '异常增长指标', value: '1.36', changeText: '较昨日 -0.12' },
]

export const warningTrendMock: WarningTrendPoint[] = [
  { date: '03-10', warningCount: 41, highRiskCount: 9, abnormalGrowthIndex: 1.12 },
  { date: '03-11', warningCount: 44, highRiskCount: 10, abnormalGrowthIndex: 1.18 },
  { date: '03-12', warningCount: 46, highRiskCount: 11, abnormalGrowthIndex: 1.21 },
  { date: '03-13', warningCount: 49, highRiskCount: 12, abnormalGrowthIndex: 1.27 },
  { date: '03-14', warningCount: 52, highRiskCount: 12, abnormalGrowthIndex: 1.31 },
  { date: '03-15', warningCount: 54, highRiskCount: 13, abnormalGrowthIndex: 1.35 },
  { date: '03-16', warningCount: 56, highRiskCount: 14, abnormalGrowthIndex: 1.36 },
]

export const warningTopicRankingMock: WarningTopicRankItem[] = [
  { topic: '产业链供给波动', heat: 96, riskLevel: '高' },
  { topic: '舆情传播加速', heat: 91, riskLevel: '高' },
  { topic: '核心商圈客流异动', heat: 87, riskLevel: '中' },
  { topic: '设备告警异常增长', heat: 82, riskLevel: '中' },
  { topic: '民生诉求聚集', heat: 76, riskLevel: '低' },
]

export const warningRiskDistributionMock: RiskDistributionItem[] = [
  { level: '高', value: 26 },
  { level: '中', value: 44 },
  { level: '低', value: 30 },
]

export const warningOverviewRecordsMock: WarningOverviewRecord[] = [
  {
    id: 'WR-1001',
    title: '重点产业链供给异常波动',
    level: '高',
    sourceObject: '产业监测平台',
    triggerRule: '热度突增触发',
    time: '2026-03-16 09:08',
    status: '处理中',
  },
  {
    id: 'WR-1002',
    title: '敏感舆情跨平台扩散',
    level: '高',
    sourceObject: '互联网公开数据',
    triggerRule: '敏感事件触发',
    time: '2026-03-16 08:52',
    status: '待处理',
  },
  {
    id: 'WR-1003',
    title: '中心城区夜间客流密度异常',
    level: '中',
    sourceObject: '城市感知网关',
    triggerRule: '地区异常触发',
    time: '2026-03-16 08:28',
    status: '处理中',
  },
  {
    id: 'WR-1004',
    title: '园区设备告警频次超阈值',
    level: '中',
    sourceObject: '设备时序库',
    triggerRule: '关键词触发',
    time: '2026-03-16 07:56',
    status: '已处理',
  },
]

export const warningRulesMock: WarningRuleItem[] = [
  {
    id: 'RULE-2001',
    name: '产业链热度突增预警',
    ruleType: '热度突增触发',
    triggerMethod: '30 分钟热度涨幅 > 35%',
    alertLevel: '高',
    status: '启用',
    updatedAt: '2026-03-15 18:40',
    description: '监测产业链主题热度短时突增并触发高等级告警。',
  },
  {
    id: 'RULE-2002',
    name: '敏感舆情关键词联动',
    ruleType: '关键词触发',
    triggerMethod: '命中关键词组 + 情绪负向阈值',
    alertLevel: '高',
    status: '启用',
    updatedAt: '2026-03-14 21:12',
    description: '针对敏感关键词组合及负向情绪进行联动预警。',
  },
  {
    id: 'RULE-2003',
    name: '重点区域客流波动告警',
    ruleType: '地区异常触发',
    triggerMethod: '客流偏离均值 > 2.2σ',
    alertLevel: '中',
    status: '启用',
    updatedAt: '2026-03-13 10:25',
    description: '识别重点区域客流异常聚集与波动。',
  },
  {
    id: 'RULE-2004',
    name: '突发事件敏感信号',
    ruleType: '敏感事件触发',
    triggerMethod: '事件风险词 + 传播链扩散',
    alertLevel: '高',
    status: '停用',
    updatedAt: '2026-03-12 17:58',
    description: '用于突发事件的敏感信号早期识别。',
  },
]

export const warningRecordMock: WarningRecordItem[] = [
  {
    id: 'AR-3001',
    title: '重点产业链供给异常波动',
    level: '高',
    sourceObject: '产业监测平台',
    triggerRule: '产业链热度突增预警',
    time: '2026-03-16 09:08',
    status: '处理中',
    description: '供应链关键节点库存下降并伴随交付周期拉长。',
    triggerReason: '近 60 分钟内“供给波动”主题热度上升 43%，触发阈值。',
    relatedContents: ['重点产业链供给波动风险分析简报', '企业产能监测快报', '预警协同处置工单'],
    suggestion: '建议启动跨部门会商机制，确认替代供应链与库存调拨方案。',
    timeline: [
      { time: '2026-03-16 09:08', content: '规则引擎触发高风险告警' },
      { time: '2026-03-16 09:14', content: '分析组完成初步研判' },
      { time: '2026-03-16 09:21', content: '进入联动处置流程' },
    ],
  },
  {
    id: 'AR-3002',
    title: '敏感舆情跨平台扩散',
    level: '高',
    sourceObject: '互联网公开数据',
    triggerRule: '敏感舆情关键词联动',
    time: '2026-03-16 08:52',
    status: '待处理',
    description: '敏感话题在多平台出现二次扩散趋势。',
    triggerReason: '关键词命中率和负向情绪指数同时超过阈值。',
    relatedContents: ['突发舆情传播链路加速预警记录', '舆情态势日分析'],
    suggestion: '建议发布统一口径说明并持续跟踪传播节点。',
    timeline: [
      { time: '2026-03-16 08:52', content: '规则触发并生成告警' },
      { time: '2026-03-16 08:59', content: '值班组接收并待分派' },
    ],
  },
  {
    id: 'AR-3003',
    title: '中心城区夜间客流密度异常',
    level: '中',
    sourceObject: '城市感知网关',
    triggerRule: '重点区域客流波动告警',
    time: '2026-03-16 08:28',
    status: '已处理',
    description: '夜间客流在核心商圈持续偏离历史均值。',
    triggerReason: '客流偏离均值达到 2.4σ，触发地区异常策略。',
    relatedContents: ['中心城区夜间客流异常增长专题研判'],
    suggestion: '建议在节假日前提高该区域预警阈值敏感度。',
    timeline: [
      { time: '2026-03-16 08:28', content: '系统告警触发' },
      { time: '2026-03-16 08:40', content: '现场分流措施执行' },
      { time: '2026-03-16 09:05', content: '事件标记已处理' },
    ],
  },
]
