<template>
  <div class="min-h-screen bg-warm-cream">
    <NavBar />
    <main class="max-w-5xl mx-auto px-4 py-6">
      <h2 class="text-2xl font-bold text-warm-orange mb-6 text-center" style="font-family: serif;">👨‍👩‍👧 人物</h2>
      <div class="flex flex-wrap gap-3 justify-center mb-8">
        <button v-for="tag in tags" :key="tag.id" @click="selectTag(selectedTag === tag.id ? null : tag.id)"
          :class="selectedTag === tag.id ? 'ring-2 ring-offset-2 scale-110' : ''"
          class="px-5 py-2.5 rounded-full text-white font-medium shadow-md hover:shadow-lg transition-all duration-200"
          :style="{ backgroundColor: tag.color }">{{ tag.name }}</button>
        <button @click="showCreate = true"
          class="px-5 py-2.5 rounded-full border-2 border-dashed border-warm-orange text-warm-orange font-medium hover:bg-warm-yellow/30 transition-all duration-200">
          + 新建
        </button>
      </div>

      <div v-if="loading" class="text-center py-20 text-warm-caramel">加载中...</div>
      <div v-else-if="selectedTag && groupedDays.length === 0" class="text-center py-16 text-warm-caramel">
        <div class="text-6xl mb-4">📷</div>
        <p>该人物还没有照片</p>
      </div>
      <div v-else-if="!selectedTag" class="text-center py-16 text-warm-caramel">
        <div class="text-6xl mb-4">🏷</div>
        <p>点击一个标签，查看对应人物的照片</p>
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

      <Teleport to="body">
        <div v-if="showCreate" class="fixed inset-0 bg-black/40 flex items-center justify-center z-50" @click.self="showCreate = false">
          <div class="bg-white rounded-2xl p-6 w-full max-w-sm mx-4 shadow-xl">
            <h3 class="text-lg font-bold text-warm-caramel mb-4">创建人物标签</h3>
            <div class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-warm-caramel mb-1">姓名</label>
                <input v-model="newTagName" placeholder="输入人物姓名"
                  class="w-full px-4 py-2.5 rounded-xl border border-warm-peach bg-warm-cream focus:outline-none focus:ring-2 focus:ring-warm-orange text-warm-caramel" />
              </div>
              <div>
                <label class="block text-sm font-medium text-warm-caramel mb-1">颜色</label>
                <div class="flex gap-2">
                  <button v-for="c in presetColors" :key="c" @click="newTagColor = c"
                    :class="newTagColor === c ? 'ring-2 ring-offset-2 ring-warm-orange' : ''"
                    class="w-8 h-8 rounded-full transition-all duration-200"
                    :style="{ backgroundColor: c }"></button>
                </div>
              </div>
              <div class="flex gap-3 pt-2">
                <button @click="createTag" :disabled="!newTagName.trim() || creating"
                  class="flex-1 py-2.5 bg-warm-orange text-white rounded-xl font-medium hover:bg-orange-600 disabled:opacity-50 transition-all">
                  {{ creating ? '创建中...' : '确认创建' }}
                </button>
                <button @click="showCreate = false"
                  class="flex-1 py-2.5 border border-warm-peach text-warm-caramel rounded-xl hover:bg-gray-50 transition-all">
                  取消
                </button>
              </div>
            </div>
          </div>
        </div>
      </Teleport>
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
const tags = ref([])
const selectedTag = ref(null)
const allPhotos = ref([])
const loading = ref(false)

const showCreate = ref(false)
const newTagName = ref('')
const newTagColor = ref('#E07B4C')
const creating = ref(false)
const presetColors = ['#E07B4C', '#4C9A8A', '#5B7FAC', '#9B6DA8', '#C44D6A', '#3A8C6E', '#D4A843', '#6B7FA3']

function syncGallery() {
  gallery.setIds(allPhotos.value.map(p => p.id))
}

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

async function selectTag(tagId) {
  selectedTag.value = tagId
  if (!tagId) {
    allPhotos.value = []
    syncGallery()
    return
  }
  loading.value = true
  try {
    const { data } = await api.get('/media', { params: { tagId, size: 200 } })
    allPhotos.value = data.content
    syncGallery()
  } catch (e) {
    console.error('加载照片失败', e)
  } finally {
    loading.value = false
  }
}

async function createTag() {
  if (!newTagName.value.trim()) return
  creating.value = true
  try {
    const { data } = await api.post('/tags', { name: newTagName.value.trim(), color: newTagColor.value })
    tags.value.push(data)
    newTagName.value = ''
    newTagColor.value = '#E07B4C'
    showCreate.value = false
  } catch (e) {
    alert('创建失败: ' + (e.response?.data?.message || e.message))
  } finally {
    creating.value = false
  }
}

onMounted(async () => {
  try {
    const { data } = await api.get('/tags')
    tags.value = data
  } catch (e) {
    console.error('加载人物标签失败', e)
  }
})
</script>