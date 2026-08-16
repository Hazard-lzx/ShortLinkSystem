# 分布式短链接系统（微服务版）

基于 **Java 17 + Spring Boot 3.2 + Spring Cloud Alibaba 2023.0.1.0** 的分布式短链接管理系统。

## 一、技术栈

| 分类 | 组件 | 版本 | 用途 |
| :--- | :--- | :--- | :--- |
| 语言/构建 | Java / Maven | 17 / 3.9+ | 父子工程，统一依赖管理 |
| 核心框架 | Spring Boot | 3.2.5 | 全部 `jakarta.*` 命名空间 |
| 微服务 | Spring Cloud Alibaba | 2023.0.1.0 | Nacos 注册/配置、Sentinel 限流 |
| 微服务 | Spring Cloud | 2023.0.1 | Gateway 网关、OpenFeign 调用 |
| ORM | MyBatis-Plus | 3.5.5 | `mybatis-plus-spring-boot3-starter` |
| 缓存/锁 | Redis + Redisson | 7.x / 3.27.0 | 多级缓存、互斥锁、布隆过滤器 |
| 消息 | Kafka | 3.x | 访问日志异步解耦 |
| 工具 | Hutool / Lombok | 5.8.29 | 工具类、BCrypt、JWT |
| 文档 | SpringDoc OpenAPI | 2.5.0 | 管理服务接口文档 `/swagger-ui.html` |

## 二、工程结构

```
short-link-parent
├── short-link-common        公共模块：统一返回、全局异常、常量、工具类、消息对象
├── short-link-gateway  8000 网关：路由分发、JWT 鉴权、跨域、Sentinel 限流、请求ID
├── short-link-api      8001 跳转服务：302 重定向、布隆过滤器、Redis 缓存、Kafka 生产
└── short-link-admin    8002 管理服务：短链 CRUD、用户管理、统计、Kafka 消费、Feign
```

### 核心链路

```
浏览器 → Gateway(8000)
           ├─ /s/{shortCode}      → short-link-api  → 布隆过滤器 → Redis 缓存 → (互斥锁)回源查库 → 302 跳转 + Kafka 发日志
           └─ /api/admin/**       → short-link-admin → 短链 CRUD → Feign 通知 api 清缓存
                                          ↑ Kafka 消费日志 → 批量入库 + 累计访问次数
```

### 高并发设计

- **缓存穿透**：Redisson 分布式布隆过滤器拦截不存在的短码；布隆误判由空对象缓存（60s）兜底
- **缓存击穿**：Redisson 可重入互斥锁控制缓存重建，未抢到锁的请求短暂等待后重读
- **缓存雪崩**：过期时间 = 基础 30min + 随机 10min
- **异步解耦**：跳转主链路只发 Kafka 消息，日志由管理服务批量消费（手动提交 Offset）入库
- **故障隔离**：跳转服务不依赖管理服务；Feign 失败降级只记日志，由缓存过期保证最终一致

## 三、快速启动

### 1. 启动中间件（Docker）

```bash
cd backend/docker
docker compose up -d
```

包含：MySQL 8.0（自动执行 `sql/init.sql` 建库建表）、Redis 7、Nacos 2.3.2（控制台 http://localhost:8848/nacos）、Kafka 3.7（KRaft 模式）。

### 2. 启动服务（按顺序）

依次运行三个启动类（需中间件就绪）：

1. `ShortLinkAdminApplication`（8002，初始化默认账号）
2. `ShortLinkApiApplication`（8001，启动时加载布隆过滤器）
3. `ShortLinkGatewayApplication`（8000，统一流量入口）

### 3. 验证

```bash
# 1. 登录（默认账号 admin/123456）
curl -X POST http://localhost:8000/api/admin/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 2. 创建短链（Authorization 换成上一步返回的 token）
curl -X POST http://localhost:8000/api/admin/link \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"originalUrl":"https://www.example.com"}'

# 3. 短链跳转（302 重定向）
curl -i http://localhost:8000/s/<返回的短码>

# 4. 查看统计
curl http://localhost:8000/api/admin/stats/overview -H "Authorization: Bearer <token>"
```

## 四、接口清单

| 方法 | 路径 | 鉴权 | 说明 |
| :--- | :--- | :--- | :--- |
| POST | `/api/admin/user/register` | 否 | 用户注册 |
| POST | `/api/admin/user/login` | 否 | 登录，返回 JWT |
| GET | `/api/admin/user/info` | 是 | 当前用户信息 |
| POST | `/api/admin/link` | 是 | 创建短链 |
| PUT | `/api/admin/link` | 是 | 编辑短链 |
| PUT | `/api/admin/link/{code}/status/{status}` | 是 | 启用/禁用 |
| DELETE | `/api/admin/link/{code}` | 是 | 删除短链 |
| GET | `/api/admin/link/{code}` | 是 | 短链详情 |
| GET | `/api/admin/link/page` | 是 | 分页条件查询 |
| GET | `/api/admin/stats/overview` | 是 | 统计总览 |
| GET | `/api/admin/stats/trend?days=7` | 是 | 访问趋势 |
| GET | `/api/admin/stats/top?limit=10` | 是 | 访问量 TopN |
| GET | `/s/{shortCode}` | 否 | 短链 302 跳转 |

管理服务接口文档：http://localhost:8002/swagger-ui.html（直连，绕过网关鉴权白名单限制）。

## 五、配置说明

- **本地配置兜底**：各服务 `application.yml` 为本地默认配置，可直接启动
- **Nacos 配置中心**：在 Nacos 控制台创建 `short-link-api.yaml` / `short-link-admin.yaml`（DataId）即可动态覆盖本地配置（如限流阈值、缓存过期时间），无需重启
- **JWT 密钥**：网关与 admin 的 `short-link.jwt.secret` 必须一致；生产环境请通过 Nacos 配置并定期更换
- **敏感信息**：数据库/Redis 密码默认为本地 Docker 编排默认值，生产环境务必修改

## 六、默认账号

| 账号 | 密码 | 说明 |
| :--- | :--- | :--- |
| admin | 123456 | 用户表为空时由 admin 服务启动自动创建（BCrypt 加密存储） |
