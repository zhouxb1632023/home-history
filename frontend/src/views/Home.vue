<template>
  <div class="min-h-screen bg-warm-cream">
    <NavBar />
    <main class="max-w-5xl mx-auto px-4 py-6">
      <div class="mb-6 text-center">
        <h2 class="text-2xl font-bold text-warm-orange" style="font-family: serif;">{{ greeting }}</h2>
        <p class="text-warm-caramel text-sm mt-1">{{ todayStr }}</p>
      </div>

      <div class="flex gap-3 mb-8 flex-wrap items-center justify-center">
        <input type="date" v-model="startDate"
          class="px-3 py-2 rounded-xl border border-warm-peach bg-white text-sm text-warm-caramel focus:outline-none focus:ring-2 focus:ring-warm-orange" />
        <span class="text-warm-caramel text-sm">至</span>
        <input type="date" v-model="endDate"
          class="px-3 py-2 rounded-xl border border-warm-peach bg-white text-sm text-warm-caramel focus:outline-none focus:ring-2 focus:ring-warm-orange" />
        <select v-model="filterTag" class="px-3 py-2 rounded-xl border border-warm-peach bg-white text-sm text-warm-caramel">
          <option value="">全部人物</option>
          <option v-for="tag in tags" :key="tag.id" :value="tag.id">{{ tag.name }}</option>
        </select>
        <input v-model="keyword" placeholder="搜索..." @keyup.enter="search"
          class="px-3 py-2 rounded-xl border border-warm-peach bg-white text-sm text-warm-caramel w-32 focus:outline-none focus:ring-2 focus:ring-warm-orange" />
        <button @click="search" class="px-4 py-2 bg-warm-orange text-white rounded-xl text-sm hover:bg-orange-600 transition-colors">🔍</button>
      </div>

      <div v-if="loading" class="text-center py-20 text-warm-caramel">加载中...</div>
      <div v-else-if="groupedDays.length === 0" class="text-center py-20">
        <div class="text-6xl mb-4">📲</div>
        <p class="text-warm-caramel text-lg">还没有照片，去上传第一张吧！</p>
        <router-link to="/upload" class="mt-4 inline-block px-6 py-2 bg-warm-orange text-white rounded-xl hover:bg-orange-600 transition-colors">📷 上传照片</router-link>
      </div>

      <div v-else class="relative">
        <div class="absolute left-4 md:left-8 top-0 bottom-0 w-0.5 bg-warm-peach"></div>

        <div v-for="(day, di) in groupedDays" :key="day.date" class="relative pb-10 last:pb-0">
          <div class="flex items-center gap-4 md:gap-8 mb-5">
            <div class="relative z-10 flex-shrink-0 w-8 h-8 md:w-16 md:h-16 rounded-full bg-warm-orange text-white flex flex-col items-center justify-center shadow-md"
              :class="di === 0 ? 'ring-4 ring-warm-yellow/40' : ''">
              <span class="text-xs md:text-base font-bold leading-none">{{ day.dayNum }}</span>
              <span class="text-[10px] md:text-xs leading-none mt-0.5">{{ day.monthStr }}</span>
            </div>
            <div class="text-left">
              <p class="text-sm md:text-base font-medium text-warm-caramel">{{ day.weekday }}</p>
              <p class="text-xs text-gray-400">{{ day.date }} · {{ day.photos.length }} 张</p>
            </div>
          </div>

          <div class="ml-12 md:ml-24 grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3 md:gap-4">
            <PhotoCard v-for="media in day.photos" :key="media.id" :media="media" />
          </div>
        </div>
      </div>

      <div v-if="hasMore && !loading" class="text-center mt-8">
        <button @click="loadMore"
          class="px-6 py-2.5 border-2 border-dashed border-warm-orange text-warm-orange rounded-xl hover:bg-warm-yellow/30 transition-all text-sm font-medium">
          加载更多
        </button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import api from '../utils/api'
import { useGalleryStore } from '../stores/gallery'
import NavBar from '../components/NavBar.vue'
import PhotoCard from '../components/PhotoCard.vue'

const gallery = useGalleryStore()
const allPhotos = ref([])
const tags = ref([])
const loading = ref(true)
const hasMore = ref(false)
const page = ref(0)
const startDate = ref('')
const endDate = ref('')
const filterTag = ref('')
const keyword = ref('')
const pageSize = 200

function syncGallery() {
  gallery.setIds(allPhotos.value.map(p => p.id))
}

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了，看看今天的回忆吧 🌙'
  if (h < 12) return '早上好，新的一天 ☀️'
  if (h < 18) return '午后时光，翻翻相册 📉'
  return '傍晚好，温暖的回忆时光 🍵'
})
const todayStr = computed(() => new Date().toLocaleDateString('zh-CN', {
  year: 'numeric', month: 'long', day: 'numeric', weekday: 'long'
}))

function formatDateGroup(d) {
  const date = new Date(d)
  const dayNum = date.getDate()
  const monthStr = (date.getMonth() + 1) + '月'
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const weekday = weekdays[date.getDay()]
  return { dayNum, monthStr, weekday }
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

async function search() {
  loading.value = true
  page.value = 0
  allPhotos.value = []
  await doFetch()
  loading.value = false
}

async function loadMore() {
  page.value++
  await doFetch()
}

async function doFetch() {
  const params = { page: page.value, size: pageSize }
  if (startDate.value) params.startDate = startDate.value
  if (endDate.value) params.endDate = endDate.value
  if (filterTag.value) params.tagId = filterTag.value
  if (keyword.value) params.keyword = keyword.value
  try {
    const { data } = await api.get('/media', { params })
    allPhotos.value.push(...data.content)
    syncGallery()
    hasMore.value = !data.last
  } catch (e) {
    console.error('加载照片失败', e)
  }
}

onMounted(async () => {
  try {
    const { data } = await api.get('/tags')
    tags.value = data
  } catch (e) {
    console.error('加载标签失败', e)
  }
  await search()
})
</script>