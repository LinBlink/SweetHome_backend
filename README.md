> [!WARNING]
>
> ### 开发者警告：这是一个 Alpha 版本
> 部分功能仍在测试阶段，稳定性有待验证。请勿擅自用于任何商业用途，仅供学习参考

<div align="center">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?logo=java&logoColor=white" alt="Java 17"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot 3.5"/>
  <img src="https://img.shields.io/badge/Spring_Cloud-2025-6DB33F?logo=spring&logoColor=white" alt="Spring Cloud 2025"/>
  <img src="https://img.shields.io/badge/Apache_Dubbo-3.3-FB542B?logo=apachedubbo&logoColor=white" alt="Dubbo 3.3"/>
  <img src="https://img.shields.io/badge/Nacos-2.4-1890FF?logo=alibabacloud&logoColor=white" alt="Nacos 2.4"/>
  <img src="https://img.shields.io/badge/MyBatis_Plus-3.5-DA392E?logo=mybatis&logoColor=white" alt="MyBatis-Plus"/>
  <br/>
  <img src="https://img.shields.io/badge/Redis-7-FF4438?logo=redis&logoColor=white" alt="Redis 7"/>
  <img src="https://img.shields.io/badge/Apache_Kafka-3.9-231F20?logo=apachekafka&logoColor=white" alt="Kafka 3.9"/>
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white" alt="MySQL 8.0"/>
  <img src="https://img.shields.io/badge/WebSocket-实时-FF6B35?logo=socket.io&logoColor=white" alt="WebSocket"/>
  <img src="https://img.shields.io/badge/JPush-极光推送-4A90D9" alt="JPush"/>
</div>


<h1 align="center">🏡 SweetHome — 过家家</h1>

<h3 align="center">项目官网：https://www.sweethome.asia</h3>

<p align="center">
  <b>面向家庭的社交与智能生活平台后端</b><br/>
  <i>从家庭关系图谱出发，重新定义家人之间的连接方式</i>
</p>

<p align="center">
  <a href="#-功能特性">功能特性</a> ·
  <a href="#-系统架构">系统架构</a> ·
  <a href="#-技术栈">技术栈</a> ·
  <a href="#-模块说明">模块说明</a> ·
  <a href="#-快速开始">快速开始</a> ·
  <a href="#-API-概览">API 概览</a> ·
  <a href="#-设计亮点">设计亮点</a>
</p>

---

<div align="center">

APP页面展示

  <img src="https://img.wathan.cn/images/2026/07/b207f431a4ed0a849ab5e571aaaee2380220540c1810cb4081c8732959d80c6d.webp" width="30%" />
  <img src="https://img.wathan.cn/images/2026/07/ee822b9db77a733c3f871e3448a83b5b8d7ed83f3aff8bdc8b46710f1bdd2d09.webp" width="30%" />
  <img src="https://img.wathan.cn/images/2026/07/ec6635a636390abe4fd3cc8f9205585e4ea63edf45323b50878c59bb7aa9c716.webp" width="30%" />
</div>

---

## ✨ 功能特性

### 👨‍👩‍👧‍👦 家庭关系管理
- **家庭创建与加入** — 注册即创建家庭，或通过邀请码/管理员审批加入
- **亲属关系引擎** — 基于图的 BFS 最短路径算法，自动计算成员间的称谓（爷爷、舅舅、堂姐...），**服务端只产出语言无关的 `relationCode`，本地化由客户端完成**
- **多家庭切换** — 用户同一时刻只属于一个家庭，切换时自动级联退出旧家庭

### 💬 即时通讯
- **WebSocket 实时聊天** — 原生 WebSocket（非 STOMP），支持文本/图片/语音/视频/红包消息
- **Redis Pub/Sub 广播** — 支持多实例水平扩展，消息通过 Redis 频道在各实例间扇出
- **HTTP 兜底** — WebSocket 断线时降级到 REST 接口发送
- **未读计数** — 基于游标的消息状态跟踪

### 💰 电子红包
- **Lua 脚本保证原子性** — 抢红包操作在 Redis 中原子执行，支持高并发
- **Redis Stream 持久化** — 红包领取记录先进入 Stream，由后台消费者异步落库
- **自动过期** — 定时任务将过期红包退款

### 📍 位置服务
- **实时位置上报** — 家庭成员位置追踪
- **电子围栏** — 自定义地理围栏，越界告警（通过极光推送通知）

### 📸 家庭动态（Moments）
- **图文/视频动态** — 类似朋友圈的家庭动态
- **点赞与评论** — 互动功能
- **公共动态广场** — 跨家庭动态聚合

### ❤️ 健康管理
- **健康指标记录** — 身高、体重、血压，按日追踪
- **可见性控制** — 每项指标可独立设置对家人公开/私密
- **每日提醒** — 定时推送提醒记录健康数据

### 🔐 安全认证
- **JWT 双 Token** — accessToken（15 分钟）+ refreshToken（30 天）
- **网关统一认证** — 所有请求统一在 Gateway 层鉴权，下游服务信任 `X-User-Id` 头
- **RSA 非对称签名** — 仅 auth-service 持有私钥签发，其他服务只持公钥验证

### ☁️ 云原生基础设施
- **Nacos** — 服务注册发现 + 配置中心（共享配置）
- **Dubbo** — 服务间 RPC 调用（最长 3 跳链路：auth → user → family → chat）
- **Kafka** — 事件驱动（缓存失效广播、围栏告警、离线通知、健康提醒）
- **JPush** — 极光推送，App 后台时接收告警通知
- **Caffeine + Redis** — 两级缓存，热点数据就近加速
- **Cloudflare R2** — 对象存储（头像、聊天图片、视频、语音）

### 🌐多语言支持

✅ 中文简体
✅ 中文繁体
✅ 英语
✅ 缅甸语（AI翻译，未进行人工测试）
✅ 韩语（AI翻译，未进行人工测试）
✅ 日语（AI翻译，未进行人工测试）

---

## 🏗 系统架构

```
                      ┌─────────────────────────────────────────────┐
                      │          Spring Cloud Gateway :8080         │
                      │    统一认证 · 路由转发 · 白名单 · JWT 验证     │
                      └──────────┬────────────────┬────────────────┘
                                 │                │
                    ┌────────────┴──────┐  ┌──────┴──────────────┐
                    │  REST Routes      │  │  WebSocket Route    │
                    │  /v1/auth/**      │  │  /v1/ws             │
                    │  /v1/users/**     │  └──────────┬──────────┘
                    │  /v1/families/**  │             │
                    │  /v1/conversations│  ┌──────────┴──────────┐
                    │  /v1/location/**  │  │    chat-service     │
                    │  /v1/moment/**    │  │  WebSocket Handler   │
                    │  /v1/health/**    │  │  Redis Pub/Sub       │
                    │  /v1/redpacket/** │  └─────────────────────┘
                    └────────┬─────────┘
                             │
        ┌────────────────────┼────────────────────────────┐
        │                    │                            │
   ┌────┴─────┐      ┌──────┴──────┐             ┌──────┴──────┐
   │ auth-svc │      │  user-svc   │             │ family-svc  │
   │  :8081   │◄────►│   :8082     │◄───────────►│   :8083     │
   │ JWT签发  │ Dubbo│  用户/上传   │   Dubbo     │ 家庭/亲属关系 │
   └──────────┘      └──────┬──────┘             └──────┬──────┘
                            │                          │
                     ┌──────┴──────┐             ┌──────┴──────┐
                     │ location-svc│             │ moment-svc  │
                     │   :8085     │             │   :8086     │
                     │ 位置/围栏    │             │ 动态/点赞/评论│
                     └─────────────┘             └─────────────┘
              ┌──────────────────┼──────────────────┐
        ┌─────┴──────┐    ┌──────┴──────┐    ┌─────┴──────┐
        │ health-svc │    │redpacket-svc│    │ chat-svc   │
        │   :8087    │    │   :8088     │    │   :8084    │
        │ 健康记录/提醒│    │ 红包/抢红包 │    │ 会话/消息   │
        └────────────┘    └─────────────┘    └────────────┘
```

### 基础设施层

```
           ┌─────────────────────────────────────────┐
           │           Docker Compose                │
           │                                         │
           │  ┌─────────┐  ┌─────────┐  ┌─────────┐  │
           │  │  Nacos   │  │  MySQL  │  │  Redis  │  │
           │  │ :8848   │  │ :3306   │  │ :6379   │  │
           │  └─────────┘  └─────────┘  └─────────┘  │
           │  ┌─────────┐  ┌─────────┐               │
           │  │  Kafka  │  │ Kafdrop│               │
           │  │ :9092   │  │ :9000  │               │
           │  └─────────┘  └─────────┘               │
           └─────────────────────────────────────────┘
```

### 请求生命周期

```
客户端请求
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│  Gateway (AuthGlobalFilter)                              │
│  1. 检查白名单（登录/注册/刷新/家庭预览）                   │
│  2. 从 Authorization 头或 ?token= 取 JWT                  │
│  3. RSA 公钥验签 + 检查 type=access                       │
│  4. 删除外部 X-User-Id，写入真实 userId                    │
│  5. 转发到下游微服务                                      │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│  微服务 (UserContextInterceptor)                          │
│  1. 读取 X-User-Id 请求头                                 │
│  2. 写入 ThreadLocal (UserContext)                        │
│  3. 业务代码调用 UserContext.getUserId()                   │
│  4. 请求结束后清除 ThreadLocal                             │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│  返回统一格式 Result<T>                                    │
│  { "code": 200, "message": "success", "data": {...} }    │
└──────────────────────────────────────────────────────────┘
```

---

## 🛠 技术栈

| 类别 | 技术 | 用途 |
|------|------|------|
| **语言** | Java 17 | 开发和运行 |
| **框架** | Spring Boot 3.5, Spring Cloud 2025 | 微服务基础设施 |
| **服务治理** | Spring Cloud Alibaba 2025, Nacos 2.4 | 注册发现 & 配置中心 |
| **RPC** | Apache Dubbo 3.3 | 微服务间远程调用 |
| **网关** | Spring Cloud Gateway (WebFlux) | 统一入口 & 认证 |
| **ORM** | MyBatis-Plus 3.5 | 数据持久化 |
| **缓存** | Caffeine (L1) + Redis 7 (L2) | 两级缓存加速 |
| **消息队列** | Apache Kafka 3.9 | 事件驱动 & 异步解耦 |
| **实时通信** | WebSocket + Redis Pub/Sub | 即时聊天 & 多实例广播 |
| **数据库** | MySQL 8.0 | 关系型数据存储 |
| **对象存储** | Cloudflare R2 (S3 兼容) | 文件存储（头像/图片/视频/语音） |
| **推送** | 极光推送 (JPush) | App 离线通知 |
| **安全** | JWT (RSA-256), BCrypt | 认证 & 密码加密 |
| **构建** | Maven Wrapper | 项目构建 & 依赖管理 |

---

## 📦 模块说明

| 模块 | 端口 | Dubbo 端口 | 说明 |
|------|------|-----------|------|
| **common** | — | — | 共享核心库：统一响应体 `Result<T>`、异常体系 `ErrorCode`/`BusinessException`/`GlobalExceptionHandler`、`UserContext`（ThreadLocal）、Dubbo 异常过滤器、共享常量 |
| **api** | — | — | Dubbo 接口契约（`UserApi`/`FamilyApi`/`ChatApi`）+ 共享 DTO/VO，供 provider/consumer 共同依赖 |
| **gateway** | 8080 | — | 统一网关：路由转发 + `AuthGlobalFilter`（JWT 统一验证）+ CORS |
| **auth-service** | 8081 | 20880 | 认证中心：注册/登录/登出/刷新 Token，RSA JWT 签发，BCrypt 密码 |
| **user-service** | 8082 | 20881 | 用户服务：个人信息、头像/图片/视频/语音上传（R2）、推送 Token 注册、两级缓存 |
| **family-service** | 8083 | 20882 | 家庭服务：家庭 CRUD、成员管理、邀请码、申请审批、**KinshipEngine**（亲属称谓计算） |
| **chat-service** | 8084 | 20883 | 聊天服务：会话管理、消息历史、**WebSocket 实时通信**、Redis 多实例广播 |
| **location-service** | 8085 | 20884 | 位置服务：实时位置上报、**电子围栏**、越界告警 |
| **moment-service** | 8086 | 20885 | 动态服务：家庭动态（朋友圈）、点赞/评论、**公共动态广场** |
| **health-service** | 8087 | 20886 | 健康服务：身高/体重/血压纪录、可见性控制、**每日提醒推送** |
| **redpacket-service** | 8088 | 20887 | 红包服务：发红包/抢红包、**Redis Lua 原子操作**、异步落库、过期退款 |
| **shop-service** | — | — | 商城服务（预留模块） |
| **ai-service** | — | — | AI 服务（预留模块） |

---

## 🚀 快速开始

### 前置要求

- JDK 17+
- Docker & Docker Compose
- Maven Wrapper（项目自带 `./mvnw`）

### 1. 启动基础设施

```bash
docker compose -f dockercompose.yml up -d
```

将启动：
- **Nacos** `:8848` — 服务注册中心 & 配置中心
- **MySQL 8.0** `:3306` — 数据库（自动执行初始化建表脚本）
- **Redis 7** `:6379` — 缓存 & WebSocket Pub/Sub
- **Kafka 3.9** `:9092` — 事件总线
- **Kafdrop** `:9000` — Kafka 可视化 GUI

### 2. 配置环境变量

```bash
cp .env.example .env
```

编辑 `.env`，填入必要配置：

```ini
MYSQL_ROOT_PASSWORD=your_password
MYSQL_SERVER_ADDR=localhost
NACOS_SERVER_ADDR=localhost
NACOS_NAMESPACE=dev

# 生成 RSA 密钥对: 运行 auth-service 下的 KeyGenerator.java
JWT_PRIVATE_KEY=your_base64_private_key
JWT_PUBLIC_KEY=your_base64_public_key

# 极光推送（可选）
JPUSH_APP_KEY=your_app_key
JPUSH_MASTER_SECRET=your_master_secret

# Cloudflare R2（可选，不上传文件可忽略）
R2_ACCESS_KEY_ID=your_r2_key
R2_SECRET_ACCESS_KEY=your_r2_secret
R2_ENDPOINT=https://your-account.r2.cloudflarestorage.com
R2_PUBLIC_BASE_URL=https://pub.your-domain.com
```

### 3. 配置 Nacos

在 Nacos 控制台（http://localhost:8848）创建以下共享配置（`DEFAULT_GROUP`）：

- `common-spring.yaml` — 通用 Spring 配置（数据源、Redis、Kafka）
- `common-logs.yaml` — 通用日志配置
- `common-mybatis.yaml` — MyBatis-Plus 配置

> 详细配置项见各服务的 `application.yml`。

### 4. 构建 & 启动服务

```bash
# 构建全部模块
./mvnw clean package -DskipTests

# 按依赖顺序启动服务（每个新开终端）
./mvnw spring-boot:run -pl auth-service
./mvnw spring-boot:run -pl user-service
./mvnw spring-boot:run -pl family-service
./mvnw spring-boot:run -pl chat-service
./mvnw spring-boot:run -pl location-service
./mvnw spring-boot:run -pl moment-service
./mvnw spring-boot:run -pl health-service
./mvnw spring-boot:run -pl redpacket-service
```

或使用你喜欢的 IDE（IntelliJ IDEA）逐个启动。

---

## 📖 API 概览

完整 API 文档见 [`doc/API.md`](./doc/API.md)（2424 行，涵盖全部接口）。

### 认证服务
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v1/auth/register` | 注册（创建或加入家庭） |
| POST | `/v1/auth/login` | 登录 |
| POST | `/v1/auth/refresh` | 刷新 Token |
| POST | `/v1/auth/logout` | 登出 |

### 用户服务
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/v1/users/me` | 当前用户信息 |
| PUT | `/v1/users/me` | 更新个人信息 |
| POST | `/v1/users/upload/avatar` | 上传头像 |
| POST | `/v1/users/upload/image` | 上传聊天图片 |
| POST | `/v1/users/upload/video` | 上传视频 |
| POST | `/v1/users/upload/audio` | 上传语音 |
| POST | `/v1/users/push-token` | 注册推送 Token |
| DELETE | `/v1/users/push-token` | 注销推送 Token |

### 家庭服务
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/v1/families/lookup` | 凭邀请码预览家庭 |
| GET | `/v1/families/{id}` | 家庭详情 |
| GET | `/v1/families/{id}/members` | 家庭成员列表（含称谓） |
| POST | `/v1/families/{id}/invite` | 生成邀请码 |
| POST | `/v1/families/join` | 凭邀请码加入 |
| POST | `/v1/families/join-requests` | 提交加入申请 |
| GET | `/v1/families/{id}/join-requests` | 查看申请列表 |
| POST | `/v1/families/join-requests/{id}/approve` | 批准申请 |
| POST | `/v1/families/join-requests/{id}/reject` | 拒绝申请 |

### 聊天服务
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/v1/conversations` | 会话列表 |
| POST | `/v1/conversations` | 创建私聊 |
| GET | `/v1/conversations/{id}/messages` | 消息历史（游标分页） |
| POST | `/v1/conversations/{id}/messages` | 发送消息（HTTP 兜底） |
| PUT | `/v1/conversations/{id}/read` | 标记已读 |
| WS | `/v1/ws?token=` | WebSocket 实时通信 |

### 位置服务 · 动态服务 · 健康服务 · 红包服务

详见 [`doc/API.md`](./doc/API.md) 对应章节。

---

## 💡 设计亮点

### 🧬 亲属关系引擎
`family-service` 核心创新。将家庭建模为有向图（成员=节点，血缘/姻亲=边），通过 BFS 搜索最短路径并编码为 `F/M/S/Son/Dau` tokens，再将绕路路径折叠简化（如 `F.Son` → 兄弟），最终产出语言无关的 `relationCode`。**本地化翻译完全交由客户端处理**，支持任意语言而无需修改后端。

### 🚪 网关统一认证
`AuthGlobalFilter` 作为单一安全关口：白名单放行登录/注册，其余请求统一验签，**删除并重写 `X-User-Id` 头**。下游服务零信任外部请求，彻底杜绝用户 ID 伪造。

### 💬 水平扩展的 WebSocket 架构
聊天服务通过 **Redis Pub/Sub** 实现跨实例消息广播。每个实例维护本地 `userId → WebSocketSession` 映射，收到消息后发布到 `conv:<id>` 频道，所有实例订阅并推送给本地的接收方连接。

### 🔄 原子抢红包
基于 **Redis Lua 脚本** 实现红包抢夺的原子性，一次网络往返完成余额检查、扣减、记录，避免高并发下的超抢。红包领取流水先写入 Redis Stream 再异步消费落库，保证性能的同时不丢数据。

### 🧩 事件驱动架构
基于 **Kafka + Outbox 模式** 实现可靠事件发布：
- **用户缓存失效广播** — user-service L1 缓存更新时通知其他实例
- **电子围栏报警** — location-service 检测越界 → Kafka → user-service → JPush
- **聊天离线通知** — 目标用户不在线时，通过 Kafka 触发推送
- **健康提醒** — 定时调度 → Kafka → JPush

### 📐 统一响应 & 异常体系
全局 `Result<T>` 包装 + `ErrorCode` 枚举 + `BusinessException` + `GlobalExceptionHandler`，确保所有接口返回结构一致的 JSON 错误信息。Dubbo 层通过 `DubboExceptionFilter` 跨服务传播业务异常，不丢失错误码。

---

## 📁 项目结构

```
sh/
├── common/                    # 共享核心库
│   └── src/main/java/asia/sweethome/common/
│       ├── constants/         # 共享常量（ConversationType, MessageType, RelationType...）
│       ├── context/           # UserContext (ThreadLocal)
│       ├── dubbo/             # DubboExceptionFilter
│       ├── entity/vo/         # Result<T> 统一响应体
│       └── exception/         # ErrorCode, BusinessException, GlobalExceptionHandler
│
├── api/                       # Dubbo 接口契约
│   └── src/main/java/asia/sweethome/api/
│       ├── entity/dto/        # 共享请求/响应 DTO
│       ├── UserApi.java       # 用户 Dubbo 接口
│       ├── FamilyApi.java     # 家庭 Dubbo 接口
│       └── ChatApi.java       # 聊天 Dubbo 接口
│
├── gateway/                   # 服务网关 :8080
├── auth-service/              # 认证中心 :8081
├── user-service/              # 用户服务 :8082
├── family-service/            # 家庭服务 :8083
├── chat-service/              # 聊天服务 :8084
├── location-service/          # 位置服务 :8085
├── moment-service/            # 动态服务 :8086
├── health-service/            # 健康服务 :8087
├── redpacket-service/         # 红包服务 :8088
├── shop-service/              # 商城（预留）
├── ai-service/                # AI 服务（预留）
│
├── doc/                       # 文档
│   ├── API.md                 # 完整 API 文档
│   ├── PORTS.md               # 端口分配表
│   ├── ROADMAP.md             # 开发路线图
│   └── ...
│
├── docker/                    # Docker 配置
│   └── mysql/mysql-init/      # 数据库初始化 SQL
│
├── dockercompose.yml          # 基础设施编排
├── pom.xml                    # 父 POM
├── .env                       # 本地环境变量
└── CLAUDE.md                  # Claude Code 开发指南
```

---

## 🧪 关于测试

当前项目以教学/展示为目的，测试覆盖有限。`auth-service` 提供了一个 `KeyGenerator.java`（独立 `main` 方法），用于生成 RSA JWT 密钥对。

---

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交变更 (`git commit -m 'feat: 添加某个很棒的功能'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

代码风格：
- 遵循项目现有的代码风格和架构约定
- 保持中文教学注释的写作风格
- 统一使用 `Result<T>` 作为响应包装
- 业务异常使用 `ErrorCode` 枚举 + `BusinessException`

---

## 🗺️ ROADMAP

### **正在开发**

- 手机短信验证
- OAuth 支持更多登录方式

### 还在后头

**家庭AI助手**

- 天然适配APP，使用便捷
- 获取一手家庭成员详细信息
- 获取家庭成员实时位置
- 获取家庭成员最近发布的动态（了解心理状况等）
- 收集一切可获取的事件信息，生成每日总结报告
- 指导家庭积极向上，促进家庭和谐发展

**邻里互惠(家庭小卖部系统)**

- **内部销售**：家庭成员间闲置物品互通有无，其他成员可请求获取
- **外部销售**：其他家庭可浏览并选择请求获取或直接下单购买
- 基于熟人信任机制，交易过程透明，避免扯皮纠纷

**家庭详细信息**

- 记录出生日期（作为AI参考依据，可自动计算年龄）

**家庭排行榜**

- **步数统计**：前端统计，后端存储数据
- **关心统计**：分析成员间消息互动频率，识别最关心彼此的成员组合
- 对互动不足的家庭成员自动提醒，鼓励多多关注家人

**家庭相册**

- **动态相册**：实时更新的家庭照片集
- **专用相册**：按主题或事件分类整理
- **家庭影音档案**：统一的音视频资料库

**老人照料功能**

- 手机跌倒检测，异常时及时提醒
- 适配大按钮，方便老人使用

**家庭OA系统（财务审批）**

- 孩子零花钱征用申请流程
- （更多OA功能待扩展）

**家庭自律系统**

- 24小时监测手机使用情况
- 自动生成使用报告并上报

**家庭倒数日/大事件**

- 记录家庭成员生日倒计时
- 重要纪念日提醒

 **族谱功能增强**
- 支持族谱成员新增
- 每个成员对应唯一邀请码
- 新成员凭邀请码注册并入家

---

## 📄 许可证

本项目仅供个人学习和交流使用。

---

<p align="center">
  <b>SweetHome · 过家家</b><br/>
  <i>家的温暖，一触即达</i>
</p>
