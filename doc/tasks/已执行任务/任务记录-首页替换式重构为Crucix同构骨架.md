# 任务：首页替换式重构为 Crucix 同构骨架

## 任务状态
- 状态：已完成，待人工验收
- 开始时间：2026-04-01
- 最后更新时间：2026-04-01
- 任务性质：替换式重构，不是继续优化旧首页

## 本轮任务来源
用户最新明确要求：

1. 当前首页虽然已经把自动滚动与地图联动拆开，但本质上仍然是在旧首页骨架上修补；
2. 当前首页仍保留明显的 frame、面板、卡片拼盘和后台页结构；
3. 本轮必须放弃当前首页骨架，按 Crucix 首页的“极轻顶部 + 中央主地图舞台 + 右侧 signal rail”做替换式重构；
4. 本轮重点不再是补功能，而是整体视觉骨架必须大变，且一眼能看出接近 Crucix 首页结构。

## 对当前现状的重新判断
当前首页虽已存在以下拆分：

- `HaidianCockpitPanel.vue`
- `SituationTopBar.vue`
- `SituationMapOverlayBar.vue`
- `SituationRightFocusPanel.vue`

并且已经具备以下正确方向：

- 自动滚动只推动右侧流本身；
- 点击 ticker 条目时才聚焦地图并打开详情；
- 首页已经去掉了底部主轮播和左侧大摘要墙。

但当前仍不满足本轮目标，原因是：

1. 首页整体仍有明显 frame 容器与后台面板感；
2. 主体区仍是“固定网格 + 业务面板”的拼接方式；
3. 右侧仍偏业务说明栏，不够像单一的 signal rail；
4. 地图虽然较大，但整体舞台感不足，仍像被包在页面内容区中的一块模块；
5. 顶部 chrome 和地图顶部 strip 仍带有一定的工具条和卡片感。

## 本轮允许保留的内容
- `HaidianCockpitPanel.vue` 作为首页入口组件
- `HaidianCockpitMapStage.vue` 及地图底层交互能力
- 点位、图层、zone、ticker、详情跳转数据结构
- 自动滚动不联动地图、点击条目才联动地图的正确交互逻辑

## 本轮不允许保留的内容
- 当前首页的 frame 式整体包裹壳
- 后台式大面板 / 卡片拼盘表达
- 顶部像 header 工具区的结构
- 右侧像业务说明面板的布局层次
- 首页仍然“像旧后台，只是更深色一点”的视觉印象

## 本轮目标骨架
```text
页面最上层：极轻顶部 chrome
┌──────────────────────────────────────────────────────────┐
│ 系统名 / ACTIVE状态 / 当前区域 / 当前时间 / 极少量按钮    │
└──────────────────────────────────────────────────────────┘

页面主舞台：主体压场 + 右侧 signal rail
┌────────────────────────────────────────────────┬────────────────────┐
│                                                │                    │
│            中央主地图 / 态势舞台                │  右侧重要情报滚动流  │
│                                                │                    │
│    地图顶部仅保留一条极窄 overlay strip         │  标题区极轻          │
│    用于态势摘要高亮 + 图层轻控制                │  持续滚动            │
│                                                │  当前项高亮          │
│    地图必须占页面绝对主导面积                   │  hover暂停           │
│                                                │  点击打开详情        │
│                                                │  点击时地图再联动     │
└────────────────────────────────────────────────┴────────────────────┘
```

## 分阶段执行
### 阶段1：替换首页骨架
- 删除旧首页 frame / card / panel 感
- 让首页只保留极轻顶部、地图主舞台、右侧 rail
- 首页第一眼必须先看到地图和右侧 rail 的主辅关系

### 阶段2：重做顶部 chrome 与地图顶部 strip
- 顶部高度压到 40px～56px 目标范围
- strip 只保留一条单行 overlay
- 摘要与图层切换都收进这条窄 strip

### 阶段3：重做右侧 signal rail
- 改成单一的信息解释层，而不是多个业务块
- 保留持续滚动、hover 暂停、当前项高亮
- 点击条目时：联动地图并打开详情

### 阶段4：视觉收尾与回归
- 降低后台感和卡片感
- 跑构建与 UI 冒烟
- 更新状态文档和任务文档

## 本轮验收硬指标
1. 顶部整体高度桌面端控制在 40px～56px，不能超过 64px；
2. 地图区约 70%～76%，右侧 rail 约 24%～30%；
3. 地图顶部 strip 高度控制在 42px～56px；
4. 页面第一眼必须是“一个主舞台 + 一条侧边 rail”，不能还是后台拼盘页；
5. 自动滚动仍然不能驱动地图跳动；
6. 点击 ticker 条目时必须联动地图并打开详情。

## 本次改动说明
- 已执行完成，核心结果如下：
  1. 首页仍保留 `HaidianCockpitPanel.vue` 作为入口，但已重写其模板骨架，不再沿用旧的 frame + 固定网格 + 业务面板式布局；
  2. 首页顶部已改成极轻 chrome，只保留系统名、ACTIVE 状态、区域/时间与极少量动作按钮；
  3. 首页主体已改成“地图主舞台 + 右侧 signal rail”双区结构，地图保持主视觉，右侧 rail 明显增强存在感；
  4. 地图顶部只保留一条极窄 overlay strip，不再出现双层工具带和厚摘要块；
  5. 右侧已新增真正的 `SituationSignalRail.vue`，不再勉强沿用旧的右侧业务面板结构；
  6. 自动滚动仍然只推动 rail 本身，不联动地图；点击条目时，才切图层、联动地图并打开详情；
  7. 已补充 ticker mock 数据密度，并修复了一个 ticker 指向不存在点位的隐患；
  8. 已同步更新 UI 冒烟脚本，保证回归验证针对的是新首页骨架，而不是旧 DOM；
  9. 已删除旧的 `SituationRightFocusPanel.vue` 和 `SituationBottomTicker.vue`，避免旧过渡组件继续干扰后续开发判断。

## 涉及文件
- `src/views/dashboard/OverviewDashboard.vue`
- `src/components/dashboard/HaidianCockpitPanel.vue`
- `src/components/dashboard/SituationTopBar.vue`
- `src/components/dashboard/SituationMapOverlayBar.vue`
- `src/components/dashboard/SituationSignalRail.vue`
- `src/components/dashboard/HaidianCockpitMapStage.vue`
- `src/mock/dashboardCockpit.ts`
- `scripts/ui-smoke.mjs`
- `src/components/dashboard/SituationRightFocusPanel.vue`（已废弃删除）
- `src/components/dashboard/SituationBottomTicker.vue`（已废弃删除）

## 验收结果
- `npm run build`：通过
- `node scripts/ui-smoke.mjs`：通过
- 本轮回归已覆盖：
  - 首页地图加载
  - 顶部轻 chrome 按钮交互
  - 地图顶部 strip 摘要点击与图层切换
  - 右侧 signal rail 自动滚动
  - hover 暂停与恢复
  - 自动滚动不联动地图
  - 点击 rail 条目进入详情
  - 菜单链路切换

## 当前结论
- 本轮不是继续优化旧首页，而是完成了首页视觉骨架的替换式重构；
- 当前首页已经从“后台拼盘感驾驶舱”切换为更接近 Crucix 的“极轻顶部 + 中央地图主舞台 + 右侧 signal rail”结构；
- 当前状态为“待人工验收”，在用户确认前不归档该任务文档。
