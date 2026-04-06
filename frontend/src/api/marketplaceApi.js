import api, { unwrapApiResponse } from './axios'

export async function createListing(input) {
  const res = await api.post('/marketplace', input)
  return unwrapApiResponse(res)
}

export async function getMarketplaceItems({ category, status, page = 0, size = 20 } = {}) {
  const params = { page, size }
  if (category) params.category = category
  if (status) params.status = status

  const res = await api.get('/marketplace', { params })
  return unwrapApiResponse(res)
}

export async function getMarketplaceItemById(id) {
  const res = await api.get(`/marketplace/${id}`)
  return unwrapApiResponse(res)
}

export async function getMyMarketplaceListings() {
  const res = await api.get('/marketplace/my-listings')
  return unwrapApiResponse(res)
}

export async function createMarketplaceOrder(id) {
  const res = await api.post(`/marketplace/${id}/create-order`)
  return unwrapApiResponse(res)
}

export async function verifyMarketplacePayment(input) {
  const res = await api.post('/marketplace/payments/verify', input)
  return unwrapApiResponse(res)
}

export async function markMarketplaceItemSold(id) {
  const res = await api.patch(`/marketplace/${id}/mark-sold`)
  return unwrapApiResponse(res)
}
