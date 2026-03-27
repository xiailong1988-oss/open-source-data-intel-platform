import { dashboardMockData } from './dashboard'
import type { DashboardSpatialOverview } from '../types/dashboard'
import type { TopicWorkbenchCode, TopicWorkbenchData } from '../types/topicWorkbench'

const baseOverview = dashboardMockData.spatialOverview

const pickRegions = (names: string[]) =>
  baseOverview.regions.filter((region) => names.includes(region.name))

const pickPoints = (ids: string[]) =>
  baseOverview.points.filter((point) => ids.includes(point.id))

const pickEvents = (ids: string[]) =>
  baseOverview.events.filter((event) => ids.includes(event.id))

const pickTopics = (ids: string[]) =>
  baseOverview.topics.filter((topic) => ids.includes(topic.id))

const createTopicOverview = (
  code: TopicWorkbenchCode,
  layerBriefs: DashboardSpatialOverview['defaultSummary']['layerBriefs'],
  regions: string[],
  pointIds: string[],
  eventIds: string[],
  topicIds: string[],
): DashboardSpatialOverview => ({
  ...baseOverview,
  mapName: `topic-${code}`,
  headlines:
    code === 'economy'
      ? [
          { id: 'industry-alerts', label: '产业预警', value: '14 条', hint: '聚焦重点产业链、项目调度与物流波动' },
          { id: 'core-regions', label: '重点地区', value: '5 区', hint: '大兴、顺义、丰台、房山与通州为重点观察区' },
          { id: 'project-events', label: '重点事件', value: '9 项', hint: '项目调度、冷链物流与客流承压同步跟踪' },
          { id: 'briefing-ready', label: '待出简报', value: '3 份', hint: '经济专题已形成从 GIS 到简报输出的闭环' },
        ]
      : [
          { id: 'innovation-institutes', label: '重点机构', value: '18 家', hint: '中关村、怀柔科学城与北部创新带持续活跃' },
          { id: 'tech-topics', label: '技术热点', value: '7 项', hint: '模型研发、协同攻关和论坛活动热度居前' },
          { id: 'innovation-events', label: '重点事件', value: '8 项', hint: '论坛、联合攻关和科研合作事件持续推进' },
          { id: 'briefing-ready', label: '待出简报', value: '2 份', hint: '创新专题已接入专题简报与问答闭环' },
        ],
  regions: pickRegions(regions),
  points: pickPoints(pointIds),
  events: pickEvents(eventIds),
  topics: pickTopics(topicIds),
  quickLinks:
    code === 'economy'
      ? [
          { id: 'EQ-01', title: '产业链检索', description: '聚焦经济专题线索', icon: 'Search', path: '/smart-search', query: { topic: 'economy', keyword: '产业链波动', mode: '语义检索' } },
          { id: 'EQ-02', title: '专题问答', description: '进入经济专题问答', icon: 'ChatDotRound', path: '/smart-qa', query: { topic: 'economy', question: '当前北京重点产业链有哪些高风险信号？', autoAsk: 'true' } },
          { id: 'EQ-03', title: '专题简报', description: '查看经济专题简报', icon: 'Document', path: '/report-center/briefings', query: { topic: '经济专题' } },
          { id: 'EQ-04', title: '返回 GIS 总览', description: '回到综合态势首页', icon: 'DataAnalysis', path: '/dashboard', query: { layer: '专题研判' } },
        ]
      : [
          { id: 'IN-01', title: '创新检索', description: '追踪机构与技术热点', icon: 'Search', path: '/smart-search', query: { topic: 'innovation', keyword: '创新资源流动', mode: '语义检索' } },
          { id: 'IN-02', title: '专题问答', description: '进入创新专题问答', icon: 'ChatDotRound', path: '/smart-qa', query: { topic: 'innovation', question: '当前北京创新资源最集中的区域有哪些？', autoAsk: 'true' } },
          { id: 'IN-03', title: '专题简报', description: '查看创新专题简报', icon: 'Document', path: '/report-center/briefings', query: { topic: '创新专题' } },
          { id: 'IN-04', title: '返回 GIS 总览', description: '回到综合态势首页', icon: 'DataAnalysis', path: '/dashboard', query: { layer: '重点事件' } },
        ],
  recentFocuses:
    code === 'economy'
      ? [
          { id: 'E-RF-01', title: '北京重点产业链运行周报', meta: '最近关联报告 · 08:40', path: '/report-center/detail/RP-202603-112' },
          { id: 'E-RF-02', title: '亦庄制造链路调度', meta: '最近跟踪事件 · 08:21', path: '/knowledge-build/event-management', query: { keyword: '亦庄制造链路调度', autoOpen: 'first' } },
          { id: 'E-RF-03', title: '顺义冷链物流风险点', meta: '最近联动预警 · 08:51', path: '/analysis-warning/records', query: { keyword: '顺义冷链物流风险点持续波动', autoOpen: 'first' } },
        ]
      : [
          { id: 'I-RF-01', title: '中关村创新要素日分析', meta: '最近关联报告 · 17:20', path: '/report-center/detail/RP-202603-097' },
          { id: 'I-RF-02', title: '中关村论坛保障任务', meta: '最近跟踪事件 · 08:42', path: '/knowledge-build/event-management', query: { keyword: '中关村论坛保障任务', autoOpen: 'first' } },
          { id: 'I-RF-03', title: '怀柔科学城协同攻关', meta: '最近专题观察 · 07:56', path: '/knowledge-build/event-management', query: { keyword: '怀柔科学城协同攻关', autoOpen: 'first' } },
        ],
  defaultSummary: {
    briefing:
      code === 'economy'
        ? '经济专题聚焦重点产业链、园区项目和物流链路的变化，当前主线集中在亦庄智能制造、顺义冷链和副中心消费活跃带。'
        : '创新专题聚焦中关村、怀柔科学城和北部创新带的资源流动，当前重点观察论坛活动、协同攻关和机构合作。',
    instructions: [
      '优先通过 GIS 主视图查看本专题重点区域与点位分布，再进入事件、预警和简报链路。',
      '点击热点区域和点位后，右侧情报面板会同步展示专题摘要、关联对象与继续追踪入口。',
      '通过专题快捷入口可继续跳转到智能检索、智能问答、简报中心和综合态势首页。',
    ],
    keyRegions: regions,
    hotspotSummary:
      code === 'economy'
        ? '大兴、顺义和通州构成当前经济专题的三条主线，分别对应制造链路、物流承压和消费活跃度变化。'
        : '海淀、怀柔和昌平构成当前创新专题主轴，分别承接创新要素、科研协同和北部创新延展。',
    layerBriefs,
  },
})

export const topicWorkbenchMockMap: Record<TopicWorkbenchCode, TopicWorkbenchData> = {
  economy: {
    code: 'economy',
    eyebrow: 'Economic Topic Workbench',
    title: '经济专题工作台',
    description: '围绕产业动态、重点地区、供应链相关事件和投资项目变化构建经济专题 GIS 研判视图。',
    heroHint: '从经济视角直接追踪区域热度、链路风险和简报输出状态。',
    metrics: [
      { id: 'industry-chain', label: '重点产业链', value: 12, hint: '6 条高频跟踪链路已接入日常研判' },
      { id: 'project-signals', label: '项目变化', value: '24 项', hint: '覆盖项目推进、投资波动和园区调度' },
      { id: 'high-risk', label: '高风险线索', value: 6, hint: '高风险集中在制造链路与冷链物流' },
      { id: 'briefings', label: '待出简报', value: 3, hint: '专题简报与事件专报均已具备 mock 输出能力' },
    ],
    signalTitle: '经济活跃区对比',
    signalSubtitle: '按区域统计产业、项目和物流相关线索热度',
    signalData: [
      { label: '大兴区', value: 94 },
      { label: '顺义区', value: 88 },
      { label: '通州区', value: 82 },
      { label: '丰台区', value: 76 },
      { label: '房山区', value: 71 },
    ],
    overview: createTopicOverview(
      'economy',
      {
        综合: '综合图层用于查看产业热区、关键链路和重点区域的整体热度，帮助快速识别经济态势主线。',
        风险预警: '风险预警图层突出制造、物流和项目调度中的异常点位，便于快速进入处置链路。',
        重点事件: '重点事件图层承接项目调度、活动保障和供应链联动任务，便于持续跟踪。',
        专题研判: '专题研判图层聚焦产业链、投资项目和外围经济样本，是进入简报和专题报告的核心入口。',
      },
      ['大兴区', '顺义区', '通州区', '丰台区', '房山区'],
      ['PT-TOP-01', 'PT-WARN-02', 'PT-WARN-03', 'PT-EVT-02', 'PT-TOP-03'],
      ['THREAD-01', 'THREAD-04', 'THREAD-02'],
      ['TOPIC-02', 'TOPIC-03', 'TOPIC-04'],
    ),
    focusEvents: [
      {
        id: 'E-FE-01',
        title: '亦庄制造链路调度',
        region: '大兴区',
        time: '08:21',
        status: '持续跟踪',
        level: '高',
        summary: '设备波动与供应链交付节奏出现耦合变化，已进入专题简报链路。',
        keyword: '亦庄智能制造供应链',
      },
      {
        id: 'E-FE-02',
        title: '顺义冷链物流风险点',
        region: '顺义区',
        time: '08:51',
        status: '待复核',
        level: '高',
        summary: '冷链节点时效连续偏离阈值，需关注后续物流稳定性。',
        keyword: '顺义冷链物流风险点持续波动',
      },
      {
        id: 'E-FE-03',
        title: '副中心客流与消费联动',
        region: '通州区',
        time: '08:28',
        status: '处理中',
        level: '中',
        summary: '文旅活动带动区域消费活跃度提升，需要同步关注客流承压。',
        keyword: '副中心客流与消费活跃度快报',
      },
    ],
    recommendedQuestions: [
      { id: 'E-Q-01', question: '当前北京重点产业链有哪些高风险信号？', description: '从专题问答继续追踪制造、物流和项目链路。' },
      { id: 'E-Q-02', question: '亦庄智能制造供应链最近有哪些关键变化？', description: '聚焦亦庄园区核心企业、设备与预警联动。' },
      { id: 'E-Q-03', question: '副中心消费活跃度抬升背后有哪些风险？', description: '联动客流、活动和消费专题数据。' },
    ],
    briefingReferences: [
      {
        id: 'BF-202603-201',
        title: '经济专题晨间简报',
        type: '每日态势简报',
        generatedAt: '2026-03-16 08:10',
        status: '已生成',
        summary: '汇总重点产业链波动、项目变化和物流预警。',
        reportId: 'RP-202603-112',
      },
      {
        id: 'BF-202603-203',
        title: '亦庄制造链路事件专报',
        type: '事件专报',
        generatedAt: '2026-03-16 08:48',
        status: '待审核',
        summary: '聚焦大兴区制造链路调度和高风险预警。',
      },
    ],
    briefingKeyword: '经济专题',
  },
  innovation: {
    code: 'innovation',
    eyebrow: 'Innovation Topic Workbench',
    title: '创新专题工作台',
    description: '围绕创新机构、技术热点、项目动态和研究合作构建创新专题 GIS 研判视图。',
    heroHint: '从创新视角追踪资源流动、重点论坛和科研协同主线。',
    metrics: [
      { id: 'institutes', label: '重点机构', value: 18, hint: '覆盖中关村、怀柔科学城与北部创新带' },
      { id: 'hotspots', label: '技术热点', value: '7 项', hint: '模型研发、协同攻关和技术发布热度持续抬升' },
      { id: 'collaboration', label: '研究合作', value: 11, hint: '重点联合攻关项目进入持续跟踪状态' },
      { id: 'briefings', label: '待出简报', value: 2, hint: '创新专题已接入专题简报与日报双模板' },
    ],
    signalTitle: '创新资源热度对比',
    signalSubtitle: '按区域统计机构、活动和技术热点热度',
    signalData: [
      { label: '海淀区', value: 98 },
      { label: '怀柔区', value: 88 },
      { label: '昌平区', value: 74 },
      { label: '平谷区', value: 66 },
      { label: '朝阳区', value: 58 },
    ],
    overview: createTopicOverview(
      'innovation',
      {
        综合: '综合图层用于观察创新资源在北京的空间分布与整体热度，快速识别重点机构和热点区域。',
        风险预警: '风险预警图层聚焦资源供给节奏、活动保障和创新链条中的异常信号。',
        重点事件: '重点事件图层围绕论坛活动、联合攻关和科研协同任务展开，便于继续跟踪。',
        专题研判: '专题研判图层用于沉淀创新要素、科研合作和技术热点，可直接流向简报和报告。',
      },
      ['海淀区', '怀柔区', '昌平区', '平谷区'],
      ['PT-COMP-02', 'PT-EVT-01', 'PT-EVT-03', 'PT-TOP-03'],
      ['THREAD-03', 'THREAD-05'],
      ['TOPIC-01'],
    ),
    focusEvents: [
      {
        id: 'I-FE-01',
        title: '中关村论坛保障任务',
        region: '海淀区',
        time: '08:42',
        status: '跟踪中',
        level: '中',
        summary: '重点机构、论坛活动和传播热度进入同一时间窗口，需要持续观察。',
        keyword: '中关村论坛保障',
      },
      {
        id: 'I-FE-02',
        title: '怀柔科学城协同攻关',
        region: '怀柔区',
        time: '07:56',
        status: '稳定推进',
        level: '低',
        summary: '多家科研机构联合攻关，是创新专题的重点协同样本。',
        keyword: '怀柔科学城协同攻关',
      },
      {
        id: 'I-FE-03',
        title: '研发资源供给节奏波动',
        region: '海淀区',
        time: '09:05',
        status: '观察中',
        level: '中',
        summary: '资源供给节奏波动触发创新专题风险提示，需联动预警链路。',
        keyword: '研发资源供给节奏波动',
      },
    ],
    recommendedQuestions: [
      { id: 'I-Q-01', question: '当前北京创新资源最集中的区域有哪些？', description: '从专题问答继续追踪海淀、怀柔和昌平的资源分布。' },
      { id: 'I-Q-02', question: '中关村论坛相关事件目前有哪些重点线索？', description: '聚焦论坛活动、机构联动和传播热点。' },
      { id: 'I-Q-03', question: '怀柔科学城最近有哪些协同攻关进展？', description: '联动科研机构、事件与专题简报。' },
    ],
    briefingReferences: [
      {
        id: 'BF-202603-301',
        title: '创新专题晨间简报',
        type: '每日态势简报',
        generatedAt: '2026-03-16 08:06',
        status: '已生成',
        summary: '汇总创新资源流动、重点论坛活动和科研协同态势。',
        reportId: 'RP-202603-097',
      },
      {
        id: 'BF-202603-305',
        title: '中关村论坛专题简报',
        type: '专题简报',
        generatedAt: '2026-03-16 08:44',
        status: '待审核',
        summary: '聚焦中关村论坛保障任务、重点机构与传播热度。',
      },
    ],
    briefingKeyword: '创新专题',
  },
}
