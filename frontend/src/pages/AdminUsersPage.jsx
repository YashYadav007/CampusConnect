import { useEffect, useState } from 'react'
import { activateAdminUser, deactivateAdminUser, listAdminUsers } from '../api/adminApi'
import { getApiErrorMessage } from '../api/axios'
import { AdminLayout } from '../components/admin/AdminLayout'
import { Alert } from '../components/common/Alert'
import { Badge } from '../components/common/Badge'
import { Button } from '../components/common/Button'
import { EmptyState } from '../components/common/EmptyState'
import { Loader } from '../components/common/Loader'
import { formatDateTime } from '../utils/formatDate'
import { useToast } from '../hooks/useToast'
import { useAuth } from '../hooks/useAuth'

export function AdminUsersPage() {
  const { user: currentUser } = useAuth()
  const { showToast } = useToast()
  const [loading, setLoading] = useState(true)
  const [err, setErr] = useState('')
  const [users, setUsers] = useState([])
  const [actingId, setActingId] = useState(null)

  const load = async () => {
    setLoading(true)
    setErr('')
    try {
      const data = await listAdminUsers()
      setUsers(data || [])
    } catch (e) {
      setErr(getApiErrorMessage(e))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const toggleUser = async (target) => {
    setErr('')
    setActingId(target.id)
    try {
      const updated = target.isActive ? await deactivateAdminUser(target.id) : await activateAdminUser(target.id)
      setUsers((prev) => prev.map((item) => (item.id === updated.id ? updated : item)))
      showToast({
        tone: 'success',
        title: target.isActive ? 'User deactivated' : 'User activated',
        message: updated.email,
      })
    } catch (e) {
      const message = getApiErrorMessage(e)
      setErr(message)
      showToast({ tone: 'error', title: 'User action failed', message })
    } finally {
      setActingId(null)
    }
  }

  return (
    <AdminLayout title="Users" subtitle="View all users and manage account status.">
      <div className="grid gap-4">
        {err ? <Alert tone="error" title="Could not load users">{err}</Alert> : null}

        {loading ? (
          <Loader label="Loading users" />
        ) : users.length ? (
          <div className="cc-card overflow-hidden">
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-slate-200 text-sm">
                <thead className="bg-slate-50 text-left text-slate-600">
                  <tr>
                    <th className="px-4 py-3 font-semibold">User</th>
                    <th className="px-4 py-3 font-semibold">Course</th>
                    <th className="px-4 py-3 font-semibold">Roles</th>
                    <th className="px-4 py-3 font-semibold">Reputation</th>
                    <th className="px-4 py-3 font-semibold">Status</th>
                    <th className="px-4 py-3 font-semibold">Joined</th>
                    <th className="px-4 py-3 font-semibold text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-200/80 bg-white">
                  {users.map((item) => {
                    const isSelf = currentUser?.id === item.id
                    return (
                      <tr key={item.id}>
                        <td className="px-4 py-4 align-top">
                          <div className="font-extrabold text-slate-900">{item.fullName}</div>
                          <div className="mt-1 text-slate-600">{item.email}</div>
                          <div className="mt-1 text-xs font-semibold text-slate-500">Year {item.yearOfStudy || '—'}</div>
                        </td>
                        <td className="px-4 py-4 align-top text-slate-700">{item.course || '—'}</td>
                        <td className="px-4 py-4 align-top">
                          <div className="flex flex-wrap gap-2">
                            {(item.roles || []).map((role) => (
                              <Badge key={role} tone={role === 'ROLE_ADMIN' ? 'indigo' : 'slate'}>
                                {role}
                              </Badge>
                            ))}
                          </div>
                        </td>
                        <td className="px-4 py-4 align-top font-semibold text-slate-800">{item.reputationPoints ?? 0}</td>
                        <td className="px-4 py-4 align-top">
                          <Badge tone={item.isActive ? 'green' : 'red'}>{item.isActive ? 'ACTIVE' : 'INACTIVE'}</Badge>
                        </td>
                        <td className="px-4 py-4 align-top text-slate-600">{formatDateTime(item.createdAt)}</td>
                        <td className="px-4 py-4 align-top text-right">
                          <Button
                            size="sm"
                            variant={item.isActive ? 'danger' : 'secondary'}
                            onClick={() => toggleUser(item)}
                            loading={actingId === item.id}
                            disabled={actingId === item.id || isSelf}
                          >
                            {item.isActive ? 'Deactivate' : 'Activate'}
                          </Button>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          </div>
        ) : (
          <EmptyState title="No users found" hint="User accounts will appear here once people register." />
        )}
      </div>
    </AdminLayout>
  )
}
