import api, { unwrapApiResponse } from './axios'

export async function registerUser(input) {
  const res = await api.post('/auth/register', input)
  return unwrapApiResponse(res)
}

export async function loginUser(input) {
  const res = await api.post('/auth/login', input)
  return unwrapApiResponse(res)
}

export async function fetchMe() {
  const res = await api.get('/auth/me')
  return unwrapApiResponse(res)
}
