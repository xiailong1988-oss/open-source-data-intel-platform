-- Task-10: initialize default roles and administrator account

INSERT INTO sys_role (
    role_code,
    role_name,
    status,
    created_by,
    updated_by,
    remark
)
SELECT
    'ADMIN',
    '系统管理员',
    1,
    0,
    0,
    'Task-10 default role'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role
    WHERE role_code = 'ADMIN'
      AND deleted = 0
);

INSERT INTO sys_role (
    role_code,
    role_name,
    status,
    created_by,
    updated_by,
    remark
)
SELECT
    'ANALYST',
    '情报分析员',
    1,
    0,
    0,
    'Task-10 basic role'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role
    WHERE role_code = 'ANALYST'
      AND deleted = 0
);

INSERT INTO sys_user (
    username,
    password,
    real_name,
    phone,
    email,
    status,
    created_by,
    updated_by,
    remark
)
SELECT
    'admin',
    'ad89b64d66caa8e30e5d5ce4a9763f4ecc205814c412175f3e2c50027471426d',
    '默认管理员',
    '',
    'admin@local.test',
    1,
    0,
    0,
    'Task-10 default administrator'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_user
    WHERE username = 'admin'
      AND deleted = 0
);

INSERT INTO sys_user_role (
    user_id,
    role_id,
    created_by,
    updated_by,
    remark
)
SELECT
    user_item.id,
    role_item.id,
    0,
    0,
    'Task-10 default administrator role binding'
FROM sys_user user_item
JOIN sys_role role_item ON role_item.role_code = 'ADMIN' AND role_item.deleted = 0
WHERE user_item.username = 'admin'
  AND user_item.deleted = 0
  AND NOT EXISTS (
      SELECT 1
      FROM sys_user_role relation_item
      WHERE relation_item.user_id = user_item.id
        AND relation_item.role_id = role_item.id
        AND relation_item.deleted = 0
  );
