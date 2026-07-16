-- ========================================================
-- 羽毛球馆综合服务系统 - 数据库设计
-- 数据库：badminton_db
-- 版本：1.0
-- 说明：包含用户、场地预约、教练课程、商品商城、支付等全量表结构
-- ========================================================

CREATE DATABASE IF NOT EXISTS badminton_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE badminton_db;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    openid VARCHAR(64) DEFAULT NULL COMMENT '微信openid（后期微信登录时填充）',
    unionid VARCHAR(64) DEFAULT NULL COMMENT '微信unionid',
    nickname VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号，作为登录账号',
    password VARCHAR(128) NOT NULL DEFAULT '' COMMENT 'BCrypt加密密码',
    role VARCHAR(32) NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN/COACH',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0否 1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_openid (openid),
    UNIQUE KEY uk_phone (phone),
    KEY idx_role (role),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 管理后台用户表
CREATE TABLE IF NOT EXISTS sys_admin (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    username VARCHAR(32) NOT NULL COMMENT '账号',
    password VARCHAR(128) NOT NULL COMMENT '加密密码',
    real_name VARCHAR(32) DEFAULT NULL COMMENT '姓名',
    role VARCHAR(32) NOT NULL DEFAULT 'STAFF' COMMENT '角色：SUPER/ADMIN/STAFF',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 3. 场地表
CREATE TABLE IF NOT EXISTS venue (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '场地ID',
    name VARCHAR(64) NOT NULL COMMENT '场地名称，如A馆1号场',
    location VARCHAR(128) DEFAULT NULL COMMENT '位置描述',
    price_per_hour DECIMAL(10,2) NOT NULL COMMENT '每小时单价',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0关闭 1开放',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_status_sort (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场地表';

-- 4. 教练表
CREATE TABLE IF NOT EXISTS coach (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '教练ID',
    name VARCHAR(32) NOT NULL COMMENT '教练姓名',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像',
    phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    intro TEXT COMMENT '个人介绍',
    specialty VARCHAR(255) DEFAULT NULL COMMENT '擅长领域，逗号分隔',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_status_sort (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教练表';

-- 5. 教练课程表
CREATE TABLE IF NOT EXISTS coach_course (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程ID',
    coach_id BIGINT NOT NULL COMMENT '教练ID',
    name VARCHAR(64) NOT NULL COMMENT '课程名称',
    category VARCHAR(32) NOT NULL COMMENT '分类：成人/青少年/1对1/团体',
    duration_minutes INT NOT NULL DEFAULT 60 COMMENT '单次时长（分钟）',
    price DECIMAL(10,2) NOT NULL COMMENT '单次价格',
    description TEXT COMMENT '课程说明',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0下架 1上架',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_coach_category (coach_id, category),
    CONSTRAINT fk_coach_course_coach FOREIGN KEY (coach_id) REFERENCES coach(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教练课程表';

-- 6. 商品分类表
CREATE TABLE IF NOT EXISTS product_category (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(32) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID，0为一级',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_parent_sort (parent_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 7. 商品SPU表
CREATE TABLE IF NOT EXISTS product_spu (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'SPU_ID',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    name VARCHAR(128) NOT NULL COMMENT '商品名称',
    subtitle VARCHAR(255) DEFAULT NULL COMMENT '副标题',
    main_image VARCHAR(255) DEFAULT NULL COMMENT '主图',
    detail TEXT COMMENT '详情富文本',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0下架 1上架',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_category_status (category_id, status),
    KEY idx_sort (sort_order),
    CONSTRAINT fk_spu_category FOREIGN KEY (category_id) REFERENCES product_category(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SPU表';

-- 8. 场地时段表（运营提前配置某天某场地有哪些时段可约）
CREATE TABLE IF NOT EXISTS venue_slot (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '时段ID',
    venue_id BIGINT NOT NULL COMMENT '场地ID',
    booking_date DATE NOT NULL COMMENT '预约日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    price DECIMAL(10,2) NOT NULL COMMENT '该时段售价',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0关闭 1可约 2已约',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_venue_date_time (venue_id, booking_date, start_time),
    KEY idx_date_status (booking_date, status),
    KEY idx_venue_date (venue_id, booking_date),
    CONSTRAINT fk_slot_venue FOREIGN KEY (venue_id) REFERENCES venue(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场地时段表';

-- 9. 教练排班表
CREATE TABLE IF NOT EXISTS coach_schedule (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '排班ID',
    coach_id BIGINT NOT NULL COMMENT '教练ID',
    work_date DATE NOT NULL COMMENT '工作日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    course_id BIGINT DEFAULT NULL COMMENT '关联课程ID，空表示通用可约',
    is_booked TINYINT NOT NULL DEFAULT 0 COMMENT '是否被预约：0否 1是',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_coach_date_time (coach_id, work_date, start_time),
    KEY idx_date_booked (work_date, is_booked),
    CONSTRAINT fk_schedule_coach FOREIGN KEY (coach_id) REFERENCES coach(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_schedule_course FOREIGN KEY (course_id) REFERENCES coach_course(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教练排班表';

-- 10. 商品SKU表
CREATE TABLE IF NOT EXISTS product_sku (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'SKU_ID',
    spu_id BIGINT NOT NULL COMMENT 'SPU_ID',
    sku_code VARCHAR(64) NOT NULL COMMENT 'SKU编码',
    specs JSON NOT NULL COMMENT '规格组合 {"颜色":"红色","尺码":"L"}',
    specs_hash VARCHAR(64) AS (MD5(CAST(specs AS CHAR))) STORED COMMENT '规格组合哈希，用于唯一索引',
    price DECIMAL(10,2) NOT NULL COMMENT '售价',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0下架 1上架',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sku_code (sku_code),
    UNIQUE KEY uk_spu_specs (spu_id, specs_hash),
    KEY idx_spu_status (spu_id, status),
    CONSTRAINT fk_sku_spu FOREIGN KEY (spu_id) REFERENCES product_spu(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SKU表';

-- 11. 场地预约订单表
CREATE TABLE IF NOT EXISTS venue_booking (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL COMMENT '业务订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    venue_id BIGINT NOT NULL COMMENT '场地ID',
    slot_id BIGINT NOT NULL COMMENT '时段ID',
    booking_date DATE NOT NULL COMMENT '预约日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    amount DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    pay_amount DECIMAL(10,2) DEFAULT NULL COMMENT '实付金额',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待支付 1已支付 2已取消 3已核销',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    expire_time DATETIME NOT NULL COMMENT '订单过期时间（默认15分钟）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    UNIQUE KEY uk_slot_user (slot_id, user_id),
    KEY idx_user_status (user_id, status),
    KEY idx_expire_time (expire_time),
    CONSTRAINT fk_vb_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_vb_venue FOREIGN KEY (venue_id) REFERENCES venue(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_vb_slot FOREIGN KEY (slot_id) REFERENCES venue_slot(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场地预约订单表';

-- 12. 私教课程预约订单表
CREATE TABLE IF NOT EXISTS coach_booking (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL COMMENT '业务订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    coach_id BIGINT NOT NULL COMMENT '教练ID',
    schedule_id BIGINT NOT NULL COMMENT '排班ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    work_date DATE NOT NULL COMMENT '工作日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    amount DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    pay_amount DECIMAL(10,2) DEFAULT NULL COMMENT '实付金额',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待支付 1已支付 2已取消 3已完成',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    expire_time DATETIME NOT NULL COMMENT '订单过期时间（默认15分钟）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    UNIQUE KEY uk_schedule_user (schedule_id, user_id),
    KEY idx_user_status (user_id, status),
    CONSTRAINT fk_cb_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_cb_coach FOREIGN KEY (coach_id) REFERENCES coach(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_cb_schedule FOREIGN KEY (schedule_id) REFERENCES coach_schedule(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_cb_course FOREIGN KEY (course_id) REFERENCES coach_course(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私教课程预约订单表';

-- 13. 购物车表
CREATE TABLE IF NOT EXISTS cart_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '购物车项ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU_ID',
    spu_id BIGINT NOT NULL COMMENT 'SPU_ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    selected TINYINT NOT NULL DEFAULT 1 COMMENT '是否选中：0否 1是',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_sku (user_id, sku_id),
    KEY idx_user_selected (user_id, selected),
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_cart_sku FOREIGN KEY (sku_id) REFERENCES product_sku(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_cart_spu FOREIGN KEY (spu_id) REFERENCES product_spu(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 14. 商城订单表
CREATE TABLE IF NOT EXISTS shop_order (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL COMMENT '业务订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '商品总金额',
    pay_amount DECIMAL(10,2) NOT NULL COMMENT '应付金额',
    freight_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '运费',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待付款 1已付款 2已发货 3已收货 4已完成 5已取消',
    receiver_name VARCHAR(32) DEFAULT NULL COMMENT '收货人',
    receiver_phone VARCHAR(20) DEFAULT NULL COMMENT '收货电话',
    receiver_address VARCHAR(255) DEFAULT NULL COMMENT '收货地址',
    remark VARCHAR(255) DEFAULT NULL COMMENT '用户备注',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    ship_time DATETIME DEFAULT NULL COMMENT '发货时间',
    receive_time DATETIME DEFAULT NULL COMMENT '收货时间',
    cancel_time DATETIME DEFAULT NULL COMMENT '取消时间',
    expire_time DATETIME NOT NULL COMMENT '订单过期时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_status (user_id, status),
    KEY idx_expire_time (expire_time),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城订单表';

-- 15. 商城订单明细表
CREATE TABLE IF NOT EXISTS shop_order_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL COMMENT '业务订单号',
    sku_id BIGINT NOT NULL COMMENT 'SKU_ID',
    spu_id BIGINT NOT NULL COMMENT 'SPU_ID',
    spu_name VARCHAR(128) NOT NULL COMMENT '商品名称冗余',
    sku_specs JSON NOT NULL COMMENT '规格冗余',
    sku_image VARCHAR(255) DEFAULT NULL COMMENT 'SKU图片冗余',
    price DECIMAL(10,2) NOT NULL COMMENT '下单时单价',
    quantity INT NOT NULL COMMENT '数量',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '小计',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_order_id (order_id),
    CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES shop_order(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_item_sku FOREIGN KEY (sku_id) REFERENCES product_sku(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_item_spu FOREIGN KEY (spu_id) REFERENCES product_spu(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城订单明细表';

-- 16. 支付流水表
CREATE TABLE IF NOT EXISTS payment_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '支付ID',
    payment_no VARCHAR(32) NOT NULL COMMENT '内部支付流水号',
    out_trade_no VARCHAR(32) NOT NULL COMMENT '业务订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_type VARCHAR(16) NOT NULL COMMENT '订单类型：VENUE/COACH/SHOP',
    channel VARCHAR(16) NOT NULL COMMENT '支付渠道：WECHAT_JSAPI',
    amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待支付 1支付中 2成功 3失败',
    transaction_id VARCHAR(64) DEFAULT NULL COMMENT '微信支付流水号',
    prepay_id VARCHAR(64) DEFAULT NULL COMMENT '微信预支付ID',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_no (payment_no),
    UNIQUE KEY uk_transaction_id (transaction_id),
    KEY idx_out_trade_no (out_trade_no),
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付流水表';

-- 17. 微信支付回调记录表
CREATE TABLE IF NOT EXISTS wechat_pay_notify (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    out_trade_no VARCHAR(32) NOT NULL COMMENT '业务订单号',
    transaction_id VARCHAR(64) NOT NULL COMMENT '微信支付流水号',
    notify_body TEXT NOT NULL COMMENT '微信回调完整报文',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待处理 1处理成功 2处理失败',
    process_result VARCHAR(255) DEFAULT NULL COMMENT '处理结果说明',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_transaction_id (transaction_id),
    KEY idx_out_trade_no (out_trade_no),
    CONSTRAINT fk_notify_payment FOREIGN KEY (transaction_id) REFERENCES payment_record(transaction_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信支付回调记录表';
