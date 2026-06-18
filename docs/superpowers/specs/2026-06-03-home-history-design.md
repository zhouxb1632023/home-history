# Home History - 家庭相册设计文档

> 设计日期: 2026-06-03
> 状态: 已确认

## 1. 项目概述

Home History 是一个局域网部署的家庭相册系统，支持浏览器和移动端访问。用户可以原封不动地上传手机拍摄的照片和视频，按日期或人物标签浏览，附带文字说明，界面温馨有趣。

## 2. 技术选型

| 层级 | 技术 | 说明 |
|------|------|------|
| 后端框架 | Spring Boot 3 + Java 17 | 单体应用，RESTful API |
| 数据库 | H2 (开发) / MySQL (生产) | JPA/Hibernate ORM |
| 认证 | Spring Security + JWT | BCrypt 密码加密 |
| 前端 | Vue 3 + Vite + Tailwind CSS | Pinia 状态管理 |
| 移动端 | Flutter 3 | Android + iOS 跨平台 |
| 文件存储 | 本地磁盘 | 原文件不压缩 |
| 视频处理 | FFmpeg | 生成视频封面缩略图 |
| 图片处理 | Java ImageIO / Thumbnailator | 生成图片缩略图 |

## 3. 数据库设计

### user 表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名，唯一 |
| password | VARCHAR(255) | BCrypt 加密 |
| nickname | VARCHAR(50) | 昵称 |
| avatar_url | VARCHAR(500) | 头像 URL |
| role | ENUM('ADMIN','MEMBER') | 角色 |
| created_at | TIMESTAMP | 创建时间 |

### media 表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 上传者 FK→user |
| filename | VARCHAR(500) | 存储文件名 UUID |
| original_name | VARCHAR(500) | 原始文件名 |
| file_path | VARCHAR(1000) | 存储路径 |
| media_type | ENUM('PHOTO','VIDEO') | 媒体类型 |
| file_size | BIGINT | 文件大小 |
| mime_type | VARCHAR(100) | MIME 类型 |
| width | INT | 宽度 |
| height | INT | 高度 |
| thumbnail_path | VARCHAR(1000) | 缩略图路径 |
| taken_at | TIMESTAMP | 拍摄日期 |
| description | TEXT | 文字说明 |
| created_at | TIMESTAMP | 上传时间 |

### tag 表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(50) | 标签名 |
| color | VARCHAR(7) | 标签颜色 #RRGGBB |
| created_by | BIGINT | 创建者 FK→user |

### media_tag 关联表
| 字段 | 类型 | 说明 |
|------|------|------|
| media_id | BIGINT | FK→media |
| tag_id | BIGINT | FK→tag |

## 4. API 接口

### 认证
- `POST /api/auth/register` - 注册
- `POST /api/auth/login` - 登录，返回 JWT
- `GET /api/auth/me` - 当前用户信息

### 媒体文件
- `POST /api/media/upload` - 上传（multipart），支持批量
- `GET /api/media?page=&size=&tag=&startDate=&endDate=&keyword=&type=` - 分页查询
- `GET /api/media/{id}` - 单个文件详情
- `GET /api/media/{id}/file` - 获取原文件流
- `GET /api/media/{id}/thumbnail` - 获取缩略图
- `GET /api/media/calendar?year=&month=` - 日历视图（每月有照片的日期）
- `PUT /api/media/{id}` - 更新描述
- `DELETE /api/media/{id}` - 删除文件

### 标签
- `GET /api/tags` - 标签列表
- `POST /api/tags` - 创建标签
- `PUT /api/media/{id}/tags` - 更新媒体标签
- `DELETE /api/tags/{id}` - 删除标签

## 5. 文件存储

```
storage/
├── photos/2024/06/abc123.jpg      # 原图
├── videos/2024/06/xyz456.mp4      # 原视频
└── thumbnails/2024/06/            # 缩略图
    ├── abc123_thumb.jpg
    └── xyz456_thumb.jpg
```

- 文件按年月分目录存储
- 存储文件名为 UUID，保留原始文件名在数据库
- 原文件完全不压缩
- 缩略图：图片 400px 宽，视频用 FFmpeg 截取第一帧

## 6. 前端页面结构

| 页面 | 路径 | 功能 |
|------|------|------|
| 登录/注册 | /login | 用户登录注册 |
| 首页 | / | 日历热力图 + 瀑布流时间线 + 今日回忆 |
| 上传 | /upload | 拖拽/点击上传，批量，填写描述和标签 |
| 详情 | /media/:id | 查看原图/播放视频，显示描述和标签 |
| 人物 | /people | 人物标签列表 + 按人物浏览 |

## 7. 视觉风格 - 暖阳手账风

- **主色调**: 暖橙 `#E07B4C` / 米白背景 `#FFF8F0` / 焦糖文字 `#8B6B5A`
- **设计元素**: 圆角卡片、虚线边框、便签卡片、柔和阴影
- **字体**: 标题可用衬线体/手写体，正文无衬线
- **交互**: 平滑过渡、暖色渐变、可爱小图标点缀

## 8. 移动端功能

Flutter App 功能与 Web 端一致：
- 照片/视频上传（从相册选择或拍照）
- 时间线浏览
- 人物标签筛选
- 视频播放
- 搜索
- JWT 登录

## 9. 部署

- 后端打包为 executable JAR
- 前端 `npm run build` 生成静态文件，由 Spring Boot 静态资源托管或 Nginx 反向代理
- 局域网内通过 IP:端口 访问
- Flutter App 配置 API 地址为局域网 IP

## 10. 非功能需求

- 局域网部署，无需公网访问
- 所有 API 需 JWT 认证（除登录注册）
- 文件上传支持断点续传（后续优化）
- 缩略图异步生成
- 支持主流图片格式（JPG、PNG、HEIC、WEBP）和视频格式（MP4、MOV）
