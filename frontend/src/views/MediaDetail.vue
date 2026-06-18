<template>
  <div class="min-h-screen bg-warm-cream">
    <NavBar />
    <main class="max-w-4xl mx-auto px-4 py-6 relative">
      <button @click="$router.back()" class="text-warm-caramel hover:text-warm-orange mb-4 inline-block text-sm">← 返回</button>

      <div v-if="media" class="bg-white rounded-3xl overflow-hidden border border-warm-peach shadow-lg relative">
        <!-- 左箭头 -->
        <button v-if="prevId" @click="goTo(prevId)"
          class="absolute left-3 top-1/2 -translate-y-1/2 z-10 w-10 h-10 md:w-12 md:h-12 rounded-full bg-white/80 hover:bg-white shadow-lg flex items-center justify-center text-warm-orange text-xl hover:scale-110 transition-all duration-200">
          ‹
        </button>

        <!-- 右箭头 -->
        <button v-if="nextId" @click="goTo(nextId)"
          class="absolute right-3 top-1/2 -translate-y-1/2 z-10 w-10 h-10 md:w-12 md:h-12 rounded-full bg-white/80 hover:bg-white shadow-lg flex items-center justify-center text-warm-orange text-xl hover:scale-110 transition-all duration-200">
          ›
        </button>

        <div v-if="media.mediaType === 'PHOTO'" class="flex items-center justify-center bg-gray-50 min-h-[50vh]">
          <img :src="`/api/media/${media.id}/file`" :alt="media.description || ''" class="max-w-full max-h-[70vh] object-contain" />
        </div>
        <div v-else class="bg-black">
          <video :src="`/api/media/${media.id}/file`" controls class="w-full max-h-[70vh]" />
        </div>

        <!-- 导航指示器 -->
        <div v-if="galleryTotal > 1" class="absolute bottom-3 left-1/2 -translate-x-1/2 bg-black/50 text-white text-xs px-3 py-1 rounded-full">
          {{ currentPos }} / {{ galleryTotal }}
        </div>

        <div class="p-6">
          <h3 class="text-lg font-medium text-warm-caramel mb-3">📑 说明</h3>
          <p class="text-warm-caramel leading-relaxed" v-if="media.description">{{ media.description }}</p>
          <p class="text-gray-400 italic" v-else>暂无说明</p>
          <div class="mt-4 flex flex-wrap gap-2">
            <span v-for="tag in media.tags" :key="tag.id" class="px-3 py-1 rounded-full text-white text-sm" :style="{ backgroundColor: tag.color }">{{ tag.name }}</span>
          </div>
          <div class="mt-4 pt-4 border-t border-warm-cream flex flex-wrap justify-between text-sm text-gray-400 gap-2">
            <span>📮 {{ formatDate(media.takenAt) }}</span>
            <span>👁 {{ media.uploaderName }}</span>
            <span>📝 {{ formatSize(media.fileSize) }}</span>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../utils/api'
import { useGalleryStore } from '../stores/gallery'
import NavBar from '../components/NavBar.vue'

const route = useRoute()
const router = useRouter()
const gallery = useGalleryStore()
const media = ref(null)

const prevId = computed(() => {
  const idx = gallery.ids.indexOf(Number(route.params.id))
  return idx > 0 ? gallery.ids[idx - 1] : null
})
const nextId = computed(() => {
  const idx = gallery.ids.indexOf(Number(route.params.id))
  return idx >= 0 && idx < gallery.ids.length - 1 ? gallery.ids[idx + 1] : null
})
const currentPos = computed(() => gallery.ids.indexOf(Number(route.params.id)) + 1)
const galleryTotal = computed(() => gallery.ids.length)

function goTo(id) {
  router.replace(`/media/${id}`)
}

function formatDate(d) { return d ? new Date(d).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' }) : '' }
function formatSize(b) { return b ? (b / (1024 * 1024)).toFixed(1) + ' MB' : '' }

async function loadMedia(id) {
  media.value = null
  try {
    const { data } = await api.get(`/media/${id}`)
    media.value = data
  } catch (e) {
    console.error('加载详情失败', e)
  }
}

onMounted(() => loadMedia(route.params.id))

watch(() => route.params.id, (newId) => loadMedia(newId))
</script>