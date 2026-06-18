import { API_BASE } from './env'

export function mediaFileUrl(id) {
  return `${API_BASE}/media/${id}/file`
}

export function mediaThumbnailUrl(id) {
  return `${API_BASE}/media/${id}/thumbnail`
}

// 颜色常量
export const COLORS = {
  warmOrange: '#E07B4C',
  warmCream: '#FFF8F0',
  warmCaramel: '#8B6B5A',
  warmPeach: '#FFD4B8',
  warmYellow: '#FFECD2',
}

export function parseColor(hex) {
  const h = hex.replace('#', '')
  return h
}
