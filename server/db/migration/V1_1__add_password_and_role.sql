-- ========================================================
-- 迁移脚本：为用户表增加密码和角色字段
-- 适用场景：已有 v1.0 数据库需要升级到支持账号密码登录
-- ========================================================

USE badminton_db;

-- 增加密码字段
ALTER TABLE sys_user ADD COLUMN password VARCHAR(128) NOT NULL DEFAULT '' COMMENT 'BCrypt加密密码' AFTER phone;

-- 增加角色字段
ALTER TABLE sys_user ADD COLUMN role VARCHAR(32) NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN/COACH' AFTER password;

-- openid 改为可空，因为先支持账号密码登录，后期微信登录再填充
ALTER TABLE sys_user MODIFY COLUMN openid VARCHAR(64) DEFAULT NULL COMMENT '微信openid（后期微信登录时填充）';

-- 手机号改为可空，账号密码登录时必填，微信登录用户可为空
ALTER TABLE sys_user MODIFY COLUMN phone VARCHAR(20) DEFAULT NULL COMMENT '手机号，作为登录账号';

-- 将空字符串手机号转为 NULL，避免唯一索引冲突
UPDATE sys_user SET phone = NULL WHERE phone = '';

-- 处理重复手机号：保留 id 最小的一条，其余暂时置为 NULL
-- 后续可在管理后台人工补全或让用户重新绑定
UPDATE sys_user u1
    JOIN (
        SELECT phone, MIN(id) AS min_id
        FROM sys_user
        WHERE phone IS NOT NULL
        GROUP BY phone
        HAVING COUNT(*) > 1
    ) u2 ON u1.phone = u2.phone AND u1.id > u2.min_id
SET u1.phone = NULL;

-- 手机号唯一索引
ALTER TABLE sys_user ADD UNIQUE KEY uk_phone (phone);

-- 角色索引
ALTER TABLE sys_user ADD KEY idx_role (role);
