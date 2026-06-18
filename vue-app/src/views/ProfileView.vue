<template>
  <div class="profile-page">
    <!-- 顶部渐变背景 -->
    <div class="profile-header">
      <router-link to="/" class="back-btn">← 返回</router-link>
      <h1 class="page-title">我的</h1>
    </div>

    <div class="profile-body">
      <!-- 头像区域 -->
      <div class="avatar-section">
        <div
          class="avatar-circle"
          @click="triggerUpload"
        >
          <img v-if="userStore.avatarUrl" :src="avatarImgSrc" alt="头像" />
          <span v-else class="default-avatar">{{ defaultAvatar }}</span>
          <div class="avatar-overlay">
            <span class="overlay-icon">📷</span>
            <span class="overlay-text">更换</span>
          </div>
        </div>
        <input
          ref="fileInput"
          type="file"
          accept="image/*"
          class="hidden-input"
          @change="handleUpload"
        />
        <p class="avatar-hint">点击更换头像</p>
      </div>

      <!-- 用户名 -->
      <div class="info-card">
        <label class="info-label">用户名</label>
        <div class="info-value">{{ userInfo.username || userInfo.nickname || '未设置' }}</div>
      </div>

      <!-- 分割线 -->
      <div class="divider"></div>

      <!-- 操作列表 -->
      <div class="action-list">
        <div class="action-item" @click="switchAccount">
          <span class="action-icon">👤</span>
          <span class="action-text">切换账号</span>
          <span class="action-arrow">›</span>
        </div>
        <div class="action-item" @click="handleLogout">
          <span class="action-icon">🚪</span>
          <span class="action-text">退出登录</span>
          <span class="action-arrow">›</span>
        </div>
      </div>
    </div>

    <!-- 上传中遮罩 -->
    <div v-if="uploading" class="upload-mask">
      <div class="upload-spinner">
        <span class="spinner-icon">⏳</span>
        <p>上传中...</p>
      </div>
    </div>

    <!-- 底部导航栏 -->
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const uploading = ref(false)
const fileInput = ref(null)

const cartoonAvatars = ['🐱', '🐶', '🦊', '🐼', '🐨', '🐯', '🦁', '🐰', '🐻', '🐸']
const defaultAvatar = cartoonAvatars[Math.floor(Math.random() * cartoonAvatars.length)]

// 头像图片源：通过后端接口获取
const avatarImgSrc = computed(() => {
  if (userStore.avatarUrl) {
    return `/api/auth/avatar?t=${Date.now()}`
  }
  return ''
})

// 用户信息：包含 username 和 nickname
const userInfo = computed(() => {
  let saved = {}
  try {
    saved = JSON.parse(localStorage.getItem('auth_user') || '{}')
  } catch {
    saved = {}
  }
  return { username: saved.username || '', nickname: userStore.nickname }
})

function triggerUpload() {
  fileInput.value?.click()
}

async function handleUpload(event) {
  const file = event.target.files?.[0]
  if (!file) return
  uploading.value = true
  try {
    await userStore.uploadAvatar(file)
  } catch (e) {
    console.error('头像上传失败', e)
    alert('头像上传失败，请重试')
  } finally {
    uploading.value = false
    event.target.value = ''
  }
}

function switchAccount() {
  userStore.clearAuth()
  router.push('/login')
}

function handleLogout() {
  if (confirm('确定要退出登录吗？')) {
    userStore.clearAuth()
    router.push('/login')
  }
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  min-height: 100dvh;
  background: var(--warm-cream);
  padding-bottom: 80px;
}

.profile-header {
  background: linear-gradient(135deg, var(--warm-orange), #f09433);
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  color: #fff;
  font-size: 16px;
  text-decoration: none;
  min-width: 44px;
  min-height: 44px;
  display: flex;
  align-items: center;
}

.page-title {
  color: #fff;
  font-size: 20px;
  font-weight: bold;
  font-family: serif;
  margin: 0;
}

.profile-body {
  padding: 0 16px;
  margin-top: -20px;
}

/* 头像 */
.avatar-section {
  text-align: center;
  margin-bottom: 24px;
}

.avatar-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 4px solid #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  cursor: pointer;
  overflow: hidden;
  position: relative;
  background: var(--warm-yellow);
  margin: 0 auto;
  transition: opacity 0.2s;
}

.avatar-circle:hover {
  opacity: 0.85;
}

.avatar-circle img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-avatar {
  font-size: 36px;
  line-height: 80px;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
  border-radius: 50%;
}

.avatar-circle:hover .avatar-overlay {
  opacity: 1;
}

.overlay-icon {
  font-size: 18px;
}

.overlay-text {
  font-size: 10px;
  color: #fff;
}

.avatar-hint {
  color: var(--warm-caramel);
  font-size: 12px;
  margin-top: 8px;
}

.hidden-input {
  display: none;
}

/* 信息卡片 */
.info-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 12px;
  border: 1.5px solid var(--warm-peach);
}

.info-label {
  font-size: 13px;
  color: var(--warm-caramel);
  font-weight: 500;
  display: block;
  margin-bottom: 8px;
}

.info-value {
  font-size: 15px;
  color: var(--warm-caramel);
  padding: 10px 14px;
  background: var(--warm-cream);
  border-radius: 12px;
}

/* 分割线 */
.divider {
  height: 1px;
  background: var(--warm-peach);
  margin: 20px 0;
}

/* 操作列表 */
.action-list {
  background: #fff;
  border-radius: 16px;
  border: 1.5px solid var(--warm-peach);
  overflow: hidden;
}

.action-item {
  display: flex;
  align-items: center;
  padding: 16px;
  cursor: pointer;
  transition: background 0.15s;
  -webkit-tap-highlight-color: transparent;
}

.action-item:active {
  background: var(--warm-cream);
}

.action-item + .action-item {
  border-top: 1px solid var(--warm-peach);
}

.action-icon {
  font-size: 20px;
  margin-right: 12px;
}

.action-text {
  flex: 1;
  font-size: 15px;
  color: var(--warm-caramel);
}

.action-arrow {
  font-size: 20px;
  color: #ccc;
}

/* 上传遮罩 */
.upload-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.upload-spinner {
  background: #fff;
  border-radius: 16px;
  padding: 24px 32px;
  text-align: center;
}

.spinner-icon {
  font-size: 32px;
}

.upload-spinner p {
  color: var(--warm-caramel);
  font-size: 14px;
  margin-top: 8px;
}

</style>