# 人才绩效管理系统 - 后端

## 项目简介

人才绩效管理系统（Talent HR）是一套面向企业 HR 的人才评估与筛选平台。支持灵活配置评估指标、多条件组合筛选、基于表达式引擎的智能评分，帮助 HR 团队高效完成人才盘点、晋升评估和绩效排名。

---

## 核心功能

| 模块 | 功能说明 |
|------|----------|
| **认证授权** | JWT Token 认证，Spring Security 拦截校验 |
| **人才管理** | 人才信息 CRUD，支持自定义指标值存储 |
| **指标管理** | 动态配置评估指标（文本/数值/枚举），支持分值映射、权重、方向 |
| **人才筛选** | 多条件组合筛选（AND/OR 逻辑），支持 EQ/IN/GT/GTE/LT/LTE/LIKE 等操作符 |
| **智能评分** | 集成 **Aviator 表达式引擎**，支持自定义评分公式，自动归一化+排名 |
| **缓存加速** | Redis 缓存评分结果、人才列表，10 分钟自动过期 |
| **API 文档** | Knife4j (OpenAPI 3) 自动生成，支持在线调试 |

---

## 技术栈

| 层面 | 技术 | 版本 |
|------|------|------|
| 核心框架 | Spring Boot | 3.2.5 |
| ORM | MyBatis-Plus | 3.5.6 |
| 数据库 | MySQL | 8.0+ |
| 缓存 | Redis | 6.0+ |
| 安全认证 | Spring Security + JWT (JJWT) | 0.12.5 |
| 表达式引擎 | Aviator | 5.4.3 |
| API 文档 | Knife4j (OpenAPI 3) | 4.3.0 |
| 连接池 | HikariCP + Lettuce (Redis) | - |
| 工具库 | Lombok | - |

---

## 环境要求

- **JDK** 17+
- **Maven** 3.6+
- **MySQL** 8.0+ (Docker 推荐)
- **Redis** 6.0+ (Docker 推荐)

---

## 快速启动

### 1. 启动 MySQL

```bash
docker run -d --name docker-mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root \
  mysql:8.0
```

### 2. 启动 Redis

```bash
docker run -d --name docker-redis -p 6379:6379 redis:7
```

### 3. 创建数据库

```bash
docker exec docker-mysql mysql -u root -proot -e "CREATE DATABASE IF NOT EXISTS hr;"
```

### 4. 编译并启动

```bash
cd backend

# 编译打包
mvn clean package -DskipTests

# 启动应用（首次启动自动建表 + 初始化测试数据）
java -jar target/talent-hr-1.0.0.jar

# 或直接 Maven 启动
mvn spring-boot:run
```

### 5. 验证

```bash
# 登录
curl -X POST http://localhost:8088/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# API 文档
open http://localhost:8088/doc.html
```

---

## 默认账户

| 用户名 | 密码 | 说明 |
|--------|------|------|
| admin | admin123 | 管理员账户 |

---

## 项目结构

```
backend/
├── pom.xml                            # Maven 配置
├── README.md
└── src/main/
    ├── java/com/talent/
    │   ├── TalentApplication.java     # 启动类 (@EnableCaching)
    │   ├── config/                    # 配置类
    │   │   ├── SecurityConfig.java    # Spring Security + JWT 过滤器
    │   │   ├── RedisConfig.java       # Redis 缓存配置
    │   │   ├── MyBatisPlusConfig.java # 分页插件
    │   │   └── MyMetaObjectHandler.java # 自动填充时间戳
    │   ├── controller/                # 控制器
    │   │   ├── AuthController.java    # 登录认证
    │   │   ├── TalentController.java  # 人才管理
    │   │   ├── AttributeController.java # 指标管理
    │   │   ├── ScreeningController.java # 筛选方案
    │   │   └── ScoringController.java # 评分管理
    │   ├── service/                   # 业务逻辑
    │   │   ├── TalentService.java
    │   │   ├── TalentAttributeService.java
    │   │   ├── ScreeningService.java
    │   │   ├── ScoringService.java    # 评分计算（Aviator）
    │   │   └── SelectionConditionService.java
    │   ├── entity/                    # 实体类
    │   │   ├── Talent.java
    │   │   ├── TalentAttribute.java
    │   │   ├── TalentAttrValue.java
    │   │   ├── ScreeningPlan.java
    │   │   ├── SelectionCondition.java
    │   │   ├── ScoringPlan.java
    │   │   └── TalentScore.java
    │   ├── mapper/                    # Mapper 接口
    │   ├── common/                    # 工具类
    │   │   ├── R.java                 # 统一响应
    │   │   ├── JwtUtil.java           # JWT 工具
    │   │   └── ExpressionEvaluator.java # Aviator 表达式求值器
    │   └── init/
    │       └── DataInitializer.java   # 初始化测试数据
    └── resources/
        ├── application.yml            # 应用配置
        └── schema.sql                 # 数据库建表脚本
```

---

## API 概览

| 模块 | 前缀 | 主要接口 |
|------|------|----------|
| 认证 | `/api/auth` | `POST /login` |
| 人才 | `/api/talent` | CRUD + 分页查询 + 详情（含指标值） |
| 指标 | `/api/attribute` | CRUD + 启用/禁用 |
| 筛选 | `/api/screening` | 方案 CRUD + 条件管理 + 执行筛选 |
| 评分 | `/api/scoring` | 方案 CRUD + 计算评分 + 排名查询 |

完整 API 文档访问：http://localhost:8088/doc.html

---

## 评分表达式

评分方案支持 **Aviator 表达式**自定义计算公式，变量名对应指标编码（code）：

```java
// 示例：加权综合评分
education * 0.25 + work_years * 0.20 + perf_rating * 0.20 
  + english_level * 0.10 + manage_years * 0.05 
  + cert_count * 0.05 + age * 0.05 + height * 0.05 
  + hometown * 0.05
```

支持 Aviator 全部语法：算术运算、条件判断 (`if/else`)、内置函数 (`math.abs` 等)。

---

## 配置说明

核心配置项见 `application.yml`：

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `server.port` | 服务端口 | 8088 |
| `spring.datasource.*` | MySQL 连接 | root/root@localhost:3306/hr |
| `spring.data.redis.*` | Redis 连接 | localhost:6379 |
| `spring.cache.redis.time-to-live` | 缓存过期时间 | 600000ms (10分钟) |
| `talent.jwt.secret` | JWT 密钥 | (见配置文件) |
| `talent.jwt.expiration` | Token 有效期 | 86400000ms (24小时) |

---

## 初始化数据

首次启动时自动初始化：

| 数据类型 | 数量 | 说明 |
|----------|------|------|
| 评估指标 | 9 个 | 学历、工作年限、身高、英语水平、籍贯、绩效、年龄、资格证书、管理年限 |
| 人才数据 | 20 条 | 模拟员工信息，覆盖技术/市场/人事/财务等部门 |
| 筛选方案 | 3 个 | 高级技术人才、年轻骨干、高学历或高绩效 |
| 评分方案 | 1 个 | 综合能力评分（加权9指标） |
