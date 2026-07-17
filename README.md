# badminton-project 羽毛球馆综合服务系统

> 一个面向羽毛球馆的预约 + 商城综合服务系统，包含小程序端、管理后台和 Web 用户端。

---

## 1. 项目概述

### 1.1 产品定位

面向羽毛球馆的综合服务系统：

1. **场地预约**：按小时查询场地占用，选择时段生成订单。
2. **教练课程**：查看教练信息、课程，预约私教课程。
3. **商品商城**：商品浏览、购物车、下单。

### 1.2 技术栈

| 端 | 技术 | 说明 |
|----|------|------|
| 后端 | Spring Boot 3.3.5 + MyBatis Plus | JDK 17+，RESTful API |
| 数据库 | MySQL 8.0 | 主数据库 |
| 缓存/锁 | Redis 5.0.14 + Redisson | 缓存 + 分布式锁 |
| 管理后台 | Vue 3 + Vite + Element Plus | 运营管理 |
| Web 用户端 | 原生 HTML + CSS + JS | 无需构建 |
| 小程序端 | UniApp（待开发） | 跨端小程序 |
| 接口文档 | Knife4j | Swagger 增强版 |
| 支付 | 微信支付（待接入） | JSAPI 支付 |

### 1.3 项目结构

```
badminton-project/
├── server/          # Spring Boot 后端
├── admin/           # Vue3 管理后台
├── web/             # 静态 Web 用户端
├── mini/            # UniApp 小程序端（待开发）
└── README.md
```

### 1.4 已实现 vs 待实现

| 功能 | 状态 |
|------|------|
| 用户账号密码登录/注册 | ✅ |
| JWT 角色鉴权（USER/ADMIN/COACH） | ✅ |
| 密码修改 | ✅ |
| 场地管理 + 预约 | ✅（支付待接入） |
| 教练管理 + 课程预约 | ✅（支付待接入） |
| 商城商品/分类/SKU/购物车/订单 | ✅（支付待接入） |
| 微信支付 | 🔴 未接入 |
| 小程序端 | 🔴 未开发 |
| 短信验证码 | 🔴 未接入 |
| 物流追踪 | 🔴 未接入 |

---

## 2. 快速启动

### 2.1 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0
- Redis 5.0+
- Node.js 18+（用于 admin 前端）

### 2.2 数据库

1. 创建数据库 `badminton_db`
2. 执行 `server/db/schema.sql` 创建表结构
3. 如从旧版本升级，执行 `server/db/migration/V1_1__add_password_and_role.sql`

### 2.3 后端启动

```bash
cd server
mvn spring-boot:run
```

默认端口 `8080`，接口文档：http://localhost:8080/doc.html

### 2.4 管理后台启动

```bash
cd admin
npm install
npm run dev
```

默认地址 http://localhost:5173

### 2.5 Web 用户端

直接打开 `web/index.html` 即可（建议用本地服务器）。

---

## 3. 认证与授权

### 3.1 登录方式

| 方式 | 端点 | 说明 |
|------|------|------|
| 账号密码登录 | `POST /api/user/login/password` | 手机号 + 密码 |
| 用户注册 | `POST /api/user/register` | 手机号 + 密码，默认角色 USER |
| 微信登录 | `POST /api/user/login` | 当前为开发 mock，需替换真实微信 SDK |

### 3.2 角色体系

| 角色 | 说明 |
|------|------|
| `USER` | 普通用户，可预约场地/教练、购物 |
| `ADMIN` | 管理员，可管理场地/教练/课程/商品/订单 |
| `COACH` | 教练，当前仅可预约课程（完整自助端点待开发） |

### 3.3 默认管理员

系统启动时，如果数据库中没有 ADMIN 角色用户，会检查环境变量 `DEFAULT_ADMIN_PASSWORD`：

```bash
# Windows PowerShell
$env:DEFAULT_ADMIN_PASSWORD = "your-strong-password"

# Linux / macOS
export DEFAULT_ADMIN_PASSWORD=your-strong-password
```

若未配置，则不会创建默认管理员，需在日志中查看提示。

### 3.4 JWT 密钥

生产环境务必配置环境变量覆盖默认密钥：

```bash
export JWT_SECRET=your-256-bit-secret-key-at-least-32-characters-long
```

---

## 4. 最近更新

### 2026-07-17

- ✅ 修复代码审查发现的 8 个问题（状态码覆盖、硬编码密码、迁移脚本、空引用等）
- ✅ 权限认证模块 focused-fix（路由守卫、403 统一处理、JWT 密钥可配置）
- ✅ 用户修改密码功能
- ✅ COACH 角色最小权限标注

---

## 5. 核心模块接口

### 5.1 用户模块

| URL | Method | 功能 | 权限 |
|-----|--------|------|------|
| `/api/user/register` | POST | 用户注册 | 公开 |
| `/api/user/login/password` | POST | 账号密码登录 | 公开 |
| `/api/user/login` | POST | 微信登录（mock） | 公开 |
| `/api/user/info` | GET | 当前用户信息 | 登录 |
| `/api/user/password` | PUT | 修改密码 | 登录 |
| `/api/user/logout` | POST | 退出登录 | 登录 |

### 5.2 场地模块

| URL | Method | 功能 | 权限 |
|-----|--------|------|------|
| `/api/venue/list` | GET | 场地列表 | 公开 |
| `/api/venue/detail/{id}` | GET | 场地详情 | 公开 |
| `/api/venue/slots` | GET | 场地时段 | 公开 |
| `/api/venue` | POST | 新增场地 | ADMIN |
| `/api/venue/{id}` | PUT | 修改场地 | ADMIN |
| `/api/venue/{id}` | DELETE | 删除场地 | ADMIN |
| `/api/venue/booking/prepare` | POST | 预约场地 | 登录 |
| `/api/venue/booking/cancel/{id}` | POST | 取消预约 | 登录 |
| `/api/venue/booking/my` | GET | 我的预约 | 登录 |

### 5.3 教练课程模块

| URL | Method | 功能 | 权限 |
|-----|--------|------|------|
| `/api/coach/list` | GET | 教练列表 | 公开 |
| `/api/coach/detail/{id}` | GET | 教练详情 | 公开 |
| `/api/coach/schedule` | GET | 排班查询 | 公开 |
| `/api/coach` | POST | 新增教练 | ADMIN |
| `/api/coach/{id}` | PUT | 修改教练 | ADMIN |
| `/api/coach/{id}` | DELETE | 删除教练 | ADMIN |
| `/api/coach/course` | POST | 新增课程 | ADMIN |
| `/api/coach/course/{id}` | PUT | 修改课程 | ADMIN |
| `/api/coach/course/{id}` | DELETE | 删除课程 | ADMIN |
| `/api/coach/schedule` | POST | 新增排班 | ADMIN |
| `/api/coach/schedule/{id}` | PUT | 修改排班 | ADMIN |
| `/api/coach/schedule/{id}` | DELETE | 删除排班 | ADMIN |
| `/api/coach/book` | POST | 预约私教课 | USER/COACH |
| `/api/coach/book/cancel/{id}` | POST | 取消预约 | USER/COACH |
| `/api/coach/book/my` | GET | 我的课程预约 | USER/COACH |

### 5.4 商城模块

| URL | Method | 功能 | 权限 |
|-----|--------|------|------|
| `/api/shop/categories` | GET | 分类列表 | 公开 |
| `/api/shop/products` | GET | 商品列表 | 公开 |
| `/api/shop/detail/{id}` | GET | 商品详情 | 公开 |
| `/api/shop/categories` | POST | 新增分类 | ADMIN |
| `/api/shop/products` | POST | 新增商品 | ADMIN |
| `/api/shop/products/{id}` | PUT | 修改商品 | ADMIN |
| `/api/shop/products/{id}` | DELETE | 删除商品 | ADMIN |
| `/api/shop/skus` | POST | 新增 SKU | ADMIN |
| `/api/shop/cart/list` | GET | 购物车 | 登录 |
| `/api/shop/cart/add` | POST | 加入购物车 | 登录 |
| `/api/shop/order/create` | POST | 创建订单 | 登录 |
| `/api/shop/order/my` | GET | 我的订单 | 登录 |
| `/api/shop/orders` | GET | 全部订单 | ADMIN |
| `/api/shop/order/ship/{id}` | POST | 发货 | ADMIN |

---

## 6. 部署配置

### 6.1 环境变量

| 变量 | 说明 | 是否必填 |
|------|------|---------|
| `JWT_SECRET` | JWT 签名密钥，至少 32 字符 | 生产必填 |
| `DEFAULT_ADMIN_PASSWORD` | 默认管理员密码 | 首次启动必填 |
| `MYSQL_PASSWORD` | MySQL 密码 | 生产建议 |
| `REDIS_PASSWORD` | Redis 密码（如有） | 按需 |
| `WECHAT_MINIAPP_APP_ID` | 微信小程序 AppID | 微信支付时必填 |
| `WECHAT_MINIAPP_APP_SECRET` | 微信小程序 AppSecret | 微信支付时必填 |

### 6.2 生产上线清单

- [ ] 配置 `JWT_SECRET`，不使用默认密钥
- [ ] 配置 `DEFAULT_ADMIN_PASSWORD` 并删除默认账号或修改密码
- [ ] 接入真实微信登录
- [ ] 接入微信支付
- [ ] 配置 HTTPS 和域名白名单
- [ ] 配置 MySQL/Redis 连接信息
- [ ] 配置支付回调地址
- [ ] 完成小程序端开发

---

## 7. 开发团队

- 项目代码统一放在 `D:\AI Program\badminton-project`
- 远程仓库：https://github.com/R-enternal/badminton-project

---

*最后更新：2026-07-17*
