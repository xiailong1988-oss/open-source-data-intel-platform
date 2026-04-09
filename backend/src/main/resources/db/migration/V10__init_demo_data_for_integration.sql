-- Task-11: initialize demo data for frontend integration and acceptance

-- 1. Supplement region data for richer frontend switching scenarios.
INSERT INTO biz_region (
    id, parent_id, region_code, region_name, region_level, region_type, center_lng, center_lat, zoom_level, sort_no,
    enabled, created_by, created_time, updated_by, updated_time, deleted, remark
) VALUES
    (3007, 3001, 'QLQJD', '青龙桥街道', 2, 'street', 116.287000, 39.999000, 13, 6, 1, 0, NOW(), 0, NOW(), 0, 'Task-11 联调补充地区'),
    (3008, 3001, 'WSLJD', '万寿路街道', 2, 'street', 116.285000, 39.913000, 13, 7, 1, 0, NOW(), 0, NOW(), 0, 'Task-11 联调补充地区');

INSERT INTO biz_region_geo (
    id, region_id, geo_json, simplified_geo_json, version_no, status, created_by, created_time, updated_by, updated_time, deleted, remark
) VALUES
    (3107, 3007, '{"type":"FeatureCollection","features":[]}', '{"type":"FeatureCollection","features":[]}', 1, 1, 0, NOW(), 0, NOW(), 0, 'Task-11 联调补充地区边界'),
    (3108, 3008, '{"type":"FeatureCollection","features":[]}', '{"type":"FeatureCollection","features":[]}', 1, 1, 0, NOW(), 0, NOW(), 0, 'Task-11 联调补充地区边界');

-- 2. Supplement event types. Existing V2 map layers already cover the four event categories required by the homepage.
INSERT INTO biz_event_type (
    id, event_code, event_name, event_category, color_code, icon_code, sort_no,
    enabled, created_by, created_time, updated_by, updated_time, deleted, remark
) VALUES
    (4005, 'public_opinion_hotspot', '网络舆情', 'hotspot', '#7C3AED', 'message-circle', 5, 1, 0, NOW(), 0, NOW(), 0, 'Task-11 联调补充事件类型'),
    (4006, 'security_inspection', '安保巡检', 'risk', '#0EA5E9', 'shield-check', 6, 1, 0, NOW(), 0, NOW(), 0, 'Task-11 联调补充事件类型'),
    (4007, 'activity_support', '活动保障', 'emergency', '#16A34A', 'flag', 7, 1, 0, NOW(), 0, NOW(), 0, 'Task-11 联调补充事件类型'),
    (4008, 'key_person_focus', '重点人员关注', 'focus', '#8B5CF6', 'user-alert', 8, 1, 0, NOW(), 0, NOW(), 0, 'Task-11 联调补充事件类型');

-- 3. Supplement intel reference data for richer homepage source and tag display.
INSERT INTO biz_intel_source (
    id, source_name, source_type, source_url, credibility_level, enabled,
    created_by, created_time, updated_by, updated_time, deleted, remark
) VALUES
    (6003, '视频巡查', 'video', 'https://demo.local/video-patrol', 4, 1, 0, NOW(), 0, NOW(), 0, 'Task-11 联调补充来源'),
    (6004, '热线工单平台', 'service_hotline', 'https://demo.local/hotline', 4, 1, 0, NOW(), 0, NOW(), 0, 'Task-11 联调补充来源'),
    (6005, '应急值守', 'duty', 'https://demo.local/duty', 5, 1, 0, NOW(), 0, NOW(), 0, 'Task-11 联调补充来源');

INSERT INTO biz_intel_tag (
    id, tag_name, tag_type, sort_no, enabled,
    created_by, created_time, updated_by, updated_time, deleted, remark
) VALUES
    (7005, '园区运行', 'theme', 5, 1, 0, NOW(), 0, NOW(), 0, 'Task-11 联调补充标签'),
    (7006, '交通保障', 'service', 6, 1, 0, NOW(), 0, NOW(), 0, 'Task-11 联调补充标签'),
    (7007, '舆情研判', 'risk', 7, 1, 0, NOW(), 0, NOW(), 0, 'Task-11 联调补充标签'),
    (7008, '活动保障', 'theme', 8, 1, 0, NOW(), 0, NOW(), 0, 'Task-11 联调补充标签');

-- 4. Initialize demo intel data for homepage ticker, stream, map point, and admin pages.
INSERT INTO biz_intel_item (
    id, title, summary, content, region_id, event_type_id, source_id, importance_level, heat_score, focus_flag, risk_level,
    occur_time, publish_time, lng, lat, address, status, review_status, data_origin, ext_json,
    created_by, created_time, updated_by, updated_time, deleted, remark
) VALUES
    (
        9001,
        '中关村论坛周边客流与传播热度同步上升',
        '论坛会场周边客流上升，线上讨论量同步增加，需持续关注传播外溢情况。',
        '今日上午中关村论坛周边会场和商业区客流明显增长，网络讨论量同步上升，部分话题已扩散至本地社区平台。当前现场秩序总体平稳，建议继续关注高峰时段交通与传播走势。',
        3004, 4005, 6002, 3, 98, 1, 2,
        DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 90 MINUTE),
        116.316800, 39.985500, '中关村论坛会场周边', 1, 1, 'web', '{"channel":"forum","trend":"up"}',
        0, NOW(), 0, NOW(), 0, 'Task-11 首页热点示例'
    ),
    (
        9002,
        '西北旺智算园区夜间网络波动已触发预警',
        '园区核心链路夜间出现短时波动，巡检与值守人员已同步跟进。',
        '西北旺镇智算园区夜间监测发现核心网络链路波动，值守人员已完成首轮排查，当前业务基本恢复。为避免高峰期再次抖动，建议继续跟踪设备和链路负载。',
        3002, 4006, 6005, 3, 87, 1, 3,
        DATE_SUB(NOW(), INTERVAL 5 HOUR), DATE_SUB(NOW(), INTERVAL 270 MINUTE),
        116.268400, 40.048500, '西北旺镇智算园区A区', 1, 1, 'duty', '{"scene":"park-network","severity":"medium"}',
        0, NOW(), 0, NOW(), 0, 'Task-11 地图点位示例'
    ),
    (
        9003,
        '上地软件园核心楼宇出现短时聚集排队',
        '午间出入口短时排队，现场疏导后秩序恢复正常。',
        '上地软件园核心楼宇在午间时段出现短时排队和人员聚集，视频巡查与物业引导后，现场通行秩序已恢复正常。后续仍需关注上下班高峰的通行承压情况。',
        3003, 4002, 6003, 2, 78, 0, 2,
        DATE_SUB(NOW(), INTERVAL 8 HOUR), DATE_SUB(NOW(), INTERVAL 450 MINUTE),
        116.312200, 40.031400, '上地软件园核心楼宇出入口', 1, 1, 'video', '{"crowdLevel":"medium","resolved":true}',
        0, NOW(), 0, NOW(), 0, 'Task-11 首页流示例'
    ),
    (
        9004,
        '学院路高校周边大型活动保障压力增加',
        '活动筹备与校周交通叠加，属地保障压力有所上升。',
        '学院路街道高校周边活动筹备进入密集阶段，校园周边交通和人员组织压力上升。当前秩序总体可控，但建议加强外围交通接驳和重点时段值守安排。',
        3005, 4007, 6004, 2, 74, 1, 2,
        DATE_SUB(NOW(), INTERVAL 11 HOUR), DATE_SUB(NOW(), INTERVAL 600 MINUTE),
        116.361600, 39.992100, '学院路高校周边保障点位', 1, 1, 'manual', '{"eventWindow":"weekend","supportLevel":"high"}',
        0, NOW(), 0, NOW(), 0, 'Task-11 重点关注示例'
    ),
    (
        9005,
        '清河街道交通接驳在晚高峰出现拥堵预警',
        '重点路口晚高峰车流提升，接驳保障需要动态调度。',
        '清河街道重点路口晚高峰车流明显增加，接驳保障压力上升。当前未形成长时间拥堵，但建议继续关注公交接驳和临时引导措施执行情况。',
        3006, 4001, 6005, 2, 69, 0, 2,
        DATE_SUB(NOW(), INTERVAL 15 HOUR), DATE_SUB(NOW(), INTERVAL 870 MINUTE),
        116.339100, 40.038600, '清河街道重点接驳路口', 1, 1, 'duty', '{"traffic":"peak","dispatch":"dynamic"}',
        0, NOW(), 0, NOW(), 0, 'Task-11 风险事件示例'
    ),
    (
        9006,
        '海淀区重点人员关联舆情进入连续观察阶段',
        '关联话题连续两日处于活跃状态，需保持滚动研判。',
        '重点人员关联舆情在多个公开平台连续活跃，部分讨论已与区域热点话题交叉扩散。当前尚未形成线下风险，但建议继续保持滚动研判和专题跟踪。',
        3001, 4008, 6002, 4, 91, 1, 3,
        DATE_SUB(NOW(), INTERVAL 20 HOUR), DATE_SUB(NOW(), INTERVAL 1140 MINUTE),
        116.305000, 39.960800, '海淀区综合研判中心', 1, 1, 'web', '{"topic":"focus-person","observation":"continuous"}',
        0, NOW(), 0, NOW(), 0, 'Task-11 重点人员示例'
    ),
    (
        9007,
        '青龙桥街道景区周边临时管控信息引发讨论',
        '景区周边临时管控信息引发关注，传播热度仍在上升。',
        '青龙桥街道景区周边发布临时管控提示后，本地社交平台讨论量有所上升。现场秩序可控，但建议继续关注景区客流和传播反馈。',
        3007, 4003, 6002, 2, 65, 0, 1,
        DATE_SUB(NOW(), INTERVAL 28 HOUR), DATE_SUB(NOW(), INTERVAL 1620 MINUTE),
        116.288200, 39.998700, '青龙桥街道景区周边道路', 1, 1, 'web', '{"scene":"tourism","attention":"rising"}',
        0, NOW(), 0, NOW(), 0, 'Task-11 新增地区示例'
    ),
    (
        9008,
        '万寿路街道活动保障复盘显示秩序总体平稳',
        '活动结束后复盘显示保障措施有效，可作为历史样例联调。',
        '万寿路街道近期活动保障复盘显示，现场秩序和外围交通总体平稳。该条数据主要作为历史时间样例，便于前端验证不同时间维度下的列表展示。',
        3008, 4007, 6001, 1, 45, 0, 1,
        DATE_SUB(NOW(), INTERVAL 121 HOUR), DATE_SUB(NOW(), INTERVAL 119 HOUR),
        116.286100, 39.913600, '万寿路街道活动保障区域', 1, 1, 'manual', '{"historySample":true}',
        0, NOW(), 0, NOW(), 0, 'Task-11 历史时间样例'
    ),
    (
        9009,
        '上地街道园区巡检线索待人工复核',
        '园区巡检发现异常线索，当前仍处于人工复核阶段。',
        '上地街道园区巡检发现一条待核实线索，当前尚未完成人工复核。本条数据用于后台管理页面联调，首页公开接口不会展示。',
        3003, 4006, 6001, 2, 55, 0, 2,
        DATE_SUB(NOW(), INTERVAL 6 HOUR), DATE_SUB(NOW(), INTERVAL 330 MINUTE),
        116.314000, 40.032500, '上地街道园区巡检点位', 1, 0, 'manual', '{"reviewStage":"pending"}',
        0, NOW(), 0, NOW(), 0, 'Task-11 后台待审核样例'
    ),
    (
        9010,
        '清河街道历史重复线索归档示例',
        '历史重复线索已归档停用，用于后台状态联调。',
        '清河街道一条历史重复线索已被归档停用。本条数据用于后台管理端验证停用和驳回状态展示，不参与首页统计与地图点位展示。',
        3006, 4001, 6001, 1, 20, 0, 1,
        DATE_SUB(NOW(), INTERVAL 220 HOUR), DATE_SUB(NOW(), INTERVAL 215 HOUR),
        116.338500, 40.037900, '清河街道历史归档点位', 0, 2, 'manual', '{"archiveReason":"duplicate"}',
        0, NOW(), 0, NOW(), 0, 'Task-11 后台停用样例'
    );

INSERT INTO biz_intel_item_tag (
    id, intel_item_id, tag_id, created_by, created_time, updated_by, updated_time, deleted, remark
) VALUES
    (9201, 9001, 7003, 0, NOW(), 0, NOW(), 0, 'Task-11 联调标签关系'),
    (9202, 9001, 7007, 0, NOW(), 0, NOW(), 0, 'Task-11 联调标签关系'),
    (9203, 9002, 7005, 0, NOW(), 0, NOW(), 0, 'Task-11 联调标签关系'),
    (9204, 9002, 7003, 0, NOW(), 0, NOW(), 0, 'Task-11 联调标签关系'),
    (9205, 9003, 7005, 0, NOW(), 0, NOW(), 0, 'Task-11 联调标签关系'),
    (9206, 9003, 7002, 0, NOW(), 0, NOW(), 0, 'Task-11 联调标签关系'),
    (9207, 9004, 7008, 0, NOW(), 0, NOW(), 0, 'Task-11 联调标签关系'),
    (9208, 9004, 7002, 0, NOW(), 0, NOW(), 0, 'Task-11 联调标签关系'),
    (9209, 9005, 7006, 0, NOW(), 0, NOW(), 0, 'Task-11 联调标签关系'),
    (9210, 9005, 7008, 0, NOW(), 0, NOW(), 0, 'Task-11 联调标签关系'),
    (9211, 9006, 7003, 0, NOW(), 0, NOW(), 0, 'Task-11 联调标签关系'),
    (9212, 9006, 7007, 0, NOW(), 0, NOW(), 0, 'Task-11 联调标签关系'),
    (9213, 9007, 7002, 0, NOW(), 0, NOW(), 0, 'Task-11 联调标签关系'),
    (9214, 9007, 7007, 0, NOW(), 0, NOW(), 0, 'Task-11 联调标签关系'),
    (9215, 9008, 7006, 0, NOW(), 0, NOW(), 0, 'Task-11 联调标签关系'),
    (9216, 9008, 7008, 0, NOW(), 0, NOW(), 0, 'Task-11 联调标签关系'),
    (9217, 9009, 7005, 0, NOW(), 0, NOW(), 0, 'Task-11 联调标签关系'),
    (9218, 9009, 7003, 0, NOW(), 0, NOW(), 0, 'Task-11 联调标签关系'),
    (9219, 9010, 7006, 0, NOW(), 0, NOW(), 0, 'Task-11 联调标签关系');

-- 5. Initialize homepage hotspot topics with linked intel items.
INSERT INTO biz_hotspot_topic (
    id, topic_name, topic_category, region_id, heat_score, mention_count,
    latest_event_time, latest_intel_id, owner_dept, keyword, summary, enabled,
    created_by, created_time, updated_by, updated_time, deleted, remark
) VALUES
    (
        8201, '中关村论坛传播热度观察', 'public-opinion', 3004, 118, 1560,
        DATE_SUB(NOW(), INTERVAL 90 MINUTE), 9001, '网信研判组', '中关村论坛,会场周边',
        '围绕会场周边客流、网络讨论和传播扩散的热点专题，适合首页演示与专题联调。', 1,
        0, NOW(), 0, NOW(), 0, 'Task-11 首页热点专题'
    ),
    (
        8202, '西北旺智算园区运行波动', 'park-operation', 3002, 114, 1240,
        DATE_SUB(NOW(), INTERVAL 270 MINUTE), 9002, '园区运行专班', '智算园区,链路波动',
        '聚焦智算园区设备链路波动和夜间值守处置情况的专题，适合专题卡片演示。', 1,
        0, NOW(), 0, NOW(), 0, 'Task-11 首页热点专题'
    ),
    (
        8203, '学院路高校活动保障', 'activity-support', 3005, 112, 1035,
        DATE_SUB(NOW(), INTERVAL 600 MINUTE), 9004, '属地保障组', '高校活动,交通保障',
        '聚焦高校周边活动筹备、保障压力和周边秩序变化，适合首页热点专题展示。', 1,
        0, NOW(), 0, NOW(), 0, 'Task-11 首页热点专题'
    ),
    (
        8204, '清河交通接驳压力', 'traffic-support', 3006, 109, 980,
        DATE_SUB(NOW(), INTERVAL 870 MINUTE), 9005, '交通保障组', '清河接驳,晚高峰',
        '围绕晚高峰接驳压力、交通疏导和现场保障的演示专题。', 1,
        0, NOW(), 0, NOW(), 0, 'Task-11 首页热点专题'
    ),
    (
        8205, '重点人员关联舆情观察', 'focus-watch', 3001, 107, 910,
        DATE_SUB(NOW(), INTERVAL 1140 MINUTE), 9006, '综合研判组', '重点人员,关联舆情',
        '围绕重点人员关联话题的连续观察专题，便于演示重点关注与热点专题之间的联动。', 1,
        0, NOW(), 0, NOW(), 0, 'Task-11 首页热点专题'
    ),
    (
        8206, '青龙桥景区周边传播关注', 'tourism-watch', 3007, 105, 860,
        DATE_SUB(NOW(), INTERVAL 1620 MINUTE), 9007, '文旅保障组', '景区周边,临时管控',
        '围绕景区周边临时管控和传播反馈的演示专题，补充新增地区联调数据。', 1,
        0, NOW(), 0, NOW(), 0, 'Task-11 首页热点专题'
    );

-- 6. Initialize homepage focus targets with linked intel items.
INSERT INTO biz_focus_target (
    id, target_name, target_type, region_id, focus_level, latest_event_time,
    latest_intel_id, latest_event_title, status, keyword, summary, enabled,
    created_by, created_time, updated_by, updated_time, deleted, remark
) VALUES
    (
        8301, '中关村论坛会场重点观察对象', 'venue', 3004, 3, DATE_SUB(NOW(), INTERVAL 90 MINUTE),
        9001, '中关村论坛周边客流与传播热度同步上升', '连续观察', '会场周边,客流,论坛',
        '首页重点关注演示对象，适合验证高优先级标签、详情跳转和时间展示。', 1,
        0, NOW(), 0, NOW(), 0, 'Task-11 首页重点关注'
    ),
    (
        8302, '西北旺智算园区核心设施', 'facility', 3002, 3, DATE_SUB(NOW(), INTERVAL 270 MINUTE),
        9002, '西北旺智算园区夜间网络波动已触发预警', '优先跟踪', '智算园区,网络链路',
        '聚焦园区核心设施稳定性和处置进展的重点关注对象。', 1,
        0, NOW(), 0, NOW(), 0, 'Task-11 首页重点关注'
    ),
    (
        8303, '上地软件园核心楼宇', 'building', 3003, 3, DATE_SUB(NOW(), INTERVAL 450 MINUTE),
        9003, '上地软件园核心楼宇出现短时聚集排队', '持续关注', '软件园,楼宇秩序',
        '用于演示园区核心楼宇场景下的重点关注卡片展示。', 1,
        0, NOW(), 0, NOW(), 0, 'Task-11 首页重点关注'
    ),
    (
        8304, '学院路高校活动保障点位', 'support-point', 3005, 3, DATE_SUB(NOW(), INTERVAL 600 MINUTE),
        9004, '学院路高校周边大型活动保障压力增加', '连续观察', '高校周边,活动保障',
        '用于演示高校周边保障类重点对象的联调效果。', 1,
        0, NOW(), 0, NOW(), 0, 'Task-11 首页重点关注'
    ),
    (
        8305, '清河交通接驳保障单元', 'transport-unit', 3006, 3, DATE_SUB(NOW(), INTERVAL 870 MINUTE),
        9005, '清河街道交通接驳在晚高峰出现拥堵预警', '动态监测', '晚高峰,接驳保障',
        '用于验证交通接驳类重点对象的状态和最近事件信息展示。', 1,
        0, NOW(), 0, NOW(), 0, 'Task-11 首页重点关注'
    ),
    (
        8306, '海淀重点人员关联线索', 'person', 3001, 3, DATE_SUB(NOW(), INTERVAL 1140 MINUTE),
        9006, '海淀区重点人员关联舆情进入连续观察阶段', '连续观察', '重点人员,关联舆情',
        '用于演示全区级重点对象的卡片展示与详情跳转。', 1,
        0, NOW(), 0, NOW(), 0, 'Task-11 首页重点关注'
    );

-- 7. Initialize collection source and task demo data for admin pages.
INSERT INTO biz_collection_source (
    id, source_name, source_category, source_url, access_type, enabled, status, region_scope, owner_name,
    latest_collect_time, description, created_by, created_time, updated_by, updated_time, deleted, remark
) VALUES
    (
        10001, '区网格事件交换接口', 'gov_system', 'https://demo.local/grid-api', 'api', 1, 'running', '海淀区',
        '张敏', DATE_SUB(NOW(), INTERVAL 20 MINUTE),
        '面向区级网格事件交换的标准接口，供首页联调与后台分页演示。', 0, NOW(), 0, NOW(), 0, 'Task-11 数据源演示'
    ),
    (
        10002, '园区视频巡查汇聚库', 'business_system', 'jdbc:mysql://demo.local/patrol', 'database_sync', 1, 'running', '西北旺镇、上地街道',
        '李峰', DATE_SUB(NOW(), INTERVAL 45 MINUTE),
        '聚合园区视频巡查结构化结果，适合展示数据库同步类数据源。', 0, NOW(), 0, NOW(), 0, 'Task-11 数据源演示'
    ),
    (
        10003, '互联网公开舆情站点', 'public_data', 'https://demo.local/public-opinion', 'message_subscribe', 1, 'running', '海淀区',
        '王倩', DATE_SUB(NOW(), INTERVAL 10 MINUTE),
        '用于接收公开舆情订阅消息，支撑热点类演示数据来源说明。', 0, NOW(), 0, NOW(), 0, 'Task-11 数据源演示'
    ),
    (
        10004, '重点区域传感设备', 'iot_device', 'mqtt://demo.local/sensor', 'message_subscribe', 1, 'abnormal', '清河街道、学院路街道',
        '赵航', DATE_SUB(NOW(), INTERVAL 2 HOUR),
        '重点区域传感设备流，当前用于展示异常中的设备型数据源。', 0, NOW(), 0, NOW(), 0, 'Task-11 数据源演示'
    ),
    (
        10005, '外部协查数据投递', 'third_party', 'smb://demo-server/exchange/packages', 'file_import', 0, 'stopped', '海淀区',
        '陈楠', DATE_SUB(NOW(), INTERVAL 1 DAY),
        '第三方协查材料投递目录，用于展示停用状态的数据源。', 0, NOW(), 0, NOW(), 0, 'Task-11 数据源演示'
    );

INSERT INTO biz_collection_task (
    id, task_name, source_id, task_type, cron_expr, frequency_desc, enabled, run_status, latest_result,
    latest_run_time, record_count, success_rate, description, created_by, created_time, updated_by, updated_time, deleted, remark
) VALUES
    (
        10101, '海淀区网格事件增量采集', 10001, 'incremental_collect', '0 */15 * * * ?', '每15分钟',
        1, 'running', 'success', DATE_SUB(NOW(), INTERVAL 5 MINUTE), 12840, 99.60,
        '持续同步区级网格事件增量，适合演示运行中任务。', 0, NOW(), 0, NOW(), 0, 'Task-11 采集任务演示'
    ),
    (
        10102, '园区视频巡查全量同步', 10002, 'full_sync', '0 0 */2 * * ?', '每2小时',
        1, 'running', 'partial_success', DATE_SUB(NOW(), INTERVAL 35 MINUTE), 54210, 97.20,
        '按批次同步园区视频巡查结果，适合演示部分成功场景。', 0, NOW(), 0, NOW(), 0, 'Task-11 采集任务演示'
    ),
    (
        10103, '公开舆情实时订阅', 10003, 'realtime_subscribe', 'streaming', '实时',
        1, 'running', 'success', DATE_SUB(NOW(), INTERVAL 3 MINUTE), 8860, 99.10,
        '接收公开舆情实时订阅消息，适合演示实时类采集任务。', 0, NOW(), 0, NOW(), 0, 'Task-11 采集任务演示'
    ),
    (
        10104, '重点区域设备健康巡检', 10004, 'scheduled_check', '0 0/30 * * * ?', '每30分钟',
        1, 'abnormal', 'failed', DATE_SUB(NOW(), INTERVAL 50 MINUTE), 1260, 82.40,
        '检查重点区域传感设备健康状态，用于演示异常中的采集任务。', 0, NOW(), 0, NOW(), 0, 'Task-11 采集任务演示'
    ),
    (
        10105, '协查文件导入任务', 10005, 'full_sync', '0 0 2 * * ?', '每天02:00',
        0, 'paused', 'success', DATE_SUB(NOW(), INTERVAL 1 DAY), 420, 100.00,
        '处理外部协查文件导入，用于展示已暂停任务。', 0, NOW(), 0, NOW(), 0, 'Task-11 采集任务演示'
    );

-- 8. Initialize data package, package file, and delivery record demo data for admin pages.
INSERT INTO biz_data_package (
    id, package_name, package_type, stat_month, region_scope, file_count, total_size, status,
    publish_version, publish_time, published_by_name, publish_note, description,
    created_by, created_time, updated_by, updated_time, deleted, remark
) VALUES
    (
        11001, '海淀区首页联调演示数据包', 'monthly_brief', DATE_FORMAT(CURRENT_DATE, '%Y-%m'), '海淀区',
        3, 8486912, 'published', 2, DATE_SUB(NOW(), INTERVAL 1 DAY), '系统管理员', '联调演示版本已发布',
        '用于首页概览、快讯条、地图点位联调的月度演示数据包。', 0, NOW(), 0, NOW(), 0, 'Task-11 数据包演示'
    ),
    (
        11002, '重点专题研判样例数据包', 'special_dataset', DATE_FORMAT(CURRENT_DATE, '%Y-%m'), '中关村街道、西北旺镇',
        2, 4194304, 'published', 1, DATE_SUB(NOW(), INTERVAL 12 HOUR), '系统管理员', '专题研判联调使用',
        '用于热点专题与重点关注页面联调的样例数据包。', 0, NOW(), 0, NOW(), 0, 'Task-11 数据包演示'
    ),
    (
        11003, '交付归档草稿包', 'delivery_archive', DATE_FORMAT(DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH), '%Y-%m'), '清河街道、学院路街道',
        2, 2097152, 'draft', 0, NULL, '', '',
        '用于后台管理页展示草稿状态的交付归档包。', 0, NOW(), 0, NOW(), 0, 'Task-11 数据包演示'
    ),
    (
        11004, '首页专题服务发布包', 'topic_service', DATE_FORMAT(CURRENT_DATE, '%Y-%m'), '海淀区',
        1, 1572864, 'published', 1, DATE_SUB(NOW(), INTERVAL 6 HOUR), '系统管理员', '专题服务联调用',
        '用于首页专题服务和交付记录页联调的发布包。', 0, NOW(), 0, NOW(), 0, 'Task-11 数据包演示'
    );

INSERT INTO biz_data_package_file (
    id, package_id, file_name, file_path, file_size, file_type, checksum, storage_type,
    storage_bucket, storage_object_key, download_url, sort_order,
    created_by, created_time, updated_by, updated_time, deleted, remark
) VALUES
    (
        11101, 11001, 'dashboard-overview.json', '/packages/demo/dashboard-overview.json', 2457600, 'json',
        'sha256-demo-11001-01', 'LOCAL', '', 'demo/dashboard-overview.json', 'https://download.local/demo/dashboard-overview.json', 1,
        0, NOW(), 0, NOW(), 0, 'Task-11 数据包文件'
    ),
    (
        11102, 11001, 'intel-stream.csv', '/packages/demo/intel-stream.csv', 5242880, 'csv',
        'sha256-demo-11001-02', 'LOCAL', '', 'demo/intel-stream.csv', 'https://download.local/demo/intel-stream.csv', 2,
        0, NOW(), 0, NOW(), 0, 'Task-11 数据包文件'
    ),
    (
        11103, 11001, 'map-points.geojson', '/packages/demo/map-points.geojson', 786432, 'geojson',
        'sha256-demo-11001-03', 'LOCAL', '', 'demo/map-points.geojson', 'https://download.local/demo/map-points.geojson', 3,
        0, NOW(), 0, NOW(), 0, 'Task-11 数据包文件'
    ),
    (
        11104, 11002, 'hotspot-topic.xlsx', '/packages/topic/hotspot-topic.xlsx', 1048576, 'xlsx',
        'sha256-demo-11002-01', 'LOCAL', '', 'topic/hotspot-topic.xlsx', 'https://download.local/topic/hotspot-topic.xlsx', 1,
        0, NOW(), 0, NOW(), 0, 'Task-11 数据包文件'
    ),
    (
        11105, 11002, 'focus-target.xlsx', '/packages/topic/focus-target.xlsx', 3145728, 'xlsx',
        'sha256-demo-11002-02', 'LOCAL', '', 'topic/focus-target.xlsx', 'https://download.local/topic/focus-target.xlsx', 2,
        0, NOW(), 0, NOW(), 0, 'Task-11 数据包文件'
    ),
    (
        11106, 11003, 'delivery-archive-summary.docx', '/packages/archive/delivery-archive-summary.docx', 524288, 'docx',
        'sha256-demo-11003-01', 'LOCAL', '', 'archive/delivery-archive-summary.docx', '', 1,
        0, NOW(), 0, NOW(), 0, 'Task-11 数据包文件'
    ),
    (
        11107, 11003, 'delivery-checklist.pdf', '/packages/archive/delivery-checklist.pdf', 1572864, 'pdf',
        'sha256-demo-11003-02', 'LOCAL', '', 'archive/delivery-checklist.pdf', '', 2,
        0, NOW(), 0, NOW(), 0, 'Task-11 数据包文件'
    ),
    (
        11108, 11004, 'topic-service-package.zip', '/packages/service/topic-service-package.zip', 1572864, 'zip',
        'sha256-demo-11004-01', 'LOCAL', '', 'service/topic-service-package.zip', 'https://download.local/service/topic-service-package.zip', 1,
        0, NOW(), 0, NOW(), 0, 'Task-11 数据包文件'
    );

INSERT INTO biz_delivery_record (
    id, delivery_type, related_id, related_name, related_version, receiver_name, receiver_org, contact_phone,
    delivery_time, delivery_method, description, created_by, created_time, updated_by, updated_time, deleted, remark
) VALUES
    (
        11201, 'data_package', 11001, '海淀区首页联调演示数据包', 2, '联调值班人员', '海淀区值班室', '13800000001',
        DATE_SUB(NOW(), INTERVAL 20 HOUR), 'online_download', '已通过在线链接完成首页联调数据包交付。', 0, NOW(), 0, NOW(), 0, 'Task-11 交付记录演示'
    ),
    (
        11202, 'data_package', 11002, '重点专题研判样例数据包', 1, '专题研判专班', '专题研判组', '13800000002',
        DATE_SUB(NOW(), INTERVAL 8 HOUR), 'email', '已通过邮件发送专题研判样例数据包。', 0, NOW(), 0, NOW(), 0, 'Task-11 交付记录演示'
    ),
    (
        11203, 'data_package', 11004, '首页专题服务发布包', 1, '前端联调人员', '前端联调组', '13800000003',
        DATE_SUB(NOW(), INTERVAL 4 HOUR), 'disk_handover', '已完成专题服务发布包介质交付。', 0, NOW(), 0, NOW(), 0, 'Task-11 交付记录演示'
    );
