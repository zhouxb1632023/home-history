import { defineStore } from 'pinia'

export const useGalleryStore = defineStore('gallery', {
  state: () => ({
    ids: []
  }),
  actions: {
    setIds(list) {
      this.ids = list
    }
  }
})