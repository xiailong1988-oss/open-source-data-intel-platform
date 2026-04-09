-- Task-02：创建系统基础表、地区基础表、地图基础表与事件类型表。

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    username VARCHAR(64) NOT NULL COMMENT '登录账号',
    password VARCHAR(255) NOT NULL COMMENT '登录密码密文',
    real_name VARCHAR(64) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(32) NOT NULL DEFAULT '' COMMENT '手机号',
    email VARCHAR(128) NOT NULL DEFAULT '' COMMENT '邮箱',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
    last_login_time DATETIME NULL COMMENT '最近登录时间',
    created_by BIGINT NOT NULL DEFAULT 0 COMMENT '创建人',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT NOT NULL DEFAULT 0 COMMENT '修改人',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    remark VARCHAR(500) NOT NULL DEFAULT '' COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
    created_by BIGINT NOT NULL DEFAULT 0 COMMENT '创建人',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT NOT NULL DEFAULT 0 COMMENT '修改人',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    remark VARCHAR(500) NOT NULL DEFAULT '' COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统角色表';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_by BIGINT NOT NULL DEFAULT 0 COMMENT '创建人',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT NOT NULL DEFAULT 0 COMMENT '修改人',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    remark VARCHAR(500) NOT NULL DEFAULT '' COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS sys_dict_type (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    dict_code VARCHAR(64) NOT NULL COMMENT '字典编码',
    dict_name VARCHAR(128) NOT NULL COMMENT '字典名称',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
    created_by BIGINT NOT NULL DEFAULT 0 COMMENT '创建人',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT NOT NULL DEFAULT 0 COMMENT '修改人',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    remark VARCHAR(500) NOT NULL DEFAULT '' COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dict_code (dict_code),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典类型表';

CREATE TABLE IF NOT EXISTS sys_dict_data (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    type_id BIGINT NOT NULL COMMENT '字典类型ID',
    item_label VARCHAR(128) NOT NULL COMMENT '展示名称',
    item_value VARCHAR(128) NOT NULL COMMENT '实际值',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
    ext_json JSON NULL COMMENT '扩展信息',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
    created_by BIGINT NOT NULL DEFAULT 0 COMMENT '创建人',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT NOT NULL DEFAULT 0 COMMENT '修改人',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    remark VARCHAR(500) NOT NULL DEFAULT '' COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_type_value (type_id, item_value),
    KEY idx_type_id (type_id),
    KEY idx_type_sort (type_id, sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典数据表';

CREATE TABLE IF NOT EXISTS sys_oper_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    module_name VARCHAR(128) NOT NULL COMMENT '模块名称',
    action_name VARCHAR(128) NOT NULL COMMENT '操作名称',
    operator_id BIGINT NOT NULL DEFAULT 0 COMMENT '操作人ID',
    operator_name VARCHAR(64) NOT NULL DEFAULT '' COMMENT '操作人名称',
    request_uri VARCHAR(255) NOT NULL DEFAULT '' COMMENT '请求地址',
    request_method VARCHAR(16) NOT NULL DEFAULT '' COMMENT '请求方法',
    request_param TEXT NULL COMMENT '请求参数',
    response_data TEXT NULL COMMENT '返回结果',
    success_flag TINYINT NOT NULL DEFAULT 1 COMMENT '是否成功：1成功 0失败',
    operate_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    created_by BIGINT NOT NULL DEFAULT 0 COMMENT '创建人',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT NOT NULL DEFAULT 0 COMMENT '修改人',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    remark VARCHAR(500) NOT NULL DEFAULT '' COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_operator_id (operator_id),
    KEY idx_operate_time (operate_time),
    KEY idx_success_flag (success_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';

CREATE TABLE IF NOT EXISTS biz_region (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父级ID',
    region_code VARCHAR(64) NOT NULL COMMENT '地区编码',
    region_name VARCHAR(128) NOT NULL COMMENT '地区名称',
    region_level INT NOT NULL COMMENT '地区层级',
    region_type VARCHAR(64) NOT NULL COMMENT '地区类型',
    center_lng DECIMAL(10, 6) NOT NULL DEFAULT 0.000000 COMMENT '中心经度',
    center_lat DECIMAL(10, 6) NOT NULL DEFAULT 0.000000 COMMENT '中心纬度',
    zoom_level INT NOT NULL DEFAULT 11 COMMENT '默认缩放级别',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：1启用 0停用',
    created_by BIGINT NOT NULL DEFAULT 0 COMMENT '创建人',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT NOT NULL DEFAULT 0 COMMENT '修改人',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    remark VARCHAR(500) NOT NULL DEFAULT '' COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_region_code (region_code),
    KEY idx_parent_id (parent_id),
    KEY idx_region_level (region_level),
    KEY idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='地区基础表';

CREATE TABLE IF NOT EXISTS biz_region_geo (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    region_id BIGINT NOT NULL COMMENT '地区ID',
    geo_json LONGTEXT NOT NULL COMMENT '原始地区边界GeoJSON',
    simplified_geo_json LONGTEXT NULL COMMENT '简化地区边界GeoJSON',
    version_no INT NOT NULL DEFAULT 1 COMMENT '版本号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
    created_by BIGINT NOT NULL DEFAULT 0 COMMENT '创建人',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT NOT NULL DEFAULT 0 COMMENT '修改人',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    remark VARCHAR(500) NOT NULL DEFAULT '' COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_region_id (region_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='地区边界表';

CREATE TABLE IF NOT EXISTS biz_map_layer (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    layer_code VARCHAR(64) NOT NULL COMMENT '图层编码',
    layer_name VARCHAR(128) NOT NULL COMMENT '图层名称',
    layer_type VARCHAR(64) NOT NULL COMMENT '图层类型',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：1启用 0停用',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
    ext_json JSON NULL COMMENT '样式扩展配置',
    created_by BIGINT NOT NULL DEFAULT 0 COMMENT '创建人',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT NOT NULL DEFAULT 0 COMMENT '修改人',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    remark VARCHAR(500) NOT NULL DEFAULT '' COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_layer_code (layer_code),
    KEY idx_layer_type (layer_type),
    KEY idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='地图图层配置表';

CREATE TABLE IF NOT EXISTS biz_event_type (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    event_code VARCHAR(64) NOT NULL COMMENT '事件类型编码',
    event_name VARCHAR(128) NOT NULL COMMENT '事件类型名称',
    event_category VARCHAR(64) NOT NULL COMMENT '事件分类',
    color_code VARCHAR(32) NOT NULL DEFAULT '' COMMENT '前端展示色值',
    icon_code VARCHAR(64) NOT NULL DEFAULT '' COMMENT '图标编码',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：1启用 0停用',
    created_by BIGINT NOT NULL DEFAULT 0 COMMENT '创建人',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT NOT NULL DEFAULT 0 COMMENT '修改人',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    remark VARCHAR(500) NOT NULL DEFAULT '' COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_event_code (event_code),
    KEY idx_event_category (event_category),
    KEY idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='事件类型表';
