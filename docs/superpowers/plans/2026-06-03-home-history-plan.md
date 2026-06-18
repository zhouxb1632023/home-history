# Home History 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建家庭相册系统 - 支持 Web + 移动端，局域网部署，原文件上传不压缩，按日期/人物标签浏览。

**Architecture:** Spring Boot 3 单体后端提供 REST API + JWT 认证，Vue 3 前端 SPA，Flutter 移动端 App。本地磁盘存储，H2 数据库。

**Tech Stack:** Spring Boot 3 / Java 17 / JPA / JWT / Vue 3 / Vite / Tailwind CSS / Pinia / Flutter 3 / FFmpeg

---

## Phase 1: 后端基础框架

### Task 1: 创建 Spring Boot 项目骨架

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/homehistory/HomeHistoryApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/resources/application-dev.yml`

**pom.xml** - Spring Boot 3.2.5 父项目，依赖: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security, spring-boot-starter-validation, h2, mysql-connector-j, jjwt-api/impl/jackson 0.12.5, thumbnailator 0.4.20, metadata-extractor 2.19.0, lombok. multipart max-file-size: 2048MB.

**HomeHistoryApplication.java** - @SpringBootApplication 主类，main 方法启动。

**application.yml** - 激活 dev profile，multipart 2048MB，jpa ddl-auto: update，open-in-view: false。自定义配置: app.jwt.secret, app.jwt.expiration-ms: 86400000, app.storage.path: ./storage, app.thumbnail.width: 400.

**application-dev.yml** - H2 file database ./data/homehistory，h2 console enabled，server.port: 8089.

验证: `cd backend && mvn spring-boot:run` → 启动在 8089 端口。

---

### Task 2: 数据库实体模型

**Files:**
- Create: `backend/src/main/java/com/homehistory/model/enums/UserRole.java` (ADMIN, MEMBER)
- Create: `backend/src/main/java/com/homehistory/model/enums/MediaType.java` (PHOTO, VIDEO)
- Create: `backend/src/main/java/com/homehistory/model/User.java` - @Entity @Table("app_user"), id, username(unique), password, nickname, avatarUrl, role(@Enumerated), createdAt(@PrePersist)
- Create: `backend/src/main/java/com/homehistory/model/Media.java` - id, user(@ManyToOne), filename, originalName, filePath, mediaType(@Enumerated), fileSize, mimeType, width, height, thumbnailPath, takenAt, description(TEXT), createdAt
- Create: `backend/src/main/java/com/homehistory/model/Tag.java` - id, name, color(default #E07B4C), createdBy(@ManyToOne→User), createdAt
- Create: `backend/src/main/java/com/homehistory/model/MediaTag.java` - id, media(@ManyToOne→Media), tag(@ManyToOne→Tag)

全部使用 Lombok @Data @Builder @NoArgsConstructor @AllArgsConstructor.

---

### Task 3: JPA Repository 层

**Files:**
- Create: `backend/src/main/java/com/homehistory/repository/UserRepository.java` - findByUsername, existsByUsername
- Create: `backend/src/main/java/com/homehistory/repository/MediaRepository.java` - searchMedia(@Query JPQL，支持 tagId/startDate/endDate/keyword/type 动态过滤+分页), findDatesWithMedia(year, month)
- Create: `backend/src/main/java/com/homehistory/repository/TagRepository.java` - findAllByOrderByNameAsc, existsByName
- Create: `backend/src/main/java/com/homehistory/repository/MediaTagRepository.java` - findByMediaId, deleteByMediaId

---

### Task 4: JWT 安全配置

**Files:**
- Create: `backend/src/main/java/com/homehistory/security/JwtUtil.java` - generateToken(HS256), extractUsername, validateToken，密钥从 ${app.jwt.secret} 注入
- Create: `backend/src/main/java/com/homehistory/security/JwtAuthFilter.java` - OncePerRequestFilter，从 Authorization: Bearer header 提取 token，验证后设置 SecurityContext
- Create: `backend/src/main/java/com/homehistory/security/CustomUserDetailsService.java` - 从 UserRepository 加载用户
- Create: `backend/src/main/java/com/homehistory/config/SecurityConfig.java` - 关闭 CSRF，STATELESS session，/api/auth/** 和 /h2-console/** permitAll，其余需认证，添加 JwtAuthFilter
- Create: `backend/src/main/java/com/homehistory/config/CorsConfig.java` - 允许所有来源的 CORS

---

## Phase 2: 后端核心业务

### Task 5: DTO 定义

**Files:**
- Create: `backend/src/main/java/com/homehistory/dto/RegisterRequest.java` - username(@NotBlank @Size 2-50), password(@NotBlank @Size 4-100), nickname(@Size max 50)
- Create: `backend/src/main/java/com/homehistory/dto/LoginRequest.java` - username, password @NotBlank
- Create: `backend/src/main/java/com/homehistory/dto/LoginResponse.java` - token, username, nickname, role
- Create: `backend/src/main/java/com/homehistory/dto/MediaResponse.java` - id, originalName, mediaType, fileSize, mimeType, width, height, takenAt, description, uploaderName, tags(List<TagDto>), createdAt. TagDto: id, name, color
- Create: `backend/src/main/java/com/homehistory/dto/TagRequest.java` - name(@NotBlank), color
- Create: `backend/src/main/java/com/homehistory/dto/CalendarDay.java` - day(int), count(int)

---

### Task 6: AuthService & AuthController

**Files:**
- Create: `backend/src/main/java/com/homehistory/service/AuthService.java` - register(RegisterRequest): 检查用户名唯一，BCrypt 加密密码，保存用户，生成 JWT 返回 LoginResponse。login(LoginRequest): 验证用户名密码，生成 JWT 返回。
- Create: `backend/src/main/java/com/homehistory/controller/AuthController.java` - POST /api/auth/register, POST /api/auth/login

---

### Task 7: FileService 文件存储服务

**Files:**
- Create: `backend/src/main/java/com/homehistory/service/FileService.java`

核心方法:
- storeFile(MultipartFile, isVideo): 按年月分目录存储(storage/photos/2024/06/ 或 storage/videos/2024/06/)，UUID 文件名，保留扩展名
- generateThumbnail(filePath, isVideo): 图片用 Thumbnailator 缩放到 400px 宽，视频用 FFmpeg 截取第1秒帧 -> storage/thumbnails/2024/06/
- deleteFile(filePath): 删除文件
- @PostConstruct init(): 创建 photos/videos/thumbnails 目录

---

### Task 8: MediaService 完整实现

**Files:**
- Create: `backend/src/main/java/com/homehistory/service/MediaService.java`
- Create: `backend/src/main/java/com/homehistory/controller/MediaController.java`

**MediaService 方法:**
- upload(List<MultipartFile>, description, tagIds, takenAt): 遍历文件，调用 FileService 存储，生成缩略图，保存 Media 实体，关联标签，返回 MediaResponse 列表
- search(tagId, startDate, endDate, keyword, type, page, size): 调用 MediaRepository.searchMedia，分页返回
- getById(id): 返回单个
- updateDescription(id, desc): 更新描述
- updateTags(mediaId, tagIds): 先删除旧关联，再创建新关联
- delete(id): 删除文件+缩略图+关联+数据库记录
- getCalendar(year, month): 获取当月有照片的日期
- toResponse(Media): 实体转 DTO

**MediaController 端点:**
- POST /api/media/upload (multipart)
- GET /api/media (查询参数: tagId, startDate, endDate, keyword, type, page, size)
- GET /api/media/{id}
- GET /api/media/{id}/file (返回原文件流)
- GET /api/media/{id}/thumbnail (返回缩略图)
- GET /api/media/calendar?year=&month=
- PUT /api/media/{id}/description
- PUT /api/media/{id}/tags
- DELETE /api/media/{id}

---

### Task 9: TagService & TagController

**Files:**
- Create: `backend/src/main/java/com/homehistory/service/TagService.java` - getAll(), create(TagRequest), delete(id)
- Create: `backend/src/main/java/com/homehistory/controller/TagController.java` - GET /api/tags, POST /api/tags, DELETE /api/tags/{id}

---

## Phase 3: Vue 3 前端

### Task 10: Vue 项目初始化

Run: `npm create vite@latest frontend -- --template vue`
Install: `vue-router@4 pinia axios tailwindcss @tailwindcss/vite video.js`

**vite.config.js** - proxy /api → localhost:8089，proxy /storage → localhost:8089

**src/style.css** - @import "tailwindcss"，自定义 warm 主题色(@theme): warm-orange #E07B4C, warm-cream #FFF8F0, warm-caramel #8B6B5A, warm-yellow #FFECD2, warm-peach #FFD4B8

---

### Task 11: 前端核心基础设施

**Files:**
- Create: `frontend/src/utils/api.js` - axios 实例，baseURL: /api，拦截器自动附加 JWT token，401 自动跳转登录
- Create: `frontend/src/stores/auth.js` - Pinia store: login/register/logout，持久化到 localStorage
- Create: `frontend/src/router/index.js` - 路由: /login, / (Home, requiresAuth), /upload (requiresAuth), /media/:id (requiresAuth), /people (requiresAuth)，路由守卫检查 token

---

### Task 12: 登录注册页面

**File:** `frontend/src/views/Login.vue`
- 暖阳手账风: 米白背景，白色圆角卡片，虚线边框，暖橙色标题
- 登录/注册切换 tab
- 表单: 用户名、密码、(注册时)昵称
- 错误提示，成功后跳转首页

---

### Task 13: 首页 & 核心组件

**Files:**
- Create: `frontend/src/components/NavBar.vue` - 白色半透明导航栏，品牌名，上传/人物链接，用户昵称，退出按钮
- Create: `frontend/src/components/PhotoCard.vue` - 圆角卡片，缩略图，视频标识(▶播放按钮)，标签badge，描述文字，日期
- Create: `frontend/src/views/Home.vue` - 问候语(根据时间变化)，日期范围筛选，标签筛选，关键词搜索，瀑布流网格(grid-cols-2 md:3 lg:4)，分页，空状态引导上传

---

### Task 14: 上传页面

**File:** `frontend/src/views/Upload.vue`
- 拖拽/点击上传区域，虚线边框
- 文件列表预览(名称+大小，可移除)
- 文字说明 textarea
- 人物标签选择(彩色按钮，多选)
- 拍摄日期选择
- 上传进度提示
- 成功后跳转首页

---

### Task 15: 详情页 & 人物页

**Files:**
- Create: `frontend/src/views/MediaDetail.vue` - 原图展示/视频播放器，描述，标签，元信息(日期/上传者/文件大小)，返回按钮
- Create: `frontend/src/views/People.vue` - 标签按钮列表(彩色圆角)，点击筛选对应人物照片，瀑布流展示

---

## Phase 4: Flutter 移动端

### Task 16: Flutter 项目初始化

Run: `flutter create --org com.homehistory mobile`

**pubspec.yaml 添加依赖:**
dio, image_picker, video_player, cached_network_image, shared_preferences, intl, path_provider

---

### Task 17: Flutter 核心代码

**Files:**
- Create: `mobile/lib/theme/app_theme.dart` - warmOrange/warmCream/warmCaramel 常量，warmTheme ThemeData
- Create: `mobile/lib/services/api_service.dart` - Dio 单例，拦截器附加 JWT token，baseUrl 指向局域网 IP:8089
- Create: `mobile/lib/services/auth_service.dart` - login/register/logout，SharedPreferences 持久化 token
- Create: `mobile/lib/main.dart` - 检查登录状态，路由到 LoginScreen 或 HomeScreen
- Create: `mobile/lib/screens/login_screen.dart` - 登录/注册切换，暖色主题输入框
- Create: `mobile/lib/screens/home_screen.dart` - AppBar + 搜索栏 + 标签筛选 + GridView 照片网格 + 下拉刷新
- Create: `mobile/lib/screens/upload_screen.dart` - ImagePicker 选择照片/视频，描述输入，标签选择，上传
- Create: `mobile/lib/screens/detail_screen.dart` - 全屏图片/视频播放，描述，标签，元信息
- Create: `mobile/lib/widgets/photo_grid_item.dart` - CachedNetworkImage 缩略图，视频标识

---

## 验证清单

- [ ] 后端启动验证 (`mvn spring-boot:run`)
- [ ] 注册/登录 API 测试
- [ ] 上传图片/视频并验证缩略图生成
- [ ] 前端启动验证 (`npm run dev`)
- [ ] 全流程: 注册→登录→上传→浏览→详情→人物筛选
- [ ] Flutter App 启动并连接后端
- [ ] 移动端全流程验证
