import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { Loader } from '../common/Loader'

export function ProtectedRoute({ children, requireAdmin = false }) {
  const { isAuthed, isAdmin, bootstrapping } = useAuth()
  const location = useLocation()

  if (bootstrapping) return <Loader label="Checking session" />

  if (!isAuthed) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  if (requireAdmin && !isAdmin) {
    return <Navigate to="/" replace />
  }

  return children
}
