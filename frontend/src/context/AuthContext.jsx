import { useCallback, useEffect, useMemo, useState } from 'react'
import { fetchMe, loginUser } from '../api/authApi'
import { AuthContext } from './authContext'

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('cc_token') || null)
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem('cc_user')
    return raw ? JSON.parse(raw) : null
  })
  const [bootstrapping, setBootstrapping] = useState(true)

  const persist = useCallback((nextToken, nextUser) => {
    if (nextToken) localStorage.setItem('cc_token', nextToken)
    else localStorage.removeItem('cc_token')

    if (nextUser) localStorage.setItem('cc_user', JSON.stringify(nextUser))
    else localStorage.removeItem('cc_user')

    setToken(nextToken)
    setUser(nextUser)
  }, [])

  const logout = useCallback(() => {
    persist(null, null)
  }, [persist])

  const login = useCallback(
    async ({ email, password }) => {
      const data = await loginUser({ email, password })
      // backend returns: { token, user }
      persist(data.token, data.user)
      return data
    },
    [persist]
  )

  const refreshMe = useCallback(async () => {
    if (!token) return null
    const me = await fetchMe()
    persist(token, me)
    return me
  }, [persist, token])

  useEffect(() => {
    let alive = true
    ;(async () => {
      try {
        if (token) {
          await refreshMe()
        }
      } catch {
        if (alive) logout()
      } finally {
        if (alive) setBootstrapping(false)
      }
    })()
    return () => {
      alive = false
    }
  }, [token, refreshMe, logout])

  const value = useMemo(
    () => {
      const roles = Array.isArray(user?.roles) ? user.roles : []
      return {
        token,
        user,
        isAuthed: Boolean(token && user),
        isAdmin: roles.includes('ROLE_ADMIN'),
        bootstrapping,
        login,
        logout,
        refreshMe,
      }
    },
    [token, user, bootstrapping, login, logout, refreshMe]
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
