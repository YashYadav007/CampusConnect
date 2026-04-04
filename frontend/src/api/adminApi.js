import api, { unwrapApiResponse } from './axios'

export async function getAdminStats() {
  const res = await api.get('/admin/stats')
  return unwrapApiResponse(res)
}

export async function listAdminUsers() {
  const res = await api.get('/admin/users')
  return unwrapApiResponse(res)
}

export async function deactivateAdminUser(userId) {
  const res = await api.patch(`/admin/users/${userId}/deactivate`)
  return unwrapApiResponse(res)
}

export async function activateAdminUser(userId) {
  const res = await api.patch(`/admin/users/${userId}/activate`)
  return unwrapApiResponse(res)
}

export async function listAdminQuestions() {
  const res = await api.get('/admin/questions')
  return unwrapApiResponse(res)
}

export async function getAdminQuestion(questionId) {
  const res = await api.get(`/admin/questions/${questionId}`)
  return unwrapApiResponse(res)
}

export async function listAdminQuestionAnswers(questionId) {
  const res = await api.get(`/admin/questions/${questionId}/answers`)
  return unwrapApiResponse(res)
}

export async function deleteAdminQuestion(questionId) {
  const res = await api.delete(`/admin/questions/${questionId}`)
  return unwrapApiResponse(res)
}

export async function deleteAdminAnswer(answerId) {
  const res = await api.delete(`/admin/answers/${answerId}`)
  return unwrapApiResponse(res)
}

export async function listAdminLostFound() {
  const res = await api.get('/admin/lost-found')
  return unwrapApiResponse(res)
}

export async function getAdminLostFound(postId) {
  const res = await api.get(`/admin/lost-found/${postId}`)
  return unwrapApiResponse(res)
}

export async function deleteAdminLostFound(postId) {
  const res = await api.delete(`/admin/lost-found/${postId}`)
  return unwrapApiResponse(res)
}

export async function listAdminClaims() {
  const res = await api.get('/admin/claims')
  return unwrapApiResponse(res)
}
