import api, { unwrapApiResponse } from './axios'

export async function listLostFound({ type, status, page = 0, size = 20 } = {}) {
  const params = { page, size }
  if (type) params.type = type
  if (status) params.status = status

  const res = await api.get('/lost-found', { params })
  return unwrapApiResponse(res)
}

export async function getLostFound(id) {
  const res = await api.get(`/lost-found/${id}`)
  return unwrapApiResponse(res)
}

export async function createLostFound(input) {
  const res = await api.post('/lost-found', input)
  return unwrapApiResponse(res)
}
