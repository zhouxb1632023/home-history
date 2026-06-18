# 家庭相册 (Vue)

基于 Vue 3 + Vite 开发的家庭相册 App，可通过 HBuilderX 打包为 Android/iOS 应用。

## 功能

- 用户注册 / 登录
- 照片/视频浏览（网格视图）
- 标签筛选
- 照片详情查看
- 照片/视频上传

## 快速开始

### 1. 安装依赖

```bash
cd vue-app
npm install
```

### 2. 开发模式

```bash
npm run dev
```

在浏览器中打开 `http://localhost:5173` 预览。

### 3. 用 HBuilderX 打包

1. 打开 HBuilderX
2. **文件 → 新建 → 5+App 项目**（或使用现有项目导入方式）
3. 将 `vue-app/dist` 目录内容作为应用的 `www` 目录
4. 在 `manifest.json` 中配置应用基本信息
5. 点击 **发行 → 云打包** 或 **本地打包**

### 4. 构建生产包

```bash
npm run build
```

构建产物在 `dist/` 目录。

## 项目结构

```
vue-app/
├── src/
│   ├── api/           # API 请求层
│   ├── assets/        # 静态资源
│   ├── components/    # 公共组件
│   ├── router/        # 路由配置
│   ├── stores/        # Pinia 状态管理
│   ├── views/         # 页面组件
│   ├── App.vue        # 根组件
│   └── main.js        # 入口
├── public/            # 静态资源
├── index.html         # HTML 入口
├── package.json
├── vite.config.js     # Vite 配置
└── dist/              # 构建产物（HBuilderX 使用）
```

## 后端对接

默认后端地址: `http://127.0.0.1:8089/api`
可在 `src/api/constants.js` 中修改。

## 后端 API 接口

| 方法   | 路径              | 说明           |
|--------|-------------------|----------------|
| POST   | /api/auth/login   | 登录           |
| POST   | /api/auth/register| 注册           |
| GET    | /api/tags         | 获取标签列表   |
| GET    | /api/media        | 搜索媒体       |
| POST   | /api/media/upload | 上传媒体       |
| GET    | /api/media/:id    | 媒体详情       |
| GET    | /api/media/:id/file | 媒体文件下载 |
| GET    | /api/media/:id/thumbnail | 缩略图  |
