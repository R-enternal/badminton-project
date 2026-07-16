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

-- 手机号唯一索引
ALTER TABLE sys_user ADD UNIQUE KEY uk_phone (phone);

-- 角色索引
ALTER TABLE sys_user ADD KEY idx_role (role);
