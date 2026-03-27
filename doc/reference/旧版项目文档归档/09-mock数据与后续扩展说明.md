# mock 数据与后续扩展说明

## 当前哪些页面使用 mock 数据

当前项目全部业务页面都使用 mock 数据，包括：

- 首页综合态势
- 地图总览
- 数据接入
- 数据治理
- 知识构建
- 智能检索
- 智能问答
- 分析预警
- 报告中心
- 系统管理

## mock 数据存放位置

统一位于 `src/mock`：

- [dashboard.ts](E:/codex-test/open-source-data-intel-platform/src/mock/dashboard.ts)
- [dataAccess.ts](E:/codex-test/open-source-data-intel-platform/src/mock/dataAccess.ts)
- [dataGovernance.ts](E:/codex-test/open-source-data-intel-platform/src/mock/dataGovernance.ts)
- [knowledgeBuild.ts](E:/codex-test/open-source-data-intel-platform/src/mock/knowledgeBuild.ts)
- [smartSearch.ts](E:/codex-test/open-source-data-intel-platform/src/mock/smartSearch.ts)
- [smartQa.ts](E:/codex-test/open-source-data-intel-platform/src/mock/smartQa.ts)
- [analysisWarning.ts](E:/codex-test/open-source-data-intel-platform/src/mock/analysisWarning.ts)
- [reportCenter.ts](E:/codex-test/open-source-data-intel-platform/src/mock/reportCenter.ts)
- [systemManagement.ts](E:/codex-test/open-source-data-intel-platform/src/mock/systemManagement.ts)
- [uiConfig.ts](E:/codex-test/open-source-data-intel-platform/src/mock/uiConfig.ts)

## mock 数据按模块组织方式

当前遵循“一个业务域一个 mock 文件”的原则：

- 首页相关集中在 `dashboard.ts`
- 接入相关集中在 `dataAccess.ts`
- 治理相关集中在 `dataGovernance.ts`
- 知识相关集中在 `knowledgeBuild.ts`
- 检索与问答分别拆开
- 预警与报告分别拆开
- 系统管理独立维护
- 顶部工作台 UI 配置独立维护

这种组织方式的好处是：

- 页面与数据域一一对应
- 后续替换时能按模块逐步迁移
- 更容易维护字段一致性

其中 `uiConfig.ts` 当前用于承载：

- 顶部统一检索 / 问答入口模式
- 顶部专题视角选项
- 推荐问题
- 顶部命令栏 placeholder

后续可直接迁移到 `/api/config/ui`。

## 页面如何消费 mock 数据

当前大多数页面采用以下模式：

1. 页面直接从对应 `mock/*.ts` 导入初始数据
2. 页面用 `ref([...mockData])` 生成本地可变副本
3. 列表页通过 `computed` 做前端筛选
4. 详情页通过当前选中对象显示抽屉或弹窗
5. 页面间联动依赖 query 参数和页面自身的解析逻辑

## query 参数、筛选和 mock 的关系

当前系统很多页面都支持通过 query 参数进行“钻取进入”，例如：

- 首页卡片跳到列表页
- 图表点击跳到列表页
- 地图点位和区域跳到检索 / 预警 / 事件 / 实体
- 列表页跳到详情页时通过 `autoOpen=first` 自动打开首条

这意味着：

- mock 数据必须保证关键字段完整
- 页面必须有稳定的 query 参数解析逻辑
- 目标页必须能根据参数在 mock 数据中定位到对象或列表

## 地图 mock 的组织方式

首页地图的 mock 目前放在 `dashboard.ts` 中的 `spatialOverview` 字段内，主要分为：

- `layers`：图层名称
- `regions`：区域与区域统计
- `points`：地图点位
- `quickLinks`：快捷入口
- `recentFocuses`：最近关注
- `defaultSummary`：默认状态说明

后续如果地图继续扩展，可以考虑单独拆出 `dashboardMap.ts`。

## 智能问答 mock 的组织方式

问答 mock 目前放在 `smartQa.ts`，包括：

- 历史问题
- 推荐问题
- fallback 答案生成函数

后续如果接真实模型，可以保留现有答案结构，仅替换数据来源。

## 预警、报告、检索等 mock 的组织方式

- 预警：分为概览数据、规则数据、记录数据
- 报告：分为列表数据和详情数据
- 检索：分为筛选项和结果项

这种拆法适合未来逐步替换为：

- 概览接口
- 列表接口
- 详情接口
- 辅助筛选配置接口

## 后续替换成真实接口的改造建议

### 建议新增 API 层

后续可在 `src/api` 或 `src/services` 中按模块新增接口文件，例如：

- `dashboard.ts`
- `dataAccess.ts`
- `warning.ts`
- `report.ts`

### 替换策略

建议按“模块逐步迁移”的方式替换：

1. 先保留现有类型定义
2. 页面不直接依赖真实接口，而是统一走 service 层
3. mock 与真实接口在 service 层可切换
4. 先替换列表接口，再替换详情接口，再替换联动接口

### 地图迁移策略

地图后续接真实服务时，建议：

- 保留当前图层、区域、点位和右侧面板的前端结构
- 后端返回区域统计、点位列表和关联对象
- 前端继续使用当前交互逻辑，只替换数据来源

## 当前注意事项

- mock 数据字段名必须与页面展示字段保持一致
- 增加新联动时，必须先补 mock 字段
- 修改 query 参数行为时，必须同步检查目标页是否支持解析
