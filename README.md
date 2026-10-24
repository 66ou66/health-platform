# 健康颐养平台 (health_platform)

面向患者、志愿者和管理员三方的前后端分离健康服务平台，支持服务需求发布与接单、健康档案、SOS 紧急求助、志愿者任务分配与管理后台等功能。

## 技术栈

| 端 | 技术 |
| --- | --- |
| 后端 | Spring Boot 3.2.5 · Java 21 · MyBatis-Plus · MySQL · JWT (jjwt) |
| 前端 | Vue 3 · Vite · Element Plus · Pinia · Vue Router · Axios · Sass |
| 地图 | 高德地图 Web 服务（逆地理编码） |

## 目录结构

```
health_platform/
├── README.md                          # 项目说明（本文件）
├── health-platform-backend/           # 后端服务（Spring Boot + Maven）
│   ├── pom.xml                        # Maven 配置
│   ├── mvnw / mvnw.cmd                # Maven Wrapper
│   └── src/
│       ├── main/
│       │   ├── java/edu/dlnu/health_platform/
│       │   │   ├── App.java           # 启动类（含 @SpringBootApplication）
│       │   │   ├── config/            # 配置类（MyBatis-Plus、Web 拦截器注册）
│       │   │   ├── controller/        # 接口层（admin/auth/patient/volunteer）
│       │   │   ├── dal/               # Mapper 层（MyBatis-Plus）
│       │   │   ├── entity/            # 实体类
│       │   │   ├── interceptor/       # 登录拦截器
│       │   │   ├── service/           # 业务层（含 impl/ 实现）
│       │   │   ├── utils/             # 工具类（JWT、高德、用户上下文）
│       │   │   └── vo/                # 视图对象（request/ 请求、response/ 响应）
│       │   └── resources/
│       │       ├── application.yml    # 应用配置（端口、数据库、JWT、高德 Key，敏感项已改为 ${环境变量:默认值}）
│       │       ├── application-local.yml  # 本地真实配置（已加入 .gitignore，不入库，首次使用需自行创建）
│       │       └── sql/health_platform.sql   # 数据库初始化脚本
│       └── test/java/edu/dlnu/health_platform/  # 单元测试
└── health-platform-frontend/          # 前端应用（Vue 3 + Vite）
    ├── package.json                   # 依赖与脚本
    ├── vite.config.js                 # Vite 配置
    ├── index.html
    └── src/
        ├── api/                       # 接口请求封装（admin/patient/user/volunteer）
        ├── assets/                    # 静态资源与全局样式
        ├── components/                # 公共组件
        ├── router/                    # 路由
        ├── stores/                    # Pinia 状态管理
        ├── utils/                     # 工具（请求实例、常量、高德）
        └── views/                     # 页面（admin/patient/volunteer）
```

## 启动方式

### 后端

1. 准备 MySQL，执行 `health-platform-backend/src/main/resources/sql/health_platform.sql` 初始化数据库；
2. 配置数据库账号密码、JWT 密钥与高德 Key：推荐在 `health-platform-backend/src/main/resources/` 下新建 `application-local.yml`（已被 .gitignore 忽略，不会入库），覆盖 `application.yml` 中带默认值的配置项；也可以直接通过环境变量 `DB_USERNAME` / `DB_PASSWORD` / `JWT_SECRET` / `AMAP_WEB_SERVICE_KEY` 注入。注意：高德 Key 没有内置默认值，不配置则地图逆地理编码功能不可用；
3. 启动服务：

```bash
cd health-platform-backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

服务地址：`http://localhost:8080/health_platform`

### 前端

```bash
cd health-platform-frontend
npm install
npm run dev
```

## 角色说明

- **患者 (patient)**：发布服务需求、上传健康档案、发起 SOS 求助、查看请求进度；
- **志愿者 (volunteer)**：查看任务大厅、接单、上报位置、完成任务并评价；
- **管理员 (admin)**：用户审核、任务分配、SOS 监控、服务分类管理。

## 演示视频

> TODO：演示视频暂未随仓库分发（仓库不存放约 12MB 的视频文件），后续将通过 **B 站链接** 或 **GitHub Release 附件** 的方式补充，敬请期待。
