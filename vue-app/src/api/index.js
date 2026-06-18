import http from './http'

export async function login(username, password) {
  const res = await http.post('/auth/login', { username, password })
  return res.data
}

export async function register(username, password, nickname) {
  const res = await http.post('/auth/register', { username, password, nickname })
  return res.data
}

export async function logout() {
  await http.post('/auth/logout')
}

export async function updateProfile(nickname) {
  const res = await http.put('/auth/profile', { nickname })
  return res.data
}

export async function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  const res = await http.post('/auth/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return res.data
}

export async function getTags() {
  const res = await http.get('/tags')
  return res.data
}

export async function searchMedia(params = {}) {
  const res = await http.get('/media', { params })
  return res.data
}

export async function uploadMedia(formData) {
  const res = await http.post('/media/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return res.data
}

export async function getMediaDetail(id) {
  const res = await http.get(`/media/${id}`)
  return res.data
}