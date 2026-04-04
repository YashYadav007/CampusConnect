import api, { unwrapApiResponse } from './axios'

export async function listQuestions({ page = 0, size = 20 } = {}) {
  const res = await api.get('/questions', { params: { page, size } })
  return unwrapApiResponse(res)
}

export async function searchQuestions({ keyword, page = 0, size = 20 } = {}) {
  const res = await api.get('/questions/search', { params: { keyword, page, size } })
  return unwrapApiResponse(res)
}

export async function getQuestion(id) {
  const res = await api.get(`/questions/${id}`)
  return unwrapApiResponse(res)
}

export async function askQuestion(input) {
  const res = await api.post('/questions', input)
  return unwrapApiResponse(res)
}

export async function addAnswer(questionId, input) {
  const res = await api.post(`/questions/${questionId}/answers`, input)
  return unwrapApiResponse(res)
}

export async function listAnswers(questionId) {
  const res = await api.get(`/questions/${questionId}/answers`)
  return unwrapApiResponse(res)
}

export async function voteAnswer(answerId, input) {
  const res = await api.post(`/answers/${answerId}/vote`, input)
  return unwrapApiResponse(res)
}

export async function acceptAnswer(answerId) {
  const res = await api.post(`/answers/${answerId}/accept`)
  return unwrapApiResponse(res)
}
