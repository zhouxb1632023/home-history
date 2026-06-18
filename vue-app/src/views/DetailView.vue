<template>
  <div class="detail-page">
    <div class="detail-header">
      <button class="back-btn" @click="$router.push('/')">←</button>
      <h2 class="detail-title">{{ media.originalName || '详情' }}</h2>
      <div class="spacer"></div>
    </div>

    <div class="detail-body">
      <video
        v-if="isVideo && videoUrl"
        :src="videoUrl"
        controls
        class="media-player"
        playsinline
      ></video>

      <img
        v-else-if="!isVideo && imageUrl"
        :src="imageUrl"
        :alt="media.originalName"
        class="media-image"
      />

      <div class="detail-info">
        <div class="info-section">
          <h3 class="info-label">📝 说明</h3>
          <p class="info-text">{{ media.description || '暂无说明' }}</p>
        </div>

        <div v-if="media.tags && media.tags.length" class="info-section">
          <h3 class="info-label">🏷️ 标签</h3>
          <div class="tags-wrap">
            <span
              v-for="tag in media.tags"
              :key="tag.id"
              class="detail-tag"
              :style="{ backgroundColor: tag.color || '#E07B4C' }"
            >
              {{ tag.name }}
            </span>
          </div>
        </div>

        <div v-if="media.takenAt" class="info-section">
          <h3 class="info-label">📅 拍摄时间</h3>
          <p class="info-time">{{ formatDate(media.takenAt) }}</p>
        </div>
      </div>
    </div>

    <!-- 底部导航栏 -->
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useRoute } from 'vue-router'
import { getMediaDetail } from '@/api'
import { mediaFileUrl } from '@/api/constants'

const router = useRouter()
const route = useRoute()
const media = ref({})

const isVideo = computed(() => media.value.mediaType === 'VIDEO')
const imageUrl = computed(() => {
  if (isVideo.value) return ''
  return mediaFileUrl(route.params.id)
})
const videoUrl = computed(() => {
  if (!isVideo.value) return ''
  return mediaFileUrl(route.params.id)
})

function formatDate(d) {
  if (!d) return ''
  const date = new Date(d)
  return date.toLocaleString('zh-CN', {
    year: 'numeric', month: 'long', day: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

async function loadDetail() {
  try {
    const data = await getMediaDetail(route.params.id)
    media.value = data
  } catch (e) { /* ignore */ }
}

async function handleLogout() {
  localStorage.removeItem('auth_token')
  localStorage.removeItem('auth_user')
  router.replace('/login')
}

loadDetail()
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  min-height: 100dvh;
  background: #fff;
  display: flex;
  flex-direction: column;
}

.detail-header {
  background: rgba(255, 255, 255, 0.95);
  border-bottom: 1px solid var(--warm-peach);
  height: 50px;
  display: flex;
  align-items: center;
  padding: 0 12px;
  position: sticky;
  top: 0;
  z-index: 50;
}

.back-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  padding: 4px 8px;
  color: var(--warm-orange);
}

.detail-title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  color: var(--warm-caramel);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.spacer { width: 40px; }

.detail-body {
  flex: 1;
  overflow-y: auto;
}

.media-player {
  width: 100%;
  max-height: 60vh;
  background: #000;
}

.media-image {
  width: 100%;
  max-height: 65vh;
  object-fit: contain;
  background: var(--warm-cream);
  display: block;
}

.detail-info {
  padding: 16px;
}

.info-section { margin-bottom: 18px; }
.info-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--warm-caramel);
  margin-bottom: 8px;
}

.info-text {
  color: var(--warm-caramel);
  font-size: 15px;
  line-height: 1.6;
}

.tags-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.detail-tag {
  padding: 5px 14px;
  border-radius: 14px;
  font-size: 13px;
  color: #fff;
}

.info-time {
  color: var(--warm-caramel);
  font-size: 14px;
}
</style>
