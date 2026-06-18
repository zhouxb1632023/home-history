<template>
  <div class="home-page">
    <!-- 顶部导航栏（纯标题） -->
    <nav class="navbar">
      <span class="nav-title">📸 家庭相册</span>
      <button class="filter-toggle" @click="showFilter = !showFilter">
        <template v-if="showFilter">✕</template>
        <template v-else>🔍</template>
      </button>
    </nav>

    <!-- 搜索遮罩层 -->
    <Transition name="filter">
      <div v-if="showFilter" class="filter-overlay" @click.self="showFilter = false">
        <div class="filter-panel">
          <h3 class="filter-title">筛选</h3>
          <div class="filter-row">
            <label class="filter-label">开始日期</label>
            <input type="date" v-model="startDate" class="filter-input" />
          </div>
          <div class="filter-row">
            <label class="filter-label">结束日期</label>
            <input type="date" v-model="endDate" class="filter-input" />
          </div>
          <div class="filter-row">
            <label class="filter-label">人物</label>
            <select v-model="filterTag" class="filter-input">
              <option value="">全部人物</option>
              <option v-for="tag in tags" :key="tag.id" :value="tag.id">{{ tag.name }}</option>
            </select>
          </div>
          <div class="filter-row">
            <label class="filter-label">关键词</label>
            <input v-model="keyword" placeholder="搜索说明..." class="filter-input" @keyup.enter="doSearch" />
          </div>
          <button class="btn-search" @click="doSearch">🔍 搜索</button>
          <button class="btn-reset" @click="resetFilter">重置</button>
        </div>
      </div>
    </Transition>

    <!-- 问候语 -->
    <div class="greeting">
      <h2 class="greeting-text">{{ greeting }}</h2>
      <p class="greeting-date">{{ todayStr }}</p>
    </div>

    <!-- 内容区域 -->
    <div class="content">
      <div v-if="loading" class="loading">加载中...</div>

      <div v-else-if="groupedDays.length === 0" class="empty">
        <div class="empty-icon">📲</div>
        <p>还没有照片，去上传第一张吧！</p>
        <router-link to="/upload" class="empty-btn">📷 上传照片</router-link>
      </div>

      <template v-else>
        <div class="timeline">
          <div v-for="(day, di) in groupedDays" :key="day.date" class="timeline-day">
            <div class="day-marker" :class="{ 'today-marker': di === 0 }">
              <span class="day-num">{{ day.dayNum }}</span>
              <span class="month-str">{{ day.monthStr }}</span>
            </div>
            <div class="day-info">
              <p class="day-weekday">{{ day.weekday }}</p>
              <p class="day-date">{{ day.date }} · {{ day.photos.length }} 张</p>
            </div>
            <div class="photo-grid">
              <div
                v-for="media in day.photos"
                :key="media.id"
                class="photo-card"
                @click="goDetail(media)"
              >
                <div class="photo-wrapper">
                  <img
                    :src="thumbUrl(media.id)"
                    :alt="media.description || ''"
                    loading="lazy"
                  />
                  <div v-if="media.mediaType === 'VIDEO'" class="play-badge"><span>▶</span></div>
                </div>
                <div class="photo-info">
                  <div class="photo-tags">
                    <span
                      v-for="tag in media.tags"
                      :key="tag.id"
                      class="photo-tag"
                      :style="{ backgroundColor: tag.color }"
                    >
                      {{ tag.name }}
                    </span>
                  </div>
                  <p class="photo-desc" v-if="media.description">{{ media.description }}</p>
                  <p class="photo-time">{{ formatDate(media.takenAt) }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- 底部导航栏 -->
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTags, searchMedia } from '@/api'
import { mediaThumbnailUrl } from '@/api/constants'

const router = useRouter()
const allPhotos = ref([])
const tags = ref([])
const loading = ref(true)
const showFilter = ref(false)
const startDate = ref('')
const endDate = ref('')
const filterTag = ref('')
const keyword = ref('')

const WEEKDAYS = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了 🌙'
  if (h < 12) return '早上好 ☀️'
  if (h < 18) return '午后时光 📉'
  return '傍晚好 🍵'
})

const todayStr = computed(() =>
  new Date().toLocaleDateString('zh-CN', {
    year: 'numeric', month: 'long', day: 'numeric', weekday: 'long'
  })
)

function formatDateGroup(d) {
  const date = new Date(d)
  return {
    dayNum: date.getDate(),
    monthStr: (date.getMonth() + 1) + '月',
    weekday: WEEKDAYS[date.getDay()]
  }
}

const groupedDays = computed(() => {
  const groups = new Map()
  for (const photo of allPhotos.value) {
    const dateKey = photo.takenAt ? photo.takenAt.substring(0, 10) : '未知日期'
    if (!groups.has(dateKey)) {
      groups.set(dateKey, { date: dateKey, ...formatDateGroup(dateKey), photos: [] })
    }
    groups.get(dateKey).photos.push(photo)
  }
  return [...groups.values()]
})

function thumbUrl(id) {
  return mediaThumbnailUrl(id)
}

function formatDate(d) {
  if (!d) return ''
  const date = new Date(d)
  return date.toLocaleDateString('zh-CN', { month: 'long', day: 'numeric' })
}

async function loadTags() {
  try { tags.value = await getTags() } catch (e) { /* ignore */ }
}

async function doSearch() {
  showFilter.value = false
  loading.value = true
  allPhotos.value = []
  try {
    const params = { size: 200 }
    if (startDate.value) params.startDate = startDate.value
    if (endDate.value) params.endDate = endDate.value
    if (filterTag.value) params.tagId = filterTag.value
    if (keyword.value) params.keyword = keyword.value
    const res = await searchMedia(params)
    allPhotos.value = res.content || []
  } catch (e) {
    allPhotos.value = []
  } finally {
    loading.value = false
  }
}

function resetFilter() {
  startDate.value = ''
  endDate.value = ''
  filterTag.value = ''
  keyword.value = ''
  doSearch()
}

function goDetail(media) {
  router.push(`/detail/${media.id}`)
}

async function handleLogout() {
  localStorage.removeItem('auth_token')
  localStorage.removeItem('auth_user')
  router.replace('/login')
}

onMounted(() => { loadTags().then(doSearch) })
</script>

<style scoped>
.home-page { padding-bottom: 80px;  padding-bottom: 80px;
  min-height: 100vh;
  min-height: 100dvh;
  background: var(--warm-cream);
  display: flex;
  flex-direction: column;
}

/* 顶部导航栏 */
.navbar {
  background: var(--warm-orange);
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 14px;
  position: sticky;
  top: 0;
  z-index: 50;
}

.nav-title {
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  font-family: serif;
}

.filter-toggle {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  font-size: 22px;
  cursor: pointer;
  padding: 4px 8px;
  color: #fff;
  min-height: 44px;
  min-width: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 搜索遮罩 */
.filter-overlay {
  position: fixed;
  inset: 0;
  z-index: 60;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  justify-content: flex-end;
}

.filter-panel {
  width: 85%;
  max-width: 360px;
  background: var(--warm-cream);
  padding: 20px 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.filter-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--warm-caramel);
  margin: 0;
  text-align: center;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--warm-peach);
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-label {
  font-size: 14px;
  color: var(--warm-caramel);
  white-space: nowrap;
  min-width: 70px;
}

.filter-input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid var(--warm-peach);
  border-radius: 12px;
  background: #fff;
  font-size: 14px;
  color: var(--warm-caramel);
  outline: none;
  min-height: 40px;
}

.filter-input:focus {
  border-color: var(--warm-orange);
  box-shadow: 0 0 0 2px rgba(224, 123, 76, 0.12);
}

.btn-search {
  width: 100%;
  padding: 14px;
  background: var(--warm-orange);
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  min-height: 48px;
}

.btn-reset {
  width: 100%;
  padding: 12px;
  background: #fff;
  color: var(--warm-caramel);
  border: 1px solid var(--warm-peach);
  border-radius: 12px;
  font-size: 14px;
  cursor: pointer;
  min-height: 44px;
}

/* 搜索面板过渡动画 */
.filter-enter-active {
  transition: transform 0.3s ease;
}
.filter-leave-active {
  transition: transform 0.25s ease;
}
.filter-enter-from,
.filter-leave-to {
  transform: translateX(100%);
}
.filter-enter-to,
.filter-leave-from {
  transform: translateX(0);
}
.filter-overlay-enter-active {
  transition: opacity 0.3s ease;
}
.filter-overlay-leave-active {
  transition: opacity 0.2s ease;
}
.filter-overlay-enter-from,
.filter-overlay-leave-to {
  opacity: 0;
}

/* 问候语 */
.greeting {
  text-align: center;
  padding: 0 14px 10px;
}

.greeting-text {
  color: var(--warm-orange);
  font-size: 17px;
  font-family: serif;
}

.greeting-date {
  color: var(--warm-caramel);
  font-size: 12px;
  margin-top: 2px;
}

/* 内容区 */
.content {
  flex: 1;
  padding: 0 8px;
  overflow-y: auto;
  padding-bottom: 8px;
}

.loading { text-align: center; padding: 60px 16px; color: var(--warm-caramel); }
.empty { text-align: center; padding: 60px 16px; }
.empty-icon { font-size: 44px; margin-bottom: 12px; }

.empty-btn {
  margin-top: 14px;
  display: inline-block;
  padding: 10px 22px;
  background: var(--warm-orange);
  color: #fff;
  border-radius: 12px;
  font-size: 14px;
}

/* 时间线 */
.timeline {
  position: relative;
  padding-left: 20px;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 6px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: var(--warm-peach);
}

.timeline-day { position: relative; padding-bottom: 24px; }
.timeline-day:last-child { padding-bottom: 0; }

.day-marker {
  position: absolute;
  left: -16px;
  top: 0;
  width: 32px;
  height: 32px;
  background: var(--warm-orange);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  z-index: 1;
  box-shadow: 0 2px 6px rgba(224, 123, 76, 0.3);
}

.day-marker .day-num { font-size: 12px; font-weight: bold; line-height: 1; }
.day-marker .month-str { font-size: 7px; line-height: 1; margin-top: 1px; }

.day-marker.today-marker {
  box-shadow: 0 0 0 5px rgba(255, 236, 210, 0.5), 0 2px 6px rgba(224, 123, 76, 0.3);
}

.day-info { margin-left: 46px; margin-bottom: 8px; }
.day-weekday { font-size: 13px; font-weight: 500; color: var(--warm-caramel); margin: 0; }
.day-date { font-size: 11px; color: #999; margin: 2px 0 0; }

.photo-grid {
  margin-left: 46px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.photo-card {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.2s;
  border: 1px solid var(--warm-cream);
}

.photo-card:active {
  transform: scale(0.97);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.photo-wrapper {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  background: var(--warm-cream);
}

.photo-wrapper img { width: 100%; height: 100%; object-fit: cover; }

.play-badge {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.play-badge::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
}

.play-badge span {
  position: relative;
  z-index: 1;
  font-size: 20px;
  color: #fff;
}

.photo-info { padding: 8px 10px 10px; }

.photo-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 4px;
}

.photo-tag {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  color: #fff;
}

.photo-desc {
  font-size: 12px;
  color: var(--warm-caramel);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin: 0;
  line-height: 1.4;
}

.photo-time { font-size: 11px; color: #bbb; margin: 3px 0 0; }

</style>