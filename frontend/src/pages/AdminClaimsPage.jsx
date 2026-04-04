import { useEffect, useState } from 'react'
import { listAdminClaims } from '../api/adminApi'
import { getApiErrorMessage } from '../api/axios'
import { AdminLayout } from '../components/admin/AdminLayout'
import { Alert } from '../components/common/Alert'
import { Badge } from '../components/common/Badge'
import { EmptyState } from '../components/common/EmptyState'
import { Loader } from '../components/common/Loader'
import { formatDateTime } from '../utils/formatDate'

function toneForStatus(status) {
  if (status === 'APPROVED') return 'green'
  if (status === 'REJECTED') return 'red'
  return 'amber'
}

export function AdminClaimsPage() {
  const [loading, setLoading] = useState(true)
  const [err, setErr] = useState('')
  const [claims, setClaims] = useState([])

  useEffect(() => {
    let alive = true
    ;(async () => {
      setLoading(true)
      setErr('')
      try {
        const data = await listAdminClaims()
        if (alive) setClaims(data || [])
      } catch (e) {
        if (alive) setErr(getApiErrorMessage(e))
      } finally {
        if (alive) setLoading(false)
      }
    })()
    return () => {
      alive = false
    }
  }, [])

  return (
    <AdminLayout title="Claims" subtitle="Read-only overview of claim activity across lost & found posts.">
      <div className="grid gap-4">
        {err ? <Alert tone="error" title="Could not load claims">{err}</Alert> : null}

        {loading ? (
          <Loader label="Loading claims" />
        ) : claims.length ? (
          <div className="grid gap-3">
            {claims.map((claim) => (
              <div key={claim.id} className="cc-card p-5">
                <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                  <div>
                    <div className="text-base font-extrabold tracking-tight text-slate-900">{claim.postTitle}</div>
                    <div className="mt-1 text-sm text-slate-600">
                      Claimer: {claim.claimer?.fullName || 'Unknown'} · {formatDateTime(claim.createdAt)}
                    </div>
                    <div className="mt-3 whitespace-pre-wrap text-sm leading-relaxed text-slate-800">{claim.message}</div>
                  </div>
                  <div className="flex flex-col items-start gap-2 sm:items-end">
                    <Badge tone={toneForStatus(claim.status)}>{claim.status}</Badge>
                    <div className="text-xs font-semibold text-slate-500">Claim #{claim.id}</div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <EmptyState title="No claims yet" hint="Claim requests will appear here once found items receive submissions." />
        )}
      </div>
    </AdminLayout>
  )
}
