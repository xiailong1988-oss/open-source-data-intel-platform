-- Task-07: initialize homepage hotspot and focus target demo data

INSERT INTO biz_hotspot_topic (
    id, topic_name, topic_category, region_id, heat_score, mention_count,
    latest_event_time, latest_intel_id, owner_dept, keyword, summary, enabled,
    created_by, created_time, updated_by, updated_time, deleted, remark
) VALUES
    (8001, 'ZGC innovation flow', 'innovation', 3001, 96, 1340, '2026-04-08 09:10:00', NULL, 'Innovation Center', 'zhongguancun innovation', 'Topic for innovation institutions, model releases, and knowledge flow updates.', 1, 0, NOW(), 0, NOW(), 0, 'Task-07 demo data'),
    (8002, 'Sub-center culture and tourism', 'city-operation', 3001, 92, 1185, '2026-04-08 08:52:00', NULL, 'City Operations Center', 'sub-center tourism', 'Topic for tourism events, venue traffic, and city vitality signals.', 1, 0, NOW(), 0, NOW(), 0, 'Task-07 demo data'),
    (8003, 'Yizhuang smart manufacturing chain', 'industry', 3001, 94, 1064, '2026-04-08 08:36:00', NULL, 'Industry Taskforce', 'yizhuang supply chain', 'Topic for equipment operations, supply chain changes, and park activity trends.', 1, 0, NOW(), 0, NOW(), 0, 'Task-07 demo data'),
    (8004, 'Forum communication chain', 'risk-communication', 3004, 90, 865, '2026-04-08 09:18:00', NULL, 'Communication Analysis Team', 'forum communication', 'Topic for media attention, forum discussions, and external communication spillover.', 1, 0, NOW(), 0, NOW(), 0, 'Task-07 demo data'),
    (8005, 'Research collaboration watch', 'research-watch', 3005, 86, 712, '2026-04-08 08:40:00', NULL, 'Research Collaboration Team', 'research collaboration', 'Topic for university collaboration, research projects, and result transfer activity.', 1, 0, NOW(), 0, NOW(), 0, 'Task-07 demo data');

INSERT INTO biz_focus_target (
    id, target_name, target_type, region_id, focus_level, latest_event_time,
    latest_intel_id, latest_event_title, status, keyword, summary, enabled,
    created_by, created_time, updated_by, updated_time, deleted, remark
) VALUES
    (8101, 'ZGC science city key institution', 'institution', 3004, 3, '2026-04-08 09:06:00', NULL, 'Forum venue temporary control', 'continuous-watch', 'science city institution', 'Key institution linked with forum activity, model release, and policy attention.', 1, 0, NOW(), 0, NOW(), 0, 'Task-07 demo data'),
    (8102, 'Northwest smart compute project', 'project', 3002, 3, '2026-04-08 08:46:00', NULL, 'Park network fluctuation alert', 'priority-watch', 'smart compute project', 'Key project linked with network fluctuation, drills, and investment attention.', 1, 0, NOW(), 0, NOW(), 0, 'Task-07 demo data'),
    (8103, 'Shangdi software leading enterprise', 'enterprise', 3003, 2, '2026-04-08 08:31:00', NULL, 'Compute dispatch anomaly alert', 'tracking', 'software enterprise', 'Leading enterprise linked with compute chain updates and industry sentiment.', 1, 0, NOW(), 0, NOW(), 0, 'Task-07 demo data'),
    (8104, 'Research road key team', 'team', 3005, 2, '2026-04-08 08:14:00', NULL, 'Research collaboration discussion increased', 'continuous-observation', 'research team', 'Key team with multiple collaboration clues suitable for homepage focus display.', 1, 0, NOW(), 0, NOW(), 0, 'Task-07 demo data'),
    (8105, 'Qinghe transport support unit', 'support-unit', 3006, 1, '2026-04-08 07:58:00', NULL, 'Passenger flow overflow alert', 'monitoring', 'transport support', 'Support target around transport flow and activity window observation.', 1, 0, NOW(), 0, NOW(), 0, 'Task-07 demo data');
