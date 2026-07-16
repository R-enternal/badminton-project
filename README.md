# badminton-mini / badminton-server 完整开发方案

> 角色：资深程序员输出给产品经理的完整可执行方案  
> 版本：v1.0  
> 日期：2026-07-15

---

## 1. 项目概述

### 1.1 产品定位

羽毛球馆综合服务小程序，面向 C 端用户，提供：

1. **场地预约**：按小时查询场地占用情况，选择时段并支付。
2. **教练与课程**：查看教练头像、介绍、课程分类，预约单次课程。
3. **商品商城**：商品列表（分类/搜索/排序）、商品详情（规格/库存）、购物车、下单、微信支付、物流状态追踪。

### 1.2 技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| 后端 | Spring Boot 3.x + MyBatis Plus | JDK 17+，统一 RESTful API |
| 数据库 | MySQL 8.0 | 主数据库，InnoDB 引擎 |
| 缓存/锁 | Redis 7.x | 缓存热点数据 + Redisson 分布式锁 |
| 管理后台 | Vue 3 + Element Plus | 运营人员管理场馆、教练、商品、订单 |
| 接口文档 | Knife4j 4.x | 基于 Swagger 的增强版接口文档 |
| 小程序 | UniApp 3 + Vue3 + Pinia | 跨端小程序 + H5 |
| 支付 | 微信支付 Native / JSAPI | 小程序内使用 JSAPI 支付 |

### 1.3 系统边界

- **本期不做**：会员等级体系、优惠券、积分、分销、退款流程、多门店。
- **本期做**：场地预约、教练课程预约、商品商城、微信支付、订单状态流转。

---

## 2. 数据库设计

### 2.1 设计原则

- **单表优先**：业务初期避免过度拆分，使用逻辑外键 + 应用层校验。
- **冗余可接受**：在订单表中冗余商品/场馆/教练名称，减少联表查询。
- **并发控制**：预约类业务使用数据库乐观锁 + Redis 分布式锁双重保障。
- **软删除**：所有业务表保留 `deleted` 字段，数据不物理删除。

### 2.2 表清单

| 序号 | 表名 | 用途 |
|------|------|------|
| 1 | `sys_user` | 用户表（微信登录） |
| 2 | `sys_admin` | 管理后台用户 |
| 3 | `venue` | 羽毛球场地 |
| 4 | `venue_slot` | 场地可预约时段模板 |
| 5 | `venue_booking` | 场地预约订单 |
| 6 | `coach` | 教练信息 |
| 7 | `coach_course` | 教练课程分类/价格 |
| 8 | `coach_schedule` | 教练排班 |
| 9 | `coach_booking` | 私教课程预约订单 |
| 10 | `product_category` | 商品分类 |
| 11 | `product_spu` | 商品 SPU |
| 12 | `product_sku` | 商品 SKU（规格） |
| 13 | `cart_item` | 购物车 |
| 14 | `shop_order` | 商城订单 |
| 15 | `shop_order_item` | 商城订单明细 |
| 16 | `payment_record` | 支付流水 |
| 17 | `wechat_pay_notify` | 微信支付回调记录 |

### 2.3 完整 DDL

```sql
-- 用户表
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `openid` VARCHAR(64) NOT NULL COMMENT '微信openid',
  `unionid` VARCHAR(64) DEFAULT NULL COMMENT '微信unionid',
  `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  KEY `idx_phone` (`phone`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 管理后台用户
CREATE TABLE `sys_admin` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(32) NOT NULL COMMENT '账号',
  `password` VARCHAR(128) NOT NULL COMMENT '加密密码',
  `real_name` VARCHAR(32) DEFAULT NULL COMMENT '姓名',
  `role` VARCHAR(32) NOT NULL DEFAULT 'STAFF' COMMENT '角色：SUPER/ADMIN/STAFF',
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 场地表
CREATE TABLE `venue` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(64) NOT NULL COMMENT '场地名称，如A馆1号场',
  `location` VARCHAR(128) DEFAULT NULL COMMENT '位置描述',
  `price_per_hour` DECIMAL(10,2) NOT NULL COMMENT '每小时单价',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0关闭 1开放',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场地表';

-- 场地时段模板（运营提前配置某天某场地有哪些时段可约）
CREATE TABLE `venue_slot` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `venue_id` BIGINT NOT NULL COMMENT '场地ID',
  `booking_date` DATE NOT NULL COMMENT '预约日期',
  `start_time` TIME NOT NULL COMMENT '开始时间',
  `end_time` TIME NOT NULL COMMENT '结束时间',
  `price` DECIMAL(10,2) NOT NULL COMMENT '该时段售价',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0关闭 1可约 2已约',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_venue_date_time` (`venue_id`, `booking_date`, `start_time`),
  KEY `idx_date_status` (`booking_date`, `status`),
  KEY `idx_venue_date` (`venue_id`, `booking_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场地时段表';

-- 场地预约订单
CREATE TABLE `venue_booking` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(32) NOT NULL COMMENT '业务订单号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `venue_id` BIGINT NOT NULL COMMENT '场地ID',
  `slot_id` BIGINT NOT NULL COMMENT '时段ID',
  `booking_date` DATE NOT NULL COMMENT '预约日期',
  `start_time` TIME NOT NULL COMMENT '开始时间',
  `end_time` TIME NOT NULL COMMENT '结束时间',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
  `pay_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '实付金额',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待支付 1已支付 2已取消 3已核销',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `expire_time` DATETIME NOT NULL COMMENT '订单过期时间（默认15分钟）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  UNIQUE KEY `uk_slot_user` (`slot_id`, `user_id`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场地预约订单表';

-- 教练表
CREATE TABLE `coach` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(32) NOT NULL COMMENT '教练姓名',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `intro` TEXT COMMENT '个人介绍',
  `specialty` VARCHAR(255) DEFAULT NULL COMMENT '擅长领域，逗号分隔',
  `status` TINYINT NOT NULL DEFAULT 1,
  `sort_order` INT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教练表';

-- 教练课程
CREATE TABLE `coach_course` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `coach_id` BIGINT NOT NULL COMMENT '教练ID',
  `name` VARCHAR(64) NOT NULL COMMENT '课程名称',
  `category` VARCHAR(32) NOT NULL COMMENT '分类：成人/青少年/1对1/团体',
  `duration_minutes` INT NOT NULL DEFAULT 60 COMMENT '单次时长（分钟）',
  `price` DECIMAL(10,2) NOT NULL COMMENT '单次价格',
  `description` TEXT COMMENT '课程说明',
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_coach_category` (`coach_id`, `category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教练课程表';

-- 教练排班
CREATE TABLE `coach_schedule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `coach_id` BIGINT NOT NULL COMMENT '教练ID',
  `work_date` DATE NOT NULL COMMENT '工作日期',
  `start_time` TIME NOT NULL COMMENT '开始时间',
  `end_time` TIME NOT NULL COMMENT '结束时间',
  `course_id` BIGINT DEFAULT NULL COMMENT '关联课程ID，空表示通用可约',
  `is_booked` TINYINT NOT NULL DEFAULT 0 COMMENT '是否被预约：0否 1是',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_coach_date_time` (`coach_id`, `work_date`, `start_time`),
  KEY `idx_date_booked` (`work_date`, `is_booked`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教练排班表';

-- 私教课程预约订单
CREATE TABLE `coach_booking` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(32) NOT NULL COMMENT '业务订单号',
  `user_id` BIGINT NOT NULL,
  `coach_id` BIGINT NOT NULL,
  `schedule_id` BIGINT NOT NULL COMMENT '排班ID',
  `course_id` BIGINT NOT NULL,
  `work_date` DATE NOT NULL,
  `start_time` TIME NOT NULL,
  `end_time` TIME NOT NULL,
  `amount` DECIMAL(10,2) NOT NULL,
  `pay_amount` DECIMAL(10,2) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待支付 1已支付 2已取消 3已完成',
  `pay_time` DATETIME DEFAULT NULL,
  `expire_time` DATETIME NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  UNIQUE KEY `uk_schedule_user` (`schedule_id`, `user_id`),
  KEY `idx_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私教课程预约订单表';

-- 商品分类
CREATE TABLE `product_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(32) NOT NULL COMMENT '分类名称',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID，0为一级',
  `sort_order` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent_sort` (`parent_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 商品SPU
CREATE TABLE `product_spu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `name` VARCHAR(128) NOT NULL COMMENT '商品名称',
  `subtitle` VARCHAR(255) DEFAULT NULL COMMENT '副标题',
  `main_image` VARCHAR(255) DEFAULT NULL COMMENT '主图',
  `detail` TEXT COMMENT '详情富文本',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0下架 1上架',
  `sort_order` INT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_category_status` (`category_id`, `status`),
  KEY `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SPU表';

-- 商品SKU
CREATE TABLE `product_sku` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `spu_id` BIGINT NOT NULL COMMENT 'SPU_ID',
  `sku_code` VARCHAR(64) NOT NULL COMMENT 'SKU编码',
  `specs` JSON NOT NULL COMMENT '规格组合 {"颜色":"红色","尺码":"L"}',
  `price` DECIMAL(10,2) NOT NULL COMMENT '售价',
  `stock` INT NOT NULL DEFAULT 0 COMMENT '库存',
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sku_code` (`sku_code`),
  UNIQUE KEY `uk_spu_specs` (`spu_id`, (CAST(`specs` AS CHAR(255) ARRAY))),
  KEY `idx_spu_status` (`spu_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SKU表';

-- 购物车
CREATE TABLE `cart_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `sku_id` BIGINT NOT NULL,
  `spu_id` BIGINT NOT NULL,
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `selected` TINYINT NOT NULL DEFAULT 1 COMMENT '是否选中：0否 1是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_sku` (`user_id`, `sku_id`),
  KEY `idx_user_selected` (`user_id`, `selected`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 商城订单
CREATE TABLE `shop_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(32) NOT NULL COMMENT '业务订单号',
  `user_id` BIGINT NOT NULL,
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '商品总金额',
  `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '应付金额',
  `freight_amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '运费',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待付款 1已付款 2已发货 3已收货 4已完成 5已取消',
  `receiver_name` VARCHAR(32) DEFAULT NULL COMMENT '收货人',
  `receiver_phone` VARCHAR(20) DEFAULT NULL COMMENT '收货电话',
  `receiver_address` VARCHAR(255) DEFAULT NULL COMMENT '收货地址',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '用户备注',
  `pay_time` DATETIME DEFAULT NULL,
  `ship_time` DATETIME DEFAULT NULL,
  `receive_time` DATETIME DEFAULT NULL,
  `cancel_time` DATETIME DEFAULT NULL,
  `expire_time` DATETIME NOT NULL COMMENT '订单过期时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城订单表';

-- 商城订单明细
CREATE TABLE `shop_order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `order_no` VARCHAR(32) NOT NULL,
  `sku_id` BIGINT NOT NULL,
  `spu_id` BIGINT NOT NULL,
  `spu_name` VARCHAR(128) NOT NULL COMMENT '商品名称冗余',
  `sku_specs` JSON NOT NULL COMMENT '规格冗余',
  `sku_image` VARCHAR(255) DEFAULT NULL,
  `price` DECIMAL(10,2) NOT NULL COMMENT '下单时单价',
  `quantity` INT NOT NULL COMMENT '数量',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '小计',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城订单明细表';

-- 支付流水
CREATE TABLE `payment_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `payment_no` VARCHAR(32) NOT NULL COMMENT '内部支付流水号',
  `out_trade_no` VARCHAR(32) NOT NULL COMMENT '业务订单号',
  `user_id` BIGINT NOT NULL,
  `channel` VARCHAR(16) NOT NULL COMMENT '支付渠道：WECHAT_JSAPI',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待支付 1支付中 2成功 3失败',
  `transaction_id` VARCHAR(64) DEFAULT NULL COMMENT '微信支付流水号',
  `prepay_id` VARCHAR(64) DEFAULT NULL COMMENT '微信预支付ID',
  `pay_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  UNIQUE KEY `uk_transaction_id` (`transaction_id`),
  KEY `idx_out_trade_no` (`out_trade_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付流水表';

-- 微信支付回调记录
CREATE TABLE `wechat_pay_notify` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `out_trade_no` VARCHAR(32) NOT NULL,
  `transaction_id` VARCHAR(64) NOT NULL,
  `notify_body` TEXT NOT NULL COMMENT '微信回调完整报文',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待处理 1处理成功 2处理失败',
  `process_result` VARCHAR(255) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transaction_id` (`transaction_id`),
  KEY `idx_out_trade_no` (`out_trade_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信支付回调记录表';
```

### 2.4 ER 图文字描述

```
sys_user 1 ──────── N venue_booking
                    N coach_booking
                    N cart_item
                    N shop_order
                    N payment_record

venue 1 ─────────── N venue_slot 1 ─────────── 1 venue_booking（一个时段只能被一个人约）

coach 1 ─────────── N coach_course
coach 1 ─────────── N coach_schedule 1 ─────── 1 coach_booking

coach_course 1 ───── N coach_schedule

product_category 1 ── N product_spu 1 ──────── N product_sku 1 ───── N cart_item
                                                                  N shop_order_item

shop_order 1 ─────── N shop_order_item
shop_order 1 ─────── 1 payment_record
shop_order 1 ─────── N wechat_pay_notify
```

### 2.5 预约订单表并发控制字段设计说明

**场地预约 `venue_slot` 表关键字段：**

- `status`：0关闭 1可约 2已约。查询时只显示 `status=1` 的时段。
- `version`：乐观锁版本号。更新状态时 `UPDATE venue_slot SET status=2, version=version+1 WHERE id=? AND version=?`。

**为什么同时用乐观锁 + 分布式锁？**

| 方案 | 优点 | 缺点 |
|------|------|------|
| 仅乐观锁 | 数据库原生支持，无外部依赖 | 高并发下大量更新失败，用户体验差 |
| 仅 Redis 分布式锁 | 把冲突拦截在数据库外，吞吐高 | Redis 故障或网络抖动可能丢锁 |
| 两者结合 | 分布式锁过滤绝大多数并发，乐观锁做最后一道防线 | 实现稍复杂 |

**推荐**：两者结合。Redis 锁控制"同时只有一人能尝试预约某个时段"，数据库乐观锁保证极端情况下的数据正确性。

---

## 3. 后端接口设计

### 3.1 接口全局约定

- 统一前缀：`/api`
- 统一响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

- 认证：JWT Bearer Token，登录接口除外。
- 时间格式：`yyyy-MM-dd HH:mm:ss`
- 分页参数：`page`（从1开始）、`size`（默认10）

### 3.2 用户模块

| URL | Method | 功能 | 请求参数 | 返回 |
|-----|--------|------|----------|------|
| `/api/user/login` | POST | 微信登录 | `{ "code": "xxx" }` | `{ token, userInfo }` |
| `/api/user/info` | GET | 获取当前用户信息 | Header Token | `UserInfo` |
| `/api/user/logout` | POST | 退出登录 | Header Token | `boolean` |

### 3.3 场地预约模块

| URL | Method | 功能 | 请求参数 | 返回 |
|-----|--------|------|----------|------|
| `/api/venue/list` | GET | 场地列表 | 无 | `List<VenueVO>` |
| `/api/venue/slots` | GET | 某日所有场地时段 | `date` | `List<VenueSlotVO>` |
| `/api/venue/booking/prepare` | POST | 预占时段生成订单 | `{ slotId }` | `VenueBookingVO` |
| `/api/venue/booking/pay` | POST | 调起支付 | `{ orderNo }` | `PayParamsVO` |
| `/api/venue/booking/my` | GET | 我的预约列表 | `status?` | `List<VenueBookingVO>` |
| `/api/venue/booking/cancel/{id}` | POST | 取消预约 | `id` | `boolean` |

**核心业务代码：场地预约（含 Redis 分布式锁）**

```java
@Service
@RequiredArgsConstructor
public class VenueBookingServiceImpl implements VenueBookingService {

    private final RedissonClient redissonClient;
    private final VenueSlotMapper venueSlotMapper;
    private final VenueBookingMapper venueBookingMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VenueBookingVO prepareBooking(Long userId, Long slotId) {
        // 1. 构造 Redis 锁 key：每个时段一把锁
        String lockKey = "lock:venue:slot:" + slotId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            // 2. 尝试加锁，最多等 3 秒，持锁 10 秒（看门狗会自动续期）
            locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException("当前时段过于火爆，请刷新后重试");
            }

            // 3. 查询时段，使用悲观锁或乐观锁查询
            VenueSlot slot = venueSlotMapper.selectById(slotId);
            if (slot == null || slot.getStatus() != 1) {
                throw new BusinessException("该时段已被预约或已关闭");
            }

            // 4. 检查用户是否已预约该时段
            Long count = venueBookingMapper.selectCount(
                new LambdaQueryWrapper<VenueBooking>()
                    .eq(VenueBooking::getSlotId, slotId)
                    .eq(VenueBooking::getUserId, userId)
                    .in(VenueBooking::getStatus, 0, 1, 3)
            );
            if (count > 0) {
                throw new BusinessException("您已预约该时段");
            }

            // 5. 乐观锁更新时段状态为已约
            int affected = venueSlotMapper.update(null,
                new LambdaUpdateWrapper<VenueSlot>()
                    .eq(VenueBooking::getId, slotId)
                    .eq(VenueSlot::getStatus, 1)
                    .eq(VenueSlot::getVersion, slot.getVersion())
                    .set(VenueSlot::getStatus, 2)
                    .setSql("version = version + 1")
            );
            if (affected == 0) {
                throw new BusinessException("预约失败，该时段刚被他人预约");
            }

            // 6. 生成待支付订单
            VenueBooking booking = new VenueBooking();
            booking.setOrderNo(generateOrderNo());
            booking.setUserId(userId);
            booking.setVenueId(slot.getVenueId());
            booking.setSlotId(slotId);
            booking.setBookingDate(slot.getBookingDate());
            booking.setStartTime(slot.getStartTime());
            booking.setEndTime(slot.getEndTime());
            booking.setAmount(slot.getPrice());
            booking.setStatus(0);
            booking.setExpireTime(LocalDateTime.now().plusMinutes(15));
            venueBookingMapper.insert(booking);

            return convertToVO(booking);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("系统繁忙，请稍后重试");
        } finally {
            // 7. 只有当前线程持有锁时才释放
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
```

### 3.4 私教课程模块

| URL | Method | 功能 | 请求参数 | 返回 |
|-----|--------|------|----------|------|
| `/api/coach/list` | GET | 教练列表 | 无 | `List<CoachVO>` |
| `/api/coach/{id}` | GET | 教练详情 | `id` | `CoachDetailVO` |
| `/api/coach/schedule` | GET | 教练可约排期 | `coachId, date` | `List<CoachScheduleVO>` |
| `/api/coach/book` | POST | 预约私教课 | `{ scheduleId, courseId }` | `CoachBookingVO` |
| `/api/coach/booking/pay` | POST | 调起支付 | `{ orderNo }` | `PayParamsVO` |
| `/api/coach/booking/my` | GET | 我的课程预约 | `status?` | `List<CoachBookingVO>` |
| `/api/coach/booking/cancel/{id}` | POST | 取消预约 | `id` | `boolean` |

### 3.5 商城模块

| URL | Method | 功能 | 请求参数 | 返回 |
|-----|--------|------|----------|------|
| `/api/shop/categories` | GET | 商品分类 | `parentId?` | `List<CategoryVO>` |
| `/api/shop/products` | GET | 商品列表 | `categoryId?, keyword?, sort?, page, size` | `PageResult<ProductVO>` |
| `/api/shop/product/{id}` | GET | 商品详情 | `id` | `ProductDetailVO` |
| `/api/shop/cart/list` | GET | 购物车列表 | 无 | `List<CartItemVO>` |
| `/api/shop/cart/add` | POST | 加入购物车 | `{ skuId, quantity }` | `CartItemVO` |
| `/api/shop/cart/update` | PUT | 修改购物车数量 | `{ cartItemId, quantity }` | `boolean` |
| `/api/shop/cart/delete/{id}` | DELETE | 删除购物车项 | `id` | `boolean` |
| `/api/shop/cart/select` | POST | 选中/取消选中 | `{ cartItemId, selected }` | `boolean` |
| `/api/shop/order/create` | POST | 创建订单 | `{ cartItemIds, address, remark? }` | `ShopOrderVO` |
| `/api/shop/order/pay` | POST | 调起支付 | `{ orderNo }` | `PayParamsVO` |
| `/api/shop/order/my` | GET | 我的订单 | `status?, page, size` | `PageResult<ShopOrderVO>` |
| `/api/shop/order/{id}` | GET | 订单详情 | `id` | `ShopOrderDetailVO` |
| `/api/shop/order/receive/{id}` | POST | 确认收货 | `id` | `boolean` |

### 3.6 支付回调模块

| URL | Method | 功能 | 说明 |
|-----|--------|------|------|
| `/api/pay/notify/wechat` | POST | 微信支付回调 | 微信服务器主动推送 |

**微信支付回调完整处理流程：**

```java
@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
@Slf4j
public class PayNotifyController {

    private final WechatPayProperties wechatPayProperties;
    private final WechatPayVerifier verifier;
    private final PaymentService paymentService;

    @PostMapping("/notify/wechat")
    public ResponseEntity<String> wechatNotify(HttpServletRequest request) {
        try {
            // 1. 读取回调报文
            String body = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
            log.info("微信支付回调报文: {}", body);

            // 2. 构造通知对象并验签
            NotificationParser parser = new NotificationParser(
                new NotificationConfig(wechatPayProperties.getMchId(),
                    wechatPayProperties.getApiV3Key(),
                    wechatPayProperties.getNotifySerial(),
                    wechatPayProperties.getNotifyPubKey())
            );
            Transaction transaction = parser.parse(body, Transaction.class);

            // 3. 先保存原始回调记录（幂等第一步：按微信 transaction_id 唯一索引去重）
            WechatPayNotify notifyRecord = new WechatPayNotify();
            notifyRecord.setOutTradeNo(transaction.getOutTradeNo());
            notifyRecord.setTransactionId(transaction.getTransactionId());
            notifyRecord.setNotifyBody(body);
            paymentService.saveNotifyRecord(notifyRecord);

            // 4. 幂等处理：根据 transaction_id 查询是否已处理成功
            boolean processed = paymentService.isNotifyProcessed(transaction.getTransactionId());
            if (processed) {
                log.info("回调已处理，直接返回成功: {}", transaction.getTransactionId());
                return ResponseEntity.ok("{\"code\":\"SUCCESS\",\"message\":\"成功\"}");
            }

            // 5. 处理业务：更新支付流水 + 更新订单状态
            paymentService.handlePaySuccess(transaction);

            // 6. 返回微信成功响应
            return ResponseEntity.ok("{\"code\":\"SUCCESS\",\"message\":\"成功\"}");
        } catch (Exception e) {
            log.error("微信支付回调处理失败", e);
            return ResponseEntity.status(500).body("{\"code\":\"FAIL\",\"message\":\"处理失败\"}");
        }
    }
}
```

```java
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRecordMapper paymentRecordMapper;
    private final ShopOrderMapper shopOrderMapper;
    private final VenueBookingMapper venueBookingMapper;
    private final CoachBookingMapper coachBookingMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePaySuccess(Transaction transaction) {
        String outTradeNo = transaction.getOutTradeNo();

        // 1. 根据业务订单号查询支付流水，加锁防止并发重复处理
        PaymentRecord payment = paymentRecordMapper.selectOne(
            new LambdaQueryWrapper<PaymentRecord>()
                .eq(PaymentRecord::getOutTradeNo, outTradeNo)
                .last("FOR UPDATE")
        );
        if (payment == null || payment.getStatus() == 2) {
            // 已处理或不存在，直接返回
            return;
        }

        // 2. 更新支付流水
        payment.setStatus(2);
        payment.setTransactionId(transaction.getTransactionId());
        payment.setPayTime(LocalDateTime.now());
        paymentRecordMapper.updateById(payment);

        // 3. 根据订单号前缀判断业务类型，更新对应订单
        if (outTradeNo.startsWith("SV")) { // 场地预约订单
            venueBookingMapper.update(null,
                new LambdaUpdateWrapper<VenueBooking>()
                    .eq(VenueBooking::getOrderNo, outTradeNo)
                    .set(VenueBooking::getStatus, 1)
                    .set(VenueBooking::getPayAmount, payment.getAmount())
                    .set(VenueBooking::getPayTime, LocalDateTime.now())
            );
        } else if (outTradeNo.startsWith("SC")) { // 私教课程订单
            coachBookingMapper.update(null,
                new LambdaUpdateWrapper<CoachBooking>()
                    .eq(CoachBooking::getOrderNo, outTradeNo)
                    .set(CoachBooking::getStatus, 1)
                    .set(CoachBooking::getPayAmount, payment.getAmount())
                    .set(CoachBooking::getPayTime, LocalDateTime.now())
            );
        } else if (outTradeNo.startsWith("SO")) { // 商城订单
            shopOrderMapper.update(null,
                new LambdaUpdateWrapper<ShopOrder>()
                    .eq(ShopOrder::getOrderNo, outTradeNo)
                    .set(ShopOrder::getStatus, 1)
                    .set(ShopOrder::getPayTime, LocalDateTime.now())
            );
            // 扣减 SKU 库存已在下单时预占，这里可补充真实扣减逻辑
        }
    }
}
```

**为什么回调要幂等？**

- 微信可能会重复发送回调通知（网络超时、重试机制）。
- 如果不幂等，用户支付一次可能被重复计入多次，导致资损。
- 幂等三板斧：唯一索引去重 + 状态机判断 + 数据库行锁。

---

## 4. 小程序端页面结构

### 4.1 页面路由规划

```json
{
  "pages": [
    { "path": "pages/index/index" },
    { "path": "pages/venue/list" },
    { "path": "pages/venue/detail" },
    { "path": "pages/coach/list" },
    { "path": "pages/coach/detail" },
    { "path": "pages/shop/index" },
    { "path": "pages/shop/detail" },
    { "path": "pages/cart/index" },
    { "path": "pages/order/create" },
    { "path": "pages/order/list" },
    { "path": "pages/order/detail" },
    { "path": "pages/mine/index" },
    { "path": "pages/login/login" }
  ],
  "tabBar": {
    "list": [
      { "pagePath": "pages/index/index", "text": "首页" },
      { "pagePath": "pages/venue/list", "text": "预约" },
      { "pagePath": "pages/shop/index", "text": "商城" },
      { "pagePath": "pages/mine/index", "text": "我的" }
    ]
  }
}
```

### 4.2 每个页面的核心 UI 组件说明

| 页面 | 核心组件 | 说明 |
|------|----------|------|
| 首页 `index/index` | Banner、快捷入口、热门推荐 | 跳转到场地/教练/商城 |
| 场地列表 `venue/list` | DateSelector、VenueCard、TimeGrid | 横向日期选择，下方展示各场地时段 |
| 场地详情 `venue/detail` | ImageSwiper、InfoPanel、TimeGrid、BottomBar | 展示场地图片和时段选择 |
| 教练列表 `coach/list` | CoachCard、SearchBar | 卡片展示教练头像和擅长领域 |
| 教练详情 `coach/detail` | Avatar、IntroPanel、CourseTabs、ScheduleList、BottomBar | 教练介绍 + 课程分类 + 排期 |
| 商城首页 `shop/index` | CategoryTabs、SearchBar、SortBar、ProductGrid | 分类、搜索、排序 |
| 商品详情 `shop/detail` | ImageSwiper、PricePanel、SpecSelector、SkuStock、BottomBar | 规格选择、库存显示 |
| 购物车 `cart/index` | CartItem、SelectAllBar、BottomBar | 增删改、数量调整、全选 |
| 确认订单 `order/create` | AddressCard、OrderItemList、RemarkInput、SubmitBar | 下单确认 |
| 订单列表 `order/list` | OrderTabs、OrderCard | 按状态筛选 |
| 订单详情 `order/detail` | OrderTimeline、OrderItemList、LogisticsCard | 发货状态追踪 |
| 个人中心 `mine/index` | UserCard、MenuGrid | 订单/购物车/预约入口 |
| 登录页 `login/login` | WxLoginButton、ProtocolCheckbox | 微信一键登录 |

### 4.3 状态管理方案（Pinia）

```javascript
// src/store/index.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(uni.getStorageSync('token') || '')
  const userInfo = ref(null)

  const isLogin = computed(() => !!token.value)

  function setToken(val) {
    token.value = val
    uni.setStorageSync('token', val)
  }

  function setUserInfo(info) {
    userInfo.value = info
    uni.setStorageSync('userInfo', info)
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
  }

  async function restoreLogin() {
    const savedToken = uni.getStorageSync('token')
    const savedUser = uni.getStorageSync('userInfo')
    if (savedToken && savedUser) {
      token.value = savedToken
      userInfo.value = savedUser
    }
  }

  return { token, userInfo, isLogin, setToken, setUserInfo, logout, restoreLogin }
})
```

**为什么用 Pinia 不用 Vuex？**

| 维度 | Pinia | Vuex 4 |
|------|-------|--------|
| 语法 | Composition API 风格，更简洁 | Options 风格较繁琐 |
| TS 支持 | 更好 | 一般 |
| 体积 | 更小 | 稍大 |
| 推荐度 | Vue 官方推荐 | 维护模式 |

**结论**：新项目直接用 Pinia。

---

## 5. 核心技术难点实现

### 5.1 如何用 Redis 分布式锁保证场地预约的原子性

已在 3.3 节给出完整 Java 代码。这里补充关键设计决策：

**锁粒度**：每个 `slotId` 一把锁，锁 key 为 `lock:venue:slot:{slotId}`。

**为什么不用用户级锁？**

- 用户级锁：`lock:user:{userId}`，只能防止同一用户并发，无法防止多用户抢同一场地。
- 资源级锁：`lock:venue:slot:{slotId}`，精确锁住被竞争的资源，性能最好。

**Redisson 看门狗机制：**

- `tryLock(3, 10, TimeUnit.SECONDS)` 中 `leaseTime=10s` 会启用看门狗。
- 业务执行期间，Redisson 会自动续期，避免锁过期但业务未执行完。
- 业务结束后必须 `unlock()`，且判断 `isHeldByCurrentThread()` 防止误释放他人锁。

### 5.2 私教课教练排班的冲突检测算法

**场景**：用户选择教练 + 日期 + 课程，系统需确保该时段未被预约，且课程时长不跨出排班区间。

```java
@Service
@RequiredArgsConstructor
public class CoachScheduleServiceImpl implements CoachScheduleService {

    private final CoachScheduleMapper coachScheduleMapper;
    private final CoachCourseMapper coachCourseMapper;

    @Override
    public void bookSchedule(Long userId, Long scheduleId, Long courseId) {
        // 1. 查询排班
        CoachSchedule schedule = coachScheduleMapper.selectById(scheduleId);
        if (schedule == null || schedule.getIsBooked() == 1) {
            throw new BusinessException("该时段已被预约");
        }

        // 2. 查询课程时长
        CoachCourse course = coachCourseMapper.selectById(courseId);
        if (course == null || course.getStatus() != 1) {
            throw new BusinessException("课程不存在或已下架");
        }

        // 3. 计算课程结束时间
        LocalTime courseEndTime = schedule.getStartTime()
            .plusMinutes(course.getDurationMinutes());

        // 4. 判断课程是否超出排班区间
        if (courseEndTime.isAfter(schedule.getEndTime())) {
            throw new BusinessException("课程时长超出该排班可用时间");
        }

        // 5. 检查同教练同时间段是否有其他预约（数据库唯一索引兜底）
        // 使用 Redis 分布式锁控制并发，锁 key: lock:coach:schedule:{scheduleId}
        // ... 加锁、更新 is_booked=1、生成订单 ...
    }
}
```

**冲突检测规则：**

1. 排班本身未被预约（`is_booked=0`）。
2. 课程时长不超过排班可用时长。
3. 数据库唯一索引 `(coach_id, work_date, start_time)` 防止同一时段重复排班。
4. Redis 锁防止多用户同时预约同一排班。

### 5.3 购物车数据在本地持久化与云端同步的策略

**方案选择：**

| 方案 | 优点 | 缺点 | 推荐 |
|------|------|------|------|
| 仅云端 | 数据一致性强，多端同步 | 弱网/无网时无法操作 | 不推荐 |
| 仅本地 | 响应快，离线可用 | 多端不同步，换设备丢失 | 不推荐 |
| 本地优先 + 云端同步 | 体验好，最终一致 | 实现复杂 | **推荐** |

**推荐实现：**

```javascript
// src/composables/useCart.js
import { ref } from 'vue'
import { getCartList, addCartItem, updateCartItem, deleteCartItem } from '@/api/shop.js'

const CART_KEY = 'local_cart'

export function useCart() {
  const cartList = ref([])

  // 1. 初始化：先读本地缓存，再拉云端合并
  async function initCart() {
    const local = uni.getStorageSync(CART_KEY) || []
    try {
      const remote = await getCartList()
      cartList.value = mergeCart(local, remote)
      uni.setStorageSync(CART_KEY, cartList.value)
      // 将本地新增项同步到云端
      await syncToCloud(local, remote)
    } catch (e) {
      cartList.value = local
    }
  }

  // 2. 添加购物车：先改本地，再异步调接口
  async function addToCart(skuId, quantity = 1) {
    const existing = cartList.value.find(item => item.skuId === skuId)
    if (existing) {
      existing.quantity += quantity
    } else {
      cartList.value.push({ skuId, quantity, selected: true, localOnly: true })
    }
    uni.setStorageSync(CART_KEY, cartList.value)
    try {
      await addCartItem({ skuId, quantity })
      await initCart() // 同步完成后刷新
    } catch (e) {
      // 标记待同步，下次联网时重试
      markPendingSync()
    }
  }

  function mergeCart(local, remote) {
    const map = new Map(remote.map(item => [item.skuId, item]))
    local.forEach(item => {
      if (!map.has(item.skuId)) {
        map.set(item.skuId, item)
      }
    })
    return Array.from(map.values())
  }

  return { cartList, initCart, addToCart }
}
```

**为什么这样做？**

- 用户点击"加入购物车"时立即有反馈（本地优先）。
- 登录后和每次进入购物车页时同步云端，保证多端一致。
- 网络异常时保留本地数据，网络恢复后自动重试。

---

## 6. 部署与上线清单

### 6.1 服务器环境配置要求

| 组件 | 版本/配置 | 说明 |
|------|-----------|------|
| 操作系统 | CentOS 7+ / Ubuntu 20.04+ | 推荐 Ubuntu 22.04 LTS |
| JDK | OpenJDK 17 | Spring Boot 3.x 要求 |
| MySQL | 8.0+ | 生产建议主从 |
| Redis | 7.x | 建议集群或哨兵 |
| Nginx | 1.20+ | 反向代理 + HTTPS |
| 微信小程序 | 基础库 2.30+ | 支持 Vue3 渲染 |

### 6.2 微信小程序后台需要配置的域名

| 类型 | 域名示例 | 说明 |
|------|----------|------|
| request 合法域名 | `https://api.yourdomain.com` | 后端 API 域名，必须 HTTPS |
| uploadFile | `https://api.yourdomain.com` | 如需上传图片 |
| downloadFile | `https://cdn.yourdomain.com` | 图片资源 CDN |
| 业务域名 | `https://h5.yourdomain.com` | H5 页面（如有） |

**微信支付配置：**

- 小程序后台 → 微信支付 → 绑定商户号
- 商户平台 → 产品中心 → JSAPI 支付开通
- 配置支付回调地址：`https://api.yourdomain.com/api/pay/notify/wechat`

### 6.3 上线前必须完成的测试用例清单

#### 功能测试

- [ ] 微信登录成功，token 正确写入本地存储
- [ ] 未登录用户访问预约/购物车/个人中心被拦截到登录页
- [ ] 场地时段查询正确显示可约/已约状态
- [ ] 两人同时预约最后一个时段，只有一人成功
- [ ] 15 分钟未支付订单自动释放场地时段
- [ ] 教练排班与课程时长冲突时给出明确提示
- [ ] 商品分类、搜索、排序结果正确
- [ ] SKU 规格选择后价格和库存正确更新
- [ ] 购物车数量调整、删除、全选、结算金额正确
- [ ] 商城订单创建后库存正确预占
- [ ] 微信支付成功后订单状态变为已付款
- [ ] 微信支付回调幂等：同一笔支付多次回调只处理一次
- [ ] 订单发货后用户可查看物流状态
- [ ] 确认收货后订单状态变为已完成

#### 性能测试

- [ ] 场地时段列表接口 QPS > 100（Redis 缓存）
- [ ] 商品列表接口响应时间 < 200ms
- [ ] 10 并发同时预约同一时段，只有 1 人成功，其余得到友好提示

#### 安全测试

- [ ] 所有 `/api/**` 接口（除登录外）必须携带有效 token
- [ ] 支付回调必须验签，伪造回调被拒绝
- [ ] SQL 注入测试（MyBatis Plus 参数化查询）
- [ ] 越权测试：用户 A 不能操作用户 B 的订单

#### 兼容性测试

- [ ] 微信小程序 iOS 真机
- [ ] 微信小程序 Android 真机
- [ ] H5 端 Chrome / Safari

---

## 7. 分阶段实施建议

| 阶段 | 周期 | 交付物 |
|------|------|--------|
| 第一阶段 | 1 周 | 数据库设计定稿 + 后端基础框架 + 用户登录 + 接口文档 |
| 第二阶段 | 1.5 周 | 场地预约模块（含 Redis 锁 + 支付） |
| 第三阶段 | 1.5 周 | 教练课程模块 |
| 第四阶段 | 2 周 | 商城模块（商品/购物车/订单/支付/物流） |
| 第五阶段 | 1 周 | 管理后台 + 测试 + 部署上线 |

---

## 8. 关键决策总结

| 决策点 | 选择 | 原因 |
|--------|------|------|
| 后端框架 | Spring Boot 3.x | 长期支持，生态完善 |
| 数据库 | MySQL 8.0 + InnoDB | 支持 JSON 字段、窗口函数 |
| 缓存/锁 | Redis + Redisson | 高性能分布式锁 |
| 管理后台 | Vue 3 + Element Plus | 与小程序技术栈一致，降低学习成本 |
| 文档 | Knife4j | 比原生 Swagger UI 更友好 |
| 并发控制 | 乐观锁 + 分布式锁 | 兼顾性能与数据安全 |
| 购物车 | 本地优先 + 云端同步 | 用户体验与数据一致性平衡 |
| 支付幂等 | 唯一索引 + 状态机 + 行锁 | 防止资损 |
