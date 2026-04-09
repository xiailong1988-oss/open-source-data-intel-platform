-- Task-04：初始化最小来源与标签参考数据，支撑当前情报模块创建、修改和联查验证。

INSERT INTO biz_intel_source (
    id, source_name, source_type, source_url, credibility_level, enabled,
    created_by, created_time, updated_by, updated_time, deleted, remark
) VALUES
    (6001, '人工录入', 'manual', '', 5, 1, 0, NOW(), 0, NOW(), 0, 'Task-04 最小参考数据'),
    (6002, '网络巡查', 'web', 'https://example.com', 4, 1, 0, NOW(), 0, NOW(), 0, 'Task-04 最小参考数据');

INSERT INTO biz_intel_tag (
    id, tag_name, tag_type, sort_no, enabled,
    created_by, created_time, updated_by, updated_time, deleted, remark
) VALUES
    (7001, '校园安全', 'theme', 1, 1, 0, NOW(), 0, NOW(), 0, 'Task-04 最小参考数据'),
    (7002, '群体活动', 'theme', 2, 1, 0, NOW(), 0, NOW(), 0, 'Task-04 最小参考数据'),
    (7003, '舆情关注', 'risk', 3, 1, 0, NOW(), 0, NOW(), 0, 'Task-04 最小参考数据'),
    (7004, '民生服务', 'service', 4, 1, 0, NOW(), 0, NOW(), 0, 'Task-04 最小参考数据');
