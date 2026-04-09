-- Task-02：初始化系统字典、地区、事件类型与地图图层基础数据。

INSERT INTO sys_dict_type (
    id, dict_code, dict_name, status, created_by, created_time, updated_by, updated_time, deleted, remark
) VALUES
    (1001, 'region_type', '地区类型', 1, 0, NOW(), 0, NOW(), 0, 'Task-02 初始化'),
    (1002, 'region_level', '地区层级', 1, 0, NOW(), 0, NOW(), 0, 'Task-02 初始化'),
    (1003, 'map_layer_type', '地图图层类型', 1, 0, NOW(), 0, NOW(), 0, 'Task-02 初始化'),
    (1004, 'event_category', '事件分类', 1, 0, NOW(), 0, NOW(), 0, 'Task-02 初始化');

INSERT INTO sys_dict_data (
    id, type_id, item_label, item_value, sort_no, ext_json, status, created_by, created_time, updated_by, updated_time, deleted, remark
) VALUES
    (2001, 1001, '区', 'district', 1, NULL, 1, 0, NOW(), 0, NOW(), 0, '地区类型'),
    (2002, 1001, '街道', 'street', 2, NULL, 1, 0, NOW(), 0, NOW(), 0, '地区类型'),
    (2003, 1001, '镇', 'town', 3, NULL, 1, 0, NOW(), 0, NOW(), 0, '地区类型'),
    (2011, 1002, '一级', '1', 1, '{\"description\": \"区级范围\"}', 1, 0, NOW(), 0, NOW(), 0, '地区层级'),
    (2012, 1002, '二级', '2', 2, '{\"description\": \"街道或镇级范围\"}', 1, 0, NOW(), 0, NOW(), 0, '地区层级'),
    (2021, 1003, '事件图层', 'event', 1, NULL, 1, 0, NOW(), 0, NOW(), 0, '图层类型'),
    (2022, 1003, '重点关注图层', 'focus', 2, NULL, 1, 0, NOW(), 0, NOW(), 0, '图层类型'),
    (2031, 1004, '风险事件', 'risk', 1, NULL, 1, 0, NOW(), 0, NOW(), 0, '事件分类'),
    (2032, 1004, '突发事件', 'emergency', 2, NULL, 1, 0, NOW(), 0, NOW(), 0, '事件分类'),
    (2033, 1004, '热点事件', 'hotspot', 3, NULL, 1, 0, NOW(), 0, NOW(), 0, '事件分类'),
    (2034, 1004, '重点关注', 'focus', 4, NULL, 1, 0, NOW(), 0, NOW(), 0, '事件分类');

INSERT INTO biz_region (
    id, parent_id, region_code, region_name, region_level, region_type, center_lng, center_lat, zoom_level, sort_no,
    enabled, created_by, created_time, updated_by, updated_time, deleted, remark
) VALUES
    (3001, 0, 'HDQ', '海淀区', 1, 'district', 116.298056, 39.959912, 11, 1, 1, 0, NOW(), 0, NOW(), 0, '海淀区全域初始化数据'),
    (3002, 3001, 'XBWZ', '西北旺镇', 2, 'town', 116.265000, 40.050000, 13, 1, 1, 0, NOW(), 0, NOW(), 0, '重点地区初始化数据'),
    (3003, 3001, 'SDJD', '上地街道', 2, 'street', 116.313000, 40.033000, 13, 2, 1, 0, NOW(), 0, NOW(), 0, '重点地区初始化数据'),
    (3004, 3001, 'ZGCJD', '中关村街道', 2, 'street', 116.316000, 39.983000, 13, 3, 1, 0, NOW(), 0, NOW(), 0, '重点地区初始化数据'),
    (3005, 3001, 'XYLJD', '学院路街道', 2, 'street', 116.360000, 39.990000, 13, 4, 1, 0, NOW(), 0, NOW(), 0, '重点地区初始化数据'),
    (3006, 3001, 'QHJD', '清河街道', 2, 'street', 116.338000, 40.039000, 13, 5, 1, 0, NOW(), 0, NOW(), 0, '重点地区初始化数据');

INSERT INTO biz_region_geo (
    id, region_id, geo_json, simplified_geo_json, version_no, status, created_by, created_time, updated_by, updated_time, deleted, remark
) VALUES
    (3101, 3001, '{\"type\":\"FeatureCollection\",\"features\":[]}', '{\"type\":\"FeatureCollection\",\"features\":[]}', 1, 1, 0, NOW(), 0, NOW(), 0, '待补充海淀区真实边界数据'),
    (3102, 3002, '{\"type\":\"FeatureCollection\",\"features\":[]}', '{\"type\":\"FeatureCollection\",\"features\":[]}', 1, 1, 0, NOW(), 0, NOW(), 0, '待补充西北旺镇真实边界数据'),
    (3103, 3003, '{\"type\":\"FeatureCollection\",\"features\":[]}', '{\"type\":\"FeatureCollection\",\"features\":[]}', 1, 1, 0, NOW(), 0, NOW(), 0, '待补充上地街道真实边界数据'),
    (3104, 3004, '{\"type\":\"FeatureCollection\",\"features\":[]}', '{\"type\":\"FeatureCollection\",\"features\":[]}', 1, 1, 0, NOW(), 0, NOW(), 0, '待补充中关村街道真实边界数据'),
    (3105, 3005, '{\"type\":\"FeatureCollection\",\"features\":[]}', '{\"type\":\"FeatureCollection\",\"features\":[]}', 1, 1, 0, NOW(), 0, NOW(), 0, '待补充学院路街道真实边界数据'),
    (3106, 3006, '{\"type\":\"FeatureCollection\",\"features\":[]}', '{\"type\":\"FeatureCollection\",\"features\":[]}', 1, 1, 0, NOW(), 0, NOW(), 0, '待补充清河街道真实边界数据');

INSERT INTO biz_event_type (
    id, event_code, event_name, event_category, color_code, icon_code, sort_no,
    enabled, created_by, created_time, updated_by, updated_time, deleted, remark
) VALUES
    (4001, 'risk_event', '风险事件', 'risk', '#FF6B3B', 'warning-circle', 1, 1, 0, NOW(), 0, NOW(), 0, '首页风险类基础事件'),
    (4002, 'emergency_event', '突发事件', 'emergency', '#D7263D', 'flash', 2, 1, 0, NOW(), 0, NOW(), 0, '首页突发类基础事件'),
    (4003, 'hotspot_event', '热点事件', 'hotspot', '#FFB703', 'fire', 3, 1, 0, NOW(), 0, NOW(), 0, '首页热点类基础事件'),
    (4004, 'focus_target', '重点关注', 'focus', '#006D77', 'eye', 4, 1, 0, NOW(), 0, NOW(), 0, '首页重点关注类基础事件');

INSERT INTO biz_map_layer (
    id, layer_code, layer_name, layer_type, enabled, sort_no, ext_json, created_by, created_time, updated_by, updated_time, deleted, remark
) VALUES
    (5001, 'risk_event', '风险事件图层', 'event', 1, 1, '{\"color\":\"#FF6B3B\",\"icon\":\"warning-circle\",\"defaultVisible\":true}', 0, NOW(), 0, NOW(), 0, '首页默认图层'),
    (5002, 'emergency_event', '突发事件图层', 'event', 1, 2, '{\"color\":\"#D7263D\",\"icon\":\"flash\",\"defaultVisible\":true}', 0, NOW(), 0, NOW(), 0, '首页默认图层'),
    (5003, 'hotspot_event', '热点事件图层', 'event', 1, 3, '{\"color\":\"#FFB703\",\"icon\":\"fire\",\"defaultVisible\":true}', 0, NOW(), 0, NOW(), 0, '首页默认图层'),
    (5004, 'focus_target', '重点关注图层', 'focus', 1, 4, '{\"color\":\"#006D77\",\"icon\":\"eye\",\"defaultVisible\":true}', 0, NOW(), 0, NOW(), 0, '首页默认图层');
