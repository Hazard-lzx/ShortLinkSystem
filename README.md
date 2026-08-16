# 分布式短链接系统

基于 **Spring Boot 3.2 + Spring Cloud Alibaba 2023** 微服务架构 + **Vue 3 + TypeScript** 前端的分布式短链接管理系统。

## 项目结构

```
short-link-system
├── backend/                 # 后端微服务（Maven 多模块）
│   ├── short-link-common    # 公共模块：统一返回、全局异常、常量、工具类
│   ├── short-link-gateway   # 网关（8000）：路由分发、JWT 鉴权、跨域、Sentinel 限流
│   ├── short-link-api       # 跳转服务（8001）：302 重定向、布隆过滤器、Redis 缓存
│   └── short-link-admin     # 管理服务（8002）：短链 CRUD、用户管理、统计看板
│   └── docker/              # 中间件一键编排（MySQL、Redis、Nacos、Kafka）
└── frontend/                # 前端 SPA（Vue 3 + TypeScript + Element Plus）
```

## 技术栈

### 后端

| 分类 | 组件 | 版本 |
| :--- | :--- | :--- |
| 语言 | Java | 17 |
| 框架 | Spring Boot / Spring Cloud | 3.2.5 / 2023.0.1 |
| 微服务 | Spring Cloud Alibaba | 2023.0.1.0 |
| 注册/配置 | Nacos | 2.3.2 |
| 限流 | Sentinel |  |
| 网关 | Spring Cloud Gateway |  |
| ORM | MyBatis-Plus | 3.5.5 |
| 缓存 | Redis + Redisson | 7.x |
| 消息队列 | Kafka | 3.x |
| 构建 | Maven | 3.9+ |

### 前端

| 分类 | 组件 | 版本 |
| :--- | :--- | :--- |
| 框架 | Vue | 3.5+ (Composition API) |
| 语言 | TypeScript | 5.x |
| 构建 | Vite | 5.x |
| UI | Element Plus | 2.8+ |
| 状态管理 | Pinia |  |
| 路由 | Vue Router | 4.x |
| 图表 | ECharts | 5.x |
| HTTP | Axios |  |

## 快速启动

### 前置条件

- JDK 17+
- Maven 3.9+
- Node.js 18+
- Docker & Docker Compose

### 1. 启动中间件

```bash
cd backend/docker
docker compose up -d
```

一键启动 MySQL 8.0、Redis 7、Nacos 2.3.2、Kafka 3.7（KRaft 模式）。

### 2. 配置环境变量

配置文件中的敏感信息已替换为环境变量占位符，运行前请设置（替换为你的实际值）：

```bash
# Windows PowerShell
$env:DB_PASSWORD="你的MySQL密码"
$env:REDIS_PASSWORD="你的Redis密码"
$env:JWT_SECRET="你的JWT密钥"

# Linux / macOS
export DB_PASSWORD="你的MySQL密码"
export REDIS_PASSWORD="你的Redis密码"
export JWT_SECRET="你的JWT密钥"
```

### 3. 启动后端

按顺序启动三个微服务：

```bash
cd backend

# 编译全部模块
mvn clean package -DskipTests

# 启动管理服务（8002，初始化数据库表）
java -jar short-link-admin/target/short-link-admin-1.0.0.jar

# 启动跳转服务（8001，加载布隆过滤器）
java -jar short-link-api/target/short-link-api-1.0.0.jar

# 启动网关（8000，统一入口）
java -jar short-link-gateway/target/short-link-gateway-1.0.0.jar
```

也可在 IDE 中直接运行对应模块的 `Application` 启动类。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`，已配置代理转发到后端网关 `http://localhost:8000`。

### 5. 用户注册

首次使用需注册账号。网关白名单已放通注册接口：

```bash
curl -X POST http://localhost:8000/api/admin/user/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

注册完成后即可登录使用。

## 核心架构

### 请求链路

```
浏览器 → Gateway (8000)
           ├─ /s/{shortCode}       → short-link-api  → 布隆过滤器 → Redis → 回源查库 → 302 跳转 + Kafka 日志
           └─ /api/admin/**        → short-link-admin → 短链管理 → Feign 通知清缓存
                                          ↑ Kafka 消费日志 → 批量入库
```

### 高并发设计

- **缓存穿透**：Redisson 分布式布隆过滤器拦截不存在的短码；布隆误判由空对象缓存（60s）兜底
- **缓存击穿**：Redisson 可重入互斥锁控制缓存重建，未抢到锁的请求短暂等待后重读
- **缓存雪崩**：过期时间 = 基础 30min + 随机 10min，避免大量 Key 同时过期
- **异步解耦**：跳转链路只发 Kafka 消息，日志由管理服务批量消费入库
- **故障隔离**：跳转服务不依赖管理服务；Feign 降级只记日志，缓存过期保证最终一致

## 接口概览

### 管理接口（`/api/admin/**`）

| 方法 | 路径 | 鉴权 | 说明 |
| :--- | :--- | :--- | :--- |
| POST | `/user/register` | 否 | 用户注册 |
| POST | `/user/login` | 否 | 登录，返回 JWT |
| GET | `/user/info` | 是 | 当前用户信息 |
| POST | `/link` | 是 | 创建短链 |
| PUT | `/link` | 是 | 编辑短链 |
| PUT | `/link/{code}/status/{status}` | 是 | 启用/禁用 |
| DELETE | `/link/{code}` | 是 | 删除短链 |
| GET | `/link/{code}` | 是 | 短链详情 |
| GET | `/link/page` | 是 | 分页查询 |
| GET | `/stats/overview` | 是 | 统计总览 |
| GET | `/stats/trend?days=7` | 是 | 访问趋势 |
| GET | `/stats/top?limit=10` | 是 | 访问量 TopN |

### 跳转接口

| 方法 | 路径 | 鉴权 | 说明 |
| :--- | :--- | :--- | :--- |
| GET | `/s/{shortCode}` | 否 | 短链 302 跳转 |

## 配置说明

- **Nacos 配置中心**：各服务 `bootstrap.yml` 指定了 Nacos 地址，可在 Nacos 控制台创建 `short-link-api.yaml` / `short-link-admin.yaml` 动态覆盖本地配置
- **JWT 密钥**：网关与 admin 服务的 `short-link.jwt.secret` 必须保持一致
- **环境变量**：`${DB_PASSWORD}`、`${REDIS_PASSWORD}`、`${JWT_SECRET}` 需在启动前设置
- **本地兜底**：各服务 `application.yml` 为本地默认配置，Nacos 不可用时自动兜底

## 详细文档

后端各模块的详细说明参见 [backend/README.md](backend/README.md)。