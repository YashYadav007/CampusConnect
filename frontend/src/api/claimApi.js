import api, { unwrapApiResponse } from './axios'

export async function createClaim(postId, input) {
  const res = await api.post(`/lost-found/${postId}/claim`, input)
  return unwrapApiResponse(res)
}

export async function listClaimsForPost(postId) {
  const res = await api.get(`/lost-found/${postId}/claims`)
  return unwrapApiResponse(res)
}

export async function approveClaim(claimId) {
  const res = await api.post(`/claims/${claimId}/approve`)
  return unwrapApiResponse(res)
}

export async function rejectClaim(claimId) {
  const res = await api.post(`/claims/${claimId}/reject`)
  return unwrapApiResponse(res)
}
