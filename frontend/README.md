# 人才绩效管理系统 - 前端

## 项目简介

人才绩效管理系统前端，基于 Vue 3 + Element Plus 构建，提供人才管理、指标配置、人才筛选和智能评分的可视化操作界面。

---

## 页面功能

| 页面 | 路由 | 功能说明 |
|------|------|----------|
| **登录页** | `/login` | 用户名密码登录，JWT Token 认证 |
| **人才列表** | `/talent/list` | 展示所有人才信息，动态显示指标列，搜索/添加/编辑/删除 |
| **添加人才** | `/talent/add` | 基本信息 + 动态指标表单（文本/数值/枚举） |
| **编辑人才** | `/talent/edit/:id` | 回填已有数据，支持修改全部字段 |
| **指标管理** | `/attribute/list` | 配置评估指标：数据类型、分值映射、权重、方向等 |
| **筛选方案** | `/screening/plans` | 创建筛选方案、配置多条件组合、执行筛选并查看结果 |
| **评分管理** | `/scoring/plans` | 创建评分方案、触发评分计算、查看排名与明细 |

---

## 技术栈

| 层面 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue 3 (Composition API) | ^3.4.21 |
| 构建工具 | Vite | ^5.2.0 |
| 路由 | Vue Router | ^4.3.0 |
| HTTP 客户端 | Axios | ^1.6.8 |
| UI 组件库 | Element Plus | ^2.7.0 |
| 图标 | @element-plus/icons-vue | ^2.3.1 |
| 状态管理 | Pinia | ^2.1.7 |

---

## 环境要求

- **Node.js** 18+
- **npm** 9+

---

## 快速启动

### 1. 安装依赖

```bash
cd frontend
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

开发服务器默认运行在 **http://localhost:3000**，API 请求自动代理到 `http://localhost:8088`。

### 3. 访问页面

打开浏览器访问 http://localhost:3000，使用默认账户登录：

| 用户名 | 密码 |
|--------|------|
| admin | admin123 |

---

## 项目结构

```
frontend/
├── index.html                     # 入口 HTML
├── package.json                   # 依赖与脚本
├── vite.config.js                 # Vite 配置（端口、代理）
└── src/
    ├── main.js                    # 应用入口（Vue、Pinia、ElementPlus 初始化）
    ├── App.vue                    # 根组件（全局布局：顶栏 + 侧栏 + 内容区）
    ├── router/
    │   └── index.js               # 路由配置 + 登录鉴权守卫
    ├── api/
    │   ├── http.js                # Axios 封装（Token 注入、响应拦截）
    │   └── index.js               # 所有 API 接口定义（5 模块 25 接口）
    └── views/
        ├── Login.vue              # 登录页
        ├── TalentList.vue         # 人才列表（动态列、搜索、删除）
        ├── TalentEdit.vue         # 添加/编辑人才（动态指标表单）
        ├── AttributeList.vue      # 指标管理（类型配置、分值映射）
        ├── ScreeningPlans.vue     # 筛选方案（多条件 + 执行筛选）
        └── ScoringPlans.vue       # 评分管理（计算 + 排名 + 明细）
```

---

## 代理配置

开发环境下，所有 `/api` 开头的请求会被 Vite 代理转发到后端：

```js
// vite.config.js
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8088',
      changeOrigin: true,
    }
  }
}
```

如需修改后端地址，编辑 `vite.config.js` 中的 `target` 字段。

---

## 构建部署

```bash
# 生产构建
npm run build

# 预览构建结果
npm run preview
```

构建产物输出至 `dist/` 目录，可直接部署到 Nginx、Cloudflare Pages 等静态托管平台。

---

## 功能截图说明

### 人才列表
- 表格动态展示人才基本信息 + 所有启用指标列
- 按姓名/部门关键字搜索
- 支持分页

### 指标管理
- 配置指标名、编码、数据类型
- 枚举类型可设置选项列表和分值映射
- 一键启用/禁用指标
- 设置权重和方向（越高越好 / 越低越好）

### 筛选方案
- 创建方案并配置 AND / OR 逻辑
- 每个方案支持多条筛选条件
- 操作符支持：EQ(=)、NE(≠)、GT(>)、GTE(≥)、LT(<)、LTE(≤)、IN、LIKE
- 一键执行筛选，弹窗展示匹配人才

### 评分管理
- 查看所有指标的权重参考面板
- 触发评分计算，服务端执行 Aviator 表达式求值
- 排名结果分色显示：绿色(≥70)、橙色(≥50)、红色(<50)
- 点击展开查看各项指标明细得分
