import axios from 'axios'

function resolveApiBaseUrl() {
  const configured = import.meta.env.VITE_API_BASE_URL?.trim()
  if (!configured) {
    return '/api'
  }

  const normalized = configured.replace(/\/+$/, '')
  return normalized.endsWith('/api') ? normalized : `${normalized}/api`
}

const baseURL = resolveApiBaseUrl()

const api = axios.create({
  baseURL,
  timeout: 20000,
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('cc_token')
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export function unwrapApiResponse(res) {
  const payload = res?.data
  if (!payload) throw new Error('No response from server')
  if (payload.success === false) {
    throw new Error(payload.message || 'Request failed')
  }
  return payload.data
}

export function getApiErrorMessage(err) {
  const msg =
    err?.response?.data?.message ||
    err?.response?.data?.error ||
    err?.message ||
    'Something went wrong'
  return typeof msg === 'string' ? msg : 'Something went wrong'
}

export default api
