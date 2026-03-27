import type {
  EntityItem,
  EntityTypeDistributionItem,
  EventItem,
  HotEventItem,
  KnowledgeMetric,
  RelationTypeDistributionItem,
} from '../types/knowledgeBuild'

export const knowledgeMetricsMock: KnowledgeMetric[] = [
  { id: 'entityTotal', label: '实体总数', value: '1,782,440', change: '较昨日 +2.8%' },
  { id: 'relationTotal', label: '关系总数', value: '6,491,208', change: '较昨日 +3.5%' },
  { id: 'eventTotal', label: '事件总数', value: '124,860', change: '较昨日 +1.9%' },
  { id: 'tagTotal', label: '标签总数', value: '38,204', change: '较昨日 +0.7%' },
  { id: 'newEntityToday', label: '今日新增实体', value: '13,820', change: '较昨日 +4.1%' },
  { id: 'newEventToday', label: '今日新增事件', value: '926', change: '较昨日 +2.2%' },
]

export const entityTypeDistributionMock: EntityTypeDistributionItem[] = [
  { type: '机构', value: 32 },
  { type: '人物', value: 25 },
  { type: '地点', value: 18 },
  { type: '项目', value: 15 },
  { type: '产品', value: 10 },
]

export const relationTypeDistributionMock: RelationTypeDistributionItem[] = [
  { type: '上下游关联', value: 41 },
  { type: '合作关系', value: 23 },
  { type: '从属关系', value: 17 },
  { type: '事件参与', value: 12 },
  { type: '空间关联', value: 7 },
]

export const hotEventMockList: HotEventItem[] = [
  { id: 'HE-5001', eventName: '重点产业链供给波动', eventType: '产业事件', heat: 96, riskLevel: '高', updatedAt: '2026-03-16 09:12' },
  { id: 'HE-5002', eventName: '核心商圈客流异动', eventType: '运营事件', heat: 91, riskLevel: '中', updatedAt: '2026-03-16 08:55' },
  { id: 'HE-5003', eventName: '突发舆情传播加速', eventType: '舆情事件', heat: 89, riskLevel: '高', updatedAt: '2026-03-16 08:36' },
  { id: 'HE-5004', eventName: '重点区域安全告警聚集', eventType: '公共安全', heat: 84, riskLevel: '中', updatedAt: '2026-03-16 08:08' },
]

export const entityMockList: EntityItem[] = [
  {
    id: 'EN-6001',
    entityName: '华新智能制造有限公司',
    entityType: '机构',
    standardName: '华新智能制造有限公司',
    aliasCount: 3,
    relatedContentCount: 186,
    confidence: 0.97,
    updatedAt: '2026-03-16 09:01',
    aliases: ['华新智造', '华新制造', 'HXIM'],
    relatedContents: ['产业链运行周报', '企业产能波动监测', '重点项目推进简报'],
    relatedEvents: ['重点产业链供给波动', '核心零部件库存预警'],
    relatedRelations: ['合作关系: 城投供应集团', '上下游关联: 远辰电子'],
    basicInfo: { 注册地: '高新区', 行业: '高端装备', 法定代表人: '陈睿', 成立时间: '2015-06-18' },
  },
  {
    id: 'EN-6002',
    entityName: '中心商圈',
    entityType: '地点',
    standardName: '中心城区核心商圈',
    aliasCount: 2,
    relatedContentCount: 143,
    confidence: 0.94,
    updatedAt: '2026-03-16 08:48',
    aliases: ['核心商圈', '中心商业区'],
    relatedContents: ['城市客流异常波动快报', '消费活跃度监测日报'],
    relatedEvents: ['核心商圈客流异动'],
    relatedRelations: ['空间关联: 地铁枢纽A', '事件参与: 商业活动B'],
    basicInfo: { 行政区: '中心城区', 功能定位: '商业消费', 人流等级: '高', 重点时段: '18:00-22:00' },
  },
  {
    id: 'EN-6003',
    entityName: '城市运行监测项目',
    entityType: '项目',
    standardName: '城市运行监测项目（一期）',
    aliasCount: 1,
    relatedContentCount: 98,
    confidence: 0.92,
    updatedAt: '2026-03-16 08:22',
    aliases: ['运行监测一期'],
    relatedContents: ['项目进展月报', '城市运行指标专题'],
    relatedEvents: ['重点区域安全告警聚集'],
    relatedRelations: ['从属关系: 城市治理中心'],
    basicInfo: { 主管单位: '城市治理中心', 启动时间: '2024-04-01', 状态: '运行中', 预算规模: '2.8 亿' },
  },
]

export const eventMockList: EventItem[] = [
  {
    id: 'EV-7001',
    eventName: '重点产业链供给波动',
    eventType: '产业事件',
    location: '高新区',
    startTime: '2026-03-14 10:20',
    riskLevel: '高',
    relatedContentCount: 126,
    status: '持续跟踪',
    summary: '关键零部件供应周期拉长，导致产业链两级节点产能波动。',
    timeline: [
      { time: '2026-03-14 10:20', content: '监测系统识别供应链交付延迟信号' },
      { time: '2026-03-14 14:40', content: '关联企业库存预警阈值触发' },
      { time: '2026-03-15 09:05', content: '形成专题研判并进入持续跟踪' },
    ],
    involvedEntities: ['华新智能制造有限公司', '远辰电子', '城投供应集团'],
    relatedContents: ['重点产业链运行周报', '企业产能波动监测简报'],
    riskExplanation: '若供给波动持续超过 72 小时，可能触发重点项目交付风险，建议启动替代供应链预案。',
  },
  {
    id: 'EV-7002',
    eventName: '核心商圈客流异动',
    eventType: '运营事件',
    location: '中心城区',
    startTime: '2026-03-15 18:10',
    riskLevel: '中',
    relatedContentCount: 94,
    status: '研判中',
    summary: '夜间时段客流超出历史均值，呈现异常聚集趋势。',
    timeline: [
      { time: '2026-03-15 18:10', content: '实时客流监测触发预警阈值' },
      { time: '2026-03-15 18:22', content: '视频结构化识别密度持续升高' },
      { time: '2026-03-15 19:05', content: '进入事件研判流程并通知值守组' },
    ],
    involvedEntities: ['中心商圈', '地铁枢纽A', '活动场馆B'],
    relatedContents: ['城市客流异常波动快报', '公共安全态势日报'],
    riskExplanation: '当前风险以秩序管理压力上升为主，建议加强重点区域分流引导。',
  },
  {
    id: 'EV-7003',
    eventName: '突发舆情传播加速',
    eventType: '舆情事件',
    location: '全市',
    startTime: '2026-03-15 21:36',
    riskLevel: '高',
    relatedContentCount: 138,
    status: '已处置',
    summary: '敏感话题传播链路在短时内迅速扩散，已完成初步处置。',
    timeline: [
      { time: '2026-03-15 21:36', content: '舆情平台检测到传播速率异常' },
      { time: '2026-03-15 22:10', content: '触发跨部门协同处置机制' },
      { time: '2026-03-16 00:18', content: '处置完成并进入回溯评估阶段' },
    ],
    involvedEntities: ['舆情中心', '媒体平台X', '监管部门Y'],
    relatedContents: ['舆情态势日分析', '突发舆情传播链路报告'],
    riskExplanation: '事件已阶段性收敛，但需持续关注二次传播和话题反弹风险。',
  },
]
