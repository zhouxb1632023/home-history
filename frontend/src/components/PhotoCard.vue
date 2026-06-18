<template>
  <div
    class="bg-white rounded-2xl overflow-hidden shadow-md hover:shadow-xl transition-all duration-300 transform hover:-translate-y-1 cursor-pointer border border-warm-cream"
    @click="$router.push(`/media/${media.id}`)"
  >
    <div class="aspect-square overflow-hidden bg-warm-cream relative">
      <img
        :src="`/api/media/${media.id}/thumbnail`"
        :alt="media.description || ''"
        class="w-full h-full object-cover"
        loading="lazy"
      />
      <div v-if="media.mediaType === 'VIDEO'" class="absolute inset-0 flex items-center justify-center">
        <div class="w-10 h-10 bg-black/40 rounded-full flex items-center justify-center text-white text-lg">▶</div>
      </div>
    </div>
    <div class="p-3">
      <div class="flex flex-wrap gap-1 mb-2">
        <span v-for="tag in media.tags" :key="tag.id"
          class="px-2 py-0.5 text-xs rounded-full text-white"
          :style="{ backgroundColor: tag.color }">
          {{ tag.name }}
        </span>
      </div>
      <p class="text-sm text-warm-caramel line-clamp-2" v-if="media.description">{{ media.description }}</p>
      <p class="text-xs text-gray-400 mt-1">{{ formatDate(media.takenAt) }}</p>
    </div>
  </div>
</template>

<script setup>
defineProps(['media'])
function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN', { month: 'long', day: 'numeric' })
}
</script>
